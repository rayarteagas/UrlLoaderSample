package com.mukoapps.urlloader

import android.util.LruCache
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.net.URL
import java.util.concurrent.Semaphore


class Loader private constructor(){
    //used to throttle simultaneous downloads to a max of 10
    private val semaphore = Semaphore(10)
    var cacheSize = 4 * 1024 * 1024 // 4MiB
    private val itemsFlow = Channel<String>()
    private val mainJob = Job()


    //centralized cache just to keep it simple without breaking the SOLID design pattern, a better
    // approach would be a per-downloadable type cache to avoid extra instantiation on cached resources
    private val cache = object : LruCache<String, DownloadableContent>(cacheSize) {
        override fun sizeOf(key: String, value: DownloadableContent): Int {
            return value.size

        }
    }

    data class DownloadTask(val downloadables: MutableList<Downloadable<*>>) {
        var task: Deferred<Unit>? = null

    }

    private val downloadTasks = mutableMapOf<String, DownloadTask>()

    private fun resizeCache(newSize: Int)
    {
        cache.resize(newSize)
    }

    /**
     * Check if a downloadable is cached, if so, immediately call loaded callback, if not cached,
     * will check if there is some task already downloading the same resource, if so, attach this
     * downloadable to that task, so it gets notified when such task finish loading the needed resource.
     *
     * If not cached and there are no tasks downloading the needed resource, push the url to the
     * "downloader" {@link #itemsFlow} channel, it will download the resource on its turn
     */
    private fun load(downloadable: Downloadable<*>) = CoroutineScope(Dispatchers.Main).launch {
        cache.maxSize()
        val fromCache = cache.get(downloadable.url)
        if (fromCache != null) {
            downloadable.callOnLoad(fromCache,null)
            return@launch
        }
        if (downloadTasks.containsKey(downloadable.url)) {
            downloadTasks[downloadable.url]?.downloadables!!.add(downloadable)
        } else {
            downloadTasks[downloadable.url] = DownloadTask(mutableListOf(downloadable))
            itemsFlow.send(downloadable.url)
        }
    }

    /**
     * cancels a downloadable, if there are no more downloadables needing the current resource
     * the corresponding download task will be cancelled
     */
    private fun cancel(downloadable: Downloadable<*>) {
        val urlAssociatedTask = downloadTasks[downloadable.url]
        if (urlAssociatedTask != null) {
            urlAssociatedTask.downloadables.remove(downloadable)
            if (urlAssociatedTask.downloadables.size < 1)
            {
                urlAssociatedTask.task?.cancel()
            }
        }
    }

    /**
     * This will process all the downloads, calling the downloadables onLoad callbacks when finished
     *.
     * After downloading a url, it will call the onLoad callback for each downloadable associated
     * with the downloaded url.
     */
    private fun mainProcess() = CoroutineScope(mainJob).launch {

        //I know using while(true) is a bad practice in majority of cases, but here it will run
        //during the whole app lifecycle. Other approaches would imply making this class disposable and
        //instantiable, and looping only when needed, but in this particular case, would add extra
        //and unwanted complexity as this is only with illustrative purposes
        while (true) {
            //semaphore is used to throttle downloads to a max of 10
            semaphore.acquire()
            //get an url to download from the queue
            val url = itemsFlow.receive()

            //this will run inside this coroutine scope, not extracted on purpose
            fun notifyCompletion(content:DownloadableContent?, throwable: Throwable?)
            {
                launch (Dispatchers.Main){
                    downloadTasks[url]?.downloadables?.forEach {
                        it.callOnLoad(content,throwable)
                    }
                    downloadTasks.remove(url)
                }
                semaphore.release()
            }

            //download urls asynchronously
            val def = async {
                if(downloadTasks[url]==null)
                    return@async
                val result = try {
                    URL(url).readBytes()
                } catch (e: Exception) {
                    //in case of error notify downloadables
                  notifyCompletion(null, e)
                    return@async
                }
                //if url is downloaded successfully
                //add its contents to cache
                cache.put(url, result)
                //notify all associated downloadables of completion
                notifyCompletion(result, null)
            }
            //attach this deferred result to download task
            downloadTasks[url]?.task = def
        }
    }

    //Used to implement singleton pattern, Instantiation would be a better approach
    //but this is used for demonstration purposes
    companion object {
        private var instance: Loader = Loader()

        init {
            instance.mainProcess()
        }

        fun load(downloadable: Downloadable<*>) {
            instance.load(downloadable)
        }

        fun cancel(downloadable: Downloadable<*>) {
            instance.cancel(downloadable)
        }

        fun resizeCache(newSize: Int)
        {
            instance.resizeCache(newSize)
        }

    }
}