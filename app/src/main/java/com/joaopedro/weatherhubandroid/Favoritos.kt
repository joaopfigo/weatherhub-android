package com.joaopedro.weatherhubandroid

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Favoritos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favoritos)

        findViewById<Button>(R.id.btnVoltarFavoritos).setOnClickListener {
            finish()
        }
    }
}