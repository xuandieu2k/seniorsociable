package vn.xdeuhug.seniorsociable.personalPage.ui.activity

import android.annotation.SuppressLint
import android.view.View
import org.jetbrains.anko.startActivity
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Address
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Hobby
import vn.xdeuhug.seniorsociable.personalPage.databinding.ActivityEditInforPersonalBinding
import vn.xdeuhug.seniorsociable.personalPage.ui.adapter.HobbiesAdapter
import vn.xdeuhug.seniorsociable.personalPage.ui.dialog.HobbiesDialog
import vn.xdeuhug.seniorsociable.ui.dialog.PlaceDialog
import vn.xdeuhug.seniorsociable.utils.AppUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 29 / 11 / 2023
 */
class EditInforPersonalActivity : AppActivity() {
    private lateinit var binding: ActivityEditInforPersonalBinding

    private lateinit var hobbiesAdapter: HobbiesAdapter
    private var listHobbies = ArrayList<Hobby>()

    private var addressUpdate = Address() // địa điểm làm việc

    override fun getLayoutView(): View {
        binding = ActivityEditInforPersonalBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        setDataForView()
        setDataHobbies()
        setClickEditHobbies()
        setClickEditWorkAt()
        setClickEditBasicInfor()
        setShowViewHobby()
    }

    override fun onResume() {
        super.onResume()
        setDataForView()
    }

    private fun setDataForView() {
        val user = UserCache.getUser()
        binding.tvFullname.text = user.name
        if (user.birthday.isNotEmpty()) {
            binding.tvBirthday.text = user.birthday
        } else {
            binding.tvBirthday.text = getString(R.string.not_update)
        }
        if (user.address.address.isNotEmpty()) {
            binding.tvFromTo.text = user.address.address
        } else {
            binding.tvFromTo.text = getString(R.string.not_update)
        }
        addressUpdate = user.workAt
        if (user.workAt.address.isNotEmpty()) {
            binding.tvWorkAt.text = user.workAt.address
        } else {
            binding.tvWorkAt.text = getString(R.string.not_update)
        }

        when (user.gender) {
            AppConstants.NOT_UPDATE -> {
                binding.tvGender.text = getString(R.string.not_update)
            }

            AppConstants.MAN -> {
                binding.tvGender.text = getString(R.string.man)
            }

            AppConstants.WOMAN -> {
                binding.tvGender.text = getString(R.string.woman)
            }
        }

        when (user.maritalStatus) {
            AppConstants.MARITAL_STATUS_NOT_UPDATE -> {
                binding.tvMarryStatus.text = getString(R.string.not_update)
            }

            AppConstants.MARITAL_STATUS_SINGLE -> {
                binding.tvMarryStatus.text = getString(R.string.single)
            }

            AppConstants.MARITAL_STATUS_MARRIED -> {
                binding.tvMarryStatus.text = getString(R.string.married)
            }
        }
    }

    private fun setClickEditBasicInfor() {
        binding.btnEditBasic.clickWithDebounce {
            try {
                startActivity<EditInforBasicActivity>()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun setClickEditWorkAt() {
        binding.btnWorkAt.clickWithDebounce {
            PlaceDialog.Builder(getContext(), getString(R.string.work_at_location), addressUpdate)
                .onActionDone(object : PlaceDialog.Builder.OnActionDone {
                    override fun onActionDone(isConfirm: Boolean, address: Address?) {
                        if (isConfirm) {
                            addressUpdate = address!!
                            binding.tvWorkAt.text = address.address.ifEmpty { getString(R.string.not_update) }
                        }
                        showDialog(getString(R.string.is_processing))
                        val userUpdate = UserCache.getUser()
                        userUpdate.workAt = addressUpdate
                        UserManagerFSDB.updateAddressUser(
                            userUpdate,
                            object : UserManagerFSDB.Companion.FireStoreCallback<Unit> {
                                override fun onSuccess(result: Unit) {
                                    hideDialog()
                                    UserCache.saveUser(userUpdate)
                                    updateViewWorkAt()
                                }

                                override fun onFailure(exception: Exception) {
                                    hideDialog()
                                    toast(getString(R.string.please_try_later))
                                }

                            })
                    }

                }).create().show()
        }
    }

    private fun updateViewWorkAt() {
        binding.tvWorkAt.text = UserCache.getUser().workAt.address
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setDataHobbies() {
        val list = AppUtils.getListHobbyIds(
            UserCache.getUser().hobbies, AppUtils.createHobbiesList(getContext(), resources)
        )
        listHobbies.clear()
        listHobbies.addAll(list)
        hobbiesAdapter = HobbiesAdapter(getContext())
        // Create recycleView
        AppUtils.initRecyclerViewVerticalWithFlexBoxLayout(binding.rvHobbies, hobbiesAdapter)
        hobbiesAdapter.setData(listHobbies)
        hobbiesAdapter.notifyDataSetChanged()
    }

    private fun setClickEditHobbies() {
        binding.btnHobbies.clickWithDebounce {
            val list = ArrayList<Hobby>()
            list.addAll(listHobbies)
            val dialog = HobbiesDialog(getContext(), resources, list)
            dialog.onActionDone(object : HobbiesDialog.OnActionDone {
                @SuppressLint("NotifyDataSetChanged")
                override fun onActionDone(isConfirm: Boolean, list: ArrayList<Hobby>) {
                    if (isConfirm) {
                        showDialog(getString(R.string.is_processing))
                        val userUpdate = UserCache.getUser()
                        userUpdate.hobbies = list.map { it.id } as ArrayList<Int>
                        UserManagerFSDB.updateHobbyUser(
                            userUpdate,
                            object : UserManagerFSDB.Companion.FireStoreCallback<Unit> {
                                override fun onSuccess(result: Unit) {
                                    hideDialog()
                                    UserCache.saveUser(userUpdate)
                                    listHobbies.clear()
                                    listHobbies.addAll(list)
                                    hobbiesAdapter.notifyDataSetChanged()
                                    setShowViewHobby()
                                }

                                override fun onFailure(exception: Exception) {
                                    hideDialog()
                                    toast(getString(R.string.please_try_later))
                                }

                            })
                    }
                }

            })
            dialog.show()
        }
    }

    private fun setShowViewHobby() {
        if (listHobbies.isNotEmpty()) {
            binding.tvNoHobby.hide()
            binding.rvHobbies.show()
        } else {
            binding.tvNoHobby.show()
            binding.rvHobbies.hide()
        }
    }

    override fun initData() {
        //
    }
}