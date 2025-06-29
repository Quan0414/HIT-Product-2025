package com.example.hitproduct.util

import android.content.res.Resources

object Constant {

    var languageDefault = Resources.getSystem().configuration.locales[0].language!!
    const val READ_TIME_OUT    = 30L  // thời gian chờ đọc 30 giây
    const val CONNECT_TIME_OUT = 30L  // thời gian chờ kết nối 30 giây
}
