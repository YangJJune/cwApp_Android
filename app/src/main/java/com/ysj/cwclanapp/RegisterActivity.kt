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
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.ysj.cwclanapp.databinding.ActivityRegisterBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterActivity : AppCompatActivity() {
    lateinit var binding: ActivityRegisterBinding
    lateinit var auth:FirebaseAuth
    lateinit var db:FirebaseDatabase
    lateinit var regActivity:RegisterActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val spinner: Spinner = binding.raceSpinner
        spinner.adapter = ArrayAdapter.createFromResource(this,R.array.race_arr, android.R.layout.simple_spinner_item)
        setContentView(binding.root)

        auth = Firebase.auth
        db = Firebase.database
        regActivity = this
        binding.registerBtn.setOnClickListener {
            val email:String = binding.emailedit.text.toString()
            val pw:String = binding.passwordEdit.text.toString()
            val nickname = binding.idEdit.text.toString()
            var isValid = true//유효한 닉네임인가
            lateinit var snap:DataSnapshot
            lateinit var snap_alreadyUse:DataSnapshot
            if(pw.equals(binding.passwordEdit2.text.toString())) {
                CoroutineScope(Dispatchers.IO).launch {
                    CoroutineScope(Dispatchers.IO).async {
                        snap = db.getReference("user_tier").get().await()
                        snap_alreadyUse = db.getReference("users").get().await()
                    }.await()
                    if(!snap.hasChild(nickname.lowercase())){
                        regActivity.runOnUiThread {
                            Toast.makeText(regActivity,"닉네임이 올바르지 않은 것 같습니다\n지속적으로 해당 현상이 발생할 경우\n 문의 부탁드립니다", Toast.LENGTH_SHORT).show()
                        }
                        isValid = false
                    }
                    if(snap_alreadyUse.hasChild(nickname.lowercase()) or snap_alreadyUse.hasChild(nickname)){
                        regActivity.runOnUiThread {
                            Toast.makeText(regActivity,"이미 사용 중인 닉네임입니다.\n본인이 아닌 경우\n문의 부탁드립니다", Toast.LENGTH_SHORT).show()
                        }
                        isValid = false
                    }
                    if(!isValid){
                        return@launch
                    }

                    auth.createUserWithEmailAndPassword(email,pw).addOnCompleteListener(regActivity) { task->
                        if(task.isSuccessful){
                            val user = task.result.user
                            user!!.updateProfile(userProfileChangeRequest {
                                displayName = nickname
                            })

                            Log.d("test",user.toString())
                            user!!.sendEmailVerification()

                            Firebase.database.getReference("userRace").child(binding.idEdit.text.toString()).setValue(binding.raceSpinner.selectedItem.toString())
                            regActivity.runOnUiThread {
                                Toast.makeText(regActivity, "회원가입 성공", Toast.LENGTH_SHORT).show()
                            }
                            finish()
                        }
                        else{
                            if(task.exception!! is FirebaseAuthUserCollisionException){
                                val tmpException: FirebaseAuthUserCollisionException = (task.exception as FirebaseAuthUserCollisionException?)!!
                                when(tmpException.errorCode){
                                    "ERROR_INVALID_EMAIL"->{
                                        regActivity.runOnUiThread {
                                            Toast.makeText(
                                                regActivity,
                                                "올바른 이메일이 아닙니다",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    "ERROR_EMAIL_ALREADY_IN_USE" ->{
                                        regActivity.runOnUiThread {
                                            Toast.makeText(
                                                regActivity, "이미 사용 중인 email입니다. 다른 email을 사용해주세요",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    else -> {
                                        regActivity.runOnUiThread {
                                            Toast.makeText(
                                                regActivity,
                                                "회원가입 실패, 문제확인을 위해 개발자에 문의 바랍니다",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                            else if(task.exception!! is FirebaseAuthInvalidCredentialsException){
                                val tmpException: FirebaseAuthInvalidCredentialsException = (task.exception as FirebaseAuthInvalidCredentialsException?)!!
                                Log.d("error",tmpException.errorCode)
                                when(tmpException.errorCode){
                                    "ERROR_INVALID_EMAIL" ->{
                                        regActivity.runOnUiThread {
                                            Toast.makeText(
                                                regActivity,
                                                "올바른 형태의 이메일이 아닙니다",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    "ERROR_WEAK_PASSWORD" ->{
                                        regActivity.runOnUiThread {
                                            Toast.makeText(
                                                regActivity,
                                                "올바른 형태의 비밀번호가 아닙니다",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                            else{
                                regActivity.runOnUiThread {
                                    Toast.makeText(
                                        regActivity,
                                        task.exception!!.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
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