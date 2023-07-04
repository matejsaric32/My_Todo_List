package matejsaric32.android.mytodolist.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import matejsaric32.android.mytodolist.databinding.ItemCardBinding
import matejsaric32.android.mytodolist.models.Card

/**
 * Adapter for displaying the cards in a [RecyclerView].
 * @see Card
 * @see CardActivity
 * @param context Context of the activity.
 * @param list List of boards.
 */

class CardAdapter(
    private val context: Context,
    private var list: ArrayList<Card>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener: CardAdapter.OnClickListener? = null

    /**
     * Inflates item_board.xml layout file and returns the view holder.
     */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        )
    }

    /**
     * Binds each item's data to his view and .
     */


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is ViewHolder) {

            holder.tvTitle?.text = model.name
            if (model.colorLabel!!.isNotEmpty()){
                holder.vColorLabel!!.setBackgroundColor(Color.parseColor(model.colorLabel))
            }else{
                holder.vColorLabel!!.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if(onClickListener != null) {
                    onClickListener?.onClick(position, model)
                }
            }

            /**
             * If there is no checklist in list of cards hide text view
             */

            if (model.checkList.size == 0){
                holder.clCheckList.visibility = View.GONE
            }else{
                model.checkList.stream().filter { it.isChecked }.count()
                holder.tvTaskListNumber.text = "${model.checkList.stream().filter { it.isChecked }.count()} / ${model.checkList.size}"
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
     * Interface for handling clicks on the items.
     */

    interface OnClickListener {
        fun onClick(position: Int, model: Card)
    }

    /**
     * Function that sets the on click listener.
     * @see MainActivity.setUpBoardRecyclerView
     */

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    /**
     * Custom nested class made to hold the views of the item_card.xml layout.
     */

    class ViewHolder(bind: ItemCardBinding) : RecyclerView.ViewHolder(bind.root) {
        val tvTitle = bind.tvCardName
        val vColorLabel = bind.viewLabelColor
        val clCheckList = bind.clTaskList
        val tvTaskListNumber = bind.tvTaskListNumber
    }

}