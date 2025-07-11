package com.a303.helpmet.di

import com.a303.helpmet.presentation.feature.preride.RideTimeSetViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val rideTimeSetModule = module {
    viewModel { RideTimeSetViewModel() }
}