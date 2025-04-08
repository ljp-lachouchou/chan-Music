package com.software.mymusicplayer.view

import android.content.Intent
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.view.menu.MenuPresenter
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.software.mymusicplayer.NoteDetailActivity
import com.software.mymusicplayer.R
import com.software.mymusicplayer.adapter.MyRecyclerViewAdapter
import com.software.mymusicplayer.adapter.NoteAdapter
import com.software.mymusicplayer.base.BaseFragment
import com.software.mymusicplayer.entity.Note
import com.software.mymusicplayer.entity.User
import com.software.mymusicplayer.manager.NoteDatabaseManager
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.RedisClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Callback

class NoteFragment : BaseFragment(),NoteAdapter.OnNoteClickListener{
    private lateinit var notes:MutableSet<Note>
    private lateinit var bar:Toolbar
    private var user: User? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: NoteAdapter
    private lateinit var refreshLayout: RefreshLayout
    private var limit = 20
    private val PAGE_SIZE = 20
    private var page = 2
    private var isLoading = false
    private var isScrolling = false
    override fun initData() {
        notes = mutableSetOf()
        user = UserDatabaseManager.user
        Log.i("USER_DEBUG",user.toString())
    }
    override fun refreshData() {
        isLoading = true

        lifecycleScope.launch(Dispatchers.IO) {
            val note = NoteDatabaseManager.getInstance(requireContext())
                .getDatabase()
                .getDao()
                .getNotes(limit)
            notes = note.toMutableSet()
            withContext(Dispatchers.Main){
                recyclerViewAdapter.setData(notes.toMutableList())
                isLoading = false
                refreshLayout.finishRefresh()
            }
        }


    }

    override fun onStart() {
        super.onStart()
        recyclerView.scrollToPosition(0)
        refreshData()

    }
    fun updateMoreData(){
        Log.i("sizes",page.toString())
        lifecycleScope.launch(Dispatchers.IO) {
            val note = NoteDatabaseManager.getInstance(requireContext())
                .getDatabase()
                .getDao()
                .getNotesByLimit(limit,(page - 1) * PAGE_SIZE)
            Log.i("note.sikze",note.size.toString())
            if (note.isNotEmpty()) {
                val oldSize = notes.size
                notes.addAll(note)
                val newSize = notes.size
                if (notes.size % PAGE_SIZE == 0)page++
                withContext(Dispatchers.Main) {
                    if (newSize > oldSize)
                        recyclerViewAdapter.addData(note.toMutableList())
                }
            } else {
                // 没有更多数据时提示
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(),
                        "没有更多数据了", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun initView(view: View) {
        super.initView(view)
        recyclerViewAdapter = NoteAdapter(context,notes.toMutableList())
        recyclerView = view.findViewById(R.id.note_recycler)
        recyclerViewAdapter.setOnClickListener(this)
        recyclerView.adapter = recyclerViewAdapter
        refreshLayout = view.findViewById(R.id.note_refresh)
        bar = view.findViewById(R.id.note_search_bar)
        refreshLayout.setOnRefreshListener {
            refreshData()
        }
        refreshLayout.setOnLoadMoreListener {
            Log.i("DEBUGHH",isScrolling.toString() +
                    " " + isLoading)
            if (!isLoading && !isScrolling) {
                updateMoreData()
            }
            refreshLayout.finishLoadMore()
        }

        bar.inflateMenu(R.menu.insert_note_menu)
        refreshData()
        clickEvent()

    }

    private fun clickEvent() {
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val position = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount
                if (position >= totalItemCount){
                    refreshLayout.autoLoadMore()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    isScrolling = true
                }else if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    isScrolling = false
                }
            }
        })
    }

    override fun getToolBarId(): Int = R.id.note_search_bar
    override fun getLayoutId(): Int = R.layout.fragment_note
    override fun onClick(note:Note) {
        Log.i("note点击了",Gson().toJson(note))
       lifecycleScope.launch (Dispatchers.IO){
           val res = RedisClient.getInstance()
               .getService(RedisService::class.java)
               .zIncBy("notes:view",1.0, note.id.toString())
               .execute()
           withContext(Dispatchers.Main){
               Log.i("note点击了",res.body()?.value.toString())
               startActivity(Intent(activity?.applicationContext,NoteDetailActivity::class.java).apply {
                   putExtra("detailNote",note)
               })
           }
       }

    }

}