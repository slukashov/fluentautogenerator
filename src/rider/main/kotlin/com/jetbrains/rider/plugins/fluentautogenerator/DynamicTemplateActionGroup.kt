package com.jetbrains.rider.plugins.fluentautogenerator

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.jetbrains.rider.plugins.fluentautogenerator.settings.FluentGeneratorSettingsState

class DynamicTemplateActionGroup : ActionGroup() {
    
    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val settings = FluentGeneratorSettingsState.instance
        return settings.customTemplates.map { template ->
            GenerateFromCustomTemplateAction(template) as AnAction
        }.toTypedArray()
    }
}