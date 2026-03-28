package com.jetbrains.rider.plugins.fluentautogenerator.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext

/**
 * Forces SQL file names into the C# IntelliSense dropdown menu.
 */
class SqlFileCompletionContributor : CompletionContributor() {

    init {
        // This tells the IDE to run this logic ANY time a basic code completion menu opens
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), object : CompletionProvider<CompletionParameters>() {
            
            override fun addCompletions(
                parameters: CompletionParameters,
                context: ProcessingContext,
                result: CompletionResultSet
            ) {
                val element = parameters.position

                // 1. Look up the tree to ensure we are inside an EmbeddedScript call
                var isEmbeddedScriptCall = false
                var currentParent = element.parent
                
                for (i in 0..3) {
                    if (currentParent?.text?.contains("EmbeddedScript") == true) {
                        isEmbeddedScriptCall = true
                        break
                    }
                    currentParent = currentParent?.parent
                }

                // If we aren't inside EmbeddedScript, stop here and let standard IntelliSense run
                if (!isEmbeddedScriptCall) return

                val project = element.project
                val scope = GlobalSearchScope.projectScope(project)

                // 2. Find all SQL files
                val sqlFileType = FileTypeManager.getInstance().getFileTypeByExtension("sql")
                val virtualFiles = FileTypeIndex.getFiles(sqlFileType, scope)

                for (virtualFile in virtualFiles) {
                    val path = virtualFile.path.lowercase()
                    
                    // 3. Filter out the bin/obj folders
                    if (path.contains("/bin/") || path.contains("/obj/")) continue

                    // 4. Build the dropdown item
                    val lookupElement = LookupElementBuilder.create(virtualFile.name)
                        .withIcon(AllIcons.Nodes.DataTables)
                        .withTypeText("SQL Migration")
                        .withCaseSensitivity(false)

                    // 5. Force it into the menu!
                    result.addElement(lookupElement)
                }
            }
        })
    }
}