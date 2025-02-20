package vn.xdeuhug.seniorsociable.personalPage.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.gson.Gson
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.entity.LocalMedia
import org.jetbrains.anko.startActivity
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.UploadFireStorageConstants
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.personalPage.constants.PersonalPageConstants
import vn.xdeuhug.seniorsociable.personalPage.databinding.ActivityEditProfileBinding
import vn.xdeuhug.seniorsociable.personalPage.ui.dialog.EditDescriptionDialog
import vn.xdeuhug.seniorsociable.ui.dialog.PreviewMediaDialog
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.PhotoPickerUtils
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.TimeUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 14 / 11 / 2023
 */
class EditProfileActivity : AppActivity() {
    private lateinit var binding: ActivityEditProfileBinding

    //
    private lateinit var launcher: ActivityResultLauncher<Intent>

    //
    var typeNextView = PersonalPageConstants.TYPE_AVATAR // Type chuyển màn
    private var localMedia = ArrayList<LocalMedia>()
    override fun getLayoutView(): View {
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    var dialogEditDescription: EditDescriptionDialog.Builder? = null

    override fun initView() {
        AppUtils.setFontTypeFaceTitleBar(this, binding.tbEditInformation)
        setDataForView()
        setClickView()
        setViewDescription()
        setViewAvatar()
        setViewBackground()
        setLauncher()
        setClickImage()
    }

    override fun onResume() {
        super.onResume()
        setDataForView()
        setViewDescription()
        setViewAvatar()
        setViewBackground()
    }

    private fun setClickImage() {
        binding.btnAvatar.clickWithDebounce {
            typeNextView = PersonalPageConstants.TYPE_AVATAR
            PhotoPickerUtils.showImagePickerChooseAvatarNotGif(this, launcher)
        }

        binding.btnBackgound.clickWithDebounce {
            typeNextView = PersonalPageConstants.TYPE_BACKGROUND
            PhotoPickerUtils.showImagePickerChooseAvatarNotGif(this, launcher)
        }
        binding.imvAvatar.clickWithDebounce {
            val multiMedia = MultiMedia()
            multiMedia.id = UserCache.getUser().id
            multiMedia.url = UserCache.getUser().avatar
            PreviewMediaDialog.Builder(
                getContext(),
                multiMedia,
                arrayListOf(multiMedia),
                UploadFireStorageConstants.TYPE_AVATAR,
                "",
                null
            ).onActionDone(object : PreviewMediaDialog.Builder.OnActionDone {
                override fun onActionDone(isConfirm: Boolean) {
                    //
                }
            }).create().show()
        }
        binding.imvBackground.clickWithDebounce {
            val multiMedia = MultiMedia()
            multiMedia.id = UserCache.getUser().id
            multiMedia.url = UserCache.getUser().background
            PreviewMediaDialog.Builder(
                getContext(),
                multiMedia,
                arrayListOf(multiMedia),
                UploadFireStorageConstants.TYPE_AVATAR,
                "",
                null
            ).onActionDone(object : PreviewMediaDialog.Builder.OnActionDone {
                override fun onActionDone(isConfirm: Boolean) {
                    //
                }
            }).create().show()
        }
    }

    private fun setLauncher() {
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    localMedia = PictureSelector.obtainSelectorList(data) as ArrayList<LocalMedia>
                    try {
                        when (typeNextView) {
                            PersonalPageConstants.TYPE_AVATAR -> {
                                startActivity<UpdateAvatarActivity>(
                                    PersonalPageConstants.LOCAL_MEDIA to Gson().toJson(
                                        localMedia[0]
                                    )
                                )
                            }

                            PersonalPageConstants.TYPE_BACKGROUND -> {
                                startActivity<UpdateBackgroundActivity>(
                                    PersonalPageConstants.LOCAL_MEDIA to Gson().toJson(
                                        localMedia[0]
                                    )
                                )
                            }
                        }
                    } catch (ex: Exception) {
                        // Lỗi
                    }
                } else {
                    //
                }
            }
    }

    private fun setClickView() {
        binding.btnDescription.clickWithDebounce {
            dialogEditDescription =
                EditDescriptionDialog.Builder(getContext(), UserCache.getUser().description)
                    .onActionDone(object : EditDescriptionDialog.Builder.OnActionDone {
                        override fun onActionDone(isConfirm: Boolean, description: String) {
                            if (isConfirm) {
                                showDialog(getString(R.string.is_processing))
                                val user = UserCache.getUser()
                                user.description = description
                                UserManagerFSDB.updateDescriptionUser(user,
                                    object : UserManagerFSDB.Companion.FireStoreCallback<Unit> {
                                        override fun onSuccess(result: Unit) {
                                            UserCache.saveUser(user)
                                            hideDialog()
                                            dialogEditDescription!!.dismiss()
                                            setViewDescription()
                                        }

                                        override fun onFailure(exception: Exception) {
                                            toast(R.string.please_try_later)
                                            hideDialog()
                                        }

                                    })
                            }
                        }

                    })
            dialogEditDescription!!.create().show()
        }
        binding.edtDescription.clickWithDebounce {
            dialogEditDescription =
                EditDescriptionDialog.Builder(getContext(), UserCache.getUser().description)
                    .onActionDone(object : EditDescriptionDialog.Builder.OnActionDone {
                        override fun onActionDone(isConfirm: Boolean, description: String) {
                            if (isConfirm) {
                                showDialog()
                                val user = UserCache.getUser()
                                user.description = description
                                UserManagerFSDB.updateDescriptionUser(user,
                                    object : UserManagerFSDB.Companion.FireStoreCallback<Unit> {
                                        override fun onSuccess(result: Unit) {
                                            UserCache.saveUser(user)
                                            hideDialog()
                                            dialogEditDescription!!.dismiss()
                                        }

                                        override fun onFailure(exception: Exception) {
                                            toast(R.string.please_try_later)
                                            hideDialog()
                                        }

                                    })
                            }
                        }

                    })
            dialogEditDescription!!.create().show()
        }

        binding.btnInformation.clickWithDebounce {
            try {
                startActivity<EditInforPersonalActivity>()
            } catch (ex: Exception)
            {
                //
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDataForView() {
        val user = UserCache.getUser()
        PhotoShowUtils.loadAvatarImage(
            UploadFireStorageUtils.getRootURLAvatarById(user.id), user.avatar, binding.imvAvatar
        )

        PhotoShowUtils.loadBackgroundImageCenterCrop(
            UploadFireStorageUtils.getRootURLBackgroundById(user.id),
            user.background,
            binding.imvBackground
        )
        if (user.birthday.isNotEmpty()) {
            binding.tvBirthday.text = "${getString(R.string.birthday)} ${user.birthday}"
        } else {
            binding.tvBirthday.text = getString(R.string.not_update)
        }
        if (user.address.address.isNotEmpty()) {
            binding.tvFromTo.text = "${getString(R.string.from_to)} ${user.address.address}"
        } else {
            binding.tvFromTo.text = getString(R.string.not_update)
        }
        binding.tvJoinIn.text = TimeUtils.getJoinIn(user.timeCreated, this)
        binding.edtDescription.setText(user.description)
    }

    private fun setViewDescription() {
        val description = UserCache.getUser().description.trim()
        if (description.isNotEmpty()) {
            binding.edtDescription.setText(description)
            binding.btnDescription.text = getString(R.string.edit)
        } else {
            binding.edtDescription.setText(description)
            binding.btnDescription.text = getString(R.string.add)
        }
    }
    private fun setViewAvatar() {
        val avatar = UserCache.getUser().avatar.trim()
        if (avatar.isNotEmpty()) {
            binding.btnAvatar.text = getString(R.string.edit)
        } else {
            binding.btnAvatar.text = getString(R.string.add)
        }
    }
    private fun setViewBackground() {
        val background = UserCache.getUser().background.trim()
        if (background.isNotEmpty()) {
            binding.btnBackgound.text = getString(R.string.edit)
        } else {
            binding.btnBackgound.text = getString(R.string.add)
        }
    }

    override fun initData() {
        //
    }
}