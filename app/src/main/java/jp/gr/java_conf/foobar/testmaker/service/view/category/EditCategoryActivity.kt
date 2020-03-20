package jp.gr.java_conf.foobar.testmaker.service.view.category

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityEditCategoryBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Category
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditCategoryActivity : BaseActivity() {
    private val inputMethodManager by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    private val editCategoryViewModel: EditCategoryViewModel by viewModel()
    private val categoryViewModel: CategoryViewModel by viewModel()

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityEditCategoryBinding>(this, R.layout.activity_edit_category).apply {
            lifecycleOwner = this@EditCategoryActivity
            viewModel = editCategoryViewModel
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd(binding.adView)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        editCategoryViewModel.titleCategory.observeNonNull(this) {
            binding.buttonAdd.isClickable = it.isNotEmpty()
        }

        binding.editTitle.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED)
            } else {
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }

        binding.buttonAdd.setOnClickListener {
            sendFirebaseEvent("add-category")
            editCategoryViewModel.titleCategory.value?.let {
                categoryViewModel.create(Category(name = it, color = binding.colorChooser.getColorId())).also { id ->
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra("category_id", id)
                    })
                    finish()
                }
            }
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
        fun startActivityForResult(activity: Activity, requestCode: Int) {
            activity.startActivityForResult(Intent(activity, EditCategoryActivity::class.java), requestCode)
        }
    }
}
