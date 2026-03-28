package com.jetbrains.rider.plugins.fluentautogenerator.injection

import com.intellij.lang.Language
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost

/**
 * Injects the SQL language into C# string literals used within FluentMigrator's Execute.Sql()
 */
class FluentSqlLanguageInjector : MultiHostInjector {

    /**
     * The IDE calls this method to determine what language (if any) to inject.
     */
    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        // 1. We only care about elements that can host an injected language (like string literals)
        if (context !is PsiLanguageInjectionHost) return

        // 2. We need to check the surrounding code to see if it's an Execute.Sql call.
        // In the lightweight frontend PSI, looking at the parent's text is a quick, safe heuristic.
        val parentText = context.parent?.text ?: return

        if (parentText.contains("Execute.Sql") || parentText.contains("IfDatabase")) {
            
            // 3. Find the built-in SQL language using its ID.
            // We use findLanguageByID so we don't have to add heavy database dependencies to your gradle file!
            val sqlLanguage = Language.findLanguageByID("SQL") ?: return

            // 4. Calculate where the actual SQL starts and ends inside the string.
            // A standard string is "SELECT * FROM Users", so we want to ignore the first and last character (the quotes).
            val textLength = context.textLength
            if (textLength < 2) 
                return // Skip empty strings
            
            val innerStringRange = TextRange(1, textLength - 1)

            // 5. Tell the IDE to inject SQL into those specific bounds!
            registrar.startInjecting(sqlLanguage)
                .addPlace(null, null, context, innerStringRange)
                .doneInjecting()
        }
    }

    /**
     * Tells the IDE which types of code elements this injector cares about.
     */
    override fun elementsToInjectIn(): List<Class<out PsiElement>> {
        return listOf(PsiLanguageInjectionHost::class.java)
    }
}