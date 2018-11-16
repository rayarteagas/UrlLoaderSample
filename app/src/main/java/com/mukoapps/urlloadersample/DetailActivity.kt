package com.mukoapps.urlloadersample

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewCompat
import com.mukoapps.urlloader.DownloadableBitmap
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val pin = intent.extras.getSerializable("pin") as Pin

        pict.setBackgroundColor(Color.parseColor(pin.color))
        url.text=pin.url
        created.text=pin.createdAt
        user.text=pin.userName
        dimentions.text = "${pin.width}x${pin.height}"

        ViewCompat.setTransitionName(pict, getString(R.string.image_item_for_transition))

        DownloadableBitmap(pin.url).load { result, throwable->
            if(throwable!=null)
                pict.setImageBitmap(BitmapFactory.decodeResource(resources,R.drawable.error))
            else
                pict.setImageBitmap(result!!)
        }

    }
}
