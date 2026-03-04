package de.espend.idea.vuejs.mcp.collector

import com.intellij.javascript.nodejs.packageJson.PackageJsonFileManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.espend.idea.vuejs.index.ComponentUsageIndex
import de.espend.idea.vuejs.mcp.McpCsvUtil
import de.espend.idea.vuejs.utils.VueJsUtil
import org.jetbrains.vuejs.lang.html.VueFile
import org.jetbrains.vuejs.model.VueModelManager

class VueComponentUsageCollector(private val project: Project) {

    fun collect(search: String?): String {
        val normalizedSearch = search
            ?.trim()
            ?.lowercase()
            ?.takeIf { it.isNotEmpty() }

        val rows = ApplicationManager.getApplication().runReadAction<List<ComponentUsageRow>> {
            collectRows(normalizedSearch)
        }

        return buildString {
            append(HEADER)
            append('\n')

            rows.forEach { row ->
                append(McpCsvUtil.escapeCsv(row.filePath))
                append(',')
                append(McpCsvUtil.escapeCsv(row.componentNameKebab))
                append(',')
                append(McpCsvUtil.escapeCsv(row.componentNamePascal))
                append(',')
                append(McpCsvUtil.escapeCsv(row.usages.joinToString(",")))
                append('\n')
            }
        }
    }

    private fun collectRows(search: String?): List<ComponentUsageRow> {
        val psiManager = PsiManager.getInstance(project)
        val components = LinkedHashMap<String, ComponentUsageRow>()
        val projectScope = GlobalSearchScope.projectScope(project)

        FileBasedIndex.getInstance().ensureUpToDate(ComponentUsageIndex.KEY, project, projectScope)

        val candidateFiles = getProjectVueFiles()

        candidateFiles.forEach { componentFile ->
            val psiFile = psiManager.findFile(componentFile) as? VueFile ?: return@forEach
            val component = VueModelManager.Companion.getComponent(psiFile)

            val sourceFile = component?.source?.containingFile?.virtualFile ?: componentFile
            val pascalName = toPascalCase(component?.defaultName ?: sourceFile.nameWithoutExtension)
            val kebabName = VueJsUtil.convertToKebabCase(pascalName)
            val relativePath = toProjectRelativePath(sourceFile.path)

            val usages = findUsagesForComponent(sourceFile, sourceFile.nameWithoutExtension)

            val row = ComponentUsageRow(
                filePath = relativePath,
                componentNameKebab = kebabName,
                componentNamePascal = pascalName,
                usages = usages
            )

            if (search != null && !matchesSearch(row, search)) {
                return@forEach
            }

            components[relativePath] = row
        }

        return components.values
            .sortedWith(compareBy<ComponentUsageRow> { it.filePath }.thenBy { it.componentNamePascal })
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

    private fun findUsagesForComponent(componentFile: VirtualFile, filenameWithoutExtension: String): List<String> {
        val usageFiles = linkedSetOf<String>()
        val files = FileBasedIndex.getInstance().getContainingFiles(
            ComponentUsageIndex.KEY,
            filenameWithoutExtension,
            GlobalSearchScope.allScope(project)
        )

        for (candidateUsageFile in files) {
            val psiUsage = PsiManager.getInstance(project).findFile(candidateUsageFile) as? VueFile ?: continue
            val fileData = FileBasedIndex.getInstance().getFileData(ComponentUsageIndex.KEY, candidateUsageFile, project)

            fileData.forEach { (_, value) ->
                if (value.size < 2) {
                    return@forEach
                }

                val refImport = value[0]
                val importAlias = value[1]

                val isTarget = if (refImport.startsWith(".")) {
                    val relativeFile = VfsUtil.findRelativeFile(refImport, candidateUsageFile)
                    componentFile == relativeFile
                } else {
                    var normalizedImport = refImport.replace("\\", "/")
                    if (normalizedImport.startsWith("~/")) {
                        normalizedImport = normalizedImport.substring(2)
                    }

                    isImportInScope(componentFile, normalizedImport)
                }

                if (!isTarget) {
                    return@forEach
                }

                val tags = VueJsUtil.getTemplateTags(psiUsage, importAlias, VueJsUtil.convertToKebabCase(importAlias))
                if (tags.isNotEmpty()) {
                    usageFiles.add(toProjectRelativePath(candidateUsageFile.path))
                }
            }
        }

        return usageFiles.sorted()
    }

    private fun isImportInScope(componentFile: VirtualFile, foreignImport: String): Boolean {
        for (packageJsonFile in PackageJsonFileManager.getInstance(project).validPackageJsonFiles) {
            val relativePath = VfsUtil.getRelativePath(componentFile, packageJsonFile.parent, '/') ?: continue
            if (relativePath.isEmpty()) {
                continue
            }

            val relativeFile = VfsUtil.findRelativeFile(foreignImport, packageJsonFile)
            if (componentFile == relativeFile) {
                return true
            }
        }

        return false
    }

    private fun matchesSearch(row: ComponentUsageRow, search: String): Boolean {
        if (row.filePath.contains(search, true)) {
            return true
        }

        if (row.componentNameKebab.contains(search, true)) {
            return true
        }

        if (row.componentNamePascal.contains(search, true)) {
            return true
        }

        return false
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

    private data class ComponentUsageRow(
        val filePath: String,
        val componentNameKebab: String,
        val componentNamePascal: String,
        val usages: List<String>
    )

    companion object {
        private const val HEADER = "file_path,component_name_kebab,component_name_pascal,usages"
    }
}
