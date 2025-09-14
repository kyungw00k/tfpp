package examples

import tfpp.cli.TfppCli
import tfpp.core.TemplateEngine
import tfpp.data.DataLoader
import java.io.File

/**
 * Basic usage examples for TFPP template processing.
 * 
 * These examples demonstrate the most common use cases:
 * - Processing single templates
 * - Loading data from various sources  
 * - Using the CLI programmatically
 */
object BasicUsage {

    /**
     * Process a simple template with JSON data.
     * 
     * ```kotlin
     * // Template: welcome.html
     * // <h1 th:text="${greeting}">Hello</h1>
     * 
     * // Data: data.json  
     * // {"greeting": "Welcome to TFPP!"}
     * 
     * val result = processSimpleTemplate()
     * // Result: <h1>Welcome to TFPP!</h1>
     * ```
     */
    fun processSimpleTemplate(): String {
        val templateEngine = TemplateEngine()
        val dataLoader = DataLoader()
        
        // Load template
        val template = File("examples/templates/welcome.html")
        
        // Load data
        val data = dataLoader.loadFromFile(File("examples/data/simple.json"))
        
        // Process template
        return templateEngine.processTemplate(template, data)
    }

    /**
     * Process template with YAML data source.
     * 
     * ```kotlin
     * // Template: blog-post.html
     * // <article>
     * //   <h1 th:text="${post.title}">Title</h1>
     * //   <p th:text="${post.content}">Content</p>
     * // </article>
     * 
     * // Data: blog.yaml
     * // post:
     * //   title: "Getting Started with TFPP"
     * //   content: "TFPP makes template processing easy..."
     * ```
     */
    fun processWithYamlData(): String {
        val templateEngine = TemplateEngine()
        val dataLoader = DataLoader()
        
        val template = File("examples/templates/blog-post.html")
        val data = dataLoader.loadFromFile(File("examples/data/blog.yaml"))
        
        return templateEngine.processTemplate(template, data)
    }

    /**
     * Process template with CLI variables.
     * 
     * ```kotlin
     * // Equivalent to:
     * // tfpp process template.html --var name="John" --var age=30
     * ```
     */
    fun processWithCliVariables(): String {
        val templateEngine = TemplateEngine()
        val dataLoader = DataLoader()
        
        // Simulate CLI variables
        val cliVars = mapOf(
            "name" to "John Doe",
            "age" to 30,
            "active" to true
        )
        
        val template = File("examples/templates/user-profile.html")
        
        return templateEngine.processTemplate(template, cliVars)
    }

    /**
     * Merge multiple data sources with precedence.
     * 
     * ```kotlin
     * // Base data from file + CLI overrides
     * // CLI variables take precedence over file data
     * ```
     */
    fun processWithMergedData(): String {
        val templateEngine = TemplateEngine()
        val dataLoader = DataLoader()
        
        // Load base data from file
        val baseData = dataLoader.loadFromFile(File("examples/data/config.json"))
        
        // CLI overrides
        val cliOverrides = mapOf(
            "environment" to "production",
            "debug" to false
        )
        
        // Merge with CLI taking precedence
        val mergedData = dataLoader.mergeData(listOf(baseData, cliOverrides))
        
        val template = File("examples/templates/config.xml")
        
        return templateEngine.processTemplate(template, mergedData)
    }
}

/**
 * Advanced usage examples showing more complex scenarios.
 */
object AdvancedUsage {

    /**
     * Process template with conditional logic.
     * 
     * ```html
     * <div th:if="${user.isAdmin}">
     *   <p>Admin panel</p>
     * </div>
     * <div th:unless="${user.isAdmin}">
     *   <p>Regular user view</p>
     * </div>
     * ```
     */
    fun processWithConditionals(): String {
        val templateEngine = TemplateEngine()
        
        val data = mapOf(
            "user" to mapOf(
                "name" to "Alice",
                "isAdmin" to true,
                "permissions" to listOf("read", "write", "admin")
            )
        )
        
        val template = File("examples/templates/admin-dashboard.html")
        
        return templateEngine.processTemplate(template, data)
    }

    /**
     * Process template with iteration over collections.
     * 
     * ```html
     * <ul>
     *   <li th:each="item : ${items}" th:text="${item.name}">Item</li>
     * </ul>
     * ```
     */
    fun processWithIteration(): String {
        val templateEngine = TemplateEngine()
        
        val data = mapOf(
            "items" to listOf(
                mapOf("name" to "First Item", "priority" to "high"),
                mapOf("name" to "Second Item", "priority" to "medium"),
                mapOf("name" to "Third Item", "priority" to "low")
            )
        )
        
        val template = File("examples/templates/item-list.html")
        
        return templateEngine.processTemplate(template, data)
    }

    /**
     * Process template with utility functions.
     * 
     * ```html
     * <p th:text="${#dates.format(publishDate, 'yyyy-MM-dd')}">Date</p>
     * <p th:text="${#strings.capitalize(title)}">Title</p>
     * <p th:text="${#numbers.formatDecimal(price, 2, 2)}">Price</p>
     * ```
     */
    fun processWithUtilities(): String {
        val templateEngine = TemplateEngine()
        
        val data = mapOf(
            "publishDate" to "2025-01-15T10:00:00",
            "title" to "getting started with tfpp",
            "price" to 29.99
        )
        
        val template = File("examples/templates/formatted-content.html")
        
        return templateEngine.processTemplate(template, data)
    }
}