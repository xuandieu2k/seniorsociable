package vn.xdeuhug.seniorsociable.post.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.adapters.AdapterViewBindingAdapter
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.burhanrashid52.photoediting.StickerBSFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.utils.ToastUtils
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import ja.burhanrashid52.photoeditor.PhotoFilter
import ja.burhanrashid52.photoeditor.SaveFileResult
import ja.burhanrashid52.photoeditor.SaveSettings
import ja.burhanrashid52.photoeditor.TextStyleBuilder
import ja.burhanrashid52.photoeditor.ViewType
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder
import ja.burhanrashid52.photoeditor.shape.ShapeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.anko.support.v4.startActivity
import pyxis.uzuki.live.richutilskt.utils.getSizeByMb
import pyxis.uzuki.live.richutilskt.utils.toast
import timber.log.Timber
import vn.xdeuhug.base.action.HandlerAction
import vn.xdeuhug.seniorsociable.BuildConfig
import vn.xdeuhug.seniorsociable.cache.UserCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.database.FireCloudManager
import vn.xdeuhug.seniorsociable.database.FriendManagerFSDB
import vn.xdeuhug.seniorsociable.database.StoryManagerFSDB
import vn.xdeuhug.seniorsociable.database.UserManagerFSDB
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelPost.Story
import vn.xdeuhug.seniorsociable.model.entity.modelUser.Hobby
import vn.xdeuhug.seniorsociable.post.R
import vn.xdeuhug.seniorsociable.post.base.BaseActivity
import vn.xdeuhug.seniorsociable.post.filters.FilterListener
import vn.xdeuhug.seniorsociable.post.filters.FilterViewAdapter
import vn.xdeuhug.seniorsociable.post.tools.EditingToolsAdapter
import vn.xdeuhug.seniorsociable.post.tools.ToolType
import vn.xdeuhug.seniorsociable.post.ui.fragment.supportStoryFragment.EmojiBSFragment
import vn.xdeuhug.seniorsociable.post.ui.fragment.supportStoryFragment.PropertiesBSFragment
import vn.xdeuhug.seniorsociable.post.ui.fragment.supportStoryFragment.ShapeBSFragment
import vn.xdeuhug.seniorsociable.post.ui.fragment.supportStoryFragment.TextEditorDialogFragment
import vn.xdeuhug.seniorsociable.post.utils.FileSaveHelper
import vn.xdeuhug.seniorsociable.ui.dialog.ExitDialog
import vn.xdeuhug.seniorsociable.ui.dialog.WarningDialog
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.PhotoPickerUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils
import vn.xdeuhug.seniorsociable.widget.AppTextView
import java.io.File
import java.io.IOException
import java.util.Date
import java.util.UUID


/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 03 / 12 / 2023
 */
