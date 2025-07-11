package com.a303.helpmet.di

import android.content.Context
import com.a303.helpmet.data.network.RetrofitProvider
import com.a303.helpmet.data.network.interceptor.ErrorInterceptor
import com.a303.helpmet.data.repository.DeviceRepository
import com.a303.helpmet.data.service.FakeNavigationService
import com.a303.helpmet.data.service.NavigationService
import com.a303.helpmet.domain.usecase.GetCellularNetworkUseCase
import com.a303.helpmet.domain.usecase.GetWifiNetworkUseCase
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module

val networkModule = module {
    // Json 파서
    single { Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    } }

    // 로깅 인터셉터
    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }


    // 에러 처리 인터셉터
    single<Interceptor> {
        ErrorInterceptor()
    }

//    single {
//        RetrofitProvider(
//            jsonParser = get(),
//            loggingInterceptor = get(),
//            errorInterceptor = get()
//        )
//    }
    single { (context: Context) ->
        RetrofitProvider(
            context = context,
            jsonParser = get(),
            loggingInterceptor = get(),
            errorInterceptor = get()
        )
    }


    // NavigationService를 RetrofitProvider에서 꺼내 등록
    single<NavigationService> { get<RetrofitProvider>().navigationService }
    single { get<RetrofitProvider>().deviceService }

    single { DeviceRepository(get()) }


    single { GetCellularNetworkUseCase(get()) } // context 주입 필요
    single { GetWifiNetworkUseCase(get()) }
}

val fakeNetworkModule = module {
    single<NavigationService> {
        // Json 설정은 networkModule과 동일하게
        FakeNavigationService()
    }
}
