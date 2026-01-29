package com.ijackey.iMusic.data.model

data class EqualizerBand(
    val frequency: Int,
    val gain: Float,
    val label: String
)

data class EqualizerPreset(
    val name: String,
    val bands: List<Float>
)

object EqualizerPresets {
    val presets = listOf(
        EqualizerPreset("正常", listOf(0f, 0f, 0f, 0f, 0f)),
        EqualizerPreset("摇滚", listOf(3f, 2f, -1f, -1f, 3f)),
        EqualizerPreset("流行", listOf(-1f, 2f, 3f, 2f, -1f)),
        EqualizerPreset("古典", listOf(3f, 2f, -1f, 2f, 3f)),
        EqualizerPreset("爵士", listOf(2f, 1f, 1f, 2f, 3f)),
        EqualizerPreset("重低音", listOf(5f, 3f, 0f, -2f, -3f))
    )
}