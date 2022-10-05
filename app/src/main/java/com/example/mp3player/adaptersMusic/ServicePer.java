package com.example.mp3player.adaptersMusic;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPoolAdapter;
import com.example.mp3player.MainActivity;

class ServicePer extends AppCompatActivity {

    final String xx = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        BitmapDrawable bitmapDrawable;



        registerForActivityResult(new ActivityResultContracts.RequestPermission(),granted -> {
            if (granted){

            }else {

            }
        });

    }
}
