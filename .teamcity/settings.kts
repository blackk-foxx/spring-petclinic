import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2021.1"

project {

    params {
        param("env.PROJECT_PARAM_A", "")
        text(name = "env.PROJECT_PARAM_B", value = "project-level B default", label = "Project-level param B",
             description = "Description for project-level param B", display = ParameterDisplay.PROMPT, allowEmpty = false)
    }

    buildType(A)
    buildType(B)
    buildType(C)

    sequential {
        parallel {
            buildType(A).produces("?.txt")
            buildType(B).produces("?.txt")
        }
        buildType(C)
            .requires(A, "?.txt")
            .requires(B, "?.txt")
            .produces("?.txt")
    }
}

fun BuildType.produces(artifactRules: String): BuildType {
    this.artifactRules = artifactRules
    return this
}

fun BuildType.requires(upstream: BuildType, artifactRules: String): BuildType {
    dependencies.artifacts(upstream) {
        this.artifactRules = artifactRules
    }
    return this
}

object A : MyBuildType({
    name = "A"

    params {
        param("env.BUILD_A_PARAM_A", "build A param A default")
        text(name = "env.BUILD_A_PARAM_B", value = "build A param B default", label = "Build A param B",
             description = "Description for build A param B", display = ParameterDisplay.PROMPT, allowEmpty = false)
    }

    steps {
        script {
            scriptContent = """
                echo BUILD_A_PARAM_A=${'$'}BUILD_A_PARAM_A
                echo BUILD_A_PARAM_B=${'$'}BUILD_A_PARAM_B
                echo ${'$'}BUILD_A_PARAM_A, ${'$'}BUILD_A_PARAM_B > a.txt
            """.trimIndent()
        }
    }
})

object B : MyBuildType({
    name = "B"

    steps {
        script {
            scriptContent = "echo B > b.txt"
        }
    }
})

object C : MyBuildType({
    name = "C"

    params {
        text(name = "reverse.dep.${A.id}.env.BUILD_A_PARAM_A", value = "", label = "Build A param A",
             description = "Description for build A param A", display = ParameterDisplay.PROMPT)
    }

    steps {
        script {
            scriptContent = """
                cat ?.txt > c.txt
                echo C, ${'$'}PROJECT_PARAM_B >> c.txt"
            """.trimIndent()
        }
    }
})

open class MyBuildType(init: MyBuildType.() -> Unit) : BuildType() {
    init {
        vcs {
            root(DslContext.settingsRoot)
            cleanCheckout = true
        }

        triggers {
            vcs {
            }
        }
        init()
    }
}

