package tfpp.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import tfpp.core.TemplateEngine
import tfpp.core.FileProcessor
import tfpp.core.FileWatcher
import tfpp.data.DataLoader
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Main CLI application class for TFPP (Thymeleaf File Preprocessor)
 */
class TfppCli : CliktCommand(
    name = "tfpp",
    help = "Thymeleaf File Preprocessor - A powerful CLI template processing tool"
) {
    init {
        subcommands(ProcessCommand(), ProcessDirCommand())
    }

    override fun run() {
        // Main command without subcommands shows help
        if (currentContext.invokedSubcommand == null) {
            echo(getFormattedHelp())
        }
    }
}

/**
 * Process command for template processing
 */
class ProcessCommand : CliktCommand(
    name = "process",
    help = "Process templates with data binding"
) {
    private val template by argument(
        name = "template",
        help = "Template file to process"
    ).file(mustExist = true, canBeDir = false).optional()

    private val dataFiles by option(
        "-d", "--data",
        help = "Data file (JSON/YAML) to bind to template"
    ).file(mustExist = true, canBeDir = false).multiple()

    private val variables by option(
        "-v", "--var",
        help = "Direct variable assignment (key=value)"
    ).multiple()

    private val output by option(
        "-o", "--output",
        help = "Output file or directory"
    ).file()

    private val mode by option(
        "-m", "--mode",
        help = "Force template mode (HTML, TEXT, CSS, JAVASCRIPT)"
    ).choice("HTML", "TEXT", "CSS", "JAVASCRIPT")

    private val config by option(
        "-c", "--config",
        help = "Configuration file"
    ).file(mustExist = true, canBeDir = false)

    private val watch by option(
        "-w", "--watch",
        help = "Watch for file changes and reprocess"
    ).flag()

    private val verbose by option(
        "--verbose",
        help = "Enable verbose logging"
    ).flag()

    override fun run() {
        if (verbose) {
            echo("TFPP - Thymeleaf File Preprocessor")
        }

        if (template == null) {
            echo("Error: Template file is required", err = true)
            throw com.github.ajalt.clikt.core.UsageError("Template file must be specified")
        }

        try {
            processTemplate()
        } catch (e: Exception) {
            echo("Error: ${e.message}", err = true)
            if (verbose) {
                e.printStackTrace()
            }
            throw com.github.ajalt.clikt.core.Abort()
        }
    }
    
    private fun processTemplate() {
        val templateFile = template!!
        val templateEngine = TemplateEngine()
        val dataLoader = DataLoader()
        
        if (verbose) {
            echo("Processing template: ${templateFile.absolutePath}")
        }
        
        // Load data from various sources
        val contextData = mutableMapOf<String, Any>()
        
        // 1. Load data from files
        dataFiles.forEach { dataFile ->
            if (verbose) {
                echo("Loading data from: ${dataFile.name}")
            }
            val fileData = dataLoader.loadFromFile(dataFile)
            contextData.putAll(fileData)
        }
        
        // 2. Parse key-value variables
        if (variables.isNotEmpty()) {
            if (verbose) {
                echo("Processing variables: $variables")
            }
            val variableData = dataLoader.parseKeyValuePairs(variables)
            contextData.putAll(variableData)
        }
        
        // 3. Determine template mode
        val templateMode = mode?.let { templateEngine.getTemplateModeFromString(it) }
        
        if (verbose) {
            val detectedMode = templateMode ?: templateEngine.detectTemplateMode(templateFile)
            echo("Template mode: $detectedMode")
            echo("Context variables: ${contextData.keys}")
        }
        
        // 4. Process the template
        val result = templateEngine.processTemplate(templateFile, contextData, templateMode)
        
        // 5. Output result
        if (output != null) {
            output!!.writeText(result)
            echo("Output written to: ${output!!.absolutePath}")
        } else {
            // Print to stdout
            println(result)
        }
        
        if (verbose) {
            echo("Template processing completed successfully")
        }
    }
}

/**
 * Process directory command for recursive template processing
 */
