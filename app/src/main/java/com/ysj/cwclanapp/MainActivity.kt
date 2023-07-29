package com.ysj.cwclanapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ysj.cwclanapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val homeFragment = HomeFragment()
    val tubeFragment = CWTubeFragment()
    val ladderFragment = LadderFragment()
    val snsFragment = CWstargramFragment()
    val recordFragment = LadderInfoFragment()

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            finish()
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.menuView.selectedItemId = R.id.homeBtn
        initFrag()
    }

    fun initFrag(){
        val fragment = supportFragmentManager.beginTransaction()
        fragment.replace(R.id.frameLayout, homeFragment)
        fragment.commit()
        binding.apply {
            menuView.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.recordBtn->{
                        val fragment = supportFragmentManager.beginTransaction()
                        fragment.addToBackStack(null)
                        fragment.replace(R.id.frameLayout, recordFragment)
                        fragment.commit()
                        true
                    }
                    R.id.mypageBtn->{
                        val fragment = supportFragmentManager.beginTransaction()
                        fragment.addToBackStack(null)
                        fragment.replace(R.id.frameLayout, homeFragment)
                        fragment.commit()
                        true

                    }
                    R.id.ladderBtn -> {
                        val fragment = supportFragmentManager.beginTransaction()
                        fragment.addToBackStack(null)
                        fragment.replace(R.id.frameLayout, ladderFragment)
                        fragment.commit()
                        true
                    }
                    R.id.homeBtn -> {
                        val fragment = supportFragmentManager.beginTransaction()
                        fragment.addToBackStack(null)
                        fragment.replace(R.id.frameLayout, homeFragment)
                        fragment.commit()
                        true
                    }
                    R.id.watchBtn -> {
                        val fragment = supportFragmentManager.beginTransaction()
                        fragment.addToBackStack(null)
                        fragment.replace(R.id.frameLayout, tubeFragment)
                        fragment.commit()
                        true
                    }
                    else -> {
                        Toast.makeText(this@MainActivity, "알 수 없는 문제가 발생하였습니다", Toast.LENGTH_SHORT)
                        Log.d("t", it.itemId.toString())
                        false
                    }
                }
            }
        }
    }
}