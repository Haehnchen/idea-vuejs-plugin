@file:Suppress("FunctionName", "unused")

package de.espend.idea.vuejs.mcp.toolset

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.mcpserver.project
import com.intellij.openapi.application.readAction
import de.espend.idea.vuejs.mcp.collector.VueAppEntrypointCollector
import de.espend.idea.vuejs.mcp.collector.VueComponentCollector
import de.espend.idea.vuejs.mcp.collector.VueComponentUsageCollector
import kotlinx.coroutines.currentCoroutineContext

class VueComponentMcpToolset : McpToolset {

    @McpTool(name = "list_vue_components")
    @McpDescription(
        description = """
        Lists all Vue.js components in the project.

        Returns CSV format with columns:
        component_name_pascal,component_name_kebab,file_path

        - component_name_pascal: Component name in PascalCase (e.g. HelloWorld)
        - component_name_kebab: Component name in kebab-case (e.g. hello-world)
        - file_path: Relative path to the component file from project root
    """
    )
    suspend fun list_vue_components(
        @McpDescription(description = "Optional partial component name filter (case-insensitive)")
        search: String? = null,
    ): String {
        val project = currentCoroutineContext().project

        return readAction {
            VueComponentCollector(project).collect(search)
        }
    }

    @McpTool(name = "list_vue_component_usages")
    @McpDescription(
        description = """
        Lists Vue.js components and usage files in CSV format.

        Returns CSV format with columns:
        file_path,component_name_kebab,component_name_pascal,usages

        - file_path: Relative path to the component file from project root
        - component_name_kebab: Component name in kebab-case (e.g. hello-world)
        - component_name_pascal: Component name in PascalCase (e.g. HelloWorld)
        - usages: Comma-separated unique relative file paths where the component is used
    """
    )
    suspend fun list_vue_component_usages(
        @McpDescription(description = "Optional partial filter by component path/name (path, PascalCase or kebab-case)")
        search: String? = null,
    ): String {
        val project = currentCoroutineContext().project

        return readAction {
            VueComponentUsageCollector(project).collect(search)
        }
    }

    @McpTool(name = "list_vue_app_entrypoints")
    @McpDescription(
        description = """
        Lists all Vue.js template files that are used as application entrypoints.

        Returns CSV format with columns:
        vue_template,entrypoint_file,detection_pattern

        - vue_template: Relative path (from project root) to the Vue template file used as root component (e.g. src/App.vue)
        - entrypoint_file: Relative path (from project root) to the JS/TS file that initializes the Vue app (e.g. src/main.js)
        - detection_pattern: Vue initialization pattern detected in the entrypoint file

        Supported detection patterns:
        - createApp     Vue 3:  createApp(App).mount('#app')
        - createElement Vue 2:  new Vue({ render: h => h(App) })
        - ${'$'}mount       Vue 2:  new App().${'$'}mount('#app')

        All file paths are relative to the project root.
    """
    )
    suspend fun list_vue_app_entrypoints(
        @McpDescription(description = "Optional partial filter on the full relative vue_template path from project root (case-insensitive, e.g. 'src/App.vue', 'App', 'src/')")
        search: String? = null,
    ): String {
        val project = currentCoroutineContext().project

        return readAction {
            VueAppEntrypointCollector(project).collect(search)
        }
    }
}
