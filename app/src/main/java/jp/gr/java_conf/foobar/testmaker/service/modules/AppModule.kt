package jp.gr.java_conf.foobar.testmaker.service.modules

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.google.firebase.Timestamp
import com.google.firebase.analytics.FirebaseAnalytics
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jp.gr.java_conf.foobar.testmaker.service.di.TimestampJsonAdapter
import jp.gr.java_conf.foobar.testmaker.service.infra.api.CloudFunctionsService
import jp.gr.java_conf.foobar.testmaker.service.infra.api.SearchService
import jp.studyplus.android.sdk.Studyplus
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SearchClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CloudFunctionsClient

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAnalytics(
        @ApplicationContext context: Context
    ): FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    @SearchClient
    @Provides
    @Singleton
    fun provideSearchClient(): Retrofit = Retrofit.Builder()
        .baseUrl("https://test-maker-server.herokuapp.com")
        .addConverterFactory(
            MoshiConverterFactory.create(
                Moshi.Builder()
                    .add(Timestamp::class.java, TimestampJsonAdapter())
                    .add(KotlinJsonAdapterFactory())
                    .build()
            )
        ).build()

    @CloudFunctionsClient
    @Provides
    @Singleton
    fun provideCloudFunctionsClient(): Retrofit = Retrofit.Builder()
        .baseUrl("https://us-central1-testmaker-1cb29.cloudfunctions.net/")
        .addConverterFactory(
            MoshiConverterFactory.create(
                Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()
            )
        )
        .build()

    @SearchClient
    @Provides
    @Singleton
    fun provideSearchApi(
        @SearchClient retrofit: Retrofit
    ): SearchService = retrofit.create(SearchService::class.java)

    @CloudFunctionsClient
    @Provides
    @Singleton
    fun provideCloudFunctionsApi(
        @CloudFunctionsClient retrofit: Retrofit
    ): CloudFunctionsService = retrofit.create(CloudFunctionsService::class.java)

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