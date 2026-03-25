package com.yourname.fluentautogenerator.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

// @State tells Rider to save this data into an XML file named FluentAutoGeneratorSettings.xml
@State(
    name = "com.yourname.fluentautogenerator.settings.FluentGeneratorSettingsState",
    storages = [Storage("FluentAutoGeneratorSettings.xml")]
)
class FluentGeneratorSettingsState : PersistentStateComponent<FluentGeneratorSettingsState> {
    
    // This is the variable that will hold the user's tags! 
    // We give it a default value so it's not empty on first install.
    var possibleTags: String = "Development, Production, Staging"
    var insertTagsAsStrings: Boolean = true
    
    override fun getState(): FluentGeneratorSettingsState = this

    override fun loadState(state: FluentGeneratorSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val instance: FluentGeneratorSettingsState
            get() = ApplicationManager.getApplication().getService(FluentGeneratorSettingsState::class.java)
    }
}