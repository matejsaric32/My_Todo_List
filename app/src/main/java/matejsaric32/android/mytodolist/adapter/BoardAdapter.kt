package matejsaric32.android.mytodolist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.databinding.ItemBoardBinding
import matejsaric32.android.mytodolist.models.Board

/**
 * Adapter for displaying the boards in a [RecyclerView].
 * @see Board
 * @see MainActivity
 * @param context Context of the activity.
 * @param list List of boards.
 */

open class BoardAdapter (
    private val context: Context,
    private var list: ArrayList<Board>) : RecyclerView.Adapter<RecyclerView.ViewHolder>()   {

    private var onClickListener: OnClickListener? = null

    /**
     * Inflates item_board.xml layout file and returns the view holder.
     */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemBoardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        )
    }

    /**
     * Binds each item's data to his view
     */

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is ViewHolder) {
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.baseline_supervised_user_circle_24)
                .into(holder.sivPicture!!)

            holder.tvTitle?.text = model.name
            holder.tvCreatedBy?.text = "Created by: ${model.createdBy}"

            /**
             * On click listener for the items and what data it has.
             * @see MainActivity.setUpBoardRecyclerView
             */

            holder.itemView.setOnClickListener {
                if(onClickListener != null) {
                    onClickListener?.onClick(position, model)
                }
            }
        }
    }

    /**
     * Gets the number of items in the list
     */

    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * Custom nested class made to hold the views of the item_board.xml layout.
     * @param bind Binding for the item_board.xml layout.
     */

    class ViewHolder(bind: ItemBoardBinding) : RecyclerView.ViewHolder(bind.root) {
        val tvTitle = bind?.tvBoardName
        val tvCreatedBy = bind?.tvBoardCreatedBy
        val sivPicture = bind?.sivBoardImage
    }

    /**
     * Interface for handling clicks on the items.
     */

    interface OnClickListener {
        fun onClick(position: Int, model: Board)
    }

    /**
     * Function that sets the on click listener.
     * @param onClickListener Interface for handling clicks on the items.
     * @see MainActivity.setUpBoardRecyclerView
     */

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

}