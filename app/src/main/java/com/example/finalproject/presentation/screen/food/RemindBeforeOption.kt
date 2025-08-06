package com.example.finalproject.presentation.screen.food

enum class RemindBeforeOption(val id: Int, val days: Int, val displayName: String) {
    SAME_DAY(1, 0, "Same day"),
    THREE_DAYS(2, 3, "3 days"),
    TEN_DAYS(3, 10, "10 days"),
    OTHER(4, -1, "Other"); // -1 indicates custom value

    companion object {
        fun getById(id: Int?): RemindBeforeOption {
            return entries.find { it.id == id } ?: SAME_DAY
        }

        fun getByDays(days: Int?): RemindBeforeOption {
            return when(days) {
                0 -> SAME_DAY
                3 -> THREE_DAYS
                10 -> TEN_DAYS
                null -> SAME_DAY
                else -> OTHER
            }
        }

        fun isCustomValue(days: Int?): Boolean {
            return days != null && days != 0 && days != 3 && days != 10
        }
    }
}