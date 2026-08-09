plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "mkernel"

include("mkernel-api")
include("mkernel-core")
include("mkernel-miarmacraft")