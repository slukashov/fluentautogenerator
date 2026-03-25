package com.jetbrains.rider.plugins.fluentautogenerator.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.*
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class FluentGeneratorSettingsConfigurable : Configurable {
    private var myMainPanel: JPanel? = null
    
    // Global Settings
    private val tagsField = JBTextField()
    private val asStringsCheckbox = JBCheckBox("Insert tags as strings (e.g. \"Dev\") instead of raw")

    // Template UI Components
    private val listModel = DefaultListModel<CustomTemplate>()
    private val templateList = JBList(listModel)
    private val templateNameField = JBTextField()
    private val templatePrefixField = JBTextField()
    private val templateContentArea = JTextArea(15, 40).apply {
        font = java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12)
    }

    // Flag to prevent recursive updates when clicking the list
    private var isUpdatingFromList = false

    override fun getDisplayName(): String = "FluentMigrator Generator"

    override fun createComponent(): JComponent? {
        // 1. Setup the List display
        templateList.setCellRenderer { _, value, _, _, _ ->
            JBLabel(value.menuName)
        }

        // 2. Build the Editor Panel (Right Side)
        val editorPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Menu Name:", templateNameField)
            .addLabeledComponent("File Prefix:", templatePrefixField)
            .addLabeledComponent("Template Content:", JBScrollPane(templateContentArea))
            .panel

        // 3. Build the List Panel with Add/Remove buttons (Left Side)
        val addBtn = JButton("Add").apply {
            addActionListener {
                val newTemplate = CustomTemplate("New Template", "Custom", "/* your code */")
                listModel.addElement(newTemplate)
                templateList.selectedIndex = listModel.size() - 1
            }
        }
        val removeBtn = JButton("Remove").apply {
            addActionListener {
                val idx = templateList.selectedIndex
                if (idx >= 0) {
                    listModel.remove(idx)
                    if (listModel.size() > 0) {
                        templateList.selectedIndex = maxOf(0, idx - 1)
                    } else {
                        clearEditor()
                    }
                }
            }
        }
        val btnPanel = JPanel(GridLayout(1, 2)).apply {
            add(addBtn)
            add(removeBtn)
        }
        val listPanel = JPanel(BorderLayout()).apply {
            add(JBScrollPane(templateList), BorderLayout.CENTER)
            add(btnPanel, BorderLayout.SOUTH)
        }

        // 4. Combine them into a Split Pane
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, editorPanel)
        splitPane.dividerLocation = 200 // Width of the list

        // 5. Listeners to save text changes into the selected object
        val docListener = object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = saveEditsToObject()
            override fun removeUpdate(e: DocumentEvent?) = saveEditsToObject()
            override fun changedUpdate(e: DocumentEvent?) = saveEditsToObject()
        }
        templateNameField.document.addDocumentListener(docListener)
        templatePrefixField.document.addDocumentListener(docListener)
        templateContentArea.document.addDocumentListener(docListener)

        // 6. Listener to load data into the editor when a list item is clicked
        templateList.addListSelectionListener {
            val selected = templateList.selectedValue
            if (selected != null) {
                isUpdatingFromList = true
                templateNameField.text = selected.menuName
                templatePrefixField.text = selected.filePrefix
                templateContentArea.text = selected.content
                isUpdatingFromList = false
            }
        }

        // 7. Put it all together in the main window
        myMainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Available tags (comma-separated): "), tagsField, 1, false)
            .addComponent(asStringsCheckbox, 1)
            .addSeparator()
            .addComponent(JBLabel("Dynamic Templates (Placeholders: {Namespace}, {Timestamp}, {ClassName}, {BranchName}, {Tags})"))
            .addComponent(splitPane)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        return myMainPanel
    }

    private fun saveEditsToObject() {
        if (isUpdatingFromList) return
        val selected = templateList.selectedValue ?: return
        selected.menuName = templateNameField.text
        selected.filePrefix = templatePrefixField.text
        selected.content = templateContentArea.text
        templateList.repaint() // Updates the name in the left-hand list visually
    }

    private fun clearEditor() {
        isUpdatingFromList = true
        templateNameField.text = ""
        templatePrefixField.text = ""
        templateContentArea.text = ""
        isUpdatingFromList = false
    }

    override fun isModified(): Boolean = true // Always allow applying for complex list edits

    override fun apply() {
        val settings = FluentGeneratorSettingsState.instance
        settings.possibleTags = tagsField.text
        settings.insertTagsAsStrings = asStringsCheckbox.isSelected
        
        // Save the list model back to the persistent settings
        val newList = mutableListOf<CustomTemplate>()
        for (i in 0 until listModel.size()) {
            newList.add(listModel.getElementAt(i))
        }
        settings.customTemplates = newList
    }

    override fun reset() {
        val settings = FluentGeneratorSettingsState.instance
        tagsField.text = settings.possibleTags
        asStringsCheckbox.isSelected = settings.insertTagsAsStrings
        
        // Load the saved templates into the UI
        listModel.clear()
        settings.customTemplates.forEach {
            // We create a copy so canceling doesn't save accidental edits
            listModel.addElement(CustomTemplate(it.menuName, it.filePrefix, it.content))
        }
        if (listModel.size() > 0) {
            templateList.selectedIndex = 0
        } else {
            clearEditor()
        }
    }

    override fun disposeUIResources() {
        myMainPanel = null
    }
}