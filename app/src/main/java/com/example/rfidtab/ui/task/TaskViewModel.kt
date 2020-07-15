package com.example.rfidtab.ui.task

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.rfidtab.base.BaseViewModel
import com.example.rfidtab.service.Resource
import com.example.rfidtab.service.db.entity.task.TaskCardListEntity
import com.example.rfidtab.service.db.entity.task.TaskResultEntity
import com.example.rfidtab.service.db.entity.task.TaskWithCards
import com.example.rfidtab.service.model.CardModel
import com.example.rfidtab.service.model.TaskStatusModel
import com.example.rfidtab.service.response.task.TaskCardResponse
import com.example.rfidtab.service.response.task.TaskResponse

class TaskViewModel(application: Application) : BaseViewModel(application) {

    fun taskNetwork(withCards: Boolean): LiveData<Resource<List<TaskResponse>>> {
        return network.task(withCards)
    }

    fun changeCard(model: CardModel): LiveData<Resource<TaskCardResponse>> {
        return network.cardChange(model)
    }

    fun taskStatusChange(model: TaskStatusModel): LiveData<Resource<String>> {
        return network.taskStatusChange(model)
    }

    fun insertTaskToDb(item: TaskWithCards) {
        db.insertTaskToDB(item)
    }

    fun findAllTasks(): LiveData<List<TaskResultEntity>> {
        return db.findAllTasks()
    }

    fun deleteTaskById(id: Int) {
        return db.deleteTaskById(id)
    }

    fun deleteCardsById(id: Int) {
        return db.deleteCardsById(id)
    }

    fun findCardsById(id: Int): LiveData<List<TaskCardListEntity>> {
        return db.findCardsById(id)
    }

    fun updateCard(cardId: Int, rfidTag: String) {
        return db.updateCard(cardId, rfidTag)
    }
}