package com.mukoapps.urlloadersample

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.recyclerview.extensions.AsyncListDiffer
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mukoapps.urlloader.Downloadable
import com.mukoapps.urlloader.DownloadableBitmap
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.pin_layout.view.*
import java.io.Serializable
import android.view.ViewAnimationUtils
import android.animation.Animator
import com.mukoapps.urlloader.Loader


class MainActivity : AppCompatActivity() {

    private lateinit var model: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Called here only for demonstrative purposes, in practice should be moved to the Application class
        Loader.resizeCache(10 * 1024 * 1024)//Set cache size to 10MB

        //get the ViewModel for this activity
        model = ViewModelProviders.of(this).get(MainViewModel::class.java)

       //configure views
        main_r_v.layoutManager = LinearLayoutManager(this)
        main_r_v.adapter = MainPinRecyclerViewAdapter()

        //attach events
        swiper.setOnRefreshListener {
            model.doRefresh()
        }

        fab.setOnClickListener {
            showCoolAnimation()
            (main_r_v.adapter as MainPinRecyclerViewAdapter).sendList(model.pinList.value!!.shuffled())
            Snackbar.make(mainView,"Pins shuffled!",Snackbar.LENGTH_SHORT).show()
        }

        //attach observers
        model.isRefreshing.observe(this, Observer {
            swiper.isRefreshing = it!!
        })

        model.pinList.observe(this, Observer {
            (main_r_v.adapter as MainPinRecyclerViewAdapter).sendList(it!!)
        })

        model.snackBarText.observe(this, Observer {
            if(it!=null)
                Snackbar.make(mainView,it,Snackbar.LENGTH_SHORT).show()
        })
    }

    private fun showCoolAnimation()
    {
        val centerX = (fab.left + fab.right) / 2
        val centerY = (fab.top + fab.bottom) / 2

        val startRadius = 0
        val endRadius = Math.max(coolAnimationView.width, coolAnimationView.height)
        val anim = ViewAnimationUtils.createCircularReveal(coolAnimationView, centerX, centerY, startRadius.toFloat(), endRadius.toFloat())
        anim.addListener(object:Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                coolAnimationView.visibility=View.INVISIBLE
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {}

        })
        coolAnimationView.visibility=View.VISIBLE
        anim.start()
    }


    inner class MainPinRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        //ListDiffer used to perform item animations on changes
        private val mDiffer = AsyncListDiffer(this, object: DiffUtil.ItemCallback<Pin>() {
            override fun areItemsTheSame(oldItem: Pin, newItem: Pin): Boolean {
               return oldItem.id==newItem.id
            }

            override fun areContentsTheSame(
                    oldPin: Pin, newPin: Pin):Boolean {
                return oldPin.likedByUser==newPin.likedByUser &&
                        oldPin.color==newPin.color &&
                        oldPin.url==newPin.url &&
                        oldPin.width==newPin.width &&
                        oldPin.height==newPin.height &&
                        oldPin.createdAt==newPin.createdAt
            }
        })

        //send new list of pins to adapter
        fun sendList(pins: List<Pin>)
        {
            mDiffer.submitList(pins)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val v = layoutInflater.inflate(R.layout.pin_layout, parent, false)
            val vh = PinHolder(v)
            vh.imageView = v.pict
            vh.textView = v.nameTv
            return vh
        }

        override fun getItemCount(): Int {
            return mDiffer.currentList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val pinHolder =holder as PinHolder
            val pin =  mDiffer.currentList[position]
            pinHolder.bindTo(pin)
        }

        internal inner class PinHolder(itemView: View): RecyclerView.ViewHolder(itemView)
        {
            lateinit var imageView: ImageView
            private var downloadable: Downloadable<*>? = null
            lateinit var textView: TextView

            fun bindTo(pin: Pin)
            {
                //clear dirty views
                downloadable?.cancel()
                imageView.setImageDrawable(null)

                //attach new content
                textView.text = pin.userName
                imageView.setBackgroundColor(Color.parseColor(pin.color))

                //preset height for future image load
                imageView.post {
                    if(pin.width < imageView.width)
                    {
                        imageView.minimumHeight=pin.height
                        return@post
                    }

                    val scale = pin.width.toFloat()/imageView.width.toFloat()
                    val scaledHeight = pin.height.toFloat()/scale
                    imageView.minimumHeight=scaledHeight.toInt()

                }

                //load image using library
                val down = DownloadableBitmap(pin.url)
                downloadable = down
                down.load {result, throwable->
                    if(throwable!=null)//if error show placeholder
                        imageView.setImageBitmap(BitmapFactory.decodeResource(resources,R.drawable.error))
                    else//show loaded image
                        imageView.setImageBitmap(result!!)
                }

                //attach events to open pin details
                itemView.setOnClickListener {
                    val intent = Intent(this@MainActivity,DetailActivity::class.java).putExtra("pin",pin as Serializable)
                    val activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this@MainActivity,
                            android.support.v4.util.Pair<View, String>(imageView,
                                    getString(R.string.image_item_for_transition)))
                    startActivity(intent,activityOptions.toBundle())

                }
            }
        }

    }
}
