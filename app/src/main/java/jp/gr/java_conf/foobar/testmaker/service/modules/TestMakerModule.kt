package jp.gr.java_conf.foobar.testmaker.service.modules

import io.realm.Realm
import jp.gr.java_conf.foobar.testmaker.service.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.activities.*
import jp.gr.java_conf.foobar.testmaker.service.models.LocalDataSource
import jp.gr.java_conf.foobar.testmaker.service.models.RemoteDataSource
import jp.gr.java_conf.foobar.testmaker.service.models.TestMakerRepository
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

fun getTestMakerModules(realm: Realm) = module {
    single { TestMakerRepository(get(), get()) }
    single { LocalDataSource(realm, get(), get()) }
    single { RemoteDataSource(get()) }
    single { SharedPreferenceManager(get()) }
    viewModel { MainViewModel(get()) }
    viewModel { EditViewModel(get(), get()) }
    viewModel { CategorizedViewModel(get()) }
    viewModel { MoveQuestionViewModel(get()) }
    viewModel { FirebaseViewModel(get()) }
    viewModel { FirebaseMyPageViewModel(get()) }

}