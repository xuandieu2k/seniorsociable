package vn.xdeuhug.seniorsociable.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.google.common.base.Strings
import com.google.gson.Gson
import timber.log.Timber
import vn.xdeuhug.seniorsociable.R
import vn.xdeuhug.seniorsociable.cache.ConfigCache
import vn.xdeuhug.seniorsociable.constants.AppConstants
import vn.xdeuhug.seniorsociable.ui.activity.MediaActivity
import java.io.File


object PhotoShowUtils {

    // Đang dùng
    fun getLinkPhoto(photo: String?): String {
        return String.format("%s%s", ConfigCache.getConfig().apiUploadShort, photo)
    }

    fun loadPhotoImageNormal(url: String, view: ImageView) {
        if (url.contains("/")) {
            Glide.with(view.context.applicationContext).asBitmap().load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                    RequestOptions().placeholder(R.drawable.ic_logo_senior_sociable_vn)
                        .error(R.drawable.ic_logo_senior_sociable_vn)
                ).into(view)
        } else {
            Glide.with(view.context.applicationContext).asBitmap().load(
                String.format(
                    "%s%s", ConfigCache.getConfig().apiUploadShort, url
                )
            ).diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                RequestOptions().placeholder(R.drawable.ic_logo_senior_sociable_vn)
                    .error(R.drawable.ic_logo_senior_sociable_vn)
            ).into(view)
        }
    }

    fun loadPhotoImagePreview(url: String, view: ImageView) {
        if (url.contains("/")) {
            Glide.with(view.context.applicationContext).load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                    RequestOptions().placeholder(R.drawable.ic_logo_senior_sociable_vn)
                        .error(R.drawable.ic_logo_senior_sociable_vn)
                ).into(view)
        } else {
            Glide.with(view.context.applicationContext).load(
                String.format(
                    "%s%s", ConfigCache.getConfig().apiUploadShort, url
                )
            ).diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                RequestOptions().placeholder(R.drawable.ic_logo_senior_sociable_vn)
                    .error(R.drawable.ic_logo_senior_sociable_vn)
            ).into(view)
        }
    }

    fun loadPhotoImagePreview(url: String, view: ImageView, callback: (width: Int, height: Int) -> Unit) {
        Glide.with(view.context.applicationContext)
            .asBitmap()
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.ic_logo_senior_sociable_vn)
                    .error(R.drawable.ic_logo_senior_sociable_vn)
            )
            .addListener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Handle failure
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (resource != null) {
                        val width = resource.width
                        val height = resource.height
                        callback(width, height)
                    }
                    return false
                }
            })
            .into(view)
    }

    fun loadPhotoNews(url: String, view: ImageView) {
        if (url.contains("/")) {
            Glide.with(view.context.applicationContext).asBitmap().load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                    RequestOptions().placeholder(R.drawable.news_image)
                        .error(R.drawable.news_image)
                ).into(view)
        } else {
            Glide.with(view.context.applicationContext).asBitmap().load(
                String.format(
                    "%s%s", ConfigCache.getConfig().apiUploadShort, url
                )
            ).diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                RequestOptions().placeholder(R.drawable.news_image)
                    .error(R.drawable.news_image)
            ).into(view)
        }
    }


    fun loadPhotoRound(url: String, view: ImageView) {
        if (url.contains("/")) {
            Glide.with(view.context.applicationContext).asBitmap().load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                    RequestOptions().placeholder(R.drawable.ic_logo_senior_sociable_vn)
                        .error(R.drawable.ic_logo_senior_sociable_vn)
                ).centerCrop().transform(
                    MultiTransformation(
                        RoundedCorners(
                            view.context.resources.getDimension(R.dimen.dp_8).toInt()
                        )
                    )
                ).into(view)
        } else {
            Glide.with(view.context.applicationContext).asBitmap().load(
                String.format(
                    "%s%s", ConfigCache.getConfig().apiUploadShort, url
                )
            ).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().transform(
                MultiTransformation(
                    RoundedCorners(
                        view.context.resources.getDimension(R.dimen.dp_8).toInt()
                    )
                )
            ).apply(
                RequestOptions().placeholder(R.drawable.ic_logo_senior_sociable_vn)
                    .error(R.drawable.ic_logo_senior_sociable_vn)
            ).into(view)
        }
    }


    fun loadMessageImage(rootUrl: String, url: String, view: ImageView) {
        if (url.contains("/")) {
            Glide.with(view.context.applicationContext).asBitmap().load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                    RequestOptions().placeholder(R.drawable.ic_logo_senior_sociable_vn)
                        .error(R.drawable.ic_logo_senior_sociable_vn)
                ).centerCrop().transform(
                    MultiTransformation(
                        RoundedCorners(
                            view.context.resources.getDimension(R.dimen.dp_8).toInt()
                        )
                    )
                ).into(view)
        } else {
            MultimediaUtils.getURLMedia("$rootUrl$url") { imageUrl ->
                Glide.with(view.context.applicationContext).asBitmap().load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().transform(
                        MultiTransformation(
                            RoundedCorners(
                                view.context.resources.getDimension(R.dimen.dp_8).toInt()
                            )
                        )
                    ).apply(
                        RequestOptions().placeholder(R.drawable.ic_logo_senior_sociable_vn)
                            .error(R.drawable.ic_logo_senior_sociable_vn)
                    ).into(view)
            }
        }
    }

    fun loadAvatarImage(rootUrl: String, url: String, view: ImageView) {
        if (url.contains("/")) {
            Glide.with(view.context.applicationContext).asBitmap().load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                    RequestOptions().placeholder(R.drawable.ic_user_default)
                        .error(R.drawable.ic_user_default)
                ).centerCrop().transform(MultiTransformation(CenterCrop(), CircleCrop())).into(view)
        } else {
            MultimediaUtils.getURLMedia("$rootUrl$url") { imageUrl ->
                Glide.with(view.context.applicationContext).asBitmap().load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().apply(
                        RequestOptions().placeholder(R.drawable.ic_user_default)
                            .error(R.drawable.ic_user_default)
                    ).transform(MultiTransformation(CenterCrop(), CircleCrop())).into(view)
            }
        }
    }

    fun loadAvatarRound(url: String, view: ImageView) {
        if (url.contains("/")) {
            Glide.with(view.context.applicationContext).asBitmap().load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                    RequestOptions().placeholder(R.drawable.ic_user_default)
                        .error(R.drawable.ic_user_default)
                ).centerCrop().transform(
                    MultiTransformation(
                        RoundedCorners(
                            view.context.resources.getDimension(R.dimen.dp_8).toInt()
                        )
                    )
                ).into(view)
        } else {
            Glide.with(view.context.applicationContext).asBitmap().load(
                String.format(
                    "%s%s", ConfigCache.getConfig().apiUploadShort, url
                )
            ).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().transform(
                MultiTransformation(
                    RoundedCorners(
                        view.context.resources.getDimension(R.dimen.dp_8).toInt()
                    )
                )
            ).apply(
                RequestOptions().placeholder(R.drawable.ic_user_default)
                    .error(R.drawable.ic_user_default)
            ).into(view)
        }
    }

    fun loadAvatarImage(url: String, view: ImageView) {
        Glide.with(view.context.applicationContext).asBitmap().load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                RequestOptions().placeholder(R.drawable.ic_user_default)
                    .error(R.drawable.ic_user_default)
            ).centerCrop().transform(MultiTransformation(CenterCrop(), CircleCrop())).into(view)
    }

    fun loadPostImage(rootUrl: String, url: String, view: ImageView) {
        if (url.contains("/")) {
            Glide.with(view.context.applicationContext).asBitmap().load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                    RequestOptions().placeholder(R.drawable.ic_logo_senior_sociable_vn)
                        .error(R.drawable.ic_logo_senior_sociable_vn)
                ).into(view)
        } else {
            MultimediaUtils.getURLMedia("$rootUrl$url") { imageUrl ->
                Timber.tag("Log URL").i(imageUrl)
                Glide.with(view.context.applicationContext).asBitmap().load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                        RequestOptions().placeholder(R.drawable.ic_logo_senior_sociable_vn)
                            .error(R.drawable.ic_logo_senior_sociable_vn)
                    ).into(view)
            }
        }
    }

    fun loadPostImageCenterCrop(rootUrl: String, url: String, view: ImageView) {
        if (url.contains("/")) {
            Glide.with(view.context.applicationContext).asBitmap().load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                    RequestOptions().placeholder(R.drawable.ic_logo_senior_sociable_vn)
                        .error(R.drawable.ic_logo_senior_sociable_vn)
                ).centerCrop().into(view)
        } else {
            MultimediaUtils.getURLMedia("$rootUrl$url") { imageUrl ->
                Glide.with(view.context.applicationContext).asBitmap().load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().apply(
                        RequestOptions().placeholder(R.drawable.ic_logo_senior_sociable_vn)
                            .error(R.drawable.ic_logo_senior_sociable_vn)
                    ).into(view)
            }
        }
    }

    fun loadAvatarImageCenterCrop(url: String, view: ImageView) {
        Glide.with(view.context.applicationContext).asBitmap().load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                RequestOptions().placeholder(R.drawable.ic_logo_senior_sociable_vn)
                    .error(R.drawable.ic_logo_senior_sociable_vn)
            ).centerCrop().into(view)
    }

    fun loadBackgroundImageCenterCrop(rootUrl: String, url: String, view: ImageView) {
        if (url.contains("/")) {
            Glide.with(view.context.applicationContext).asBitmap().load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                    RequestOptions().placeholder(R.drawable.ic_logo_senior_sociable_vn)
                        .error(R.drawable.ic_logo_senior_sociable_vn)
                ).centerCrop().into(view)
        } else {
            MultimediaUtils.getURLMedia("$rootUrl$url") { imageUrl ->
                Glide.with(view.context.applicationContext).asBitmap().load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().apply(
                        RequestOptions().placeholder(R.drawable.ic_logo_senior_sociable_vn)
                            .error(R.drawable.ic_logo_senior_sociable_vn)
                    ).into(view)
            }
        }
    }

    fun loadPosterImageCenterCrop(rootUrl: String, url: String, view: ImageView) {
        if (url.contains("/")) {
            Glide.with(view.context.applicationContext).asBitmap().load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                    RequestOptions().placeholder(R.drawable.ic_logo_senior_sociable_vn)
                        .error(R.drawable.ic_logo_senior_sociable_vn)
                ).centerCrop().into(view)
        } else {
            MultimediaUtils.getURLMedia("$rootUrl$url") { imageUrl ->
                Glide.with(view.context.applicationContext).asBitmap().load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().apply(
                        RequestOptions().placeholder(R.drawable.bg_events)
                            .error(R.drawable.bg_events)
                    ).into(view)
            }
        }
    }


    fun loadCommentImage(rootUrl: String, url: String, view: ImageView) {
        if (url.contains("/")) {
            Glide.with(view.context.applicationContext).asBitmap().load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                    RequestOptions().placeholder(R.drawable.ic_logo_senior_sociable_vn)
                        .error(R.drawable.ic_logo_senior_sociable_vn)
                ).into(view)
        } else {
            MultimediaUtils.getURLMedia("$rootUrl$url") { imageUrl ->
                Glide.with(view.context.applicationContext).asBitmap().load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                        RequestOptions().placeholder(R.drawable.ic_logo_senior_sociable_vn)
                            .error(R.drawable.ic_logo_senior_sociable_vn)
                    ).into(view)
            }
        }
    }


    //
    //Tải hình ảnh banner
    fun loadSignatureImage(url: String, view: ImageView) {
        if (url != "") {
            Glide.with(view.context.applicationContext).load(getLinkPhoto(url))
                .diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop()
                .transform(MultiTransformation(CenterCrop())).into(view)
        }
    }


    fun loadPhotoImage(url: String, view: ImageView) {
        if (url.contains("/")) {
            Glide.with(view.context.applicationContext).asBitmap().load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                    RequestOptions().placeholder(R.drawable.ic_logo_supplier)
                        .error(R.drawable.ic_logo_supplier)
                ).centerCrop().into(view)
        } else {
            Glide.with(view.context.applicationContext).asBitmap().load(
                String.format(
                    "%s%s", ConfigCache.getConfig().apiUploadShort, url
                )
            ).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().apply(
                RequestOptions().placeholder(R.drawable.ic_logo_supplier)
                    .error(R.drawable.ic_logo_supplier)
            ).into(view)
        }
    }

    fun loadFoodImage(url: String, view: ImageView) {
        if (url.contains("/")) {
            Glide.with(view.context.applicationContext).asBitmap().load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                    RequestOptions().placeholder(R.drawable.food_default)
                        .error(R.drawable.food_default)
                ).centerCrop().into(view)
        } else {
            Glide.with(view.context.applicationContext).asBitmap().load(
                java.lang.String.format(
                    "%s%s", ConfigCache.getConfig().apiUploadShort, url
                )
            ).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().apply(
                RequestOptions().placeholder(R.drawable.food_default).error(R.drawable.food_default)
            ).into(view)
        }
    }


    fun loadAvatarCompany(url: String, view: ImageView) {
        if (url.contains("/")) {
            Glide.with(view.context.applicationContext).asBitmap().load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                    RequestOptions().placeholder(R.drawable.ic_logo_supplier)
                        .error(R.drawable.ic_logo_supplier)
                ).centerCrop().transform(
                    MultiTransformation(
                        RoundedCorners(
                            view.context.resources.getDimension(R.dimen.dp_16).toInt()
                        )
                    )
                ).into(view)
        } else {
            Glide.with(view.context.applicationContext).asBitmap().load(
                String.format(
                    "%s%s", ConfigCache.getConfig().apiUploadShort, url
                )
            ).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().apply(
                RequestOptions().placeholder(R.drawable.ic_logo_supplier)
                    .error(R.drawable.ic_logo_supplier)
            ).transform(
                MultiTransformation(
                    RoundedCorners(
                        view.context.resources.getDimension(R.dimen.dp_16).toInt()
                    )
                )
            ).into(view)
        }
    }

    private fun getListMedia(photo: String): ArrayList<String> {
        val arrayList: ArrayList<String> = ArrayList()
        if (!photo.contains("/")) arrayList.add(getLinkPhoto(photo)) else arrayList.add(
            photo
        )
        return arrayList
    }

    private fun getListMedia(photo: ArrayList<String>): ArrayList<String> {
        val arrayList: ArrayList<String> = ArrayList()
        for (i in photo.indices) {
            if (!photo[i].contains("/")) {
                arrayList.add(getLinkPhoto(photo[i]))
            } else arrayList.add(photo[i])
        }
        return arrayList
    }


    fun showImageMediaSlider(photo: String, position: Int, context: Context) {
        val json = Gson().toJson(getListMedia(photo))
        val mediaIntent = Intent(context, MediaActivity::class.java)
        mediaIntent.putExtra(AppConstants.MEDIA_DATA, json)
        mediaIntent.putExtra(AppConstants.MEDIA_POSITION, position)
        context.startActivity(mediaIntent)
    }

    fun showImageMediaSlider(photo: ArrayList<String>, position: Int, context: Context) {
        val json = Gson().toJson(getListMedia(photo))
        val mediaIntent = Intent(context, MediaActivity::class.java)
        mediaIntent.putExtra(AppConstants.MEDIA_DATA, json)
        mediaIntent.putExtra(AppConstants.MEDIA_POSITION, position)
        context.startActivity(mediaIntent)
    }

    fun showImageMediaSlider(
        photo: ArrayList<String>, type: ArrayList<String>, position: Int, context: Context
    ) {
        val json = Gson().toJson(getListMedia(photo))
        val mediaIntent = Intent(context, MediaActivity::class.java)
        mediaIntent.putExtra(AppConstants.MEDIA_DATA, json)
        mediaIntent.putExtra(AppConstants.MEDIA_POSITION, position)
        mediaIntent.putExtra(AppConstants.MEDIA_DATA_TYPE, Gson().toJson(type))
        context.startActivity(mediaIntent)
    }

    fun loadGroupAvatar(url: String, view: ImageView) {
        Glide.with(view.context.applicationContext)
            .load(if (!url.contains("/")) getLinkPhoto(url) else Uri.fromFile(File(url)))
            .diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop().apply(
                RequestOptions().placeholder(R.drawable.ic_group).error(R.drawable.ic_group)
            ).transform(MultiTransformation(CenterCrop(), CircleCrop())).into(view)
    }

    fun getBitmap(urlImage: String?, context: Context): Bitmap {
        val bitmapAvatar = if (Strings.isNullOrEmpty(urlImage)) {
            Glide.with(context).asBitmap().load(
                R.drawable.ic_user_default
            ).circleCrop().placeholder(R.drawable.ic_user_default).error(R.drawable.ic_user_default)
                .submit(100, 100).get()
        } else {
            try {
                Glide.with(context).asBitmap().load(urlImage).circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL).apply(
                        RequestOptions().placeholder(R.drawable.ic_user_default)
                            .error(R.drawable.ic_user_default)
                    ).submit(100, 100).get()
            } catch (e: Exception) {
                Glide.with(context).asBitmap().load(
                    R.drawable.ic_user_default
                ).circleCrop().placeholder(R.drawable.ic_user_default)
                    .error(R.drawable.ic_user_default).submit(100, 100)//480 320
                    .get()
            }
        }
        return bitmapAvatar
    }

    fun getBitmapDimensions(context: Context, imageUrl: String, callback: (width: Int, height: Int) -> Unit) {
        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Handle failure
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (resource != null) {
                        val width = resource.width
                        val height = resource.height
                        callback(width, height)
                    }
                    return false
                }
            })
            .submit() // Do not load the image immediately, just submit to get information
    }
}