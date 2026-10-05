package com.example.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.auth.DriverAuthManager
import com.example.auth.DriverAuthResult
import com.example.data.AccountRegistrationStatus
import com.example.data.DestinationEntity
import com.example.data.FleetRepository
import com.example.data.TripRequestEntity
import com.example.data.TripStatus
import com.example.data.UserAccountEntity
import com.example.data.UserRole
import com.example.data.VehicleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AppSessionState(
    val currentRole: UserRole = UserRole.NONE,
    val activeAccountId: Int? = null,
    val loggedInUserName: String = "Bapak Andi Pratama",
    val loggedInUserDivision: String = "Guru",
    val loggedInUserEmail: String? = null,
    val authMethodBadge: String? = null,
    val isAuthLoading: Boolean = false,
    val authStatusMessage: String? = null,
    val loggedInSecurityOfficer: String = "Komandan Pos Suryo",
    val loggedInAdminUsername: String = "eccko1101",
    val isTripFormOpen: Boolean = false,
    val isMapPopupOpen: Boolean = false,
    val preselectedVehicleId: String? = null,
    val focusedMapTripId: Int? = null,
    val isGpsPermissionGranted: Boolean = false,
    val lastDeviceLat: Double? = null,
    val lastDeviceLng: Double? = null,
    val bannerMessage: String? = null
)

data class FleetDashboardUiState(
    val session: AppSessionState = AppSessionState(),
    val activeAccount: UserAccountEntity? = null,
    val vehicles: List<VehicleEntity> = emptyList(),
    val destinations: List<DestinationEntity> = emptyList(),
    val allTrips: List<TripRequestEntity> = emptyList(),
    val pendingTrips: List<TripRequestEntity> = emptyList(),
    val activeTrips: List<TripRequestEntity> = emptyList(),
    val historyTrips: List<TripRequestEntity> = emptyList(),
    val allAccounts: List<UserAccountEntity> = emptyList(),
    val pendingApprovalAccounts: List<UserAccountEntity> = emptyList(),
    val approvedAccounts: List<UserAccountEntity> = emptyList(),
    val rejectedAccounts: List<UserAccountEntity> = emptyList()
)

