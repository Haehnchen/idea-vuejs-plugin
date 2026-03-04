# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the **Vue.js Toolbox Plugin** for IntelliJ IDEA/PhpStorm - an IDE plugin that provides additional code assistance for Vue.js development. The plugin extends the official JetBrains Vue.js plugin with navigation and usage features.

**Plugin ID:** `de.espend.idea.vuejs`
**Plugin Name:** Vue.js Toolbox
**Key Dependencies:** Requires the official `org.jetbrains.plugins.vue` plugin

## Build and Development Commands

### Building the Plugin

```bash
./gradlew clean buildPlugin
```

The distributable ZIP artifact will be in `build/distributions/`.

### Running Tests

```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "tests.de.espend.idea.vuejs.index.ComponentUsageIndexTest"

# Run tests matching a pattern
./gradlew test --tests "*IndexTest"
```

## High-Level Architecture

### Main Package Organization

The codebase is organized into **6 packages** under `src/main/java/de/espend/idea/vuejs/`:

- `index/` - File-based indexes (ComponentUsageIndex, AppEntrypointIndex)
- `index/externalizer/` - Data externalizers for index serialization
- `index/utils/` - Utility classes for indexing (VueJsComponentIndexUtil)
- `index/visitor/` - PSI visitors for index collection
- `linemarker/` - LineMarker providers for navigation (ComponentUsageLineMarker, AppEntrypointLineMarker, TemplateComponentLineMarker)
- `target/` - Target suppliers for navigation
- `utils/` - General utilities (VueJsUtil, PluginErrorReporterSubmitter)

**Infrastructure:**
- File-based indexes extending `FileBasedIndexExtension<String, List<String>>`
- LineMarker providers implementing `LineMarkerProvider`
- `src/main/resources/META-INF/plugin.xml` for registering extension points

**Testing:**
- `VueJsLightJavaCodeInsightFixtureTestCase` - Base test class with assertion helpers
- **Index Testing:** `assertIndexContains()`, `assertIndex()`
- Test files use Vue.js fixtures via `myFixture.copyFileToProject()` to simulate Vue project structures
- Test fixtures are located in `src/test/java/tests/de/espend/idea/vuejs/index/fixtures/`

### Unit Test VFS Limitations

- Light tests use in-memory VFS (`temp://` protocol) - standard path resolution doesn't work
- For Vue file tests: copy fixtures first via `myFixture.copyFileToProject()` so they're available to the index

## Key Features

### Component Usage Navigation

LineMarker on `.vue` files that shows where the component is used:
- Detects `import Component from './Component.vue'` statements
- Supports PascalCase and kebab-case component tags in templates
- Navigates to the actual template usage locations

### App Entrypoint Detection

LineMarker that identifies Vue application entrypoints:
- `createApp(App)` - Vue 3 syntax
- `new Vue({ render: (createElement) => createElement(App) })` - Vue 2 syntax
- `new App().$mount(el)` - Vue 2 alternative

### In-File Component Navigation

LineMarker on component tags in templates that navigates to:
- `<script setup>` imports
- `components: { }` options
- `@Component` decorator declarations

## Important Development Notes

- **Performance:** Always use indexes and caching (`CachedValue`, `CachedValuesManager`) for expensive operations. Never iterate all files in the project directly.
- **Thread Safety:** Follow IntelliJ's threading model - read actions for PSI access, write actions for modifications. Most operations should be read-only.
- **Vue.js Integration:** This plugin builds on top of the official Vue.js plugin. Use `VueFile`, `VueModelManager`, `VueComponent`, and other classes from `org.jetbrains.vuejs.*`.

## Common Development Patterns

- **Adding a New Index:** Class extending `FileBasedIndexExtension<String, YourValueType>`
- **Adding a LineMarker:** Create a `LineMarkerProvider` implementation and register in plugin.xml
- **Vue File Detection:** Check `fileContent.getPsiFile() instanceof VueFile`
- **Component Name Conversion:** Use `VueJsUtil.convertToKebabCase()` and `VueJsUtil.convertKebabCaseToCamelCase()`

## Decompiler Tools

For analyzing bundled plugins like Vue.js you MUST use **vineflower** and NOT **Fernflower** from IntelliJ (less quality):

**vineflower**

- **GitHub:** https://github.com/Vineflower/vineflower
- **Download:** https://repo1.maven.org/maven2/org/vineflower/vineflower/1.11.2/vineflower-1.11.2.jar
- **Local copy:** `decompiled/vineflower.jar`
- **Target decompiled source dir:** (for reuse) `decompiled/PLUGIN_ID`
- **Usage:** `java -jar vineflower.jar input.jar output/`

**Bundled Plugin JARs (for decompilation):**
- **Location:** `~/.gradle/caches/[gradle-version]/transforms/*/transformed/com.jetbrains.[plugin]-[intellij-version]/[plugin]/lib/[plugin].jar`
- **Example:** `~/.gradle/caches/9.3.0/transforms/*/transformed/com.jetbrains.plugins.vue-233.14475.38/vue/lib/vue.jar`
