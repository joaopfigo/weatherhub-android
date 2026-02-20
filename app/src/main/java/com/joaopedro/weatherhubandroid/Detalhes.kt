package com.joaopedro.weatherhubandroid

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Detalhes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhes)

        findViewById<Button>(R.id.btnVoltarDetalhes).setOnClickListener {
            finish()
        }
    }
}