package jp.gr.java_conf.foobar.testmaker.service.di

import io.realm.Realm
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.infra.db.LocalDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.RemoteDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.test.TestMakerRepository
import jp.gr.java_conf.foobar.testmaker.service.view.category.CategorizedViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.edit.EditProViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.edit.EditViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.move.MoveQuestionViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.online.FirebaseMyPageViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.online.FirebaseViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.play.PlayViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.result.ResultViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.ShowTestsViewModel
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
    viewModel { EditProViewModel(get()) }
    viewModel { PlayViewModel(get()) }
    viewModel { ResultViewModel(get()) }
    viewModel { ShowTestsViewModel(get()) }

}