package com.jetbrains.rider.plugins.fluentautogenerator.settings

import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import javax.swing.JPanel

/**
 * Handles the visual layout of the Settings page.
 */
class FluentGeneratorSettingsComponent {

    // Creates a standard JetBrains text input field
    private val sqlFolderTextField = JBTextField()

    // We build the UI panel using the Kotlin UI DSL
    val panel: JPanel = panel {
        row("Default SQL folder name:") {
            cell(sqlFolderTextField)
                .comment("The directory where Alt+Enter will generate new migration files.")
        }
    }

    // A helper property to easily get/set the text from our Configurable
    var sqlFolderName: String
        get() = sqlFolderTextField.text
        set(value) {
            sqlFolderTextField.text = value
        }
}