package at.angular.gradle

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import java.io.File

/**
 * A tiny editable-JSON facade over kotlinx.serialization: parse JSON into mutable Kotlin
 * collections (LinkedHashMap/ArrayList/primitives), edit in place, write back as 2-space-indented
 * JSON with a trailing newline. Deliberately domain-agnostic — knows JSON, not Angular.
 */

@OptIn(ExperimentalSerializationApi::class)
private val json = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
}

// jsonc = true: tolerates // comments and trailing commas
// Comments are lost when writing—the parser discards them.
@OptIn(ExperimentalSerializationApi::class)
private val lenient = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
    allowComments = true
    allowTrailingComma = true
}

internal fun File.readJsonObject(jsonc: Boolean = false): MutableMap<String, Any?> =
    parseJsonObject(readText(), jsonc)

@Suppress("UNCHECKED_CAST")
internal fun parseJsonObject(text: String, jsonc: Boolean = false): MutableMap<String, Any?> =
    (if (jsonc) lenient else json).parseToJsonElement(text).toMutableTree() as? MutableMap<String, Any?>
        ?: error("root is not a JSON object")

internal fun File.writeJson(tree: Any?) =
    writeText(json.encodeToString(JsonElement.serializer(), tree.toJsonElement()) + "\n")

@Suppress("UNCHECKED_CAST")
internal fun MutableMap<String, Any?>.obj(key: String): MutableMap<String, Any?> =
    getOrPut(key) { LinkedHashMap<String, Any?>() } as? MutableMap<String, Any?>
        ?: error("\"$key\" is not a JSON object")

private fun JsonElement.toMutableTree(): Any? = when (this) {
    is JsonNull -> null
    is JsonPrimitive -> if (isString) content else booleanOrNull ?: longOrNull ?: doubleOrNull ?: content
    is JsonObject -> entries.associateTo(LinkedHashMap()) { (k, v) -> k to v.toMutableTree() }
    is JsonArray -> mapTo(ArrayList()) { it.toMutableTree() }
}

@Suppress("UNCHECKED_CAST")
private fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is Boolean -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is JsonElement -> this
    is Map<*, *> -> JsonObject((this as Map<Any?, Any?>).entries.associate { (k, v) -> k.toString() to v.toJsonElement() })
    is List<*> -> JsonArray(map { it.toJsonElement() })
    else -> JsonPrimitive(toString())
}