class CreateStoryActivity : BaseActivity(), OnPhotoEditorListener, View.OnClickListener,
    PropertiesBSFragment.Properties, ShapeBSFragment.Properties, EmojiBSFragment.EmojiListener,
    StickerBSFragment.StickerListener, AdapterViewBindingAdapter.OnItemSelected, FilterListener {

    lateinit var mPhotoEditor: PhotoEditor
    private lateinit var mPhotoEditorView: PhotoEditorView
    private lateinit var mPropertiesBSFragment: PropertiesBSFragment
    private lateinit var mShapeBSFragment: ShapeBSFragment
    private lateinit var mShapeBuilder: ShapeBuilder
    private lateinit var mEmojiBSFragment: EmojiBSFragment
    private lateinit var mStickerBSFragment: StickerBSFragment
    private lateinit var mTxtCurrentTool: AppTextView
    private lateinit var mWonderFont: Typeface
    private lateinit var mRvTools: RecyclerView
    private lateinit var mRvFilters: RecyclerView
    private lateinit var mEditingToolsAdapter: EditingToolsAdapter
    private val mFilterViewAdapter = FilterViewAdapter(this)
    private lateinit var mRootView: ConstraintLayout
    private val mConstraintSet = ConstraintSet()
    private var mIsFilterVisible = false
    //
    private var media = MultiMedia()

    //
    private lateinit var launcher: ActivityResultLauncher<Intent>

    @VisibleForTesting
    var mSaveImageUri: Uri? = null

    private lateinit var mSaveFileHelper: FileSaveHelper

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLauncher()
        mEditingToolsAdapter = EditingToolsAdapter(baseContext, this)
        makeFullScreen()
        setContentView(R.layout.activity_create_story)

        initViews()

        handleIntentImage(mPhotoEditorView.source)
        val typeFace: Typeface? = ResourcesCompat.getFont(
            this.applicationContext, vn.xdeuhug.seniorsociable.R.font.roboto_medium
        )
        mWonderFont = typeFace!!

        mPropertiesBSFragment = PropertiesBSFragment()
        mEmojiBSFragment = EmojiBSFragment()
        mStickerBSFragment = StickerBSFragment()
        mShapeBSFragment = ShapeBSFragment()
        mStickerBSFragment.setStickerListener(this)
        mEmojiBSFragment.setEmojiListener(this)
        mPropertiesBSFragment.setPropertiesChangeListener(this)
        mShapeBSFragment.setPropertiesChangeListener(this)

        val llmTools = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRvTools.layoutManager = llmTools
        mRvTools.adapter = mEditingToolsAdapter

        val llmFilters = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRvFilters.layoutManager = llmFilters
        mRvFilters.adapter = mFilterViewAdapter

        // NOTE(lucianocheng): Used to set integration testing parameters to PhotoEditor
        val pinchTextScalable = intent.getBooleanExtra(PINCH_TEXT_SCALABLE_INTENT_KEY, true)

        //Typeface mTextRobotoTf = ResourcesCompat.getFont(this, R.font.roboto_medium);
        //Typeface mEmojiTypeFace = Typeface.createFromAsset(getAssets(), "emojione-android.ttf");

        mPhotoEditor = PhotoEditor.Builder(this, mPhotoEditorView)
            .setPinchTextScalable(pinchTextScalable) // set flag to make text scalable when pinch
            //.setDefaultTextTypeface(mTextRobotoTf)
            //.setDefaultEmojiTypeface(mEmojiTypeFace)
            .build() // build photo editor sdk

        mPhotoEditor.setOnPhotoEditorListener(this)

        media = Gson().fromJson(
            intent.getStringExtra(AppConstants.MULTIMEDIA_OBJECT), MultiMedia::class.java
        )
        // set ảnh cần chỉnh sửa
        //Set Image Dynamically
        val bitmap = MediaStore.Images.Media.getBitmap(
            this.contentResolver, Uri.fromFile(File(media.realPath))
        )
        mPhotoEditorView.source.setImageBitmap(bitmap)

        mSaveFileHelper = FileSaveHelper(this)
    }

    @Suppress("DEPRECATION")
    private fun setLauncher() {
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    val localMedia = PictureSelector.obtainSelectorList(data)
                    val mediaId = UUID.randomUUID().toString()
                    media.id = mediaId
                    media.name = "Media $mediaId"
                    media.url = localMedia[0].realPath
                    media.realPath = localMedia[0].realPath
                    media.size = localMedia[0].size
                    media.height = localMedia[0].height
                    media.width = localMedia[0].width
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        this.contentResolver, Uri.fromFile(File(localMedia.first().realPath))
                    )
                    mPhotoEditorView.source.setImageBitmap(bitmap)
                } else {
                    //
                }
            }
    }

    private fun handleIntentImage(source: ImageView) {
        if (intent == null) {
            return
        }

        when (intent.action) {
            Intent.ACTION_EDIT, ACTION_NEXTGEN_EDIT -> {
                try {
                    val uri = intent.data
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    source.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            else -> {
                val intentType = intent.type
                if (intentType != null && intentType.startsWith("image/")) {
                    val imageUri = intent.data
                    if (imageUri != null) {
                        source.setImageURI(imageUri)
                    }
                }
            }
        }
    }

    private fun initViews() {
        mPhotoEditorView = findViewById(R.id.photoEditorView)
        mTxtCurrentTool = findViewById(R.id.txtCurrentTool)
        mRvTools = findViewById(R.id.rvConstraintTools)
        mRvFilters = findViewById(R.id.rvFilterView)
        mRootView = findViewById(R.id.rootView)

        val imgUndo: ImageView = findViewById(R.id.imgUndo)
        imgUndo.setOnClickListener(this)

        val imgRedo: ImageView = findViewById(R.id.imgRedo)
        imgRedo.setOnClickListener(this)

        val imgSend: ImageView = findViewById(R.id.imgSend)
        imgSend.setOnClickListener(this)

        val imgGallery: ImageView = findViewById(R.id.imgGallery)
        imgGallery.setOnClickListener(this)

        val imgSave: ImageView = findViewById(R.id.imgSave)
        imgSave.setOnClickListener(this)

        val imgClose: ImageView = findViewById(R.id.imgClose)
        imgClose.setOnClickListener(this)

        val imgShare: ImageView = findViewById(R.id.imgShare)
        imgShare.setOnClickListener(this)
    }

    override fun onEditTextChangeListener(rootView: View, text: String, colorCode: Int) {
        val textEditorDialogFragment =
            TextEditorDialogFragment.show(this, text.toString(), colorCode)
        textEditorDialogFragment.setOnTextEditorListener(object :
            TextEditorDialogFragment.TextEditorListener {
            override fun onDone(inputText: String, colorCode: Int) {
                val styleBuilder = TextStyleBuilder()
                styleBuilder.withTextColor(colorCode)
                if (rootView != null) {
                    mPhotoEditor.editText(rootView, inputText, styleBuilder)
                }
                mTxtCurrentTool.setText(vn.xdeuhug.seniorsociable.R.string.label_text)
            }
        })
    }

    override fun onAddViewListener(viewType: ViewType, numberOfAddedViews: Int) {
        Timber.tag(TAG)
            .d("onAddViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + numberOfAddedViews + "]")
    }

    override fun onRemoveViewListener(viewType: ViewType, numberOfAddedViews: Int) {
        Timber.tag(TAG)
            .d("onRemoveViewListener() called with: viewType = [" + viewType + "], numberOfAddedViews = [" + numberOfAddedViews + "]")
    }

    override fun onStartViewChangeListener(viewType: ViewType) {
        Timber.tag(TAG).d("onStartViewChangeListener() called with: viewType = [" + viewType + "]")
    }

    override fun onStopViewChangeListener(viewType: ViewType) {
        Timber.tag(TAG).d("onStopViewChangeListener() called with: viewType = [" + viewType + "]")
    }

    override fun onTouchSourceImage(event: MotionEvent) {
        Timber.tag(TAG).d("onTouchView() called with: event = [" + event + "]")
    }

    @SuppressLint("NonConstantResourceId", "MissingPermission")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.imgUndo -> mPhotoEditor.undo()
            R.id.imgRedo -> mPhotoEditor.redo()
            R.id.imgSave -> saveImage()
            R.id.imgClose -> onBackPressed()
            R.id.imgShare -> shareImage()
            R.id.imgSend -> {
                saveAndUploadImage()
            }

            R.id.imgGallery -> {
                PhotoPickerUtils.showImagePickerUploadPosterInEvent(this, launcher, ArrayList())
            }
        }
    }

    private fun shareImage() {
        val saveImageUri = mSaveImageUri
        if (saveImageUri == null) {
            showSnackbar(getString(vn.xdeuhug.seniorsociable.R.string.msg_save_image_to_share))
            return
        }

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, buildFileProviderUri(saveImageUri))
        startActivity(
            Intent.createChooser(
                intent, getString(vn.xdeuhug.seniorsociable.R.string.msg_share_image)
            )
        )
    }

    private fun buildFileProviderUri(uri: Uri): Uri {
        if (FileSaveHelper.isSdkHigherThan28()) {
            return uri
        }
        val path: String = uri.path ?: throw IllegalArgumentException("URI Path Expected")

        return FileProvider.getUriForFile(
            this, FILE_PROVIDER_AUTHORITY, File(path)
        )
    }

    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    private fun saveAndUploadImage() {
        val fileName = "PIC_${System.currentTimeMillis()}.png"
        if (mPhotoEditor.isCacheEmpty) { // Nếu null thì tạo bình thường ngực lại lưu hình mưới rồi tạo
            showLoading(getString(vn.xdeuhug.seniorsociable.R.string.is_creating_story))
            // Khởi tạo data
            val file = File(media.url)
            val fileUri = Uri.fromFile(File(media.url))
            val multiMedia = MultiMedia(
                UUID.randomUUID().toString(),
                Date().time,
                file.path,
                fileName,
                AppConstants.UPLOAD_IMAGE,
                file.length() / 1024,
                media.width,
                media.height,
                0,
                file.path
            )
            val idStory = UUID.randomUUID().toString()
            val storageReference = FirebaseStorage.getInstance().reference
            val storageRef: StorageReference = storageReference.child(
                UploadFireStorageUtils.getRootURLStoryById(idStory)
            )
            FireCloudManager.uploadFileToFirebaseStorageNotAsync(fileUri!!,multiMedia,
                storageRef,
                object :
                    FireCloudManager.Companion.FireStoreCallback<MultiMedia> {
                    override fun onSuccess(result: MultiMedia) {
                        val story =
                            Story(idStory, UserCache.getUser().id, Date(), arrayListOf(result))
                        StoryManagerFSDB.addStory(story,object :
                            StoryManagerFSDB.Companion.FireStoreCallback<Boolean>{
                            override fun onSuccess(result: Boolean) {
                                if(result)
                                {
                                    hideLoading()
                                    toast(getString(vn.xdeuhug.seniorsociable.R.string.your_story_is_posted))
                                    finish()
                                }else{
                                    hideLoading()
                                    showSnackbar(getString(vn.xdeuhug.seniorsociable.R.string.please_try_later))
                                }
                            }

                            override fun onFailure(exception: Exception) {
                                hideLoading()
                                exception.printStackTrace()
                                showSnackbar(getString(vn.xdeuhug.seniorsociable.R.string.please_try_later)+"add Story")
                            }

                        })
                    }

                    override fun onFailure(result: MultiMedia) {
                        hideLoading()
                        showSnackbar(getString(vn.xdeuhug.seniorsociable.R.string.please_try_later)+"add Image")
                    }

                })
        }else{
            val hasStoragePermission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            if (hasStoragePermission || FileSaveHelper.isSdkHigherThan28()) {
                showLoading(getString(vn.xdeuhug.seniorsociable.R.string.is_saving))
                mSaveFileHelper.createFile(fileName, object : FileSaveHelper.OnFileCreateResult {

                    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
                    override fun onFileCreateResult(
                        created: Boolean, filePath: String?, error: String?, uri: Uri?
                    ) {
                        lifecycleScope.launch {
                            if (created && filePath != null) {
                                val saveSettings = SaveSettings.Builder().setClearViewsEnabled(true)
                                    .setTransparencyEnabled(true).build()

                                val result = mPhotoEditor.saveAsFile(filePath, saveSettings)

                                if (result is SaveFileResult.Success) {
                                    // Chuẩn bị uplaood hình lên server
                                    mSaveFileHelper.notifyThatFileIsNowPubliclyAvailable(contentResolver)
                                    hideLoading()
                                    showLoading(getString(vn.xdeuhug.seniorsociable.R.string.is_creating_story))
                                    // Khởi tạo data
                                    val file = File(filePath)
                                    val multiMedia = MultiMedia(
                                        UUID.randomUUID().toString(),
                                        Date().time,
                                        filePath,
                                        fileName,
                                        AppConstants.UPLOAD_IMAGE,
                                        file.length() / 1024,
                                        media.width,
                                        media.height,
                                        0,
                                        filePath
                                    )
                                    val idStory = UUID.randomUUID().toString()
                                    val storageReference = FirebaseStorage.getInstance().reference
                                    val storageRef: StorageReference = storageReference.child(
                                        UploadFireStorageUtils.getRootURLStoryById(idStory)
                                    )
                                    FireCloudManager.uploadFileToFirebaseStorageNotAsync(uri!!,multiMedia,
                                        storageRef,
                                        object :
                                            FireCloudManager.Companion.FireStoreCallback<MultiMedia> {
                                            override fun onSuccess(result: MultiMedia) {
                                                val story =
                                                    Story(idStory, UserCache.getUser().id, Date(), arrayListOf(result))
                                                StoryManagerFSDB.addStory(story,object :
                                                    StoryManagerFSDB.Companion.FireStoreCallback<Boolean>{
                                                    override fun onSuccess(result: Boolean) {
                                                        if(result)
                                                        {
//                                                        showSnackbar(getString(vn.xdeuhug.seniorsociable.R.string.your_story_is_posted))
                                                            hideLoading()
                                                            toast(getString(vn.xdeuhug.seniorsociable.R.string.your_story_is_posted))
                                                            finish()
                                                        }else{
                                                            hideLoading()
                                                            showSnackbar(getString(vn.xdeuhug.seniorsociable.R.string.please_try_later))
                                                        }
                                                    }

                                                    override fun onFailure(exception: Exception) {
                                                        hideLoading()
                                                        exception.printStackTrace()
                                                        showSnackbar(getString(vn.xdeuhug.seniorsociable.R.string.please_try_later)+"add Story")
                                                    }

                                                })
                                            }

                                            override fun onFailure(result: MultiMedia) {
                                                hideLoading()
                                                showSnackbar(getString(vn.xdeuhug.seniorsociable.R.string.please_try_later)+"add Image")
                                            }

                                        })
                                } else {
                                    hideLoading()
                                    showSnackbar(getString(vn.xdeuhug.seniorsociable.R.string.image_save_Failed))
                                }
                            } else {
                                hideLoading()
                                error?.let { showSnackbar(error) }
                            }
                        }
                    }
                })
            } else {
                requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }


    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    private fun saveImage() {
        val fileName = "PIC_${System.currentTimeMillis()}.png"
        val hasStoragePermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (hasStoragePermission || FileSaveHelper.isSdkHigherThan28()) {
            showLoading(getString(vn.xdeuhug.seniorsociable.R.string.is_saving))
            mSaveFileHelper.createFile(fileName, object : FileSaveHelper.OnFileCreateResult {

                @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
                override fun onFileCreateResult(
                    created: Boolean, filePath: String?, error: String?, uri: Uri?
                ) {
                    lifecycleScope.launch {
                        if (created && filePath != null) {
                            val saveSettings = SaveSettings.Builder().setClearViewsEnabled(true)
                                .setTransparencyEnabled(true).build()

                            val result = mPhotoEditor.saveAsFile(filePath, saveSettings)

                            if (result is SaveFileResult.Success) {
                                mSaveFileHelper.notifyThatFileIsNowPubliclyAvailable(contentResolver)
                                hideLoading()
                                showSnackbar(getString(vn.xdeuhug.seniorsociable.R.string.image_save_successfully))
                                mSaveImageUri = uri
                                mPhotoEditorView.source.setImageURI(mSaveImageUri)
                            } else {
                                hideLoading()
                                showSnackbar(getString(vn.xdeuhug.seniorsociable.R.string.image_save_Failed))
                            }
                        } else {
                            hideLoading()
                            error?.let { showSnackbar(error) }
                        }
                    }
                }
            })
        } else {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    // TODO(lucianocheng): Replace onActivityResult with Result API from Google
    //                     See https://developer.android.com/training/basics/intents/result
//    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == RESULT_OK) {
//            when (requestCode) {
//                CAMERA_REQUEST -> {
//                    mPhotoEditor.clearAllViews()
//                    val photo = data?.extras?.get("data") as Bitmap?
//                    mPhotoEditorView.source.setImageBitmap(photo)
//                }
//
//                PICK_REQUEST -> try {
//                    mPhotoEditor.clearAllViews()
//                    val uri = data?.data
//                    val bitmap = MediaStore.Images.Media.getBitmap(
//                        contentResolver, uri
//                    )
//                    mPhotoEditorView.source.setImageBitmap(bitmap)
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            }
//        }
//    }

    override fun onColorChanged(colorCode: Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeColor(colorCode))
        mTxtCurrentTool.setText(vn.xdeuhug.seniorsociable.R.string.label_brush)
    }

    override fun onOpacityChanged(opacity: Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeOpacity(opacity))
        mTxtCurrentTool.setText(vn.xdeuhug.seniorsociable.R.string.label_brush)
    }

    override fun onShapeSizeChanged(shapeSize: Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeSize(shapeSize.toFloat()))
        mTxtCurrentTool.setText(vn.xdeuhug.seniorsociable.R.string.label_brush)
    }

    override fun onShapePicked(shapeType: ShapeType) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeType(shapeType))
    }

    override fun onEmojiClick(emojiUnicode: String) {
        mPhotoEditor.addEmoji(emojiUnicode)
        mTxtCurrentTool.setText(vn.xdeuhug.seniorsociable.R.string.label_emoji)
    }

    override fun onStickerClick(bitmap: Bitmap) {
        mPhotoEditor.addImage(bitmap)
        mTxtCurrentTool.setText(vn.xdeuhug.seniorsociable.R.string.label_sticker)
    }

    @SuppressLint("MissingPermission")
    override fun isPermissionGranted(isGranted: Boolean, permission: String?) {
        if (isGranted) {
            saveImage()
        }
    }

    @SuppressLint("MissingPermission")
    private fun showSaveDialog() {
        val dialog = ExitDialog(this, getString(vn.xdeuhug.seniorsociable.R.string.msg_save_image))
        dialog.onActionDone(object : ExitDialog.OnActionDone {
            override fun onActionDone(isConfirm: Int) {
                when (isConfirm) {
                    AppConstants.BUTTON_CONFIRM -> {
                        saveImage()
                    }

                    AppConstants.BUTTON_CANCEL -> {
                        //
                    }

                    AppConstants.BUTTON_EXIT -> {
                        finish()
                    }
                }
            }


        })
        dialog.show()
    }

    override fun onFilterSelected(photoFilter: PhotoFilter) {
        mPhotoEditor.setFilterEffect(photoFilter)
    }

    fun onToolSelected(toolType: ToolType) {
        when (toolType) {
            ToolType.SHAPE -> {
                mPhotoEditor.setBrushDrawingMode(true)
                mShapeBuilder = ShapeBuilder()
                mPhotoEditor.setShape(mShapeBuilder)
                mTxtCurrentTool.setText(vn.xdeuhug.seniorsociable.R.string.label_shape)
                showBottomSheetDialogFragment(mShapeBSFragment)
            }

            ToolType.TEXT -> {
                val textEditorDialogFragment = TextEditorDialogFragment.show(this)
                textEditorDialogFragment.setOnTextEditorListener(object :
                    TextEditorDialogFragment.TextEditorListener {
                    override fun onDone(inputText: String, colorCode: Int) {
                        val styleBuilder = TextStyleBuilder()
                        styleBuilder.withTextColor(colorCode)
                        mPhotoEditor.addText(inputText, styleBuilder)
                        mTxtCurrentTool.setText(vn.xdeuhug.seniorsociable.R.string.label_text)
                    }
                })
            }

            ToolType.ERASER -> {
                mPhotoEditor.brushEraser()
                mTxtCurrentTool.setText(vn.xdeuhug.seniorsociable.R.string.label_eraser_mode)
            }

            ToolType.FILTER -> {
                mTxtCurrentTool.setText(vn.xdeuhug.seniorsociable.R.string.label_filter)
                showFilter(true)
            }

            ToolType.EMOJI -> showBottomSheetDialogFragment(mEmojiBSFragment)
            ToolType.STICKER -> showBottomSheetDialogFragment(mStickerBSFragment)
        }
    }

    private fun showBottomSheetDialogFragment(fragment: BottomSheetDialogFragment?) {
        if (fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(supportFragmentManager, fragment.tag)
    }

    private fun showFilter(isVisible: Boolean) {
        mIsFilterVisible = isVisible
        mConstraintSet.clone(mRootView)

        val rvFilterId: Int = mRvFilters.id

        if (isVisible) {
            mConstraintSet.clear(rvFilterId, ConstraintSet.START)
            mConstraintSet.connect(
                rvFilterId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START
            )
            mConstraintSet.connect(
                rvFilterId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END
            )
        } else {
            mConstraintSet.connect(
                rvFilterId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.END
            )
            mConstraintSet.clear(rvFilterId, ConstraintSet.END)
        }

        val changeBounds = ChangeBounds()
        changeBounds.duration = 350
        changeBounds.interpolator = AnticipateOvershootInterpolator(1.0f)
        TransitionManager.beginDelayedTransition(mRootView, changeBounds)

        mConstraintSet.applyTo(mRootView)
    }

    override fun onBackPressed() {
        if (mIsFilterVisible) {
            showFilter(false)
            mTxtCurrentTool.setText(vn.xdeuhug.seniorsociable.R.string.create_story)
        } else if (!mPhotoEditor.isCacheEmpty) {
            showSaveDialog()
        } else {
            super.onBackPressed()
        }
    }

    companion object {

        private const val TAG = "EditImageActivity"

        const val FILE_PROVIDER_AUTHORITY = "${BuildConfig.APPLICATION_ID}.fileprovider"
        private const val CAMERA_REQUEST = 52
        private const val PICK_REQUEST = 53
        const val ACTION_NEXTGEN_EDIT = "action_nextgen_edit"
        const val PINCH_TEXT_SCALABLE_INTENT_KEY = "PINCH_TEXT_SCALABLE"
    }

    @SuppressLint("RestrictedApi")
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        //
    }

}