package com.haystackevents.app.`in`.view.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.haystackevents.app.`in`.databinding.LayoutCategoriesListItemBinding
import com.haystackevents.app.`in`.network.response.categories.Data
import java.util.ArrayList


class CategoriesListAdapter(val context: Context)
    : RecyclerView.Adapter<CategoriesListAdapter.ViewHolder>() {

    private var listCategories = arrayListOf<Data>()
    val categoriesMap = arrayListOf<String>()


    inner class ViewHolder(val binding: LayoutCategoriesListItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bindView(data: Data) {
            binding.categoriesItem.text = data.category

            Glide.with(context).load(data.photo).apply(RequestOptions().fitCenter()).into(
                object : CustomTarget<Drawable>(50,50){
                    override fun onLoadCleared(placeholder: Drawable?) {
                        binding.categoriesItem.setCompoundDrawablesWithIntrinsicBounds(placeholder,
                            null, null, null)
                    }
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        binding.categoriesItem.setCompoundDrawablesWithIntrinsicBounds(resource, null, null, null)
                    }
                }
            )

            binding.categoriesItem.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) categoriesMap.add(data.id)
                else categoriesMap.remove(data.id)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesListAdapter.ViewHolder =
        ViewHolder(LayoutCategoriesListItemBinding.inflate(LayoutInflater.from(
            parent.context
        ), parent, false))

    override fun onBindViewHolder(holder: CategoriesListAdapter.ViewHolder, position: Int) {
        holder.bindView(listCategories[position])
    }

    override fun getItemCount(): Int = listCategories.size

    fun update(listCategories: ArrayList<Data>) {
        this.listCategories = listCategories
        notifyDataSetChanged()
    }

    fun getSelectedCategories(): ArrayList<String> = this.categoriesMap
}