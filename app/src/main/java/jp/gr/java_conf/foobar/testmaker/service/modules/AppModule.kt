package jp.gr.java_conf.foobar.testmaker.service.modules

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.studyplus.android.sdk.Studyplus
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAnalytics(
        @ApplicationContext context: Context
    ): FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    @Provides
    @Singleton
    fun provideStudyPlus(
        @ApplicationContext context: Context,
        info: ApplicationInfo
    ): Studyplus = Studyplus(
        context = context,
        consumerKey = info.metaData.getString("studyplus_comsumer_key")!!,
        consumerSecret = info.metaData.getString("secret_studyplus_comsumer_key")!!
    )

    @Provides
    @Singleton
    fun provideApplicationInfo(
        @ApplicationContext context: Context
    ): ApplicationInfo = context.packageManager.getApplicationInfo(
        context.packageName,
        PackageManager.GET_META_DATA
    )
}