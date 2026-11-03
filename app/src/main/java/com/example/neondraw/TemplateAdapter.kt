package com.example.neondraw

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TemplateAdapter(
    private var items: List<StencilType>,
    private var selectedStencil: StencilType,
    private val onSelect: (StencilType) -> Unit
) : RecyclerView.Adapter<TemplateAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: View = view.findViewById(R.id.cardTemplate)
        val tvEmoji: TextView = view.findViewById(R.id.tvTemplateEmoji)
        val tvTitle: TextView = view.findViewById(R.id.tvTemplateTitle)
        val tvCategory: TextView = view.findViewById(R.id.tvTemplateCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_template_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvEmoji.text = item.emoji
        holder.tvTitle.text = item.title
        holder.tvCategory.text = item.category

        val isSelected = (item == selectedStencil)
        val border = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f
            setColor(if (isSelected) Color.parseColor("#4000FFFF") else Color.parseColor("#15FFFFFF"))
            setStroke(
                if (isSelected) 3 else 1,
                if (isSelected) Color.parseColor("#00FFFF") else Color.parseColor("#33FFFFFF")
            )
        }
        holder.container.background = border
        holder.tvTitle.setTextColor(if (isSelected) Color.parseColor("#00FFFF") else Color.WHITE)

        holder.container.setOnClickListener {
            selectedStencil = item
            notifyDataSetChanged()
            onSelect(item)
        }
    }

    override fun getItemCount() = items.size

    fun updateList(newItems: List<StencilType>, currentSelection: StencilType) {
        items = newItems
        selectedStencil = currentSelection
        notifyDataSetChanged()
    }
}
