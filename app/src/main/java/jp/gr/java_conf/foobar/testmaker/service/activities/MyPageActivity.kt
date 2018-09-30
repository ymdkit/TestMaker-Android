package jp.gr.java_conf.foobar.testmaker.service.activities

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.nifty.cloud.mb.core.NCMBException
import com.nifty.cloud.mb.core.NCMBObject
import com.nifty.cloud.mb.core.NCMBQuery
import com.nifty.cloud.mb.core.NCMBUser
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.AsyncLoadTest
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.MyPageAdapter
import kotlinx.android.synthetic.main.activity_my_page.*
import java.util.*


class MyPageActivity : BaseActivity() {

    internal lateinit var adapter: MyPageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        sendScreen("MyPageActivity")

        initToolBar()

        container.addView(createAd())

        text_user_name.text = getString(R.string.creator_name,NCMBUser.getCurrentUser().getString("creatorName"))

        reload()

    }

    private fun reload(){

        loading.visibility = View.VISIBLE
        recycler_view.visibility = View.GONE

        val query = NCMBQuery<NCMBObject>("Test")

        //検索件数を5件に設定
        query.setLimit(15)

        query.whereEqualTo("language",getString(R.string.language))

        query.whereEqualTo("creatorId", NCMBUser.getCurrentUser().getString("objectId"))

        //データストアでの検索を行う
        query.findInBackground { objects, e ->
            if (e != null) {
                //エラー時の処理
                Log.e("NCMB", "検索に失敗しました。エラー:" + e.message)

                Toast.makeText(baseContext,getString(R.string.load_failed), Toast.LENGTH_SHORT).show()

                loading.visibility = View.GONE
            } else {
                //成功時の処理
                Log.i("NCMB", "検索に成功しました。")

                adapter = MyPageAdapter(this, objects)

                val questionsNum = Array(objects.size){i ->  objects[i].getInt("questionsNum")}.sum()

                try {
                    val curUser = NCMBUser.getCurrentUser()
                    curUser.put("questionsSum",questionsNum)
                    curUser.save()
                } catch (e1: NCMBException) {
                    e1.printStackTrace()
                }

                text_num_tests.text = getString(R.string.online_num_questions,questionsNum)

                adapter.setOnClickListener(object: MyPageAdapter.OnClickListener{
                    override fun onClickInfoTest(obj: NCMBObject) {

                        val date = obj.getString("createDate").take(10)

                        AlertDialog.Builder(this@MyPageActivity, R.style.MyAlertDialogStyle)
                                .setTitle(obj.getString("title"))
                                .setMessage(getString(R.string.info_test,NCMBUser.getCurrentUser().getString("creatorName"),date,obj.getString("explanation")))
                                .setPositiveButton("OK", null)
                                .show()
                    }

                    override fun onClickPlayTest(obj: NCMBObject) {

                        obj.put("downloadedNum",obj.getInt("downloadedNum")+1)
                        obj.saveInBackground { e ->
                            if (e != null) {

                            } else {
                                reload()
                            }
                        }

                        val loader = AsyncLoadTest(obj.getString("content").replace("\\\n","\n").split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray(), null, realmController, this@MyPageActivity, -1)
                        loader.execute()

                    }

                    override fun onClickDeleteTest(obj: NCMBObject) {

                        val title = obj.getString("title")

                        val builder = AlertDialog.Builder(this@MyPageActivity, R.style.MyAlertDialogStyle)
                        builder.setTitle(getString(R.string.delete_exam))
                        builder.setMessage(getString(R.string.message_delete_exam, title))
                        builder.setPositiveButton(android.R.string.ok) { _, _ ->

                            obj.deleteObjectInBackground { e ->
                                if (e == null) {

                                    AlertDialog.Builder(this@MyPageActivity,R.style.MyAlertDialogStyle)
                                            .setMessage(getString(R.string.successed_delete,title))
                                            .setPositiveButton("OK", null)
                                            .show()

                                    reload()
                                }
                            }

                        }
                        builder.setNegativeButton(android.R.string.cancel, null)
                        builder.create().show()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val actionId = item.itemId

        when (actionId) {

            android.R.id.home -> {

                finish()

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
