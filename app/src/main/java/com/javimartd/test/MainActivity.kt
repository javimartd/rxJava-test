package com.javimartd.test

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.javimartd.test.databinding.ActivityMainBinding
import com.javimartd.test.rx.BaseClassesActivity
import com.javimartd.test.rx.OperatorsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBaseClasses.setOnClickListener {
            startActivity(Intent(this, BaseClassesActivity::class.java))
        }
        binding.buttonOperators.setOnClickListener {
            startActivity(Intent(this, OperatorsActivity::class.java))
        }
    }
}