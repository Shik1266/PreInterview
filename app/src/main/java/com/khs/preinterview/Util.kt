package com.khs.preinterview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * View를 비트맵으로 변환하는 확장 함수입니다.
 * 커스텀 마커 View를 NaverMapSDK에서 사용할 수 있는 Bitmap 형태로 변환할 때 사용됩니다.
 */
@SuppressLint("UseKtx")
fun View.toBitmap(): Bitmap {
    measure(
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    )
    layout(0, 0, measuredWidth, measuredHeight)
    val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    draw(canvas)
    return bitmap
}

/**
 * 시간 포맷 유틸리티 객체입니다.
 */
object TimeUtil {
    fun formatFromMillis(millis: Long): String {
        val sdf = SimpleDateFormat("MM.dd (HH:mm:ss)", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(millis))
    }

}
