package com.haystackevents.app.`in`.utils

import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.haystackevents.app.`in`.R

class RecyclerViewCustomAnimation: DefaultItemAnimator() {


    override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean {

        holder?.itemView?.animation = AnimationUtils.loadAnimation(
            holder?.itemView?.context,
            R.anim.anim_bottom_to_top
        )

        return super.animateAdd(holder)
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder?): Boolean {
        return super.animateRemove(holder)
    }

}