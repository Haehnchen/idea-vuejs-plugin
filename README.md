# IntelliJ IDEA / PhpStorm Vue.js Toolbox Plugin

[![Build Status](https://github.com/Haehnchen/idea-vuejs-plugin/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/Haehnchen/idea-vuejs-plugin/actions/workflows/gradle.yml)
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