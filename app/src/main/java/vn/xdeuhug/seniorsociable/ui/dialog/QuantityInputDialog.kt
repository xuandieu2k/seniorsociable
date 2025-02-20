package vn.xdeuhug.seniorsociable.ui.dialog

import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.databinding.QuantityInputDialogBinding
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.base.action.AnimAction
import vn.xdeuhug.base.action.ToastAction


/**
 * @Author: Nguyen The Dat
 * @Date: 12/04/2023
 */
class QuantityInputDialog {
    class Builder constructor(
        context: Context,
        private var key: Int,
        private var currentNumber: String,
        private var isAllowZero: Boolean,
        private var maxValue: String,
        private var title:String
    ) : BaseDialog.Builder<Builder>(context), OnClickListener, ToastAction {
        private var binding: QuantityInputDialogBinding =
            QuantityInputDialogBinding.inflate(LayoutInflater.from(context))

        lateinit var onActionDone: OnActionDone

        fun setOnActionDone(onActionDone: OnActionDone): Builder = apply {
            this.onActionDone = onActionDone
        }

        init {
            setAnimStyle(AnimAction.ANIM_SCALE)
            setGravity(Gravity.CENTER)
            setContentView(binding.root)
            setWidth(Resources.getSystem().displayMetrics.widthPixels * 9 / 10)
            currentNumber = AppUtils.formatDoubleToString(currentNumber.toDouble())
            binding.tvTitle.text= title
            binding.tvInput.text = AppUtils.getDecimalFormattedString(currentNumber)
            if (key == AppConstants.INTEGER_INPUT_TABLE) { // 1 là bảng nhập số lượng theo số nguyên, -1 là nhập số tờ trong báo cáo ca
                binding.btnKey000.visibility = View.VISIBLE
                binding.btnKeyDot.visibility = View.GONE
            } else if (key == AppConstants.DECIMAL_INPUT_TABLE) { // 3 là bảng nhập số lượng theo số thập phân
                binding.btnKey000.visibility = View.GONE
                binding.btnKeyDot.visibility = View.VISIBLE
            }
            eventClick()
        }

        private fun eventClick() {
            binding.btnKeyDot.setOnClickListener(this)
            binding.btnKey000.setOnClickListener(this)
            binding.btnKey0.setOnClickListener(this)
            binding.btnKey1.setOnClickListener(this)
            binding.btnKey2.setOnClickListener(this)
            binding.btnKey3.setOnClickListener(this)
            binding.btnKey4.setOnClickListener(this)
            binding.btnKey5.setOnClickListener(this)
            binding.btnKey6.setOnClickListener(this)
            binding.btnKey7.setOnClickListener(this)
            binding.btnKey8.setOnClickListener(this)
            binding.btnKey9.setOnClickListener(this)
            binding.btnKeyMinus.setOnClickListener(this)
            binding.btnKeyPlus.setOnClickListener(this)
            binding.btnKeyReset.setOnClickListener(this)
            binding.btnAccept.setOnClickListener(this)
            binding.btnKeyBack.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            super.onClick(view)
            when (view.id) {
                R.id.btnKeyDot -> {
                    if (!currentNumber.contains(".")) {
                        currentNumber = "$currentNumber."
                    }
                    binding.tvInput.text = currentNumber
                }

                R.id.btnKey000 -> {
                    currentNumber = currentNumber.replace(",", "")
                    currentNumber =
                        if (currentNumber.toDouble() > 0 && (currentNumber + "000").toDouble() <= maxValue.toDouble()) {
                            currentNumber + "000"
                        } else {
                            maxValue
                        }
                    formatCurrentNumber()
                }

                R.id.btnKey0 -> {
                    currentNumber = currentNumber.replace(",", "")
                    customCurrentNumber("0")
                    formatCurrentNumber()
                }

                R.id.btnKey1 -> {
                    currentNumber = currentNumber.replace(",", "")
                    customCurrentNumber("1")
                    formatCurrentNumber()
                }

                R.id.btnKey2 -> {
                    currentNumber = currentNumber.replace(",", "")
                    customCurrentNumber("2")
                    formatCurrentNumber()
                }

                R.id.btnKey3 -> {
                    currentNumber = currentNumber.replace(",", "")
                    customCurrentNumber("3")
                    formatCurrentNumber()
                }

                R.id.btnKey4 -> {
                    currentNumber = currentNumber.replace(",", "")
                    customCurrentNumber("4")
                    formatCurrentNumber()
                }

                R.id.btnKey5 -> {
                    currentNumber = currentNumber.replace(",", "")
                    customCurrentNumber("5")
                    formatCurrentNumber()
                }

                R.id.btnKey6 -> {
                    currentNumber = currentNumber.replace(",", "")
                    customCurrentNumber("6")
                    formatCurrentNumber()
                }

                R.id.btnKey7 -> {
                    currentNumber = currentNumber.replace(",", "")
                    customCurrentNumber("7")
                    formatCurrentNumber()
                }

                R.id.btnKey8 -> {
                    currentNumber = currentNumber.replace(",", "")
                    customCurrentNumber("8")
                    formatCurrentNumber()
                }

                R.id.btnKey9 -> {
                    currentNumber = currentNumber.replace(",", "")
                    customCurrentNumber("9")
                    formatCurrentNumber()
                }

                R.id.btnKeyMinus -> {
                    currentNumber = currentNumber.replace(",", "")
                    eventMinus()
                    formatCurrentNumber()
                }

                R.id.btnKeyPlus -> {
                    currentNumber = currentNumber.replace(",", "")
                    eventPlus()
                    formatCurrentNumber()
                }

                R.id.btnKeyReset -> {
                    currentNumber = "0"
                    binding.tvInput.text = currentNumber
                }

                R.id.btnKeyBack -> {
                    currentNumber = currentNumber.replace(",", "")
                    currentNumber = if (currentNumber.length < 2) {
                        "0"
                    } else {
                        currentNumber.substring(0, currentNumber.length - 1)
                    }
                    initNumberToView()
                }

                R.id.btnAccept -> {
                    binding.btnAccept.isEnabled = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.btnAccept.isEnabled = true
                    }, 2000)
                    actionDone()
                }
            }
        }

        private fun customCurrentNumber(keyNumber: String) {
            currentNumber =
                if (key == AppConstants.DECIMAL_INPUT_TABLE) { //Cho trường hợp nhập số thập phân hoặc số tờ ở báo cáo ca
                    if ((currentNumber.toDouble() > 0 || currentNumber == "0." ||  currentNumber == "0.0") && (currentNumber + keyNumber).toDouble() <= maxValue.toDouble()) {
                        currentNumber + keyNumber
                    } else {
                        if (currentNumber.toDouble() == 0.0) keyNumber
                        else {
                            toast(
                                getResources().getString(R.string.max_value_input) + " ${
                                    AppUtils.getDecimalFormattedString(
                                        maxValue
                                    )
                                }"
                            )
                            maxValue
                        }


                    }
                } else {
                    if (currentNumber.toDouble() > 0 && (currentNumber + keyNumber).toDouble() <= maxValue.toDouble()) {
                        currentNumber + keyNumber
                    } else {
                        if (currentNumber.toDouble() == 0.0) keyNumber
                        else {
                            toast(
                                getResources().getString(R.string.max_value_input) + " ${
                                    AppUtils.getDecimalFormattedString(
                                        maxValue
                                    )
                                }"
                            )
                            maxValue
                        }
                    }
                }
        }

        private fun formatCurrentNumber() {
            if (currentNumber.contains(".")) {
                if (!currentNumber.endsWith(".")) {
                    val temp = currentNumber.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                    if (temp[1].length > 2) {
                        currentNumber = currentNumber.substring(0, currentNumber.length - 1)
                    }
                    if (currentNumber.toDouble() > maxValue.toDouble()) {
                        currentNumber = "0"
                    }
                }
            } else {
                if (currentNumber.toDouble() > maxValue.toDouble()) {
                    currentNumber = "0"
                }
            }
            initNumberToView()
        }

        private fun initNumberToView() {
            binding.tvInput.text = AppUtils.getDecimalFormattedString(currentNumber)
        }

        private fun eventMinus() {
            if (key == AppConstants.INTEGER_INPUT_TABLE) {
                currentNumber = if (currentNumber.toDouble() == 0.0) {
                    "0"
                } else {
                    (currentNumber.toInt() - 1).toString()
                }
            } else {
                if (currentNumber.endsWith(".")) {
                    toast(getResources().getString(R.string.error_input_number))
                } else {
                    currentNumber = if (currentNumber.toDouble() < 0.01) {
                        "0"
                    } else {
                        AppUtils.formatDoubleToString(
                            AppUtils.roundOffDecimal(
                                currentNumber.toDouble() - 0.01
                            )
                        )
                    }
                }
            }
        }

        private fun eventPlus() {
            if (key == AppConstants.DECIMAL_INPUT_TABLE) {
                if (currentNumber.toDouble() >= maxValue.toDouble()) {
                    toast(
                        getResources().getString(R.string.max_value_input) + " ${
                            AppUtils.getDecimalFormattedString(
                                maxValue
                            )
                        }"
                    )
                } else {
                    if (currentNumber.endsWith(".")) {
                        toast(getResources().getString(R.string.error_input_number))
                    } else {
                        currentNumber = AppUtils.roundOffDecimal(
                            currentNumber.toDouble() + 1
                        ).toString()
                    }
                }
            } else {
                if (currentNumber.toDouble() >= maxValue.toDouble()) {
                    toast(
                        getResources().getString(R.string.max_value_input) + " ${
                            AppUtils.getDecimalFormattedString(
                                maxValue
                            )
                        }"
                    )
                } else currentNumber = (currentNumber.toInt() + 1).toString()
            }
        }

        private fun actionDone() {
            if (key == AppConstants.INTEGER_INPUT_TABLE) {
                if (checkIntegerInput()) {
                    onActionDone.onActionDone(currentNumber.replace(",", ""))
                    dismiss()
                }
            } else {
                if (checkDecimalInput()) {
                    onActionDone.onActionDone(currentNumber.replace(",", ""))
                    dismiss()
                }
            }
        }

        private fun checkIntegerInput(): Boolean {
            if (!isAllowZero && key == AppConstants.INTEGER_INPUT_TABLE) if (currentNumber.toDouble() < 1L) {
                toast(getResources().getString(R.string.min_integer_input))
                return false
            }


            return if (currentNumber.toDouble() > maxValue.toDouble()) {
                toast(
                    getResources().getString(R.string.max_value_input) + " ${
                        AppUtils.getDecimalFormattedString(
                            maxValue
                        )
                    }"
                )
                false
            } else {
                true
            }
        }

        private fun checkDecimalInput(): Boolean {
            if (!isAllowZero && currentNumber.toDouble() < 0.01) {
                toast(getResources().getString(R.string.min_decimal_input))
                return false

            }

            return if (currentNumber.toDouble() > maxValue.toDouble()) {
                toast(
                    getResources().getString(R.string.max_value_input) + " ${
                        AppUtils.getDecimalFormattedString(
                            maxValue
                        )
                    }"
                )
                false
            } else {
                true
            }
        }

        interface OnActionDone {
            fun onActionDone(result: String)
        }
    }
}