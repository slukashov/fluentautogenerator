package com.jetbrains.rider.plugins.fluentautogenerator

class CreateSqlFileAction : BaseMigrationAction() {
    override fun getDialogTitle() = "Create sql file"
    
    override fun getFileExtension() = "sql"
    
    override fun getMigrationTemplate(namespace: String,
     timestamp: String, 
     className: String, 
     branchName:String, 
     tagsAttribute: String) = "-- Write your SQL migration for $className here\n"
}