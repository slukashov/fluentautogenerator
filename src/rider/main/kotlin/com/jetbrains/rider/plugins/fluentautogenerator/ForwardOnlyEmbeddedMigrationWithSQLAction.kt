package com.jetbrains.rider.plugins.fluentautogenerator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class ForwardOnlyEmbeddedMigrationWithSQLAction : BaseMigrationAction() {
    override fun getDialogTitle() = "Generate SQL & C# Migration"
    
    override fun getFileExtension() = ""
    
    override fun getMigrationTemplate(namespace: String,
         timestamp: String, 
         className: String, 
         branchName:String, 
         tagsAttribute: String) = ""

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val folder = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val branchName = getGitBranch(folder.path)
        val defaultClassName = extractClassName(branchName)

        val settings = com.jetbrains.rider.plugins.fluentautogenerator.settings.FluentGeneratorSettingsState.instance
        val possibleTags = settings.possibleTags
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        
        val dialog = MigrationInputDialog(project, getDialogTitle(), defaultClassName, possibleTags)
        
        // showAndGet() returns true if the user clicks "OK", false if they click "Cancel"
        if (!dialog.showAndGet()) {
            return 
        }
        
        // 3. Read the results from the dialog
        val className = dialog.getClassName()
        val insertAsStrings = settings.insertTagsAsStrings
        
        val selectedTags = dialog.getSelectedTags()
        
        val tagsAttribute = if (selectedTags.isNotEmpty()) {
            val formattedTags = if (insertAsStrings) {
                selectedTags.joinToString(", ") { "\"$it\"" }
            } else {
                selectedTags.joinToString(", ")
            }
            ", Tags($formattedTags)"
        } else {
            ""
        }

        if (className.isNullOrBlank()) 
          return

        val dateFormat = java.text.SimpleDateFormat("yyyyMMddHHmmss")
        dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val versionTimestamp = dateFormat.format(java.util.Date())
        
        val namespace = calculateNamespace(folder)

        val sqlFileName = "${className}.sql"
        val csFileName = "${className}.cs"

        val sqlContent = "-- Write your SQL migration for $className here\n"
        
        val csContent = """
using System;
using FluentMigrator;

namespace $namespace;

[Migration($versionTimestamp, "$branchName")$tagsAttribute]
public class $className : ForwardOnlyMigration
{
    public override void Up()
    {
        Execute.EmbeddedScript("$sqlFileName");
    }
}
        """.trimIndent()

        ApplicationManager.getApplication().runWriteAction {
            try {
                // 1. Create the Sql folder and .sql file
                var sqlFolder = folder.findChild("Sql")
                if (sqlFolder == null) {
                    sqlFolder = folder.createChildDirectory(this, "Sql")
                }
                val sqlFile = sqlFolder.createChildData(this, sqlFileName)
                com.intellij.openapi.vfs.VfsUtil.saveText(sqlFile, sqlContent)
        
                // NEW: Ensure the wildcard exists in the .csproj!
                ensureSqlWildcardEmbedded(folder)
        
                // 2. Create the C# file
                val csFile = folder.createChildData(this, csFileName)
                com.intellij.openapi.vfs.VfsUtil.saveText(csFile, csContent)
                
                // 3. Open BOTH files in the editor
                com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project).openFile(sqlFile, true)
                com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project).openFile(csFile, true)
                
            } catch (ex: Exception) {
                com.intellij.openapi.ui.Messages.showErrorDialog(project, "Error creating files: ${ex.message}", "Error")
            }
        }
    }
}