package matejsaric32.android.mytodolist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.databinding.ItemMemberCardBinding
import matejsaric32.android.mytodolist.models.Board
import matejsaric32.android.mytodolist.models.SelectedMembers
import matejsaric32.android.mytodolist.models.User

/**
 * Adapter for displaying the selected in a [RecyclerView].
 * @see SelectedMembers
 * @see CardActivity
 * @param context Context of the activity.
 * @param list List of Members.
 */

class CardMembersAdapter(
    private val context: Context,
    private var list: ArrayList<SelectedMembers>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: CardMembersAdapter.OnClickListener? = null

    /**
     * Inflates item_member_card.xml layout file and returns the view holder.
     */


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(ItemMemberCardBinding.inflate(LayoutInflater.from(parent.context),
            parent, false))
    }

    /**
     * Binds each item's data to his view
     */

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

    /**
     * Gets the number of items in the list
     */

    override fun getItemCount(): Int {
        return list.size
    }

    /**
     * Custom nested class made to hold the views of the item_member_card.xml layout.
     * @param view View of the item_member_card.xml layout.
     * @see LabelColorListDialog
     */

    private class ViewHolder(view : ItemMemberCardBinding) : RecyclerView.ViewHolder(view.root) {
        val ivMemberProfile = view.sivMemberProfile
        val tvMemberName = view.tvMemberName
    }

    /**
     * Interface for handling clicks on the items.
     */

    interface OnClickListener {
        fun onClick()
    }

    /**
     * Sets the click listener for the items.
     * @param onClickListener Click listener for the items.
     * @see CardActivity.setUpSelectedMembersList
     */

    fun setOnClickListener(onClickListener: CardMembersAdapter.OnClickListener) {
        this.onClickListener = onClickListener
    }
}