class FleetViewModel(
    private val repository: FleetRepository
) : ViewModel() {

    private val _session = MutableStateFlow(AppSessionState())
    val session: StateFlow<AppSessionState> = _session.asStateFlow()

    private var telemetryLoopJob: Job? = null
    private var locationManager: LocationManager? = null
    private var activeLocationListener: LocationListener? = null

    val uiState: StateFlow<FleetDashboardUiState> = combine(
        _session,
        repository.vehicles,
        repository.destinations,
        repository.tripRequests,
        repository.userAccounts
    ) { sessionState, vehicles, destinations, trips, accounts ->
        val pending = trips.filter { it.tripStatusEnum == TripStatus.PENDING_APPROVAL }
        val active = trips.filter { it.tripStatusEnum == TripStatus.IN_TRANSIT }
        val history = trips.filter {
            it.tripStatusEnum == TripStatus.COMPLETED || it.tripStatusEnum == TripStatus.REJECTED
        }

        val currentAcc = sessionState.activeAccountId?.let { id ->
            accounts.firstOrNull { it.id == id }
        } ?: sessionState.loggedInUserEmail?.let { mail ->
            accounts.firstOrNull { it.email.equals(mail, ignoreCase = true) }
        }

        val pendingAccs = accounts.filter {
            it.statusEnum == AccountRegistrationStatus.PENDING_ADMIN_APPROVAL
        }
        val approvedAccs = accounts.filter {
            it.statusEnum == AccountRegistrationStatus.APPROVED
        }
        val rejectedAccs = accounts.filter {
            it.statusEnum == AccountRegistrationStatus.REJECTED
        }

        // Automatically transition a user waiting on the WaitingAdminApproval screen if Admin approves them
        val effectiveSession = if (
            sessionState.currentRole == UserRole.WAITING_ADMIN_APPROVAL &&
            currentAcc?.statusEnum == AccountRegistrationStatus.APPROVED
        ) {
            val targetRole = if (currentAcc.isSecurityTask) UserRole.SECURITY else UserRole.USER_MONITOR
            sessionState.copy(
                currentRole = targetRole,
                loggedInUserName = currentAcc.fullName.ifBlank { sessionState.loggedInUserName },
                loggedInUserDivision = currentAcc.taskRole.ifBlank { sessionState.loggedInUserDivision },
                loggedInSecurityOfficer = if (currentAcc.isSecurityTask) {
                    currentAcc.fullName.ifBlank { sessionState.loggedInSecurityOfficer }
                } else sessionState.loggedInSecurityOfficer,
                bannerMessage = "Selamat! Akun Anda telah disetujui oleh Admin Sekolah."
            )
        } else {
            sessionState
        }

        FleetDashboardUiState(
            session = effectiveSession,
            activeAccount = currentAcc,
            vehicles = vehicles,
            destinations = destinations,
            allTrips = trips,
            pendingTrips = pending,
            activeTrips = active,
            historyTrips = history,
            allAccounts = accounts,
            pendingApprovalAccounts = pendingAccs,
            approvedAccounts = approvedAccs,
            rejectedAccounts = rejectedAccs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FleetDashboardUiState()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureSeedData()
        }
        startLiveMapTelemetryLoop()
    }

    private fun startLiveMapTelemetryLoop() {
        telemetryLoopJob?.cancel()
        telemetryLoopJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(2400L)
                repository.advanceActiveTripsTelemetry(1f)
            }
        }
    }

    fun loginAsUserMonitor(userName: String, division: String) {
        val cleanName = userName.trim().ifEmpty { "Pengguna Mobil SR" }
        val cleanDiv = division.trim().ifEmpty { "Operasional Umum" }
        _session.update {
            it.copy(
                currentRole = UserRole.USER_MONITOR,
                loggedInUserName = cleanName,
                loggedInUserDivision = cleanDiv,
                authStatusMessage = null,
                isTripFormOpen = false,
                bannerMessage = "Selamat datang, $cleanName! Anda masuk sebagai Pengguna & Pemantau."
            )
        }
    }

    fun signInDriverWithGoogle(activityContext: Context, division: String) {
        val cleanDiv = division.trim().ifEmpty { "Divisi Operasional & Logistik" }
        _session.update {
            it.copy(
                isAuthLoading = true,
                authStatusMessage = null
            )
        }
        viewModelScope.launch {
            when (val result = DriverAuthManager.signInWithGoogle(activityContext, cleanDiv)) {
                is DriverAuthResult.Success -> {
                    val profile = result.profile
                    _session.update {
                        it.copy(
                            currentRole = UserRole.USER_MONITOR,
                            loggedInUserName = profile.displayName,
                            loggedInUserDivision = cleanDiv,
                            loggedInUserEmail = profile.email,
                            authMethodBadge = profile.authMethod,
                            isAuthLoading = false,
                            authStatusMessage = null,
                            isTripFormOpen = false,
                            bannerMessage = "Terverifikasi via ${profile.authMethod}: ${profile.displayName}"
                        )
                    }
                }
                is DriverAuthResult.Error -> {
                    _session.update {
                        it.copy(
                            isAuthLoading = false,
                            authStatusMessage = result.message
                        )
                    }
                }
                DriverAuthResult.Cancelled -> {
                    _session.update {
                        it.copy(
                            isAuthLoading = false,
                            authStatusMessage = "Login Google dibatalkan oleh pengemudi."
                        )
                    }
                }
            }
        }
    }

    fun signInDriverWithFirebaseEmail(
        context: Context,
        email: String,
        password: String,
        division: String
    ) {
        val cleanDiv = division.trim().ifEmpty { "Divisi Operasional & Logistik" }
        _session.update {
            it.copy(
                isAuthLoading = true,
                authStatusMessage = null
            )
        }
        viewModelScope.launch {
            when (val result = DriverAuthManager.signInWithEmailAndPassword(context, email, password)) {
                is DriverAuthResult.Success -> {
                    val profile = result.profile
                    _session.update {
                        it.copy(
                            currentRole = UserRole.USER_MONITOR,
                            loggedInUserName = profile.displayName,
                            loggedInUserDivision = cleanDiv,
                            loggedInUserEmail = profile.email,
                            authMethodBadge = profile.authMethod,
                            isAuthLoading = false,
                            authStatusMessage = null,
                            isTripFormOpen = false,
                            bannerMessage = "Login Firebase Berhasil: ${profile.displayName} (${profile.email ?: "Terverifikasi"})"
                        )
                    }
                }
                is DriverAuthResult.Error -> {
                    _session.update {
                        it.copy(
                            isAuthLoading = false,
                            authStatusMessage = result.message
                        )
                    }
                }
                DriverAuthResult.Cancelled -> {
                    _session.update { it.copy(isAuthLoading = false) }
                }
            }
        }
    }

    fun clearAuthStatusMessage() {
        _session.update { it.copy(authStatusMessage = null) }
    }

    fun loginAsSecurity(officerName: String) {
        val cleanOfficer = officerName.trim().ifEmpty { "Komandan Pos Suryo" }
        _session.update {
            it.copy(
                currentRole = UserRole.SECURITY,
                loggedInSecurityOfficer = cleanOfficer,
                isTripFormOpen = false,
                bannerMessage = "Pos Keamanan Aktif — Petugas: $cleanOfficer"
            )
        }
    }

    fun openRegisterAccountScreen() {
        _session.update {
            it.copy(
                currentRole = UserRole.REGISTER_ACCOUNT,
                authStatusMessage = null,
                isTripFormOpen = false
            )
        }
    }

    fun registerNewAccount(
        context: Context,
        email: String,
        password: String,
        initialCategory: String
    ) {
        _session.update {
            it.copy(isAuthLoading = true, authStatusMessage = null)
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.registerNewAccount(
                email = email,
                password = password,
                initialCategory = initialCategory
            )
            result.onSuccess { account ->
                // Also try sending real Firebase verification email if Firebase is configured
                DriverAuthManager.tryRegisterAndSendFirebaseVerificationEmail(
                    context = context,
                    email = account.email,
                    password = password
                )
                launch(Dispatchers.Main) {
                    _session.update {
                        it.copy(
                            currentRole = UserRole.VERIFY_EMAIL,
                            activeAccountId = account.id,
                            loggedInUserEmail = account.email,
                            isAuthLoading = false,
                            authStatusMessage = null,
                            bannerMessage = "Pendaftaran berhasil! Silakan verifikasi email ${account.email} untuk melanjutkan."
                        )
                    }
                }
            }.onFailure { err ->
                launch(Dispatchers.Main) {
                    _session.update {
                        it.copy(
                            isAuthLoading = false,
                            authStatusMessage = err.message ?: "Gagal mendaftarkan akun."
                        )
                    }
                }
            }
        }
    }

    fun verifyEmailWithCodeOrLink(
        context: Context,
        accountId: Int,
        enteredCode: String,
        forceSimulateLinkClick: Boolean = false
    ) {
        _session.update { it.copy(isAuthLoading = true, authStatusMessage = null) }
        viewModelScope.launch(Dispatchers.IO) {
            val account = repository.getAccountById(accountId)
            if (account == null) {
                launch(Dispatchers.Main) {
                    _session.update {
                        it.copy(isAuthLoading = false, authStatusMessage = "Data akun tidak ditemukan.")
                    }
                }
                return@launch
            }

            val firebaseVerified = DriverAuthManager.checkFirebaseEmailVerified(context)
            val codeMatches = enteredCode.trim() == account.verificationCode

            if (forceSimulateLinkClick || firebaseVerified || codeMatches) {
                val updated = repository.verifyAccountEmail(accountId)
                launch(Dispatchers.Main) {
                    _session.update {
                        it.copy(
                            currentRole = UserRole.NONE,
                            activeAccountId = updated?.id ?: accountId,
                            loggedInUserEmail = updated?.email ?: account.email,
                            isAuthLoading = false,
                            authStatusMessage = "Email '${account.email}' berhasil diverifikasi! Silakan Login menggunakan email & kata sandi Anda untuk mengisi Formulir Biodata Lengkap.",
                            bannerMessage = "Verifikasi Email Berhasil! Silakan Login untuk melengkapi Formulir & Upload KTP."
                        )
                    }
                }
            } else {
                launch(Dispatchers.Main) {
                    _session.update {
                        it.copy(
                            isAuthLoading = false,
                            authStatusMessage = "Kode verifikasi belum sesuai (${enteredCode.trim()}). Masukkan kode ${account.verificationCode} atau ketuk 'Konfirmasi Link Verifikasi Email'."
                        )
                    }
                }
            }
        }
    }

    fun resendVerificationEmail(context: Context, accountId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = repository.regenerateVerificationCode(accountId) ?: return@launch
            DriverAuthManager.tryRegisterAndSendFirebaseVerificationEmail(
                context = context,
                email = updated.email,
                password = updated.password
            )
            launch(Dispatchers.Main) {
                _session.update {
                    it.copy(
                        authStatusMessage = "Email verifikasi baru telah dikirim ke ${updated.email} (Kode OTP: ${updated.verificationCode})."
                    )
                }
            }
        }
    }

    fun loginWithRegisteredAccount(email: String, password: String) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || password.isBlank()) {
            _session.update {
                it.copy(authStatusMessage = "Masukkan email dan kata sandi akun yang telah terdaftar.")
            }
            return
        }
        _session.update { it.copy(isAuthLoading = true, authStatusMessage = null) }
        viewModelScope.launch(Dispatchers.IO) {
            val account = repository.getAccountByEmail(cleanEmail)
            launch(Dispatchers.Main) {
                if (account == null) {
                    _session.update {
                        it.copy(
                            isAuthLoading = false,
                            authStatusMessage = "Akun dengan email '$cleanEmail' belum terdaftar. Silakan klik menu 'Daftar Akun Baru' terlebih dahulu."
                        )
                    }
                    return@launch
                }
                if (account.password != password) {
                    _session.update {
                        it.copy(
                            isAuthLoading = false,
                            authStatusMessage = "Kata sandi salah untuk akun '$cleanEmail'."
                        )
                    }
                    return@launch
                }

                // Route based on role-play stage:
                // 1) Not verified email -> VERIFY_EMAIL
                // 2) Verified email, form not submitted or rejected -> COMPLETE_PROFILE_FORM
                // 3) Form submitted, waiting admin -> WAITING_ADMIN_APPROVAL
                // 4) Approved by admin -> USER_MONITOR or SECURITY
                when {
                    !account.isEmailVerified ||
                        account.statusEnum == AccountRegistrationStatus.PENDING_EMAIL_VERIFICATION -> {
                        _session.update {
                            it.copy(
                                currentRole = UserRole.VERIFY_EMAIL,
                                activeAccountId = account.id,
                                loggedInUserEmail = account.email,
                                isAuthLoading = false,
                                authStatusMessage = null,
                                bannerMessage = "Email Anda belum diverifikasi. Silakan selesaikan verifikasi email terlebih dahulu."
                            )
                        }
                    }

                    !account.isProfileSubmitted ||
                        account.statusEnum == AccountRegistrationStatus.PENDING_PROFILE_COMPLETION -> {
                        _session.update {
                            it.copy(
                                currentRole = UserRole.COMPLETE_PROFILE_FORM,
                                activeAccountId = account.id,
                                loggedInUserEmail = account.email,
                                isAuthLoading = false,
                                authStatusMessage = null,
                                bannerMessage = "Login berhasil! Silakan lengkapi Formulir Data Diri, Foto, KTP & Tugas Anda."
                            )
                        }
                    }

                    account.statusEnum == AccountRegistrationStatus.PENDING_ADMIN_APPROVAL ||
                        account.statusEnum == AccountRegistrationStatus.REJECTED -> {
                        _session.update {
                            it.copy(
                                currentRole = UserRole.WAITING_ADMIN_APPROVAL,
                                activeAccountId = account.id,
                                loggedInUserEmail = account.email,
                                loggedInUserName = account.fullName.ifBlank { account.email },
                                loggedInUserDivision = account.taskRole.ifBlank { "Personel Sekolah" },
                                isAuthLoading = false,
                                authStatusMessage = null,
                                bannerMessage = if (account.statusEnum == AccountRegistrationStatus.REJECTED) {
                                    "Formulir Anda memerlukan perbaikan sesuai catatan Admin Sekolah."
                                } else {
                                    "Formulir Anda sedang menunggu persetujuan Admin Sekolah."
                                }
                            )
                        }
                    }

                    account.statusEnum == AccountRegistrationStatus.APPROVED -> {
                        val targetRole = if (account.isSecurityTask) {
                            UserRole.SECURITY
                        } else {
                            UserRole.USER_MONITOR
                        }
                        _session.update {
                            it.copy(
                                currentRole = targetRole,
                                activeAccountId = account.id,
                                loggedInUserEmail = account.email,
                                loggedInUserName = account.fullName.ifBlank { "Personel SR" },
                                loggedInUserDivision = account.taskRole.ifBlank { "Operasional" },
                                loggedInSecurityOfficer = if (account.isSecurityTask) {
                                    account.fullName.ifBlank { "Petugas Keamanan" }
                                } else it.loggedInSecurityOfficer,
                                authMethodBadge = "Terverifikasi Admin Sekolah",
                                isAuthLoading = false,
                                authStatusMessage = null,
                                isTripFormOpen = false,
                                bannerMessage = "Selamat bertugas, ${account.fullName} (${account.taskRole})!"
                            )
                        }
                    }
                }
            }
        }
    }

    fun submitCompleteProfileForm(
        accountId: Int,
        fullName: String,
        address: String,
        phoneNumber: String,
        taskRole: String,
        selfPhotoUri: String,
        ktpPhotoUri: String
    ) {
        _session.update { it.copy(isAuthLoading = true, authStatusMessage = null) }
        viewModelScope.launch(Dispatchers.IO) {
            val updated = repository.submitUserProfileForm(
                accountId = accountId,
                fullName = fullName,
                address = address,
                phoneNumber = phoneNumber,
                taskRole = taskRole,
                selfPhotoUri = selfPhotoUri,
                ktpPhotoUri = ktpPhotoUri
            )
            launch(Dispatchers.Main) {
                _session.update {
                    it.copy(
                        currentRole = UserRole.WAITING_ADMIN_APPROVAL,
                        activeAccountId = updated?.id ?: accountId,
                        loggedInUserName = fullName.trim(),
                        loggedInUserDivision = taskRole.trim(),
                        isAuthLoading = false,
                        authStatusMessage = null,
                        bannerMessage = "Formulir lengkap berhasil dikirim ke Admin Sekolah! Status: Menunggu Persetujuan."
                    )
                }
            }
        }
    }

    fun reopenProfileFormForEdit() {
        _session.update {
            it.copy(
                currentRole = UserRole.COMPLETE_PROFILE_FORM,
                authStatusMessage = null
            )
        }
    }

    fun loginAsSchoolAdmin(username: String, password: String) {
        val cleanUser = username.trim()
        if (cleanUser == "eccko1101" && password == "Woyowoyo12@") {
            _session.update {
                it.copy(
                    currentRole = UserRole.SCHOOL_ADMIN,
                    loggedInAdminUsername = cleanUser,
                    authStatusMessage = null,
                    isTripFormOpen = false,
                    bannerMessage = "Selamat datang di Panel Admin Sekolah ($cleanUser)!"
                )
            }
        } else {
            _session.update {
                it.copy(
                    authStatusMessage = "Username atau Password Admin Sekolah tidak valid! Gunakan user: eccko1101"
                )
            }
        }
    }

    fun approveUserAccountByAdmin(accountId: Int, applicantName: String, notes: String) {
        val adminUser = _session.value.loggedInAdminUsername
        viewModelScope.launch(Dispatchers.IO) {
            repository.approveUserAccountByAdmin(
                accountId = accountId,
                adminUsername = "Admin Sekolah ($adminUser)",
                notes = notes
            )
            launch(Dispatchers.Main) {
                _session.update {
                    it.copy(
                        bannerMessage = "Pendaftaran akun '$applicantName' telah DISETUJUI dan diaktifkan!"
                    )
                }
            }
        }
    }

    fun rejectUserAccountByAdmin(accountId: Int, applicantName: String, reason: String) {
        val adminUser = _session.value.loggedInAdminUsername
        viewModelScope.launch(Dispatchers.IO) {
            repository.rejectUserAccountByAdmin(
                accountId = accountId,
                adminUsername = "Admin Sekolah ($adminUser)",
                reason = reason
            )
            launch(Dispatchers.Main) {
                _session.update {
                    it.copy(
                        bannerMessage = "Formulir akun '$applicantName' telah ditolak dengan catatan evaluasi."
                    )
                }
            }
        }
    }

    fun quickSwitchRole() {
        _session.update { current ->
            val nextRole = when (current.currentRole) {
                UserRole.USER_MONITOR -> UserRole.SECURITY
                UserRole.SECURITY -> UserRole.SCHOOL_ADMIN
                UserRole.SCHOOL_ADMIN -> UserRole.USER_MONITOR
                else -> UserRole.USER_MONITOR
            }
            val msg = when (nextRole) {
                UserRole.SECURITY -> "Beralih ke Halaman Keamanan (${current.loggedInSecurityOfficer})"
                UserRole.SCHOOL_ADMIN -> "Beralih ke Halaman Admin Sekolah (${current.loggedInAdminUsername})"
                else -> "Beralih ke Halaman Pengguna & Pemantau (${current.loggedInUserName})"
            }
            current.copy(
                currentRole = nextRole,
                isTripFormOpen = false,
                bannerMessage = msg
            )
        }
    }

    fun logout(context: Context? = null) {
        if (context != null) {
            viewModelScope.launch {
                DriverAuthManager.signOut(context)
            }
        }
        _session.update {
            it.copy(
                currentRole = UserRole.NONE,
                activeAccountId = null,
                loggedInUserEmail = null,
                authMethodBadge = null,
                authStatusMessage = null,
                isTripFormOpen = false,
                bannerMessage = null
            )
        }
    }

    fun openTripPlanForm(vehicleId: String? = null) {
        _session.update {
            it.copy(
                isTripFormOpen = true,
                preselectedVehicleId = vehicleId
            )
        }
    }

    fun closeTripPlanForm() {
        _session.update {
            it.copy(
                isTripFormOpen = false,
                preselectedVehicleId = null
            )
        }
    }

    fun focusTripOnMap(tripId: Int?) {
        _session.update {
            it.copy(focusedMapTripId = tripId)
        }
    }

    fun openMapPopup(tripId: Int? = null) {
        _session.update {
            it.copy(
                isMapPopupOpen = true,
                focusedMapTripId = tripId ?: it.focusedMapTripId
            )
        }
    }

    fun closeMapPopup() {
        _session.update {
            it.copy(isMapPopupOpen = false)
        }
    }

    fun clearBannerMessage() {
        _session.update { it.copy(bannerMessage = null) }
    }

    fun addCustomDestination(
        name: String,
        addressCategory: String,
        latitude: Double,
        longitude: Double,
        onCreated: (DestinationEntity) -> Unit = {}
    ) {
        if (name.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val created = repository.addCustomDestination(
                name = name,
                addressCategory = addressCategory,
                latitude = latitude,
                longitude = longitude
            )
            launch(Dispatchers.Main) {
                onCreated(created)
                _session.update {
                    it.copy(bannerMessage = "Tujuan baru '${created.name}' berhasil ditambahkan di peta OpenStreetMap.")
                }
            }
        }
    }

    fun submitTripPlan(
        vehicle: VehicleEntity,
        driverName: String,
        driverDivision: String,
        purpose: String,
        selectedDestinations: List<DestinationEntity>,
        departureEstimate: String,
        returnEstimate: String,
        startOdometerKm: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.submitTripPlan(
                vehicle = vehicle,
                driverName = driverName,
                driverDivision = driverDivision,
                purpose = purpose,
                selectedDestinations = selectedDestinations,
                departureEstimate = departureEstimate,
                returnEstimate = returnEstimate,
                startOdometerKm = startOdometerKm
            )
            launch(Dispatchers.Main) {
                _session.update {
                    it.copy(
                        isTripFormOpen = false,
                        preselectedVehicleId = null,
                        bannerMessage = "Rencana bawa ${vehicle.name} terkirim ke Pos Keamanan! Menunggu persetujuan."
                    )
                }
                onSuccess()
            }
        }
    }

    fun approveTripRequest(tripId: Int, vehicleName: String, notes: String) {
        val officer = _session.value.loggedInSecurityOfficer
        viewModelScope.launch(Dispatchers.IO) {
            repository.approveTrip(tripId = tripId, officerName = officer, notes = notes)
            launch(Dispatchers.Main) {
                _session.update {
                    it.copy(
                        focusedMapTripId = tripId,
                        bannerMessage = "Disetujui! Status $vehicleName kini 'Dalam Perjalanan' & dipantau di Peta Real-Time."
                    )
                }
            }
        }
    }

    fun rejectTripRequest(tripId: Int, vehicleName: String, reason: String) {
        val officer = _session.value.loggedInSecurityOfficer
        viewModelScope.launch(Dispatchers.IO) {
            repository.rejectTrip(tripId = tripId, officerName = officer, reason = reason)
            launch(Dispatchers.Main) {
                _session.update {
                    it.copy(
                        bannerMessage = "Pengajuan rencana bawa $vehicleName telah ditolak."
                    )
                }
            }
        }
    }

    fun completeActiveTrip(tripId: Int, vehicleName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.completeTrip(
                tripId = tripId,
                completionNote = "Kendaraan telah kembali ke Pos Utama SR."
            )
            launch(Dispatchers.Main) {
                _session.update {
                    it.copy(
                        bannerMessage = "Perjalanan $vehicleName selesai. Unit kini kembali Tersedia di Garasi."
                    )
                }
            }
        }
    }

    fun advanceSingleStepManual() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.advanceActiveTripsTelemetry(2.2f)
        }
    }

    @SuppressLint("MissingPermission")
    fun onLocationPermissionResult(context: Context, granted: Boolean) {
        _session.update { it.copy(isGpsPermissionGranted = granted) }
        if (!granted) return

        try {
            val mgr = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
            locationManager = mgr

            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val lat = location.latitude
                    val lng = location.longitude
                    val speedKmh = ((location.speed * 3.6f).toInt()).coerceAtLeast(15)
                    _session.update {
                        it.copy(
                            lastDeviceLat = lat,
                            lastDeviceLng = lng
                        )
                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.updateLiveDeviceGpsForDriver(
                            driverName = _session.value.loggedInUserName,
                            lat = lat,
                            lng = lng,
                            speedKmh = speedKmh
                        )
                    }
                }

                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            activeLocationListener = listener

            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            for (provider in providers) {
                if (mgr.isProviderEnabled(provider)) {
                    mgr.requestLocationUpdates(provider, 4000L, 5f, listener)
                    val lastKnown = mgr.getLastKnownLocation(provider)
                    if (lastKnown != null) {
                        _session.update {
                            it.copy(
                                lastDeviceLat = lastKnown.latitude,
                                lastDeviceLng = lastKnown.longitude
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore in environments without physical GPS hardware
        }
    }

    override fun onCleared() {
        super.onCleared()
        telemetryLoopJob?.cancel()
        try {
            activeLocationListener?.let { locationManager?.removeUpdates(it) }
        } catch (_: Exception) {
        }
    }

    class Factory(private val repository: FleetRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FleetViewModel(repository) as T
        }
    }
}
