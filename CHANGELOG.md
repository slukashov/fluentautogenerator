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

## Version 1.0.2

###  New Features

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

### 🔧 Improvements

- **Refactored Migration Actions**:
    - Renamed `ForwardOnlyEmbeddedMigrationAction` to `ForwardOnlyMigrationAction` for clarity
    - All migration actions now use the new `MigrationInputDialog` instead of basic input prompts
    - Improved namespace calculation with `calculateNamespace()` helper method
    - Standardized migration template generation with support for optional tags parameter


