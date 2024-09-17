package com.nntk.nba0708

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import cn.jzvd.Jzvd
import cn.jzvd.JzvdStd
import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONObject
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.MetaDataUtils
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.ResourceUtils
import com.google.common.base.Strings
import com.nntk.nba0708.entity.TeamMeta
import com.nntk.nba0708.ui.theme.Cctv5nba0708Theme
import com.nntk.nba0708.util.MusicUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import me.jessyan.autosize.internal.CustomAdapt
import java.io.File
import java.util.ArrayList
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
//        setContent {
//            Cctv5nba0708Theme {
//                // A surface container using the 'background' color from the theme
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    Greeting("Android")
//                }
//            }
//        }

        Observable.timer(0, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { o: Long? ->
                // 初始化背景高科技墙
                initBgVideo()
            }

        Observable.timer(0, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { o: Long? ->
                MusicUtil.play(this, mediaPlayer)
            }


        Observable.timer(1000, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { o: Long? ->

                // 播放客队logo
                var teamName = gameMeta.getJSONObject("guest").getString("team")
                var teamMeta =
                    teamMetaList.stream()
                        .filter { item -> item.logoInfo?.logoName.equals(teamName) }
                        .findFirst()
                        .get()
                playLogo(R.id.iv_guest_logo, teamMeta, "guest")


                // 播放主队logo
                teamName = gameMeta.getJSONObject("home").getString("team")
                teamMeta =
                    teamMetaList.stream()
                        .filter { item -> item.logoInfo?.logoName.equals(teamName) }
                        .findFirst()
                        .get()
                playLogo(R.id.iv_home_logo, teamMeta, "home")

            }


    }

    private fun loadMeta() {
        teamMetaList = JSON.parseArray(
            ResourceUtils.readAssets2String("team_meta.json5"),
            TeamMeta::class.javaObjectType
        )

        gameMeta = JSON.parseObject(ResourceUtils.readAssets2String("game.json5"))

    }

    private fun initBgVideo() {
        Jzvd.setVideoImageDisplayType(Jzvd.VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP)
        // 拷贝bg到本地sd
        val bgMp4Path = PathUtils.getInternalAppDataPath() + File.separator + "bg.mp4"
        ResourceUtils.copyFileFromRaw(R.raw.bg, bgMp4Path)
        val bgVideo = findViewById<JzvdStd>(R.id.jz_video)
        bgVideo.setUp(bgMp4Path, null, JzvdStd.SCREEN_NORMAL)
        bgVideo.startButton.performClick();
        bgVideo.jzDataSource.looping = true
        bgVideo.progressBar.visibility = View.INVISIBLE
        bgVideo.bottomProgressBar.visibility = View.INVISIBLE
    }


    private fun playLogo(imageViewId: Int, teamMeta: TeamMeta, homeGuest: String) {
        val teamName = gameMeta.getJSONObject(homeGuest).getString("team")
        val size = teamMeta.logoInfo?.size
        val imageView = findViewById<ImageView>(imageViewId)
        val animationDrawable = AnimationDrawable()
        animationDrawable.isOneShot = true
        for (i in 0..size!!) {
            val animationId = Strings.padStart(i.toString(), 5, '0')
            val frameDrawable = ContextCompat.getDrawable(
                baseContext,
                ResourceUtils.getDrawableIdByName(teamName + "_$animationId")
            )
            animationDrawable.addFrame(frameDrawable!!, 48)
        }
        imageView.setImageDrawable(animationDrawable);
        animationDrawable.start();
    }


    override fun isBaseOnWidth(): Boolean {
        return if (resources.configuration.orientation === Configuration.ORIENTATION_PORTRAIT) {
            false //根据宽度适配
        } else {
            true //根据高度适配
        }
    }

    override fun getSizeInDp(): Float {
        return MetaDataUtils.getMetaDataInApp("design_width_in_dp").toFloat()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Cctv5nba0708Theme {
        Greeting("Android")
    }
}