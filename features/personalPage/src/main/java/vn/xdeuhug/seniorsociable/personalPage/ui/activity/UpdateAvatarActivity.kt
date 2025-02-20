package vn.xdeuhug.seniorsociable.personalPage.ui.activity

import android.view.View
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppActivity
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.database.FireCloudManager
import vn.xdeuhug.seniorsociable.database.PostManagerFSDB
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Post
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.personalPage.constants.PersonalPageConstants
import vn.xdeuhug.seniorsociable.personalPage.databinding.ActivityUpdateAvatarBinding
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils
import java.util.UUID

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 15 / 11 / 2023
 */
class UpdateAvatarActivity : AppActivity() {
    private lateinit var binding: ActivityUpdateAvatarBinding
    private var localMedia = LocalMedia()
    private var multiMedia = MultiMedia()

    override fun getLayoutView(): View {
        binding = ActivityUpdateAvatarBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        setHeightView()
        setDataImage()
        setClickButton()
        setClickPost()
    }

    private fun createPost(): Post {
        val post = Post()
        post.id = UUID.randomUUID().toString()
        val textContent = binding.edtDescription.text.toString().trim()
        if (textContent.isNotEmpty()) {
            post.content = textContent
        } else {
            post.content = ""
        }
        post.idUserPost = UserCache.getUser().id
        post.type = PostConstants.TYPE_PUBLIC
        post.typePost = PostConstants.POST_UPDATE_AVATAR
        post.multiMedia = arrayListOf(multiMedia)
        return post
    }

    private fun postToFireStore(post: Post) {
        PostManagerFSDB.addPost(post, object : PostManagerFSDB.Companion.FireStoreCallback<Boolean> {
            override fun onSuccess(result: Boolean) {
                if (result) {
                    val user = UserCache.getUser()
                    user.avatar = multiMedia.url
                    updateAvatar(user)
                } else {
                    toast(R.string.post_failure)
                    hideDialog()
                }
            }

            override fun onFailure(exception: Exception) {
                toast(R.string.please_try_later)
                hideDialog()
            }
        })
    }

    private fun updateAvatar(user:User)
    {
        UserManagerFSDB.updateAvatarUser(user,object :
            UserManagerFSDB.Companion.FireStoreCallback<Unit>{
            override fun onSuccess(result: Unit) {
                toast(R.string.update_success)
                UserCache.saveUser(user)
                hideDialog()
                finish()
            }

            override fun onFailure(exception: Exception) {
                hideDialog()
            }

        })
    }

    private fun setClickPost() {
        binding.tvSave.clickWithDebounce {
            showDialog(getString(R.string.is_processing))
            if (binding.cbCheckUpToPost.isChecked) {
                val idUser = UserCache.getUser().id
                val storageReference = FirebaseStorage.getInstance().reference
                val storageRef: StorageReference =
                    storageReference.child(UploadFireStorageUtils.getRootURLAvatarById(idUser))
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        FireCloudManager.Companion.uploadFilesToFirebaseStorage(arrayListOf(multiMedia), storageRef, object :
                            FireCloudManager.Companion.FireStoreCallback<ArrayList<MultiMedia>> {
                            override fun onSuccess(result: ArrayList<MultiMedia>) {
                                val post = createPost()
                                postToFireStore(post)
                            }

                            override fun onFailure(result: ArrayList<MultiMedia>) {
                                toast(R.string.please_try_later)
                                hideDialog()
                            }
                        })
                    } catch (e: Exception) {
                        toast(R.string.please_try_later)
                        hideDialog()
                    }
                }
            } else {
                val idUser = UserCache.getUser().id
                val storageReference = FirebaseStorage.getInstance().reference
                val storageRef: StorageReference =
                    storageReference.child(UploadFireStorageUtils.getRootURLAvatarById(idUser))
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        FireCloudManager.Companion.uploadFilesToFirebaseStorage(arrayListOf(multiMedia), storageRef, object :
                            FireCloudManager.Companion.FireStoreCallback<ArrayList<MultiMedia>> {
                            override fun onSuccess(result: ArrayList<MultiMedia>) {
                                val user = UserCache.getUser()
                                user.avatar = multiMedia.url
                                updateAvatar(user)
                            }

                            override fun onFailure(result: ArrayList<MultiMedia>) {
                                toast(R.string.please_try_later)
                                hideDialog()
                            }
                        })
                    } catch (e: Exception) {
                        toast(R.string.please_try_later)
                        hideDialog()
                    }
                }
            }
        }
    }

    private fun setClickButton() {
        binding.btnBack.clickWithDebounce {
            finish()
        }
    }

    private fun setDataImage() {
        localMedia = Gson().fromJson(
            intent.getStringExtra(PersonalPageConstants.LOCAL_MEDIA), LocalMedia::class.java
        )
        PhotoShowUtils.loadAvatarImage(
            "", localMedia.realPath, binding.imvAvatarCircle
        )

        PhotoShowUtils.loadAvatarImageCenterCrop(
            localMedia.realPath, binding.imvAvatarBg
        )
        setMultiMedia()
    }

    private fun setMultiMedia() {
        multiMedia = MultiMedia()
        val mediaId = UUID.randomUUID().toString()
        multiMedia.id = mediaId
        multiMedia.name = "Media $mediaId"
        multiMedia.url = localMedia.realPath
        multiMedia.realPath = localMedia.realPath
        multiMedia.size = localMedia.size
        multiMedia.height = localMedia.height
        multiMedia.width = localMedia.width
        if (PictureMimeType.isHasVideo(localMedia.mimeType)) {
            multiMedia.type = AppConstants.UPLOAD_VIDEO
        }
        if (PictureMimeType.isHasImage(localMedia.mimeType)) {
            multiMedia.type = AppConstants.UPLOAD_IMAGE
        }

    }

    private fun setHeightView() {
        //
    }

    override fun initData() {
        //
    }
}