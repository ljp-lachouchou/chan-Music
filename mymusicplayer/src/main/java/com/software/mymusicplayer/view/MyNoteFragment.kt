package com.software.mymusicplayer.view


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.software.mymusicplayer.NoteDetailActivity
import com.software.mymusicplayer.R
import com.software.mymusicplayer.adapter.NoteAdapter
import com.software.mymusicplayer.adapter.RecommendAlbumAdapter
import com.software.mymusicplayer.entity.Note
import com.software.mymusicplayer.manager.NoteDatabaseManager
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.RedisClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyNoteFragment : Fragment(),NoteAdapter.OnNoteClickListener {
    private lateinit var recyclerView: CustomRecyclerView
    private lateinit var albumAdapter: NoteAdapter
    private lateinit var notes:MutableList<Note>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_count_list,container,false)
        notes = mutableListOf()
        initView(view)
        updateCollectData()
        return view
    }

    override fun onResume() {
        updateCollectData()
        super.onResume()
    }
    private fun updateCollectData(){
        lifecycleScope.launch(Dispatchers.IO) {
            val note = NoteDatabaseManager.getInstance(requireContext())
                .getDatabase()
                .getDao()
                .getNotesByUser(UserDatabaseManager.user?.userName!!)
            Log.i("notesss",note.size.toString())
            notes = note.toMutableList()
            withContext(Dispatchers.Main){
                albumAdapter.setData(notes)
            }
        }

    }
    private fun initView(view: View) {
        recyclerView = view.findViewById(R.id.album_recycler)
        albumAdapter = NoteAdapter(requireContext(),notes)
        albumAdapter.setOnClickListener(this)
        recyclerView.adapter = albumAdapter
    }


    override fun onClick(note: Note) {
        lifecycleScope.launch (Dispatchers.IO){
            val res = RedisClient.getInstance()
                .getService(RedisService::class.java)
                .zIncBy("notes:view",1.0, note.id.toString())
                .execute()
            withContext(Dispatchers.Main){
                Log.i("note点击了",res.body()?.value.toString())
                startActivity(Intent(activity?.applicationContext, NoteDetailActivity::class.java).apply {
                    putExtra("detailNote",note)
                })
            }
        }
    }
}