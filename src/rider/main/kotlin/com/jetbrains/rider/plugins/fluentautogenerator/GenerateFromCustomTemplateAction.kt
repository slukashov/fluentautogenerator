package com.jetbrains.rider.plugins.fluentautogenerator

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rider.plugins.fluentautogenerator.settings.CustomTemplate

class GenerateFromCustomTemplateAction(private val template: CustomTemplate) : BaseMigrationAction() {
    
    init {
        templatePresentation.text = template.menuName
        templatePresentation.icon = com.intellij.icons.AllIcons.Nodes.Class 
    }

    override fun getDialogTitle() = "Generate: ${template.menuName}"
    override fun getFileExtension() = "cs"

    override fun  getMigrationTemplate(namespace: String,
                      timestamp: String,
                      className: String, 
                      branchName: String,
                      tagsAttribute: String): String {
        return template.content
            .replace("{Namespace}", namespace)
            .replace("{Timestamp}", timestamp)
            .replace("{ClassName}", className)
            .replace("{BranchName}", branchName)
            .replace("{Tags}", tagsAttribute)
            .replace("{Prefix}", template.filePrefix)
    }
}