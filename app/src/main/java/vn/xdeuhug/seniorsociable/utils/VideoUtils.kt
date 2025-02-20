@file:Suppress("DEPRECATION")

package vn.xdeuhug.seniorsociable.utils

import android.content.Context
import android.net.Uri
import cn.jzvd.JZDataSource
import cn.jzvd.Jzvd
import cn.jzvd.JzvdStd
import cn.jzvd.JzvdStdTikTok
import com.danikula.videocache.HttpProxyCacheServer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import vn.xdeuhug.seniorsociable.app.AppApplication
import vn.xdeuhug.seniorsociable.model.entity.modelPexels.VideoFile
import vn.xdeuhug.seniorsociable.utils.AppUtils.show


/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 16 / 10 / 2023
 */
object  VideoUtils {
    @Suppress("DEPRECATION")
    fun loadVideoPost(rootUrl: String, url: String, playerView: PlayerView) {
        val simpleExoPlayer = SimpleExoPlayer.Builder(playerView.context).build()
        playerView.player = simpleExoPlayer

        val dataSourceFactory = DefaultDataSourceFactory(playerView.context, "Video")
        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(Uri.parse(url)))
        simpleExoPlayer.prepare(videoSource)
        // Đặt PlayerView để tự động bắt đầu khi đã sẵn sàng
        playerView.useController = false
        simpleExoPlayer.playWhenReady = true

        // Theo dõi sự kiện kết thúc video
        simpleExoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    // Video đã kết thúc, hãy phát lại từ đầu
                    simpleExoPlayer.seekTo(0)
                }
            }
        })
        //
    }

    fun loadVideo(video: VideoFile, zVideo: JzvdStdTikTok, app: AppApplication) {
        val proxy: HttpProxyCacheServer = app.getProxy(app)!!
        val proxyUrl = proxy.getProxyUrl(video.link)
        PhotoShowUtils.loadPhotoImageNormal(proxyUrl, zVideo.posterImageView)
        val jzDataSource = JZDataSource(
            proxyUrl, ""
        )
        jzDataSource.looping = true
        zVideo.setUp(jzDataSource, Jzvd.SCREEN_NORMAL)
        zVideo.startVideoAfterPreloading()
        zVideo.bottomProgressBar.show()
    }

    fun loadVideoInPost(videoUrl: String, zVideo: JzvdStd, app: AppApplication) {
        val proxy: HttpProxyCacheServer = app.getProxy(app)!!
        val proxyUrl = proxy.getProxyUrl(videoUrl)
        PhotoShowUtils.loadPostImageCenterCrop("",proxyUrl, zVideo.posterImageView)
        val jzDataSource = JZDataSource(
            proxyUrl, ""
        )
        zVideo.setUp(jzDataSource, Jzvd.SCREEN_NORMAL)
    }

    fun loadVideo(videoUrl: String, zVideo: JzvdStd) {
        PhotoShowUtils.loadPhotoImageNormal(videoUrl, zVideo.posterImageView)
        val jzDataSource = JZDataSource(
            videoUrl, ""
        )
        zVideo.setUp(jzDataSource, Jzvd.SCREEN_NORMAL)
        zVideo.startVideoAfterPreloading()
    }

    fun loadVideoWithDataSource(jzDataSource: JZDataSource, zVideo: JzvdStd) {
        zVideo.setUp(jzDataSource, Jzvd.SCREEN_NORMAL)
        zVideo.startVideoAfterPreloading()
    }

    /**
     * @param rootUrl bao gồm các loại id
     */
    fun loadVideoFromFirebase(rootUrl: String, urlVideo: String, zVideo: JzvdStd) {
        if (urlVideo.contains("/")) {
            loadVideo(urlVideo, zVideo)
        } else {
            MultimediaUtils.getURLMedia("$rootUrl$urlVideo") { imageUrl ->
                loadVideo(imageUrl, zVideo)
            }
        }
    }


}