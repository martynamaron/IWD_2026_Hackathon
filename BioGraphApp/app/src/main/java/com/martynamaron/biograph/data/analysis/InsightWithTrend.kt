package com.martynamaron.biograph.data.analysis

import com.martynamaron.biograph.data.local.InsightEntity

data class InsightWithTrend(
    val insight: InsightEntity,
    val trend: TrendDirection?
)
