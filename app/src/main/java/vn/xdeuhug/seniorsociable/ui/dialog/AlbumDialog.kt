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
import vn.xdeuhug.seniorsociable.database.AlbumManagerFSDB
import vn.xdeuhug.seniorsociable.databinding.DialogAlbumBinding
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.Album
import vn.xdeuhug.seniorsociable.ui.adapter.AlbumAdapter
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.clickWithDebounce
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 15 / 10 / 2023
 */
class AlbumDialog {
    @SuppressLint("NotifyDataSetChanged")
    class Builder constructor(
        context: Context, var albumSelected: Album?
    ) : BaseDialog.Builder<Builder>(context), AlbumAdapter.OnListenerCLick {
        private var binding: DialogAlbumBinding =
            DialogAlbumBinding.inflate(LayoutInflater.from(context))

        //
        private lateinit var onActionDone: OnActionDone
        private lateinit var albumAdapter: AlbumAdapter
        private var listAlbum = ArrayList<Album>()
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
            albumAdapter = AlbumAdapter(context)
            albumAdapter.onListenerCLick = this
            // Create recycleView
            AppUtils.initRecyclerViewVertical(binding.rvAlbum, albumAdapter)
            albumAdapter.setData(listAlbum)
            startShimmer()
            getDataAlbum()
            binding.tbAlbum.setOnTitleBarListener(object : OnTitleBarListener {
                override fun onLeftClick(view: View?) {
                    onActionDone.onActionDone(false, null)
                    dismiss()
                }

                override fun onTitleClick(view: View?) {
                    //
                }

                override fun onRightClick(view: View?) {
                    //
                }

            })
            setClickButtonCreateAlbum()
        }

        private fun startShimmer() {
            binding.sflLoadData.startShimmer()
            binding.rvAlbum.hide()
            binding.sflLoadData.show()
        }

        private fun getDataAlbum() {
            AlbumManagerFSDB.getAllAlbums(object :
                AlbumManagerFSDB.Companion.FireStoreCallback<ArrayList<Album>> {
                override fun onSuccess(result: ArrayList<Album>) {
                    handleSuccess(0)
                    listAlbum.clear()
                    if(albumSelected != null)
                    {
                        result.forEach {
                            if (it.id == albumSelected!!.id) {
                                it.isChecked = true
                            }
                        }
                    }
                    listAlbum.addAll(result)
                    albumAdapter.setData(listAlbum)
                    albumAdapter.notifyDataSetChanged()
                    if (listAlbum.isEmpty()) {
                        binding.rlBackgroundNotFound.show()
                        binding.rvAlbum.hide()
                    } else {
                        binding.rlBackgroundNotFound.hide()
                        binding.rvAlbum.show()
                    }
                }

                override fun onFailure(exception: Exception) {
                    ToastUtils.showToast(getContext(), getString(R.string.please_try_later))
                }

            })
        }

        private fun setClickButtonCreateAlbum() {
            binding.rlCreateAlbum.clickWithDebounce {
                AddAlbumDialog.Builder(getContext())
                    .onActionDone(object : AddAlbumDialog.Builder.OnActionDone {
                        override fun onActionDone(isConfirm: Boolean) {
                            binding.sflLoadData.startShimmer()
                            getDataAlbum()
                        }

                    }).create().show()
            }
        }

        interface OnActionDone {
            fun onActionDone(isConfirm: Boolean, album: Album?)
        }

        override fun onClick(position: Int) {
            listAlbum[position].isChecked = true
            albumAdapter.notifyItemChanged(position)
            onActionDone.onActionDone(true, listAlbum[position])
            dismiss()
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun handleSuccess(timer: Long) {
            postDelayed({
                binding.rvAlbum.show()
                // hide and stop simmer
                binding.sflLoadData.stopShimmer()
                binding.sflLoadData.hide()
            }, timer)

        }
    }
}