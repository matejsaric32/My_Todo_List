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

open class BoardAdapter(
    private val context: Context,
    private var list: ArrayList<Board>) : RecyclerView.Adapter<RecyclerView.ViewHolder>()   {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemBoardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is ViewHolder) {
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_account_circle_24)
                .into(holder.sivPicture!!)

            holder.tvTitle?.text = model.name
            holder.tvCreatedBy?.text = "Created by: ${model.createdBy}"

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

    class ViewHolder(bind: ItemBoardBinding) : RecyclerView.ViewHolder(bind.root) {
        val tvTitle = bind?.tvBoardName
        val tvCreatedBy = bind?.tvBoardCreatedBy
        val sivPicture = bind?.sivBoardImage
    }

    interface OnClickListener {
        fun onClick(position: Int, model: Board)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

}