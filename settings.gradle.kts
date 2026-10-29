rootProject.name = "OrnekCloudStreamRepo"

// Her alt klasörde build.gradle.kts varsa otomatik proje olarak eklenir.
// Yeni eklenti eklemek için: klasör aç + build.gradle.kts koy, başka bir şey yapma.
val disabled = listOf("tools", "gradle", "plugins", "build")

File(rootDir, ".").eachDir { dir ->
    if (!disabled.contains(dir.name) && File(dir, "build.gradle.kts").exists() && File(dir, "src").exists()) {
        include(dir.name)
    }
}

fun File.eachDir(block: (File) -> Unit) {
    listFiles()?.filter { it.isDirectory }?.forEach { block(it) }
}
