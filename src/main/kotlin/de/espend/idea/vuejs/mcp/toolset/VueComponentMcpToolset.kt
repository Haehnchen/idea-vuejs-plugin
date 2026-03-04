package de.espend.idea.vuejs.mcp.toolset

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.openapi.project.ProjectManager
import de.espend.idea.vuejs.mcp.collector.VueComponentCollector
import de.espend.idea.vuejs.mcp.collector.VueComponentUsageCollector

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
        val project = ProjectManager.getInstance().openProjects.firstOrNull { !it.isDisposed }
            ?: return "component_name_pascal,component_name_kebab,file_path\n"

        return VueComponentCollector(project).collect(search)
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
        val project = ProjectManager.getInstance().openProjects.firstOrNull { !it.isDisposed }
            ?: return "file_path,component_name_kebab,component_name_pascal,usages\n"

        return VueComponentUsageCollector(project).collect(search)
    }
}
