package vn.xdeuhug.seniorsociable.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.Gravity
import android.view.LayoutInflater
import timber.log.Timber
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.base.action.AnimAction
import vn.xdeuhug.seniorsociable.databinding.DialogChooseOptionBottomBinding
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 14 / 11 / 2023
 */
class ChooseOptionBottomDialog {
    @SuppressLint("NotifyDataSetChanged")
    class Builder constructor(
        context: Context, option: Int
    ) : BaseDialog.Builder<Builder>(context) {
        private var option = 2
        private var text1 = ""
        private var text2 = ""
        private var text3 = ""
        private var text4 = ""
        private var text5 = ""

        private var imv1 = vn.xdeuhug.seniorsociable.R.drawable.ic_add_circle
        private var imv2 = vn.xdeuhug.seniorsociable.R.drawable.ic_add_circle
        private var imv3 = vn.xdeuhug.seniorsociable.R.drawable.ic_add_circle
        private var imv4 = vn.xdeuhug.seniorsociable.R.drawable.ic_add_circle
        private var imv5 = vn.xdeuhug.seniorsociable.R.drawable.ic_add_circle
        private var binding: DialogChooseOptionBottomBinding =
            DialogChooseOptionBottomBinding.inflate(LayoutInflater.from(context))

        //
        private lateinit var onActionDone: OnActionDone
        fun onActionDone(onActionDone: OnActionDone): Builder = apply {
            this.onActionDone = onActionDone
        }

        init {
            setCancelable(true)
            setAnimStyle(AnimAction.ANIM_BOTTOM)
            setGravity(Gravity.BOTTOM)
            setContentView(binding.root)
            setWidth(Resources.getSystem().displayMetrics.widthPixels)
            this.option = option
            when (this.option) {
                1 -> {
                    binding.ll1.show()
                    binding.ll2.hide()
                    binding.ll3.hide()
                    binding.ll4.hide()
                    binding.ll5.hide()
                }

                2 -> {
                    binding.ll1.show()
                    binding.ll2.show()
                    binding.ll3.hide()
                    binding.ll4.hide()
                    binding.ll5.hide()
                }

                3 -> {
                    binding.ll1.show()
                    binding.ll2.show()
                    binding.ll3.show()
                    binding.ll4.hide()
                    binding.ll5.hide()
                }

                4 -> {
                    binding.ll1.show()
                    binding.ll2.show()
                    binding.ll3.show()
                    binding.ll4.show()
                    binding.ll5.hide()
                }

                5 -> {
                    binding.ll1.show()
                    binding.ll2.show()
                    binding.ll3.show()
                    binding.ll4.show()
                    binding.ll5.show()
                }
            }
            AppUtils.setChildListener(binding.ll1){
                onActionDone.onClickButton1()
            }
            AppUtils.setChildListener(binding.ll2){
                onActionDone.onClickButton2()
            }
            AppUtils.setChildListener(binding.ll3){
                onActionDone.onClickButton3()
            }
            AppUtils.setChildListener(binding.ll4){
                onActionDone.onClickButton4()
            }
            AppUtils.setChildListener(binding.ll5){
                onActionDone.onClickButton5()
            }
        }

        private fun setContentForView() {
            binding.tv1.text = text1
            binding.tv2.text = text2
            binding.tv3.text = text3
            binding.tv4.text = text4
            binding.tv5.text = text5
            Timber.tag("Log text").i("$text1 $text2 $text3 $text4 $text5")
            binding.imv1.setImageResource(imv1)
            binding.imv2.setImageResource(imv2)
            binding.imv3.setImageResource(imv3)
            binding.imv4.setImageResource(imv4)
            binding.imv5.setImageResource(imv5)
        }

        fun setDataForView(
            text1: String,
            text2: String,
            text3: String,
            text4: String,
            text5: String,
            imv1: Int,
            imv2: Int,
            imv3: Int,
            imv4: Int,
            imv5: Int
        ) {
            this.text1 = text1
            this.text2 = text2
            this.text3 = text3
            this.text4 = text4
            this.text5 = text5
            this.imv1 = imv1
            this.imv2 = imv2
            this.imv3 = imv3
            this.imv4 = imv4
            this.imv5 = imv5
            setContentForView()
        }


        interface OnActionDone {
            fun onActionDone(isConfirm: Boolean)
            fun onClickButton1()
            fun onClickButton2()
            fun onClickButton3()
            fun onClickButton4()
            fun onClickButton5()
        }
    }
}