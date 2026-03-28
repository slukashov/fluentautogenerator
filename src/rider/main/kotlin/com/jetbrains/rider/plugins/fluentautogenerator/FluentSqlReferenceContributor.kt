package com.jetbrains.rider.plugins.fluentautogenerator.references

import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext

/**
 * Tells the IDE to inject clickable references into C# string literals.
 */
class FluentSqlReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        // We target all elements that host injected languages (which includes C# string literals)
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PsiLanguageInjectionHost::class.java),
            FluentSqlReferenceProvider()
        )
    }
}



