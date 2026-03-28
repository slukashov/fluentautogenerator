# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## 1.0.0
- Initial version

## 1.0.1
- Added SQL file generation capability for migrations
- Implemented FluentMigrator project detection—actions only show in compatible projects
- Refactored migration generation logic into shared BaseMigrationAction base class
- Fixed class resolution issues in migration action implementations

## 1.0.2

### Added

- **Migration Tags System**: Added support for tagging migrations with custom labels (e.g., Development, Production, Staging)
    - New `MigrationInputDialog` with multi-select list for choosing tags during migration creation
    - Tags are inserted into the `[Migration]` attribute alongside the timestamp
    - Configurable tag formatting: string literals (`"Dev"`) or raw identifiers if you use constants (`Dev`)

- **Plugin Settings UI**: Added settings page under Tools → FluentMigrator Generator
    - Configure available tags (comma-separated list)
    - Toggle between string literal and raw identifier formatting for tags
    - Settings persist across IDE sessions

- **New Migration Action**: Added "Create Forward only migration with SQL" action
    - Generates both a `.sql` file and a corresponding `.cs` file that executes the SQL
    - SQL file is embedded and executed via the `SqlScript()` method in the migration class
    - Useful for complex SQL operations that are easier to maintain in separate SQL files

### Changed

- **Refactored Migration Actions**:
    - Renamed `ForwardOnlyEmbeddedMigrationAction` to `ForwardOnlyMigrationAction` for clarity
    - All migration actions now use the new `MigrationInputDialog` instead of basic input prompts
    - Improved namespace calculation with `calculateNamespace()` helper method
    - Standardized migration template generation with support for optional tags parameter

## 1.0.3

### Added

- **Custom Template System**: Added support for creating and using custom migration templates
    - New dynamic menu in context menu for custom templates created by users
    - Templates support placeholders: `{Namespace}`, `{Timestamp}`, `{ClassName}`, `{BranchName}`, `{Tags}`, and `{Prefix}`
    - Custom templates can be added, edited, and removed through the settings UI
    - Each template has a configurable menu name and file prefix

- **Navigation Gutter Icon**: Added line marker provider for SQL migrations
    - Gutter icon appears next to `.sql` file references in C# migration code
    - Click the icon to navigate directly to the associated SQL file in the `Sql/` subfolder
    - Improves workflow when working with forward-only migrations with embedded SQL

- **Enhanced Settings UI**: Major redesign of the plugin settings page
    - Added split-pane interface for managing custom templates
    - Left panel: List of all custom templates with Add/Remove buttons
    - Right panel: Editor for template name, file prefix, and content
    - Templates are editable with live preview
    - Settings now include template management alongside existing tag configuration

### Fixed

- Fixed namespace resolution in `FluentGeneratorSettingsState` and `FluentGeneratorSettingsConfigurable`
- Updated plugin.xml to properly register the new dynamic template action group and line marker provider

## 1.0.3.1
### Fixed
- Updated search for line markers

## 1.0.3.2
### Fixed
- Replaced call of getFilesByName to getVirtualFilesByName

## 1.0.4
### Added

- **SQL Language Injection**
  - Injects SQL language into C# string literals within `Execute.Sql()` calls
  - Enables SQL syntax highlighting and validation directly in C# migration code
  - Supports `Execute.Sql()` and `IfDatabase()` contexts

- **SQL File References & Navigation**
  - Provides smart reference resolution for SQL file paths in migration code
  - Enables "Go to Definition" navigation from C# code to SQL files
  - Supports code completion for SQL file references

- **Code Completion for SQL Files**
  - Auto-completes SQL file paths when editing migration code
  - Shows available SQL files in the project with smart filtering
  - Improves developer experience when referencing SQL files

- **Code Inspections**
  - Warns if referenced `.sql` files are not marked as `EmbeddedResource` in `.csproj`
  - Prevents runtime errors from missing SQL embeddings
  - Yellow squiggle warnings in the editor

- **Quick Fixes & Intentions**
  - Quick fix (Alt+Enter) to create missing SQL migration files

- **Settings UI Component**
  - UI component for managing SQL-related settings
  - Integrates with existing settings page