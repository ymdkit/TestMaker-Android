package com.example.infra.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
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

    @SearchClient
    @Provides
    @Singleton
    fun provideSearchClient(): Retrofit = Retrofit.Builder()
        .baseUrl("https://test-maker-server.herokuapp.com")
        .addConverterFactory(
            MoshiConverterFactory.create(
                Moshi.Builder()
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
    ): SearchApi = retrofit.create(SearchApi::class.java)

    @CloudFunctionsClient
    @Provides
    @Singleton
    fun provideCloudFunctionsApi(
        @CloudFunctionsClient retrofit: Retrofit
    ): CloudFunctionsApi = retrofit.create(CloudFunctionsApi::class.java)

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFireStore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()

}