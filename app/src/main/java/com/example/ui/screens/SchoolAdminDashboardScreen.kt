package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AccountRegistrationStatus
import com.example.data.TripRequestEntity
import com.example.data.UserAccountEntity
import com.example.data.UserRole
import com.example.ui.FleetDashboardUiState
import com.example.ui.components.VintageNearFullScreenMapPopup
import com.example.ui.components.VintageNotificationBanner
import com.example.ui.components.VintageOrnamentalDivider
import com.example.ui.components.VintageRealTimeMapPanel
import com.example.ui.components.VintageTopBar
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.CopperRustRed
import com.example.ui.theme.DeepInkBrown
import com.example.ui.theme.EspressoBrown
import com.example.ui.theme.ForestEmerald
import com.example.ui.theme.MetallicGold
import com.example.ui.theme.RichLeatherBrown
import com.example.ui.theme.SoftGoldHighlight
import com.example.ui.theme.SoftMochaText
import com.example.ui.theme.VintageAmberWarning
import com.example.ui.theme.VintageCardCream
import com.example.ui.theme.VintageCreamBg
import com.example.ui.theme.VintageCrimsonBg
import com.example.ui.theme.VintageCrimsonReject
import com.example.ui.theme.VintageGreenBg
import com.example.ui.theme.VintageGreenSuccess
import com.example.ui.theme.VintageParchmentSurface
import com.example.ui.theme.VintageWarmBorder

