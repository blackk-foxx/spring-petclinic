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


// TODO: Use the nice produces/requires syntax described in https://blog.jetbrains.com/teamcity/2019/04/configuration-as-code-part-4-extending-the-teamcity-dsl/

project {

    buildType(A)
    buildType(B)
    buildType(C)

    sequential {
        parallel {
            buildType(A) {

            }

            buildType(B) {

            }
        }
        buildType(C) {

        }
    }
}

object A : BuildType({
    name = "A"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        script {
            scriptContent = "echo A > a.txt"
        }
    }

    triggers {
        vcs {
        }
    }

    artifactRules = "*.txt"
})

object B : BuildType({
    name = "B"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        script {
            scriptContent = "echo B > b.txt"
        }
    }

    triggers {
        vcs {
        }
    }

    artifactRules = "*.txt"
})

object C : BuildType({
    name = "C"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        script {
            scriptContent = "cat *.txt > c.txt; echo C >> c.txt"
        }
    }

    dependencies.artifacts(A) {
        artifactRules = "*.txt"
    }

    dependencies.artifacts(B) {
        artifactRules = "*.txt"
    }

    artifactRules = "*.txt"
})


