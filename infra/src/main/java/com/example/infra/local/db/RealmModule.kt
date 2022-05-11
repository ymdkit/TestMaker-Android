package com.example.infra.local.db

import android.content.Context
import com.example.infra.local.Migration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.realm.Realm
import io.realm.RealmConfiguration
import java.io.FileNotFoundException
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalSourceModule {

    @Provides
    @Singleton
    fun provideRealm(
        @ApplicationContext context: Context
    ): Realm {
        Realm.init(context)
        val config = RealmConfiguration.Builder()
            .schemaVersion(19)
            .build()

        try {
            Realm.migrateRealm(config, Migration())
        } catch (ignored: FileNotFoundException) {
            // If the Realm file doesn't exist, just ignore.
        }
        return Realm.getInstance(config)
    }
}