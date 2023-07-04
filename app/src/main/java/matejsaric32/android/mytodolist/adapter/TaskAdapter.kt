package matejsaric32.android.mytodolist.adapter

import android.annotation.SuppressLint
import android.content.ClipData.Item
import android.content.Context
import android.content.res.Resources
import android.text.Editable
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.grpc.internal.SharedResourceHolder.Resource
import matejsaric32.android.mytodolist.R
import matejsaric32.android.mytodolist.activities.TaskListActivity
import matejsaric32.android.mytodolist.databinding.ItemTaskBinding
import matejsaric32.android.mytodolist.models.Board
import matejsaric32.android.mytodolist.models.Card
import matejsaric32.android.mytodolist.models.Task
import matejsaric32.android.mytodolist.utils.Constants
import java.util.*
import kotlin.collections.ArrayList

/**
 * Adapter for displaying the Tasks in a [RecyclerView].
 * With all its forms and nested recycler views for cards.
 * @see Task
 * @see Card
 * @see TaskListActivity
 * @see CardAdapter
 * @param context Context of the activity.
 * @param list List of task.
 */

class TaskAdapter(
    private val context: Context,
    private var list: ArrayList<Task>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /**
     * Inflates item_task.xml layout file and returns the view holder.
     * And makes it have width of 70% of the screen and adds margins to it.
     */

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskAdapter.ViewHolder {
        val viewBinding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context),
            parent,
            false)

        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)

        layoutParams.setMargins((15.toDp().toPx()),
            (15.toDp().toPx()),
            (90.toDp().toPx()),
            (15.toDp().toPx()))

        viewBinding.root.layoutParams=layoutParams

        return ViewHolder(viewBinding)
    }

    /**
     * Binds each item's data to his view
     */

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[holder.adapterPosition]

        if (holder is ViewHolder) {

            if(holder.adapterPosition == list.size-1){
               holder.llAddTask.visibility = View.VISIBLE
               holder.clTaskTitle.visibility = View.GONE
            }else{
                holder.llAddTask.visibility = View.GONE
                holder.clTaskTitle.visibility = View.VISIBLE
            }

            /**
             * Listener for adding creating new list
             */

            holder.tvTitle.text = Editable.Factory.getInstance().newEditable(model.title)
            holder.llAddTask.setOnClickListener {
                holder.llAddTask.visibility = View.GONE
                holder.cvAddTaskListName.visibility = View.VISIBLE
            }

            /**
             * Listener for canceling creating new list
             */

            holder.btnCloseAddTaskList.setOnClickListener {
                holder.llAddTask.visibility = View.VISIBLE
                holder.cvAddTaskListName.visibility = View.GONE
            }
            /**
             *  Listener for crating new list
             */
            holder.btnFinishAddListName.setOnClickListener {
                val listName = holder.etAddTaskListName.text.toString()
                if(listName.isNotEmpty()){
                    if (context is TaskListActivity){
                        context.createTaskList(listName)
                    }
                }else{
                    Toast.makeText(context, "Please enter a list name", Toast.LENGTH_SHORT).show()
                }
            }
            /**
             * Listener for editing list name
             */
            holder.btnEditTaskName.setOnClickListener {

                holder.etEditTaskName.text = Editable.Factory.getInstance().newEditable(model.title)
                holder.cvCardTaskTitle.visibility = View.GONE
                holder.cvEditTaskName.visibility = View.VISIBLE
            }
            /**
             * Listener for canceling editing list name
             */
            holder.btnCloseEditListTaskName.setOnClickListener {
                holder.cvCardTaskTitle.visibility = View.VISIBLE
                holder.cvEditTaskName.visibility = View.GONE
            }
            /**
             * Listener for finishing editing list name
             */
            holder.btnFinishEditListName.setOnClickListener {
                val listName = holder.etEditTaskName.text.toString()
                if(listName.isNotEmpty()){
                    if (context is TaskListActivity){
                        context.updateTaskList(model, holder.adapterPosition, listName)
                    }
                }else{
                    Toast.makeText(context, "Please enter a list name", Toast.LENGTH_SHORT).show()
                }
            }
            /**
             * Listener for deleting list
             */
            holder.btnDeleteListName.setOnClickListener {
               alertDialogForDeleteList(holder.adapterPosition, model.title!!)
            }
            /**
             * Listener for adding card and showing it and CRUD operations
             */

            if(model.cards.size > 0){
                holder.rvTaskList.layoutManager = LinearLayoutManager(context)
                holder.rvTaskList.setHasFixedSize(true)

                val adapter = CardAdapter(context, model.cards)
                holder.rvTaskList.adapter = adapter

                /**
                 * Listener for dragging and dropping cards
                 */

                val itemTouchHelper = ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                ){
                    override fun onMove(
                        recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder
                    ): Boolean {
                        val draggedPosition = viewHolder.adapterPosition
                        val targetPosition = target.adapterPosition

                        Collections.swap(model.cards, draggedPosition, targetPosition)
                        adapter.notifyItemMoved(draggedPosition, targetPosition)
                        (context as TaskListActivity).swipeCardPlaces(model.cards, holder.adapterPosition)
                        return true
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                    }
                })

                itemTouchHelper.attachToRecyclerView(holder.rvTaskList)

                /**
                 * Listener when clicked go's to card details
                 * @see TaskListActivity.cardDetails
                 */

                adapter.setOnClickListener(object: CardAdapter.OnClickListener{
                    override fun onClick(positionCard: Int, model: Card) {
                        if (context is TaskListActivity){
                            context.cardDetails(holder.adapterPosition, positionCard)
                        }
                    }
                })
            }

            /**
             * Listener for adding card
             */

            holder.llAddCard.setOnClickListener {
                Toast.makeText(context, "dwqdq", Toast.LENGTH_SHORT).show()
                holder.llAddCard.visibility = View.GONE
                holder.cvAddCard.visibility = View.VISIBLE

                holder.btnCloseCardAdd.setOnClickListener {
                    holder.llAddCard.visibility = View.VISIBLE
                    holder.cvAddCard.visibility = View.GONE
                }

                /**
                 * Listener for adding card
                 * @see TaskListActivity.addCardToTaskList
                 */
                holder.btnFinishCardAdd.setOnClickListener {
                    val cardName = holder.etCardName.text.toString()
                    if(cardName.isNotEmpty()){
                        if (context is TaskListActivity){
                            context.addCardToTaskList(holder.adapterPosition, cardName.toString())
                        }
                    }else{
                        Toast.makeText(context, "Please enter a card name",
                            Toast.LENGTH_SHORT).show()
                    }
                }

                /**
                 * Setting up recycler view for cards in list of tasks
                 */

                holder.rvTaskList.layoutManager = LinearLayoutManager(context)
                holder.rvTaskList.setHasFixedSize(true)

                val adapter = CardAdapter(context, model.cards)
                holder.rvTaskList.adapter = adapter

                /**
                 * Listener when clicked go's to card details
                 */

                adapter.setOnClickListener(object: CardAdapter.OnClickListener{
                    override fun onClick(positionCard: Int, model: Card) {
                        if (context is TaskListActivity){
                            context.cardDetails(holder.adapterPosition, positionCard)
                        }
                    }
                })

            }

            holder.btnCloseCardAdd.setOnClickListener {
                Toast.makeText(context,"dwqedqe", Toast.LENGTH_LONG).show()
                holder.llAddCard.visibility = View.VISIBLE
                holder.cvAddCard.visibility = View.GONE
            }

        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: ItemTaskBinding) : RecyclerView.ViewHolder(view.root){
        val llAddTask = view.llAddTask
        val tvAddTask = view.tvAddTask

        val clTaskTitle = view.clTaskDetails

        val cvAddTaskListName = view.cvAddTaskListName
        val btnCloseAddTaskList = view.ibCloseListName
        val etAddTaskListName = view.etListName
        val btnFinishAddListName = view.ibFinishListName

        val cvCardTaskTitle = view.cvCardTaskTitle
        val tvTitle = view.tvTitle
        val btnEditTaskName = view.ibEditListName
        val btnDeleteListName = view.ibDeleteListName

        val cvEditTaskName = view.cvEditTaskName
        val btnCloseEditListTaskName = view.ibCloseEditListName
        val etEditTaskName = view.etEditListName
        val btnFinishEditListName = view.ibEditFinishListName

        val rvTaskList = view.rvCardList

        val cvAddCard = view.cvCardTitleName
        val btnCloseCardAdd = view.ibCloseCardName
        val etCardName = view.etCardName
        val btnFinishCardAdd = view.ibFinishCardName

        val llAddCard = view.llAddCard
    }

    private fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun alertDialogForDeleteList(position: Int, title: String) {

        val builderMaterial = MaterialAlertDialogBuilder(context)
        .setTitle("Alert")
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setMessage("Are you sure you want to delete $title.")
        .setPositiveButton(Html.fromHtml("<font color='#000000'>Yes</font>")) { dialog, which ->
            dialog.dismiss()
            if (context is TaskListActivity) {
                context.deleteTaskList(position)
            }
        }
        .setNegativeButton(Html.fromHtml("<font color='#000000'>No</font>")){ dialog, which ->
            dialog.dismiss()}
        .create()

        builderMaterial.setCancelable(false)
        builderMaterial.show()
    }


}