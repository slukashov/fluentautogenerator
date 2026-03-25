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
