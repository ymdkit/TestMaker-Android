package jp.gr.java_conf.foobar.testmaker.service.di

import android.content.pm.ApplicationInfo
import com.google.firebase.analytics.FirebaseAnalytics
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.realm.Realm
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.api.CloudFunctionsService
import jp.gr.java_conf.foobar.testmaker.service.infra.api.SearchClient
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.db.CategoryDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.db.LocalDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.infra.db.TestDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.RemoteDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.*
import jp.gr.java_conf.foobar.testmaker.service.view.category.CategoryViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.category.EditCategoryViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.edit.*
import jp.gr.java_conf.foobar.testmaker.service.view.group.GroupDetailViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.group.GroupListViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.group.HistoryTestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.main.LocalMainViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.online.FirebaseMyPageViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.online.FirebaseViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.play.PlayViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.result.ResultViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.ShowTestsViewModel
import jp.studyplus.android.sdk.Studyplus
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

fun getTestMakerModules(realm: Realm, info: ApplicationInfo) = module {
    single { TestMakerRepository(get(), get()) }
    single { TestRepository(get()) }
    single { CategoryRepository(get(), get()) }
    single { GroupRepository(get()) }
    single { HistoryRepository(get()) }
    single { LocalDataSource(realm, get()) }
    single { CategoryDataSource(realm) }
    single { TestDataSource(realm) }
    single { Auth() }
    single { RemoteDataSource(get(), get()) }
    single { SharedPreferenceManager(get()) }
    single {
        Retrofit.Builder()
            .baseUrl("https://us-central1-testmaker-1cb29.cloudfunctions.net/")
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()
                )
            )
            .build()
    }
    single { get<Retrofit>().create(CloudFunctionsService::class.java) }
    single { FirebaseAnalytics.getInstance(get()) }
    single { SearchClient() }
    single { get<SearchClient>().create() }
    single {
        Studyplus(
            context = get(),
            consumerKey = info.metaData.getString("studyplus_comsumer_key")!!,
            consumerSecret = info.metaData.getString("secret_studyplus_comsumer_key")!!
        )
    }
    single { TestMakerLogger(get()) }
    viewModel { CategoryViewModel(get()) }
    viewModel { TestViewModel(get()) }
    viewModel { MainViewModel(get(), get(), get(), get()) }
    viewModel { EditTestViewModel() }
    viewModel { EditWriteQuestionViewModel(get()) }
    viewModel { EditSelectQuestionViewModel(get()) }
    viewModel { EditCompleteQuestionViewModel(get()) }
    viewModel { EditSelectCompleteQuestionViewModel(get()) }
    viewModel { EditCategoryViewModel() }
    viewModel { FirebaseViewModel(get(), get(), get()) }
    viewModel { FirebaseMyPageViewModel(get(), get()) }
    viewModel { (test: Test, questions: List<Question>) -> PlayViewModel(test, questions, get()) }
    viewModel { LocalMainViewModel(get(), get()) }
    viewModel { ShowTestsViewModel(get(), get(), get()) }
    viewModel { GroupListViewModel(get()) }
    viewModel { GroupDetailViewModel(get(), get()) }
    viewModel { HistoryTestViewModel(get()) }
    viewModel { (testId: Long) -> ResultViewModel(testId = testId, get(), get(), get()) }

}