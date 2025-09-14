repositories {
    mavenCentral()
}

version = "1.0.0"

plugins {
    id("ru.vyarus.mkdocs") version "4.0.1"
}

mkdocs {
    sourcesDir = "."
    buildDir = "site"
    
    python {
        scope = ru.vyarus.gradle.plugin.python.PythonExtension.Scope.VIRTUALENV
        pip("mkdocs:1.6.0", "mkdocs-material:9.5.18")
    }
}

tasks.register<Exec>("serveDocs") {
    group = "documentation"
    description = "Serve MkDocs documentation locally"
    dependsOn("mkdocsBuild")
    
    commandLine("mkdocs", "serve")
    workingDir = file(projectDir)
}

