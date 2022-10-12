package com.example.teachjr.di

import com.example.teachjr.data.source.repository.AuthRepository
import com.example.teachjr.data.source.repository.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class FirebaseModule {

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    fun provideAuthRepository(impl: AuthRepositoryImpl): AuthRepository {
        /**
         * Since we have constructor injection inside the 'AuthRepositoryImpl'
         * We can directly get its instance here
          */
        return impl
    }
}