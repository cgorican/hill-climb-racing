package si.um.feri.hillclimbracing.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import si.um.feri.hillclimbracing.R
import si.um.feri.hillclimbracing.Track
import si.um.feri.hillclimbracing.enums.DifficultyEnum

class TrackAdapter(
    private var data: MutableList<Track>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<TrackAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(p0: View?, position: Int)
        fun onItemLongClick(p0: View?, position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {
        val title: TextView = itemView.findViewById(R.id.displayTitle)
        val difficulty: TextView = itemView.findViewById(R.id.displayDifficultyValue)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION) {
                listener.onItemClick(p0, position)
            }
        }

        override fun onLongClick(p0: View?): Boolean {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION) {
                listener.onItemLongClick(p0, position)
                return true
            }
            return false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.track_card_view, parent, false)

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemsViewModel = data[position]

        holder.title.text = String.format("%s",itemsViewModel.title)
        holder.difficulty.text = String.format("%d", itemsViewModel.difficulty.value)
        //holder.difficulty.background.setTint(Color.RED) -- not working for R.colors
    }

    override fun getItemCount() = data.size
}