package vn.xdeuhug.seniorsociable.admin.ui.activity

import android.annotation.SuppressLint
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.NestedScrollView
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.admin.databinding.ActivityManagementAccountBinding
import vn.xdeuhug.seniorsociable.admin.ui.adapter.AccountAdapter
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.ui.dialog.AccountDialog
import vn.xdeuhug.seniorsociable.utils.AppUtils
import java.util.Date

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 29 / 12 / 2023
 */
class ManagementAccountActivity : AppActivity(), AdapterView.OnItemSelectedListener,
    AccountAdapter.OnListenerCLick, AccountAdapter.OnClickImageViewListener {
    private lateinit var binding: ActivityManagementAccountBinding

    /* Data and Adapter for Post */
    private lateinit var accountAdapter: AccountAdapter
    private var listUser = ArrayList<User>()

    /* paging*/
    private var limit = AppConstants.PAGE_SIZE
    private var currentPage = 1
    private var lastVisible: DocumentSnapshot? = null
    private var loading = false
    private var lastPage = false

    /* end */
    override fun getLayoutView(): View {
        binding = ActivityManagementAccountBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        AppUtils.setFontTypeFaceTitleBar(getContext(), binding.tbTitle)
        startShimmer()
        initStatusFilter()
        setDataPost()
        postDelayed({
            getDataUser()
        }, 1000)
        customPaginate()
    }

    override fun initData() {
        //
    }

    /*
   * Init Data For Post
   * */
    @SuppressLint("NotifyDataSetChanged")
    private fun setDataPost() {
        accountAdapter = AccountAdapter(this)
        accountAdapter.onListenerCLick = this
        accountAdapter.imageViewClickListener = this
        accountAdapter.setData(listUser)
        // Create recycleView
        binding.rvAccount.show()
        AppUtils.initRecyclerViewVertical(binding.rvAccount, accountAdapter)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getDataUser() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val listTriple = async {
                    UserManagerFSDB.getAllUserWithStatus(
                        getStatusFilter(),
                        limit,
                        lastVisible
                    )
                }.await()
                if (currentPage == 1) {
                    handleSuccess(0)
                    listUser.clear()
                    binding.splReset.finishRefresh()
                }
                // UI update code here
                listUser.addAll(listTriple.first)
                accountAdapter.notifyDataSetChanged()
                lastVisible = listTriple.second
                lastPage = listTriple.third
                loading = false
                binding.loadMore.hide()
                showViewNoData()
            } catch (e: Exception) {
                // Xử lý ngoại lệ khi gặp lỗi
                e.printStackTrace()
                toast(R.string.please_try_later)
                loading = false
                binding.loadMore.hide()
                binding.splReset.finishRefresh()
                showViewNoData()
            }
        }
    }

    private fun customPaginate() {
        binding.splReset.setOnRefreshListener {
            postDelayed({
                currentPage = 1
                lastPage = false
                lastVisible = null
                getDataUser()
            }, 1000)
        }
        binding.nsvData.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            var recyclerViewBottom = binding.rvAccount.bottom
            if (binding.loadMore.visibility == View.VISIBLE) {
                recyclerViewBottom = binding.rvAccount.bottom + binding.loadMore.height
            }
            val nestedScrollViewHeight = binding.nsvData.height

            if (binding.rvAccount.visibility == View.VISIBLE && binding.sflLoadData.visibility == View.GONE) {
                if (scrollY + nestedScrollViewHeight >= recyclerViewBottom) {
                    if (!loading && !lastPage) {
                        binding.loadMore.show()
                        binding.nsvData.post {
                            binding.nsvData.smoothScrollTo(
                                0,
                                binding.rvAccount.bottom + binding.loadMore.height
                            )
                        }
                        loading = true
                        currentPage += 1
                        getDataUser()
                    }
                }
            }
        })
    }

    private fun initStatusFilter() {
        val arrayText = arrayListOf(
            getString(R.string.all),
            getString(R.string.is_active),
            getString(R.string.temp_lock),
            getString(R.string.is_lock_un_limited)
        )

        val adapterFilter =
            ArrayAdapter(this, R.layout.simple_spinner_item_custom, arrayText)
        adapterFilter.setDropDownViewResource(R.layout.simple_list_item_activated_custom)
        binding.spnFilter.adapter = adapterFilter
        binding.spnFilter.onItemSelectedListener = this
        binding.spnFilter.setSelection(0)
        binding.imvDrownList.clickWithDebounce {
            binding.spnFilter.performClick()
        }
    }

    private fun getStatusFilter(): Int {
        return binding.spnFilter.selectedItemPosition
    }

    private fun resetDataPost() {
        currentPage = 1
        lastVisible = null
        startShimmer()
        postDelayed({
            getDataUser()
        }, 1000)
    }

    private fun startShimmer() {
        binding.sflLoadData.startShimmer()
        binding.sflLoadData.show()
        binding.rvAccount.hide()
        binding.rlBackgroundNotFound.hide()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun handleSuccess(timer: Long) {
        postDelayed({
            binding.rvAccount.show()
            binding.sflLoadData.stopShimmer()
            binding.sflLoadData.hide()
        }, timer)

    }

    private fun showViewNoData() {
        if (listUser.isNotEmpty()) {
            binding.rlBackgroundNotFound.hide()
            binding.rvAccount.show()
        } else {
            binding.rlBackgroundNotFound.show()
            binding.rvAccount.hide()
        }
    }

    private var countSetSpinnerData = 0
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (countSetSpinnerData > 0) {
            resetDataPost()
        }
        countSetSpinnerData++
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //
    }

    override fun onClick(position: Int) {
        //
    }

    override fun onClickButtonRemove(position: Int, user: User) {
        //
    }

    override fun onClickButtonEdit(position: Int, user: User) {
        val typeDialog = when (user.typeActive) {
            AppConstants.ACTIVATING -> {
                AccountDialog.Builder.TYPE_LOCK
            }

            else -> {
                AccountDialog.Builder.TYPE_OPEN
            }
        }
        val dialog = AccountDialog.Builder(getContext(), user, typeDialog).onActionDone(object :
            AccountDialog.Builder.OnActionDone {

            override fun onActionDone(
                isBlock: Boolean,
                reasonBlock: String,
                typeActive: Int,
                timeStart: Date?,
                timeEnd: Date?,
                userNew: User
            ) {
                if (isBlock) // khóa tài khoản
                {
                    UserManagerFSDB.updateBlockUser(
                        userNew.id,
                        timeStart,
                        timeEnd,
                        typeActive,
                        reasonBlock,
                        object :
                            UserManagerFSDB.Companion.FireStoreCallback<Boolean> {
                            override fun onSuccess(result: Boolean) {
                                listUser[position] = userNew
                                accountAdapter.notifyItemChanged(position)
                                toast(R.string.lock_account_success)
                            }

                            override fun onFailure(exception: Exception) {
                                exception.printStackTrace()
                                toast(R.string.please_try_later)
                            }

                        })
                } else { // mở tài khaoanr
                    UserManagerFSDB.updateOpenUser(user.id, object :
                        UserManagerFSDB.Companion.FireStoreCallback<Boolean> {
                        override fun onSuccess(result: Boolean) {
                            listUser[position] = userNew
                            accountAdapter.notifyItemChanged(position)
                            toast(R.string.open_account_success)
                        }

                        override fun onFailure(exception: Exception) {
                            exception.printStackTrace()
                            toast(R.string.please_try_later)
                        }

                    })
                }
            }

        })
        dialog.create().show()
    }

}