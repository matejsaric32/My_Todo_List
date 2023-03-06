package matejsaric32.android.mytodolist.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import matejsaric32.android.mytodolist.adapter.ColorListAdapter
import matejsaric32.android.mytodolist.adapter.MemberAdapter
import matejsaric32.android.mytodolist.databinding.DialogListBinding
import matejsaric32.android.mytodolist.models.User
import matejsaric32.android.mytodolist.utils.Constants

abstract class MemberListDialog(
    context: Context,
    private var list: ArrayList<User>,
) : Dialog(context){

        private var mAdapter: MemberAdapter? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val binding = DialogListBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setCanceledOnTouchOutside(true)
            setCancelable(true)
            setUpRecyclerView(binding)
        }

    private fun setUpRecyclerView(binding: DialogListBinding){
        binding.tvTitle.text = "Select member"
        binding.rvList.layoutManager = LinearLayoutManager(context)
        mAdapter = MemberAdapter(context, list)
        binding.rvList.adapter = mAdapter
        mAdapter?.setOnClickListener(object : MemberAdapter.OnClickListener{
            override fun onClick(position: Int, model: User, action: String) {
                dismiss()
                onItemSelected(model, Constants.SELECT)
//                Toast.makeText(context, "You clicked on ${model.name}", Toast.LENGTH_SHORT).show()
            }

        })
    }

    protected abstract fun onItemSelected(model: User, action: String)

}