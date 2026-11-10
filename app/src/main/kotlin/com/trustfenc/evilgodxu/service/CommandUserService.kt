package com.trustfenc.evilgodxu.service

import android.content.ComponentName
import android.content.Context
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

// Shizuku UserService：在具有 shell 权限的独立进程中执行命令
class CommandUserService : ICommandService.Stub() {

    companion object {
        private const val COMMAND_TIMEOUT_SECONDS = 30L
        private const val READER_JOIN_TIMEOUT_MS = 5_000L

        // processNameSuffix 必须显式给出，否则 UserServiceArgs 绑定时会因进程名为空失败
        fun createServiceArgs(context: Context): Shizuku.UserServiceArgs =
            Shizuku.UserServiceArgs(ComponentName(context, CommandUserService::class.java))
                .processNameSuffix("user_service")
    }

    override fun executeCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(command)
            val output = StringBuilder()
            val error = StringBuilder()

            // 并发读取 stdout/stderr，避免任一管道写满时子进程阻塞造成死锁
            val stdoutReader = thread(name = "fence-stdout") {
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    reader.lineSequence().forEach { output.appendLine(it) }
                }
            }
            val stderrReader = thread(name = "fence-stderr") {
                BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                    reader.lineSequence().forEach { error.appendLine(it) }
                }
            }

            val completed = process.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            stdoutReader.join(READER_JOIN_TIMEOUT_MS)
            stderrReader.join(READER_JOIN_TIMEOUT_MS)

            if (!completed) {
                process.destroyForcibly()
                return "Error (timeout after $COMMAND_TIMEOUT_SECONDS seconds)"
            }

            val exitCode = process.exitValue()
            if (exitCode == 0) {
                output.toString()
            } else {
                "Error (exit code $exitCode): $error"
            }
        } catch (e: Exception) {
            "Exception: ${e.message}"
        }
    }

    // binder 断开时客户端会收到 onServiceDisconnected，无需额外存活检查
    override fun isAlive(): Boolean = true
}
