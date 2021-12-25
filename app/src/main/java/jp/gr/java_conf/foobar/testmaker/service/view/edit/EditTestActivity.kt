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
import jp.gr.java_conf.foobar.testmaker.service.domain.CreateTestSource
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.category.CategoryViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.category.EditCategoryActivity
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditTestActivity : BaseActivity() {

    private val inputMethodManager by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    private val testViewModel: TestViewModel by viewModel()
    private val categoryViewModel: CategoryViewModel by viewModel()
    private val editTestViewModel: EditTestViewModel by viewModel()

    private val logger: TestMakerLogger by inject()

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityEditTestBinding>(this, R.layout.activity_edit_test).apply {
            lifecycleOwner = this@EditTestActivity
            viewModel = editTestViewModel
        }
    }

    private val controller by lazy {
        CategoryController(
                object : CategoryController.OnCategoryEventListener {
                    override fun onClickAddCategory() {
                        EditCategoryActivity.startActivityForResult(this@EditTestActivity, REQUEST_EDIT_CATEGORY)
                    }

                    override fun onClickDeleteCategory(category: Category) {
                        categoryViewModel.delete(category)
                    }
                }
        )
    }

    private var test: Test? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd(binding.adView)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (intent.hasExtra("id")) {
            testViewModel.tests.find { it.id == intent.getLongExtra("id", -1L) }?.let {
                test = it
                editTestViewModel.titleTest.value = it.title
                binding.colorChooser.setColorId(it.color)
                controller.selectedCategory = categoryViewModel.categoriesLiveData.value?.find { category -> it.category == category.name }
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

        categoryViewModel.categoriesLiveData.observeNonNull(this) {
            controller.categories = it
        }

        binding.buttonAdd.setOnClickListener {
            test?.let {
                testViewModel.update(it, binding.editTitle.text.toString(), binding.colorChooser.getColorId(), controller.selectedCategory?.name)
            } ?: run {
                testViewModel.create(
                    title = binding.editTitle.text.toString(),
                    color = binding.colorChooser.getColorId(),
                    category = controller.selectedCategory?.name,
                    source = CreateTestSource.SELF.title)
                logger.logCreateTestEvent(binding.editTitle.text.toString(), CreateTestSource.SELF.title)
            }

            showToast(getString(R.string.msg_save_test))
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQUEST_EDIT_CATEGORY -> {
                controller.selectedCategory = categoryViewModel.get(data?.getLongExtra("category_id", -1L)
                        ?: -1L)
            }
        }
    }

    companion object {
        const val REQUEST_EDIT_CATEGORY = 10000

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
