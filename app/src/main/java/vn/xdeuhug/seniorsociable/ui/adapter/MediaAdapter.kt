package vn.xdeuhug.seniorsociable.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import cn.jzvd.JZDataSource
import org.jetbrains.anko.dimen
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.app.AppAdapter
import vn.xdeuhug.seniorsociable.app.AppApplication
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.constants.PostConstants
import vn.xdeuhug.seniorsociable.databinding.LayoutOneMediaBinding
import vn.xdeuhug.seniorsociable.model.entity.modelMedia.MultiMedia
import vn.xdeuhug.seniorsociable.model.entity.modelUser.User
import vn.xdeuhug.seniorsociable.utils.AppUtils
import vn.xdeuhug.seniorsociable.utils.AppUtils.hide
import vn.xdeuhug.seniorsociable.utils.AppUtils.show
import vn.xdeuhug.seniorsociable.utils.AppUtils.toArrayList
import vn.xdeuhug.seniorsociable.utils.PhotoShowUtils
import vn.xdeuhug.seniorsociable.utils.UploadFireStorageUtils
import vn.xdeuhug.seniorsociable.utils.VideoUtils

/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 22 / 10 / 2023
 */
class MediaAdapter(context: Context) : AppAdapter<MultiMedia>(context) {
    var onListenerCLick: OnListenerCLick? = null

    constructor(context: Context, userPost: User, typePost: Int, idPost: String) : this(context) {
        this.userPost = userPost
        this.typePost = typePost
        this.idPost = idPost
    }

