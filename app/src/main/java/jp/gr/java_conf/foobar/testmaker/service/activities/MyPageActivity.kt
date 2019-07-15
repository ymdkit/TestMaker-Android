package jp.gr.java_conf.foobar.testmaker.service.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.nifcloud.mbaas.core.NCMBException
import com.nifcloud.mbaas.core.NCMBObject
import com.nifcloud.mbaas.core.NCMBQuery
import com.nifcloud.mbaas.core.NCMBUser
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.extensions.toTest
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.MyPageAdapter
import kotlinx.android.synthetic.main.activity_my_page.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MyPageActivity : BaseActivity() {

    internal lateinit var adapter: MyPageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        initToolBar()

        createAd(container)

        edit_profile.setOnClickListener { _ ->

            val dialogLayout = LayoutInflater.from(this@MyPageActivity).inflate(R.layout.dialog_edit_user_name, findViewById(R.id.layout_dialog_edit_user))

            val editUsername = dialogLayout.findViewById<EditText>(R.id.edit_user_name)

            editUsername.setText(NCMBUser.getCurrentUser().getString("creatorName"))

            val buttonSaveProfile = dialogLayout.findViewById<Button>(R.id.button_save_profile)

            val builder = AlertDialog.Builder(this@MyPageActivity, R.style.MyAlertDialogStyle)
            builder.setView(dialogLayout)
            builder.setTitle(getString(R.string.message_edit_profile))
            val dialog = builder.show()

            buttonSaveProfile.setOnClickListener{

                if(editUsername.text.toString() == ""){
                    Toast.makeText(baseContext,getString(R.string.message_shortage),Toast.LENGTH_SHORT).show()

                    return@setOnClickListener
                }

                val user = NCMBUser.getCurrentUser()

                user.put("creatorName",editUsername.text.toString())
                user.saveInBackground { e ->

                    if (e != null) {

                        //保存失敗
                        AlertDialog.Builder(this@MyPageActivity, R.style.MyAlertDialogStyle)
                                .setMessage(getString(R.string.failed_upload))
                                .setPositiveButton("OK", null)
                                .show()

                    } else {
                        //保存成功
                        AlertDialog.Builder(this@MyPageActivity, R.style.MyAlertDialogStyle)
                                .setMessage(getString(R.string.successed_edit_profile))
                                .setPositiveButton("OK", null)
                                .show()

                        reloadUserProfile()

                    }

                }

                dialog.dismiss()
            }

        }

        reloadTests()
        reloadUserProfile()

        swipe_refresh.setOnRefreshListener {
            reloadTests()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_my_page, menu)

        return true
    }

    private fun reloadTests(){

        recycler_view.visibility = View.GONE

        val query = NCMBQuery<NCMBObject>("Test")

        query.setLimit(15)

        query.whereEqualTo("language",getString(R.string.language))

        query.whereEqualTo("creatorId", NCMBUser.getCurrentUser().getString("objectId"))

        //データストアでの検索を行う
        query.findInBackground { objects, e ->
            swipe_refresh.isRefreshing = false

            if (e != null) {
                //エラー時の処理
                Log.e("NCMB", "検索に失敗しました。エラー:" + e.message)

                Toast.makeText(baseContext,getString(R.string.load_failed), Toast.LENGTH_SHORT).show()

            } else {
                //成功時の処理
                Log.i("NCMB", "検索に成功しました。")

                adapter = MyPageAdapter(this, objects)

                val questionsNum = Array(objects.size){i ->  objects[i].getInt("questionsNum")}.sum()

                val limit = if (NCMBUser.getCurrentUser().getBoolean("expert") ) Constants.ONLINE_QUESTIONS_LIMIT_EXPERT else Constants.ONLINE_QUESTIONS_LIMIT_NORMAL

                try {
                    val curUser = NCMBUser.getCurrentUser()
                    curUser.put("questionsSum",questionsNum)
                    curUser.save()
                } catch (e1: NCMBException) {
                    e1.printStackTrace()
                }

                text_num_tests.text = getString(R.string.online_num_questions,questionsNum,limit)

                adapter.setOnClickListener(object: MyPageAdapter.OnClickListener{
                    override fun onClickInfoTest(obj: NCMBObject) {

                        val date = obj.getString("createDate").take(10)

                        val dialogLayout = LayoutInflater.from(this@MyPageActivity).inflate(R.layout.dialog_online_test_info, findViewById(R.id.layout_dialog_info))

                        val textInfo = dialogLayout.findViewById<TextView>(R.id.text_info)
                        textInfo.text = getString(R.string.info_test, obj.getString("objectId"),NCMBUser.getCurrentUser().getString("creatorName"), date, obj.getString("explanation"))

                        val builder = AlertDialog.Builder(this@MyPageActivity, R.style.MyAlertDialogStyle)
                        builder.setView(dialogLayout)
                        builder.setTitle(obj.getString("title"))
                        builder.show()
                    }

                    override fun onClickPlayTest(obj: NCMBObject) {

                        obj.put("downloadedNum",obj.getInt("downloadedNum")+1)
                        obj.saveInBackground { e ->
                            if (e != null) {

                            } else {
                                reloadTests()
                            }
                        }

                        GlobalScope.launch(Dispatchers.Main) {
                            withContext(Dispatchers.Default) { obj.getString("content").replace("\\\n", "\n").toTest(baseContext) }.let{
                                realmController.convert(it,-1L)

                                Toast.makeText(baseContext, baseContext.getString(R.string.message_success_load, it.title), Toast.LENGTH_LONG).show()

                                val intent = Intent(this@MyPageActivity, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                startActivity(intent)

                            }
                        }
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

                                    reloadTests()
                                }
                            }

                        }
                        builder.setNegativeButton(android.R.string.cancel, null)
                        builder.create().show()
                    }
                })

                recycler_view.visibility = View.VISIBLE

                recycler_view.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(applicationContext)
                recycler_view.setHasFixedSize(true)
                recycler_view.adapter = this.adapter
            }
        }
    }

    private fun reloadUserProfile(){

        text_user_name.text = getString(R.string.creator_name,NCMBUser.getCurrentUser().getString("creatorName"))

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val actionId = item.itemId

        when (actionId) {
            R.id.nav_help -> {

                startActivity(Intent(Intent.ACTION_VIEW, Uri
                        .parse(getString(R.string.help_url))))

            }

            android.R.id.home -> {

                finish()

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
