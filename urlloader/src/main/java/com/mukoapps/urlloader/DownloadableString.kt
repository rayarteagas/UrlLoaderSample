package com.mukoapps.urlloader

class DownloadableString(url: String) : Downloadable<String>(url) {
    override fun transform(content: DownloadableContent): String {
        val str = content.toString(Charsets.UTF_8)
        return str
    }
}