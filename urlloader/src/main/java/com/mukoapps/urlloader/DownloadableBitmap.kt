package com.mukoapps.urlloader

import android.graphics.Bitmap
import android.graphics.BitmapFactory

class DownloadableBitmap(url: String) : Downloadable<Bitmap>(url) {
    override fun transform(content: DownloadableContent): Bitmap {
        return BitmapFactory.decodeByteArray(content, 0, content.size)
    }
}