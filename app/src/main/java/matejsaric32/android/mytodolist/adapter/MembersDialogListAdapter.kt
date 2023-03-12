package matejsaric32.android.mytodolist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.databinding.ItemCardMembersDialogBinding
import matejsaric32.android.mytodolist.databinding.ItemMemberCardBinding
import matejsaric32.android.mytodolist.models.SelectedMembers
import matejsaric32.android.mytodolist.models.User
import matejsaric32.android.mytodolist.utils.Constants

class MembersDialogListAdapter(
    private val context: Context,
    private var list: ArrayList<SelectedMembers>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: MembersDialogListAdapter.OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemCardMembersDialogBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is ViewHolder) {
            holder.tvMemberName.text = model.name

            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_account_circle_24)
                .into(holder.ivMemberProfile)

            holder.isSelected.visibility = if (model.isSelected) View.VISIBLE else View.GONE

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model, Constants.SELECT)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class ViewHolder(view : ItemCardMembersDialogBinding) : RecyclerView.ViewHolder(view.root) {
        val ivMemberProfile = view.sivMemberProfile
        val tvMemberName = view.tvMemberName
        val isSelected = view.ivMemberSelected
    }

    interface OnClickListener {
        fun onClick(position: Int, model: SelectedMembers, action: String)
    }

    fun setOnClickListener(onClickListener: MembersDialogListAdapter.OnClickListener) {
        this.onClickListener = onClickListener
    }
}