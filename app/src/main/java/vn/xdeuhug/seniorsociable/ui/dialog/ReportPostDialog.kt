package vn.xdeuhug.seniorsociable.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.get
import timber.log.Timber
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.base.action.AnimAction
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.database.ReportManagerFSDB
import vn.xdeuhug.seniorsociable.databinding.DialogReportPostBinding
import vn.xdeuhug.seniorsociable.model.entity.modelReport.Reason
import vn.xdeuhug.seniorsociable.model.entity.modelReport.Report
import vn.xdeuhug.seniorsociable.utils.AppUtils.clickWithDebounce
import java.util.Date
import java.util.UUID

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 19 / 12 / 2023
 */
class ReportPostDialog {
    @SuppressLint("NotifyDataSetChanged")
    class Builder constructor(
        context: Context, var idPost: String
    ) : BaseDialog.Builder<Builder>(context), AdapterView.OnItemSelectedListener {
        private var binding: DialogReportPostBinding =
            DialogReportPostBinding.inflate(LayoutInflater.from(context))

        private var isShow = false

        //
        private var listReason = ArrayList<Reason>()

        //
        lateinit var onActionDone: OnActionDone

        fun onActionDone(onActionDone: OnActionDone): Builder = apply {
            this.onActionDone = onActionDone
        }

        init {
            setCancelable(true)
            setAnimStyle(AnimAction.ANIM_IOS)
            setGravity(Gravity.CENTER)
            setContentView(binding.root)
            getDialog()?.window?.setBackgroundDrawableResource(R.drawable.bg_border_dialog_blue_new)
            setWidth(Resources.getSystem().displayMetrics.widthPixels * 8 / 10)
            setDataAdapter()
            binding.spnReason.onItemSelectedListener = this
            binding.btnSend.clickWithDebounce {
                dismiss()
                onActionDone.onActionDone(true)
            }
            binding.btnSend.clickWithDebounce {
                val rp = Report(
                    UUID.randomUUID().toString(),
                    UserCache.getUser().id,
                    binding.spnReason.selectedItem as Reason,
                    Date(),
                    idPost,
                    binding.edtReason.text.toString().trim()
                )
                ReportManagerFSDB.addReport(
                    rp,
                    object : ReportManagerFSDB.Companion.FireStoreCallback<Boolean> {
                        override fun onSuccess(result: Boolean) {
                            onActionDone.onActionDone(result)
                        }

                        override fun onFailure(exception: Exception) {
                            //
                            Timber.tag("Log ex").e(exception)
                        }

                    })
            }

        }

        private fun setDataAdapter() {
            val itemDefault = Reason(0, getString(R.string.choose_reason)!!, true)
            listReason.add(itemDefault)
            val list = arrayListOf(
                Reason(1, getString(R.string.content_not_true)!!, false),
                Reason(2, getString(R.string.raw_language)!!, false),
                Reason(3, getString(R.string.not_true_fact)!!, false),
                Reason(4, getString(R.string.different_reason)!!, false)
            )
            listReason.addAll(list)
            val adapterTopicFeedback = ArrayAdapter(
                getContext(), R.layout.simple_spinner_item_custom, listReason
            )
            adapterTopicFeedback.setDropDownViewResource(R.layout.simple_list_item_activated_custom)
            binding.spnReason.adapter = adapterTopicFeedback
            binding.spnReason.setSelection(0)
            binding.imvDrownList.clickWithDebounce {
                binding.spnReason.performClick()
            }
        }


        interface OnActionDone {
            fun onActionDone(isConfirm: Boolean)
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val reason = binding.spnReason.selectedItem as Reason
            binding.btnSend.isEnabled = !reason.isHint
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            //
        }
    }
}