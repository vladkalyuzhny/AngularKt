package at.angular.compiler

import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import java.io.File

val AnnotationDescriptor.fields
    get() = allValueArguments.entries.associate { it.key.asString() to it.value.value }

fun String.substringAround() = substring(1, lastIndex)

fun String.surroundWith(start: String, end: String = start) = "$start$this$end"

tailrec fun guessProjectDir(path: String): String {
    val preSrcDir = path.substringBeforeLast("/src")
    return when {
        path == preSrcDir || File(preSrcDir, "build").isDirectory -> preSrcDir
        else -> guessProjectDir(preSrcDir)
    }
}