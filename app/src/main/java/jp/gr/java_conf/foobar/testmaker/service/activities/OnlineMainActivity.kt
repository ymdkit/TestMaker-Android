package jp.gr.java_conf.foobar.testmaker.service.activities

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.nifty.cloud.mb.core.NCMB
import com.nifty.cloud.mb.core.NCMBObject
import com.nifty.cloud.mb.core.NCMBQuery
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.AsyncLoadTest
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.OnlineTestAdapter
import kotlinx.android.synthetic.main.activity_online_main.*


class OnlineMainActivity : BaseActivity() {

    internal lateinit var adapter: OnlineTestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_main)

        container.addView(createAd())

        initToolBar()

        expand.setOnClickListener {




            val obj = NCMBObject("Test")
            obj.put("content", realmController.list[0].testToString(this@OnlineMainActivity))
            obj.put("title",realmController.list[0].title)
            obj.put("color",realmController.list[0].color)
            obj.put("language",getString(R.string.language))
            obj.put("downloadedNum",0)
            obj.put("questionsNum",realmController.list[0].getQuestionsForEach().size)
            obj.saveInBackground { e ->
                if (e != null) {
                    //保存失敗
                    AlertDialog.Builder(this@OnlineMainActivity)
                            .setTitle("Notification from Nifty")
                            .setMessage("Error:" + e.message)
                            .setPositiveButton("OK", null)
                            .show()

                } else {
                    //保存成功
                    AlertDialog.Builder(this@OnlineMainActivity)
                            .setTitle("Notification from Nifty")
                            .setMessage("Save successful! with ID:" + obj.objectId)
                            .setPositiveButton("OK", null)
                            .show()

                    reload()

                }
            }
        }

        NCMB.initialize(this.applicationContext,"11a0bc05538273ecd8e5d6152a9379119f16115c19082eae88c101adeb963f15","afc1899b2ed65520fc935e8d680723828a09203347d18be035408491451262c8")

        reload()
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

            android.R.id.home -> {

                finish()

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun reload(){

        val query = NCMBQuery<NCMBObject>("Test")

        //検索件数を5件に設定
        query.setLimit(5)

        query.whereEqualTo("language",getString(R.string.language))

        //データストアでの検索を行う
        query.findInBackground { objects, e ->
            if (e != null) {
                //エラー時の処理
                Log.e("NCMB", "検索に失敗しました。エラー:" + e.message)
            } else {
                //成功時の処理
                Log.i("NCMB", "検索に成功しました。")

                adapter = OnlineTestAdapter(this, objects)

                adapter.setOnClickListener(object: OnlineTestAdapter.OnClickListener{
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

                recycler_view.layoutManager = LinearLayoutManager(applicationContext)
                recycler_view.setHasFixedSize(true)
                recycler_view.adapter = this.adapter
            }
        }

    }
}
