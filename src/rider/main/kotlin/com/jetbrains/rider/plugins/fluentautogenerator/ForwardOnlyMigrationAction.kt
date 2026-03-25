package com.jetbrains.rider.plugins.fluentautogenerator

class ForwardOnlyMigrationAction : BaseMigrationAction() {
    override fun getDialogTitle() = "Generate ForwardOnlyMigration"
    
    override fun getFileExtension() = "cs"
    
    override fun getMigrationTemplate(namespace: String,
         timestamp: String, 
         className: String, 
         branchName:String, 
         tagsAttribute: String) = """
using System;
using FluentMigrator;

namespace $namespace;

[Migration($timestamp, "$branchName")$tagsAttribute]
public class $className : ForwardOnlyMigration
{
    public override void Up()
    {
        // Implement the logic to apply the migration
    }
}

    """.trimIndent()
}