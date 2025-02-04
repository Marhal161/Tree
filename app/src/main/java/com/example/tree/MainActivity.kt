package com.example.tree

import android.graphics.Rect
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
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random

class MainActivity : AppCompatActivity(), GestureDetector.OnDoubleTapListener, SurfaceHolder.Callback, SensorEventListener {

    private lateinit var surfaceView: SurfaceView
    private lateinit var backgroundImageView: ImageView
    private lateinit var starImageView: ImageView
    private lateinit var heartImageView: ImageView
    private lateinit var moonImageView: ImageView
    private lateinit var gestureDetector: GestureDetector
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var videoMediaPlayer: MediaPlayer
    private lateinit var sensorManager: SensorManager
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f

    private var dX: Float = 0f
    private var dY: Float = 0f

    private val items = mutableListOf<ImageView>()
    private val maxItems = 13

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

        // Инициализируем SensorManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)

        // Устанавливаем обработчики нажатий для кнопок
        starImageView.setOnClickListener {
            createAndDropItem(R.drawable.star)
        }
        heartImageView.setOnClickListener {
            createAndDropItem(R.drawable.heart)
        }
        moonImageView.setOnClickListener {
            createAndDropItem(R.drawable.moon)
        }
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
        sensorManager.unregisterListener(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        videoMediaPlayer.setDisplay(holder)
        videoMediaPlayer.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration
            currentAcceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            if (acceleration > 12) {
                dropRandomItems()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun dropRandomItems() {
        val random = Random
        val starCount = random.nextInt(4)
        val heartCount = random.nextInt(4)
        val moonCount = random.nextInt(4)

        repeat(starCount) {
            createAndDropItem(R.drawable.star)
        }
        repeat(heartCount) {
            createAndDropItem(R.drawable.heart)
        }
        repeat(moonCount) {
            createAndDropItem(R.drawable.moon)
        }
    }

    private fun createAndDropItem(drawableRes: Int) {
        if (items.size >= maxItems) {
            // Удаляем самую старую фигуру
            val oldestItem = items.removeAt(0)
            val container = findViewById<ConstraintLayout>(R.id.main)
            container.removeView(oldestItem)
        }

        val imageView = ImageView(this)
        imageView.setImageResource(drawableRes)
        imageView.scaleType = ImageView.ScaleType.FIT_XY
        val layoutParams = ConstraintLayout.LayoutParams(100, 100)
        imageView.layoutParams = layoutParams
        val container = findViewById<ConstraintLayout>(R.id.main)
        container.addView(imageView)

        // Устанавливаем elevation для нового объекта, чтобы он отображался поверх предыдущих
        imageView.elevation = 10f * container.childCount

        val randomX = Random.nextInt(backgroundImageView.width - 100)
        imageView.x = randomX.toFloat()
        imageView.y = -100f

        items.add(imageView)
        startFallAnimation(imageView)
        setupDragAndDrop(imageView)
    }

    private fun startFallAnimation(view: View) {
        val targetY = backgroundImageView.height.toFloat() - view.height.toFloat()
        val animator = ValueAnimator.ofFloat(view.y, targetY)
        animator.duration = 2000 // Длительность анимации в миллисекундах
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            view.y = value
            checkCollision(view)
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // После завершения анимации можно сделать что-то еще, например, сбросить позицию объекта
                // view.y = 0f // Сброс позиции объекта (если нужно)
            }
        })
        animator.start()
    }

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
                    checkCollision(v)
                }
                MotionEvent.ACTION_UP -> {
                    // Отпускание, начинаем анимацию падения
                    startFallAnimation(v)
                }
            }
            true
        }
    }

    private fun checkCollision(view: View) {
        for (otherView in items) {
            if (view != otherView && areViewsColliding(view, otherView)) {
                resolveCollision(view, otherView)
            }
        }
        // Проверяем, чтобы объект не выходил за пределы экрана
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

    private fun areViewsColliding(view1: View, view2: View): Boolean {
        val rect1 = Rect()
        view1.getHitRect(rect1)
        val rect2 = Rect()
        view2.getHitRect(rect2)
        return Rect.intersects(rect1, rect2)
    }

    private fun resolveCollision(view1: View, view2: View) {
        // Простое разрешение коллизии: сдвигаем объекты в противоположные стороны
        val dx = view1.x - view2.x
        val dy = view1.y - view2.y
        val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        if (distance == 0f) return

        val overlap = (view1.width + view2.width) / 2f - distance
        val moveX = overlap * (dx / distance) / 2f
        val moveY = overlap * (dy / distance) / 2f

        // Сдвигаем объекты в противоположные стороны
        view1.x += moveX
        view1.y += moveY
        view2.x -= moveX
        view2.y -= moveY

        // Проверяем, чтобы объекты не выходили за пределы экрана
        if (view1.y + view1.height > backgroundImageView.height) {
            view1.y = (backgroundImageView.height - view1.height).toFloat()
        }
        if (view2.y + view2.height > backgroundImageView.height) {
            view2.y = (backgroundImageView.height - view2.height).toFloat()
        }
        if (view1.x < 0) {
            view1.x = 0f
        }
        if (view1.x + view1.width > backgroundImageView.width) {
            view1.x = (backgroundImageView.width - view1.width).toFloat()
        }
        if (view2.x < 0) {
            view2.x = 0f
        }
        if (view2.x + view2.width > backgroundImageView.width) {
            view2.x = (backgroundImageView.width - view2.width).toFloat()
        }
    }
}
