package de.espend.idea.vuejs.mcp.collector

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.util.indexing.FileBasedIndex
import de.espend.idea.vuejs.mcp.McpCsvUtil
import de.espend.idea.vuejs.utils.VueJsUtil
import org.jetbrains.vuejs.lang.html.VueFile
import org.jetbrains.vuejs.model.VueModelManager

class VueComponentCollector(private val project: Project) {

    fun collect(search: String?): String {
        val normalizedSearch = search
            ?.trim()
            ?.lowercase()
            ?.takeIf { it.isNotEmpty() }

        val rows = ApplicationManager.getApplication().runReadAction<List<ComponentRow>> {
            collectRows(normalizedSearch)
        }

        return buildString {
            append(HEADER)
            append('\n')

            rows.forEach { row ->
                append(McpCsvUtil.escapeCsv(row.componentNamePascal))
                append(',')
                append(McpCsvUtil.escapeCsv(row.componentNameKebab))
                append(',')
                append(McpCsvUtil.escapeCsv(row.filePath))
                append('\n')
            }
        }
    }

    private fun collectRows(search: String?): List<ComponentRow> {
        val psiManager = PsiManager.getInstance(project)
        val rows = LinkedHashMap<String, ComponentRow>()

        val files = getProjectVueFiles()

        files.forEach { virtualFile ->
            val psiFile = psiManager.findFile(virtualFile) as? VueFile ?: return@forEach
            val component = VueModelManager.Companion.getComponent(psiFile)

            val sourceFile = component?.source?.containingFile?.virtualFile ?: virtualFile
            val inferredName = component?.defaultName.takeUnless { it.isNullOrBlank() }
                ?: sourceFile.nameWithoutExtension

            val pascalName = toPascalCase(inferredName)
            val kebabName = VueJsUtil.convertToKebabCase(pascalName)
            val relativePath = toProjectRelativePath(sourceFile.path)

            if (search != null && !pascalName.contains(search, true) && !kebabName.contains(search, true)) {
                return@forEach
            }

            rows.putIfAbsent(relativePath, ComponentRow(pascalName, kebabName, relativePath))
        }

        return rows.values
            .sortedWith(compareBy<ComponentRow> { it.filePath }.thenBy { it.componentNamePascal })
    }

    private fun getProjectVueFiles(): List<VirtualFile> {
        val files = linkedSetOf<VirtualFile>()

        FileBasedIndex.getInstance().iterateIndexableFiles({ file ->
            if (!file.isDirectory && "vue".equals(file.extension, true)) {
                files.add(file)
            }
            true
        }, project, null)

        if (files.isEmpty()) {
            for (contentRoot in ProjectRootManager.getInstance(project).contentRoots) {
                if (!contentRoot.isDirectory) {
                    continue
                }
                for (child in contentRoot.children) {
                    if (!child.isDirectory && "vue".equals(child.extension, true)) {
                        files.add(child)
                    }
                }
            }
        }

        return files.sortedBy { it.path }
    }

    private fun toProjectRelativePath(filePath: String): String {
        val basePath = project.basePath
            ?: ProjectRootManager.getInstance(project).contentRoots.firstOrNull()?.path
            ?: return filePath
        val normalizedBasePath = basePath.trimEnd('/', '\\')
        val normalizedFilePath = filePath.replace('\\', '/')
        val normalizedPrefix = "$normalizedBasePath/".replace('\\', '/')

        return if (normalizedFilePath.startsWith(normalizedPrefix)) {
            normalizedFilePath.substring(normalizedPrefix.length)
        } else {
            normalizedFilePath
        }
    }

    private fun toPascalCase(name: String): String {
        if (name.isBlank()) {
            return "Component"
        }

        val parts = name.split(Regex("[-_\\s]+"))
            .filter { it.isNotBlank() }

        if (parts.size > 1) {
            return parts.joinToString(separator = "") { part ->
                part.replaceFirstChar { c -> c.uppercaseChar() }
            }
        }

        return name.replaceFirstChar { c -> c.uppercaseChar() }
    }

    private data class ComponentRow(
        val componentNamePascal: String,
        val componentNameKebab: String,
        val filePath: String,
    )

    companion object {
        private const val HEADER = "component_name_pascal,component_name_kebab,file_path"
    }
}
