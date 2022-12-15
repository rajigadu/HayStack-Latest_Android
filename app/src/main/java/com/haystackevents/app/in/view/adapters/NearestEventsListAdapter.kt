package com.haystackevents.app.`in`.view.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.haystackevents.app.`in`.R
import com.haystackevents.app.`in`.view.fragments.HomeFragment
import com.haystackevents.app.`in`.databinding.LayoutNearestEventsItemBinding
import com.haystackevents.app.`in`.network.config.AppConfig.IMAGE_BASE_URL
import com.haystackevents.app.`in`.network.response.nearest_events.NearestEventData
import java.io.IOException
import java.net.URL
import java.util.*

class NearestEventsListAdapter(var context: Context)
    : RecyclerView.Adapter<NearestEventsListAdapter.ViewHolder>() {

    private var listNearestEvents = arrayListOf<NearestEventData>()
    private var clickListener: NearestEventsOnClick? = null



    inner class ViewHolder(val binding: LayoutNearestEventsItemBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bindView(nearEvents: NearestEventData) {
            binding.eventName.text = nearEvents.event_name
            binding.eventsCategory.text = nearEvents.category

            /*if (nearEvents.photo.isNullOrEmpty()) {
                setEventTextColor(
                    IMAGE_BASE_URL + nearEvents.photo,
                    binding.eventName, binding.eventsCategory
                )
            }*/

            binding.nearestEventItem.setOnClickListener {
                clickListener?.nearestEventClick(nearEvents)
            }

            //Log.e("TAG", "images: "+nearEvents.photo)
            Glide.with(context)
                .asBitmap()
                .load(IMAGE_BASE_URL + nearEvents.photo)
                .placeholder(R.drawable.events_default_bg_)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {

                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {

                        if (resource != null) {
                            val palette = Palette.from(resource).generate()
                            val vibrant = palette.getVibrantColor(
                                ContextCompat.getColor(context, R.color.black)
                            )
                            val vibrantLight = palette.getMutedColor(
                                ContextCompat.getColor(context, R.color.white)
                            )
                            binding.eventsCategory.setTextColor(vibrantLight)
                            binding.eventName.setTextColor(vibrantLight)
                        }

                        return false
                    }

                })
                .into(binding.eventImage)
        }
    }

    private fun setEventTextColor(imageUrl: String, eventName: TextView, eventsCategory: TextView) {
        var bitmap: Bitmap? = null

        try{

            val url = URL(imageUrl)
            bitmap = BitmapFactory.decodeStream(url.openStream())

        }catch (e: IOException) {
            e.printStackTrace()
        }
        //val palette = Palette.from(bitmap!!).generate()
        
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutNearestEventsItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(listNearestEvents[position])
    }

    override fun getItemCount(): Int = listNearestEvents.size

    fun update(listNearestEvents: ArrayList<NearestEventData>, listener: HomeFragment){
        this.listNearestEvents = listNearestEvents
        this.clickListener = listener
        notifyDataSetChanged()
    }

    interface NearestEventsOnClick{
        fun nearestEventClick(nearEvents: NearestEventData)
    }
}