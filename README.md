# FluentAutoGenerator

A **Rider & ReSharper plugin** that streamlines FluentMigrator database migration scaffolding. Generate production-ready migration boilerplate in seconds directly from your IDE.

[![Rider](https://img.shields.io/jetbrains/plugin/v/RIDER_PLUGIN_ID.svg?label=Rider&colorB=0A7BBB&style=for-the-badge&logo=rider)](https://plugins.jetbrains.com/plugin/RIDER_PLUGIN_ID)
[![ReSharper](https://img.shields.io/jetbrains/plugin/v/RESHARPER_PLUGIN_ID.svg?label=ReSharper&colorB=0A7BBB&style=for-the-badge&logo=resharper)](https://plugins.jetbrains.com/plugin/RESHARPER_PLUGIN_ID)

---

## 📋 Overview

**FluentAutoGenerator** eliminates boilerplate when working with FluentMigrator migrations. Instead of manually writing migration class scaffolds and managing timestamps, this plugin:

- Generates migration classes with a single right-click
- Auto-populates metadata from Git branch names
- Creates UTC-based versioning automatically
- Supports multiple migration types (forward-only and up/down)
- Opens the generated file immediately for editing

Perfect for teams maintaining large database migration codebases or wanting to standardize migration naming conventions across projects.

---

## ✨ Features

### Two Migration Templates

1. **Forward Only Migration**
   - Generates a `ForwardOnlyMigration` class
   - Includes pre-configured `Execute.EmbeddedScript()` call
   - Ideal for deployments that never rollback

2. **Up/Down Migration**
   - Generates standard `Migration` class with `Up()` and `Down()` methods
   - Provides empty method bodies for you to implement
   - Full rollback support

### Smart Metadata Auto-Population

- **Timestamp**: Uses current UTC time for the `[Migration]` attribute
- **Branch Name**: Extracted from Git HEAD and used as migration description
- **Class Name**: Auto-suggested from branch name (e.g., `feature/add-users` → `AddUsers`)
- **Namespace**: Uses your project name for consistency

### Tight IDE Integration

- Works in Rider and ReSharper with minimal configuration
- Context menu appears only on folders in Solution Explorer
- Generated file opens automatically for immediate editing
- Custom icon for visual clarity

---

## 📦 Requirements

### For Users

- **Rider 2025.3+** or **ReSharper 2025.3+**
- **Git** installed and accessible on PATH (for branch name extraction)
- Active C# project in the IDE

### For Developers (Building from Source)

- **Gradle 8.8+**
- **Kotlin 1.x+**
- **Java 17+**
- JetBrains IDE SDK (auto-managed by gradle-intellij-plugin)

---

## 🚀 Installation

### From JetBrains Marketplace

1. Open **Rider** → **Preferences/Settings** → **Plugins**
2. Search for **FluentAutoGenerator**
3. Click **Install** and restart the IDE

Alternatively:
1. Visit the [plugin page](https://plugins.jetbrains.com/plugin/RIDER_PLUGIN_ID)
2. Download the `.zip` file
3. Install via **Plugins** → **⚙️** → **Install Plugin from Disk**

---

## 💻 Usage

### Creating a New Migration

1. In **Solution Explorer**, navigate to your migrations folder
2. **Right-click** the folder → **FluentMigrator**
3. Choose migration type:
   - **Forward only migration...** → Creates forward-only template
   - **Up/down migration...** → Creates bidirectional template
4. Enter a migration name (optional; defaults are auto-suggested)
5. The new `.cs` file opens immediately

### Example Workflow

```
src/MyApp
├── Migrations/
│   ├── 202401101530_CreateUsersTable.cs
│   ├── 202401101545_AddEmailToUsers.cs
│   └── [right-click] → FluentMigrator → Forward only migration...
│       → Input: "AddIndexes"
│       → Generated: 202401101600_AddIndexes.cs (opens for editing)
```

### Generated Code Example

**Forward Only Migration:**
```csharp
using System;
using FluentMigrator;

namespace MyApp.Migrations
{
    [Migration(202401101600, "feature/add-indexes")]
    public class AddIndexes : ForwardOnlyMigration
    {
        public override void Up()
        {
            Execute.EmbeddedScript("AddIndexes.sql");
        }
    }
}
```

**Up/Down Migration:**
```csharp
using System;
using FluentMigrator;

namespace MyApp.Migrations
{
    [Migration(202401101600, "feature/add-indexes")]
    public class AddIndexes : Migration
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
```

---

## 🏗️ Project Structure

```
FluentAutoGenerator/
├── src/
│   └── rider/
│       └── main/
│           ├── kotlin/com/jetbrains/rider/plugins/fluentautogenerator/
│           │   └── FrontendFluentAction.kt          # Main action logic
│           └── resources/META-INF/
│               └── plugin.xml                        # Plugin manifest
├── protocol/                                          # RD protocol definitions
│   └── build.gradle.kts
├── build.gradle.kts                                   # Root Gradle build
├── gradle.properties                                  # Default properties
├── CHANGELOG.md                                       # Version history
└── README.md                                          # This file
```

### Key Components

| File | Purpose |
|------|---------|
| `FrontendFluentAction.kt` | Implements base action, dialog logic, file generation |
| `plugin.xml` | IDE manifest; registers context menu actions |
| `build.gradle.kts` | Gradle build configuration; handles plugin packaging |

---

## 🛠️ Development

### Building the Plugin

```bash
# Clone the repository
git clone https://github.com/yourusername/FluentAutoGenerator.git
cd FluentAutoGenerator

# Build the plugin
./gradlew build

# Output: build/distributions/ReSharperPlugin.FluentAutoGenerator-*.zip
```

### Running in Development Mode

```bash
# Launch Rider/ReSharper with the plugin loaded
./gradlew runIde
```

This opens a sandbox Rider instance with hot-reload for testing.

### Testing

```bash
# Build and run tests (if implemented)
./gradlew test
```

### Publishing to Marketplace

```bash
# Publish to JetBrains Plugin Repository
# (Requires valid PublishToken in gradle.properties)
./gradlew publishPlugin
```

---

## 📝 Configuration

### Gradle Properties (`gradle.properties`)

Key properties you may customize:

```properties
# Plugin version (follows Semantic Versioning)
PluginVersion=1.0.0

# Target IDE version (e.g., 2025.3)
ProductVersion=2025.3

# JetBrains Plugin Repository token for publishing
PublishToken=YOUR_TOKEN_HERE

# Kotlin stdlib dependency management
kotlin.stdlib.default.dependency=false
```

---

## 🤝 Contributing

Contributions are welcome! Here's how to get started:

1. **Fork** the repository
2. **Create a feature branch**: `git checkout -b feature/my-improvement`
3. **Make your changes** and test with `./gradlew runIde`
4. **Commit** with clear messages: `git commit -am "Add feature X"`
5. **Push** to your fork: `git push origin feature/my-improvement`
6. **Open a Pull Request** with a detailed description

### Areas for Contribution

- Additional migration template types
- Support for custom naming conventions
- Internationalization (i18n)
- Performance optimizations
- Bug fixes and documentation improvements

---

## 🐛 Troubleshooting

### Plugin Not Appearing in Context Menu

**Issue**: The FluentMigrator context menu doesn't show.

**Solution**:
1. Verify you're right-clicking on a **folder** (not a file)
2. Ensure the folder is in Solution Explorer (not a regular file browser)
3. Restart the IDE: **File** → **Invalidate Caches** → **Invalidate and Restart**

### Git Branch Name Not Detected

**Issue**: Default migration name is "DefaultModel" instead of branch-derived name.

**Solution**:
1. Ensure **Git is installed** and accessible: `git --version`
2. Verify the project is a **valid Git repository**: `git rev-parse --git-dir`
3. Check that the active branch is recognized: `git rev-parse --abbrev-ref HEAD`

### File Not Opening After Generation

**Issue**: Migration file is created but doesn't open in the editor.

**Solution**:
1. Check IDE logs: **Help** → **Show Log in Explorer/Finder**
2. Look for errors related to file creation or editor opening
3. Ensure your IDE has write permissions in the target folder

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Serhii Lukashov**  
[LinkedIn Profile](https://www.linkedin.com/in/slukashov/)

---

## 📚 Additional Resources

- [FluentMigrator Documentation](https://fluentmigrator.github.io/)
- [JetBrains Plugin Development Guide](https://plugins.jetbrains.com/docs/intellij/)
- [Rider Documentation](https://rider-support.jetbrains.com/)
- [ReSharper Documentation](https://www.jetbrains.com/help/resharper/)

---

## ⭐ Show Your Support

If this plugin has been helpful, consider:

- **Starring** this repository
- **Rating** the plugin on [JetBrains Marketplace](https://plugins.jetbrains.com/)
- **Sharing** with your team
- **Reporting** bugs and suggesting features
