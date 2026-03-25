package com.jetbrains.rider.plugins.fluentautogenerator

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBTextField
import com.intellij.ui.ScrollPaneFactory
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.ListSelectionModel

class MigrationInputDialog(
    project: Project,
    dialogTitle: String,
    defaultName: String,
    private val availableTags: List<String>
) : DialogWrapper(project) {

    private val nameField = JBTextField(defaultName)
    
    // NEW: Create a JBList instead of Checkboxes
    private val tagsList = JBList(availableTags).apply {
        selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        visibleRowCount = 5 // Shows 5 items before needing to scroll
    }

    init {
        title = dialogTitle
        init()
    }

    override fun createCenterPanel(): JComponent {
        val builder = FormBuilder.createFormBuilder()
            .addLabeledComponent("Migration name:", nameField)

        if (availableTags.isNotEmpty()) {
            // Wrap the list in a scroll pane so it doesn't break the UI if there are 20 tags
            val scrollPane = ScrollPaneFactory.createScrollPane(tagsList)
            builder.addLabeledComponent("Select tags (Ctrl/Cmd + Click):", scrollPane)
        }

        return builder.panel
    }

    fun getClassName(): String = nameField.text.trim()
    
    // NEW: Grab the selected items straight from the list
    fun getSelectedTags(): List<String> = tagsList.selectedValuesList
}