@Composable
fun SchoolAdminDashboardScreen(
    uiState: FleetDashboardUiState,
    onApproveAccount: (accountId: Int, applicantName: String, notes: String) -> Unit,
    onRejectAccount: (accountId: Int, applicantName: String, reason: String) -> Unit,
    onCompleteTrip: (TripRequestEntity) -> Unit,
    onSelectTripOnMap: (Int?) -> Unit,
    onOpenMapPopup: (Int?) -> Unit,
    onCloseMapPopup: () -> Unit,
    onAdvanceManualStep: () -> Unit,
    onLocationPermissionResult: (android.content.Context, Boolean) -> Unit,
    onQuickSwitchRole: () -> Unit,
    onLogout: () -> Unit,
    onDismissBanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 0 = Persetujuan Pendaftaran Akun, 1 = Direktori Personel Sekolah, 2 = Peta Armada Sekolah
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var inspectedAccount by remember { mutableStateOf<UserAccountEntity?>(null) }

    if (uiState.session.isMapPopupOpen) {
        VintageNearFullScreenMapPopup(
            activeTrips = uiState.activeTrips,
            allDestinations = uiState.destinations,
            focusedTripId = uiState.session.focusedMapTripId,
            isGpsEnabled = uiState.session.isGpsPermissionGranted,
            onSelectTrip = onSelectTripOnMap,
            onCompleteTrip = { trip ->
                onCompleteTrip(trip)
                onCloseMapPopup()
            },
            onAdvanceManualStep = onAdvanceManualStep,
            onLocationPermissionResult = onLocationPermissionResult,
            onDismiss = onCloseMapPopup,
            allTrips = uiState.allTrips
        )
    }

    if (inspectedAccount != null) {
        val acc = inspectedAccount!!
        AlertDialog(
            onDismissRequest = { inspectedAccount = null },
            containerColor = VintageCardCream,
            titleContentColor = EspressoBrown,
            textContentColor = DeepInkBrown,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = EspressoBrown
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Berkas Foto & KTP Pendaftar",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "${acc.fullName.ifBlank { acc.email }} • Tugas: ${acc.taskRole.ifBlank { "-" }}",
                        style = MaterialTheme.typography.labelLarge,
                        color = EspressoBrown,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "1. Foto Diri Personel:",
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftMochaText
                    )
                    AccountMediaPreviewBox(
                        label = "Foto Diri",
                        uriString = acc.selfPhotoUri,
                        isKtp = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    )
                    Text(
                        text = "2. Foto KTP (Kartu Tanda Penduduk):",
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftMochaText
                    )
                    AccountMediaPreviewBox(
                        label = "Foto KTP",
                        uriString = acc.ktpPhotoUri,
                        isKtp = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                    )
                    Text(
                        text = "Alamat: ${acc.address}\nNo. HP: ${acc.phoneNumber}\nEmail: ${acc.email} (Terverifikasi)",
                        style = MaterialTheme.typography.bodySmall,
                        color = DeepInkBrown
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { inspectedAccount = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EspressoBrown,
                        contentColor = SoftGoldHighlight
                    )
                ) {
                    Text("Tutup Pratinjau")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VintageCreamBg,
        topBar = {
            VintageTopBar(
                title = "Admin Sekolah SR",
                subtitle = "User: ${uiState.session.loggedInAdminUsername} • Verifikasi Akun & Armada",
                currentRole = UserRole.SCHOOL_ADMIN,
                pendingCountForBadge = uiState.pendingApprovalAccounts.size,
                onQuickSwitchRole = onQuickSwitchRole,
                onLogout = onLogout
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = VintageParchmentSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (uiState.pendingApprovalAccounts.isNotEmpty()) {
                                    Badge(
                                        containerColor = CopperRustRed,
                                        contentColor = Color.White
                                    ) {
                                        Text("${uiState.pendingApprovalAccounts.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.HowToReg,
                                contentDescription = "Verifikasi Akun"
                            )
                        }
                    },
                    label = { Text("Verifikasi Akun (${uiState.pendingApprovalAccounts.size})") },
                    modifier = Modifier.testTag("admin_tab_approvals")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Data Personel"
                        )
                    },
                    label = { Text("Personel (${uiState.approvedAccounts.size})") },
                    modifier = Modifier.testTag("admin_tab_personnel")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Peta Armada"
                        )
                    },
                    label = { Text("Peta Armada (${uiState.activeTrips.size})") },
                    modifier = Modifier.testTag("admin_tab_map")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 14.dp,
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (uiState.session.bannerMessage != null) {
                    item {
                        VintageNotificationBanner(
                            message = uiState.session.bannerMessage,
                            onDismiss = onDismissBanner
                        )
                    }
                }

                // Summary Header Card for School Admin
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = EspressoBrown),
                        border = BorderStroke(2.dp, MetallicGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = MetallicGold,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "PANEL OTORITAS ADMIN SEKOLAH",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MetallicGold,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Verifikasi Biodata, KTP & Tugas Personel Sekolah SR",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = VintageCreamBg.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AdminStatChip(
                                    count = uiState.pendingApprovalAccounts.size.toString(),
                                    label = "Menunggu",
                                    accent = MetallicGold,
                                    modifier = Modifier.weight(1f)
                                )
                                AdminStatChip(
                                    count = uiState.approvedAccounts.size.toString(),
                                    label = "Disetujui",
                                    accent = SoftGoldHighlight,
                                    modifier = Modifier.weight(1f)
                                )
                                AdminStatChip(
                                    count = uiState.activeTrips.size.toString(),
                                    label = "Mobil Jalan",
                                    accent = MetallicGold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                when (selectedTab) {
                    0 -> {
                        item {
                            VintageOrnamentalDivider(
                                label = "PERMOHONAN PENDAFTARAN AKUN (${uiState.pendingApprovalAccounts.size})"
                            )
                        }

                        if (uiState.pendingApprovalAccounts.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = VintageCardCream),
                                    border = BorderStroke(1.dp, VintageWarmBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = ForestEmerald,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Semua Formulir Pendaftaran Telah Diverifikasi",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = EspressoBrown,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Belum ada pengajuan akun baru yang menunggu persetujuan Admin Sekolah.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftMochaText,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(
                                items = uiState.pendingApprovalAccounts,
                                key = { it.id }
                            ) { account ->
                                PendingAccountApprovalCard(
                                    account = account,
                                    onInspectPhotos = { inspectedAccount = account },
                                    onApprove = { notes ->
                                        onApproveAccount(
                                            account.id,
                                            account.fullName.ifBlank { account.email },
                                            notes
                                        )
                                    },
                                    onReject = { reason ->
                                        onRejectAccount(
                                            account.id,
                                            account.fullName.ifBlank { account.email },
                                            reason
                                        )
                                    }
                                )
                            }
                        }
                    }

                    1 -> {
                        item {
                            VintageOrnamentalDivider(
                                label = "PERSONEL AKTIF & RIWAYAT PENDAFTARAN (${uiState.allAccounts.size})"
                            )
                        }

                        items(
                            items = uiState.allAccounts,
                            key = { it.id }
                        ) { account ->
                            PersonnelDirectoryCard(
                                account = account,
                                onInspectPhotos = { inspectedAccount = account },
                                onQuickApproveIfNotApproved = {
                                    onApproveAccount(
                                        account.id,
                                        account.fullName.ifBlank { account.email },
                                        "Disetujui langsung oleh Admin Sekolah (eccko1101)."
                                    )
                                }
                            )
                        }
                    }

                    2 -> {
                        item {
                            VintageOrnamentalDivider(label = "PEMANTAUAN PETA ARMADA SEKOLAH")
                        }

                        item {
                            VintageRealTimeMapPanel(
                                activeTrips = uiState.activeTrips,
                                allDestinations = uiState.destinations,
                                focusedTripId = uiState.session.focusedMapTripId,
                                isGpsEnabled = uiState.session.isGpsPermissionGranted,
                                onSelectTrip = onSelectTripOnMap,
                                onCompleteTrip = onCompleteTrip,
                                onAdvanceManualStep = onAdvanceManualStep,
                                onLocationPermissionResult = onLocationPermissionResult,
                                isInsidePopup = false,
                                onOpenFullPopup = {
                                    onOpenMapPopup(uiState.session.focusedMapTripId)
                                },
                                allTrips = uiState.allTrips
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingAccountApprovalCard(
    account: UserAccountEntity,
    onInspectPhotos: () -> Unit,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    var adminNote by remember(account.id) {
        mutableStateOf("Berkas KTP, Foto Diri, dan penugasan ${account.taskRole} telah diverifikasi.")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_pending_account_card_${account.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = VintageCardCream),
        border = BorderStroke(2.dp, AntiqueGold),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = VintageAmberWarning.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, VintageAmberWarning)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = EspressoBrown,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "MENUNGGU PERSETUJUAN ADMIN",
                            style = MaterialTheme.typography.labelSmall,
                            color = EspressoBrown,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (account.isSecurityTask) EspressoBrown else SoftGoldHighlight,
                    border = BorderStroke(1.dp, AntiqueGold)
                ) {
                    Text(
                        text = "TUGAS: ${account.taskRole.ifBlank { account.initialAccountCategory }.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (account.isSecurityTask) SoftGoldHighlight else DeepInkBrown,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = account.fullName.ifBlank { "Nama Belum Diisi" },
                style = MaterialTheme.typography.titleLarge,
                color = EspressoBrown,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Details Box
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VintageParchmentSurface),
                border = BorderStroke(1.dp, VintageWarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AdminInfoLine(
                        icon = Icons.Default.Email,
                        label = "Email (Terverifikasi)",
                        value = account.email
                    )
                    AdminInfoLine(
                        icon = Icons.Default.Work,
                        label = "Tugas / Jabatan",
                        value = "${account.taskRole} (${if (account.isSecurityTask) "Akses Keamanan" else "Akses Pengguna & Pemantau"})"
                    )
                    AdminInfoLine(
                        icon = Icons.Default.Phone,
                        label = "Nomor HP",
                        value = account.phoneNumber
                    )
                    AdminInfoLine(
                        icon = Icons.Default.Home,
                        label = "Alamat Lengkap",
                        value = account.address
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Self Photo & KTP Photo side-by-side preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onInspectPhotos() }
                ) {
                    Text(
                        text = "FOTO DIRI:",
                        style = MaterialTheme.typography.labelSmall,
                        color = EspressoBrown,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AccountMediaPreviewBox(
                        label = "Foto Diri",
                        uriString = account.selfPhotoUri,
                        isKtp = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onInspectPhotos() }
                ) {
                    Text(
                        text = "FOTO KTP:",
                        style = MaterialTheme.typography.labelSmall,
                        color = EspressoBrown,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AccountMediaPreviewBox(
                        label = "Foto KTP",
                        uriString = account.ktpPhotoUri,
                        isKtp = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onInspectPhotos,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, AntiqueGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = null,
                    tint = EspressoBrown,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Perbesar & Periksa Detail Foto Diri + KTP",
                    style = MaterialTheme.typography.labelMedium,
                    color = EspressoBrown
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = adminNote,
                onValueChange = { adminNote = it },
                label = { Text("Catatan Verifikasi Admin Sekolah") },
                singleLine = true,
                colors = vintageTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_note_input_${account.id}")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onReject(adminNote) },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, VintageCrimsonReject),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = VintageCrimsonBg,
                        contentColor = VintageCrimsonReject
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_reject_account_btn_${account.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tolak Formulir",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = { onApprove(adminNote) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VintageGreenSuccess,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_approve_account_btn_${account.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Setujui Akun",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonnelDirectoryCard(
    account: UserAccountEntity,
    onInspectPhotos: () -> Unit,
    onQuickApproveIfNotApproved: () -> Unit
) {
    val isApproved = account.statusEnum == AccountRegistrationStatus.APPROVED

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VintageCardCream),
        border = BorderStroke(1.dp, if (isApproved) ForestEmerald else VintageWarmBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (account.isSecurityTask) EspressoBrown else SoftGoldHighlight)
                            .border(1.dp, AntiqueGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (account.isSecurityTask) Icons.Default.Shield else Icons.Default.Person,
                            contentDescription = null,
                            tint = if (account.isSecurityTask) MetallicGold else EspressoBrown,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = account.fullName.ifBlank { account.email },
                            style = MaterialTheme.typography.titleMedium,
                            color = EspressoBrown,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tugas: ${account.taskRole.ifBlank { "Belum mengisi formulir" }} • HP: ${account.phoneNumber.ifBlank { "-" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftMochaText
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isApproved) VintageGreenBg else VintageParchmentSurface,
                    border = BorderStroke(1.dp, if (isApproved) VintageGreenSuccess else AntiqueGold)
                ) {
                    Text(
                        text = when (account.statusEnum) {
                            AccountRegistrationStatus.APPROVED -> "AKTIF"
                            AccountRegistrationStatus.PENDING_ADMIN_APPROVAL -> "MENUNGGU"
                            AccountRegistrationStatus.REJECTED -> "DITOLAK"
                            else -> "PROSES DAFTAR"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isApproved) VintageGreenSuccess else EspressoBrown,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Email: ${account.email} • Alamat: ${account.address.ifBlank { "-" }}",
                style = MaterialTheme.typography.bodySmall,
                color = DeepInkBrown
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onInspectPhotos,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AntiqueGold),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Lihat Foto & KTP", style = MaterialTheme.typography.labelSmall, color = EspressoBrown)
                }

                if (!isApproved) {
                    Button(
                        onClick = onQuickApproveIfNotApproved,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VintageGreenSuccess,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Setujui Sekarang", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminInfoLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = EspressoBrown,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SoftMochaText,
                fontSize = 10.sp
            )
            Text(
                text = value.ifBlank { "-" },
                style = MaterialTheme.typography.bodySmall,
                color = DeepInkBrown,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AdminStatChip(
    count: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = RichLeatherBrown,
        border = BorderStroke(1.dp, MetallicGold.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge,
                color = accent,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = VintageCreamBg,
                fontSize = 10.sp
            )
        }
    }
}
