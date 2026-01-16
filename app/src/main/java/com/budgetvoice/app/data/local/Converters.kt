package com.budgetvoice.app.data.local

import androidx.room.TypeConverter
import com.budgetvoice.app.data.model.ExpenseCategory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, formatter) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.format(formatter)
    }

    @TypeConverter
    fun fromCategory(category: ExpenseCategory): String {
        return category.name
    }

    @TypeConverter
    fun toCategory(value: String): ExpenseCategory {
        return ExpenseCategory.valueOf(value)
    }
}
