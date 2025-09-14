package tfpp.core

import io.methvin.watcher.DirectoryChangeEvent
import io.methvin.watcher.DirectoryWatcher
import kotlinx.coroutines.*
import java.io.File
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean

/**
 * File watcher for monitoring template changes and triggering reprocessing
 */
class FileWatcher(
    private val sourceDir: File,
    private val templateEngine: TemplateEngine,
    private val fileProcessor: FileProcessor,
    private val contextData: Map<String, Any>,
    private val verbose: Boolean = false
) {
    
    private val isWatching = AtomicBoolean(false)
    private var watcherJob: Job? = null
    private var directoryWatcher: DirectoryWatcher? = null
    
    /**
     * Start watching for file changes
     */
    fun startWatching(
        outputDir: File,
        templateExtensions: Set<String> = FileProcessor.DEFAULT_TEMPLATE_EXTENSIONS
    ) {
        if (isWatching.get()) {
            if (verbose) println("File watcher is already running")
            return
        }
        
        isWatching.set(true)
        
        if (verbose) {
            println("Starting file watcher for: ${sourceDir.absolutePath}")
            println("Output directory: ${outputDir.absolutePath}")
            println("Watching extensions: $templateExtensions")
        }
        
        try {
            directoryWatcher = DirectoryWatcher.builder()
                .path(sourceDir.toPath())
                .listener { event ->
                    handleFileChange(event, outputDir, templateExtensions)
                }
                .build()
            
            watcherJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    directoryWatcher?.watchAsync()?.get()
                } catch (e: InterruptedException) {
                    if (verbose) println("File watcher interrupted")
                } catch (e: Exception) {
                    if (verbose) println("File watcher error: ${e.message}")
                }
            }
            
            if (verbose) {
                println("File watcher started successfully")
                println("Press Ctrl+C to stop watching and exit")
            }
            
        } catch (e: Exception) {
            isWatching.set(false)
            throw RuntimeException("Failed to start file watcher: ${e.message}", e)
        }
    }
    
    /**
     * Stop watching for file changes
     */
    fun stopWatching() {
        if (!isWatching.get()) {
            return
        }
        
        if (verbose) {
            println("Stopping file watcher...")
        }
        
        isWatching.set(false)
        
        try {
            directoryWatcher?.close()
            watcherJob?.cancel()
            
            if (verbose) {
                println("File watcher stopped")
            }
        } catch (e: Exception) {
            if (verbose) {
                println("Error stopping file watcher: ${e.message}")
            }
        }
    }
    
    /**
     * Handle file change events
     */
    private fun handleFileChange(
        event: DirectoryChangeEvent,
        outputDir: File,
        templateExtensions: Set<String>
    ) {
        val changedPath = event.path()
        val relativePath = sourceDir.toPath().relativize(changedPath)
        
        when (event.eventType()) {
            DirectoryChangeEvent.EventType.CREATE,
            DirectoryChangeEvent.EventType.MODIFY -> {
                if (isTemplateFile(changedPath, templateExtensions)) {
                    processChangedTemplate(changedPath, outputDir, relativePath)
                } else {
                    copyChangedAsset(changedPath, outputDir, relativePath)
                }
            }
            
            DirectoryChangeEvent.EventType.DELETE -> {
                handleDeletedFile(outputDir, relativePath)
            }
            
            else -> {
                // Ignore other event types
            }
        }
    }
    
    /**
     * Process a changed template file
     */
    private fun processChangedTemplate(
        templatePath: Path,
        outputDir: File,
        relativePath: Path
    ) {
        try {
            val templateFile = templatePath.toFile()
            val outputPath = outputDir.toPath().resolve(relativePath)
            val outputFile = outputPath.toFile()
            
            if (verbose) {
                println("Template changed: $relativePath")
            }
            
            // Create output directory if needed
            outputFile.parentFile?.mkdirs()
            
            // Process the template
            val result = templateEngine.processTemplate(templateFile, contextData)
            outputFile.writeText(result)
            
            if (verbose) {
                println("Template processed: $relativePath")
            }
            
        } catch (e: Exception) {
            if (verbose) {
                println("Error processing changed template $relativePath: ${e.message}")
            }
        }
    }
    
    /**
     * Copy a changed asset file
     */
    private fun copyChangedAsset(
        assetPath: Path,
        outputDir: File,
        relativePath: Path
    ) {
        try {
            val outputPath = outputDir.toPath().resolve(relativePath)
            
            if (verbose) {
                println("Asset changed: $relativePath")
            }
            
            // Create output directory if needed
            outputPath.parent?.let { java.nio.file.Files.createDirectories(it) }
            
            // Copy the file
            java.nio.file.Files.copy(
                assetPath, 
                outputPath, 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            )
            
            if (verbose) {
                println("Asset copied: $relativePath")
            }
            
        } catch (e: Exception) {
            if (verbose) {
                println("Error copying changed asset $relativePath: ${e.message}")
            }
        }
    }
    
    /**
     * Handle deleted files
     */
    private fun handleDeletedFile(
        outputDir: File,
        relativePath: Path
    ) {
        try {
            val outputPath = outputDir.toPath().resolve(relativePath)
            val outputFile = outputPath.toFile()
            
            if (outputFile.exists()) {
                outputFile.delete()
                
                if (verbose) {
                    println("Deleted output file: $relativePath")
                }
            }
            
        } catch (e: Exception) {
            if (verbose) {
                println("Error deleting output file $relativePath: ${e.message}")
            }
        }
    }
    
    /**
     * Check if a file should be treated as a template
     */
    private fun isTemplateFile(path: Path, templateExtensions: Set<String>): Boolean {
        val extension = path.fileName.toString().substringAfterLast('.', "").lowercase()
        return extension in templateExtensions || extension.isEmpty()
    }
    
    /**
     * Wait for the watcher to complete (blocks current thread)
     */
    suspend fun awaitCompletion() {
        watcherJob?.join()
    }
    
    /**
     * Check if the watcher is currently running
     */
    fun isRunning(): Boolean = isWatching.get()
}