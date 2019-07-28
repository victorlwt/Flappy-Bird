package com.example.flappybird;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

public class GameView extends View {
//    This is the custom view class
    Handler handler;    //Used to schedule a runnable after delay
    Runnable runnable;
    final int UPDATE_MILLIS = 30;
    Bitmap background, top_pipe, bottom_pipe;
    Display display;
    Point point;
    int dWidth, dHeight;    //Get device's width and height
    Rect rect;
//    A bitmap array for bird
    Bitmap[] birds;
    int birdFrame = 0;  //Tracking bird's action
    int birdX, birdY;    //Tracking bird's position
    int velocity = 0, gravity = 3;
    boolean gameState = false;
    int gap = 400;      //Gap between top pipe and bottom pipe
    int minOffset, maxOffset;
    int num_pipes = 4;
    int pipe_velocity = 8;
    int pipe_distance;
    int[] pipeX = new int[num_pipes];
    int[] pipeY_top = new int[num_pipes];
    Random random;
    int score = 0;
    int cur_pipe = 0;   //Tracking the current pipe

    public GameView(Context context){
        super(context);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                invalidate();//This will call onDraw
            }
        };

        background = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        bottom_pipe = BitmapFactory.decodeResource(getResources(), R.drawable.pipe);
        Matrix r = new Matrix();
        r.postRotate(180);
        top_pipe = Bitmap.createBitmap(bottom_pipe, 0, 0, bottom_pipe.getWidth(), bottom_pipe.getHeight(), r, true);
        display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        point = new Point();
        display.getSize(point);
        dWidth = point.x;
        dHeight = point.y;
        rect = new Rect(0, 0, dWidth, dHeight);
        birds = new Bitmap[3];
        Bitmap bird_base = BitmapFactory.decodeResource(getResources(), R.drawable.bird);
        birds[0] = Bitmap.createBitmap(bird_base, 0, 0, bird_base.getWidth()/3, bird_base.getHeight());
        birds[1] = Bitmap.createBitmap(bird_base, bird_base.getWidth()/3, 0, bird_base.getWidth()/3, bird_base.getHeight());
        birds[2] = Bitmap.createBitmap(bird_base, bird_base.getWidth()*2/3, 0, bird_base.getWidth()/3, bird_base.getHeight());
        birdX = dWidth/2 - birds[0].getWidth()/2;
        birdY = dHeight/2 - birds[0].getHeight()/2;
        pipe_distance = dWidth*3/4;
        minOffset = gap/2;
        maxOffset = dHeight - minOffset - gap;
        random = new Random();
        for(int i=0;i<num_pipes; i++) {
            pipeX[i] = dWidth + i*pipe_distance;
            pipeY_top[i] = minOffset + random.nextInt(maxOffset-minOffset+1);
        }
    }

    public void show_score(Canvas canvas){
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 40, getResources().getDisplayMetrics()));
        paint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics metric = paint.getFontMetrics();
        int textHeight = (int) Math.ceil(metric.descent - metric.ascent);
        int y = (int)(textHeight - metric.descent);
        String s = Integer.toString(score);
        canvas.drawText(s, dWidth/2-s.length()/2, y, paint);
    }
    public void restart(){
        score = 0;
        cur_pipe = 0;
        birdX = dWidth/2 - birds[0].getWidth()/2;
        birdY = dHeight/2 - birds[0].getHeight()/2;
        for(int i=0;i<num_pipes; i++) {
            pipeX[i] = dWidth + i*pipe_distance;
            pipeY_top[i] = minOffset + random.nextInt(maxOffset-minOffset+1);
        }
    }

    public boolean check_collision(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2){
        if(y2 > y1+h1 || y2+h2 < y1 || x2 > x1+w1 || x2+w2 <x1){
            return false;
        }
        return true;
    }
    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
//        We draw our view here
//        Draw the background on canvas
        canvas.drawBitmap(background, null, rect, null);
        if(birdFrame >= 2) {
            birdFrame = 0;
        }
        if (gameState) {
            birdFrame++;
//          Check if score should be added
            if(birdX > pipeX[cur_pipe]+top_pipe.getWidth()){
                score++;
                cur_pipe++;
                if(cur_pipe > num_pipes-1){
                    cur_pipe = 0;
                }
            }
//          Check if the bird is above the bottom
            if (birdY < dHeight - birds[0].getHeight() || velocity < 0) {
                velocity += gravity;
                birdY += velocity;
            }
            for(int i=0;i<num_pipes; i++) {
                pipeX[i] -= pipe_velocity;
                if (pipeX[i] < -top_pipe.getWidth()) {
                    pipeX[i] += num_pipes * pipe_distance;
                    pipeY_top[i] = minOffset + random.nextInt(maxOffset - minOffset + 1);
                }
                canvas.drawBitmap(top_pipe, pipeX[i], pipeY_top[i] - top_pipe.getHeight(), null);
                canvas.drawBitmap(bottom_pipe, pipeX[i], pipeY_top[i] + gap, null);
                if (check_collision(birdX, birdY, birds[0].getWidth(), birds[0].getHeight(), pipeX[i], pipeY_top[i] - top_pipe.getHeight(), top_pipe.getWidth(), top_pipe.getHeight())) {
                    gameState = false;
                }
                if (check_collision(birdX, birdY, birds[0].getWidth(), birds[0].getHeight(), pipeX[i], pipeY_top[i] + gap, top_pipe.getWidth(), top_pipe.getHeight())) {
                    gameState = false;
                }
            }
        }
        canvas.drawBitmap(birds[birdFrame], birdX, birdY, null);
        show_score(canvas);
        handler.postDelayed(runnable, UPDATE_MILLIS);
    }
//    Touch event
    @Override
    public boolean onTouchEvent(MotionEvent event){
        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN){
            //Move the bird upward
            velocity = -30;
            if(!gameState) {
                restart();
                gameState = true;
            }
        }

        return true;//Return true to tell the action is done and no further actions is needed
    }
}
