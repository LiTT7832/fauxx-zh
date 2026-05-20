package com.fauxx.ui.format

import com.fauxx.data.model.ActionType

/**
 * Short, human-readable display label for an [ActionType], used in filter chips
 * and other UI where the raw enum name is too long or underscore-heavy.
 *
 * The enum's `name` remains the canonical identifier for DB rows, exports, and
 * logs — this extension is purely a presentation concern.
 */
val ActionType.label: String
    get() = when (this) {
        ActionType.SEARCH_QUERY -> "搜索"
        ActionType.AD_CLICK -> "广告点击"
        ActionType.PAGE_VISIT -> "页面访问"
        ActionType.LOCATION_SPOOF -> "位置"
        ActionType.DNS_LOOKUP -> "DNS"
        ActionType.COOKIE_HARVEST -> "Cookie"
        ActionType.DEEP_LINK_VISIT -> "深层链接"
        ActionType.FINGERPRINT_ROTATE -> "指纹"
    }
