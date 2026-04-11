package edu.hitsz.aircraftwar.data

import androidx.core.content.ContentProviderCompat.requireContext
import edu.hitsz.aircraftwar.AircraftWarApplication
import edu.hitsz.aircraftwar.logic.difficulty.Difficulty
import edu.hitsz.aircraftwar.setting.Setting
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.BufferedWriter

object DataManager {
  private var fileName: String? = null
  fun saveData(data: SingleGameInfo) {
    when (Setting.getDifficulty()) {
      "easy" -> {
        fileName = "easy.txt"
      }
      "medium" -> {
        fileName = "medium.txt"
      }
      "hard" -> {
        fileName = "hard.txt"
      }
    }
    // 2. 构建文件路径（应用私有目录下 scores 子目录，便于管理）
    val scoresDir = File(AircraftWarApplication.context.filesDir, fileName)
    if (!scoresDir.exists()) {
      scoresDir.mkdirs()
    }
    val file = File(scoresDir, fileName)

    // 3. 将数据转换为一行文本（格式：用户名,分数,时间戳）
    val line = "${data.userName},${data.score},${data.date}\n"

    // 4. 追加写入文件
    try {
      FileOutputStream(file, true).use { fos ->
        OutputStreamWriter(fos).use { osw ->
          BufferedWriter(osw).use { bw ->
            bw.write(line)
          }
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      // 生产环境可改用 Log.e 或上报异常
    }
  }

  fun loadData(difficulty: String): List<SingleGameInfo> {
    when (difficulty) {
      "easy" -> {
        fileName = "easy.txt"
      }
      "medium" -> {
        fileName = "medium.txt"
      }
      "hard" -> {
        fileName = "hard.txt"
      }
    }
    val list = mutableListOf<SingleGameInfo>()
    val scoresDir = File(AircraftWarApplication.context.filesDir, fileName)
    if (!scoresDir.exists()) {
      return list
    }
    scoresDir.listFiles()?.forEach { file ->
      file.readLines()?.forEach { line ->
        val parts = line.split(",")
        if (parts.size == 3) {
          list.add(SingleGameInfo(parts[0], parts[1].toInt(), parts[2]))
        }
      }
    }
    list.sortByDescending { it.score } // 按分数降序排序
    return list
  }
}