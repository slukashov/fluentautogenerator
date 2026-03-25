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

        val settings = com.yourname.fluentautogenerator.settings.FluentGeneratorSettingsState.instance
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
                // 1. Find or create the "Sql" subfolder!
                var sqlFolder = folder.findChild("Sql")
                if (sqlFolder == null) {
                    sqlFolder = folder.createChildDirectory(this, "Sql")
                }

                // 2. Create the SQL file INSIDE the Sql subfolder
                val sqlFile = sqlFolder.createChildData(this, sqlFileName)
                VfsUtil.saveText(sqlFile, sqlContent)

                // 3. Create the C# file in the folder you right-clicked
                val csFile = folder.createChildData(this, csFileName)
                VfsUtil.saveText(csFile, csContent)

                // 4. Open BOTH files in the editor side-by-side
                FileEditorManager.getInstance(project).openFile(sqlFile, true)
                FileEditorManager.getInstance(project).openFile(csFile, true)
                
            } catch (ex: Exception) {
                Messages.showErrorDialog(project, "Error creating files: ${ex.message}", "Error")
            }
        }
    }
}