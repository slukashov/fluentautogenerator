package com.jetbrains.rider.plugins.fluentautogenerator.references

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*

/**
 * The actual hyperlink logic that resolves the string to the physical file.
 */
class SqlFileReference(
    element: PsiElement, 
    textRange: TextRange, 
    private val fileName: String
) : PsiReferenceBase<PsiElement>(element, textRange) {

    // This is where the magic happens! When the user Ctrl+Clicks, 
    // it runs this method to find the destination file.
    override fun resolve(): PsiElement? {
        val project = element.project
        val scope = com.intellij.psi.search.GlobalSearchScope.projectScope(project)
        
        // Reusing the FilenameIndex logic we wrote earlier!
        val virtualFiles = com.intellij.psi.search.FilenameIndex.getVirtualFilesByName(fileName, scope)
        val targetVirtualFile = virtualFiles.firstOrNull { virtualFile ->
            val path = virtualFile.path.lowercase()
            !path.contains("/bin/") && !path.contains("/obj/")
        } ?: return null
        
        return PsiManager.getInstance(project).findFile(targetVirtualFile)
    }

    // Optional: Enables auto-complete (Ctrl+Space) for the filename!
    override fun getVariants(): Array<Any> = emptyArray() 
}