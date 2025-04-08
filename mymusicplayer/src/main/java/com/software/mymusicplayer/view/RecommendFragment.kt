package com.software.mymusicplayer.view

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import com.scwang.smart.refresh.layout.api.RefreshHeader
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.software.mymusicplayer.ListActivity
import com.software.mymusicplayer.R
import com.software.mymusicplayer.adapter.MyViewPagerAdapterInFragment
import com.software.mymusicplayer.adapter.RecommendAlbumAdapter
import com.software.mymusicplayer.base.BaseFragment
import com.software.mymusicplayer.contracts.RecommendContracts
import com.software.mymusicplayer.entity.SongList.Album
import com.software.mymusicplayer.entity.Artist

import com.software.mymusicplayer.entity.Song
import com.software.mymusicplayer.manager.UserDatabaseManager
import com.software.mymusicplayer.model.RecommendModel.Companion.OFFSET_VALUE1
import com.software.mymusicplayer.model.RecommendModel.Companion.OFFSET_VALUE2
import com.software.mymusicplayer.model.RecommendModel.Companion.SEARCH_VALUE1
import com.software.mymusicplayer.model.RecommendModel.Companion.SEARCH_VALUE2
import com.software.mymusicplayer.presenter.RecommendPresenter
import com.software.mymusicplayer.service.RedisService
import com.software.mymusicplayer.utils.DpAndPx
import com.software.mymusicplayer.utils.RedisClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.random.Random

