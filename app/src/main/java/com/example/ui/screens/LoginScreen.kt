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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VintageOrnamentalDivider
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepBronzeGold
import com.example.ui.theme.DeepInkBrown
import com.example.ui.theme.EspressoBrown
import com.example.ui.theme.MetallicGold
import com.example.ui.theme.RichLeatherBrown
import com.example.ui.theme.SoftGoldHighlight
import com.example.ui.theme.SoftMochaText
import com.example.ui.theme.VintageCardCream
import com.example.ui.theme.VintageCreamBg
import com.example.ui.theme.VintageParchmentSurface
import com.example.ui.theme.VintageWarmBorder

@Composable
fun LoginScreen(
    defaultUserName: String,
    defaultUserDivision: String,
    defaultSecurityOfficer: String,
    activeTripsCount: Int,
    pendingTripsCount: Int,
    pendingAdminAccountsCount: Int = 0,
    prefilledEmail: String = "",
    isAuthLoading: Boolean = false,
    authStatusMessage: String? = null,
    onOpenRegisterAccount: () -> Unit = {},
    onLoginWithRegisteredEmail: (String, String) -> Unit = { _, _ -> },
    onLoginAsUserMonitor: (String, String) -> Unit,
    onGoogleSignInDriver: (android.content.Context, String) -> Unit = { _, _ -> },
    onFirebaseEmailSignInDriver: (android.content.Context, String, String, String) -> Unit = { _, _, _, _ -> },
    onLoginAsSecurity: (String) -> Unit,
    onLoginAsSchoolAdmin: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // 0 = Pengguna & Pemantau Mobil, 1 = Keamanan (Security), 2 = Admin Sekolah
    var selectedTab by remember { mutableIntStateOf(0) }

    var userName by remember { mutableStateOf(defaultUserName) }
    var userDivision by remember { mutableStateOf(defaultUserDivision) }
    var registeredEmail by remember(prefilledEmail) {
        mutableStateOf(prefilledEmail.ifBlank { "ustadz.fauzi@sekolahsr.sch.id" })
    }
    var registeredPassword by remember { mutableStateOf("Password123") }
    var showQuickDemoProfile by remember { mutableStateOf(false) }
    var showFirebaseEmailPanel by remember { mutableStateOf(false) }
    var driverEmail by remember { mutableStateOf("") }
    var driverPassword by remember { mutableStateOf("") }
    var securityOfficer by remember { mutableStateOf(defaultSecurityOfficer) }
    var securityGateCode by remember { mutableStateOf("SR-POS-01") }
    var adminUsername by remember { mutableStateOf("eccko1101") }
    var adminPassword by remember { mutableStateOf("Woyowoyo12@") }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        VintageCreamBg,
                        VintageParchmentSurface,
                        VintageCreamBg
                    )
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Vintage Emblem Crest Header
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(RichLeatherBrown, EspressoBrown)
                        )
                    )
                    .border(3.dp, MetallicGold, CircleShape)
                    .padding(6.dp)
                    .border(1.dp, SoftGoldHighlight.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = "Emblem Bawa Mobil SR",
                    tint = MetallicGold,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = SoftGoldHighlight,
                border = BorderStroke(1.dp, AntiqueGold)
            ) {
                Text(
                    text = "ARMADA OPERASIONAL TERPADU",
                    style = MaterialTheme.typography.labelSmall,
                    color = EspressoBrown,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Bawa Mobil SR",
                style = MaterialTheme.typography.displayMedium,
                color = EspressoBrown,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Sistem Rencana Perjalanan, Persetujuan Pos Keamanan & Pemantauan Peta Real-Time",
                style = MaterialTheme.typography.bodyMedium,
                color = SoftMochaText,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Fleet Units Showcase Banner (Gran Max, N-Max, Avanza Veloz)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = VintageParchmentSurface),
                border = BorderStroke(1.dp, VintageWarmBorder)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "UNIT ARMADA TERDAFTAR",
                        style = MaterialTheme.typography.labelSmall,
                        color = EspressoBrown,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        FleetMiniBadge(
                            icon = Icons.Default.LocalShipping,
                            name = "Gran Max",
                            plate = "L 8841 SR"
                        )
                        FleetMiniBadge(
                            icon = Icons.Default.TwoWheeler,
                            name = "N-Max",
                            plate = "L 4029 SR"
                        )
                        FleetMiniBadge(
                            icon = Icons.Default.DirectionsCar,
                            name = "Avanza Veloz",
                            plate = "L 1925 SR"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // prominent Register New Account Banner (Pengguna & Keamanan)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = EspressoBrown),
                border = BorderStroke(1.5.dp, MetallicGold),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "BELUM PUNYA AKUN PERSONEL?",
                            style = MaterialTheme.typography.labelSmall,
                            color = MetallicGold,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Daftar Akun Pengguna / Keamanan • Verifikasi Email & Upload KTP",
                            style = MaterialTheme.typography.bodySmall,
                            color = VintageCreamBg
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = onOpenRegisterAccount,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MetallicGold,
                            contentColor = DeepInkBrown
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("open_register_account_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Daftar Akun",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            VintageOrnamentalDivider(label = "PILIH AKSES MASUK")

            Spacer(modifier = Modifier.height(12.dp))

            // Role Selector Cards (Pengguna & Pemantau, Keamanan, Admin Sekolah)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RoleSelectionCard(
                    title = "Pengguna",
                    subtitle = "Login Akun & Bawa Mobil",
                    icon = Icons.Default.Person,
                    badgeText = "$activeTripsCount Jalan",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    testTag = "role_card_user_monitor",
                    modifier = Modifier.weight(1f)
                )

                RoleSelectionCard(
                    title = "Keamanan",
                    subtitle = "Pos Izin & Peta Armada",
                    icon = Icons.Default.Shield,
                    badgeText = "$pendingTripsCount Izin",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    testTag = "role_card_security",
                    modifier = Modifier.weight(1f)
                )

                RoleSelectionCard(
                    title = "Admin Sekolah",
                    subtitle = "Verifikasi KTP & Tugas",
                    icon = Icons.Default.AdminPanelSettings,
                    badgeText = "$pendingAdminAccountsCount Akun",
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    testTag = "role_card_school_admin",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Login Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = VintageCardCream),
                border = BorderStroke(2.dp, AntiqueGold),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    if (selectedTab == 0) {
                        // Login Pengguna & Akun Terdaftar (Setelah Verifikasi Email)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = EspressoBrown,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Login Akun Pengguna / Personel",
                                style = MaterialTheme.typography.titleLarge,
                                color = EspressoBrown
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Masuk dengan akun yang telah didaftarkan & diverifikasi email untuk mengisi Formulir Biodata/KTP atau memakai mobil.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftMochaText
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = registeredEmail,
                            onValueChange = { registeredEmail = it },
                            label = { Text("Email Akun Terdaftar") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email Akun",
                                    tint = EspressoBrown
                                )
                            },
                            singleLine = true,
                            colors = vintageTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_registered_email_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = registeredPassword,
                            onValueChange = { registeredPassword = it },
                            label = { Text("Kata Sandi Akun") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Kata Sandi",
                                    tint = EspressoBrown
                                )
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            colors = vintageTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_registered_password_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                onLoginWithRegisteredEmail(registeredEmail, registeredPassword)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EspressoBrown,
                                contentColor = SoftGoldHighlight
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_registered_submit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = MetallicGold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Login Akun Terverifikasi",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Preset Accounts for Testing Role-Play Stages
                        Text(
                            text = "CONTOH AKUN TERDAFTAR (KLIK UNTUK UJI ROLE-PLAY):",
                            style = MaterialTheme.typography.labelSmall,
                            color = EspressoBrown,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PresetProfileChip(
                                label = "Fauzi (Menunggu Admin)",
                                isSelected = registeredEmail.contains("fauzi"),
                                onClick = {
                                    registeredEmail = "ustadz.fauzi@sekolahsr.sch.id"
                                    registeredPassword = "Password123"
                                },
                                modifier = Modifier.weight(1f)
                            )
                            PresetProfileChip(
                                label = "Andi (Guru Aktif)",
                                isSelected = registeredEmail.contains("andi.guru"),
                                onClick = {
                                    registeredEmail = "andi.guru@sekolahsr.sch.id"
                                    registeredPassword = "Password123"
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            onClick = { showQuickDemoProfile = !showQuickDemoProfile },
                            shape = RoundedCornerShape(10.dp),
                            color = VintageParchmentSurface,
                            border = BorderStroke(1.dp, VintageWarmBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (showQuickDemoProfile) {
                                    "Sembunyikan Login Cepat Nama Langsung"
                                } else {
                                    "Atau Masuk Cepat Tanpa Password (Mode Langsung)"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = EspressoBrown,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp)
                            )
                        }

                        if (showQuickDemoProfile) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PresetProfileChip(
                                    label = "Andi (Guru)",
                                    isSelected = userName.contains("Andi"),
                                    onClick = {
                                        userName = "Bapak Andi Pratama"
                                        userDivision = "Guru"
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                PresetProfileChip(
                                    label = "Budi (Waliasuh)",
                                    isSelected = userName.contains("Budi"),
                                    onClick = {
                                        userName = "Mas Budi Santoso"
                                        userDivision = "Waliasuh"
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                PresetProfileChip(
                                    label = "Siti (TU Tendik)",
                                    isSelected = userName.contains("Siti"),
                                    onClick = {
                                        userName = "Ibu Siti Rahma"
                                        userDivision = "TU Tendik"
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = userName,
                                onValueChange = { userName = it },
                                label = { Text("Nama Lengkap Pengguna / Pemantau") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Nama",
                                        tint = EspressoBrown
                                    )
                                },
                                singleLine = true,
                                colors = vintageTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_user_name_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = userDivision,
                                onValueChange = { userDivision = it },
                                label = { Text("Tugas / Bagian Keperluan") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = "Tugas",
                                        tint = EspressoBrown
                                    )
                                },
                                singleLine = true,
                                colors = vintageTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_user_division_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { onLoginAsUserMonitor(userName, userDivision) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RichLeatherBrown,
                                    contentColor = SoftGoldHighlight
                                ),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(vertical = 12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_user_submit_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = MetallicGold
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Masuk Cepat sebagai Pengguna & Pemantau",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        VintageOrnamentalDivider(label = "ATAU AKSES AMAN PENGEMUDI")

                        Spacer(modifier = Modifier.height(12.dp))

                        // Google Sign-In via Credential Manager & Firebase Auth Button
                        OutlinedButton(
                            onClick = { onGoogleSignInDriver(context, userDivision) },
                            enabled = !isAuthLoading,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, AntiqueGold),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = VintageParchmentSurface,
                                contentColor = EspressoBrown
                            ),
                            contentPadding = PaddingValues(vertical = 12.dp, horizontal = 14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_google_credential_button")
                        ) {
                            if (isAuthLoading) {
                                CircularProgressIndicator(
                                    color = EspressoBrown,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Memverifikasi Kredensial Pengemudi...",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = EspressoBrown
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Google Sign-In",
                                    tint = DeepBronzeGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Login Aman dengan Google (Firebase Auth)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = EspressoBrown,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            onClick = { showFirebaseEmailPanel = !showFirebaseEmailPanel },
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("toggle_firebase_email_auth_button")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = SoftMochaText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (showFirebaseEmailPanel) {
                                        "Sembunyikan Login Email Firebase Pengemudi"
                                    } else {
                                        "Gunakan Akun Email Firebase Pengemudi"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EspressoBrown,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (showFirebaseEmailPanel) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = VintageParchmentSurface),
                                border = BorderStroke(1.dp, VintageWarmBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp)
                                ) {
                                    Text(
                                        text = "Autentikasi Pengemudi via Firebase Email",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = EspressoBrown,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = driverEmail,
                                        onValueChange = { driverEmail = it },
                                        label = { Text("Email Pengemudi") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Email,
                                                contentDescription = "Email",
                                                tint = EspressoBrown
                                            )
                                        },
                                        singleLine = true,
                                        colors = vintageTextFieldColors(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("firebase_driver_email_input")
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = driverPassword,
                                        onValueChange = { driverPassword = it },
                                        label = { Text("Kata Sandi") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Kata Sandi",
                                                tint = EspressoBrown
                                            )
                                        },
                                        visualTransformation = PasswordVisualTransformation(),
                                        singleLine = true,
                                        colors = vintageTextFieldColors(),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("firebase_driver_password_input")
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            onFirebaseEmailSignInDriver(
                                                context,
                                                driverEmail,
                                                driverPassword,
                                                userDivision
                                            )
                                        },
                                        enabled = !isAuthLoading,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = RichLeatherBrown,
                                            contentColor = SoftGoldHighlight
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("firebase_driver_email_submit_button")
                                    ) {
                                        Text(
                                            text = "Verifikasi Akun Firebase",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }

                        if (!authStatusMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SoftGoldHighlight.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, AntiqueGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = authStatusMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DeepInkBrown,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    } else if (selectedTab == 1) {
                        // Login Keamanan (Security)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = EspressoBrown,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Login Pos Keamanan (Security)",
                                style = MaterialTheme.typography.titleLarge,
                                color = EspressoBrown
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Halaman khusus petugas keamanan untuk menyetujui atau menolak rencana bawa mobil serta memantau lokasi unit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftMochaText
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "PILIH PETUGAS PIKET POS:",
                            style = MaterialTheme.typography.labelSmall,
                            color = EspressoBrown,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PresetProfileChip(
                                label = "Komandan Suryo",
                                isSelected = securityOfficer.contains("Suryo"),
                                onClick = {
                                    securityOfficer = "Komandan Pos Suryo"
                                    securityGateCode = "SR-POS-01"
                                },
                                modifier = Modifier.weight(1f)
                            )
                            PresetProfileChip(
                                label = "Petugas Danang",
                                isSelected = securityOfficer.contains("Danang"),
                                onClick = {
                                    securityOfficer = "Petugas Keamanan Danang"
                                    securityGateCode = "SR-GERBANG-UTAMA"
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = securityOfficer,
                            onValueChange = { securityOfficer = it },
                            label = { Text("Nama Petugas Keamanan Piket") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Nama Petugas",
                                    tint = EspressoBrown
                                )
                            },
                            singleLine = true,
                            colors = vintageTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_security_officer_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = securityGateCode,
                            onValueChange = { securityGateCode = it },
                            label = { Text("Kode Pos / Regu Jaga") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "Kode Pos",
                                    tint = EspressoBrown
                                )
                            },
                            singleLine = true,
                            colors = vintageTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_security_code_input")
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { onLoginAsSecurity(securityOfficer) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RichLeatherBrown,
                                contentColor = SoftGoldHighlight
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_security_submit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MetallicGold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Masuk ke Halaman Keamanan",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else {
                        // Login Admin Sekolah (eccko1101 / Woyowoyo12@)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = EspressoBrown,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Login Halaman Admin Sekolah",
                                style = MaterialTheme.typography.titleLarge,
                                color = EspressoBrown
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Otoritas khusus Admin Sekolah untuk memeriksa Formulir Biodata, Foto Diri, KTP, dan Tugas (Keamanan, Dapur, Kebersihan, Waliasuh, Wali Asrama, Guru, TU Tendik).",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftMochaText
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            onClick = {
                                adminUsername = "eccko1101"
                                adminPassword = "Woyowoyo12@"
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = SoftGoldHighlight,
                            border = BorderStroke(1.dp, AntiqueGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "KREDENSIAL RESMI ADMIN SEKOLAH",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EspressoBrown,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "User: eccko1101 • Pass: Woyowoyo12@",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DeepInkBrown,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "Isi Otomatis",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EspressoBrown,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = adminUsername,
                            onValueChange = { adminUsername = it },
                            label = { Text("Username Admin Sekolah") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Username Admin",
                                    tint = EspressoBrown
                                )
                            },
                            singleLine = true,
                            colors = vintageTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_admin_username_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = { adminPassword = it },
                            label = { Text("Password Admin Sekolah") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Key,
                                    contentDescription = "Password Admin",
                                    tint = EspressoBrown
                                )
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            colors = vintageTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_admin_password_input")
                        )

                        if (!authStatusMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SoftGoldHighlight.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, AntiqueGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = authStatusMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DeepInkBrown,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onLoginAsSchoolAdmin(adminUsername, adminPassword) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EspressoBrown,
                                contentColor = SoftGoldHighlight
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_admin_submit_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MetallicGold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Masuk ke Halaman Admin Sekolah",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FleetMiniBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    name: String,
    plate: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(EspressoBrown)
                .border(1.dp, MetallicGold, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = MetallicGold,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = DeepInkBrown,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = plate,
            style = MaterialTheme.typography.labelSmall,
            color = SoftMochaText,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun RoleSelectionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeText: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) EspressoBrown else VintageCardCream
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MetallicGold else VintageWarmBorder
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) MetallicGold else VintageParchmentSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isSelected) DeepInkBrown else EspressoBrown,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) SoftGoldHighlight.copy(alpha = 0.2f) else VintageParchmentSurface,
                    border = BorderStroke(1.dp, if (isSelected) MetallicGold else VintageWarmBorder)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) SoftGoldHighlight else EspressoBrown,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) SoftGoldHighlight else DeepInkBrown
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) VintageCreamBg.copy(alpha = 0.85f) else SoftMochaText,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun PresetProfileChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) SoftGoldHighlight else VintageParchmentSurface,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) AntiqueGold else VintageWarmBorder
        ),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = DeepInkBrown,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun vintageTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = EspressoBrown,
    unfocusedBorderColor = AntiqueGold,
    focusedLabelColor = EspressoBrown,
    unfocusedLabelColor = SoftMochaText,
    cursorColor = EspressoBrown,
    focusedContainerColor = VintageCreamBg,
    unfocusedContainerColor = VintageCreamBg
)
