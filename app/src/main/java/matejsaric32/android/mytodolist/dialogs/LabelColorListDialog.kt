package matejsaric32.android.mytodolist.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import matejsaric32.android.mytodolist.adapter.ColorListAdapter
import matejsaric32.android.mytodolist.databinding.DialogListBinding

abstract class LabelColorListDialog(
    context: Context,
    private var list: ArrayList<String>,
    private var mSelectedColor: String = "") : Dialog(context) {

    private var mAdapter: ColorListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DialogListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setUpRecyclerView(binding)
    }

    private fun setUpRecyclerView(binding: DialogListBinding) {
        binding.tvTitle.text = "Choose Label Color"
        binding.rvList.layoutManager = LinearLayoutManager(context)
        mAdapter = ColorListAdapter(context, list, mSelectedColor)
        binding.rvList.adapter = mAdapter
        mAdapter?.setOnClickListener(object : ColorListAdapter.OnClickListener{
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
                mAdapter!!.notifyDataSetChanged()
            }
        })
    }

    protected abstract fun onItemSelected(color: String)



}