    private var userPost: User? = null
    private var typePost = PostConstants.POST_DEFAULT
    private var idPost = ""
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = LayoutOneMediaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolderOne(binding)
    }

    inner class ViewHolderOne(private val binding: LayoutOneMediaBinding) :
        AppViewHolder(binding.root) {

        init {
            binding.root.clickWithDebounce {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerCLick!!.onClick(
                        null,getData()[position], getData().toArrayList(), userPost!!.id
                    )
                }
            }
            binding.plvVideo.fullscreenButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListenerCLick!!.onClick(
                        binding.plvVideo.jzDataSource,getData()[position], getData().toArrayList(), userPost!!.id
                    )
                }
            }
        }


        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        override fun onBindView(position: Int) {
            val sizeMedia = getData().size
            val item = getItem(position)
            val urlAndRootUrl = getRootURLAndURL(item)
            when (sizeMedia) {
                1 -> {
                    when (item.type) {
                        AppConstants.UPLOAD_IMAGE -> {
                            AppUtils.scaleImageFitWidthScreenPost(
                                binding.imvImage,
                                getContext(),
                                AppUtils.convertPixelToDp(getResources(), item.height),
                                AppUtils.convertPixelToDp(getResources(), item.width)
                            )
                            binding.imvImage.show()
                            binding.plvVideo.hide()
                            PhotoShowUtils.loadPostImageCenterCrop(
                                urlAndRootUrl.first, urlAndRootUrl.second, binding.imvImage
                            )
                        }

                        AppConstants.UPLOAD_VIDEO -> {
                            binding.plvVideo.show()
                            binding.imvImage.hide()
                            AppUtils.scaleVideoFitWidthScreenPost(
                                binding.plvVideo,
                                getContext(),
                                AppUtils.convertPixelToDp(getResources(), item.height),
                                AppUtils.convertPixelToDp(getResources(), item.width)
                            )
                            VideoUtils.loadVideoInPost(item.url, binding.plvVideo, getContext().applicationContext as AppApplication)
                        }
                    }
                }

                2 -> {
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    when (position) {
                        0 -> {
                            layoutParams.marginEnd = getContext().dimen(R.dimen.dp_2)
                            binding.root.layoutParams = layoutParams
                        }

                        1 -> {
                            layoutParams.marginStart = getContext().dimen(R.dimen.dp_2)
                            binding.root.layoutParams = layoutParams
                        }
                    }
                    when (item.type) {
                        AppConstants.UPLOAD_IMAGE -> {
                            AppUtils.scaleImageHaftWidthScreen(
                                binding.imvImage,
                                getContext(),
                                AppUtils.convertPixelToDp(getResources(), item.height),
                                AppUtils.convertPixelToDp(getResources(), item.width)
                            )
                            binding.imvImage.show()
                            binding.plvVideo.hide()
                            PhotoShowUtils.loadPostImageCenterCrop(
                                urlAndRootUrl.first, urlAndRootUrl.second, binding.imvImage
                            )
                        }

                        AppConstants.UPLOAD_VIDEO -> {
                            binding.plvVideo.show()
                            binding.imvImage.hide()
                            AppUtils.scaleVideoHaftWidthScreen(
                                binding.plvVideo,
                                getContext(),
                                AppUtils.convertPixelToDp(getResources(), item.height),
                                AppUtils.convertPixelToDp(getResources(), item.width)
                            )
                            VideoUtils.loadVideoInPost(
                                item.url,
                                binding.plvVideo,
                                getContext().applicationContext as AppApplication
                            )
                        }
                    }
                }

                3 -> {
                    when (position) {
                        0 -> {
                            val layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )
                            layoutParams.marginEnd = getContext().dimen(R.dimen.dp_2)
                            binding.root.layoutParams = layoutParams
                        }

                        1 -> {

                            val layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            layoutParams.marginStart = getContext().dimen(R.dimen.dp_2)
                            layoutParams.bottomMargin = getContext().dimen(R.dimen.dp_2)
                            binding.root.layoutParams = layoutParams
                        }

                        2 -> {

                            val layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            layoutParams.marginStart = getContext().dimen(R.dimen.dp_2)
                            layoutParams.topMargin = getContext().dimen(R.dimen.dp_2)
                            binding.root.layoutParams = layoutParams
                        }
                    }
                    when (position) {
                        0 -> {
                            when (item.type) {
                                AppConstants.UPLOAD_IMAGE -> {
                                    AppUtils.scaleImageThreePartFourWidthScreen(
                                        binding.imvImage,
                                        getContext(),
                                        AppUtils.convertPixelToDp(getResources(), item.height),
                                        AppUtils.convertPixelToDp(getResources(), item.width)
                                    )
                                    binding.imvImage.show()
                                    binding.plvVideo.hide()
                                    PhotoShowUtils.loadPostImageCenterCrop(
                                        urlAndRootUrl.first, urlAndRootUrl.second, binding.imvImage
                                    )
                                }

                                AppConstants.UPLOAD_VIDEO -> {
                                    binding.plvVideo.show()
                                    binding.imvImage.hide()
                                    AppUtils.scaleVideoThreePartFourWidthScreen(
                                        binding.plvVideo,
                                        getContext(),
                                        AppUtils.convertPixelToDp(getResources(), item.height),
                                        AppUtils.convertPixelToDp(getResources(), item.width)
                                    )
                                    VideoUtils.loadVideoInPost(
                                        item.url,
                                        binding.plvVideo,
                                        getContext().applicationContext as AppApplication
                                    )
                                }
                            }
                        }

                        1, 2 -> {
                            when (item.type) {
                                AppConstants.UPLOAD_IMAGE -> {
                                    AppUtils.scaleImageOnePartFourWidthScreen(
                                        binding.imvImage,
                                        getContext(),
                                        AppUtils.convertPixelToDp(getResources(), item.height),
                                        AppUtils.convertPixelToDp(getResources(), item.width),
                                        2
                                    )
                                    binding.imvImage.show()
                                    binding.plvVideo.hide()
                                    PhotoShowUtils.loadPostImageCenterCrop(
                                        urlAndRootUrl.first, urlAndRootUrl.second, binding.imvImage
                                    )
                                }

                                AppConstants.UPLOAD_VIDEO -> {
                                    binding.plvVideo.show()
                                    binding.imvImage.hide()
                                    AppUtils.scaleVideoOnePartFourWidthScreen(
                                        binding.plvVideo,
                                        getContext(),
                                        AppUtils.convertPixelToDp(getResources(), item.height),
                                        AppUtils.convertPixelToDp(getResources(), item.width),
                                        2
                                    )
                                    VideoUtils.loadVideoInPost(
                                        item.url,
                                        binding.plvVideo,
                                        getContext().applicationContext as AppApplication
                                    )
                                }
                            }
                        }
                    }
                }

                4 -> {
                    when (position) {
                        0 -> {
                            val layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )
                            layoutParams.marginEnd = getContext().dimen(R.dimen.dp_2)
                            binding.root.layoutParams = layoutParams
                        }

                        1 -> {

                            val layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            layoutParams.marginStart = getContext().dimen(R.dimen.dp_2)
                            layoutParams.bottomMargin = getContext().dimen(R.dimen.dp_2)
                            binding.root.layoutParams = layoutParams
                        }

                        2 -> {

                            val layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            layoutParams.marginStart = getContext().dimen(R.dimen.dp_2)
                            layoutParams.topMargin = getContext().dimen(R.dimen.dp_2)
                            layoutParams.bottomMargin = getContext().dimen(R.dimen.dp_2)
                            binding.root.layoutParams = layoutParams
                        }

                        3 -> {

                            val layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            layoutParams.marginStart = getContext().dimen(R.dimen.dp_2)
                            layoutParams.topMargin = getContext().dimen(R.dimen.dp_2)
                            binding.root.layoutParams = layoutParams
                        }
                    }
                    when (position) {
                        0 -> {
                            when (item.type) {
                                AppConstants.UPLOAD_IMAGE -> {
                                    AppUtils.scaleImageThreePartFourWidthScreenWithFourItem(
                                        binding.imvImage,
                                        getContext(),
                                        AppUtils.convertPixelToDp(getResources(), item.height),
                                        AppUtils.convertPixelToDp(getResources(), item.width)
                                    )
                                    binding.imvImage.show()
                                    binding.plvVideo.hide()
                                    PhotoShowUtils.loadPostImageCenterCrop(
                                        urlAndRootUrl.first, urlAndRootUrl.second, binding.imvImage
                                    )
                                }

                                AppConstants.UPLOAD_VIDEO -> {
                                    binding.plvVideo.show()
                                    binding.imvImage.hide()
                                    AppUtils.scaleVideoThreePartFourWidthScreenWithFourItem(
                                        binding.plvVideo,
                                        getContext(),
                                        AppUtils.convertPixelToDp(getResources(), item.height),
                                        AppUtils.convertPixelToDp(getResources(), item.width)
                                    )
                                    VideoUtils.loadVideoInPost(
                                        item.url,
                                        binding.plvVideo,
                                        getContext().applicationContext as AppApplication
                                    )
                                }
                            }
                        }

                        1, 2, 3 -> {
                            when (item.type) {
                                AppConstants.UPLOAD_IMAGE -> {
                                    AppUtils.scaleImageOnePartFourWidthScreen(
                                        binding.imvImage,
                                        getContext(),
                                        AppUtils.convertPixelToDp(getResources(), item.height),
                                        AppUtils.convertPixelToDp(getResources(), item.width),
                                        3
                                    )
                                    binding.imvImage.show()
                                    binding.plvVideo.hide()
                                    PhotoShowUtils.loadPostImageCenterCrop(
                                        urlAndRootUrl.first, urlAndRootUrl.second, binding.imvImage
                                    )
                                }

                                AppConstants.UPLOAD_VIDEO -> {
                                    binding.plvVideo.show()
                                    binding.imvImage.hide()
                                    AppUtils.scaleVideoOnePartFourWidthScreen(
                                        binding.plvVideo,
                                        getContext(),
                                        AppUtils.convertPixelToDp(getResources(), item.height),
                                        AppUtils.convertPixelToDp(getResources(), item.width),
                                        3
                                    )
                                    VideoUtils.loadVideoInPost(
                                        item.url,
                                        binding.plvVideo,
                                        getContext().applicationContext as AppApplication
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

    }


    private fun getRootURLAndURL(item: MultiMedia): Pair<String, String> {
        return when (typePost) {
            PostConstants.POST_DEFAULT -> {
                Pair(UploadFireStorageUtils.getRootURLPostById(idPost), item.url)
            }

            PostConstants.POST_UPDATE_AVATAR -> {
                Pair(UploadFireStorageUtils.getRootURLAvatarById(userPost!!.id), item.url)
            }

            PostConstants.POST_UPDATE_BACKGROUND -> {
                Pair(UploadFireStorageUtils.getRootURLBackgroundById(userPost!!.id), item.url)
            }

            else -> {
                Pair("", "")
            }
        }
    }

    interface OnListenerCLick {
        fun onClick(jzDataSource: JZDataSource?,multiMedia: MultiMedia, lisMultiMedia: ArrayList<MultiMedia>, idPost: String)
    }
}