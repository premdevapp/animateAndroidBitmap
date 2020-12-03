package com.example.myrunninganimate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {
    //instances
    private Thread gameThread;
    private SurfaceHolder ourHolder;
    private volatile boolean isPlaying;
    private Canvas canvas;
    private Bitmap bitmapRunning;
    private boolean isMoving;
    private float runSpeedPerSecond = 500;
    private float manXPos = 10, manYPos = 10;
    private int frameWidth = 230, frameHeight = 274;
    private int frameCount = 8, currentFrame = 0;
    private long fps;
    private long timeThisFrame;
    private long lastFrameChaneTime = 0;
    private int frmeLenInMilliSec = 50;
    // rect pos
    private Rect frameToDraw = new Rect(0,0,frameWidth, frameHeight);
    private RectF whereToDraw = new RectF(manXPos, manXPos, manXPos + frameWidth, frameHeight);

    public GameView(Context context) {
        super(context);
        ourHolder = getHolder();
        bitmapRunning = BitmapFactory.decodeResource(getResources(), R.drawable.running_man);
        bitmapRunning = Bitmap.createScaledBitmap(bitmapRunning, frameWidth*frameCount, frameHeight, false);

    }

    @Override
    public void run() {
        while (isPlaying){
            long startFrameTime = System.currentTimeMillis();
            update();
            draw();
            timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1){
                fps = 1000 / timeThisFrame;
            }
        }

    }

    private void update() {
        if (isMoving) {
            manXPos = manXPos + runSpeedPerSecond / fps;

            if (manXPos > getWidth()) {
                manYPos += (int) frameHeight;
                manXPos = 10;
            }

            if (manYPos + frameHeight > getHeight()) {
                manYPos = 10;
            }
        }
    }

    public void manageCurrentFrame() {
        long time = System.currentTimeMillis();

        if (isMoving) {
            if (time > lastFrameChaneTime + frmeLenInMilliSec) {
                lastFrameChaneTime = time;
                currentFrame++;

                if (currentFrame >= frameCount) {
                    currentFrame = 0;
                }
            }
        }

        frameToDraw.left = currentFrame * frameWidth;
        frameToDraw.right = frameToDraw.left + frameWidth;
    }

    public void draw() {
        if (ourHolder.getSurface().isValid()) {
            canvas = ourHolder.lockCanvas();
            canvas.drawColor(Color.WHITE);
            whereToDraw.set((int) manXPos, (int) manYPos, (int) manXPos
                    + frameWidth, (int) manYPos + frameHeight);
            manageCurrentFrame();
            canvas.drawBitmap(bitmapRunning, frameToDraw, whereToDraw, null);
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause() {
        isPlaying = false;

        try {
            gameThread.join();
        } catch(InterruptedException e) {
            Log.e("ERR", "Joining Thread");
        }
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
            isMoving = !isMoving;
        }

        return true;
    }
}
