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
              
      if (file == null || !file.isDirectory) {
          e.presentation.isEnabledAndVisible = false
          return
      }
      
      e.presentation.isEnabledAndVisible = isFluentMigratorProject(file)
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
                val extension = getFileExtension()
                val fileName = "${className}.${extension}"
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
    abstract fun getFileExtension(): String
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
    
    private fun isFluentMigratorProject(folder: com.intellij.openapi.vfs.VirtualFile): Boolean {
        var current: com.intellij.openapi.vfs.VirtualFile? = folder
        
        // Walk up the folder tree until we find the project file
        while (current != null) {
            // Look for any .csproj file in the current directory
            val csproj = current.children?.firstOrNull { it.extension == "csproj" }
            
            if (csproj != null) {
                return try {
                    // Read the file and check for the NuGet package
                    val content = com.intellij.openapi.vfs.VfsUtilCore.loadText(csproj)
                    content.contains("FluentMigrator")
                } catch (ex: Exception) {
                    false
                }
            }
            // Move up to the parent folder and check again
            current = current.parent
        }
        
        // If we hit the top and found no .csproj, hide the menu
        return false
    }
}