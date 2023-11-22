package com.example.mylife

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
//BottomNavigationView 기능
class MainActivity : AppCompatActivity() {


    private val fl: FrameLayout by lazy {
        findViewById(R.id.fl_container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bnv_main = findViewById<BottomNavigationView>(R.id.bnv_main)
        bnv_main.setOnItemSelectedListener { item ->
            changeFragment(
                when (item.itemId) {
                    R.id.first -> { //날씨 및 뉴스 (리사이클 뷰)
                        bnv_main.itemIconTintList =
                            ContextCompat.getColorStateList(this, R.color.white)
                        bnv_main.itemTextColor =
                            ContextCompat.getColorStateList(this, R.color.black)
                        Home()
                    }
                    R.id.second -> { //서울역 탑승 수 (그래프)
                        bnv_main.itemIconTintList =
                            ContextCompat.getColorStateList(this, R.color.white)
                        bnv_main.itemTextColor =
                            ContextCompat.getColorStateList(this, R.color.black)
                        Subway()
                    }
                    R.id.third -> { //부동산 (웹뷰)
                        bnv_main.itemIconTintList =
                            ContextCompat.getColorStateList(this, R.color.white)
                        bnv_main.itemTextColor =
                            ContextCompat.getColorStateList(this, R.color.black)
                        Estate()
                    }

                    else -> {

                    }
                } as Fragment
            )
            true
        }
        bnv_main.selectedItemId = R.id.first
    }



    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fl_container, fragment)
            .commit()
    }

}