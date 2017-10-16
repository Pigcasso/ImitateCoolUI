package io.julian.imitate.jikelike.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import io.julian.imitate.jikelike.R;
import io.julian.imitate.jikelike.util.DimenUtils;

import static android.text.TextUtils.isEmpty;

/**
 * @author Zhu Liang
 */

public class PopTextView extends View {

    private static final String TAG = "PopTextView";

    private static final int LETTER_SPACING = DimenUtils.spTopx(4);
    private static final int VERTICAL_SPACING = DimenUtils.spTopx(10);
    private static final int TEXT_COLOR_ALPHA = 255;
    private static final int TEXT_COLOR_R = 0;
    private static final int TEXT_COLOR_G = 0;
    private static final int TEXT_COLOR_B = 0;

    // 相同部分的文字
    private String mSameText;
    // 不同部分的文字(当前)
    private String mBeforeDiffText;
    // 不同部分的文字(最终)
    private String mAfterDiffText;

    private int mBeforeNum;
    private int mAfterNum;

    private Paint mSameTextPaint;
    private Paint mBeforeDiffTextPaint;
    private Paint mAfterDiffTextPaint;

    private Rect mSameTextRect;
    private Rect mBeforeDiffTextRect;
    private Rect mAfterDiffTextRect;

    private int mDistance;

    private float mRatio;

    public PopTextView(Context context) {
        this(context, null);
    }

    public PopTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PopTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mSameTextPaint = new Paint();
        mSameTextPaint.setAntiAlias(true);
        mSameTextPaint.setTextSize(DimenUtils.spTopx(20));
        mSameTextPaint.setColor(Color.argb(TEXT_COLOR_ALPHA, TEXT_COLOR_R, TEXT_COLOR_G, TEXT_COLOR_B));
        mBeforeDiffTextPaint = new Paint(mSameTextPaint);
        mAfterDiffTextPaint = new Paint(mSameTextPaint);

