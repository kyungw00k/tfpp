package tfpp.core

import tfpp.data.DataLoader
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

/**
 * File processor for handling single files and directory recursion
 */
class FileProcessor(
    private val templateEngine: TemplateEngine,
    private val dataLoader: DataLoader
) {
    
    /**
     * Process a single template file
     */
    fun processSingleFile(
        templateFile: File,
        outputFile: File?,
        contextData: Map<String, Any>,
        templateMode: org.thymeleaf.templatemode.TemplateMode? = null
    ): String {
        val result = templateEngine.processTemplate(templateFile, contextData, templateMode)
        
        outputFile?.let { output ->
            output.parentFile?.mkdirs()
            output.writeText(result)
        }
        
        return result
    }
    
    /**
     * Process all template files in a directory recursively
     */
    fun processDirectory(
        sourceDir: File,
        outputDir: File,
        contextData: Map<String, Any>,
        templateExtensions: Set<String> = DEFAULT_TEMPLATE_EXTENSIONS,
        verbose: Boolean = false
    ): ProcessingResult {
        if (!sourceDir.isDirectory) {
            throw IllegalArgumentException("Source path is not a directory: ${sourceDir.absolutePath}")
        }
        
        outputDir.mkdirs()
        
        val processedFiles = mutableListOf<ProcessedFile>()
        val errors = mutableListOf<ProcessingError>()
        
        Files.walk(sourceDir.toPath()).use { paths ->
            paths.filter { it.isRegularFile() }
                .filter { isTemplateFile(it, templateExtensions) }
                .forEach { templatePath ->
                    try {
                        val relativePath = sourceDir.toPath().relativize(templatePath)
                        val outputPath = outputDir.toPath().resolve(relativePath)
                        
                        if (verbose) {
                            println("Processing: $relativePath")
                        }
                        
                        val templateFile = templatePath.toFile()
                        val outputFile = outputPath.toFile()
                        
                        // Create output directory if needed
                        outputFile.parentFile?.mkdirs()
                        
                        // Process the template
                        val result = templateEngine.processTemplate(templateFile, contextData)
                        outputFile.writeText(result)
                        
                        processedFiles.add(
                            ProcessedFile(
                                sourcePath = templatePath,
                                outputPath = outputPath,
                                templateMode = templateEngine.detectTemplateMode(templateFile)
                            )
                        )
                        
                    } catch (e: Exception) {
                        errors.add(
                            ProcessingError(
                                filePath = templatePath,
                                error = e,
                                message = "Failed to process template: ${e.message}"
                            )
                        )
                        
                        if (verbose) {
                            println("Error processing $templatePath: ${e.message}")
                        }
                    }
                }
        }
        
        return ProcessingResult(
            processedFiles = processedFiles,
            errors = errors,
            totalProcessed = processedFiles.size,
            totalErrors = errors.size
        )
    }
    
    /**
     * Check if a file should be treated as a template based on extension
     */
    private fun isTemplateFile(path: Path, templateExtensions: Set<String>): Boolean {
        val extension = path.extension.lowercase()
        return extension in templateExtensions || extension.isEmpty() // Include files without extension
    }
    
    /**
     * Copy non-template files from source to output directory
     */
    fun copyNonTemplateFiles(
        sourceDir: File,
        outputDir: File,
        templateExtensions: Set<String> = DEFAULT_TEMPLATE_EXTENSIONS,
        verbose: Boolean = false
    ): CopyResult {
        val copiedFiles = mutableListOf<Path>()
        val errors = mutableListOf<ProcessingError>()
        
        Files.walk(sourceDir.toPath()).use { paths ->
            paths.filter { it.isRegularFile() }
                .filter { !isTemplateFile(it, templateExtensions) }
                .forEach { sourcePath ->
                    try {
                        val relativePath = sourceDir.toPath().relativize(sourcePath)
                        val outputPath = outputDir.toPath().resolve(relativePath)
                        
                        if (verbose) {
                            println("Copying: $relativePath")
                        }
                        
                        outputPath.parent?.let { Files.createDirectories(it) }
                        Files.copy(sourcePath, outputPath, 
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING)
                        
                        copiedFiles.add(outputPath)
                        
                    } catch (e: Exception) {
                        errors.add(
                            ProcessingError(
                                filePath = sourcePath,
                                error = e,
                                message = "Failed to copy file: ${e.message}"
                            )
                        )
                        
                        if (verbose) {
                            println("Error copying $sourcePath: ${e.message}")
                        }
                    }
                }
        }
        
        return CopyResult(
            copiedFiles = copiedFiles,
            errors = errors,
            totalCopied = copiedFiles.size,
            totalErrors = errors.size
        )
    }
    
    companion object {
        val DEFAULT_TEMPLATE_EXTENSIONS = setOf(
            "html", "htm", "xml", "txt", "text", 
            "css", "js", "json", "yaml", "yml",
            "md", "markdown", "sql", "properties"
        )
    }
}

/**
 * Result of processing templates in a directory
 */
data class ProcessingResult(
    val processedFiles: List<ProcessedFile>,
    val errors: List<ProcessingError>,
    val totalProcessed: Int,
    val totalErrors: Int
) {
    val isSuccess: Boolean get() = totalErrors == 0
}

/**
 * Information about a successfully processed file
 */
data class ProcessedFile(
    val sourcePath: Path,
    val outputPath: Path,
    val templateMode: org.thymeleaf.templatemode.TemplateMode
)

/**
 * Information about a processing error
 */
data class ProcessingError(
    val filePath: Path,
    val error: Exception,
    val message: String
)

/**
 * Result of copying non-template files
 */
data class CopyResult(
    val copiedFiles: List<Path>,
    val errors: List<ProcessingError>,
    val totalCopied: Int,
    val totalErrors: Int
) {
    val isSuccess: Boolean get() = totalErrors == 0
}