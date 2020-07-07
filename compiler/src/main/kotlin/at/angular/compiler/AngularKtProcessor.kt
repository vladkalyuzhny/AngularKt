package at.angular.compiler

import at.angular.core.Component
import at.angular.core.Directive
import at.angular.core.NgModule
import de.jensklingenberg.mpapt.model.AbstractProcessor
import de.jensklingenberg.mpapt.model.Element
import de.jensklingenberg.mpapt.model.RoundEnvironment
import de.jensklingenberg.mpapt.utils.KotlinPlatformValues
import org.jetbrains.kotlin.platform.TargetPlatform

class AngularKtProcessor : AbstractProcessor() {

    override fun isTargetPlatformSupported(platform: TargetPlatform): Boolean {
        return when (val name = platform.first().platformName) {
            KotlinPlatformValues.JS -> true
            KotlinPlatformValues.JVM -> false
            KotlinPlatformValues.NATIVE -> false
            else -> {
                log(name)
                false
            }
        }
    }

    override fun process(roundEnvironment: RoundEnvironment) {
        roundEnvironment.getElementsAnnotatedWith(NgModule::class.java.name).forEach {
            if (it is Element.ClassElement) {
                /*val fields = it.annotation?.fields
                AngularTsFactory(it.classDescriptor.canonicalFilePath() ?: return@forEach)
                    .forPackage(it.pack)
                    .forAnnotation(NgModule::class.java.simpleName to "@angular/core")
                    .forClass(it.simpleName)
                    .writeToFile()*/
            }
        }
        roundEnvironment.getElementsAnnotatedWith(Component::class.java.name).forEach {
            if (it is Element.ClassElement) {
                /*val fields = it.annotation?.fields
                AngularTsFactory(it.classDescriptor.canonicalFilePath() ?: return@forEach)
                    .forPackage(it.pack)
                    .forAnnotation(Component::class.java.simpleName to "@angular/core")
                    .withRawField(fields, "selector")
                    .withPathField(fields, "templateUrl")
                    .withPathsField(fields, "styleUrls")
                    .forClass(it.simpleName)
                    .writeToFile()*/
            }
        }
        roundEnvironment.getElementsAnnotatedWith(Directive::class.java.name).forEach {
            if (it is Element.ClassElement) {
                /*val fields = it.annotation?.fields
                AngularTsFactory(it.classDescriptor.canonicalFilePath() ?: return@forEach)
                    .forPackage(it.pack)
                    .forAnnotation(Directive::class.java.simpleName to "@angular/core")
                    .forClass(it.simpleName)
                    .writeToFile()*/
            }
        }
    }

    override fun getSupportedAnnotationTypes() = setOf(
        NgModule::class.java.name,
        Component::class.java.name,
        Directive::class.java.name
    )
}
