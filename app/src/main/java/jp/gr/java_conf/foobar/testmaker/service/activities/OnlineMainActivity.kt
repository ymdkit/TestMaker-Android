package jp.gr.java_conf.foobar.testmaker.service.activities

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.Secure
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.widget.SearchView
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.OnlineTestAdapter
import kotlinx.android.synthetic.main.activity_online_main.*
import java.util.*
import android.widget.Toast
import com.nifcloud.mbaas.core.*
import jp.gr.java_conf.foobar.testmaker.service.extensions.toTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.UnsupportedEncodingException
import java.net.URLEncoder


class OnlineMainActivity : BaseActivity() {

    internal lateinit var adapter: OnlineTestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_main)

        var info: ApplicationInfo? = null
        try {
            info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        NCMB.initialize(this.applicationContext, info?.metaData?.getString("ncbm_app_key") , info?.metaData?.getString("ncbm_client_key"))

        createAd(container)

        initToolBar()
        toolbar.title = getString(R.string.old_online_main)
        cardView.visibility = View.GONE

        login()

        swipe_refresh.setOnRefreshListener {
            reload("")
        }

        reload("")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_online_main, menu)

        val searchView = menu.findItem(R.id.menu_search).actionView as SearchView

        searchView.queryHint = getString(R.string.hint_search)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {


                reload(s)

                return false
            }

            override fun onQueryTextChange(s: String): Boolean {

                reload(s)

                return false
            }
        })

        searchView.setOnCloseListener {

            false
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val actionId = item.itemId

        when (actionId) {
            R.id.action_sort -> {

                AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setTitle(getString(R.string.sort))
                        .setItems(resources.getStringArray(R.array.sort_online)) { _, which ->

                            sharedPreferenceManager.sortOnline = which

                            reload("")
                        }.show()
            }

            R.id.action_profile -> {

                startActivityForResult(Intent(this@OnlineMainActivity, MyPageActivity::class.java), 0)

            }



            android.R.id.home -> {

                finish()

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun reload(searchWord: String) {

        swipe_refresh.isRefreshing = true
        recycler_view.visibility = View.GONE

        val query = NCMBQuery<NCMBObject>("Test")

        //検索件数を50件に設定
        query.setLimit(50)

        query.whereEqualTo("language", getString(R.string.language))

        when (sharedPreferenceManager.sortOnline) {

            Constants.SORT_DOWNLOAD -> query.addOrderByDescending("downloadedNum")

            Constants.SORT_DATE_DESCENDING -> query.addOrderByDescending("createDate")

            Constants.SORT_DATE_ASCENDING -> query.addOrderByAscending("createDate")

        }

        //データストアでの検索を行う
        query.findInBackground { objects, e ->
            swipe_refresh.isRefreshing = false

            if (e != null) {
                //エラー時の処理
                Log.e("NCMB", "検索に失敗しました。エラー:" + e.message)

                Toast.makeText(baseContext, getString(R.string.load_failed), Toast.LENGTH_SHORT).show()

            } else {
                //成功時の処理
                Log.i("NCMB", "検索に成功しました。")

                //検索ワードで絞り込む
                val localObjects: MutableList<NCMBObject> = objects.filter { it.getString("title").contains(searchWord) || it.getString("explanation").contains(searchWord) || it.getString("objectId").contains(searchWord) } as MutableList<NCMBObject>

                adapter = OnlineTestAdapter(this, localObjects)

                adapter.setOnClickListener(object : OnlineTestAdapter.OnClickListener {
                    override fun onClickInfoTest(obj: NCMBObject) {

                        val date = obj.getString("createDate").take(10)

                        val q = NCMBUser.getQuery()

                        q.whereEqualTo("objectId", obj.getString("creatorId"))

                        q.setLimit(1)

                        q.findInBackground { users, e ->
                            if (e != null || users.size == 0) {
                                val dialogLayout = LayoutInflater.from(this@OnlineMainActivity).inflate(R.layout.dialog_online_test_info, findViewById(R.id.layout_dialog_info))

                                val textInfo = dialogLayout.findViewById<TextView>(R.id.text_info)
                                textInfo.text = getString(R.string.info_test, obj.getString("objectId"), getString(R.string.unknown_user), date, obj.getString("explanation"))

                                val builder = AlertDialog.Builder(this@OnlineMainActivity, R.style.MyAlertDialogStyle)
                                builder.setView(dialogLayout)
                                builder.setTitle(obj.getString("title"))
                                builder.show()
                            } else {
                                val dialogLayout = LayoutInflater.from(this@OnlineMainActivity).inflate(R.layout.dialog_online_test_info, findViewById(R.id.layout_dialog_info))

                                val textInfo = dialogLayout.findViewById<TextView>(R.id.text_info)
                                textInfo.text = getString(R.string.info_test, obj.getString("objectId"), users[0].getString("creatorName"), date, obj.getString("explanation"))

                                val builder = AlertDialog.Builder(this@OnlineMainActivity, R.style.MyAlertDialogStyle)
                                builder.setView(dialogLayout)
                                builder.setTitle(obj.getString("title"))
                                builder.show()
                            }
                        }
                    }

                    override fun onClickPlayTest(obj: NCMBObject) {

                        val user = NCMBUser.getCurrentUser()

                        val array = user.getList("downloadedIds")

                        if (!array.any { it == obj.getString("objectId") }) {

                            obj.put("downloadedNum", obj.getInt("downloadedNum") + 1)

                            array.add(obj.getString("objectId"))

                            user.put("downloadedIds", array)
                        }

                        user.saveInBackground {}

                        obj.saveInBackground { e ->
                            if (e == null) {
                                reload("")
                            }
                        }

                        GlobalScope.launch(Dispatchers.Main) {
                            withContext(Dispatchers.Default) { obj.getString("content").replace("\\\n", "\n").toTest(baseContext) }.let {
                                realmController.convert(it, -1L)

                                Toast.makeText(baseContext, baseContext.getString(R.string.message_success_load, it.title), Toast.LENGTH_LONG).show()

                                finish()

                            }
                        }
                    }
                })

                recycler_view.visibility = View.VISIBLE

                recycler_view.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(applicationContext)
                recycler_view.setHasFixedSize(true)
                recycler_view.adapter = this.adapter
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        reload("")

    }

    @SuppressLint("HardwareIds")
    fun login() {

        try {
            NCMBUser.logout()
        } catch (e: NCMBException) {
            e.printStackTrace()
        }

        // UUIDを取得します
        val uuid = Secure.getString(applicationContext.contentResolver,
                Secure.ANDROID_ID)

        try {
            NCMBUser.loginInBackground(uuid, uuid) { _, e ->
                if (e != null) {
                    //エラー時の処理
                    if (e.code == "E401002") {

                        //NCMBUserのインスタンスを作成
                        val user = NCMBUser()
                        //ユーザ名を設定
                        user.userName = uuid
                        //パスワードを設定
                        user.setPassword(uuid)

                        //設定したユーザ名とパスワードで会員登録を行う
                        user.signUpInBackground { er ->
                            if (er != null) {
                                //会員登録時にエラーが発生した場合の処理
                                Log.d("", "Signup error$er")
                            } else {
                                //lastLoginを更新します（ラストログインのタイミングを取得するために）
                                val acl = NCMBAcl()

                                acl.publicReadAccess = true
                                acl.publicWriteAccess = true

                                try {
                                    val curUser = NCMBUser.getCurrentUser()
                                    val now = Date()
                                    curUser.put("lastLoginDate", now)
                                    curUser.put("creatorName", getString(R.string.guest))
                                    curUser.put("downloadedIds", arrayListOf(""))
                                    curUser.put("expert", false)
                                    curUser.acl = acl    // 追加する
                                    curUser.save()
                                } catch (e1: NCMBException) {
                                    e1.printStackTrace()
                                }
                            }
                        }
                    }
                } else {
                    val curUser = NCMBUser.getCurrentUser()

                    //lastLoginを更新します（ラストログインのタイミングを取得するために）
                    try {
                        val now = Date()
                        curUser.put("lastLoginDate", now)
                        curUser.save()
                    } catch (e1: NCMBException) {
                        e1.printStackTrace()
                    }
                }
            }
        } catch (e: NCMBException) {
            e.printStackTrace()
        }
    }
}
