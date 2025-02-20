package vn.xdeuhug.seniorsociable.personalPage.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.hjq.bar.OnTitleBarListener
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.base.action.AnimAction
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.databinding.DialogHobbiesBinding
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Hobby
import vn.xdeuhug.seniorsociable.ui.adapter.HobbiesAdapter
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 30 / 11 / 2023
 */
class HobbyDialog {
    @SuppressLint("NotifyDataSetChanged")
    class Builder constructor(
        context: Context, var listHobbiesSelected: ArrayList<Hobby>
    ) : BaseDialog.Builder<Builder>(context), HobbiesAdapter.OnListenerSelected {
        private var binding: DialogHobbiesBinding =
            DialogHobbiesBinding.inflate(LayoutInflater.from(context))

        //
        private lateinit var onActionDone: OnActionDone
        private var hobbiesAdapter: HobbiesAdapter
        private var listHobbies = ArrayList<Hobby>()
        fun onActionDone(onActionDone: OnActionDone): Builder = apply {
            this.onActionDone = onActionDone
        }

        init {
            setCancelable(false)
            setAnimStyle(AnimAction.ANIM_SCALE)
            setGravity(Gravity.CENTER)
            setContentView(binding.root)
            getDialog()?.window?.setBackgroundDrawableResource(R.drawable.bg_border_dialog)
            setWidth(Resources.getSystem().displayMetrics.widthPixels)
            setHeight(Resources.getSystem().displayMetrics.heightPixels)
            //
            AppUtils.setFontTypeFaceTitleBar(getContext(),binding.tbTitle)
            hobbiesAdapter = HobbiesAdapter(context)
            hobbiesAdapter.onListenerSelected = this
            // Create recycleView
            AppUtils.initRecyclerViewVertical(binding.rvHobbies, hobbiesAdapter)
            hobbiesAdapter.setData(listHobbies)
            startShimmer()
            getDataHobbies()
            binding.tbTitle.setOnTitleBarListener(object : OnTitleBarListener {
                override fun onLeftClick(view: View?) {
                    onActionDone.onActionDone(false, ArrayList())
                    dismiss()
                }

                override fun onTitleClick(view: View?) {
                    //
                }

                override fun onRightClick(view: View?) {
                    //
                }

            })
//            setClickButtonCreateAlbum()
        }

        private fun startShimmer() {
            binding.sflLoadData.startShimmer()
            binding.rvHobbies.hide()
            binding.sflLoadData.show()
        }

        private fun getDataHobbies()
        {
            listHobbies.clear()
            listHobbies.addAll(AppUtils.getListHobby(getContext()))
//            if (listHobbiesSelected.isNotEmpty()) {
//                val listIds = listHobbiesSelected.map { it.id }
//                listHobbies.forEach {
//                    if (it.id in listIds) {
//                        it.isChecked = true
//                    }
//                }
//            }
            hobbiesAdapter.setData(listHobbies)
            handleSuccess(0)
            hobbiesAdapter.notifyDataSetChanged()
            if (listHobbies.isEmpty()) {
                binding.rlBackgroundNotFound.show()
                binding.rvHobbies.hide()
            } else {
                binding.rlBackgroundNotFound.hide()
                binding.rvHobbies.show()
            }
        }

        interface OnActionDone {
            fun onActionDone(isConfirm: Boolean, list: ArrayList<Hobby>)
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun handleSuccess(timer: Long) {
            postDelayed({
                binding.rvHobbies.show()
                // hide and stop simmer
                binding.sflLoadData.stopShimmer()
                binding.sflLoadData.hide()
            }, timer)

        }

        override fun onSelected(position: Int) {
//            listHobbies[position].isChecked = true
//            hobbiesAdapter.notifyItemChanged(position)
//            onActionDone.onActionDone(true, listHobbies)
//            dismiss()
        }
    }
}