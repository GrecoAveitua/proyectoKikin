package com.example.karatecompetitionmanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.karatecompetitionmanager.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 segundos

    private ImageView imgLogo;
    private TextView tvTitle;
    private TextView tvSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Inicializar vistas
        imgLogo = findViewById(R.id.splash_logo);
        tvTitle = findViewById(R.id.splash_title);
        tvSubtitle = findViewById(R.id.splash_subtitle);

        // Cargar y aplicar animaciones
        setupAnimations();

        // Navegar a MainActivity después del splash
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Cerrar SplashActivity

                // Animación de transición entre activities
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, SPLASH_DURATION);
    }

    private void setupAnimations() {
        // Animación de fade in para el logo
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(1500);

        // Animación de slide up para el título
        Animation slideUp = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        slideUp.setDuration(1000);
        slideUp.setStartOffset(500);

        // Animación de fade in para el subtítulo
        Animation fadeInSubtitle = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeInSubtitle.setDuration(1000);
        fadeInSubtitle.setStartOffset(1000);

        // Aplicar animaciones
        imgLogo.startAnimation(fadeIn);
        tvTitle.startAnimation(slideUp);
        tvSubtitle.startAnimation(fadeInSubtitle);

        // Animación de rotación para el logo
        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate_logo);
        if (rotate != null) {
            imgLogo.startAnimation(rotate);
        }
    }

    @Override
    public void onBackPressed() {
        // Deshabilitar botón de retroceso en splash screen
        // No hacer nada para evitar que el usuario salga durante el splash
        super.onBackPressed();
    }
}