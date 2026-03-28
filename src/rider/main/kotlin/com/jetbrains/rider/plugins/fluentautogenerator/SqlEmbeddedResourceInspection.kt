package com.jetbrains.rider.plugins.fluentautogenerator.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * Warns the user if a .sql file referenced in EmbeddedScript is not marked as an EmbeddedResource.
 */
class SqlEmbeddedResourceInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                // 1. Only look at leaf elements (basic text tokens)
                if (element !is LeafPsiElement) return

                val text = element.text

                // 2. Check if it's a string literal containing a .sql file
                if (!text.endsWith(".sql\"") && !text.endsWith(".sql'")) return

                // 3. Check if the parent context looks like an EmbeddedScript call
                val parentText = element.parent?.text ?: return
                if (!parentText.contains("EmbeddedScript")) return

                val cleanFileName = text.trim('\"', '\'')

                // 4. Find the .csproj file associated with this code
                val currentVirtualFile = element.containingFile.virtualFile ?: return
                val csprojFile = findCsproj(currentVirtualFile) ?: return

                // 5. Read the .csproj content and check for the EmbeddedResource tag
                val csprojText = VfsUtilCore.loadText(csprojFile)
                
                // A simple heuristic: check if the file name is in the csproj at all.
                // For a stricter check, you could parse the XML, but this text check is very fast!
                if (!csprojText.contains(cleanFileName)) {
                    
                    // 6. Register the warning! This creates the yellow squiggle.
                    holder.registerProblem(
                        element,
                        "File '$cleanFileName' might not be set as an EmbeddedResource in the .csproj",
                        ProblemHighlightType.WARNING
                    )
                }
            }
        }
    }

    /**
     * Walks up the directory tree to find the nearest .csproj file.
     */
    private fun findCsproj(startFile: VirtualFile): VirtualFile? {
        var currentDir = startFile.parent
        // Traverse up the folders until we find a .csproj or hit the top of the project
        while (currentDir != null) {
            val csproj = currentDir.children.find { it.extension == "csproj" }
            if (csproj != null) return csproj
            currentDir = currentDir.parent
        }
        return null
    }
}