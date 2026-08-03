package com.allinone.toolbox.utils

import android.content.Context
import com.allinone.toolbox.App
import kotlin.random.Random

/**
 * 激活码双重验证：LITE（第一码）+ PRO（第二码）
 *
 * 会员等级：
 *  NONE - 未激活
 *  LITE - 第一码激活（使用 50 个 AT2024-Axxx 码），解锁基础会员
 *  PRO  - 第二码激活（使用 50 个 AT2024-Pxxx 码），必须先有 LITE 才能升级到 PRO
 *
 * 每个激活码全球唯一、仅能使用一次。
 */
object ActivationUtils {

    enum class MemberLevel(val level: Int) {
        NONE(0), LITE(1), PRO(2);

        val displayName: String get() = when (this) {
            NONE -> "未激活"
            LITE -> "会员 LITE"
            PRO  -> "会员 PRO"
        }
    }

    // ===== LITE 激活码（第一层：原 50 个码）
    private val LITE_CODES = arrayOf(
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

    // ===== PRO 激活码（第二层：新增 50 个码）
    private val PRO_CODES = arrayOf(
        "AT2024-P001-NYBV-S4B2",
        "AT2024-P002-X6WT-MQ2M",
        "AT2024-P003-JLMR-NYH4",
        "AT2024-P004-46Q7-MLCS",
        "AT2024-P005-PDA7-L4QD",
        "AT2024-P006-S27L-VMQX",
        "AT2024-P007-NZ5Q-2UX4",
        "AT2024-P008-ZBZ7-MRWN",
        "AT2024-P009-PLS3-ULME",
        "AT2024-P010-SYP2-CGSC",
        "AT2024-P011-VAYV-3RVX",
        "AT2024-P012-MPB2-HB8D",
        "AT2024-P013-R7AB-QAHQ",
        "AT2024-P014-W47Q-NMEC",
        "AT2024-P015-ALD9-R6UW",
        "AT2024-P016-JDE7-ZD5Z",
        "AT2024-P017-FS4G-ZGGA",
        "AT2024-P018-J8VC-7KJS",
        "AT2024-P019-U9DG-WCDM",
        "AT2024-P020-B4N4-JXVL",
        "AT2024-P021-URXV-FL8C",
        "AT2024-P022-RUXB-UTVE",
        "AT2024-P023-VSSJ-E2MQ",
        "AT2024-P024-8TYP-KR2N",
        "AT2024-P025-NZVP-445P",
        "AT2024-P026-Z2GS-DKR2",
        "AT2024-P027-FP4M-6PZH",
        "AT2024-P028-WXCM-EWQ5",
        "AT2024-P029-7SH9-UBS7",
        "AT2024-P030-CCNQ-2DKX",
        "AT2024-P031-RJBA-APGW",
        "AT2024-P032-6828-PC7S",
        "AT2024-P033-R3U5-QYQ5",
        "AT2024-P034-N7G2-2PVL",
        "AT2024-P035-BXPC-6EWG",
        "AT2024-P036-U88W-TR9Z",
        "AT2024-P037-S8XR-JAEZ",
        "AT2024-P038-7BX7-NNUC",
        "AT2024-P039-Q7K7-W4GT",
        "AT2024-P040-YGUP-3K2D",
        "AT2024-P041-ZKEL-LXYE",
        "AT2024-P042-PWVH-PB95",
        "AT2024-P043-8RDM-A7KC",
        "AT2024-P044-62S8-YXRN",
        "AT2024-P045-549X-5P9M",
        "AT2024-P046-34TP-E543",
        "AT2024-P047-NCDC-VU6H",
        "AT2024-P048-STDA-Y4T8",
        "AT2024-P049-7HHZ-Y9YQ",
        "AT2024-P050-QD8H-VUE5"
    )

    private const val PREFS = "all_in_one_prefs"
    private const val KEY_USED_LITE = "used_lite_codes"
    private const val KEY_USED_PRO  = "used_pro_codes"
    private const val KEY_LEVEL     = "member_level_int"
    private const val KEY_LITE_CODE = "lite_code"
    private const val KEY_PRO_CODE  = "pro_code"
    private const val KEY_LITE_TIME = "lite_activate_time"
    private const val KEY_PRO_TIME  = "pro_activate_time"

    // ===== 兼容接口：只要 LITE/PRO 都算会员
    fun isMember(): Boolean = memberLevel().level >= MemberLevel.LITE.level
    fun isProMember(): Boolean = memberLevel().level >= MemberLevel.PRO.level
    fun memberLevel(): MemberLevel {
        val prefs = App.instance.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return when (prefs.getInt(KEY_LEVEL, 0)) {
            2 -> MemberLevel.PRO
            1 -> MemberLevel.LITE
            else -> MemberLevel.NONE
        }
    }

    /** 兼容旧版：已使用过旧版 is_member=true 但 KEY_LEVEL 仍为 0 的用户，升级为 LITE */
    private fun migrateLegacy() {
        val prefs = App.instance.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean("is_member", false) && prefs.getInt(KEY_LEVEL, 0) == 0) {
            prefs.edit()
                .putInt(KEY_LEVEL, MemberLevel.LITE.level)
                .apply()
        }
    }

