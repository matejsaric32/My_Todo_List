package matejsaric32.android.mytodolist.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.databinding.ItemMemberBinding
import matejsaric32.android.mytodolist.models.Board
import matejsaric32.android.mytodolist.models.User
import matejsaric32.android.mytodolist.utils.Constants

/**
 * Adapter for displaying the boards in a [RecyclerView].
 * @see
 * @see nigdje
 * @param context Context of the activity.
 * @param list List of boards.
 */

class MembersCardList(
    private val context: Context,
    private var list: ArrayList<User>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onClickListener: MemberAdapter.OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            ItemMemberBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        )
    }

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

//            if (model.selected) {
//                holder.ivSelected?.visibility = View.VISIBLE
//            }else{
//                holder.ivSelected?.visibility = View.INVISIBLE
//            }

            holder.itemView.setOnClickListener{
                if (onClickListener != null) {
//                    onClickListener?.onClick(position, model, Constants.UN_SELECT)
                }else{
//                    onClickListener?.onClick(position, model, Constants.SELECT)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(bind: ItemMemberBinding) : RecyclerView.ViewHolder(bind.root) {
        val tvName = bind?.tvMemberName
        val tvEmail = bind?.tvMemberEmail
        val sivProfile = bind?.sivMemberProfile
    }

    interface OnClickListener {
        fun onClick(position: Int, model: User, action: String)
    }

    fun setOnClickListener(onClickListener: MemberAdapter.OnClickListener) {
        this.onClickListener = onClickListener
    }
}
