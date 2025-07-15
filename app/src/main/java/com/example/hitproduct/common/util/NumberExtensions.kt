package com.example.hitproduct.common.util

import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

/** Định dạng Int thành chuỗi có dấu phẩy ngăn cách hàng nghìn */
fun Int.toThousandComma(): String =
    NumberFormat.getNumberInstance(Locale.US).format(this)

/** Nếu là Long */
fun Long.toThousandComma(): String =
    NumberFormat.getNumberInstance(Locale.US).format(this)

/** Nếu bạn muốn luôn có 2 chữ số thập phân */
fun Double.toThousandComma(): String =
    DecimalFormat("#,###.##").format(this)