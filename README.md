# IntelliJ IDEA / PhpStorm Vue.js Toolbox Plugin

[![Build Status](https://github.com/Haehnchen/idea-vuejs-plugin/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/Haehnchen/idea-vuejs-plugin/actions/workflows/gradle.yml)
[![zread](https://img.shields.io/badge/Ask_Zread-_.svg?style=flat&color=00b0aa&labelColor=000000&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQuOTYxNTYgMS42MDAxSDIuMjQxNTZDMS44ODgxIDEuNjAwMSAxLjYwMTU2IDEuODg2NjQgMS42MDE1NiAyLjI0MDFWNC45NjAxQzEuNjAxNTYgNS4zMTM1NiAxLjg4ODEgNS42MDAxIDIuMjQxNTYgNS42MDAxSDQuOTYxNTZDNS4zMTUwMiA1LjYwMDEgNS42MDE1NiA1LjMxMzU2IDUuNjAxNTYgNC45NjAxVjIuMjQwMUM1LjYwMTU2IDEuODg2NjQgNS4zMTUwMiAxLjYwMDEgNC45NjE1NiAxLjYwMDFaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00Ljk2MTU2IDEwLjM5OTlIMi4yNDE1NkMxLjg4ODEgMTAuMzk5OSAxLjYwMTU2IDEwLjY4NjQgMS42MDE1NiAxMS4wMzk5VjEzLjc1OTlDMS42MDE1NiAxNC4xMTM0IDEuODg4MSAxNC4zOTk5IDIuMjQxNTYgMTQuMzk5OUg0Ljk2MTU2QzUuMzE1MDIgMTQuMzk5OSA1LjYwMTU2IDE0LjExMzQgNS42MDE1NiAxMy43NTk5VjExLjAzOTlDNS42MDE1NiAxMC42ODY0IDUuMzE1MDIgMTAuMzk5OSA0Ljk2MTU2IDEwLjM5OTlaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik0xMy43NTg0IDEuNjAwMUgxMS4wMzg0QzEwLjY4NSAxLjYwMDEgMTAuMzk4NCAxLjg4NjY0IDEwLjM5ODQgMi4yNDAxVjQuOTYwMUMxMC4zOTg0IDUuMzEzNTYgMTAuNjg1IDUuNjAwMSAxMS4wMzg0IDUuNjAwMUgxMy43NTg0QzE0LjExMTkgNS42MDAxIDE0LjM5ODQgNS4zMTM1NiAxNC4zOTg0IDQuOTYwMVYyLjI0MDFDMTQuMzk4NCAxLjg4NjY0IDE0LjExMTkgMS42MDAxIDEzLjc1ODQgMS42MDAxWiIgZmlsbD0iI2ZmZiIvPgo8cGF0aCBkPSJNNCAxMkwxMiA0TDQgMTJaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00IDEyTDEyIDQiIHN0cm9rZT0iI2ZmZiIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8L3N2Zz4K&logoColor=ffffff)](https://zread.ai/Haehnchen/idea-vuejs-plugin)
[![Version](http://phpstorm.espend.de/badge/23832/version)](https://plugins.jetbrains.com/plugin/23832)
[![Downloads](http://phpstorm.espend.de/badge/23832/downloads)](https://plugins.jetbrains.com/plugin/23832)
[![Downloads last month](http://phpstorm.espend.de/badge/23832/last-month)](https://plugins.jetbrains.com/plugin/23832)

| Key           | Value                                                     |
|---------------|-----------------------------------------------------------|
| Plugin Url    | https://plugins.jetbrains.com/plugin/23832-vue-js-toolbox |
| ID            | de.espend.idea.vuejs                                      |
| Documentation | https://espend.de/phpstorm/plugin/vuejs-toolbox           |
| Changelog     | [CHANGELOG](CHANGELOG.md)                                 |

## Install

* Install the plugin by going to `Settings -> Plugins -> Browse repositories` and then search for `Vue.js Toolbox`.

## MCP Support

Provides MCP tools for AI assistants. See [MCP Server Plugin](https://plugins.jetbrains.com/plugin/26071-mcp-server) for setup instructions.

### Available Tools

| Tool | Description |
|------|-------------|
| `list_vue_components` | Lists Vue.js components with name and file path |
| `list_vue_component_usages` | Lists Vue.js components with unique usage file paths |

## Component Usages

### Navigation to component usages

```vue
<-- ButtonCounter.vue -->
<template/>
```

#### Target

```vue
<-- AnotherFile.vue -->
<script>
import ButtonCounter from './ButtonCounter.vue'
</script>

<template>
  <ButtonCounter />
  <button-counter />
</template>
```

### Infile navigation via LineMarker

```vue
<template>
  <ButtonCounter />
  <button-counter />
</template>
```

#### Targets

```vue
<script setup>
    import ButtonCounter from './ButtonCounter.vue'
</script>
```

```javascript
export default {
  components: {
    ButtonCounter
  }
}
```

```javascript
@Component({
  components: {
    ButtonCounter,
  },
})
export default {}
```

Entrypoint
---------------------

# Linemarker for vue.js targeting its creation

```javascript
import App from './App.vue'
const app = createApp(App)
```

```javascript
import Vue from 'vue'
import App from './App.vue'

const app = new Vue({
    render: (createElement) => createElement(App)
});
```

```javascript
import App from './App.vue';
new App().$mount(el);
```