class RecommendFragment :
    BaseFragment(),RecommendContracts.IView,
        RecommendAlbumAdapter.OnItemClickListener
{ lateinit var refreshHeader: RefreshHeader
    lateinit var refreshLayout: RefreshLayout
    lateinit var presenter: RecommendPresenter
    private lateinit var recommendSongsOne:MutableList<Song>
    private lateinit var shimmer:ShimmerFrameLayout
    private lateinit var recommendSongsTwo:MutableList<Song>
    private lateinit var recommendAlbums:MutableSet<Album>
    private lateinit var recommendViewPager2OneFragments:List<ViewPagerItem>
    private lateinit var recommendViewPager2TwoFragments:List<ViewPagerItem>
    private lateinit var recommendViewPager2One: ViewPager2
    private lateinit var recommendViewPager2Two: ViewPager2
    private lateinit var dataOneInOne : MutableList<Song>
    private lateinit var dataTwoInOne : MutableList<Song>
    private lateinit var dataThreeInOne : MutableList<Song>
    private lateinit var dataOneInTwo : MutableList<Song>
    private lateinit var dataTwoInTwo : MutableList<Song>
    private lateinit var dataThreeInTwo : MutableList<Song>
    private lateinit var collectAlbumRecycler:CustomRecyclerView
    private lateinit var albumRecommendRecyclerView: CustomRecyclerView
    private lateinit var recyclerAdapter: RecommendAlbumAdapter
    private lateinit var collectAlbumAdapter: RecommendAlbumAdapter
    private lateinit var realLayout:LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var collectAlbums:MutableSet<Album>
    var reload = false
    val TAG = "RecommendFragment"
    private val context = this
    override fun getToolBarId(): Int = R.id.search_bar

    override fun getLayoutId(): Int = R.layout.fragment_recommend

    /**
     * 专辑的操作
     */
    override fun onItemClick(album: Album) {
        val intent = Intent(getContext(),ListActivity::class.java)
        intent.putExtra("clickAlbum",album)
        startActivity(intent)

    }

    override fun onStart() {

        updateCollectData()
        super.onStart()
    }
    override fun refreshData(){
        lifecycleScope.launch (Dispatchers.IO){
            val job1 = async { presenter.getDataOne() }
            val job2 = async { presenter.getDataTwo() }
            awaitAll(job1,job2)
            withContext(Dispatchers.Main){
                try{
                    updateViewPagerData()
                    recyclerAdapter.setData(recommendAlbums)
                    reload = true
                    refreshLayout.finishRefresh()
                    if (shimmer.isShimmerStarted){
                        shimmer.stopShimmer()
                        shimmer.visibility = View.GONE
                        realLayout.visibility = View.VISIBLE
                    }

                }catch (e:Exception){
                    Log.e("ERROR",e.printStackTrace().toString())
                    Log.i("RELOAD",reload.toString())
                    withContext(Dispatchers.IO){
                        job1.cancel()
                        job2.cancel()
                        delay(1000)
                        if (!reload) {
                            refreshData()
                        }
                    }
                }

            }

        }



    }
    /**
     * 初始化数据
     */
    override fun initData() {
        presenter = RecommendPresenter(context)
        initViewPagersData()
        recommendAlbums = mutableSetOf()
        collectAlbums = mutableSetOf()

    }

    private fun initViewPagersData() {
        recommendSongsTwo = mutableListOf()
        recommendSongsOne = mutableListOf()
        dataOneInOne = mutableListOf()
        dataTwoInOne = mutableListOf()
        dataThreeInOne = mutableListOf()
        dataOneInTwo = mutableListOf()
        dataTwoInTwo = mutableListOf()
        dataThreeInTwo = mutableListOf()
        recommendViewPager2OneFragments = listOf(ViewPagerItem(dataOneInOne),
            ViewPagerItem(dataTwoInOne),ViewPagerItem(dataThreeInOne))
        recommendViewPager2TwoFragments = listOf(ViewPagerItem(dataOneInTwo),
            ViewPagerItem(dataTwoInTwo),ViewPagerItem(dataThreeInTwo))
    }

    /**
     * 初始化视图
     */
    override fun initView(view: View) {
        super.initView(view)
        shimmer = view.findViewById(R.id.shimmer)
        refreshHeader = view.findViewById(R.id.fragment_ch)
        refreshLayout = view.findViewById(R.id.fragment_srl)
        refreshLayout.setRefreshHeader(refreshHeader)
        albumRecommendRecyclerView = view.findViewById(R.id.recommend_album)
        collectAlbumRecycler = view.findViewById(R.id.my_collect_album)
        recyclerAdapter = RecommendAlbumAdapter(getContext(),recommendAlbums,
            albumRecommendRecyclerView)
        recyclerAdapter.setOnItemClickListener(this)
        collectAlbumAdapter = RecommendAlbumAdapter(getContext(),collectAlbums,
            collectAlbumRecycler)
        collectAlbumAdapter.setOnItemClickListener(this)
        collectAlbumRecycler.adapter = collectAlbumAdapter
        val linearLayoutManager = LinearLayoutManager(getContext())
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        albumRecommendRecyclerView.layoutManager = linearLayoutManager
        albumRecommendRecyclerView.adapter = recyclerAdapter
        scrollView = view.findViewById(R.id.recommend_scrollView)
        initViewPagers(view)
        realLayout = view.findViewById(R.id.real_layout)
        refreshLayout.setOnRefreshListener{
            refreshData()
        }
        updateCollectData()
        Log.i("collectAlbum",collectAlbums.size.toString())
        refreshLayout.autoRefresh()



    }
    private fun updateCollectData(){
        lifecycleScope.launch(Dispatchers.IO) {
            val albums = mutableSetOf<Album>()
            RedisClient.getInstance()
                .getService(RedisService::class.java)
                .sMembers("user:${UserDatabaseManager.user!!.userName}:albums")
                .execute().body()?.forEach {
                    s ->
                    albums.add(Gson().fromJson(s,Album::class.java))
                    }
            collectAlbums = albums
            Log.i("collectAlbum",collectAlbums.size.toString())
            withContext(Dispatchers.Main){
                collectAlbumAdapter.setData(collectAlbums)
            }

        }

    }
    private fun initViewPagers(view: View) {
        recommendViewPager2One = view.findViewById(R.id.recommend_songs_vp2_one)
        recommendViewPager2Two = view.findViewById(R.id.recommend_songs_vp2_two)

        recommendViewPager2One.adapter =
            MyViewPagerAdapterInFragment(recommendViewPager2OneFragments,context)
        recommendViewPager2Two.adapter =
            MyViewPagerAdapterInFragment(recommendViewPager2TwoFragments,context)
        viewPagerGoodDisplay(recommendViewPager2One)
        viewPagerGoodDisplay(recommendViewPager2Two)
    }

    private fun viewPagerGoodDisplay(viewPager2: ViewPager2) {
        viewPager2.offscreenPageLimit = 2
        viewPager2.setPageTransformer { page, position ->
            val scale = 1f - (0.3f * abs(position))
            page.x = scale
            page.y = scale
            page.translationX = position * -DpAndPx.dpToPx(10,viewPager2)
        }
    }


    override fun successOneTODO(
        songs: MutableList<Song>,
        artists: MutableSet<Artist>,
        albums: MutableSet<Album>
    ) {
        recommendAlbums = albums
        recommendSongsOne = songs
        SEARCH_VALUE1 =  Random.nextInt(0,4)
        OFFSET_VALUE1 = Random.nextInt(0,10000)
        Log.i("SEARCH_VALUE","SEARCH_VALUE = ${SEARCH_VALUE1}")
        Log.i("OFFSET_VALUE","OFFSET_VALUE = ${OFFSET_VALUE1}")
    }

    override fun failOneTODO(t: Throwable?) {
        Log.i(TAG,t?.printStackTrace().toString())
    }

    override fun onResume() {
        super.onResume()
        if (shimmer.visibility == View.VISIBLE){
            refreshData()
        }

    }
    override fun successTwoTODO(
        songs: MutableList<Song>,
        artists: MutableSet<Artist>,
        albums: MutableSet<Album>
    ) {
        recommendSongsTwo=songs
        SEARCH_VALUE2 =  Random.nextInt(0,4)
        OFFSET_VALUE2 = Random.nextInt(0,10000)
    }



    override fun failTwoTODO(t: Throwable?) {
        Log.i(TAG,t?.printStackTrace().toString())
    }
    /**
     * 更新ViewPager的操作
     */
    private fun updateViewPagerData() {
        updateDataOne()
        updateDataTwo()
        updateFragmentsOne()
        updateFragmentsTwo()
    }
    private fun updateDataOne(){
        Log.i(TAG,recommendSongsOne.size.toString())
        Log.i("size",recommendSongsOne.size.toString())
        dataOneInOne=recommendSongsOne.subList(0,3)
        dataTwoInOne=recommendSongsOne.subList(3,6)
        dataThreeInOne=recommendSongsOne.subList(6,9)
    }
    private fun updateDataTwo(){
        dataOneInTwo=recommendSongsTwo.subList(0,3)
        dataTwoInTwo=recommendSongsTwo.subList(3,6)
        dataThreeInTwo=recommendSongsTwo.subList(6,9)
    }
    private fun updateFragmentsOne() {
        recommendViewPager2OneFragments[0].setData(dataOneInOne)
        recommendViewPager2OneFragments[1].setData(dataTwoInOne)
        recommendViewPager2OneFragments[2].setData(dataThreeInOne)
    }
    private fun updateFragmentsTwo() {
        recommendViewPager2TwoFragments[0].setData(dataOneInTwo)
        recommendViewPager2TwoFragments[1].setData(dataTwoInTwo)
        recommendViewPager2TwoFragments[2].setData(dataThreeInTwo)
    }


    override fun onDestroy() {
        super.onDestroy()
    }

//


}