package si.um.feri.hillclimbracing.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import si.um.feri.hillclimbracing.R
import si.um.feri.hillclimbracing.Track
import si.um.feri.hillclimbracing.enums.DifficultyEnum

class TrackAdapter(
    private var data: MutableList<Track>,
    private val context: Context,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<TrackAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(p0: View?, position: Int)
        fun onItemLongClick(p0: View?, position: Int)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {
        val title: TextView = itemView.findViewById(R.id.displayTitle)
        val diff: TextView = itemView.findViewById(R.id.displayDifficultyValue)
        val desc: TextView = itemView.findViewById(R.id.displayDescription)

        val layout: ConstraintLayout = itemView.findViewById(R.id.constraintLayout)

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.onItemClick(p0, position)
            }
        }

        override fun onLongClick(p0: View?): Boolean {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
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

        holder.title.text = itemsViewModel.title
        holder.desc.text = itemsViewModel.description
        holder.diff.text = itemsViewModel.difficulty.value.toString()

        when (itemsViewModel.difficulty.value) {
            DifficultyEnum.HARD.value -> holder.diff.background.setTint(context.getColor(R.color.diff_hard))
            DifficultyEnum.MEDIUM.value -> holder.diff.background.setTint(context.getColor(R.color.diff_medium))
            else -> holder.diff.background.setTint(context.getColor(R.color.diff_easy))
        }

        if (!itemsViewModel.description.isEmpty()) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(holder.layout)
            constraintSet.clear(R.id.displayTitle, ConstraintSet.BASELINE)
            constraintSet.connect(
                R.id.displayTitle,
                ConstraintSet.TOP,
                R.id.displayDifficultyValue,
                ConstraintSet.TOP
            )
            constraintSet.applyTo(holder.layout)
        }
        else holder.desc.visibility = View.INVISIBLE
    }

    override fun getItemCount() = data.size
}