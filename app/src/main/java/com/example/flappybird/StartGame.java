package com.example.flappybird;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class StartGame extends Activity {
    GameView gameView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstantState){
        super.onCreate(savedInstantState);
        gameView = new GameView(this);
        setContentView(gameView);
    }

}
