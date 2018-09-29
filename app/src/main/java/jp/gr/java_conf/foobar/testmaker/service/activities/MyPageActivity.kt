package jp.gr.java_conf.foobar.testmaker.service.activities

import android.os.Bundle
import jp.gr.java_conf.foobar.testmaker.service.R

class MyPageActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)
    }
}
