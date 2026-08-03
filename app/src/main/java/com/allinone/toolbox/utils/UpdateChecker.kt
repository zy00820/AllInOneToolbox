package com.allinone.toolbox.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.allinone.toolbox.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * 检查更新工具
 *
 * 通过 GitHub API 查询本仓库最新 Release，与本地 BuildConfig.VERSION_NAME 比对。
 * 仅在用户主动点击「检查更新」时联网，无任何后台请求。
 */
object UpdateChecker {

    private const val GITHUB_API =
        "https://api.github.com/repos/zy00820/AllInOneToolbox/releases/latest"
    private const val RELEASE_PAGE =
        "https://github.com/zy00820/AllInOneToolbox/releases/latest"

    /** 当前已安装版本号，如 "1.0.12" */
    fun currentVersion(): String = BuildConfig.VERSION_NAME

    /** 查询 GitHub 最新 Release */
    suspend fun checkLatestVersion(): UpdateResult = withContext(Dispatchers.IO) {
        try {
            val conn = (URL(GITHUB_API).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json")
                // GitHub API 要求带 User-Agent，否则 403
                setRequestProperty("User-Agent", "AllInOneToolbox/${BuildConfig.VERSION_NAME}")
                connectTimeout = 10000
                readTimeout = 10000
                instanceFollowRedirects = true
            }
            val code = conn.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                return@withContext UpdateResult.Failed("网络错误 (HTTP $code)")
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val json = JSONObject(body)
            val tagName = json.optString("tag_name", "")      // 如 "v1.0.11"
            val latestVersion = tagName.removePrefix("v").removePrefix("V").trim()
            val htmlUrl = json.optString("html_url", RELEASE_PAGE)
            // 取第一个资产（APK）下载链接
            var apkUrl = htmlUrl
            val assets = json.optJSONArray("assets")
            if (assets != null && assets.length() > 0) {
                apkUrl = assets.getJSONObject(0)
                    .optString("browser_download_url", htmlUrl)
            }
            if (latestVersion.isEmpty()) {
                UpdateResult.Failed("未能解析最新版本号")
            } else {
                UpdateResult.Success(
                    latestVersion = latestVersion,
                    apkUrl = apkUrl,
                    releaseUrl = htmlUrl,
                    hasUpdate = isNewer(currentVersion(), latestVersion)
                )
            }
        } catch (e: Exception) {
            UpdateResult.Failed("检查失败：${e.message ?: "未知错误"}")
        }
    }

    /** 按点分段比较版本号，latest > current 返回 true */
    fun isNewer(current: String, latest: String): Boolean {
        val c = current.split(".").map { it.toIntOrNull() ?: 0 }
        val l = latest.split(".").map { it.toIntOrNull() ?: 0 }
        val max = maxOf(c.size, l.size)
        for (i in 0 until max) {
            val cv = c.getOrElse(i) { 0 }
            val lv = l.getOrElse(i) { 0 }
            if (lv > cv) return true
            if (lv < cv) return false
        }
        return false
    }

    /** 用浏览器打开 Release 下载页 */
    fun openReleasePage(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(RELEASE_PAGE)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // 无浏览器时忽略
        }
    }

    sealed class UpdateResult {
        /** 检查成功 */
        data class Success(
            val latestVersion: String,
            val apkUrl: String,
            val releaseUrl: String,
            val hasUpdate: Boolean
        ) : UpdateResult()

        /** 检查失败（网络/解析错误） */
        data class Failed(val message: String) : UpdateResult()
    }
}
