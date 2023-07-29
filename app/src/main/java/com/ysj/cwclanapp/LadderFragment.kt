package com.ysj.cwclanapp

import android.annotation.SuppressLint
import android.opengl.Visibility
import android.os.Bundle
import android.os.Looper
import android.provider.ContactsContract.Data
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.database.ktx.values
import com.google.firebase.ktx.Firebase
import com.ysj.cwclanapp.databinding.FragmentLadderBinding
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*
import java.util.logging.Handler
import kotlin.concurrent.timer
import kotlin.coroutines.suspendCoroutine
import kotlin.properties.Delegates

class LadderFragment : Fragment(){
    lateinit var name: String
    lateinit var dataSnapshot: DataSnapshot
    lateinit var db:FirebaseDatabase
    lateinit var mainActivity: MainActivity
    lateinit var binding: FragmentLadderBinding
    private var timerTask: Timer? = null
    private var isMatching = false
    private var mycancel = false

    private fun onMatch(){
        mainActivity.runOnUiThread {
            binding.startBtn.visibility = View.INVISIBLE
            binding.cancelBtn.visibility = View.INVISIBLE
            binding.textView3.visibility = View.INVISIBLE
            binding.textView4.visibility = View.INVISIBLE
            binding.textView5.visibility = View.INVISIBLE
            binding.textView6.visibility = View.INVISIBLE

            binding.imageView2.visibility = View.VISIBLE //상대방 프사
            binding.oppId.visibility = View.VISIBLE //상대방 아이디
            binding.oppInfo.visibility = View.VISIBLE //상대방 정보
            binding.chooseTxt.visibility = View.VISIBLE //골라서 결과를 등록하세요
            binding.map.visibility = View.VISIBLE //맵 이름
            binding.p1Btn.visibility = View.VISIBLE //p1이 이겼다는 버튼
            binding.p2Btn.visibility = View.VISIBLE //p2가 이겼다는 버튼
        }

    }
    private fun offMatch(){
        mainActivity.runOnUiThread {
            binding.startBtn.visibility = View.VISIBLE
            binding.cancelBtn.visibility = View.VISIBLE
            binding.textView3.visibility = View.VISIBLE
            binding.textView4.visibility = View.VISIBLE
            binding.textView5.visibility = View.VISIBLE
            binding.textView6.visibility = View.VISIBLE

            binding.imageView2.visibility = View.INVISIBLE //상대방 프사
            binding.oppId.visibility = View.INVISIBLE //상대방 아이디
            binding.oppInfo.visibility = View.INVISIBLE //상대방 정보
            binding.chooseTxt.visibility = View.INVISIBLE //골라서 결과를 등록하세요
            binding.map.visibility = View.INVISIBLE //맵 이름
            binding.p1Btn.visibility = View.INVISIBLE //p1이 이겼다는 버튼
            binding.p2Btn.visibility = View.INVISIBLE //p2가 이겼다는 버튼
        }
    }
    suspend fun getScore(name:String): Int {
        var score = -1
        withContext(Dispatchers.IO){
            dataSnapshot = db.getReference("users").child(name).get().await()
        }
        val tmp:String = dataSnapshot.value.toString()
        score = tmp.toInt()
        return score
    }

