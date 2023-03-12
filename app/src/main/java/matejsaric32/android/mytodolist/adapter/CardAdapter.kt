package matejsaric32.android.mytodolist.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import matejsaric32.android.mytodolist.databinding.ItemBoardBinding
import matejsaric32.android.mytodolist.databinding.ItemCardBinding
import matejsaric32.android.mytodolist.models.Board
import matejsaric32.android.mytodolist.models.Card

class CardAdapter(
    private val context: Context,
    private var list: ArrayList<Card>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var onClickListener: CardAdapter.OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is ViewHolder) {

            holder.tvTitle?.text = model.name
            if (model.colorLabel!!.isNotEmpty()){
                holder.vCollorLabel!!.setBackgroundColor(Color.parseColor(model.colorLabel))
            }else{
                holder.vCollorLabel!!.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if(onClickListener != null) {
                    onClickListener?.onClick(position, model)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener {
        fun onClick(position: Int, model: Card)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    class ViewHolder(bind: ItemCardBinding) : RecyclerView.ViewHolder(bind.root) {
        val tvTitle = bind?.tvCardName
        val rvMembers = bind?.rvCardSelectedMembersList
        val vCollorLabel = bind?.viewLabelColor
    }

}