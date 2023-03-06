package matejsaric32.android.mytodolist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.databinding.ItemMemberCardBinding
import matejsaric32.android.mytodolist.models.SelectedMembers
import matejsaric32.android.mytodolist.models.User

class CardMembersAdapter(
    private val context: Context,
    private var list: ArrayList<SelectedMembers>,
    private val assignedMembers: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: CardMembersAdapter.OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemMemberCardBinding.inflate(LayoutInflater.from(parent.context),
            parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is ViewHolder) {

            holder.ivMemberProfile.visibility = View.VISIBLE
            holder.tvMemberName.visibility = View.VISIBLE

            holder.tvMemberName.text = model.name

            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_account_circle_24)
                .into(holder.ivMemberProfile)

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class ViewHolder(view : ItemMemberCardBinding) : RecyclerView.ViewHolder(view.root) {
        val ivMemberProfile = view.sivMemberProfile
        val tvMemberName = view.tvMemberName
    }

    interface OnClickListener {
        fun onClick()
    }

    fun setOnClickListener(onClickListener: CardMembersAdapter.OnClickListener) {
        this.onClickListener = onClickListener
    }
}