        mSameTextRect = new Rect();
        mBeforeDiffTextRect = new Rect(mSameTextRect);
        mAfterDiffTextRect = new Rect(mSameTextRect);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PopTextView);
        try {
            int beforeNum = ta.getInteger(R.styleable.PopTextView_beforeNum, 0);
            int afterNum = ta.getInteger(R.styleable.PopTextView_afterNum, 0);
            handleNumber(beforeNum, afterNum);
        } finally {
            ta.recycle();
        }
    }

    private void handleNumber(int beforeNum, int afterNum) {
        if (beforeNum < 0) {
            throw new IllegalStateException("Before number must be positive");
        }
        if (afterNum < 0) {
            throw new IllegalStateException("After number must be positive");
        }
        String beforeText = Integer.toString(beforeNum);
        String afterText = Integer.toString(afterNum);
        String same = "";
        int beforeLength = beforeText.length();
        for (int i = 0; i < beforeLength; i++) {
            char c = beforeText.charAt(i);
            if (c == afterText.charAt(i)) {
                same += c;
            } else {
                break;
            }
        }
        mBeforeNum = beforeNum;
        mAfterNum = afterNum;
        mSameText = same;
        mBeforeDiffText = beforeText.substring(same.length());
        mAfterDiffText = afterText.substring(same.length());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (!isEmpty(mSameText)) {
            mSameTextPaint.getTextBounds(mSameText, 0, mSameText.length(), mSameTextRect);
        }
        if (!isEmpty(mBeforeDiffText)) {
            mBeforeDiffTextPaint.getTextBounds(mBeforeDiffText, 0, mBeforeDiffText.length(),
                    mBeforeDiffTextRect);
            mDistance = mBeforeDiffTextRect.height() + VERTICAL_SPACING;
        }
        if (!isEmpty(mAfterDiffText)) {
            mAfterDiffTextPaint.getTextBounds(mAfterDiffText, 0, mAfterDiffText.length(),
                    mAfterDiffTextRect);
        }
        final int widthSize;
        final int heightSize;
        if (widthMode == MeasureSpec.AT_MOST) {
            int diffWidth = Math.max(mBeforeDiffTextRect.width(), mAfterDiffTextRect.width());
            widthSize = getPaddingLeft() + getPaddingRight() + mSameTextRect.width()
                    + (diffWidth > 0 ? diffWidth + LETTER_SPACING : 0);
        } else {
            widthSize = MeasureSpec.getSize(widthMeasureSpec);
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            int height = Math.max(mSameTextRect.height(), mBeforeDiffTextRect.height());
            height = Math.max(height, mAfterDiffTextRect.height());
            heightSize = getPaddingTop() + getPaddingBottom() + height;
        } else {
            heightSize = MeasureSpec.getSize(heightMeasureSpec);
        }
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int heightSize = getMeasuredHeight();

        final int pleft = getPaddingLeft();
        final int ptop = getPaddingTop();
        final int pbottom = getPaddingBottom();

        final int alpha = (int) (mRatio * 255);
        if (!isEmpty(mSameText)) {
            final int x = pleft;
            final int y = (heightSize - ptop - pbottom + mSameTextRect.height()) / 2;
            canvas.drawText(mSameText, x, y, mSameTextPaint);
        }
        if (mAfterNum > mBeforeNum) {
            final int offsetY = (int) (mDistance * mRatio);
            if (!isEmpty(mBeforeDiffText)) {
                final int x = pleft + mSameTextRect.width() + LETTER_SPACING;
                final int y = (heightSize - ptop - pbottom + mBeforeDiffTextRect.height()) / 2;
                mBeforeDiffTextPaint.setColor(Color.argb(TEXT_COLOR_ALPHA - alpha,
                        TEXT_COLOR_R, TEXT_COLOR_G, TEXT_COLOR_B));
                canvas.drawText(mBeforeDiffText, x, y - offsetY, mBeforeDiffTextPaint);
            }
            if (!isEmpty(mAfterDiffText)) {
                final int x = pleft + mSameTextRect.width() + LETTER_SPACING;
                final int y = (heightSize - ptop - pbottom + mBeforeDiffTextRect.height()) / 2
                        + mAfterDiffTextRect.height() + VERTICAL_SPACING;
                mAfterDiffTextPaint.setColor(Color.argb(alpha, TEXT_COLOR_R, TEXT_COLOR_G,
                        TEXT_COLOR_B));
                canvas.drawText(mAfterDiffText, x, y - offsetY, mAfterDiffTextPaint);
            }
        } else if (mAfterNum < mBeforeNum) {
            final int offsetY = (int) (mDistance * mRatio);
            if (!isEmpty(mBeforeDiffText)) {
                final int x = pleft + mSameTextRect.width() + LETTER_SPACING;
                final int y = (heightSize - ptop - pbottom + mBeforeDiffTextRect.height()) / 2;
                mBeforeDiffTextPaint.setColor(Color.argb(TEXT_COLOR_ALPHA - alpha,
                        TEXT_COLOR_R, TEXT_COLOR_G, TEXT_COLOR_B));
                canvas.drawText(mBeforeDiffText, x, y + offsetY, mBeforeDiffTextPaint);
            }
            if (!isEmpty(mAfterDiffText)) {
                final int x = pleft + mSameTextRect.width() + LETTER_SPACING;
                final int y = (heightSize - ptop - pbottom + mBeforeDiffTextRect.height()) / 2 - mBeforeDiffTextRect.height() - VERTICAL_SPACING;
                mAfterDiffTextPaint.setColor(Color.argb(alpha, TEXT_COLOR_R, TEXT_COLOR_G,
                        TEXT_COLOR_B));
                canvas.drawText(mAfterDiffText, x, y + offsetY, mAfterDiffTextPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            animatePop();
        }
        return true;
    }

    private void animatePop() {
        if (isEmpty(mBeforeDiffText) || isEmpty(mAfterDiffText)) return;
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1.0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRatio = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.setDuration(1000);
        animator.start();
    }
}