package com.example.clipboard

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class _adapter(var con_List: ArrayList<Connection_data>) :
    RecyclerView.Adapter<_adapter.my_ViewHolder>() {

    var onItemClick : ((String) -> Unit)? = null


    // Describes an item view and its place within the RecyclerView
    inner class my_ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val flowerTextView: TextView = itemView.findViewById(R.id.textView_1)
        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(con_List.get(adapterPosition).name)
            }
        }

        fun bind(word: String) {
            flowerTextView.text = word
        }
    }

    // Returns a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): my_ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.text_row_item, parent, false)
        return my_ViewHolder(view)
    }


    // Returns size of data list
    override fun getItemCount(): Int {
        return con_List.size
    }

    // Displays data at a certain position
    override fun onBindViewHolder(holder: my_ViewHolder, position: Int) {
        holder.bind(con_List[position].name)
    }
    fun submitlist(connection : Connection_data){
        Log.i("@@@@@", "ADAPTER CALLEd")
        con_List.add(connection)
        notifyItemInserted(con_List.size-1)
    }
    fun remove_item(position:Int){
        con_List.removeAt(position)
        notifyItemRemoved(position)
    }

}
