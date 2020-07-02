package com.example.rfidtab.ui.profile

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.rfidtab.R
import com.example.rfidtab.extension.toast
import com.example.rfidtab.service.AppPreferences
import com.example.rfidtab.service.Status
import com.example.rfidtab.ui.auth.AuthActivity
import kotlinx.android.synthetic.main.activity_profile.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileActivity : AppCompatActivity() {
    private val viewModel: ProfileViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        initViews()
    }

    private fun initViews() {
        profile_exit_btn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Вы хотите выйти?")
            AppPreferences.clear()
            startActivity(Intent(this, AuthActivity::class.java))
            builder.setPositiveButton("Да, выйти") { dialog, which ->
            }

            builder.setNegativeButton("Нет") { dialog, which ->
                dialog.dismiss()
            }
            builder.show()

        }
        viewModel.userInfoModel(0).observe(this, Observer { result ->
            val data = result.data
            val msg = result.msg
            when (result.status) {
                Status.SUCCESS -> {
                    task_name.text = "Пользователь: ${data!!.result.fio} "
                    task_phone.text = "Номер тел: : ${data!!.result.phone} "
                    task_email.text = "Эл почта: ${data!!.result.email} "
                    task_role.text = "Обязанность: ${data!!.result.roleTitle} "
                }
                Status.ERROR -> {
                    toast(msg)
                }
                Status.NETWORK -> {
                    toast("Проблемы с интернетом")

                }
                else -> {
                    toast("Произшла ошибка")
                }
            }


        })
    }
}