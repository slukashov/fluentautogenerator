package com.jetbrains.rider.plugins.fluentautogenerator.markers

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * Provides a gutter icon to navigate from a specific code element to its related generated code.
 */
class FluentMigrationLineMarkerProvider : RelatedItemLineMarkerProvider() {

    /**
     * This method is called by the IDE for elements visible in the editor.
     * * @param element The current code element being inspected.
     * @param result A collection where we add our marker if the element qualifies.
     */
    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        if (element !is LeafPsiElement) {
            return
        }
  
        val text = element.text
        if (!text.endsWith(".sql\"") && !text.endsWith(".sql'")) {
            return
        }
        val targetElementToNavigateTo = findTargetSqlFile(element)
         
        if (targetElementToNavigateTo != null) {
            val builder = NavigationGutterIconBuilder.create(AllIcons.Nodes.DataTables) // Choose a relevant icon
                .setTargets(targetElementToNavigateTo)
                .setTooltipText("Navigate to generated SQL migration")
        
            result.add(builder.createLineMarkerInfo(element))
        }
    }

/**
     * Locates the associated SQL file inside a specific subfolder.
     * @param sourceElement The C# element currently being evaluated.
     * @return The PSI representation of the SQL file, or null if it cannot be found.
     */
    private fun findTargetSqlFile(sourceElement: PsiElement): PsiElement? {

        val targetFolderName = "Sql" 

        val currentPsiFile = sourceElement.containingFile ?: return null
        val virtualFile = currentPsiFile.virtualFile ?: return null
        val baseFileName = virtualFile.nameWithoutExtension
        val targetSqlFileName = "$baseFileName.sql"
        val parentDirectory = virtualFile.parent ?: return null
        val targetDirectory = parentDirectory.findChild(targetFolderName) 
        if (targetDirectory == null || !targetDirectory.isDirectory) {
            return null 
        }
        val sqlVirtualFile = targetDirectory.findChild(targetSqlFileName) ?: return null
        val project = sourceElement.project
        return PsiManager.getInstance(project).findFile(sqlVirtualFile)
    }
}