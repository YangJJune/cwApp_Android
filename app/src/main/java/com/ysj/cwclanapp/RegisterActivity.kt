package com.ysj.cwclanapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.ysj.cwclanapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegisterBinding
    lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val spinner: Spinner = binding.raceSpinner
        spinner.adapter = ArrayAdapter.createFromResource(this,R.array.race_arr, android.R.layout.simple_spinner_item)
        setContentView(binding.root)

        auth = Firebase.auth
        binding.registerBtn.setOnClickListener {
            val email:String = binding.emailedit.text.toString()
            val pw:String = binding.passwordEdit.text.toString()

            if(pw.equals(binding.passwordEdit2.text.toString())) {
                auth.createUserWithEmailAndPassword(email,pw).addOnCompleteListener(this) { task->
                    if(task.isSuccessful){
                        val user = task.result.user
                        user!!.updateProfile(userProfileChangeRequest {
                            displayName = binding.idEdit.text.toString()
                        })
                        Firebase.database.getReference("userRace").child(binding.idEdit.text.toString()).setValue(binding.raceSpinner.selectedItem.toString())
                        Firebase.database.getReference("users").child(binding.idEdit.text.toString()).setValue(1200)
                        Log.d("test",user.displayName.toString())
                        Toast.makeText(this,"회원가입 성공", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    else{
                        if(task.exception!! is FirebaseAuthUserCollisionException){
                            val tmpException: FirebaseAuthUserCollisionException = (task.exception as FirebaseAuthUserCollisionException?)!!
                            when(tmpException.errorCode){
                                "ERROR_INVALID_EMAIL"->{
                                    Toast.makeText(this,"올바른 이메일이 아닙니다", Toast.LENGTH_SHORT).show()
                                }
                                "ERROR_EMAIL_ALREADY_IN_USE" ->{
                                    Toast.makeText(this,"이미 사용 중인 email입니다. 다른 email을 사용해주세요",
                                        Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    Toast.makeText(this,"회원가입 실패, 문제확인을 위해 개발자에 문의 바랍니다", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        else if(task.exception!! is FirebaseAuthInvalidCredentialsException){
                            val tmpException: FirebaseAuthInvalidCredentialsException = (task.exception as FirebaseAuthInvalidCredentialsException?)!!
                            Log.d("error",tmpException.errorCode)
                            when(tmpException.errorCode){
                                "ERROR_INVALID_EMAIL" ->{
                                    Toast.makeText(this,"올바른 형태의 이메일이 아닙니다", Toast.LENGTH_SHORT).show()
                                }
                                "ERROR_WEAK_PASSWORD" ->{
                                    Toast.makeText(this,"올바른 형태의 비밀번호가 아닙니다", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        else{
                            Toast.makeText(this,task.exception!!.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            else{
                Toast.makeText(this,"PW가 PW확인과 동일하지 않습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }
}