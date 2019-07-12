package jp.gr.java_conf.foobar.testmaker.service.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.models.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.FirebaseTestAdapter
import kotlinx.android.synthetic.main.activity_online_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class FirebaseActivity : BaseActivity() {

    private val viewModel: FirebaseViewModel by viewModel()

    private lateinit var adapter: FirebaseTestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_main)
        swipe_refresh.isRefreshing = true

        createAd(container)

        adapter = FirebaseTestAdapter(baseContext)
        adapter.download = { data: DocumentSnapshot ->
            viewModel.downloadTest(data.id)
        }
        adapter.showInfo = { data: FirebaseTest ->
            val dialogLayout = LayoutInflater.from(this@FirebaseActivity).inflate(R.layout.dialog_online_test_info, findViewById(R.id.layout_dialog_info))

            val textInfo = dialogLayout.findViewById<TextView>(R.id.text_info)
            textInfo.text = getString(R.string.info_firebase_test, data.userName, data.getDate(), data.overview)

            val builder = AlertDialog.Builder(this@FirebaseActivity, R.style.MyAlertDialogStyle)
            builder.setView(dialogLayout)
            builder.setTitle(data.name)
            builder.show()
        }

        viewModel.getTests().observeNonNull(this) {
            recycler_view.visibility = View.VISIBLE
            swipe_refresh.isRefreshing = false

            recycler_view.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(applicationContext)
            recycler_view.setHasFixedSize(true)
            recycler_view.adapter = this.adapter
            adapter.array = it
        }

        viewModel.getDownloadTest().observeNonNull(this) {

            viewModel.convert(it)
            finish()

        }

        swipe_refresh.setOnRefreshListener {
            viewModel.fetchTests()
        }

    }
}
