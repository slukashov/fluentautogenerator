package com.jetbrains.rider.plugins.fluentautogenerator

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import com.intellij.ui.ScrollPaneFactory
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JTextArea
import javax.swing.ListSelectionModel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class MigrationInputDialog(
    project: Project,
    dialogTitle: String,
    defaultName: String,
    private val availableTags: List<String>
) : DialogWrapper(project) {

    private val nameField = JBTextField(defaultName)
    private val tagsList = JBList(availableTags).apply {
        selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        visibleRowCount = 4 
    }
    
    // NEW: A read-only text area for the live C# preview
    private val previewArea = JTextArea(8, 40).apply {
        isEditable = false
        font = java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12)
        background = com.intellij.util.ui.UIUtil.getPanelBackground()
    }

    init {
        title = dialogTitle
        init()
        
        // NEW: Listen to keystrokes and clicks to update the preview instantly
        nameField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) = updatePreviewText()
            override fun removeUpdate(e: DocumentEvent?) = updatePreviewText()
            override fun changedUpdate(e: DocumentEvent?) = updatePreviewText()
        })
        tagsList.addListSelectionListener { updatePreviewText() }
        
        updatePreviewText() // Run once on startup
    }

    override fun doValidate(): ValidationInfo? {
        val text = nameField.text.trim()
        if (text.isEmpty()) return ValidationInfo("Name cannot be empty", nameField)
        if (text.contains(" ")) return ValidationInfo("Name cannot contain spaces", nameField)
        
        // Regex ensures it starts with a letter/underscore, and only contains valid C# characters
        if (!text.matches(Regex("^[a-zA-Z_][a-zA-Z0-9_]*$"))) {
            return ValidationInfo("Must be a valid C# class name (no special characters or leading numbers)", nameField)
        }
        return null
    }

    override fun createCenterPanel(): JComponent {
        val builder = FormBuilder.createFormBuilder()
            .addLabeledComponent("Migration name:", nameField)

        if (availableTags.isNotEmpty()) {
            val scrollPane = ScrollPaneFactory.createScrollPane(tagsList)
            builder.addLabeledComponent("Select tags (Ctrl/Cmd + Click):", scrollPane)
        }
        
        // Add the preview area to the bottom of the window
        builder.addSeparator()
        builder.addLabeledComponent("C# Preview:", ScrollPaneFactory.createScrollPane(previewArea))

        return builder.panel
    }

    // NEW: Feature 2 - Live Code Preview!
    private fun updatePreviewText() {
        val className = nameField.text.trim().ifEmpty { "YourNameHere" }
        val tags = tagsList.selectedValuesList
        
        // We do a quick format just for the preview
        val tagsStr = if (tags.isNotEmpty()) ", Tags(${tags.joinToString(", ") { "\"$it\"" }})" else ""
        
        val content = """
[Migration(20261231235959)$tagsStr]
public class $className : Migration
{
    public override void Up()
    {
        // ...
    }
}
        """.trimIndent()
        previewArea.text = content
    }

    fun getClassName(): String = nameField.text.trim()
    fun getSelectedTags(): List<String> = tagsList.selectedValuesList
}