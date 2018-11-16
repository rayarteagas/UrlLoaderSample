package com.mukoapps.urlloadersample

import java.io.Serializable

data class Pin(var id:String ="",
               var createdAt:String = "",
               var color:String = "#000000",
               var width:Int = 0,
               var height:Int = 0,
               var likedByUser:Boolean =false,
               var userName:String = "",
               var url:String =""):Serializable