    override fun onPause() {
        timerTask?.cancel()
        Log.d("test","Paused!")
        isMatching = false
        super.onPause()
    }
    override fun onResume() {
        timerTask?.cancel()
        Log.d("test","Resumed!")
        super.onResume()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLadderBinding.inflate(inflater, container, false)
        mainActivity = context as MainActivity
        db = Firebase.database

        CoroutineScope(Dispatchers.IO).launch {
            val auth = Firebase.auth
            if (auth.currentUser?.isAnonymous == true) {
                //비회원 로그인이면
                return@launch
            }
            name = auth.currentUser?.displayName.toString()
            if (name == null) return@launch
        }
        val returnchangeListener = object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                //TODO("Not yet implemented")
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                //TODO("Not yet implemented")
            }
            override fun onChildRemoved(snapshot: DataSnapshot) {
                Log.d("test",snapshot.key.toString())
                if(snapshot.key.toString()==name){
                    offMatch()
                }
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                //TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                //TODO("Not yet implemented")
            }
        }
        val qchangeListener = object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if(name == snapshot.key){
                    isMatching = true
                    binding.cancelBtn.visibility = View.VISIBLE
                    Toast.makeText(activity,"매칭이 시작되었습니다",Toast.LENGTH_SHORT)
                    if(timerTask != null) return
                    //TODO("매칭 예상 시간 적어두기")
                    timerTask = kotlin.concurrent.timer(period = 1000){
                        val now = binding.textView5.text.toString()
                        var sec = now.replace("[^0-9]".toRegex(), "").toString().toInt()
                        sec++
                        mainActivity.runOnUiThread {
                            binding.textView5.text = sec.toString()+"초"
                        }
                    }
                }
                //Log.d("test",snapshot.toString())
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("test",snapshot.toString())
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                if(name == snapshot.key){
                    if(mycancel == true) {
                        isMatching = false
                        binding.cancelBtn.visibility = View.INVISIBLE
                        mainActivity.runOnUiThread {
                            Toast.makeText(activity, "매칭이 취소되었습니다", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else{
                        CoroutineScope(Dispatchers.IO).launch {
                            //매치가 잡힌거임
                            mainActivity.runOnUiThread {
                                Toast.makeText(activity, "매칭이 잡혔습니다!", Toast.LENGTH_SHORT).show()
                            }
                            onMatch() //매칭 레이아웃으로 변경
                            timerTask?.cancel()

                            mainActivity.runOnUiThread {
                                binding.textView5.text = "0초"
                                binding.textView6.text = "0초"
                                binding.cancelBtn.visibility = View.INVISIBLE
                            }

                            CoroutineScope(Dispatchers.IO).async {
                                val qRef = db.getReference("recentmatch")
                                val data = qRef.get().await()
                                lateinit var opp:String
                                lateinit var opp_race:String
                                lateinit var opp_mmr:String

                                for(i in data.children){
                                    if(i.key == name){
                                        opp = i.child("opp").value.toString()
                                        var tmp = db.getReference("users").get().await()
                                        opp_mmr = tmp.child(opp).value.toString()
                                        tmp = db.getReference("userRace").get().await()
                                        opp_race = tmp.child(opp).value.toString()
                                        Log.d("test","여기")
                                        mainActivity.runOnUiThread {
                                            binding.oppId.text = opp
                                            binding.oppInfo.text = opp_race+' '+ opp_mmr.toString()
                                            binding.p1Btn.text = name
                                            binding.p2Btn.text = opp
                                        }
                                        break
                                    }
                                }
                            }.await()
                        }

                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("test",snapshot.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("test",error.toString())
            }
        }
        db.getReference("mqueue").addChildEventListener(qchangeListener)
        db.getReference("recentmatch").addChildEventListener(returnchangeListener)
        binding.startBtn.setOnClickListener {
            mycancel=false
            CoroutineScope(Dispatchers.IO).launch {
                if(name == null){
                    Toast.makeText(activity,"계정에 문제가 있습니다 관리자에 문의해주세요",Toast.LENGTH_SHORT).show()
                }
                val qRef = db.getReference("mqueue")
                var score = -1
                CoroutineScope(Dispatchers.IO).async {
                    score = getScore(name)
                }.await()

                if (name != null && score != null)
                    qRef.child(name).setValue(score)

                binding.startBtn.isClickable = false
                val slideDown = TranslateAnimation(0f,0f,0f,500f).apply {
                    duration = 1000
                    repeatCount = 0

                }

                mainActivity.runOnUiThread {
                    mainActivity.binding.menuView.startAnimation(slideDown)
                    mainActivity.binding.menuView.visibility = View.INVISIBLE
                }
            }
        }
        binding.cancelBtn.setOnClickListener {
            mycancel = true
            timerTask?.cancel()
            binding.textView5.text = "0초"
            binding.textView6.text = "0초"
            binding.cancelBtn.visibility = View.INVISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                CoroutineScope(Dispatchers.IO).async {
                    val myref = db.getReference("mqueue")
                    myref.child(name).removeValue()
                }.await()
            }
            binding.startBtn.isClickable = true
            val slideUp = TranslateAnimation(0f,0f,300f,0f).apply {
                duration = 1000
                repeatCount = 0
            }
            mainActivity.runOnUiThread {
                mainActivity.binding.menuView.visibility = View.VISIBLE
                mainActivity.binding.menuView.startAnimation(slideUp)
            }
        }

        binding.p1Btn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                CoroutineScope(Dispatchers.IO).async {
                    val opp_num = db.getReference("recentmatch").child(name).child("num").get().await().value.toString()
                    var player_num = 1
                    val tmpRef = db.getReference("mlist").child(opp_num)
                    Log.d("test",tmpRef.toString())
                    val p1_name = tmpRef.child("p1").get().await().value.toString()
                    if(p1_name != name){
                        player_num = 2
                    }
                    tmpRef.child("p"+player_num+"_winner").setValue(binding.p1Btn.text.toString())
                }.await()
            }
        }

        binding.p2Btn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                CoroutineScope(Dispatchers.IO).async {
                    val opp_num = db.getReference("recentmatch").child(name).child("num").get().await().value.toString()
                    var player_num = 1
                    val tmpRef = db.getReference("mlist").child(opp_num)
                    val p1_name = tmpRef.child("p1").get().await().value.toString()
                    if(p1_name != name){
                        player_num = 2
                    }
                    tmpRef.child("p"+player_num+"_winner").setValue(binding.p2Btn.text.toString())
                }.await()
            }
        }

        return binding.root
    }
}