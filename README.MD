Library functionality relies on Downloadable class.
DownloadableBitmap and DownloadableString has been provided as convenience implementations, but the user can implement any kind of dowload extenging the Downloadable class as fallows:

//Using WeirdFileFormat with illustrative purpose
class CustomDownloadable(url: String):Downloadable<WeirdFileFormat>(url)
{
    override fun transform(content: DownloadableContent): CustomType {
    	return WeirdFileFormat.fromBytes(content)
	}
}


Items can be downloaded using: Downloadable.load(url), for example:

DownloadableString("http://pastebin.com/raw/wgkJgazE").load {result, error->
	if(error==null)
	{
		//display text content to the user
		displayText(result)
	}
}

Downloads are choriographed within Loader class, this is where magic happens. For example, images and JSON text are cached efficiently in memory.
The cache has a configurable max capacity, it can be setted using:

Loader.resizeCache(10 * 1024 * 1024)//Set cache size to 10MB

You can request the same image from multiple places in parallel, the Loader would take care of downloading the resource only once, and notify all Downloadables waiting for requested resource as soon as it finises downloading the contents of the url.

You can cancell any Downloadable and it will not affect other downloadables waiting for the same resource. If there are no other dowloadables waiting for the resource, the download task will be cancelled to free valuable resources. This is usefull to avoid downloading resources that are no longer needed and preserve resources.

 downloadable.cancel()

You can download infinite resources using this lib, only 10 download tasks are allowed to run at the same time(may have attached many Downloadables each), the downloaded resources are cached in memory, and old unfrequently used items are evicted when the app needs more memory.