package tfpp.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

/**
 * Configuration loader for TFPP configuration files
 */
class ConfigLoader {
    
    private val yamlMapper = ObjectMapper(YAMLFactory()).apply {
        registerModule(KotlinModule.Builder().build())
    }
    
    /**
     * Load configuration from file
     */
    fun loadConfig(configFile: File): TfppConfig {
        return try {
            yamlMapper.readValue<TfppConfig>(configFile)
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse configuration file ${configFile.name}: ${e.message}", e)
        }
    }
    
    /**
     * Load default configuration from conventional locations
     */
    fun loadDefaultConfig(workingDir: File = File(".")): TfppConfig? {
        val defaultConfigFiles = listOf(
            "tfpp.yml",
            "tfpp.yaml", 
            ".tfpp.yml",
            ".tfpp.yaml"
        )
        
        for (configFileName in defaultConfigFiles) {
            val configFile = File(workingDir, configFileName)
            if (configFile.exists() && configFile.isFile) {
                return loadConfig(configFile)
            }
        }
        
        return null
    }
    
    /**
     * Merge CLI options with config file settings
     * CLI options take precedence over config file
     */
    fun mergeWithCliOptions(
        config: TfppConfig?,
        cliOptions: CliOptions
    ): TfppConfig {
        val baseConfig = config ?: TfppConfig()
        
        return TfppConfig(
            sourceDir = cliOptions.sourceDir ?: baseConfig.sourceDir,
            outputDir = cliOptions.outputDir ?: baseConfig.outputDir,
            dataFiles = if (cliOptions.dataFiles.isNotEmpty()) cliOptions.dataFiles else baseConfig.dataFiles,
            variables = baseConfig.variables + cliOptions.variables, // Merge variables
            templateExtensions = cliOptions.templateExtensions ?: baseConfig.templateExtensions,
            copyAssets = cliOptions.copyAssets ?: baseConfig.copyAssets,
            watch = cliOptions.watch ?: baseConfig.watch,
            verbose = cliOptions.verbose ?: baseConfig.verbose,
            encoding = baseConfig.encoding,
            templateMode = cliOptions.templateMode ?: baseConfig.templateMode
        )
    }
}

/**
 * TFPP Configuration data class
 */
data class TfppConfig(
    val sourceDir: String? = null,
    val outputDir: String? = null,
    val dataFiles: List<String> = emptyList(),
    val variables: Map<String, Any> = emptyMap(),
    val templateExtensions: List<String> = listOf(
        "html", "htm", "xml", "txt", "text", 
        "css", "js", "json", "yaml", "yml",
        "md", "markdown", "sql", "properties"
    ),
    val copyAssets: Boolean = true,
    val watch: Boolean = false,
    val verbose: Boolean = false,
    val encoding: String = "UTF-8",
    val templateMode: String? = null
)

/**
 * CLI options data class for merging with config
 */
data class CliOptions(
    val sourceDir: String? = null,
    val outputDir: String? = null,
    val dataFiles: List<String> = emptyList(),
    val variables: Map<String, Any> = emptyMap(),
    val templateExtensions: List<String>? = null,
    val copyAssets: Boolean? = null,
    val watch: Boolean? = null,
    val verbose: Boolean? = null,
    val templateMode: String? = null
)