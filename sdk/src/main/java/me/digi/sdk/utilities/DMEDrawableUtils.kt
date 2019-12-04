package me.digi.sdk.utilities

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import kotlin.math.max

internal object DMEDrawableUtils {

    fun createBitmap(from: Drawable): Bitmap {
        if (from is BitmapDrawable && from.bitmap != null) {
            return from.bitmap
        }

        val bitmap = Bitmap.createBitmap(max(1, from.intrinsicWidth), max(1, from.intrinsicHeight), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        from.setBounds(0, 0, canvas.width, canvas.height)
        from.draw(canvas)
        return bitmap
    }
}