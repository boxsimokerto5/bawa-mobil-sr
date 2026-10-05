package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.FleetRepository
import com.example.data.UserRole
import com.example.ui.FleetDashboardUiState
import com.example.ui.FleetViewModel
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SecurityDashboardScreen
import com.example.ui.screens.TripPlanFormScreen
import com.example.ui.screens.UserMonitorDashboardScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = FleetRepository(database.fleetDao())

        setContent {
            MyApplicationTheme {
                val fleetViewModel: FleetViewModel = viewModel(
                    factory = FleetViewModel.Factory(repository)
                )
                val uiState by fleetViewModel.uiState.collectAsStateWithLifecycle()

                BawaMobilSrApp(
                    uiState = uiState,
                    viewModel = fleetViewModel
                )
            }
        }
    }
}

@Composable
fun BawaMobilSrApp(
    uiState: FleetDashboardUiState,
    viewModel: FleetViewModel,
    modifier: Modifier = Modifier
) {
    val session = uiState.session

    // Handle system back navigation cleanly across role screens & form
    BackHandler(enabled = session.currentRole != UserRole.NONE) {
        if (session.isTripFormOpen) {
            viewModel.closeTripPlanForm()
        } else {
            viewModel.logout()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        AnimatedContent(
            targetState = Pair(session.currentRole, session.isTripFormOpen),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "bawa_mobil_sr_navigation"
        ) { (role, isFormOpen) ->
            when {
                role == UserRole.NONE -> {
                    LoginScreen(
                        defaultUserName = session.loggedInUserName,
                        defaultUserDivision = session.loggedInUserDivision,
                        defaultSecurityOfficer = session.loggedInSecurityOfficer,
                        activeTripsCount = uiState.activeTrips.size,
                        pendingTripsCount = uiState.pendingTrips.size,
                        onLoginAsUserMonitor = { name, division ->
                            viewModel.loginAsUserMonitor(name, division)
                        },
                        onLoginAsSecurity = { officer ->
                            viewModel.loginAsSecurity(officer)
                        }
                    )
                }

                role == UserRole.USER_MONITOR && isFormOpen -> {
                    TripPlanFormScreen(
                        vehicles = uiState.vehicles,
                        destinations = uiState.destinations,
                        allTrips = uiState.allTrips,
                        preselectedVehicleId = session.preselectedVehicleId,
                        defaultDriverName = session.loggedInUserName,
                        defaultDriverDivision = session.loggedInUserDivision,
                        onAddCustomDestination = { name, addr, lat, lng, onCreated ->
                            viewModel.addCustomDestination(name, addr, lat, lng, onCreated)
                        },
                        onSubmitPlan = { vehicle, driver, div, purpose, dests, dep, ret, odo ->
                            viewModel.submitTripPlan(
                                vehicle = vehicle,
                                driverName = driver,
                                driverDivision = div,
                                purpose = purpose,
                                selectedDestinations = dests,
                                departureEstimate = dep,
                                returnEstimate = ret,
                                startOdometerKm = odo,
                                onSuccess = {}
                            )
                        },
                        onCancel = { viewModel.closeTripPlanForm() }
                    )
                }

                role == UserRole.USER_MONITOR -> {
                    UserMonitorDashboardScreen(
                        uiState = uiState,
                        onOpenTripForm = { vehicleId -> viewModel.openTripPlanForm(vehicleId) },
                        onSelectTripOnMap = { tripId -> viewModel.focusTripOnMap(tripId) },
                        onCompleteTrip = { trip ->
                            viewModel.completeActiveTrip(trip.id, trip.vehicleName)
                        },
                        onAdvanceManualStep = { viewModel.advanceSingleStepManual() },
                        onLocationPermissionResult = { ctx, granted ->
                            viewModel.onLocationPermissionResult(ctx, granted)
                        },
                        onQuickSwitchRole = { viewModel.quickSwitchRole() },
                        onLogout = { viewModel.logout() },
                        onDismissBanner = { viewModel.clearBannerMessage() }
                    )
                }

                role == UserRole.SECURITY -> {
                    SecurityDashboardScreen(
                        uiState = uiState,
                        onApproveTrip = { tripId, vehicleName, notes ->
                            viewModel.approveTripRequest(tripId, vehicleName, notes)
                        },
                        onRejectTrip = { tripId, vehicleName, reason ->
                            viewModel.rejectTripRequest(tripId, vehicleName, reason)
                        },
                        onCompleteTrip = { trip ->
                            viewModel.completeActiveTrip(trip.id, trip.vehicleName)
                        },
                        onSelectTripOnMap = { tripId -> viewModel.focusTripOnMap(tripId) },
                        onAdvanceManualStep = { viewModel.advanceSingleStepManual() },
                        onLocationPermissionResult = { ctx, granted ->
                            viewModel.onLocationPermissionResult(ctx, granted)
                        },
                        onQuickSwitchRole = { viewModel.quickSwitchRole() },
                        onLogout = { viewModel.logout() },
                        onDismissBanner = { viewModel.clearBannerMessage() }
                    )
                }
            }
        }
    }
}
