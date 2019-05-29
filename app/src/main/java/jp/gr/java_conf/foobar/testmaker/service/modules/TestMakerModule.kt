package jp.gr.java_conf.foobar.testmaker.service.modules

import jp.gr.java_conf.foobar.testmaker.service.models.LocalDataSource
import jp.gr.java_conf.foobar.testmaker.service.models.RemoteDataSource
import jp.gr.java_conf.foobar.testmaker.service.models.TestMakerRepository
import org.koin.dsl.module.module

fun getTestMakerModules() = module {
    single { TestMakerRepository(get(), get()) }
    single { LocalDataSource() }
    single { RemoteDataSource() }
}