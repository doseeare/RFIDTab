package com.example.rfidtab.ui.task

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.rfidtab.R
import com.example.rfidtab.adapter.task.TaskOnlineAdapter
import com.example.rfidtab.adapter.task.TaskOnlineListener
import com.example.rfidtab.extension.toast
import com.example.rfidtab.service.Status
import com.example.rfidtab.service.db.entity.task.TaskCardListEntity
import com.example.rfidtab.service.db.entity.task.TaskResultEntity
import com.example.rfidtab.service.db.entity.task.TaskWithCards
import com.example.rfidtab.service.model.TaskStatusEnum
import com.example.rfidtab.service.model.TaskStatusModel
import com.example.rfidtab.service.response.task.TaskResponse
import kotlinx.android.synthetic.main.alert_add.view.*
import kotlinx.android.synthetic.main.alert_scan.view.add_negative_btn
import kotlinx.android.synthetic.main.fragment_online_tasks.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.random.Random

class OnlineTaskFragment : Fragment(), TaskOnlineListener {
    private val viewModel: TaskViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_online_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iniViews()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && view != null) {
            iniViews()
        }
    }

    private fun iniViews() {
        viewModel.taskNetwork(true).observe(viewLifecycleOwner, Observer { result ->
            val data = result.data
            val msg = result.msg
            when (result.status) {
                Status.SUCCESS -> {
                    online_task_rv.adapter =
                        TaskOnlineAdapter(this, data as ArrayList<TaskResponse>)
                }
                Status.ERROR -> {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
                Status.NETWORK -> {
                    Toast.makeText(context, "Проблемы с интернетом", Toast.LENGTH_LONG)
                        .show()
                }
                else -> {
                    Toast.makeText(context, "Произошла ошибка", Toast.LENGTH_LONG).show()
                }
            }

        })
    }

    override fun onItemClicked(model: TaskResponse) {
        val intent = Intent(activity, TaskDetailActivity::class.java)
        intent.putExtra("data", model)
        intent.putExtra("isOnline", true)
        startActivity(intent)
    }


    override fun onItemSaved(model: TaskResponse) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.alert_add, null)
        dialogBuilder.setView(view)
        val alertDialog = dialogBuilder.create()

        view.add_positive_btn.setOnClickListener {
            saveItemToDb(model)
            /*    if (changeTaskStatus(model)) {
                    alertDialog.dismiss()
                }*/

        }

        view.add_negative_btn.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()

    }

    private fun changeTaskStatus(model: TaskResponse): Boolean {
        var isSuccess = false
        viewModel.taskStatusChange(
            TaskStatusModel(
                model.id,
                model.taskTypeId,
                TaskStatusEnum.takenForExecution
            )
        ).observe(viewLifecycleOwner, Observer { result ->
            val data = result.data
            val msg = result.msg
            when (result.status) {
                Status.SUCCESS -> {
                    isSuccess = true
                    toast("Вы взяли задание на исполнение")
                    saveItemToDb(model)
                }
                Status.ERROR -> {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
                Status.NETWORK -> {
                    Toast.makeText(context, "Проблемы с интернетом", Toast.LENGTH_LONG)
                        .show()
                }
                else -> {
                    Toast.makeText(context, "Произошла ошибка", Toast.LENGTH_LONG).show()
                }
            }


        })
        return isSuccess
    }

    private fun saveItemToDb(model: TaskResponse) {
        CoroutineScope(Dispatchers.IO).launch {
            val cards = ArrayList<TaskCardListEntity>()

            model.cardList.forEachIndexed { index, taskCardList ->
                val a = model.cardList[index]
                cards.add(
                    TaskCardListEntity(
                        Random.nextInt(0, 1000),
                        a.cardId,
                        a.fullName,
                        a.pipeSerialNumber,
                        a.serialNoOfNipple,
                        a.couplingSerialNumber,
                        a.rfidTagNo,
                        a.comment,
                        a.accounting,
                        a.commentProblemWithMark,
                        a.taskId,
                        a.taskTypeId
                    )
                )
            }

            val item = TaskWithCards(
                TaskResultEntity(
                    model.id,
                    model.statusId,
                    model.taskTypeId,
                    model.statusTitle,
                    model.taskTypeTitle,
                    model.createdByFio,
                    model.executorFio,
                    model.comment
                ), cards
            )
            viewModel.insertTaskToDb(item)

            withContext(Dispatchers.Main) {
                toast("Успешно сохранён!")
            }
        }
    }
}
