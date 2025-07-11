package com.a303.helpmet.presentation.feature.navigation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a303.helpmet.R
import com.a303.helpmet.presentation.model.InstructionUi
import com.a303.helpmet.presentation.model.RouteInfo
import com.a303.helpmet.util.RouteProgressCalculator
import com.a303.helpmet.util.cache.RouteCache
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.route.RouteLine
import com.kakao.vectormap.route.RouteLineOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class RouteViewModel : ViewModel() {

    // 전체 경로 데이터
    private val _routeLineOptions = MutableStateFlow<RouteLineOptions?>(null)
    val routeLineOptions: StateFlow<RouteLineOptions?> get() = _routeLineOptions

    private val _instructionList = MutableStateFlow<List<InstructionUi>?>(null)
    val instructionList: StateFlow<List<InstructionUi>?> get() = _instructionList

    private val _destination = MutableStateFlow<InstructionUi?>(null)
    val destination : StateFlow<InstructionUi?> get() = _destination

    private val _routeInfo = MutableStateFlow<RouteInfo?>(null)
    val routeInfo: StateFlow<RouteInfo?> get() = _routeInfo

    // 스냅된 사용자 위치 (진행률에 가장 가까운 점)
    private val _snappedPosition = MutableStateFlow<LatLng?>(null)
    val snappedPosition: StateFlow<LatLng?> get() = _snappedPosition

    // 지도 위에 실제 표시되는 RouteLine 객체
    var routeLine: RouteLine? = null

    // 이전 진행률 저장을 위한 변수
    private var lastProgress: Float = 0f

    // 캐시된 경로 불러오기
    fun loadFromCache() {

        RouteCache.getRoute()?.let {
            setRouteOption(it)
        }

        RouteCache.getInstructionList()?.let {
            _instructionList.value = it
            _destination.value = it.lastOrNull()
        }

        RouteCache.getRouteInfo()?.let {
            _routeInfo.value = it
        }

    }

    // 수동으로 경로 지정
    fun setRouteOption(option: RouteLineOptions) {
        _routeLineOptions.value = null
        _routeLineOptions.value = option
    }

    fun setInstructionList(list: List<InstructionUi>){
        _instructionList.value = list
        _destination.value = list.lastOrNull()
    }

    // 외부에서 직접 진행률 업데이트
    fun updateProgress(progress: Float) {
        routeLine?.progressTo(progress, 0)
    }

    // 사용자 위치 입력 → 진행률 및 스냅 위치 계산
    fun setUserPositionAndUpdateProgress(user: LatLng) {
        val route = _routeLineOptions.value?.segments?.flatMap { it.points } ?: return

        val (progress, snapped) =
            RouteProgressCalculator.calculateProgressAndSnappedPoint(user, route, lastProgress)

        if (RouteProgressCalculator.distance(user, snapped) <= 50.0) {
            routeLine?.progressTo(progress, 0)
            _snappedPosition.value = snapped
            lastProgress = progress
        }
    }
    // TEST: 테스트용 사용자 이동 시뮬레이션
    fun simulateMovementWithProgressUpdate(
        path: List<LatLng>,
        interval: Long = 2000L,
        onPositionUpdate: (LatLng) -> Unit
    ) {
        viewModelScope.launch {
            val route = _routeLineOptions.value?.segments?.flatMap { it.points } ?: return@launch

            for (point in path) {
                val (progress, snapped) =
                    RouteProgressCalculator.calculateProgressAndSnappedPoint(point, route, lastProgress)
                routeLine?.progressTo(progress, 0)
                _snappedPosition.value = snapped
                onPositionUpdate(point)
                delay(interval)
            }
        }
    }

    fun markInstructionAsSpoken(instructionUi: InstructionUi) {
        _instructionList.update { list ->
            list?.map {
                if (it.index == instructionUi.index && it.message == instructionUi.message) it.copy(isSpoken = true) else it
            }
        }
    }

}
