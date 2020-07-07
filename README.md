# @AngularKt
> Write Angular apps with pleasure on Kotlin :relaxed:

Benefits:
* Kotlin itself :sparkles: with no `;`, no `this`, `null` safety
* Fully customizable webpack
* Support of [Multiplatform Projects](https://kotlinlang.org/docs/reference/multiplatform.html)
* Support of [kotlinx.coroutines][2], [Ktor][3], [kotlinx.serialization][4]
* Fully customizable CLI

[1]: https://kotlinlang.org/docs/reference/multiplatform.html
[2]: https://github.com/Kotlin/kotlinx.coroutines
[3]: https://ktor.io/clients/http-client/multiplatform.html
[4]: https://github.com/Kotlin/kotlinx.serialization

## Basic example
```kotlin
@Component(
    selector = "app-root",
    template = """
        <p>
          Welcome to {{title}}!
        </p>
    """,
    styles = []
)
class AppComponent : OnInit {

    var title = "example"
  
    override fun ngOnInit() {
    }
}

@NgModule
class AppModule {

    companion object : NgModuleKt(
        declarations = arrayOf(
            AppComponent::class.js
        ),
        imports = arrayOf(
            BrowserModule::class.js
        ),
        providers = emptyArray(),
        bootstrap = arrayOf(AppComponent::class.js)
    )
}

fun main() {
    AngularKt.bootstrap<AppModule>()
}
```
Isn't that awesome? By the way, currently kotlin js does not
support classes in annotations, so somewhere additional code
like `companion object : <annotation>Kt` is required

## Custom CLI
As you may have guessed, it is now impossible to use [Angular CLI](https://cli.angular.io).
The gradle plugin can be used as a replacement, which currently supports
the [generate](https://angular.io/cli/generate) command. For example, in order to generate a service,
you should execute `$ sh gradlew generate service` or shorter
`$ sh gradlew g service` or even shorter `$ sh gradlew service`


## Develop
Sorry, tools are available only for Linux (may be also MacOS)  
First you need to setup environment
```shell script
$ bash tools/setup.sh
$ bash tools/build.sh
```

## Contribute
All issues and pull requests are welcome.  
If you need an idea, look for `// todo` comments in code  
Thank you in advance

## Thanks
Thanks Google for its great framework  
Special thanks these guys. Awesome job!
* [MpApt - Kotlin (Native/JS/JVM) Annotation Processor library](https://github.com/Foso/MpApt) by @Foso
* [Kotling Angular](https://github.com/Yegair/kotlin-angular) by @Yegair
* [Angular-Kotlin Starter App](https://github.com/gbaldeck/angular-kotlin-starter) by @gbaldeck

## License
This project is licensed under the Apache License, Version 2.0  
See the [LICENSE.txt](LICENSE.txt) file for more details

-------
    Copyright 2020 Vlad Kalyuzhnyu

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.