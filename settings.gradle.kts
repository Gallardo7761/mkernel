plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "nucleo"

include("nucleo-api")
include("nucleo-public")
include("nucleo-private")