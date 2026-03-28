package com.jetbrains.rider.plugins.fluentautogenerator.references

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.util.ProcessingContext

/**
 * Evaluates the string to see if it should become a clickable hyperlink.
 */
class FluentSqlReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val text = element.text
        if ((text.startsWith("\"") || text.startsWith("'")) && 
            (text.endsWith(".sql\"") || text.endsWith(".sql'"))) {
            
            val cleanFileName = text.trim('\"', '\'')
            return arrayOf(SqlFileReference(element, TextRange(1, text.length - 1), cleanFileName))
        }
        return PsiReference.EMPTY_ARRAY
    }
}