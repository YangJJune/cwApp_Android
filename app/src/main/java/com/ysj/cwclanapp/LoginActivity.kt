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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    lateinit var auth:FirebaseAuth
    lateinit var pref:SharedPreferences
    lateinit var snap: DataSnapshot
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
        binding.passwordEdit.setText(pref.getString("user_pw",""))

        if(pref.getBoolean("idChk",false)){
            binding.rememIDchk.isChecked = true
        }
        else{
            binding.rememIDchk.isChecked = false
        }

        if(pref.getBoolean("pwChk",false)){
            binding.checkBox.isChecked = true
        }
        else{
            binding.checkBox.isChecked = false
        }
        binding.loginBtn.setOnClickListener {
            val email = binding.idEdit.text.toString()
            val pw = binding.passwordEdit.text.toString()
            auth.signInWithEmailAndPassword(email,pw).addOnCompleteListener(this){task->
                if(task.isSuccessful){
                    //로그인 성공
                    if(task.result.user?.isEmailVerified == false){
                        Toast.makeText(this@LoginActivity,"이메일을 인증해주세요\n이메일을 재전송합니다",Toast.LENGTH_SHORT).show()
                        task.result.user?.sendEmailVerification()
                        return@addOnCompleteListener
                    }
                    Log.d("t","로그인 성공 "+ auth.currentUser?.displayName.toString())

                    if(binding.rememIDchk.isChecked) {
                        editor.putString("user_id", email)
                        editor.putBoolean("idChk", true)
                        editor.apply()
                    }
                    else{
                        editor.putString("user_id", "")
                        editor.putBoolean("idChk", false)
                        editor.apply()
                    }
                    if(binding.checkBox.isChecked){
                        editor.putString("user_pw", pw)
                        editor.putBoolean("pwChk", true)
                        editor.apply()
                    }
                    else{
                        editor.putString("user_pw", "")
                        editor.putBoolean("pwChk", false)
                        editor.apply()
                    }
                    if(task.result.user==null){
                        return@addOnCompleteListener
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        CoroutineScope(Dispatchers.IO).async {
                            snap = Firebase.database.getReference("user_tier").get().await()
                        }.await()
                        val nickname = task.result.user!!.displayName.toString()
                        val tier = snap.child(nickname.lowercase()).child("tier").value.toString()

                        val tierRef = Firebase.database.getReference("users")
                        var needtoInit = true
                        CoroutineScope(Dispatchers.IO).async {
                            if(tierRef.child(nickname).get().await().value != null){
                                needtoInit = false
                            }
                        }.await()

                        if(needtoInit) {

                            Log.d("test", tierRef.toString())
                            if (tier.contains("S"))
                                tierRef.child(nickname).setValue(1300)
                            else if (tier.contains("A+"))
                                tierRef.child(nickname).setValue(1250)
                            else if (tier.contains("A-"))
                                tierRef.child(nickname).setValue(1200)
                            else if (tier.contains("B+"))
                                tierRef.child(nickname).setValue(1150)
                            else if (tier.contains("B-"))
                                tierRef.child(nickname).setValue(1100)
                            else if (tier.contains("C"))
                                tierRef.child(nickname).setValue(1050)
                        }
                    }
                    val tmpIntent = Intent(this, MainActivity::class.java)
                    launcher.launch(tmpIntent)
                    finish()
                }
                else{
                    //로그인 실패
                    Toast.makeText(this,"로그인에 실패하였습니다",Toast.LENGTH_SHORT).show()
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