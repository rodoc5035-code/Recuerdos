package com.example.util

import java.util.Calendar
import java.util.Locale

data class MemoryDate(
    val year: Int,
    val month: Int, // 1 to 12
    val day: Int    // 1 to 31
) : Comparable<MemoryDate> {

    val isoString: String
        get() = "%04d-%02d-%02d".format(Locale.US, year, month, day)

    val monthNameSpanish: String
        get() = when (month) {
            1 -> "Enero"
            2 -> "Febrero"
            3 -> "Marzo"
            4 -> "Abril"
            5 -> "Mayo"
            6 -> "Junio"
            7 -> "Julio"
            8 -> "Agosto"
            9 -> "Septiembre"
            10 -> "Octubre"
            11 -> "Noviembre"
            12 -> "Diciembre"
            else -> ""
        }

    val monthShortSpanish: String
        get() = monthNameSpanish.take(3).uppercase()

    val formattedDisplay: String
        get() = "$day de $monthNameSpanish de $year"

    val shortDisplay: String
        get() = "$day $monthShortSpanish $year"

    override fun compareTo(other: MemoryDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    companion object {
        fun today(): MemoryDate {
            val cal = Calendar.getInstance()
            return MemoryDate(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                day = cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun fromIso(iso: String): MemoryDate {
            val parts = iso.split("-")
            return if (parts.size == 3) {
                MemoryDate(
                    year = parts[0].toIntOrNull() ?: 2026,
                    month = parts[1].toIntOrNull() ?: 1,
                    day = parts[2].toIntOrNull() ?: 1
                )
            } else {
                today()
            }
        }

        fun daysInMonth(year: Int, month: Int): Int {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month - 1)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        }

        // Returns day of week where 0 = Monday, 6 = Sunday (ISO standard)
        fun firstDayOfWeekIndex(year: Int, month: Int): Int {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month - 1)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            val dow = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday ... 7 = Saturday
            return (dow + 5) % 7
        }
    }
}
