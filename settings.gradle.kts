rootProject.name = "LibsDisguises"

include("minimessage")

include("shared", "plugin", "shaded")

val nmsModules = File(rootDir, "nms").listFiles()!!.filter { s -> s.isDirectory() && s.name.matches("v[\\d_]+R\\d+".toRegex()) }.map { s -> ":nms:" + s.name };
gradle.extra["nmsModules"] = nmsModules
include(nmsModules)
