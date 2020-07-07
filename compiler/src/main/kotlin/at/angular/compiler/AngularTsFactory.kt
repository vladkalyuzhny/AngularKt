package at.angular.compiler

import java.io.File
import java.nio.file.Paths

/**
 * https://developer.mozilla.org/ru/docs/Web/JavaScript/Reference/Statements/import
 */
class AngularTsFactory(filePath: String) {

    private val projectDir = guessProjectDir(filePath)

    private val tsDir = File(projectDir, "build/ts")

    private lateinit var ktDir: File

    private lateinit var packageName: String

    private val fileName = File(filePath).nameWithoutExtension

    private val imports = mutableMapOf<String, String>()

    private lateinit var annotationName: String

    private val annotationMap = mutableMapOf<String, String>()

    private lateinit var className: String

    fun forPackage(name: String): AngularTsFactory {
        packageName = name
        ktDir = File(projectDir, "src/${name.replace(".", "/")}")
        return this
    }

    fun forAnnotation(annotation: Pair<String, String?>): AngularTsFactory {
        annotation.second?.let {
            imports[annotation.first] = it
        }
        annotationName = annotation.first
        return this
    }

    fun withRawField(map: Map<String, Any?>?, key: String): AngularTsFactory {
        annotationMap[key] = when (val value = map?.get(key) ?: return this) {
            is String -> value.surroundWith("'")
            else -> value.toString()
        }
        return this
    }

    fun withPathField(map: Map<String, Any?>?, key: String): AngularTsFactory {
        map?.get(key)?.toString()?.let {
            annotationMap[key] = resolveSrcFile(it).surroundWith("'")
        }
        return this
    }

    fun withPathsField(map: Map<String, Any?>?, key: String): AngularTsFactory {
        map?.get(key)?.toString()?.let {
            val paths = it.substringAround().split(",").map { path ->
                resolveSrcFile(path.substringAround()).surroundWith("'")
            }
            annotationMap[key] = paths.joinToString().surroundWith("[", "]")
        }
        return this
    }

    fun forClass(name: String): AngularTsFactory {
        className = name
        return this
    }

    private fun resolveSrcFile(path: String): String {
        val buildPath = Paths.get(tsDir.path)
        val srcPath = Paths.get(ktDir.path).resolve(path)
        return buildPath.relativize(srcPath).toString()
    }

    fun writeToFile() {
        tsDir.mkdirs()
        val tsFile = File(tsDir, "${packageName}_$fileName.ts")
        tsFile.bufferedWriter().use {
            imports.forEach { entry ->
                it.write("import {${entry.key}} from '${entry.value}';\n")
            }
            it.write("\n@$annotationName({\n")
            annotationMap.forEach { entry ->
                it.write("  ${entry.key}: ${entry.value},\n")
            }
            it.write("})\n")
            it.write("export class $className {}\n")
            it.flush()
        }
    }
}