class ProcessDirCommand : CliktCommand(
    name = "process-dir",
    help = "Process all templates in a directory recursively"
) {
    private val sourceDir by argument(
        name = "source",
        help = "Source directory containing templates"
    ).file(mustExist = true, canBeFile = false)

    private val outputDir by argument(
        name = "output", 
        help = "Output directory for processed files"
    ).file(canBeFile = false)

    private val dataFiles by option(
        "-d", "--data",
        help = "Data file (JSON/YAML) to bind to templates"
    ).file(mustExist = true, canBeDir = false).multiple()

    private val variables by option(
        "-v", "--var",
        help = "Direct variable assignment (key=value)"
    ).multiple()

    private val config by option(
        "-c", "--config",
        help = "Configuration file"
    ).file(mustExist = true, canBeDir = false)

    private val copyNonTemplates by option(
        "--copy-assets",
        help = "Copy non-template files to output directory"
    ).flag(default = true)

    private val templateExtensions by option(
        "--template-ext",
        help = "Template file extensions (comma-separated)"
    ).default("html,htm,xml,txt,text,css,js,json,yaml,yml,md,markdown,sql,properties")

    private val watch by option(
        "-w", "--watch",
        help = "Watch for file changes and reprocess"
    ).flag()

    private val verbose by option(
        "--verbose",
        help = "Enable verbose logging"
    ).flag()

    override fun run() {
        if (verbose) {
            echo("TFPP - Processing directory: ${sourceDir.absolutePath}")
        }

        try {
            processDirectory()
        } catch (e: Exception) {
            echo("Error: ${e.message}", err = true)
            if (verbose) {
                e.printStackTrace()
            }
            throw com.github.ajalt.clikt.core.Abort()
        }
    }
    
    private fun processDirectory() {
        val templateEngine = TemplateEngine()
        val dataLoader = DataLoader()
        val fileProcessor = FileProcessor(templateEngine, dataLoader)
        
        // Load data from various sources
        val contextData = mutableMapOf<String, Any>()
        
        // 1. Load data from files
        dataFiles.forEach { dataFile ->
            if (verbose) {
                echo("Loading data from: ${dataFile.name}")
            }
            val fileData = dataLoader.loadFromFile(dataFile)
            contextData.putAll(fileData)
        }
        
        // 2. Parse key-value variables
        if (variables.isNotEmpty()) {
            if (verbose) {
                echo("Processing variables: $variables")
            }
            val variableData = dataLoader.parseKeyValuePairs(variables)
            contextData.putAll(variableData)
        }
        
        if (verbose) {
            echo("Context variables: ${contextData.keys}")
        }
        
        // Parse template extensions
        val extensions = templateExtensions.split(",").map { it.trim().lowercase() }.toSet()
        
        // Process templates
        val result = fileProcessor.processDirectory(
            sourceDir = sourceDir,
            outputDir = outputDir,
            contextData = contextData,
            templateExtensions = extensions,
            verbose = verbose
        )
        
        // Copy non-template files if requested
        val copyResult = if (copyNonTemplates) {
            fileProcessor.copyNonTemplateFiles(
                sourceDir = sourceDir,
                outputDir = outputDir,
                templateExtensions = extensions,
                verbose = verbose
            )
        } else {
            null
        }
        
        // Report results
        echo("Templates processed: ${result.totalProcessed}")
        if (result.totalErrors > 0) {
            echo("Template errors: ${result.totalErrors}", err = true)
            result.errors.forEach { error ->
                echo("  ${error.filePath}: ${error.message}", err = true)
            }
        }
        
        copyResult?.let { copy ->
            echo("Assets copied: ${copy.totalCopied}")
            if (copy.totalErrors > 0) {
                echo("Copy errors: ${copy.totalErrors}", err = true)
                copy.errors.forEach { error ->
                    echo("  ${error.filePath}: ${error.message}", err = true)
                }
            }
        }
        
        if (result.isSuccess && (copyResult?.isSuccess != false)) {
            echo("Directory processing completed successfully")
            
            // Start watch mode if requested
            if (watch) {
                startWatchMode(templateEngine, dataLoader, fileProcessor, contextData, extensions)
            }
        } else {
            throw com.github.ajalt.clikt.core.Abort()
        }
    }
    
    private fun startWatchMode(
        templateEngine: TemplateEngine,
        dataLoader: DataLoader,
        fileProcessor: FileProcessor,
        contextData: Map<String, Any>,
        templateExtensions: Set<String>
    ) {
        echo("Starting watch mode...")
        
        val fileWatcher = FileWatcher(
            sourceDir = sourceDir,
            templateEngine = templateEngine,
            fileProcessor = fileProcessor,
            contextData = contextData,
            verbose = verbose
        )
        
        try {
            fileWatcher.startWatching(outputDir, templateExtensions)
            
            // Add shutdown hook to clean up
            Runtime.getRuntime().addShutdownHook(Thread {
                fileWatcher.stopWatching()
            })
            
            // Block and wait for changes
            runBlocking {
                fileWatcher.awaitCompletion()
            }
            
        } catch (e: Exception) {
            echo("Watch mode error: ${e.message}", err = true)
            fileWatcher.stopWatching()
            throw com.github.ajalt.clikt.core.Abort()
        }
    }
}

/**
 * Main entry point for the application
 */
fun main(args: Array<String>) {
    TfppCli().main(args)
}
