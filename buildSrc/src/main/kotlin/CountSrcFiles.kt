import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.tasks.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

abstract class CountSrcFiles : DefaultTask() {

    @TaskAction
    fun perform() {
        var contents = project.layout.projectDirectory.dir("src")
        var count = 0

        val path: String = contents.asFile.absolutePath
        File(path).walkTopDown()
            .filter { item -> Files.isRegularFile(item.toPath()) }
            .forEach {
                count++
            }

        val outputDir : Directory = project.layout.buildDirectory.dir("src-stats").get()
        outputDir.asFile.mkdirs()
        val hashFile : Path = Paths.get(outputDir.asFile.absolutePath, "num-files.txt")
        File(hashFile.toUri()).writeText(count.toString())
    }
}