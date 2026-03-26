package com.jetbrains.rider.plugins.fluentautogenerator.markers

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

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
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        if (element !is LeafPsiElement) 
            return
    
        val rawText = element.text
        val isSqlString = (rawText.startsWith("\"") || rawText.startsWith("'")) && 
                           rawText.contains(".sql", ignoreCase = true)
    
        if (!isSqlString) 
           return
    
        val cleanFileName = rawText.trim('\"', '\'')
        val targetElementToNavigateTo = findTargetSqlFile(element, cleanFileName)
    
        if (targetElementToNavigateTo != null) {
            val builder = NavigationGutterIconBuilder.create(AllIcons.Nodes.DataTables)
                .setTargets(targetElementToNavigateTo)
                .setTooltipText("Navigate to $cleanFileName")
    
            result.add(builder.createLineMarkerInfo(element))
        }
    }

     /**
     * Locates the associated SQL file inside a specific subfolder.
     * @param sourceElement The C# element currently being evaluated.
     * @return The PSI representation of the SQL file, or null if it cannot be found.
     */
    private fun findTargetSqlFile(sourceElement: PsiElement, fileName: String): PsiFile? {
      val project = sourceElement.project
      val scope = GlobalSearchScope.projectScope(project)
      val virtualFiles = FilenameIndex.getVirtualFilesByName(fileName, scope)
      if (virtualFiles.isEmpty()) {
          return null
      }
      val targetVirtualFile = virtualFiles.first()
      return PsiManager.getInstance(project).findFile(targetVirtualFile)
    }
}