package com.nntk.nba0708

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.util.Util
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultDataSourceFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import cn.jzvd.Jzvd
import cn.jzvd.JzvdStd
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.MetaDataUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ResourceUtils
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.common.base.Strings
import com.nntk.nba0708.entity.TeamMeta
import com.nntk.nba0708.ui.theme.Cctv5nba0708Theme
import com.nntk.nba0708.util.MusicUtil
import com.nntk.nba0708.view.MyJzvd
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import me.jessyan.autosize.internal.CustomAdapt
import java.io.File
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity(), CustomAdapt {
    private val mediaPlayer = MediaPlayer()

    private lateinit var teamMetaList: List<TeamMeta>
    private lateinit var gameMeta: JSONObject


    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 横向屏幕
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        BarUtils.setStatusBarVisibility(window, false)
        setContentView(R.layout.main)

        loadMeta()

        val bgVideo = findViewById<MyJzvd>(R.id.bg_player)
        val gameVideo = findViewById<PlayerView>(R.id.game_player)


        val composeView = findViewById<ComposeView>(R.id.compose_view)
        composeView.visibility = View.INVISIBLE

        composeView.setContent {
            Cctv5nba0708Theme {
                Greeting("Android")
            }
        }




        Observable.timer(0, TimeUnit.MILLISECONDS)
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(Schedulers.io())
            .subscribe { o: Long? ->
                MusicUtil.play(this, mediaPlayer)
            }

        // 初始化背景高科技墙
        initBgVideo(bgVideo)
        initGameVideo(gameVideo)


        // 播放客队logo
        val imageViewGuest = findViewById<ImageView>(R.id.iv_guest_logo)
        val imageViewHome = findViewById<ImageView>(R.id.iv_home_logo)

        var teamName = gameMeta.getJSONObject("guest").getString("team")
        var teamMeta =
            teamMetaList.stream()
                .filter { item -> item.logoInfo?.logoName.equals(teamName) }
                .findFirst()
                .get()
        val guestLogo = loadLogo(imageViewGuest, teamMeta, "guest")


        // 播放主队logo
        teamName = gameMeta.getJSONObject("home").getString("team")
        teamMeta =
            teamMetaList.stream()
                .filter { item -> item.logoInfo?.logoName.equals(teamName) }
                .findFirst()
                .get()
        val homeLogo = loadLogo(imageViewHome, teamMeta, "home")



        Observable.timer(2000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { o: Long? ->
                guestLogo.start()
                homeLogo.start()
                YoYo.with(Techniques.ZoomInLeft)
                    .duration(2000)
                    .repeat(0)
                    .playOn(imageViewGuest)

                YoYo.with(Techniques.ZoomInRight)
                    .duration(2000)
                    .repeat(0)
                    .playOn(imageViewHome)


                composeView.visibility = View.VISIBLE
                YoYo.with(Techniques.ZoomInDown)
                    .duration(1000)
                    .repeat(0)
                    .playOn(composeView)


            }



        gameVideo.visibility = View.INVISIBLE

        Observable.timer(9000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { o: Long? ->

                YoYo.with(Techniques.ZoomOutLeft)
                    .duration(1000)
                    .repeat(0)
                    .onEnd {
                        gameVideo.visibility = View.VISIBLE
                        gameVideo.player?.play()
                    }
                    .playOn(imageViewGuest)



                YoYo.with(Techniques.ZoomOutRight)
                    .duration(1000)
                    .repeat(0)
                    .playOn(imageViewHome)

                YoYo.with(Techniques.ZoomOutDown)
                    .duration(1000)
                    .repeat(0)
                    .playOn(composeView)

            }


        Observable.timer(15000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { o: Long? ->


            }


    }

    private fun loadMeta() {
        teamMetaList = JSON.parseArray(
            ResourceUtils.readAssets2String("team_meta.json5"),
            TeamMeta::class.javaObjectType
        )

        gameMeta = JSON.parseObject(ResourceUtils.readAssets2String("game.json5"))

    }

    private fun initGameVideo(gameVideo: PlayerView) {
        val bgMp4Path = PathUtils.getInternalAppDataPath() + File.separator + "lakers_suns.mp4"
        ResourceUtils.copyFileFromRaw(R.raw.lakers_suns, bgMp4Path)

        val player = ExoPlayer.Builder(this).build()
        val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(this)
        val videoSource: MediaSource =
            ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                MediaItem.fromUri(Uri.parse(bgMp4Path))
            )
        player.setMediaSource(videoSource)
        player.prepare()
        gameVideo.controllerAutoShow = false
        gameVideo.player = player
    }

    private fun initBgVideo(bgVideo: MyJzvd) {
        Jzvd.setVideoImageDisplayType(Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP)
        // 拷贝bg到本地sd
        val bgMp4Path = PathUtils.getInternalAppDataPath() + File.separator + "bg.mp4"
        ResourceUtils.copyFileFromRaw(R.raw.bg, bgMp4Path)
        bgVideo.setUp(bgMp4Path, null, JzvdStd.SCREEN_NORMAL)
        bgVideo.startVideo()

        bgVideo.jzDataSource.looping = true
        bgVideo.progressBar.visibility = View.INVISIBLE
        bgVideo.bottomProgressBar.visibility = View.INVISIBLE


    }


    private fun loadLogo(
        imageView: ImageView,
        teamMeta: TeamMeta,
        homeGuest: String
    ): AnimationDrawable {
        val teamName = gameMeta.getJSONObject(homeGuest).getString("team")
        val size = teamMeta.logoInfo?.size

        val animationDrawable = AnimationDrawable()
        animationDrawable.isOneShot = true
        val loopIndex = teamMeta.logoInfo?.loopIndex
        for (i in 0..size!!) {
            val animationId = Strings.padStart(i.toString(), 5, '0')
            val frameDrawable = ContextCompat.getDrawable(
                baseContext,
                ResourceUtils.getDrawableIdByName(teamName + "_$animationId")
            )
            animationDrawable.addFrame(frameDrawable!!, 48)
        }



        imageView.setImageDrawable(animationDrawable)


        return animationDrawable

    }


    override fun isBaseOnWidth(): Boolean {
        return true
    }

    override fun getSizeInDp(): Float {
        return MetaDataUtils.getMetaDataInApp("design_width_in_dp").toFloat()
    }


    override fun onPause() {
        super.onPause()
        mediaPlayer.stop()
        MyJzvd.releaseAllVideos()
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "2007年12月25日", color = Color.White, fontSize = 12.sp)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "公牛", color = Color.White)
            Text(text = "凯尔特人", color = Color.White)
        }
    }


}

@Preview(showBackground = false)
@Composable
fun GreetingPreview() {
    Cctv5nba0708Theme {
        Greeting("Android")
    }
}