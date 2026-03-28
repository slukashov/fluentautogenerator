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
    
    // NEW: Add the text field for the default SQL directory
    private val defaultSqlDirectoryField = JBTextField()

    // Template UI Components
    private val listModel = DefaultListModel<CustomTemplate>()
    private val templateList = JBList(listModel)
    private val templateNameField = JBTextField()
    private val templatePrefixField = JBTextField()
    private val templateContentArea = JTextArea(15, 40).apply {
        font = java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12)
    }

    private var isUpdatingFromList = false

    override fun getDisplayName(): String = "FluentMigrator Generator"

    override fun createComponent(): JComponent? {
        templateList.setCellRenderer { _, value, _, _, _ ->
            JBLabel(value.menuName)
        }

        val editorPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent("Menu Name:", templateNameField)
            .addLabeledComponent("File Prefix:", templatePrefixField)
            .addLabeledComponent("Template Content:", JBScrollPane(templateContentArea))
            .panel

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

        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listPanel, editorPanel)
        splitPane.dividerLocation = 200

        val docListener = object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = saveEditsToObject()
            override fun removeUpdate(e: DocumentEvent?) = saveEditsToObject()
            override fun changedUpdate(e: DocumentEvent?) = saveEditsToObject()
        }
        templateNameField.document.addDocumentListener(docListener)
        templatePrefixField.document.addDocumentListener(docListener)
        templateContentArea.document.addDocumentListener(docListener)

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
            // NEW: Add the directory field to the top of the UI form
            .addLabeledComponent("Default SQL directory:", defaultSqlDirectoryField) 
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
        templateList.repaint()
    }

    private fun clearEditor() {
        isUpdatingFromList = true
        templateNameField.text = ""
        templatePrefixField.text = ""
        templateContentArea.text = ""
        isUpdatingFromList = false
    }

    override fun isModified(): Boolean = true

    override fun apply() {
        val settings = FluentGeneratorSettingsState.instance
        settings.possibleTags = tagsField.text
        settings.insertTagsAsStrings = asStringsCheckbox.isSelected
        
        // NEW: Save the directory name to the state
        settings.sqlFolderName = defaultSqlDirectoryField.text 
        
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
        
        // NEW: Load the directory name into the UI text box
        defaultSqlDirectoryField.text = settings.sqlFolderName
        
        listModel.clear()
        settings.customTemplates.forEach {
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