package com.allinone.toolbox.utils

import android.content.Context
import com.allinone.toolbox.App
import kotlin.random.Random

object ActivationUtils {

    private val ACTIVATION_CODES = arrayOf(
        "AT2024-A001-X7K9-MN3P",
        "AT2024-A002-LR4T-QW8V",
        "AT2024-A003-ZB3N-YH6J",
        "AT2024-A004-KM5R-CX2L",
        "AT2024-A005-PW7B-VN4H",
        "AT2024-A006-JD9F-TG5K",
        "AT2024-A007-RN2C-XM8P",
        "AT2024-A008-VH6L-BQ3T",
        "AT2024-A009-KF4J-ZD7Y",
        "AT2024-A010-MN8R-PW2L",
        "AT2024-A011-LT5X-JD9C",
        "AT2024-A012-YH3B-VK6R",
        "AT2024-A013-CQ7M-NT4F",
        "AT2024-A014-PW9K-LD5X",
        "AT2024-A015-ZB2J-HM8C",
        "AT2024-A016-VR4T-YN6L",
        "AT2024-A017-KM3F-CQ9P",
        "AT2024-A018-XD7B-LT2R",
        "AT2024-A019-JH5N-WK8M",
        "AT2024-A020-YC6T-ZB4F",
        "AT2024-A021-VN9K-LM3X",
        "AT2024-A022-PR2J-CQ5H",
        "AT2024-A023-WD7Y-NT8R",
        "AT2024-A024-XB4M-KF6L",
        "AT2024-A025-JH3C-VN9P",
        "AT2024-A026-ZT5B-RM7K",
        "AT2024-A027-LQ8F-YD2X",
        "AT2024-A028-WN6J-CP4M",
        "AT2024-A029-VR3H-LT9B",
        "AT2024-A030-KM5Y-XD6F",
        "AT2024-A031-JT7C-QW2N",
        "AT2024-A032-ZB4P-VR8L",
        "AT2024-A033-LD9F-KM3H",
        "AT2024-A034-XB6Y-JQ5T",
        "AT2024-A035-WN2C-PR7M",
        "AT2024-A036-VH4K-ZB9X",
        "AT2024-A037-LM8F-JT3R",
        "AT2024-A038-QD5Y-WN6L",
        "AT2024-A039-CP7X-VR4H",
        "AT2024-A040-KM2J-LD9F",
        "AT2024-A041-JH3T-XB5C",
        "AT2024-A042-ZB8M-QW6K",
        "AT2024-A043-VN4R-LY2P",
        "AT2024-A044-WD7F-KM9C",
        "AT2024-A045-JT5X-NT4B",
        "AT2024-A046-CQ3H-VR6Y",
        "AT2024-A047-LM8K-PW2F",
        "AT2024-A048-XB4J-ZD7R",
        "AT2024-A049-WN6T-JH5C",
        "AT2024-A050-VR9M-KQ3L"
    )

    fun getAllCodes(): Array<String> = ACTIVATION_CODES

    fun generateCheckCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..4).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    fun verifyAndActivate(inputCode: String): ActivationResult {
        val prefs = App.instance.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
        val usedCodes = prefs.getStringSet("used_codes", setOf()) ?: setOf()

        val normalizedCode = inputCode.trim().uppercase()

        if (normalizedCode !in ACTIVATION_CODES) {
            return ActivationResult.Failed("激活码无效")
        }

        if (normalizedCode in usedCodes) {
            return ActivationResult.Failed("该激活码已使用")
        }

        val updatedCodes = usedCodes.toMutableSet()
        updatedCodes.add(normalizedCode)
        prefs.edit()
            .putStringSet("used_codes", updatedCodes)
            .putBoolean("is_member", true)
            .putString("activated_code", normalizedCode)
            .putLong("activation_time", System.currentTimeMillis())
            .apply()

        return ActivationResult.Success("激活成功！")
    }

    fun isMember(): Boolean {
        val prefs = App.instance.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_member", false)
    }

    fun getActivationInfo(): String {
        val prefs = App.instance.getSharedPreferences("all_in_one_prefs", Context.MODE_PRIVATE)
        val code = prefs.getString("activated_code", "") ?: ""
        val time = prefs.getLong("activation_time", 0)
        val timeStr = if (time > 0) {
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date(time))
        } else ""
        return "激活码: $code\n激活时间: $timeStr"
    }

    sealed class ActivationResult {
        data class Success(val message: String) : ActivationResult()
        data class Failed(val message: String) : ActivationResult()
    }
}
