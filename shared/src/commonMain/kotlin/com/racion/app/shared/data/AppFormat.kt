package com.racion.app.shared.data

import com.racion.app.shared.models.DiaryEntry
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Formateo de fechas, horas y etiquetas en español.
 *
 * No depende de APIs de plataforma (NSDateFormatter / java.time) para que la misma
 * cadena se vea igual en Android, iOS y Desktop. La semana empieza en lunes.
 */
object AppFormat {

    private val weekdaysShort = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
    private val monthsShort = listOf(
        "Ene", "Feb", "Mar", "Abr", "May", "Jun",
        "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
    )

    fun nowEpochMillis(): Long = Clock.System.now().toEpochMilliseconds()

    fun localDateTime(
        epochMillis: Long,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): LocalDateTime = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone)

    fun localDate(
        epochMillis: Long,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): LocalDate = localDateTime(epochMillis, timeZone).date

    fun today(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
        Clock.System.now().toLocalDateTime(timeZone).date

    fun currentHour(timeZone: TimeZone = TimeZone.currentSystemDefault()): Int =
        Clock.System.now().toLocalDateTime(timeZone).hour

    fun epochDayOf(date: LocalDate): Long = date.toEpochDays().toLong()

    fun dateOfEpochDay(epochDay: Long): LocalDate = LocalDate.fromEpochDays(epochDay.toInt())

    /** "Mié 26 Ago" */
    fun dayLabel(date: LocalDate): String =
        "${weekdaysShort[date.dayOfWeek.ordinal]} ${date.dayOfMonth} ${monthsShort[date.monthNumber - 1]}"

    /** "ago" (quien lo muestra decide si lo pasa a mayúsculas). */
    fun shortMonth(date: LocalDate): String = monthsShort[date.monthNumber - 1]

    /** "12:45 PM" */
    fun timeLabel(
        epochMillis: Long,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        val dateTime = localDateTime(epochMillis, timeZone)
        val hour12 = if (dateTime.hour % 12 == 0) 12 else dateTime.hour % 12
        val minute = dateTime.minute.toString().padStart(2, '0')
        val suffix = if (dateTime.hour < 12) "AM" else "PM"
        return "$hour12:$minute $suffix"
    }

    /** "ALMUERZO · 12:45 PM" */
    fun mealRowMeta(
        entry: DiaryEntry,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String = "${entry.mealSlot.label.uppercase()} · ${timeLabel(entry.loggedAtEpochMillis, timeZone)}"

    /** "Hoy", "Ayer", "Hace 3 días"... */
    fun timeAgo(
        epochMillis: Long,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): String {
        val days = epochDayOf(today(timeZone)) - epochDayOf(localDate(epochMillis, timeZone))
        return when {
            days <= 0 -> "Hoy"
            days == 1L -> "Ayer"
            days < 7L -> "Hace $days días"
            days < 30L -> "Hace ${days / 7} sem"
            else -> "Hace ${days / 30} mes"
        }
    }

    fun isToday(
        epochMillis: Long,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Boolean = epochDayOf(localDate(epochMillis, timeZone)) == epochDayOf(today(timeZone))

    /** "1 unidad" / "3 unidades". */
    fun unitsLabel(units: Int): String = "$units unidad${if (units == 1) "" else "es"}"

    /** "63.0 kg" con un decimal fijo, sin APIs de formato de plataforma. */
    fun kilograms(value: Double): String {
        val scaled = (value * 10).roundToInt()
        val decimals = abs(scaled % 10)
        return "${scaled / 10}.$decimals kg"
    }
}
