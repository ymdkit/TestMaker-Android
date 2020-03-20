package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityEditTestBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Category
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.view.category.CategoryViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditTestActivity : BaseActivity() {

    private val inputMethodManager by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    private val testViewModel: TestViewModel by viewModel()
    private val categoryViewModel: CategoryViewModel by viewModel()
    private val editTestViewModel: EditTestViewModel by viewModel()

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityEditTestBinding>(this, R.layout.activity_edit_test).apply {
            lifecycleOwner = this@EditTestActivity
            viewModel = editTestViewModel
        }
    }

    private val controller by lazy {
        CategoryController().apply {
            setOnClickListener(object : CategoryController.OnClickListener {
                override fun onClickCategory(category: Category) {
                    selectedCategory = category
                }

                override fun onLongClickCategory(category: Category) {
                    selectedCategory = null
                }
            })
        }
    }

    private var test: Test? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd(binding.adView)

        if (intent.hasExtra("id")) {
            testViewModel.tests.find { it.id == intent.getLongExtra("id", -1L) }?.let {
                test = it
                editTestViewModel.titleTest.value = it.title
            }
        }

        binding.editTitle.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED)
            } else {
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }

        binding.recyclerView.adapter = controller.adapter

        categoryViewModel.categories.observeNonNull(this) {
            controller.categories = it
        }

        editTestViewModel.titleTest.observeNonNull(this) {
            binding.buttonAdd.isClickable = it.isNotEmpty()
        }

        binding.buttonAdd.setOnClickListener {
            sendFirebaseEvent("add-test")

            test?.let {
                testViewModel.update(it, binding.editTitle.text.toString(), binding.colorChooser.getColorId(), controller.selectedCategory?.name)
            } ?: run {
                testViewModel.create(binding.editTitle.text.toString(), binding.colorChooser.getColorId(), controller.selectedCategory?.name)
            }

            showToast(getString(R.string.message_add))
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        binding.editTitle.requestFocus()
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                android.R.id.home -> {

                    finish()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }

    companion object {

        fun startActivity(activity: Activity, id: Long) {
            val intent = Intent(activity, EditTestActivity::class.java).apply {
                putExtra("id", id)
            }
            activity.startActivity(intent)
        }

        fun startActivity(activity: Activity) {
            activity.startActivity(Intent(activity, EditTestActivity::class.java))
        }
    }
}
