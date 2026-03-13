package com.martynamaron.biograph.data.analysis

enum class TrendDirection(val arrow: String, val label: String) {
    STRENGTHENING("↑", "Strengthening"),
    WEAKENING("↓", "Weakening"),
    STABLE("→", "Stable");

    val displayText: String get() = "$arrow $label"
}
