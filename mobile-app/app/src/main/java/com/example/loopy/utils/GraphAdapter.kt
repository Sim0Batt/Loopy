package com.example.loopy.utils

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.loopy.R

class GraphsAdapter(private var images: List<Bitmap>) : RecyclerView.Adapter<GraphsAdapter.GraphViewHolder>() {

    inner class GraphViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.singleGraphImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GraphViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_graph, parent, false)
        return GraphViewHolder(view)
    }

    override fun onBindViewHolder(holder: GraphViewHolder, position: Int) {
        val currentImage = images[position]
        holder.imageView.setImageBitmap(currentImage)
    }

    override fun getItemCount(): Int = images.size

    fun updateData(newImages: List<Bitmap>) {
        images = newImages
        notifyDataSetChanged()
    }
}