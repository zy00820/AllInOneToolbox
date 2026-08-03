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
 * 检查更新工具（国内加速版）
 *
 * 查询策略：
 *  1. 优先走 jsdelivr CDN 读取仓库内的 version.json（国内访问快）
 *  2. jsdelivr 失败时降级到 GitHub API（兜底，国内较慢）
 *
 * 下载策略：
 *  - 默认把 github.com 链接替换为 ghproxy 镜像前缀加速下载
 *  - 同时保留原始 Release 页面链接
 */
object UpdateChecker {

    // 版本清单（jsdelivr CDN，国内加速，从仓库 @main 拉取 version.json）
    private const val JSDELIVR_VERSION_JSON =
        "https://cdn.jsdelivr.net/gh/zy00820/AllInOneToolbox@main/version.json"
    // 备用：GitHub API 直接查最新 Release
    private const val GITHUB_API_LATEST =
        "https://api.github.com/repos/zy00820/AllInOneToolbox/releases/latest"
    // Release 下载页
    private const val RELEASE_PAGE =
        "https://github.com/zy00820/AllInOneToolbox/releases/latest"
    // ghproxy 镜像前缀（下载加速）
    private const val GHPROXY_PREFIX = "https://mirror.ghproxy.com/"

    /** 当前已安装版本号，如 "1.0.12" */
    fun currentVersion(): String = BuildConfig.VERSION_NAME

    /** 给 URL 加 ghproxy 镜像前缀；已是镜像或非 GitHub 域名则原样返回 */
    private fun withMirror(url: String): String {
        if (url.startsWith(GHPROXY_PREFIX)) return url
        if (url.contains("github.com")) {
            // 多个 ghproxy 节点，用官方的 mirror.ghproxy.com
            return GHPROXY_PREFIX + url.removeSuffix("/")
        }
        return url
    }

    suspend fun checkLatestVersion(): UpdateResult = withContext(Dispatchers.IO) {
        // 1) 首选 jsdelivr CDN
        val fromCdn = fetchFromJsdelivr()
        if (fromCdn != null) return@withContext fromCdn
        // 2) 降级 GitHub API
        val fromGh = fetchFromGitHub()
        fromGh ?: UpdateResult.Failed("检查失败：网络不可用或所有查询源均失败")
    }

    private suspend fun fetchFromJsdelivr(): UpdateResult? = withContext(Dispatchers.IO) {
        try {
            val conn = open(JSDELIVR_VERSION_JSON)
            val code = conn.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                conn.disconnect()
                return@withContext null
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val json = JSONObject(body)
            val latestVersion = json.optString("versionName", "").trim()
            val apkRaw = json.optString("apkUrl", RELEASE_PAGE)
            val releasePage = json.optString("releasePage", RELEASE_PAGE)
            if (latestVersion.isEmpty()) return@withContext null
            UpdateResult.Success(
                latestVersion = latestVersion,
                apkUrl = withMirror(apkRaw),
                releaseUrl = releasePage,
                hasUpdate = isNewer(currentVersion(), latestVersion)
            )
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun fetchFromGitHub(): UpdateResult? = withContext(Dispatchers.IO) {
        try {
            val conn = open(GITHUB_API_LATEST).apply {
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "AllInOneToolbox/${BuildConfig.VERSION_NAME}")
            }
            val code = conn.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                conn.disconnect()
                return@withContext null
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            val json = JSONObject(body)
            val tagName = json.optString("tag_name", "")
            val latestVersion = tagName.removePrefix("v").removePrefix("V").trim()
            val htmlUrl = json.optString("html_url", RELEASE_PAGE)
            var apkUrl = htmlUrl
            val assets = json.optJSONArray("assets")
            if (assets != null && assets.length() > 0) {
                apkUrl = assets.getJSONObject(0).optString("browser_download_url", htmlUrl)
            }
            if (latestVersion.isEmpty()) return@withContext null
            UpdateResult.Success(
                latestVersion = latestVersion,
                apkUrl = withMirror(apkUrl),
                releaseUrl = htmlUrl,
                hasUpdate = isNewer(currentVersion(), latestVersion)
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun open(urlStr: String): HttpURLConnection {
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 8000
        conn.readTimeout = 10000
        conn.instanceFollowRedirects = true
        return conn
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

    /** 打开加速后的 APK 下载链接（直链） */
    fun openApkDownload(context: Context, apkUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(withMirror(apkUrl))).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) { openReleasePage(context) }
    }

    /** 用浏览器打开 Release 页面 */
    fun openReleasePage(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(RELEASE_PAGE)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) { /* noop */ }
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
