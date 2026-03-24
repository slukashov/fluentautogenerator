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

abstract class BaseMigrationAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        e.presentation.isEnabledAndVisible = file != null && file.isDirectory
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val folder = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val branchName = getGitBranch(folder.path)
        val defaultClassName = extractClassName(branchName)

        val className = Messages.showInputDialog(
            project,
            "Enter migration name:",
            getDialogTitle(),
            Messages.getQuestionIcon(),
            defaultClassName,
            null
        )

        if (className.isNullOrBlank()) 
            return

        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val versionTimestamp = dateFormat.format(Date())
        val namespaceName = "${project.name}.Migrations"


        // Call the specific template from the child class
        val fileContent = getMigrationTemplate(namespaceName, versionTimestamp, className.replace('-', '_'), branchName)

        ApplicationManager.getApplication().runWriteAction {
            try {
                val fileName = "${className}.cs"
                val newFile = folder.createChildData(this, fileName)
                VfsUtil.saveText(newFile, fileContent)
                FileEditorManager.getInstance(project).openFile(newFile, true)
            } catch (ex: Exception) {
                Messages.showErrorDialog(project, "Error creating file: ${ex.message}", "Error")
            }
        }
    }

    // Abstract methods that child classes must implement
    abstract fun getDialogTitle(): String
    abstract fun getMigrationTemplate(namespace: String, timestamp: String, className: String, branchName: String): String

    private fun getGitBranch(workingDir: String): String {
        return try {
            val proc = ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
                .directory(File(workingDir))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()
            proc.waitFor(2, TimeUnit.SECONDS)
            proc.inputStream.bufferedReader().readText().trim()
        } catch (e: Exception) {
            "UnknownBranch"
        }
    }

    private fun extractClassName(branchName: String): String {
        val lastPart = if (branchName.contains("/")) branchName.substringAfterLast("/") else branchName
        val words = lastPart.split("-", "_").filter { it.isNotEmpty() }
        if (words.isEmpty()) return "DefaultModel"
        val formattedName = words.joinToString("") { it.replaceFirstChar { char -> char.uppercase() } }
        return if (formattedName[0].isDigit()) "Model$formattedName" else formattedName
    }
}

// =========================================================================
// 2. THE SPECIFIC ACTIONS
// =========================================================================

class ForwardOnlyEmbeddedMigrationAction : BaseMigrationAction() {
    override fun getDialogTitle() = "Generate ForwardOnlyMigration with EmbeddedScript"
    override fun getMigrationTemplate(namespace: String, timestamp: String, className: String, branchName: String) = """
using System;
using FluentMigrator;

namespace $namespace
{
    [Migration($timestamp, "$branchName")]
    public class $className : ForwardOnlyMigration
    {
        public override void Up()
        {
            Execute.EmbeddedScript("$className.sql");
        }
    }
}
    """.trimIndent()
}

class UpDownMigrationAction : BaseMigrationAction() {
    override fun getDialogTitle() = "Generate Up/Down Migration"
    override fun getMigrationTemplate(namespace: String, timestamp: String, className: String, branchName:String) = """
using System;
using FluentMigrator;

namespace $namespace
{
    [Migration($timestamp, "$branchName")]
    public class $className : Migration
    {
        public override void Up()
        {
            // Implement the logic to apply the migration
        }

        public override void Down()
        {
            // Implement the logic to revert the changes made in Up()
        }
    }
}
    """.trimIndent()
}