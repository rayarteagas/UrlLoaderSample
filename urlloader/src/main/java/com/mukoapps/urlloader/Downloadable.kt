package com.mukoapps.urlloader

import android.util.Log

abstract class Downloadable<T>(var url: String) {
    private var cancelled = false
    private var used = false
    private lateinit var onLoad: (T?, throwable: Throwable?) -> Unit
    abstract fun transform(content: DownloadableContent): T
    fun load(onLoad: (T?, Throwable?) -> Unit) {
        Log.e("downloadable", "load()")
        if (used)
            throw IllegalStateException("This can be called only once per downloadable")
        used = true
        this.onLoad = onLoad
        Loader.load(this)
    }

    fun callOnLoad(content: DownloadableContent?, throwable: Throwable?) {
        if (cancelled)
            return
        onLoad( if(content!=null) transform(content) else null, throwable)
    }

    fun cancel() {
        cancelled = true
        Loader.cancel(this)
    }
}