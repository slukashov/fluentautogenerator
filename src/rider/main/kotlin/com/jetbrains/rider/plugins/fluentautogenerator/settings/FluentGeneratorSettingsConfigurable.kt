package com.yourname.fluentautogenerator.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class FluentGeneratorSettingsConfigurable : Configurable {
    private var myMainPanel: JPanel? = null
    private val tagsField = JBTextField()
    
    // NEW: Checkbox for the UI
    private val asStringsCheckbox = JBCheckBox("Insert tags as strings (e.g. \"Dev\") instead of raw (e.g. Dev)")

    override fun getDisplayName(): String = "FluentMigrator Generator"

    override fun createComponent(): JComponent? {
        myMainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Available tags (comma-separated): "), tagsField, 1, false)
            .addComponent(asStringsCheckbox, 1) // Add the checkbox to the menu
            .addComponentFillVertically(JPanel(), 0)
            .panel
        return myMainPanel
    }

    override fun isModified(): Boolean {
        val settings = FluentGeneratorSettingsState.instance
        return tagsField.text != settings.possibleTags || 
               asStringsCheckbox.isSelected != settings.insertTagsAsStrings
    }

    override fun apply() {
        val settings = FluentGeneratorSettingsState.instance
        settings.possibleTags = tagsField.text
        settings.insertTagsAsStrings = asStringsCheckbox.isSelected
    }

    override fun reset() {
        val settings = FluentGeneratorSettingsState.instance
        tagsField.text = settings.possibleTags
        asStringsCheckbox.isSelected = settings.insertTagsAsStrings
    }

    override fun disposeUIResources() {
        myMainPanel = null
    }
}