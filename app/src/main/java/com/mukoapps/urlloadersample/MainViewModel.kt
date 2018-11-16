package com.mukoapps.urlloadersample

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.mukoapps.urlloader.DownloadableString

class MainViewModel : ViewModel() {
    val isRefreshing = MutableLiveData<Boolean>()
    val pinList = MutableLiveData<MutableList<Pin>>()
    val snackBarText = SingleLiveEvent<String>()
    private val gson = Gson()

    init {
        pinList.value= mutableListOf()
        isRefreshing.value = false
    }

    fun doRefresh() {
        isRefreshing.value = true
        //Load the main JSON using the library
        DownloadableString("http://pastebin.com/raw/wgkJgazE").load {result, throwable->
            if(throwable!=null)
            {
                isRefreshing.value=false
                snackBarText.value = "Error refreshing"
                return@load
            }
            //add some error handling, other stuff may go wrong
            //but error handling is not the purpose of this demonstration
            try
            {
                val items = mutableListOf<Pin>()
                val array =  gson.fromJson(result, JsonArray::class.java)
                array.mapTo(items) {
                    val obj = it.asJsonObject
                    val pin = Pin()
                    pin.id = obj["id"].asString
                    pin.createdAt = obj["created_at"].asString
                    pin.color = obj["color"].asString
                    pin.width = obj["width"].asInt
                    pin.height = obj["height"].asInt
                    pin.likedByUser = obj["liked_by_user"].asBoolean
                    pin.userName = obj["user"].asJsonObject["name"].asString
                    pin.url = obj["urls"].asJsonObject["regular"].asString
                    pin
                }
                pinList.value = items
                snackBarText.value = "Refreshed correctly"
            }
            catch(e:Exception)
            {
                snackBarText.value = "Error refreshing"
            }
            isRefreshing.value=false
        }


    }

}