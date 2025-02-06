package com.example.tree

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import com.bumptech.glide.Glide
import android.net.Uri
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity(), GestureDetector.OnDoubleTapListener, SurfaceHolder.Callback, SensorEventListener {

    private lateinit var surfaceView: SurfaceView
    private lateinit var backgroundImageView: ImageView
    private lateinit var starImageView: ImageView
    private lateinit var heartImageView: ImageView
    private lateinit var moonImageView: ImageView
    private lateinit var gestureDetector: GestureDetector
    private var mediaPlayer: MediaPlayer? = null
    private var videoMediaPlayer: MediaPlayer? = null
    private lateinit var sensorManager: SensorManager
    private val handler = Handler(Looper.getMainLooper())
    private val restartAudioRunnable = Runnable { playAudio() }

    private var hungItemsCount = 0

    private var dX: Float = 0f
    private var dY: Float = 0f
    private var isVideoCompleted: Boolean = false

    private val items = mutableListOf<ImageView>()
    private val maxItems = 13

    // Список всех зон для перетаскивания
    private val dropZones: List<View> by lazy {
        listOf(
            findViewById(R.id.dropZone1),
            findViewById(R.id.dropZone2),
            findViewById(R.id.dropZone3),
            findViewById(R.id.dropZone4),
            findViewById(R.id.dropZone5),
            findViewById(R.id.dropZone6),
            findViewById(R.id.dropZone7),
            findViewById(R.id.dropZone8),
            findViewById(R.id.dropZone9),
            findViewById(R.id.dropZone10),
            findViewById(R.id.dropZone11),
            findViewById(R.id.dropZone12),
            findViewById(R.id.dropZone13),
        )
    }

    // Список для отслеживания состояния каждой dropZone
    private val dropZoneOccupied = mutableListOf<Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        surfaceView = findViewById(R.id.surfaceView)
        backgroundImageView = findViewById(R.id.backgroundImageView)
        starImageView = findViewById(R.id.starImageView)
        heartImageView = findViewById(R.id.heartImageView)
        moonImageView = findViewById(R.id.moonImageView)
        val showMessageButton: Button = findViewById(R.id.showMessageButton)

        val surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(this)

        val videoPath = "android.resource://" + packageName + "/" + R.raw.intro
        val uri: Uri = Uri.parse(videoPath)
        videoMediaPlayer = MediaPlayer.create(this, uri)
        videoMediaPlayer?.setOnCompletionListener {
            Log.d("VideoPlayer", "Video completed") // Логирование завершения видео
            isVideoCompleted = true
            surfaceView.visibility = SurfaceView.GONE
            backgroundImageView.visibility = ImageView.VISIBLE
            starImageView.visibility = ImageView.VISIBLE
            heartImageView.visibility = ImageView.VISIBLE
            moonImageView.visibility = ImageView.VISIBLE
            runOnUiThread {
                showMessageButton.visibility = Button.VISIBLE // Делаем кнопку видимой в основном потоке
            }
            playAudio()
        }

        Glide.with(this)
            .load(R.drawable.tree)
            .into(backgroundImageView)

        backgroundImageView.scaleType = ImageView.ScaleType.FIT_XY

        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (!isVideoCompleted && videoMediaPlayer?.isPlaying == true) {
                    stopVideo()
                    runOnUiThread {
                        showMessageButton.visibility = Button.VISIBLE // Делаем кнопку видимой в основном потоке
                    }
                }
                return true
            }
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)

        starImageView.setOnClickListener {
            createAndDropItem(R.drawable.star)
        }
        heartImageView.setOnClickListener {
            createAndDropItem(R.drawable.heart)
        }
        moonImageView.setOnClickListener {
            createAndDropItem(R.drawable.moon)
        }
        // Инициализация списка состояния dropZone
        dropZones.forEach {
            dropZoneOccupied.add(false)
        }

        // Установка обработчика нажатия на кнопку
        showMessageButton.setOnClickListener {
            checkAndShowMessage()
        }
    }


    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause() // Останавливаем воспроизведение музыки
    }

    override fun onResume() {
        super.onResume()
        if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
            mediaPlayer?.start() // Возобновляем воспроизведение музыки
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        if (!isVideoCompleted && videoMediaPlayer?.isPlaying == true) {
            stopVideo()
        }
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        return false
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        return false
    }

    private fun stopVideo() {
        videoMediaPlayer?.stop()
        videoMediaPlayer?.release()
        videoMediaPlayer = null
        surfaceView.visibility = SurfaceView.GONE
        backgroundImageView.visibility = ImageView.VISIBLE
        starImageView.visibility = ImageView.VISIBLE
        heartImageView.visibility = ImageView.VISIBLE
        moonImageView.visibility = ImageView.VISIBLE
        playAudio()
    }

    private fun playAudio() {
        mediaPlayer = MediaPlayer.create(this, R.raw.chicken)
        mediaPlayer?.setOnCompletionListener {
            handler.postDelayed(restartAudioRunnable, 1000) // Задержка в 1 секунду перед повторным воспроизведением
        }
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        videoMediaPlayer?.release()
        videoMediaPlayer = null
        sensorManager.unregisterListener(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        videoMediaPlayer?.setDisplay(holder)
        videoMediaPlayer?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    override fun onSensorChanged(event: SensorEvent) {}

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun startFallAnimation(view: View) {
        val targetY = backgroundImageView.height.toFloat() - view.height.toFloat()
        val animator = ValueAnimator.ofFloat(view.y, targetY)
        animator.duration = 2000
        animator.interpolator = AccelerateInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            view.y = value
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                ensureViewIsOnGround(view)
                // Обновляем состояние зоны сброса при завершении анимации падения
                updateDropZoneState(view, false)
            }
        })
        view.tag = animator
        animator.start()
    }

    private fun createAndDropItem(drawableRes: Int) {
        if (items.size >= maxItems) {
            val oldestItem = items.removeAt(0)
            val container = findViewById<ConstraintLayout>(R.id.main)
            container.removeView(oldestItem)
            updateDropZoneState(oldestItem, false)
        }

        val imageView = ImageView(this).apply {
            setImageResource(drawableRes)
            scaleType = ImageView.ScaleType.FIT_XY
            layoutParams = ConstraintLayout.LayoutParams(100, 100)
            elevation = 10f * (items.size + 1)
            x = Random.nextInt(backgroundImageView.width - 100).toFloat()
            y = -100f
        }

        val container = findViewById<ConstraintLayout>(R.id.main)
        container.addView(imageView)

        items.add(imageView)
        startFallAnimation(imageView)
        setupDragAndDrop(imageView)
    }

    private fun setupDragAndDrop(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                    v.performClick()
                    (v.tag as? Animator)?.cancel()
                    // Обновляем состояние зоны сброса при начале перемещения
                    updateDropZoneState(v, false)
                }
                MotionEvent.ACTION_MOVE -> {
                    v.x = event.rawX + dX
                    v.y = event.rawY + dY
                }
                MotionEvent.ACTION_UP -> {
                    val dropZone = dropZones.find { isViewInDropZone(v, it) }
                    if (dropZone != null) {
                        val index = dropZones.indexOf(dropZone)
                        if (!dropZoneOccupied[index]) {
                            v.x = dropZone.x + (dropZone.width - v.width) / 2
                            v.y = dropZone.y + (dropZone.height - v.height) / 2
                            dropZoneOccupied[index] = true
                        } else {
                            startFallAnimation(v)
                        }
                    } else {
                        startFallAnimation(v)
                    }
                    // Обновляем состояние зоны сброса при завершении перемещения
                    updateDropZoneState(v, true)
                }
            }
            true
        }
    }

    private fun isViewInDropZone(view: View, dropZone: View): Boolean {
        val viewRect = android.graphics.Rect()
        view.getHitRect(viewRect)

        val dropZoneRect = android.graphics.Rect()
        dropZone.getHitRect(dropZoneRect)

        return viewRect.intersect(dropZoneRect)
    }

    private fun ensureViewIsOnGround(view: View) {
        if (view.y + view.height > backgroundImageView.height) {
            view.y = (backgroundImageView.height - view.height).toFloat()
        }
        if (view.x < 0) {
            view.x = 0f
        }
        if (view.x + view.width > backgroundImageView.width) {
            view.x = (backgroundImageView.width - view.width).toFloat()
        }
    }

    private fun updateDropZoneState(view: View, isOccupied: Boolean) {
        val dropZone = dropZones.find { isViewInDropZone(view, it) }
        if (dropZone != null) {
            val index = dropZones.indexOf(dropZone)
            dropZoneOccupied[index] = isOccupied
        }
    }

    private fun showHelloWorldMessage(message: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog, null)
        val messageTextView = dialogView.findViewById<TextView>(R.id.messageTextView)
        val okButton = dialogView.findViewById<Button>(R.id.okButton)

        messageTextView.text = message

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        okButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun checkAndShowMessage() {
        val hungItemsCount = items.count { item ->
            dropZones.any { dropZone -> isViewInDropZone(item, dropZone) }
        }

        val message = when {
            hungItemsCount <= 3 -> "Ты можешь лучше"
            hungItemsCount in 4..8 -> "Ты красава"
            else -> "ТЫ перегибаешь"
        }
        showHelloWorldMessage(message)
    }

}
