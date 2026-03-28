package com.jetbrains.rider.plugins.fluentautogenerator.intentions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.jetbrains.rider.plugins.fluentautogenerator.settings.FluentGeneratorSettingsState

class CreateSqlMigrationIntention : PsiElementBaseIntentionAction() {

    override fun getText(): String = "Create missing SQL migration file"
    override fun getFamilyName(): String = "Fluent Auto Generator"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element !is LeafPsiElement) return false
        val text = element.text
        if (!text.endsWith(".sql\"") && !text.endsWith(".sql'")) return false

        val cleanFileName = text.trim('\"', '\'')
        val scope = GlobalSearchScope.projectScope(project)
        val existingFiles = FilenameIndex.getVirtualFilesByName(cleanFileName, scope)

        // Only show Alt+Enter menu if the file does NOT exist
        return existingFiles.isEmpty()
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val cleanFileName = element.text.trim('\"', '\'')
        val currentDirectory = element.containingFile.containingDirectory ?: return
        val targetFolderName = FluentGeneratorSettingsState.instance.sqlFolderName.ifBlank { "Sql" }

        WriteCommandAction.runWriteCommandAction(project, "Create SQL Migration", null, {
            try {
                var sqlDirectory = currentDirectory.findSubdirectory(targetFolderName)
                if (sqlDirectory == null) {
                    sqlDirectory = currentDirectory.createSubdirectory(targetFolderName)
                }

                val newFile = sqlDirectory.createFile(cleanFileName)
                FileEditorManager.getInstance(project).openFile(newFile.virtualFile, true)
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }
}