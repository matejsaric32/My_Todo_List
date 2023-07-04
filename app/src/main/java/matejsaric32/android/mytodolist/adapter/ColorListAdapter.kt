package matejsaric32.android.mytodolist.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import matejsaric32.android.mytodolist.databinding.DialogColorPickerBinding
import matejsaric32.android.mytodolist.databinding.ItemTaskBinding
import matejsaric32.android.mytodolist.models.Board
import matejsaric32.android.mytodolist.models.Card

/**
 * Adapter for displaying the colors in a [RecyclerView].
 * @see CardActivity
 * @param context Context of the activity.
 * @param list List of boards.
 */


class ColorListAdapter(
    private val context: Context,
    private var list: ArrayList<String>,
    private var colorSelected : String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: ColorListAdapter.OnClickListener? = null

    /**
     * Inflates dialog_color_picker.xml layout file and returns the view holder.
     */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(DialogColorPickerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false))
    }

    /**
     * Binds each item's data to his view
     */

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is ViewHolder) {
            holder.viewColor?.setBackgroundColor(Color.parseColor(model))
            holder.viewSelected?.visibility = View.VISIBLE

            if (model == colorSelected) {

                holder.viewSelected?.visibility = View.VISIBLE
            }else{
                holder.viewSelected?.visibility = View.INVISIBLE
            }

            holder.itemView.setOnClickListener{
                if (onClickListener != null) {
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
     * @param bind Binding for the dialog_color_picker.xml layout.
     */

    class ViewHolder(view: DialogColorPickerBinding) : RecyclerView.ViewHolder(view.root){
        val viewColor = view.viewMain
        val viewSelected = view.ivSelectedColor
    }

    /**
     * Interface for the click listener.
     */

    interface OnClickListener {
        fun onClick(position: Int, model: String)
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