package com.ysj.cwclanapp

import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.ysj.cwclanapp.databinding.FragmentMyPageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileInputStream
import kotlin.properties.Delegates

class MyPageFragment : Fragment() {
    lateinit var db: FirebaseDatabase
    lateinit var binding: FragmentMyPageBinding
    lateinit var recentAdapter:myRecentAdapter

    lateinit var matchData:ArrayList<myrecentData>
    lateinit var mainActivity:MainActivity
    lateinit var nickname:String
    lateinit private var uri: Uri

    lateinit var firestorage:FirebaseStorage
    override fun onResume() {
        binding.progressBar.visibility = View.VISIBLE
        super.onResume()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMyPageBinding.inflate(inflater,container,false)
        db = Firebase.database
        mainActivity = context as MainActivity

        initView()
        initClickListener()
        firestorage = Firebase.storage
        lateinit var profRef:StorageReference
        try {
            profRef = firestorage.reference.child("profiles/" + nickname)
        }
        catch(e:Exception){
            e.printStackTrace()
        }

        val localFile = File.createTempFile("profile","png")

        profRef.getFile(localFile).addOnSuccessListener {
            val imgpath = Uri.parse(localFile.absolutePath)
            mainActivity.runOnUiThread {
                binding.profileView.setImageURI(imgpath)
            }
        }.addOnFailureListener{
            mainActivity.runOnUiThread {
                Toast.makeText(mainActivity,"프로필 이미지 불러오기 실패",Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }
    val activityResult:ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == RESULT_OK && it.data != null ){
            uri = it.data!!.data!!
            var uploadTask = firestorage.getReference("profiles/"+nickname).putFile(uri)
            uploadTask.addOnFailureListener{

            }.addOnCompleteListener{

            }
            //firestorage.reference.child()
        }
    }
    fun initClickListener(){
        binding.chRace.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                var builder = AlertDialog.Builder(mainActivity)
                    .setTitle("종족변경")
                    .setMessage("변경할 종족을 선택해주세요")
                var listener = object:DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        when(which){
                            DialogInterface.BUTTON_POSITIVE ->{
                                //Z
                                db.getReference("userRace").child(nickname).setValue("Z")
                            }
                            DialogInterface.BUTTON_NEGATIVE->{
                                //P
                                db.getReference("userRace").child(nickname).setValue("P")
                            }
                            DialogInterface.BUTTON_NEUTRAL->{
                                //T
                                db.getReference("userRace").child(nickname).setValue("T")
                            }
                        }
                    }
                }
                builder.setPositiveButton("저그",listener)
                builder.setNegativeButton("프로토스",listener)
                builder.setNeutralButton("테란",listener)
                builder.show()
            }
        }
        binding.chProfileBtn.setOnClickListener {
            val filename = nickname
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            activityResult.launch(intent)
        }
    }

    fun initView(){
        val decoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        binding.recentView.addItemDecoration(decoration)
        binding.recentView.layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false)

        CoroutineScope(Dispatchers.IO).launch {
            var N = 0

           nickname = Firebase.auth.currentUser?.displayName.toString()
            var tmpRef = db.getReference("mlist")

            mainActivity.runOnUiThread {
                binding.idView.text = nickname
            }
            CoroutineScope(Dispatchers.IO).async {
                N = tmpRef.child("size").get().await().value.toString().toInt()
            }.await()
            matchData = arrayListOf()
            lateinit var dataSnapshot: DataSnapshot
            var win = 0
            var lose = 0

            for(i:Int in 0 until N) {
                CoroutineScope(Dispatchers.IO).async {
                    dataSnapshot = tmpRef.child(i.toString()).get().await()
                }.await()

                val p1Name = dataSnapshot.child("p1").value.toString()
                val p2Name = dataSnapshot.child("p2").value.toString()
                val p1Winner = dataSnapshot.child("p1_winner").value.toString()
                val p2Winner = dataSnapshot.child("p2_winner").value.toString()
                val date = dataSnapshot.child("date").value.toString().substring(0,10)
                val map = dataSnapshot.child("map").value.toString()
                if(p1Winner == "-1" || p2Winner=="-1" || p1Winner != p2Winner){
                    continue
                }

                if(p1Name == nickname){
                    //p1 is user
                    if(p1Winner == p1Name){
                        //p1 is winner
                        matchData.add(myrecentData("승",p2Name,date,map))
                        win++
                    }
                    else{
                        //p1 is loser
                        matchData.add(myrecentData("패",p2Name,date,map))
                        lose++
                    }
                    continue
                }
                if(p2Name == nickname){
                    //p2 is user
                    if(p1Winner == nickname){
                        //p2 is winner
                        matchData.add(myrecentData("승",p1Name,date,map))
                        win++
                    }
                    else{
                        //p2 is loser
                        matchData.add(myrecentData("패",p1Name,date,map))
                        lose++
                    }
                }
            }
            tmpRef = db.getReference("userRace")
            lateinit var race:String
            if (nickname != null) {
                CoroutineScope(Dispatchers.IO).async {
                    race = tmpRef.child(nickname).get().await().value.toString()
                }.await()
            }

            val tmpRef2 = db.getReference("users")
            var mmr = -1
            if (nickname != null) {
                CoroutineScope(Dispatchers.IO).async {
                    mmr = tmpRef2.child(nickname).get().await().value.toString().toInt()
                }.await()
            }

            recentAdapter = myRecentAdapter(matchData)
            lateinit var ladderviewTxt:String
            if(win+lose == 0){
                ladderviewTxt = win.toString()+"승 "+ lose.toString()+"패 "+ "0% "+mmr.toString()+"점"
            }
            else{
                ladderviewTxt = win.toString()+"승 "+ lose.toString()+"패 "+ (win.toFloat() / (win+lose).toFloat()).times(100).toInt().toString()+"% "+mmr.toString()+"점"
            }

            mainActivity.runOnUiThread {
                binding.raceView.text = race
                binding.ladderView.text = ladderviewTxt
                binding.recentView.adapter = recentAdapter
                recentAdapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.INVISIBLE
            }
        }
    }


}