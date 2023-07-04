package matejsaric32.android.mytodolist.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.adapter.ColorListAdapter
import matejsaric32.android.mytodolist.adapter.MemberAdapter
import matejsaric32.android.mytodolist.adapter.MembersCardList
import matejsaric32.android.mytodolist.adapter.MembersDialogListAdapter
import matejsaric32.android.mytodolist.databinding.DialogListBinding
import matejsaric32.android.mytodolist.models.SelectedMembers
import matejsaric32.android.mytodolist.models.User
import matejsaric32.android.mytodolist.utils.Constants

abstract class MemberListDialog(
    context: Context,
    private var list: ArrayList<SelectedMembers>,
) : Dialog(context){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DialogListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(binding)
    }

    private fun setUpRecyclerView(binding: DialogListBinding){
        binding.tvTitle.text = context.getString(R.string.select_member)

        binding.rvList.layoutManager = LinearLayoutManager(context)
        var mAdapter = MembersDialogListAdapter(context, list)
        binding.rvList.adapter = mAdapter

        mAdapter?.setOnClickListener(object : MembersDialogListAdapter.OnClickListener{
            override fun onClick(position: Int, model: SelectedMembers, action: String) {
                dismiss()
                if (model.isSelected == false){
                    onItemSelected(model, Constants.SELECT)
//                    Toast.makeText(context, "You clicked on ${model.name}", Toast.LENGTH_SHORT).show()
                }else{
                    onItemSelected(model, Constants.UN_SELECT)
//                    Toast.makeText(context, "You clicked on ${model.name}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    protected abstract fun onItemSelected(model: SelectedMembers, action: String)

}