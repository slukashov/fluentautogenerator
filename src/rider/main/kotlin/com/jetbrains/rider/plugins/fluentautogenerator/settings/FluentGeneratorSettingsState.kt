package com.jetbrains.rider.plugins.fluentautogenerator.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

// 1. Define what a Custom Template is. 
// It needs a Name (for the menu button), a File Prefix, and the Content.
data class CustomTemplate(
    var menuName: String = "Custom Table",
    var filePrefix: String = "Custom",
    var content: String = """
using System;
using FluentMigrator;

namespace {Namespace}
{
    [Migration({Timestamp})]{Tags}
    public class {Prefix}_{ClassName} : Migration
    {
        public override void Up() { }
        public override void Down() { }
    }
}
    """.trimIndent()
)

@State(
    name = "com.jetbrains.rider.plugins.fluentautogenerator.FluentGeneratorSettingsState",
    storages = [Storage("FluentAutoGeneratorSettings.xml")]
)
class FluentGeneratorSettingsState : PersistentStateComponent<FluentGeneratorSettingsState> {
    
    var possibleTags: String = "Development, Production, Staging, UK, US"
    var insertTagsAsStrings: Boolean = true
    var sqlFolderName: String = "Sql"
    
    // 2. Store a MutableList of templates!
    var customTemplates: MutableList<CustomTemplate> = mutableListOf(
        CustomTemplate("Create Basic Table", "CreateTable", "/* default create template */"),
        CustomTemplate("Alter Existing Table", "AlterTable", "/* default alter template */")
    )

    override fun getState(): FluentGeneratorSettingsState = this

    override fun loadState(state: FluentGeneratorSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: FluentGeneratorSettingsState
            get() = ApplicationManager.getApplication().getService(FluentGeneratorSettingsState::class.java)
    }
}