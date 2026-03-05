package com.example.notesai

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val drawPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val currentPath = Path()
    private val paths = mutableListOf<Pair<Path, Paint>>()
    private val redoStack = mutableListOf<Pair<Path, Paint>>()

    var onPageTouched: (() -> Unit)? = null

    private var lastX = 0f
    private var lastY = 0f
    private val tolerance = 4f

    init {
        isFocusable = true
        isFocusableInTouchMode = true

        // 🔴 REQUIRED FOR ERASER
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for ((p, paint) in paths) {
            canvas.drawPath(p, paint)
        }
        canvas.drawPath(currentPath, drawPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val pointerCount = event.pointerCount

        // Allow scrolling only if two fingers
        if (pointerCount >= 2) {
            parent?.requestDisallowInterceptTouchEvent(false)
            return false
        }

        parent?.requestDisallowInterceptTouchEvent(true)
        onPageTouched?.invoke()

        val x = event.x
        val y = event.y

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                currentPath.moveTo(x, y)
                lastX = x
                lastY = y
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = abs(x - lastX)
                val dy = abs(y - lastY)

                if (dx >= tolerance || dy >= tolerance) {
                    currentPath.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
                    lastX = x
                    lastY = y
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                paths.add(Path(currentPath) to Paint(drawPaint))
                currentPath.reset()
                redoStack.clear()
            }
        }

        invalidate()
        return true
    }

    // =======================
    // ✏️ CONTROLS
    // =======================

    fun setPenColor(color: Int) {
        drawPaint.color = color
        drawPaint.xfermode = null
    }

    fun setPenSize(size: Float) {
        drawPaint.strokeWidth = size
    }

    fun getPenSize(): Float = drawPaint.strokeWidth

    fun enableEraser(size: Float) {
        drawPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        drawPaint.strokeWidth = size
    }

    fun disableEraser() {
        drawPaint.xfermode = null
    }

    fun undo() {
        if (paths.isNotEmpty()) {
            redoStack.add(paths.removeAt(paths.lastIndex))
            invalidate()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            paths.add(redoStack.removeAt(redoStack.lastIndex))
            invalidate()
        }
    }

    fun clear() {
        paths.clear()
        redoStack.clear()
        invalidate()
    }
}