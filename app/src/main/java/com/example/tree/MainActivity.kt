package com.example.tree

import android.media.MediaPlayer
import android.os.Bundle
import com.bumptech.glide.Glide
import android.net.Uri
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity(), GestureDetector.OnDoubleTapListener, SurfaceHolder.Callback {

    private lateinit var surfaceView: SurfaceView
    private lateinit var backgroundImageView: ImageView
    private lateinit var starImageView: ImageView
    private lateinit var heartImageView: ImageView
    private lateinit var moonImageView: ImageView
    private lateinit var gestureDetector: GestureDetector
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var videoMediaPlayer: MediaPlayer

    private var dX: Float = 0f
    private var dY: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        surfaceView = findViewById(R.id.surfaceView)
        backgroundImageView = findViewById(R.id.backgroundImageView)
        starImageView = findViewById(R.id.starImageView)
        heartImageView = findViewById(R.id.heartImageView)
        moonImageView = findViewById(R.id.moonImageView)

        val surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)

        val videoPath = "android.resource://" + packageName + "/" + R.raw.intro
        val uri: Uri = Uri.parse(videoPath)
        videoMediaPlayer = MediaPlayer.create(this, uri)
        videoMediaPlayer.setOnCompletionListener {
            surfaceView.visibility = SurfaceView.GONE
            backgroundImageView.visibility = ImageView.VISIBLE
            starImageView.visibility = ImageView.VISIBLE
            heartImageView.visibility = ImageView.VISIBLE
            moonImageView.visibility = ImageView.VISIBLE
            playAudio()
        }

        Glide.with(this)
            .load(R.drawable.tree)
            .into(backgroundImageView)

        // Устанавливаем параметры масштабирования для ImageView
        backgroundImageView.scaleType = ImageView.ScaleType.FIT_XY

        // Инициализируем GestureDetector
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                videoMediaPlayer.stop()
                videoMediaPlayer.release()
                surfaceView.visibility = SurfaceView.GONE
                backgroundImageView.visibility = ImageView.VISIBLE
                starImageView.visibility = ImageView.VISIBLE
                heartImageView.visibility = ImageView.VISIBLE
                moonImageView.visibility = ImageView.VISIBLE
                playAudio()
                return true
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Добавляем обработчики перетаскивания для ImageView
        setupDragAndDrop(starImageView)
        setupDragAndDrop(heartImageView)
        setupDragAndDrop(moonImageView)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        videoMediaPlayer.stop()
        videoMediaPlayer.release()
        surfaceView.visibility = SurfaceView.GONE
        backgroundImageView.visibility = ImageView.VISIBLE
        starImageView.visibility = ImageView.VISIBLE
        heartImageView.visibility = ImageView.VISIBLE
        moonImageView.visibility = ImageView.VISIBLE
        playAudio()
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        return false
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        return false
    }

    private fun playAudio() {
        mediaPlayer = MediaPlayer.create(this, R.raw.chicken) // Замените your_audio_file на имя вашего аудиофайла
        mediaPlayer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        if (::videoMediaPlayer.isInitialized) {
            videoMediaPlayer.release()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        videoMediaPlayer.setDisplay(holder)
        videoMediaPlayer.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    private fun setupDragAndDrop(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Начало перетаскивания
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                    v.performClick() // Вызываем performClick для обработки кликов
                }
                MotionEvent.ACTION_MOVE -> {
                    // Перемещение
                    v.x = event.rawX + dX
                    v.y = event.rawY + dY
                }
            }
            true
        }
    }
}
