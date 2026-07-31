package com.allinone.toolbox.data

data class HardwareInfo(
    val cpuName: String,
    val cpuCores: Int,
    val cpuMaxFreq: String,
    val gpuName: String,
    val gpuVendor: String,
    val screenResolution: String,
    val screenDensity: String,
    val screenSize: String,
    val batteryLevel: Int,
    val batteryStatus: String,
    val batteryTechnology: String,
    val systemVersion: String,
    val androidVersion: String,
    val buildNumber: String,
    val deviceModel: String,
    val deviceBrand: String,
    val deviceCodeName: String,
    val hardwareReport: String
)