    // ===== 第一码：激活 LITE =====
    fun verifyAndActivateLite(inputCode: String): ActivationResult {
        migrateLegacy()
        val prefs = App.instance.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val used = prefs.getStringSet(KEY_USED_LITE, setOf()) ?: setOf()
        val normalized = inputCode.trim().uppercase()
        if (normalized !in LITE_CODES) return ActivationResult.Failed("LITE 激活码无效")
        if (normalized in used) return ActivationResult.Failed("该 LITE 激活码已使用")

        // 已 LITE/PRO：不能重复激活 LITE
        if (memberLevel().level >= MemberLevel.LITE.level) {
            return ActivationResult.Failed("已激活 LITE，无需重复激活")
        }

        val newUsed = used.toMutableSet().apply { add(normalized) }
        val now = System.currentTimeMillis()
        prefs.edit()
            .putStringSet(KEY_USED_LITE, newUsed)
            .putInt(KEY_LEVEL, MemberLevel.LITE.level)
            .putString(KEY_LITE_CODE, normalized)
            .putLong(KEY_LITE_TIME, now)
            // 兼容旧 key（1.0.13 及以前的会员状态
            .putBoolean("is_member", true)
            .putString("activated_code", normalized)
            .putLong("activation_time", now)
            .apply()

        return ActivationResult.LiteSuccess("第一码验证通过，已激活会员 LITE")
    }

    // ===== 第二码：激活 PRO =====
    fun verifyAndActivatePro(inputCode: String): ActivationResult {
        migrateLegacy()
        val lvl = memberLevel()
        if (lvl.level < MemberLevel.LITE.level) {
            return ActivationResult.Failed("请先激活 LITE，再激活 PRO")
        }
        if (lvl.level >= MemberLevel.PRO.level) {
            return ActivationResult.Failed("已激活 PRO，无需重复激活")
        }
        val prefs = App.instance.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val used = prefs.getStringSet(KEY_USED_PRO, setOf()) ?: setOf()
        val normalized = inputCode.trim().uppercase()
        if (normalized !in PRO_CODES) return ActivationResult.Failed("PRO 激活码无效")
        if (normalized in used) return ActivationResult.Failed("该 PRO 激活码已使用")

        val newUsed = used.toMutableSet().apply { add(normalized) }
        val now = System.currentTimeMillis()
        prefs.edit()
            .putStringSet(KEY_USED_PRO, newUsed)
            .putInt(KEY_LEVEL, MemberLevel.PRO.level)
            .putString(KEY_PRO_CODE, normalized)
            .putLong(KEY_PRO_TIME, now)
            .apply()

        return ActivationResult.ProSuccess("第二码验证通过，已激活会员 PRO")
    }

    // ===== 旧版单码激活（保留兼容，调用第一码 =====
    fun verifyAndActivate(inputCode: String): ActivationResult {
        val normalized = inputCode.trim().uppercase()
        return when {
            normalized in LITE_CODES -> verifyAndActivateLite(normalized)
            normalized in PRO_CODES  -> verifyAndActivatePro(normalized)
            else -> ActivationResult.Failed("激活码无效")
        }
    }

    fun getAllLiteCodes(): Array<String> = LITE_CODES
    fun getAllProCodes(): Array<String>  = PRO_CODES

    fun generateCheckCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..4).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    fun getActivationInfo(): String {
        migrateLegacy()
        val prefs = App.instance.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        val lite = prefs.getString(KEY_LITE_CODE, "")?.takeIf { it.isNotEmpty() }
        val pro  = prefs.getString(KEY_PRO_CODE, "")?.takeIf { it.isNotEmpty() }
        val lt = prefs.getLong(KEY_LITE_TIME, 0).takeIf { it > 0 }?.let { fmt.format(java.util.Date(it)) }
        val pt = prefs.getLong(KEY_PRO_TIME, 0).takeIf { it > 0 }?.let { fmt.format(java.util.Date(it)) }
        return buildString {
            append("会员等级: ${memberLevel().displayName}\n")
            if (lite != null) append("LITE 激活码: $lite\n激活时间: $lt")
            if (pro != null) {
                if (lite != null) append("\n")
                append("PRO 激活码: $pro\n激活时间: $pt")
            }
        }
    }

    sealed class ActivationResult {
        data class LiteSuccess(val message: String) : ActivationResult()
        data class ProSuccess(val message: String)  : ActivationResult()
        data class Failed(val message: String)      : ActivationResult()
    }
}
