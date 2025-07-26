package com.example.hitproduct.common.util

import android.content.res.Resources

object Constant {

    var languageDefault = Resources.getSystem().configuration.locales[0].language!!
    const val READ_TIME_OUT    = 30L  // thời gian chờ đọc 30 giây
    const val CONNECT_TIME_OUT = 30L  // thời gian chờ kết nối 30 giây

    const val HP_LEVEL_1 = 1000
    const val HP_LEVEL_2 = 2500
    const val HUNGER_LOW = 30
    const val HUNGER_MEDIUM = 80

    const val ARG_FOOD = "arg_food"

    const val ARG_NOTES = "arg_note"
    const val ARG_DATE = "arg_date"
    const val ARG_NOTE_ID = "arg_note_id"

}
