package com.amazon.tv.ftvstylekb

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.Button

@SuppressLint("AppCompatCustomView")
class IconButton : Button {
    private var drawableWidth = 0
    private var drawablePosition: DrawablePositions? = null
    private var iconPadding = 0

    // Cached to prevent allocation during onLayout
    private var bounds: Rect?

    enum class DrawablePositions {
        NONE, LEFT_AND_RIGHT, LEFT, RIGHT
    }

    constructor(context: Context?) : super(context) {
        bounds = Rect()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        bounds = Rect()
        applyAttributes(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        bounds = Rect()
        applyAttributes(attrs)
    }

    private fun applyAttributes(attrs: AttributeSet?) {
        // Slight contortion to prevent allocating in onLayout
        if (null == bounds) {
            bounds = Rect()
        }
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.IconButton)
        val paddingId = typedArray.getDimensionPixelSize(R.styleable.IconButton_iconPadding, 0)
        setIconPadding(paddingId)
        typedArray.recycle()
    }

    private fun setIconPadding(padding: Int) {
        iconPadding = padding
        requestLayout()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val textPaint: Paint = paint
        val text = text.toString()
        textPaint.getTextBounds(text, 0, text.length, bounds)
        val textWidth = bounds!!.width()
        val factor = if (drawablePosition == DrawablePositions.LEFT_AND_RIGHT) 2 else 1
        val contentWidth = drawableWidth + iconPadding * factor + textWidth
        val horizontalPadding: Int
//        if (textWidth > getWidth() / 2) {
//            horizontalPadding = (int) (getResources().getDimension(R.dimen.keyboard_icon_margin) + (getWidth() / 2.0) - (contentWidth / 2.0));
//        } else {
//            horizontalPadding = (int) ((getWidth() / 2.0) - (contentWidth / 2.0));
//        }
        horizontalPadding = (width / 2.0 - contentWidth / 2.0).toInt()
        compoundDrawablePadding = -horizontalPadding + iconPadding
        when (drawablePosition) {
            DrawablePositions.LEFT -> setPadding(horizontalPadding, paddingTop, 0, paddingBottom)
            DrawablePositions.RIGHT -> setPadding(0, paddingTop, horizontalPadding, paddingBottom)
            DrawablePositions.LEFT_AND_RIGHT -> setPadding(horizontalPadding, paddingTop, horizontalPadding, paddingBottom)
            else -> setPadding(0, paddingTop, 0, paddingBottom)
        }
    }

    override fun setCompoundDrawables(left: Drawable?, top: Drawable?, right: Drawable?, bottom: Drawable?) {
        super.setCompoundDrawables(left, top, right, bottom)
        if (left != null && right != null) {
            drawableWidth = left.intrinsicWidth + right.intrinsicWidth
            drawablePosition = DrawablePositions.LEFT_AND_RIGHT
        } else if (left != null) {
            drawableWidth = left.intrinsicWidth
            drawablePosition = DrawablePositions.LEFT
        } else if (right != null) {
            drawableWidth = right.intrinsicWidth
            drawablePosition = DrawablePositions.RIGHT
        } else {
            drawablePosition = DrawablePositions.NONE
        }
        requestLayout()
    }
}