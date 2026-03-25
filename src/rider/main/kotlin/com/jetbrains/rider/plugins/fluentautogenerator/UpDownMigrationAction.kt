package com.jetbrains.rider.plugins.fluentautogenerator

class UpDownMigrationAction : BaseMigrationAction() {
    override fun getDialogTitle() = "Generate Up/Down Migration"
    
    override fun getFileExtension() = "cs"
    
    override fun getMigrationTemplate(namespace: String, timestamp: String, className: String, branchName:String) = """
using System;
using FluentMigrator;

namespace $namespace
{
    [Migration($timestamp, "$branchName")]
    public class $className : Migration
    {
        public override void Up()
        {
            // Implement the logic to apply the migration
        }

        public override void Down()
        {
            // Implement the logic to revert the changes made in Up()
        }
    }
}
    """.trimIndent()
}