package vn.xdeuhug.seniorsociable.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.hjq.bar.OnTitleBarListener
import com.luck.picture.lib.utils.ToastUtils
import vn.xdeuhug.base.BaseDialog
import vn.xdeuhug.base.action.AnimAction
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.database.FriendManagerFSDB
import vn.xdeuhug.seniorsociable.databinding.DialogTagUserBinding
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.ui.adapter.TagUserAdapter
import vn.xdeuhug.seniorsociable.ui.adapter.TagUserSelectedAdapter
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 12 / 10 / 2023
 */
class TagUserDialog {
    @SuppressLint("NotifyDataSetChanged")
    class Builder constructor(
        context: Context, var listUserSelected: ArrayList<User>
    ) : BaseDialog.Builder<Builder>(context), TagUserAdapter.OnListenerSelected,
        TagUserSelectedAdapter.OnListenerRemove {
        private var binding: DialogTagUserBinding =
            DialogTagUserBinding.inflate(LayoutInflater.from(context))

        // Adapter
        private var tagUserSelectedAdapter: TagUserSelectedAdapter
        private var tagUserAdapter: TagUserAdapter
        private var listTagUserSelected = ArrayList<User>()
        private var listTagUser = ArrayList<User>()

        //
        lateinit var onActionDone: OnActionDone

        fun onActionDone(onActionDone: OnActionDone): Builder = apply {
            this.onActionDone = onActionDone
        }

        init {
            startShimmer()
            setCancelable(false)
            setAnimStyle(AnimAction.ANIM_SCALE)
            setGravity(Gravity.CENTER)
            setContentView(binding.root)
            getDialog()?.window?.setBackgroundDrawableResource(R.drawable.bg_border_dialog)
            setWidth(Resources.getSystem().displayMetrics.widthPixels)
            setHeight(Resources.getSystem().displayMetrics.heightPixels)
            // set Adapter
            tagUserAdapter = TagUserAdapter(context)
            tagUserSelectedAdapter = TagUserSelectedAdapter(context)
            tagUserAdapter.onListenerSelected = this
            tagUserSelectedAdapter.onListenerRemove = this
            // Create recycleView
            AppUtils.initRecyclerViewVertical(binding.rvUserTag, tagUserAdapter)
            tagUserAdapter.setData(listTagUser)
            // Create recycleView
            AppUtils.initRecyclerViewHorizontal(binding.rvUserTagSelected, tagUserSelectedAdapter)
            tagUserSelectedAdapter.setData(listTagUserSelected)
            getDataFriend()
            binding.tbUserTag.setOnTitleBarListener(object : OnTitleBarListener {
                override fun onLeftClick(view: View?) {
                    onActionDone.onActionDone(true, listTagUserSelected)
                    dismiss()
                }

                override fun onTitleClick(view: View?) {
                    //
                }

                override fun onRightClick(view: View?) {
                    //
                }

            })

        }

        private fun getDataFriend() {
            FriendManagerFSDB.getAllFriends(object :
                FriendManagerFSDB.Companion.FireStoreCallback<ArrayList<User>> {
                override fun onSuccess(result: ArrayList<User>) {
                    handleSuccess(0)
                    listTagUser.clear()
                    listTagUserSelected.clear()
                    val listIds = listUserSelected.map { it.id }
                    if(listIds.isNotEmpty())
                    {
                        result.forEach {
                            if(it.id in listIds)
                            {
                                it.isChecked = true
                                listTagUserSelected.add(it)
                            }
                        }
                    }
                    listTagUser.addAll(result)
                    tagUserAdapter.setData(listTagUser)
                    tagUserAdapter.notifyDataSetChanged()
                    tagUserSelectedAdapter.notifyDataSetChanged()
                    if (listTagUser.isEmpty()) {
                        binding.rlBackgroundNotFound.show()
                        binding.rvUserTag.hide()
                    } else {
                        binding.rlBackgroundNotFound.hide()
                        binding.rvUserTag.show()
                    }
                }

                override fun onFailure(exception: Exception) {
                    ToastUtils.showToast(getContext(), getString(R.string.please_try_later))
                }

            })
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun handleSuccess(timer: Long) {
            postDelayed({
                // hide and stop simmer
                binding.sflLoadData.stopShimmer()
                binding.sflLoadData.hide()
                binding.rvUserTag.show()
            }, timer)

        }

        private fun startShimmer() {
            binding.sflLoadData.startShimmer()
            binding.rvUserTag.hide()
            binding.sflLoadData.show()
        }


        interface OnActionDone {
            fun onActionDone(isConfirm: Boolean, listData: ArrayList<User>)
        }

        // Adapter  TagUserAdapter
        override fun onSelected(position: Int) {
            //
            if (listTagUser[position].isChecked) {
                listTagUserSelected.remove(listTagUser[position])
                listTagUser[position].isChecked = false
            } else {
                //
                listTagUserSelected.add(listTagUser[position])
                listTagUser[position].isChecked = true
            }
            tagUserAdapter.notifyItemChanged(position)
            tagUserSelectedAdapter.notifyDataSetChanged()
            checkExistList()
        }

        // Adapter  TagUserSelectedAdapter
        override fun onRemove(position: Int) {
            loop@ for ((index, user) in listTagUser.withIndex()) {
                if (user.id == listTagUserSelected[position].id) {
                    user.isChecked = false
                    tagUserAdapter.notifyItemChanged(index)
                    listTagUserSelected.removeAt(position)
                    tagUserSelectedAdapter.notifyItemRemoved(position)

                    break@loop // Sử dụng break@loop để thoát khỏi vòng lặp
                }
            }
            checkExistList()
        }

        private fun checkExistList()
        {
            if(listTagUserSelected.isNotEmpty())
            {
                binding.tvIsChoose.show()
            }else{
                binding.tvIsChoose.hide()
            }
        }
    }
}