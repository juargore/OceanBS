package com.glass.oceanbs.extensions

import android.content.Context
import com.glass.oceanbs.Constants
import com.glass.oceanbs.R
import com.glass.oceanbs.models.OWNER
import java.util.*

fun getDateFormatted(c: Context) : String {
    val calendar = Calendar.getInstance()
    val day = calendar[Calendar.DAY_OF_MONTH]
    val year = calendar[Calendar.YEAR]

    return c.getString(
        R.string.date_formatted,
        getNameOfDayOfWeek(c),
        day.toString(),
        getNameOfMonth(c),
        year.toString()
    )
}

fun getNameOfDayOfWeek(c: Context) : String {
    val calendar = Calendar.getInstance()
    return when (calendar[Calendar.DAY_OF_WEEK]) {
        Calendar.MONDAY -> c.getString(R.string.date_monday)
        Calendar.TUESDAY -> c.getString(R.string.date_tuesday)
        Calendar.WEDNESDAY -> c.getString(R.string.date_wednesday)
        Calendar.THURSDAY -> c.getString(R.string.date_thursday)
        Calendar.FRIDAY -> c.getString(R.string.date_friday)
        Calendar.SATURDAY -> c.getString(R.string.date_saturday)
        Calendar.SUNDAY -> c.getString(R.string.date_sunday)
        else -> ""
    }
}

fun getNameOfMonth(c: Context) : String {
    val calendar = Calendar.getInstance()
    return when(calendar[Calendar.MONTH]) {
        Calendar.JANUARY -> c.getString(R.string.month_january)
        Calendar.FEBRUARY -> c.getString(R.string.month_february)
        Calendar.MARCH -> c.getString(R.string.month_march)
        Calendar.APRIL -> c.getString(R.string.month_april)
        Calendar.MAY -> c.getString(R.string.month_may)
        Calendar.JUNE -> c.getString(R.string.month_june)
        Calendar.JULY -> c.getString(R.string.month_july)
        Calendar.AUGUST -> c.getString(R.string.month_august)
        Calendar.SEPTEMBER -> c.getString(R.string.month_september)
        Calendar.OCTOBER -> c.getString(R.string.month_october)
        Calendar.NOVEMBER -> c.getString(R.string.month_november)
        Calendar.DECEMBER -> c.getString(R.string.month_december)
        else -> ""
    }
}

fun getUserTypeStr(c: Context) : String {
    return if (Constants.getTipoUsuario(c) == OWNER) {
        c.getString(R.string.new_aftermarket_owner)
    } else {
        c.getString(R.string.new_aftermarket_collaborator)
    }
}
