package tfpp.data

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

/**
 * Data loader for various data sources (JSON, YAML, key-value pairs)
 */
class DataLoader {
    
    private val jsonMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
    }
    
    private val yamlMapper = ObjectMapper(YAMLFactory()).apply {
        registerModule(KotlinModule.Builder().build())
    }
    
    /**
     * Load data from a file (JSON or YAML based on extension)
     */
    fun loadFromFile(file: File): Map<String, Any> {
        return when (file.extension.lowercase()) {
            "json" -> loadJsonFile(file)
            "yaml", "yml" -> loadYamlFile(file)
            else -> throw IllegalArgumentException("Unsupported data file format: ${file.extension}")
        }
    }
    
    /**
     * Load data from JSON file
     */
    private fun loadJsonFile(file: File): Map<String, Any> {
        return try {
            jsonMapper.readValue<Map<String, Any>>(file)
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse JSON file ${file.name}: ${e.message}", e)
        }
    }
    
    /**
     * Load data from YAML file
     */
    private fun loadYamlFile(file: File): Map<String, Any> {
        return try {
            yamlMapper.readValue<Map<String, Any>>(file)
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse YAML file ${file.name}: ${e.message}", e)
        }
    }
    
    /**
     * Parse direct JSON string
     */
    fun parseJsonString(jsonString: String): Map<String, Any> {
        return try {
            jsonMapper.readValue<Map<String, Any>>(jsonString)
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse JSON string: ${e.message}", e)
        }
    }
    
    /**
     * Parse key-value pairs from strings like "key=value"
     */
    fun parseKeyValuePairs(pairs: List<String>): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        pairs.forEach { pair ->
            val parts = pair.split("=", limit = 2)
            if (parts.size != 2) {
                throw IllegalArgumentException("Invalid key-value pair format: '$pair'. Expected format: key=value")
            }
            
            val key = parts[0].trim()
            val value = parts[1].trim()
            
            if (key.isEmpty()) {
                throw IllegalArgumentException("Empty key in pair: '$pair'")
            }
            
            // Try to parse value as different types
            result[key] = parseValue(value)
        }
        
        return result
    }
    
    /**
     * Parse a string value to appropriate type (string, number, boolean)
     */
    private fun parseValue(value: String): Any {
        return when {
            value.equals("true", ignoreCase = true) -> true
            value.equals("false", ignoreCase = true) -> false
            value.equals("null", ignoreCase = true) -> "null" // Keep as string for template
            value.toIntOrNull() != null -> value.toInt()
            value.toDoubleOrNull() != null -> value.toDouble()
            value.startsWith("\"") && value.endsWith("\"") -> value.substring(1, value.length - 1)
            value.startsWith("'") && value.endsWith("'") -> value.substring(1, value.length - 1)
            else -> value
        }
    }
    
    /**
     * Merge multiple data maps with precedence (later maps override earlier ones)
     */
    fun mergeData(vararg dataMaps: Map<String, Any>): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        dataMaps.forEach { dataMap ->
            result.putAll(dataMap)
        }
        
        return result
    }
}