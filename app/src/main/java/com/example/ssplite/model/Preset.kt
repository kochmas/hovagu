package com.example.ssplite.model

import kotlinx.serialization.Serializable

@Serializable
data class Preset(
    val version: Int = 1,
    val name: String,
    val eq: Eq,
    val modulation: Modulation,
    val dynamics: DynamicsSettings
)

@Serializable
data class Eq(
    val low_shelf: Shelf,
    val peaks: List<Peak>,
    val high_shelf: Shelf,
    val tilt_db: Float = 0f
)

@Serializable
data class Shelf(val fc_hz: Float, val gain_db: Float)

@Serializable
data class Peak(val fc_hz: Float, val gain_db: Float, val q: Float)

@Serializable
data class Modulation(
    val enabled: Boolean,
    val rate_hz: Float,
    val depth_db: Float,
    val mode: String,
    val fc_drift_oct: Float,
    val jitter: Float
)

@Serializable
data class DynamicsSettings(
    val pregain_db: Float,
    val limiter: Limiter,
    val compressor: Compressor? = null
)

@Serializable
data class Limiter(
    val ceiling_dbfs: Float,
    val lookahead_ms: Float,
    val release_ms: Float
)

@Serializable
data class Compressor(
    val enabled: Boolean,
    val ratio: Float,
    val threshold_dbfs: Float,
    val attack_ms: Float,
    val release_ms: Float
)
