package vn.xdeuhug.seniorsociable.personalPage.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import com.hjq.bar.OnTitleBarListener
import vn.xdeuhug.seniorsociable.databinding.DialogHobbiesBinding
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Hobby
import vn.xdeuhug.seniorsociable.other.doAfterTextChanged
import vn.xdeuhug.seniorsociable.other.doOnQueryTextListener
import vn.xdeuhug.seniorsociable.ui.adapter.HobbiesAdapter
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.VNCharacterUtils
import java.util.Locale

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 30 / 11 / 2023
 */
class HobbiesDialog(
    context: Context, private var resources: Resources, var listHobbiesSelected: ArrayList<Hobby>
) : Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen),
    HobbiesAdapter.OnListenerSelected {

    private var binding: DialogHobbiesBinding =
        DialogHobbiesBinding.inflate(LayoutInflater.from(context))

    //
    private lateinit var onActionDone: OnActionDone
    private lateinit var hobbiesAdapter: HobbiesAdapter
    private var listHobbies = ArrayList<Hobby>()
    private var listHobbiesRoot = ArrayList<Hobby>()

    fun onActionDone(onActionDone: OnActionDone) {
        this.onActionDone = onActionDone
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        this.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT
        )
        setContentView(binding.root)
        //
        hobbiesAdapter = HobbiesAdapter(context)
        hobbiesAdapter.onListenerSelected = this
        // Create recycleView
        AppUtils.initRecyclerViewVertical(binding.rvHobbies, hobbiesAdapter)
        hobbiesAdapter.setData(listHobbies)
        startShimmer()
        getDataHobbies()
        setSearch()
        binding.tbTitle.setOnTitleBarListener(object : OnTitleBarListener {
            override fun onLeftClick(view: View?) {
                onActionDone.onActionDone(false,ArrayList())
                dismiss()
            }

            override fun onTitleClick(view: View?) {
                //
            }

            override fun onRightClick(view: View?) {
                val list = ArrayList<Hobby>()
                list.addAll(listHobbiesSelected)
                onActionDone.onActionDone(true,list)
                dismiss()
            }

        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setSearch() {
        binding.svSearch.doAfterTextChanged(1000) {
            listHobbies.clear()
            if (it.isNotEmpty()) {
                val list = listHobbiesRoot.filter { item ->
                    VNCharacterUtils.removeAccent(item.name.lowercase(Locale.getDefault()))
                        .contains(
                            VNCharacterUtils.removeAccent(
                                it.lowercase(
                                    Locale.getDefault()
                                )
                            )
                        )
                }
                listHobbies.addAll(list)
                //
                if (listHobbies.isNotEmpty()) {
                    hobbiesAdapter.notifyDataSetChanged()
                    binding.rvHobbies.show()
                    binding.rlBackgroundNotFound.hide()
                } else {
                    binding.rvHobbies.hide()
                    binding.rlBackgroundNotFound.show()
                }
            } else {
                //
                listHobbies.addAll(listHobbiesRoot)
                hobbiesAdapter.notifyDataSetChanged()
                binding.rvHobbies.show()
                binding.rlBackgroundNotFound.hide()
            }
        }
    }

    private fun startShimmer() {
        binding.sflLoadData.startShimmer()
        binding.rvHobbies.hide()
        binding.sflLoadData.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getDataHobbies() {
        listHobbies.clear()
        listHobbiesRoot.clear()
        listHobbies.addAll(AppUtils.createHobbiesList(context, resources))
        listHobbiesRoot.addAll(listHobbies)
        if (listHobbiesSelected.isNotEmpty()) {
            val listIds = listHobbiesSelected.map { it.id }
            listHobbies.forEach {
                if (it.id in listIds) {
                    it.isChecked = true
                }
            }
        }
        hobbiesAdapter.setData(listHobbies)
        handleSuccess()
        hobbiesAdapter.notifyDataSetChanged()
        if (listHobbies.isEmpty()) {
            binding.rlBackgroundNotFound.show()
            binding.rvHobbies.hide()
        } else {
            binding.rlBackgroundNotFound.hide()
            binding.rvHobbies.show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleSuccess() {
        binding.rvHobbies.show()
        // hide and stop simmer
        binding.sflLoadData.stopShimmer()
        binding.sflLoadData.hide()

    }


    interface OnActionDone {
        fun onActionDone(isConfirm: Boolean, list: ArrayList<Hobby>)
    }

    override fun onSelected(position: Int) {
        if (listHobbies[position].isChecked) {
            var itemRemove = Hobby()
            listHobbiesSelected.forEach {
                if (it.id == listHobbies[position].id) {
                    itemRemove = it
                }
            }
            listHobbiesSelected.remove(itemRemove)
            listHobbies[position].isChecked = false
        } else {
            //
            listHobbiesSelected.add(listHobbies[position])
            listHobbies[position].isChecked = true
        }
        hobbiesAdapter.notifyItemChanged(position)
    }
}