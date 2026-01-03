package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {
    private Paint basePaint, stickPaint;
    private float centerX, centerY, baseRadius, stickRadius;
    private float stickX, stickY;

    // Интерфейс для передачи данных в MainActivity
    public interface JoystickListener {
        void onJoystickMoved(float xPercent, float yPercent);
    }
    private JoystickListener listener;

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        basePaint = new Paint();
        basePaint.setColor(Color.DKGRAY);
        basePaint.setStyle(Paint.Style.FILL);

        stickPaint = new Paint();
        stickPaint.setColor(Color.LTGRAY);
        stickPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        centerX = w / 2f;
        centerY = h / 2f;
        baseRadius = Math.min(w, h) / 3f;
        stickRadius = baseRadius / 8f;
        stickX = centerX;
        stickY = centerY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(centerX, centerY, baseRadius, basePaint);
        canvas.drawCircle(stickX, stickY, stickRadius, stickPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = event.getX() - centerX;
            float dy = event.getY() - centerY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < baseRadius) {
                stickX = event.getX();
                stickY = event.getY();
            } else {
                // Ограничиваем движение стика пределами круга
                float ratio = baseRadius / distance;
                stickX = centerX + dx * ratio;
                stickY = centerY + dy * ratio;
            }
        } else {
            // Возвращаем в центр при отпускании
            stickX = centerX;
            stickY = centerY;
        }

        invalidate(); // Перерисовать экран

        // Вычисляем проценты (-1.0 до 1.0) для передачи в C++
        if (listener != null) {
            float xPct = (stickX - centerX) / baseRadius;
            float yPct = (stickY - centerY) / baseRadius;
            listener.onJoystickMoved(xPct, yPct);
        }
        return true;
    }

    public void setJoystickListener(JoystickListener listener) {
        this.listener = listener;
    }
}