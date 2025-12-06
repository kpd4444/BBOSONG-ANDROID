package com.cookandroid.ai_landaury.camera;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

public class LoadingSpinnerView extends View {

    private static final int BAR_COUNT = 7;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 색상 배열 (TSX에서 쓰던 그라데이션 느낌 유지)
    private final int[] colors = new int[]{
            0xFF2551A8,
            0xFF2D5FCA,
            0xFF346EEC,
            0xFF4C87FA,
            0xFF6EA2FB,
            0xFFC0DAFD,
            0xFFC0DAFD
    };

    public LoadingSpinnerView(Context context) {
        super(context);
        init();
    }

    public LoadingSpinnerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadingSpinnerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND); // 끝을 둥글게
        paint.setAntiAlias(true);

        // 회전 애니메이션 (2.4s, 시계 반대 방향)
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "rotation", 0f, -360f);
        animator.setDuration(2400);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.start();
    }

    private float dp(float value) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        canvas.save();
        canvas.translate(cx, cy);

        // 막대 공통 설정 (길이/두께 모두 동일)
        float barThickness = dp(10);     // 막대 두께
        float barLength    = dp(28);     // 막대 길이
        float radius       = dp(40);     // 원 반지름 (막대가 놓일 위치)

        paint.setStrokeWidth(barThickness);

        float angleStep = 360f / BAR_COUNT;

        for (int i = 0; i < BAR_COUNT; i++) {
            paint.setColor(colors[i]);

            canvas.save();
            canvas.rotate(angleStep * i);

            // (0, -radius - barLength) 부터 (0, -radius) 까지 수직선 하나
            float startY = -(radius + barLength);
            float endY   = -radius;
            canvas.drawLine(0, startY, 0, endY, paint);

            canvas.restore();
        }

        canvas.restore();
    }

    // 뷰를 정사각형으로 맞춰주면 더 덜 일그러져 보여서 추가
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = Math.min(
                MeasureSpec.getSize(widthMeasureSpec - 10),
                MeasureSpec.getSize(heightMeasureSpec - 10)
        );
        int spec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        super.onMeasure(spec, spec);
    }
}