package good.damn.audiovisualizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import java.util.*
import kotlin.math.abs
import kotlin.math.sin

class BubbleView(context: Context)
    : View(context)
{

    private val TAG = "BubbleView"

    private val mPaint = Paint()
    private val mPaintShiny = Paint()
    private val mRandom = Random()

    private val mRectShiny = RectF()
    private val mRectSin = RectF()
    private val mRectParabola = RectF()
    private val mBubblesRect = LinkedList<Bubble>()

    private var mCycle = 0L

    private var maxOffsetBubble = 0

    private var mWidth4 = 0f
    private var mWidthHalf = 0f
    private var mHeightHalf = 0f
    private var mBubbleBound = 0f

    private var mIsInterrupted = true

    init {
        mPaint.color = 0x86aaaaaa.toInt()
        mPaint.style = Paint.Style.STROKE

        mPaintShiny.color = 0x86aaaaaa.toInt()
        mPaintShiny.style = Paint.Style.STROKE
        mPaintShiny.strokeCap = Paint.Cap.ROUND
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)

        mWidth4 = width * 0.25f
        mWidthHalf = width * 0.5f
        mHeightHalf = height * 0.5f
        mBubbleBound = width * 0.08f
        maxOffsetBubble = (width * 0.05f).toInt()

        val k = if (width > height) height else width

        val sWidth = k * 0.01f
        mPaint.strokeWidth = sWidth
        mPaintShiny.strokeWidth = sWidth

        mRectParabola.right = k * 0.5f
        mRectParabola.bottom = k.toFloat()
    }

    override fun onDraw(
        canvas: Canvas?
    ) {

        super.onDraw(canvas)
        if (canvas == null) {
            return
        }

        if (mBubblesRect.isEmpty()) {
            if (!mIsInterrupted) {
                invalidate()
            }
            return
        }

        mCycle++

        if (mBubblesRect[0].bottom - 1 < 0) {
            Log.d(TAG, "onDraw: REMOVE ELEMENT OUT OF VIEW")
            mBubblesRect.remove()
        }

        mBubblesRect.forEach {
            it.top -= it.speed
            it.bottom -= it.speed

            val w = it.width()

            val arg = (mCycle + it.left) * 0.05f
            val sine = sin(arg)

            val exp = it.amplitude * sine

            mRectSin.left = it.left + exp
            mRectSin.right = it.right + exp

            mRectSin.top = it.top
            mRectSin.bottom = it.bottom

            canvas.drawArc(
                mRectSin,
                0f,
                360f,
                true,
                mPaint
            )

            // with shiny effect (top-right)
            val radSOff = w * 0.27f

            mRectShiny.top = mRectSin.top + radSOff
            mRectShiny.bottom = mRectSin.bottom - radSOff

            mRectShiny.left = mRectSin.left + radSOff
            mRectShiny.right = mRectSin.right - radSOff
            canvas.drawArc(
                mRectShiny,
                270f,
                90f,
                false,
                mPaintShiny
            )
        }

        if (mIsInterrupted) {
            return
        }

        invalidate()
    }

    fun interrupt() {
        mIsInterrupted = true
    }

    fun listen() {
        mIsInterrupted = false
        invalidate()
    }

    fun addBubble(
        normRadius: Float
    ) {

        val rad = abs(normRadius)
        if (!isLaidOut || rad < 0.5f) {
            return
        }

        val r = Bubble()

        val bound = mBubbleBound * rad
        val hb = bound * 0.5f
        val hw = mWidthHalf
        var offsetX = mRandom.nextInt(maxOffsetBubble)

        if (mRandom.nextBoolean()) {
            offsetX = -offsetX
        }

        r.top = height - bound
        r.left = hw - hb + offsetX

        r.bottom = height.toFloat()
        r.right = hw + hb + offsetX

        r.amplitude = mWidth4 * mRandom.nextFloat()
        r.speed = 1 + mRandom.nextFloat() * 10

        if (r.amplitude / r.speed > 2) {
            r.amplitude /= 2
        }

        mBubblesRect.add(r)
    }
}