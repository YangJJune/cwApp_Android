package com.ysj.cwclanapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.values
import com.google.firebase.ktx.Firebase
import com.ysj.cwclanapp.databinding.FragmentLinfoBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LadderInfoFragment: Fragment() {
    lateinit var db:FirebaseDatabase
    lateinit var binding: FragmentLinfoBinding
    lateinit var rankAdapter: ladderAdapter
    lateinit var recentAdapter: ladderAdapter2

    lateinit var mainActivity:MainActivity
    lateinit var rankingData:HashMap<String,Int>
    lateinit var matchData:ArrayList<cwMatch>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLinfoBinding.inflate(inflater,container,false)
        db = Firebase.database

        mainActivity = context as MainActivity
        initRecyclerView()

        return binding.root
    }

    override fun onResume() {
        binding.progressBar3.visibility = View.VISIBLE
        super.onResume()
    }

    fun initRecyclerView(){
        val decoration = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
        binding.rankView.addItemDecoration(decoration)
        binding.rankView.layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false)
        binding.recentView.addItemDecoration(decoration)
        binding.recentView.layoutManager = LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL, false)
        CoroutineScope(Dispatchers.IO).launch{
            CoroutineScope(Dispatchers.IO).async {
                rankingData = db.getReference("users").get().await().value as HashMap<String, Int>
            }.await()
            val tmp = rankingData.toList().sortedBy { -it.second }
            var users:ArrayList<userData> = arrayListOf()
            var k = 0
            for(i in tmp){
                k++
                users.add(userData(i.first, i.second, k))
            }

            rankAdapter = ladderAdapter(users)
            rankAdapter.itemClickListener = object:ladderAdapter.OnItemClickListener{
                override fun OnItemClick(position: Int, player: userData) {
                    //TODO("Not yet implemented")
                }
            }
            mainActivity.runOnUiThread {
                binding.rankView.adapter = rankAdapter
                rankAdapter.notifyDataSetChanged()
            }
            var i = 0
            val tmpRef = db.getReference("mlist")
            CoroutineScope(Dispatchers.IO).async {
                i = tmpRef.child("size").get().await().value.toString().toInt()
            }.await()
            var cnt = 0
            matchData = arrayListOf()
            while(cnt<5){
                if(i<1){
                    binding.progressBar3.visibility = View.INVISIBLE
                    break
                }
                i--
                var str1 = ""
                var str2 = ""
                CoroutineScope(Dispatchers.IO).async {
                    str1 = tmpRef.child(i.toString()).child("p1_winner").get().await().value.toString()
                    str2 = tmpRef.child(i.toString()).child("p2_winner").get().await().value.toString()
                }.await()
                if(str1 == "-1") continue
                if(str1 == str2){
                    cnt++
                    CoroutineScope(Dispatchers.IO).async {
                        val dataSnapshot = tmpRef.child(i.toString()).get().await()
                        val tmpStr = dataSnapshot.child("p1").value.toString()
                        var loser:String = ""
                        if(str1 != tmpStr){
                            loser = tmpStr
                        }
                        else{
                            loser = dataSnapshot.child("p2").value.toString()
                        }
                        var date = dataSnapshot.child("date").value.toString()
                        date = date.substring(0 until 10)
                        matchData.add(cwMatch(date,
                        str1, loser, dataSnapshot.child("map").value.toString()))
                    }.await()
                }
            }
            recentAdapter = ladderAdapter2(matchData)
            mainActivity.runOnUiThread {
                binding.recentView.adapter = recentAdapter
                binding.progressBar3.visibility = View.INVISIBLE
                recentAdapter.notifyDataSetChanged()
            }
        }
    }
}