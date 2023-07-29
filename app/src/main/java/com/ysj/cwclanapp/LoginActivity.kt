package com.ysj.cwclanapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.ysj.cwclanapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    lateinit var auth:FirebaseAuth
    lateinit var pref:SharedPreferences
    lateinit var editor:SharedPreferences.Editor
    var isIDChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        auth = Firebase.auth
        setContentView(binding.root)

        pref = getPreferences(Context.MODE_PRIVATE)
        editor = pref.edit()

        initLayout()
    }
    val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

    }

    fun initLayout(){
        binding.idEdit.setText(pref.getString("user_id",""))
        binding.loginBtn.setOnClickListener {
            val email = binding.idEdit.text.toString()
            val pw = binding.passwordEdit.text.toString()
            auth.signInWithEmailAndPassword(email,pw).addOnCompleteListener(this){task->
                if(task.isSuccessful){
                    //로그인 성공
                    Log.d("t","로그인 성공 "+ auth.currentUser?.displayName.toString())

                    editor.putString("user_id",email)
                    editor.apply()

                    val tmpIntent = Intent(this, MainActivity::class.java)
                    launcher.launch(tmpIntent)
                    finish()
                }
                else{
                    //로그인 실패
                    Log.d("t","로그인 실패")
                }
            }
        }

        binding.rememIDchk.setOnCheckedChangeListener {buttonView, isChecked->
            isIDChecked = isChecked
        }

        binding.registerBtn.setOnClickListener {
            val i = Intent(this@LoginActivity, RegisterActivity::class.java)
            launcher.launch(i)
        }
    }
}