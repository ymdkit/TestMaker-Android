package jp.gr.java_conf.foobar.testmaker.service.di

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.realm.Realm
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.api.CloudFunctionsService
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.db.CategoryDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.db.LocalDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.infra.db.TestDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.RemoteDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.CategoryRepository
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestRepository
import jp.gr.java_conf.foobar.testmaker.service.view.category.CategoryViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.category.EditCategoryViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.edit.*
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.online.FirebaseMyPageViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.online.FirebaseViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.play.PlayViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.ShowTestsViewModel
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

fun getTestMakerModules(realm: Realm) = module {
    single { TestMakerRepository(get(), get()) }
    single { TestRepository(get()) }
    single { CategoryRepository(get(), get()) }
    single { LocalDataSource(realm, get(), get()) }
    single { CategoryDataSource(realm) }
    single { TestDataSource(realm) }
    single { Auth() }
    single { RemoteDataSource(get(), get()) }
    single { SharedPreferenceManager(get()) }
    single {
        Retrofit.Builder()
                .baseUrl("https://us-central1-testmaker-1cb29.cloudfunctions.net/")
                .addConverterFactory(MoshiConverterFactory.create(Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()))
                .build()
    }
    single { get<Retrofit>().create(CloudFunctionsService::class.java) }
    viewModel { CategoryViewModel(get()) }
    viewModel { TestViewModel(get()) }
    viewModel { MainViewModel(get(), get(), get(), get()) }
    viewModel { EditTestViewModel() }
    viewModel { EditWriteQuestionViewModel(get()) }
    viewModel { EditSelectQuestionViewModel(get()) }
    viewModel { EditCompleteQuestionViewModel(get()) }
    viewModel { EditSelectCompleteQuestionViewModel(get()) }
    viewModel { EditCategoryViewModel() }
    viewModel { FirebaseViewModel(get(), get()) }
    viewModel { FirebaseMyPageViewModel(get(), get()) }
    viewModel { (test: Test, questions: List<Question>) -> PlayViewModel(test, questions, get()) }
    viewModel { ShowTestsViewModel(get(), get(), get()) }

}