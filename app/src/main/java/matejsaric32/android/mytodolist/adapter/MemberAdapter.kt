package matejsaric32.android.mytodolist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.databinding.ItemBoardBinding
import matejsaric32.android.mytodolist.databinding.ItemMemberBinding
import matejsaric32.android.mytodolist.models.Board
import matejsaric32.android.mytodolist.models.User
import matejsaric32.android.mytodolist.utils.Constants

/**
 * Adapter for displaying the users in a [RecyclerView].
 * @see User
 * @see MemberActivity
 * @param context Context of the activity.
 * @param list List of members.
 */

class MemberAdapter(
    private val context: Context,
    private var list: ArrayList<User>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    /**
     * Inflates item_member.xml layout file and returns the view holder.
     */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemMemberBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        )
    }

    /**
     * Binds each item's data to his view
     */

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is ViewHolder) {
            holder.tvName?.text = model.name
            holder.tvEmail?.text = model.email
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_account_circle_24)
                .into(holder.sivProfile!!)


            holder.btnRemove.setOnClickListener{
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
     * Custom nested class made to hold the views of the item_member.xml layout.
     * @param bind Binding for the item_member.xml layout.
     */

    class ViewHolder(bind: ItemMemberBinding) : RecyclerView.ViewHolder(bind.root) {
        val tvName = bind.tvMemberName
        val tvEmail = bind.tvMemberEmail
        val sivProfile = bind.sivMemberProfile
        val btnRemove = bind.ivMemberRemove
    }

    /**
     * Interface for handling clicks on the items.
     */

    interface OnClickListener {
        fun onClick(position: Int, model: User)
    }

    /**
     * Function that sets the on click listener.
     * @param onClickListener Interface for handling clicks on the items.
     * @see nigdje
     */

    fun setOnClickListener(onClickListener: MemberAdapter.OnClickListener) {
        this.onClickListener = onClickListener
    }

}