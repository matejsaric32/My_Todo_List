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
import matejsaric32.android.mytodolist.models.Board
import matejsaric32.android.mytodolist.models.SelectedMembers
import matejsaric32.android.mytodolist.models.User
import matejsaric32.android.mytodolist.utils.Constants

/**
 * Adapter for displaying the SelectedMembers in a [RecyclerView].
 * @see SelectedMembers
 * @see MemberListDialog
 * @param context Context of the activity.
 * @param list List of selected members.
 */

class MembersDialogListAdapter(
    private val context: Context,
    private var list: ArrayList<SelectedMembers>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: MembersDialogListAdapter.OnClickListener? = null

    /**
     * Inflates item_card_members_dialog.xml layout file and returns the view holder.
     */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemCardMembersDialogBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        )
    }

    /**
     * Binds each item's data to his view
     */

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

    private class ViewHolder(view : ItemCardMembersDialogBinding) : RecyclerView.ViewHolder(view.root) {
        val ivMemberProfile = view.sivMemberProfile
        val tvMemberName = view.tvMemberName
        val isSelected = view.ivMemberSelected
    }

    /**
     * Interface for handling clicks on the items.
     */

    interface OnClickListener {
        fun onClick(position: Int, model: SelectedMembers, action: String)
    }

    /**
     * Function that sets the on click listener.
     * @param onClickListener Interface for handling clicks on the items.
     * @see MemberListDialog.setUpRecyclerView
     */

    fun setOnClickListener(onClickListener: MembersDialogListAdapter.OnClickListener) {
        this.onClickListener = onClickListener
    }
}