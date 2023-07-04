package matejsaric32.android.mytodolist.adapter

import android.content.Context
import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import matejsaric32.android.mytodolist.activities.CardActivity
import matejsaric32.android.mytodolist.databinding.ItemCheckitemBinding
import matejsaric32.android.mytodolist.models.CheckItem

class CheckItemAdapter(
    private val context: Context,
    private var list: ArrayList<CheckItem>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemCheckitemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is ViewHolder) {
            holder.tvCheckItem.text = Editable.Factory.getInstance().newEditable(model.name)
            holder.ivCheckItem.isChecked = model.isChecked

            holder.deleteCheckItem.setOnClickListener {
                if (context is CardActivity) {
                    context.deleteCheckItem(position)
                }

            }

            holder.ivCheckItem.setOnClickListener {
                if (context is CardActivity) {
                    context.updateCheckItemStatus(position, holder.ivCheckItem.isChecked)
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class ViewHolder(binding: ItemCheckitemBinding) : RecyclerView.ViewHolder(binding.root) {
        val tvCheckItem = binding.tvTitleCheckitem
        val ivCheckItem = binding.checkBox

        val deleteCheckItem = binding.ibDeleteCheckitem
    }

    interface OnClickListener {
        fun onClick(position: Int, model: String)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

}