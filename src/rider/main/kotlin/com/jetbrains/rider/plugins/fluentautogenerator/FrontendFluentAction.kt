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
        val selectedTags = dialog.getSelectedTags()

        if (className.isBlank()) return

        // NEW: Check the user's setting for formatting
        val insertAsStrings = settings.insertTagsAsStrings

        val tagsAttribute = if (selectedTags.isNotEmpty()) {
            val formattedTags = if (insertAsStrings) {
                // Formats as: "Development", "UK"
                selectedTags.joinToString(", ") { "\"$it\"" }
            } else {
                // Formats as: Development, UK (Raw)
                selectedTags.joinToString(", ")
            }
            ", Tags($formattedTags)"
        } else {
            ""
        }

        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val versionTimestamp = dateFormat.format(Date())
        val namespaceName = calculateNamespace(folder)

        // Call the specific template from the child class
        val fileContent = getMigrationTemplate(namespaceName, versionTimestamp, className.replace('-', '_'), branchName, tagsAttribute)

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
    abstract fun getMigrationTemplate(namespace: String,
     timestamp: String,
     className: String, 
     branchName: String,
     tagsAttribute: String): String

    protected fun getGitBranch(workingDir: String): String {
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

    protected fun extractClassName(branchName: String): String {
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
    
    protected fun calculateNamespace(folder: com.intellij.openapi.vfs.VirtualFile): String {
            var current: com.intellij.openapi.vfs.VirtualFile? = folder
            var csprojFile: com.intellij.openapi.vfs.VirtualFile? = null
            var projectFolder: com.intellij.openapi.vfs.VirtualFile? = null
    
            // 1. Walk up the tree to find the .csproj file
            while (current != null) {
                val csproj = current.children?.firstOrNull { it.extension == "csproj" }
                if (csproj != null) {
                    csprojFile = csproj
                    projectFolder = current
                    break
                }
                current = current.parent
            }
    
            // Fallback if no project file is found
            if (csprojFile == null || projectFolder == null) {
                return "DefaultNamespace"
            }
    
            // 2. Determine the base namespace (Check .csproj for <RootNamespace>, else use filename)
            var rootNamespace = csprojFile.nameWithoutExtension
            try {
                val content = com.intellij.openapi.vfs.VfsUtilCore.loadText(csprojFile)
                val matcher = Pattern.compile("<RootNamespace>(.*?)</RootNamespace>").matcher(content)
                if (matcher.find()) {
                    rootNamespace = matcher.group(1).trim()
                }
            } catch (e: Exception) {
                // Ignore read errors and stick to the filename
            }
    
            // 3. Calculate the folder path difference (e.g., returns "Data/Migrations")
            // The '.' tells VfsUtilCore to replace slashes with dots!
            val relativePath = com.intellij.openapi.vfs.VfsUtilCore.getRelativePath(folder, projectFolder, '.')
    
            // 4. Combine them
            val fullNamespace = if (relativePath.isNullOrEmpty()) {
                rootNamespace
            } else {
                "$rootNamespace.$relativePath"
            }
    
            return sanitizeNamespace(fullNamespace)
        }
    
        // Ensures the namespace is perfectly valid C# syntax
        private fun sanitizeNamespace(ns: String): String {
            return ns.replace(" ", "_")
                     .replace("-", "_")
                     .split(".")
                     .joinToString(".") { part ->
                         // C# namespaces cannot start with a number
                         if (part.isNotEmpty() && part[0].isDigit()) "_$part" else part
                     }
        }
        
        protected fun ensureSqlWildcardEmbedded(folder: com.intellij.openapi.vfs.VirtualFile) {
           var current: com.intellij.openapi.vfs.VirtualFile? = folder
           var csprojFile: com.intellij.openapi.vfs.VirtualFile? = null
   
           // 1. Walk up the tree to find the .csproj file
           while (current != null) {
               val csproj = current.children?.firstOrNull { it.extension == "csproj" }
               if (csproj != null) {
                   csprojFile = csproj
                   break
               }
               current = current.parent
           }
   
           if (csprojFile == null) return
   
           try {
               var content = com.intellij.openapi.vfs.VfsUtilCore.loadText(csprojFile)
               
               // 2. Check if ANY wildcard for SQL files already exists in the project
               // We check for common patterns teams might use
               if (content.contains("\"**\\*.sql\"") || 
                   content.contains("\"**/*.sql\"") || 
                   content.contains("\"Sql\\**\\*.sql\"") ||
                   content.contains("\"Sql\\*.sql\"")) {
                   return // A wildcard is already handling it, do nothing!
               }
   
               // 3. Inject the global wildcard right before the closing </Project> tag
               val itemGroup = """
     <ItemGroup>
       <EmbeddedResource Include="**\*.sql" />
     </ItemGroup>
   
   </Project>"""
               
               content = content.replace("</Project>", itemGroup)
               com.intellij.openapi.vfs.VfsUtil.saveText(csprojFile, content)
               
           } catch (e: Exception) {
               // If we fail to read/write the project file, skip quietly
           }
       }
}