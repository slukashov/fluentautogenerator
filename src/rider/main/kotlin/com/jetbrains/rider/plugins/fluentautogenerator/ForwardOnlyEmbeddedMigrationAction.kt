package com.jetbrains.rider.plugins.fluentautogenerator

class ForwardOnlyEmbeddedMigrationAction : BaseMigrationAction() {
    override fun getDialogTitle() = "Generate ForwardOnlyMigration with EmbeddedScript"
    
    override fun getFileExtension() = "cs"
    
    override fun getMigrationTemplate(namespace: String, timestamp: String, className: String, branchName: String) = """
using System;
using FluentMigrator;

namespace $namespace
{
    [Migration($timestamp, "$branchName")]
    public class $className : ForwardOnlyMigration
    {
        public override void Up()
        {
            Execute.EmbeddedScript("$className.sql");
        }
    }
}
    """.trimIndent()
}