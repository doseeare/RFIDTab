package com.example.rfidtab.ui.task.kitorder

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.example.rfidtab.R
import com.example.rfidtab.adapter.kitorder.card.KitCardOnlineAdapter
import com.example.rfidtab.adapter.kitorder.card.KitCardSavedAdapter
import com.example.rfidtab.adapter.kitorder.card.KitCardSavedListener
import com.example.rfidtab.adapter.kitorder.kitdetail.KitDetailAddCardAdapter
import com.example.rfidtab.extension.toast
import com.example.rfidtab.service.db.entity.kitorder.KitOrderAddCardEntity
import com.example.rfidtab.service.db.entity.kitorder.KitOrderCardEntity
import com.example.rfidtab.service.db.entity.kitorder.KitOrderKitEntity
import com.example.rfidtab.service.response.kitorder.KitOrderCard
import com.example.rfidtab.service.response.kitorder.KitOrderKit
import com.example.rfidtab.util.MyUtil
import com.example.rfidtab.util.scanrfid.RfidScannerListener
import com.example.rfidtab.util.scanrfid.RfidScannerUtil
import kotlinx.android.synthetic.main.activity_kit_detail_order.*
import kotlinx.android.synthetic.main.alert_scan.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class KitOrderDetailActivity : AppCompatActivity(), KitCardSavedListener, RfidScannerListener {
    private val kitOrderViewModel: KitOrderViewModel by viewModel()
    private lateinit var scanDialog: AlertDialog
    private lateinit var currentModel: KitOrderCardEntity

    private lateinit var onlineData: KitOrderKit
    private lateinit var savedData: KitOrderKitEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kit_detail_order)
        supportActionBar?.title = "Комплектация в аренду"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initViews()
    }

    private fun initViews() {
        val isOnline = intent.getBooleanExtra("isOnline", true)

        if (isOnline) {
            onlineData = intent.getSerializableExtra("data") as KitOrderKit
            kit_order_add_btn.isVisible = false
            if (onlineData.cards.isNotEmpty()) {
                kit_order_spec_card.visibility = View.GONE
                kit_order_detail_rv.adapter =
                    KitCardOnlineAdapter(onlineData.cards as ArrayList<KitOrderCard>)

                kit_order_count_card.isVisible = false

            } else {
                kit_order_count_card.isVisible = false
                kit_order_rv_card.visibility = View.GONE
                kit_order_spec_angle_plait.text =
                    "Угол заплётчика: ${onlineData.specification.shoulderAngle}"
                kit_order_spec_long_couple.text =
                    "Длина под ключ муфта: ${onlineData.specification.turnkeyLengthCoupling}"
                kit_order_spec_long_nipple.text =
                    "Длина под ключ ниппель: ${onlineData.specification.turnkeyLengthNipple}"
                kit_order_spec_long_pipe.text =
                    "Длина трубы: ${onlineData.specification.pipeLength}"
                kit_order_spec_od_nipple.text =
                    "O.D замка ниппеля: ${onlineData.specification.odlockNipple}"
                kit_order_spec_diameter_pipe.text =
                    "Наружный диаметр трубы: ${onlineData.specification.outerDiameterOfThePipe}"
                kit_order_spec_pipe_wall.text =
                    "Толщина стенки трубы: ${onlineData.specification.pipeWallThickness}"
                kit_order_spec_id_nipple.text =
                    "I.D замка ниппель: ${onlineData.specification.idlockNipple}"

                kit_order_spec_comment.text = "Комментарий постановщика задачи: ${onlineData.specification.comment}"
            }

        } else {
            savedData = intent.getSerializableExtra("data") as KitOrderKitEntity

            kitOrderViewModel.findKitCards(savedData.id).observe(this, Observer {
                kit_order_detail_rv.adapter = KitCardSavedAdapter(this, it as ArrayList<KitOrderCardEntity>)

                if (it.isEmpty()) {
                    kitOrderViewModel.findKitOrderSpecByKitId(savedData.id).observe(this, Observer { spec ->

                        kit_order_spec_angle_plait.text = "Угол заплётчика: ${spec.shoulderAngle}"
                        kit_order_spec_long_couple.text =
                            "Длина под ключ муфта: ${spec.turnkeyLengthCoupling}"
                        kit_order_spec_long_nipple.text =
                            "Длина под ключ ниппель: ${spec.turnkeyLengthNipple}"
                        kit_order_spec_long_pipe.text = "Длина трубы: ${spec.pipeLength}"
                        kit_order_spec_od_nipple.text = "O.D замка ниппеля: ${spec.odlockNipple}"
                        kit_order_spec_diameter_pipe.text =
                            "Наружный диаметр трубы: ${spec.outerDiameterOfThePipe}"
                        kit_order_spec_pipe_wall.text =
                            "Толщина стенки трубы: ${spec.pipeWallThickness}"
                        kit_order_spec_id_nipple.text = "I.aD замка ниппель: ${spec.idlockNipple}"

                       kit_order_detail_title.text = "Количество единиц оборудования: ${spec.cardCount}"

                        kit_order_spec_comment.text = "Комментарий постановщика задачи: ${spec.comment}"


                        kitOrderViewModel.findAddCardByKitId(savedData.id).observe(this, Observer {
                            kit_order_added_rv.adapter =
                                KitDetailAddCardAdapter(it as ArrayList<KitOrderAddCardEntity>)
                        })
                        addCardsToKit(savedData.id)
                    })
                }else{
                    kit_order_detail_title.text = "Количество единиц оборудования: ${it.size}"
                    kit_order_add_btn.isVisible = false
                    kit_order_spec_card.isVisible = false
                }

            })

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun scanBtnClicked(model: KitOrderCardEntity) {
        currentModel = model
        createScanDialog(model, "")
    }

    private fun createScanDialog(model: KitOrderCardEntity, accessTag: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.alert_scan, null)
        dialogBuilder.setView(view)
        scanDialog = dialogBuilder.create()

        view.scan_name.text = model.fullName

        view.scan_problem_checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
            when (isChecked) {
                true -> view.scan_comment_out.visibility = View.VISIBLE
                false -> view.scan_comment_out.visibility = View.GONE
            }
        }
        view.scan_result_et.setText(accessTag)
        //Сохранение карточки
        view.scan_access_btn.setOnClickListener {

                if (accessTag.isNotEmpty()) {
                    if (MyUtil().equalsNoSpace(model.rfidTagNo!!, accessTag)) {
                        kitOrderViewModel.kitOrderCardConfirm(model.id, true)
                        toast("Подтверждён")
                        scanDialog.dismiss()
                    } else {
                        kitOrderViewModel.kitOrderCardConfirm(model.id, false)
                        toast("Не совпадает!")
                        scanDialog.dismiss()
                    }

                }else{
                    if (view.scan_comment_et.text.toString().isNotEmpty()) {
                        kitOrderViewModel.updateProblemCommentKitCard(model.id, view.scan_comment_et.text.toString())
                        toast("Успешно сохранён!")
                        scanDialog.dismiss()
                    } else {
                        view.scan_comment_et.error = "Не может быть пустым"
                    }
                }

        }

        //Чтение со сканера
        view.scan_scanner.setOnClickListener {
            RfidScannerUtil(this).run {
                isCancelable = false
                show(supportFragmentManager, "RfidScannerUtil")
            }
        }

        view.scan_negative_btn.setOnClickListener {
            scanDialog.dismiss()
        }

        scanDialog.show()
    }

    override fun onAccessScan(tag: String) {
        scanDialog.dismiss()
        createScanDialog(currentModel, tag)
    }

    private fun addCardsToKit(kitId: Int) {
        kit_order_add_btn.setOnClickListener {
            KitOrderAddCardBS(kitId).run {
                show(supportFragmentManager, "KitOrderAddCardBS")
            }
        }
    }

}