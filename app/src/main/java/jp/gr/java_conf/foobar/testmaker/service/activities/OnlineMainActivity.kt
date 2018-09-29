package jp.gr.java_conf.foobar.testmaker.service.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import jp.gr.java_conf.foobar.testmaker.service.R
import com.nifty.cloud.mb.core.NCMB
import com.nifty.cloud.mb.core.NCMBObject
import com.nifty.cloud.mb.core.NCMBQuery
import jp.gr.java_conf.foobar.testmaker.service.models.AsyncLoadTest
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.OnlineTestAdapter
import kotlinx.android.synthetic.main.activity_online_main.*


class OnlineMainActivity : BaseActivity() {

    internal lateinit var adapter: OnlineTestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_main)


        NCMB.initialize(this.applicationContext,"11a0bc05538273ecd8e5d6152a9379119f16115c19082eae88c101adeb963f15","afc1899b2ed65520fc935e8d680723828a09203347d18be035408491451262c8")

        val query = NCMBQuery<NCMBObject>("Test")

//検索件数を5件に設定
        query.setLimit(5)

//データストアでの検索を行う
        query.findInBackground { objects, e ->
            if (e != null) {
                //エラー時の処理
                Log.e("NCMB", "検索に失敗しました。エラー:" + e.message)
            } else {
                //成功時の処理
                Log.i("NCMB", "検索に成功しました。")
                //ListViewオブジェクトの取得 // ループカウンタ

                adapter = OnlineTestAdapter(this, objects)

                adapter.setOnClickListener(object: OnlineTestAdapter.OnClickListener{
                    override fun onClickPlayTest(content: String) {

                        val loader = AsyncLoadTest(content.replace("\\\n","\n").split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray(), null, realmController, this@OnlineMainActivity, -1)
                        loader.execute()

                        Toast.makeText(this@OnlineMainActivity,content,Toast.LENGTH_SHORT).show()
                    }

                })

                recycler_view.layoutManager = LinearLayoutManager(applicationContext)
                recycler_view.setHasFixedSize(true)
                recycler_view.adapter = this.adapter
            }
        }



    }
}
