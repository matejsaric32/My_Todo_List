package matejsaric32.android.mytodolist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.databinding.ItemAttachmentBinding
import matejsaric32.android.mytodolist.databinding.ItemBoardBinding
import matejsaric32.android.mytodolist.models.Board

class AttachmentAdapter (
    private val context: Context,
    private val list: ArrayList<String> ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemAttachmentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false)
        )
    }

    class ViewHolder(bind: ItemAttachmentBinding) : RecyclerView.ViewHolder(bind.root) {
        val ivImageView = bind.attachmentImageView
        val btnDelete = bind.attachmentDeleteImageView
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is ViewHolder) {

            Glide
                .with(context)
                .load(model)
                .centerCrop()
                .placeholder(R.drawable.baseline_insert_drive_file_24)
                .into(holder.ivImageView)

            holder.btnDelete.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener {
        fun onClick(position: Int, model: String)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }
}