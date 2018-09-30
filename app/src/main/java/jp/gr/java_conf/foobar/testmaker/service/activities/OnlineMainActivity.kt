package jp.gr.java_conf.foobar.testmaker.service.activities

import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Secure
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.nifty.cloud.mb.core.*
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.AsyncLoadTest
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.OnlineTestAdapter
import kotlinx.android.synthetic.main.activity_online_main.*
import java.util.*
import com.nifty.cloud.mb.core.NCMBUser
import com.nifty.cloud.mb.core.NCMBException
import com.nifty.cloud.mb.core.NCMBObject
import com.nifty.cloud.mb.core.FetchCallback
import java.text.SimpleDateFormat


class OnlineMainActivity : BaseActivity() {

    internal lateinit var adapter: OnlineTestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_main)

        sendScreen("OnlineMainActivity")

        NCMB.initialize(this.applicationContext,"11a0bc05538273ecd8e5d6152a9379119f16115c19082eae88c101adeb963f15","afc1899b2ed65520fc935e8d680723828a09203347d18be035408491451262c8")

        NCMBUser.logout()

        container.addView(createAd())

        initToolBar()

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
                                try {
                                    val curUser = NCMBUser.getCurrentUser()
                                    val now = Date()
                                    curUser.put("lastLoginDate", now)
                                    curUser.put("creatorName",getString(R.string.guest))
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


        expand.setOnClickListener {

            val dialogLayout = LayoutInflater.from(this@OnlineMainActivity).inflate(R.layout.dialog_alert_confirm, findViewById(R.id.layout_dialog_confirm))

            dialogLayout.findViewById<TextView>(R.id.text_alert).text = getString(R.string.alert_notes)

            val checkBox = dialogLayout.findViewById<CheckBox>(R.id.check_alert)

            if (sharedPreferenceManager.confirmNotes) {

                upload()

                return@setOnClickListener
            }

            val builder = AlertDialog.Builder(this@OnlineMainActivity, R.style.MyAlertDialogStyle)
            builder.setView(dialogLayout)
            builder.setTitle(getString(R.string.confirm_notes))
            builder.setPositiveButton(android.R.string.ok) { _, _ ->

                if (checkBox.isChecked) sharedPreferenceManager.confirmNotes = true

                upload()

            }

            builder.setNegativeButton(android.R.string.cancel, null)
            builder.show()


        }

        reload()
    }

    private fun upload(){

        val array = Array(realmController.list.size){i ->  realmController.list[i].title}

        val userId = NCMBUser.getCurrentUser().getString("objectId")


        AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setNegativeButton(android.R.string.cancel, null)
                .setTitle(getString(R.string.message_upload_test))
                .setItems(array) { _, which ->

                    val test = realmController.list[which]
                    val obj = NCMBObject("Test")
                    obj.put("content", test.testToString(this@OnlineMainActivity,true))
                    obj.put("title",test.title)
                    obj.put("color",test.color)
                    obj.put("language",getString(R.string.language))
                    obj.put("downloadedNum",0)
                    obj.put("creatorId",userId)
                    obj.put("questionsNum",test.getQuestions().count{ it.imagePath == "" } )
                    obj.saveInBackground { e ->
                        if (e != null) {
                            //保存失敗
                            AlertDialog.Builder(this@OnlineMainActivity,R.style.MyAlertDialogStyle)
                                    .setMessage(getString(R.string.failed_upload))
                                    .setPositiveButton("OK", null)
                                    .show()

                        } else {
                            //保存成功
                            AlertDialog.Builder(this@OnlineMainActivity,R.style.MyAlertDialogStyle)
                                    .setMessage(getString(R.string.successed_upload,obj.getString("title")))
                                    .setPositiveButton("OK", null)
                                    .show()

                            reload()

                        }
                    }
                }.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_online_main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val actionId = item.itemId

        when (actionId) {
            R.id.action_reload -> {
                reload()
            }

            R.id.action_profile ->{

                sendEvent("profile")

                startActivityForResult(Intent(this@OnlineMainActivity, MyPageActivity::class.java),0)

            }

            android.R.id.home -> {

                finish()

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun reload(){

        loading.visibility = View.VISIBLE
        recycler_view.visibility = View.GONE

        val query = NCMBQuery<NCMBObject>("Test")

        //検索件数を5件に設定
        query.setLimit(15)

        query.whereEqualTo("language",getString(R.string.language))

        //データストアでの検索を行う
        query.findInBackground { objects, e ->
            if (e != null) {
                //エラー時の処理
                Log.e("NCMB", "検索に失敗しました。エラー:" + e.message)

                Toast.makeText(baseContext,getString(R.string.load_failed),Toast.LENGTH_SHORT).show()

                loading.visibility = View.GONE
            } else {
                //成功時の処理
                Log.i("NCMB", "検索に成功しました。")

                adapter = OnlineTestAdapter(this, objects)

                adapter.setOnClickListener(object: OnlineTestAdapter.OnClickListener{
                    override fun onClickInfoTest(obj: NCMBObject) {

                        val date = obj.getString("createDate").take(10)

                        val user = NCMBUser()

                        user.objectId = obj.getString("creatorId")

                        user.fetchInBackground { u, e ->
                            if (e != null) {
                                AlertDialog.Builder(this@OnlineMainActivity, R.style.MyAlertDialogStyle)
                                        .setTitle(obj.getString("title"))
                                        .setMessage(getString(R.string.info_test,getString(R.string.unknown_user),date,obj.getString("explanation")))
                                        .setPositiveButton("OK", null)
                                        .show()
                                //エラー時の処理
                            } else {
                                //取得成功時の処理

                                AlertDialog.Builder(this@OnlineMainActivity, R.style.MyAlertDialogStyle)
                                        .setTitle(obj.getString("title"))
                                        .setMessage(getString(R.string.info_test,u.getString("creatorName"),date,obj.getString("explanation")))
                                        .setPositiveButton("OK", null)
                                        .show()



                            }
                        }



                    }

                    override fun onClickPlayTest(obj: NCMBObject) {

                        obj.put("downloadedNum",obj.getInt("downloadedNum")+1)
                        obj.saveInBackground { e ->
                            if (e != null) {

                            } else {
                                reload()
                            }
                        }

                        val loader = AsyncLoadTest(obj.getString("content").replace("\\\n","\n").split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray(), null, realmController, this@OnlineMainActivity, -1)
                        loader.execute()

                    }

                })

                loading.visibility = View.GONE
                recycler_view.visibility = View.VISIBLE

                recycler_view.layoutManager = LinearLayoutManager(applicationContext)
                recycler_view.setHasFixedSize(true)
                recycler_view.adapter = this.adapter
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        reload()

    }
}
