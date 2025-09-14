package tfpp.core

import org.thymeleaf.TemplateEngine as ThymeleafEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templatemode.TemplateMode
import org.thymeleaf.templateresolver.FileTemplateResolver
import java.io.File
import java.nio.file.Path
import java.util.*

/**
 * Core template processing engine powered by Thymeleaf
 */
class TemplateEngine {
    
    private val thymeleafEngine: ThymeleafEngine = ThymeleafEngine()
    
    init {
        setupTemplateResolvers()
    }
    
    /**
     * Set up template resolvers for different template modes
     */
    private fun setupTemplateResolvers() {
        val fileResolver = FileTemplateResolver().apply {
            prefix = ""
            suffix = ""
            isCacheable = false // For development, disable caching
            characterEncoding = "UTF-8"
        }
        
        thymeleafEngine.setTemplateResolver(fileResolver)
    }
    
    /**
     * Process a template file with the given context data
     */
    fun processTemplate(
        templateFile: File,
        context: Map<String, Any>,
        templateMode: TemplateMode? = null
    ): String {
        val thymeleafContext = Context(Locale.getDefault())
        
        // Add all context variables
        context.forEach { (key, value) ->
            thymeleafContext.setVariable(key, value)
        }
        
        // Detect template mode if not provided
        val resolvedMode = templateMode ?: detectTemplateMode(templateFile)
        
        // Set template mode for the resolver
        val resolver = thymeleafEngine.templateResolvers.first() as FileTemplateResolver
        resolver.templateMode = resolvedMode
        
        return thymeleafEngine.process(templateFile.absolutePath, thymeleafContext)
    }
    
    /**
     * Detect template mode based on file extension and content
     */
    fun detectTemplateMode(file: File): TemplateMode {
        return when (file.extension.lowercase()) {
            "html", "htm" -> TemplateMode.HTML
            "xml" -> TemplateMode.XML
            "js" -> TemplateMode.JAVASCRIPT
            "css" -> TemplateMode.CSS
            "txt", "text" -> TemplateMode.TEXT
            else -> {
                // Try to detect from content
                detectTemplateModeFromContent(file)
            }
        }
    }
    
    /**
     * Detect template mode from file content when extension is ambiguous
     */
    private fun detectTemplateModeFromContent(file: File): TemplateMode {
        return try {
            val firstLine = file.readLines().firstOrNull()?.trim()?.lowercase() ?: ""
            
            when {
                firstLine.startsWith("<!doctype html") || 
                firstLine.startsWith("<html") -> TemplateMode.HTML
                
                firstLine.startsWith("<?xml") ||
                firstLine.startsWith("<") -> TemplateMode.XML
                
                firstLine.contains("function") ||
                firstLine.contains("var ") ||
                firstLine.contains("const ") ||
                firstLine.contains("let ") -> TemplateMode.JAVASCRIPT
                
                firstLine.contains("{") ||
                firstLine.contains("@") -> TemplateMode.CSS
                
                else -> TemplateMode.TEXT
            }
        } catch (e: Exception) {
            // Default to TEXT mode if content detection fails
            TemplateMode.TEXT
        }
    }
    
    /**
     * Get template mode from string representation
     */
    fun getTemplateModeFromString(mode: String): TemplateMode {
        return when (mode.uppercase()) {
            "HTML" -> TemplateMode.HTML
            "XML" -> TemplateMode.XML
            "TEXT" -> TemplateMode.TEXT
            "JAVASCRIPT", "JS" -> TemplateMode.JAVASCRIPT
            "CSS" -> TemplateMode.CSS
            "RAW" -> TemplateMode.RAW
            else -> throw IllegalArgumentException("Unknown template mode: $mode")
        }
    }
}