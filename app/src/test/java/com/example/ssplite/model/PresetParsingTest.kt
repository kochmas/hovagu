package com.example.ssplite.model

import java.io.ByteArrayInputStream
import org.junit.Assert.assertEquals
import org.junit.Test

class PresetParsingTest {
    @Test
    fun parsesPresetJson() {
        val json = """
            {
              "version": 1,
              "name": "Soziale Stimme",
              "eq": {
                "low_shelf": { "fc_hz": 150, "gain_db": -10 },
                "peaks": [
                  { "fc_hz": 1000, "gain_db": 4, "q": 1.0 },
                  { "fc_hz": 2000, "gain_db": 4, "q": 1.0 }
                ],
                "high_shelf": { "fc_hz": 5000, "gain_db": -8 },
                "tilt_db": 0
              },
              "modulation": {
                "enabled": false,
                "rate_hz": 0.05,
                "depth_db": 0.8,
                "mode": "gain",
                "fc_drift_oct": 0.0,
                "jitter": 0.1
              },
              "dynamics": {
                "pregain_db": -3,
                "limiter": { "ceiling_dbfs": -1.0, "lookahead_ms": 5, "release_ms": 100 },
                "compressor": { "enabled": false, "ratio": 1.3, "threshold_dbfs": -24, "attack_ms": 15, "release_ms": 120 }
              }
            }
        """.trimIndent()
        val preset = PresetStore.load(ByteArrayInputStream(json.toByteArray()))
        assertEquals("Soziale Stimme", preset.name)
        assertEquals(-3f, preset.dynamics.pregain_db)
        assertEquals(2, preset.eq.peaks.size)
    }
}

