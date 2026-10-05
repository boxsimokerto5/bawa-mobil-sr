package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AccountRegistrationStatus
import com.example.data.SchoolTaskType
import com.example.data.UserAccountEntity
import com.example.ui.components.VintageOrnamentalDivider
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.CopperRustRed
import com.example.ui.theme.DeepBronzeGold
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
import com.example.ui.theme.VintageParchmentSurface
import com.example.ui.theme.VintageWarmBorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun RegisterAccountScreen(
    authStatusMessage: String?,
    isAuthLoading: Boolean,
    onRegisterSubmit: (android.content.Context, String, String, String) -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onBackToLogin() }

    var selectedCategory by remember { mutableStateOf("PENGGUNA") } // PENGGUNA or KEAMANAN
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(VintageCreamBg, VintageParchmentSurface, VintageCreamBg)
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToLogin,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(VintageParchmentSurface)
                        .border(1.dp, AntiqueGold, CircleShape)
                        .testTag("register_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali ke Login",
                        tint = EspressoBrown
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Pendaftaran Akun Baru",
                        style = MaterialTheme.typography.titleLarge,
                        color = EspressoBrown
                    )
                    Text(
                        text = "Tahap 1 dari 3 • Buat Akun & Verifikasi Email",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftMochaText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step Progress Banner
            RegistrationStepBanner(currentStep = 1)

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = VintageCardCream),
                border = BorderStroke(2.dp, AntiqueGold),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = EspressoBrown,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pilih Kategori Pendaftaran",
                            style = MaterialTheme.typography.titleMedium,
                            color = EspressoBrown
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryChoiceCard(
                            title = "Pengguna & Pemantau",
                            subtitle = "Guru, Waliasuh, Wali Asrama, Dapur, Kebersihan, TU Tendik",
                            icon = Icons.Default.Person,
                            isSelected = selectedCategory == "PENGGUNA",
                            onClick = { selectedCategory = "PENGGUNA" },
                            testTag = "register_category_user",
                            modifier = Modifier.weight(1f)
                        )
                        CategoryChoiceCard(
                            title = "Keamanan Sekolah",
                            subtitle = "Petugas Pos Keamanan & Penjaga Gerbang Armada",
                            icon = Icons.Default.Shield,
                            isSelected = selectedCategory == "KEAMANAN",
                            onClick = { selectedCategory = "KEAMANAN" },
                            testTag = "register_category_security",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    VintageOrnamentalDivider(label = "DATA KREDENSIAL AKUN")

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            localError = null
                        },
                        label = { Text("Alamat Email Aktif") },
                        placeholder = { Text("contoh: nama.anda@sekolahsr.sch.id") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email",
                                tint = EspressoBrown
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = vintageTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_email_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            localError = null
                        },
                        label = { Text("Buat Kata Sandi") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password",
                                tint = EspressoBrown
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = vintageTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_password_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            localError = null
                        },
                        label = { Text("Ulangi Kata Sandi") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Konfirmasi Password",
                                tint = EspressoBrown
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = vintageTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_confirm_password_input")
                    )

                    val errorToDisplay = localError ?: authStatusMessage
                    if (!errorToDisplay.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CopperRustRed.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, CopperRustRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorToDisplay,
                                style = MaterialTheme.typography.bodySmall,
                                color = CopperRustRed,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            when {
                                email.isBlank() || !email.contains("@") -> {
                                    localError = "Mohon isi alamat email yang valid."
                                }
                                password.length < 4 -> {
                                    localError = "Kata sandi minimal 4 karakter."
                                }
                                password != confirmPassword -> {
                                    localError = "Konfirmasi kata sandi tidak cocok."
                                }
                                else -> {
                                    localError = null
                                    onRegisterSubmit(context, email, password, selectedCategory)
                                }
                            }
                        },
                        enabled = !isAuthLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EspressoBrown,
                            contentColor = SoftGoldHighlight
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_submit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailUnread,
                            contentDescription = null,
                            tint = MetallicGold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Daftar & Kirim Verifikasi Email",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmailVerificationScreen(
    account: UserAccountEntity?,
    fallbackEmail: String,
    authStatusMessage: String?,
    onVerifyCode: (android.content.Context, Int, String, Boolean) -> Unit,
    onResendCode: (android.content.Context, Int) -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onBackToLogin() }

    val targetEmail = account?.email ?: fallbackEmail
    val expectedCode = account?.verificationCode ?: "482910"
    val accountId = account?.id ?: 0

    var enteredCode by remember(expectedCode) { mutableStateOf(expectedCode) }
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(VintageCreamBg, VintageParchmentSurface, VintageCreamBg)
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToLogin,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(VintageParchmentSurface)
                        .border(1.dp, AntiqueGold, CircleShape)
                        .testTag("verify_email_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali ke Login",
                        tint = EspressoBrown
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Verifikasi Email Pendaftar",
                        style = MaterialTheme.typography.titleLarge,
                        color = EspressoBrown
                    )
                    Text(
                        text = "Tahap 2 dari 3 • Konfirmasi Alamat Email",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftMochaText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            RegistrationStepBanner(currentStep = 2)

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = VintageCardCream),
                border = BorderStroke(2.dp, AntiqueGold),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(EspressoBrown)
                            .border(2.dp, MetallicGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MarkEmailRead,
                            contentDescription = "Verifikasi Email",
                            tint = MetallicGold,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Cek & Verifikasi Email Anda",
                        style = MaterialTheme.typography.headlineSmall,
                        color = EspressoBrown,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Tautan verifikasi & kode OTP telah dikirimkan ke alamat email:",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftMochaText,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SoftGoldHighlight,
                        border = BorderStroke(1.dp, AntiqueGold)
                    ) {
                        Text(
                            text = targetEmail,
                            style = MaterialTheme.typography.labelLarge,
                            color = DeepInkBrown,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated Inbox Preview Card for seamless role-play verification
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = VintageParchmentSurface),
                        border = BorderStroke(1.5.dp, DeepBronzeGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "SIMULASI KOTAK MASUK EMAIL SEKOLAH SR",
                                style = MaterialTheme.typography.labelSmall,
                                color = EspressoBrown,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Kode Verifikasi OTP Anda:",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftMochaText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = expectedCode,
                                style = MaterialTheme.typography.headlineMedium,
                                fontFamily = FontFamily.Monospace,
                                color = EspressoBrown,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 4.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    onVerifyCode(context, accountId, expectedCode, true)
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, ForestEmerald),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = ForestEmerald.copy(alpha = 0.1f),
                                    contentColor = ForestEmerald
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("simulate_email_link_click_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Klik Link Verifikasi Email Sekarang",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = enteredCode,
                        onValueChange = { enteredCode = it },
                        label = { Text("Masukkan 6 Digit Kode Verifikasi Email") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Kode OTP",
                                tint = EspressoBrown
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = vintageTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verify_email_code_input")
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
                        onClick = {
                            onVerifyCode(context, accountId, enteredCode, false)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EspressoBrown,
                            contentColor = SoftGoldHighlight
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("verify_email_submit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MetallicGold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Verifikasi Email & Lanjut ke Login",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { onResendCode(context, accountId) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AntiqueGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("resend_verification_email_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = EspressoBrown,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kirim Ulang Kode Verifikasi",
                            style = MaterialTheme.typography.labelMedium,
                            color = EspressoBrown
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompleteProfileFormScreen(
    account: UserAccountEntity?,
    onSubmitProfileForm: (
        accountId: Int,
        fullName: String,
        address: String,
        phoneNumber: String,
        taskRole: String,
        selfPhotoUri: String,
        ktpPhotoUri: String
    ) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onLogout() }

    val accountId = account?.id ?: 0
    val defaultTask = account?.taskRole?.takeIf { it.isNotBlank() }
        ?: if (account?.initialAccountCategory == "KEAMANAN") {
            SchoolTaskType.KEAMANAN.label
        } else {
            SchoolTaskType.GURU.label
        }

    var fullName by remember(account?.id) { mutableStateOf(account?.fullName ?: "") }
    var address by remember(account?.id) { mutableStateOf(account?.address ?: "") }
    var phoneNumber by remember(account?.id) { mutableStateOf(account?.phoneNumber ?: "") }
    var selectedTask by remember(account?.id) { mutableStateOf(defaultTask) }
    var taskDropdownExpanded by remember { mutableStateOf(false) }

    var selfPhotoUri by remember(account?.id) { mutableStateOf(account?.selfPhotoUri ?: "") }
    var ktpPhotoUri by remember(account?.id) { mutableStateOf(account?.ktpPhotoUri ?: "") }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Zero-permission Android Photo Picker for Self Photo & KTP Photo
    val selfPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selfPhotoUri = uri.toString()
            validationError = null
        }
    }

    val ktpPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            ktpPhotoUri = uri.toString()
            validationError = null
        }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(VintageCreamBg, VintageParchmentSurface, VintageCreamBg)
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = ForestEmerald.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, ForestEmerald)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = ForestEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "EMAIL TERVERIFIKASI: ${account?.email ?: "-"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ForestEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Formulir Lengkap Personel",
                        style = MaterialTheme.typography.headlineSmall,
                        color = EspressoBrown
                    )
                    Text(
                        text = "Lengkapi foto diri, biodata, KTP & tugas untuk dikirim ke Admin Sekolah",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftMochaText
                    )
                }

                OutlinedButton(
                    onClick = onLogout,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AntiqueGold),
                    modifier = Modifier.testTag("profile_form_logout_button")
                ) {
                    Text("Keluar", style = MaterialTheme.typography.labelMedium, color = EspressoBrown)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            RegistrationStepBanner(currentStep = 3)

            if (account?.statusEnum == AccountRegistrationStatus.REJECTED &&
                account.adminNotes.isNotBlank()
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CopperRustRed.copy(alpha = 0.12f)),
                    border = BorderStroke(1.5.dp, CopperRustRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "CATATAN PERBAIKAN DARI ADMIN SEKOLAH:",
                            style = MaterialTheme.typography.labelSmall,
                            color = CopperRustRed,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = account.adminNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = DeepInkBrown
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = VintageCardCream),
                border = BorderStroke(2.dp, AntiqueGold),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // 1. Upload Foto Diri
                    Text(
                        text = "1. UPLOAD FOTO DIRI (PROFIL RESMI)",
                        style = MaterialTheme.typography.labelMedium,
                        color = EspressoBrown,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    PhotoUploadCard(
                        title = if (selfPhotoUri.isNotBlank()) "Foto Diri Terlampir" else "Belum Ada Foto Diri",
                        subtitle = "Pilih dari Galeri HP atau gunakan Foto Pas Resmi",
                        photoUri = selfPhotoUri,
                        isKtpCard = false,
                        onPickFromGallery = {
                            selfPhotoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onUsePresetSample = {
                            selfPhotoUri = "preset://foto_diri_${selectedTask.lowercase().replace(" ", "_")}"
                            validationError = null
                        },
                        galleryButtonTag = "upload_self_photo_button",
                        presetButtonTag = "preset_self_photo_button"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    VintageOrnamentalDivider(label = "BIODATA LENGKAP PERSONEL")

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Nama Lengkap
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            validationError = null
                        },
                        label = { Text("2. Nama Lengkap (Sesuai KTP)") },
                        placeholder = { Text("Contoh: Bapak Ahmad Sulaiman, S.Pd.") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Nama Lengkap",
                                tint = EspressoBrown
                            )
                        },
                        singleLine = true,
                        colors = vintageTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_full_name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Alamat Lengkap
                    OutlinedTextField(
                        value = address,
                        onValueChange = {
                            address = it
                            validationError = null
                        },
                        label = { Text("3. Alamat Domisili Lengkap") },
                        placeholder = { Text("Contoh: Jl. Ketintang Baru No. 15, Surabaya") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Alamat",
                                tint = EspressoBrown
                            )
                        },
                        minLines = 2,
                        maxLines = 3,
                        colors = vintageTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_address_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Nomor HP
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            validationError = null
                        },
                        label = { Text("4. Nomor HP / WhatsApp Aktif") },
                        placeholder = { Text("Contoh: 081234567890") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Nomor HP",
                                tint = EspressoBrown
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        colors = vintageTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_phone_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 5. Upload Foto KTP
                    Text(
                        text = "5. UPLOAD FOTO KTP (KARTU TANDA PENDUDUK)",
                        style = MaterialTheme.typography.labelMedium,
                        color = EspressoBrown,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    PhotoUploadCard(
                        title = if (ktpPhotoUri.isNotBlank()) "Foto KTP Terlampir" else "Belum Ada Foto KTP",
                        subtitle = "Unggah foto KTP asli yang jelas untuk verifikasi Admin Sekolah",
                        photoUri = ktpPhotoUri,
                        isKtpCard = true,
                        onPickFromGallery = {
                            ktpPhotoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onUsePresetSample = {
                            ktpPhotoUri = "preset://ktp_nik_3578${(100000..999999).random()}"
                            validationError = null
                        },
                        galleryButtonTag = "upload_ktp_photo_button",
                        presetButtonTag = "preset_ktp_photo_button"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 6. Tugas (Dropdown Pilihan)
                    Text(
                        text = "6. PILIH TUGAS / JABATAN DI SEKOLAH",
                        style = MaterialTheme.typography.labelMedium,
                        color = EspressoBrown,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = taskDropdownExpanded,
                        onExpandedChange = { taskDropdownExpanded = !taskDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedTask,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tugas / Unit Kerja") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Work,
                                    contentDescription = "Tugas",
                                    tint = EspressoBrown
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = taskDropdownExpanded)
                            },
                            colors = vintageTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                                .testTag("profile_task_dropdown")
                        )

                        ExposedDropdownMenu(
                            expanded = taskDropdownExpanded,
                            onDismissRequest = { taskDropdownExpanded = false },
                            modifier = Modifier.background(VintageCardCream)
                        ) {
                            SchoolTaskType.entries.forEach { taskOption ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = taskOption.label,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = DeepInkBrown,
                                                fontWeight = if (selectedTask == taskOption.label) {
                                                    FontWeight.Bold
                                                } else {
                                                    FontWeight.Normal
                                                }
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = if (taskOption.mapsToSecurityRole) {
                                                    EspressoBrown.copy(alpha = 0.12f)
                                                } else {
                                                    SoftGoldHighlight
                                                }
                                            ) {
                                                Text(
                                                    text = if (taskOption.mapsToSecurityRole) {
                                                        "Akses Pos Keamanan"
                                                    } else {
                                                        "Akses Pengguna & Pemantau"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = EspressoBrown,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedTask = taskOption.label
                                        taskDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Task Chips so user can also tap directly without extra clicks
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SchoolTaskType.entries.take(4).forEach { item ->
                            TaskQuickChip(
                                label = item.label,
                                selected = selectedTask == item.label,
                                onClick = { selectedTask = item.label },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SchoolTaskType.entries.drop(4).forEach { item ->
                            TaskQuickChip(
                                label = item.label,
                                selected = selectedTask == item.label,
                                onClick = { selectedTask = item.label },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (!validationError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CopperRustRed.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, CopperRustRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = validationError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = CopperRustRed,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            when {
                                selfPhotoUri.isBlank() -> {
                                    validationError = "Mohon unggah atau lampirkan Foto Diri terlebih dahulu."
                                }
                                fullName.trim().length < 3 -> {
                                    validationError = "Mohon isi Nama Lengkap sesuai KTP."
                                }
                                address.trim().length < 5 -> {
                                    validationError = "Mohon isi Alamat Domisili lengkap."
                                }
                                phoneNumber.trim().length < 8 -> {
                                    validationError = "Mohon isi Nomor HP / WhatsApp aktif."
                                }
                                ktpPhotoUri.isBlank() -> {
                                    validationError = "Mohon unggah atau lampirkan Foto KTP terlebih dahulu."
                                }
                                selectedTask.isBlank() -> {
                                    validationError = "Mohon pilih Tugas / Jabatan."
                                }
                                else -> {
                                    validationError = null
                                    onSubmitProfileForm(
                                        accountId,
                                        fullName,
                                        address,
                                        phoneNumber,
                                        selectedTask,
                                        selfPhotoUri,
                                        ktpPhotoUri
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EspressoBrown,
                            contentColor = SoftGoldHighlight
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 15.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_complete_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MetallicGold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Kirim Formulir ke Admin Sekolah",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WaitingAdminApprovalScreen(
    account: UserAccountEntity?,
    onEditFormAgain: () -> Unit,
    onJumpToSchoolAdminLogin: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onLogout() }
    val scrollState = rememberScrollState()
    val isRejected = account?.statusEnum == AccountRegistrationStatus.REJECTED

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(VintageCreamBg, VintageParchmentSurface, VintageCreamBg)
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 580.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = VintageCardCream),
                border = BorderStroke(2.dp, if (isRejected) CopperRustRed else AntiqueGold),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(if (isRejected) CopperRustRed else EspressoBrown)
                            .border(3.dp, MetallicGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRejected) Icons.Default.Warning else Icons.Default.HourglassTop,
                            contentDescription = "Status Persetujuan",
                            tint = MetallicGold,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isRejected) {
                            CopperRustRed.copy(alpha = 0.14f)
                        } else {
                            VintageAmberWarning.copy(alpha = 0.18f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isRejected) CopperRustRed else VintageAmberWarning
                        )
                    ) {
                        Text(
                            text = if (isRejected) {
                                "STATUS: PERLU PERBAIKAN BERKAS"
                            } else {
                                "STATUS: MENUNGGU PERSETUJUAN ADMIN SEKOLAH"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isRejected) CopperRustRed else EspressoBrown,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isRejected) {
                            "Formulir Memerlukan Revisi"
                        } else {
                            "Formulir Lengkap Telah Terkirim"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = EspressoBrown,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isRejected) {
                            "Admin Sekolah telah memeriksa berkas Anda dan memberikan catatan evaluasi di bawah ini."
                        } else {
                            "Biodata, Foto Diri, Foto KTP, dan penugasan Anda telah masuk ke antrean Halaman Admin Sekolah (eccko1101) untuk diverifikasi."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftMochaText,
                        textAlign = TextAlign.Center
                    )

                    if (isRejected && !account?.adminNotes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CopperRustRed.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, CopperRustRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Catatan Admin Sekolah:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CopperRustRed,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = account?.adminNotes ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DeepInkBrown
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    VintageOrnamentalDivider(label = "RINGKASAN BERKAS TERKIRIM")

                    Spacer(modifier = Modifier.height(12.dp))

                    // Submitted Details Summary Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = VintageParchmentSurface),
                        border = BorderStroke(1.dp, VintageWarmBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SummaryRowItem(label = "Nama Lengkap", value = account?.fullName ?: "-")
                            SummaryRowItem(label = "Email Terverifikasi", value = account?.email ?: "-")
                            SummaryRowItem(label = "Tugas / Unit", value = account?.taskRole ?: "-")
                            SummaryRowItem(label = "Nomor HP", value = account?.phoneNumber ?: "-")
                            SummaryRowItem(label = "Alamat", value = account?.address ?: "-")
                            SummaryRowItem(
                                label = "Berkas Foto & KTP",
                                value = "Foto Diri & KTP Terlampir"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AccountMediaPreviewBox(
                            label = "Foto Diri",
                            uriString = account?.selfPhotoUri ?: "",
                            isKtp = false,
                            modifier = Modifier.weight(1f)
                        )
                        AccountMediaPreviewBox(
                            label = "Foto KTP",
                            uriString = account?.ktpPhotoUri ?: "",
                            isKtp = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedButton(
                        onClick = onEditFormAgain,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, AntiqueGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_submitted_profile_button")
                    ) {
                        Text(
                            text = "Perbarui / Edit Formulir Biodata",
                            style = MaterialTheme.typography.labelLarge,
                            color = EspressoBrown
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onJumpToSchoolAdminLogin,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EspressoBrown,
                            contentColor = SoftGoldHighlight
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(vertical = 13.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("switch_to_admin_login_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MetallicGold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ke Halaman Login (Buka Admin Sekolah)",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegistrationStepBanner(currentStep: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = VintageParchmentSurface),
        border = BorderStroke(1.dp, VintageWarmBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepPill(stepNumber = 1, title = "Daftar Akun", isActive = currentStep == 1, isDone = currentStep > 1)
            StepPill(stepNumber = 2, title = "Verifikasi Email", isActive = currentStep == 2, isDone = currentStep > 2)
            StepPill(stepNumber = 3, title = "Formulir & KTP", isActive = currentStep == 3, isDone = currentStep > 3)
        }
    }
}

@Composable
private fun StepPill(
    stepNumber: Int,
    title: String,
    isActive: Boolean,
    isDone: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isDone -> ForestEmerald
                        isActive -> EspressoBrown
                        else -> VintageWarmBorder
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isDone) "✓" else stepNumber.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive || isDone) DeepInkBrown else SoftMochaText,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun CategoryChoiceCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) EspressoBrown else VintageParchmentSurface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MetallicGold else VintageWarmBorder
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MetallicGold else EspressoBrown,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) SoftGoldHighlight else DeepInkBrown,
                fontWeight = FontWeight.Bold
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
private fun TaskQuickChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) EspressoBrown else VintageParchmentSurface,
        border = BorderStroke(1.dp, if (selected) MetallicGold else VintageWarmBorder),
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) SoftGoldHighlight else DeepInkBrown,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun PhotoUploadCard(
    title: String,
    subtitle: String,
    photoUri: String,
    isKtpCard: Boolean,
    onPickFromGallery: () -> Unit,
    onUsePresetSample: () -> Unit,
    galleryButtonTag: String,
    presetButtonTag: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = VintageParchmentSurface),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (photoUri.isNotBlank()) ForestEmerald else AntiqueGold
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AccountMediaPreviewBox(
                    label = if (isKtpCard) "KTP" else "Foto",
                    uriString = photoUri,
                    isKtp = isKtpCard,
                    modifier = Modifier.size(width = if (isKtpCard) 110.dp else 78.dp, height = 78.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (photoUri.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ForestEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (photoUri.isNotBlank()) ForestEmerald else DeepInkBrown,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftMochaText,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPickFromGallery,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EspressoBrown,
                        contentColor = SoftGoldHighlight
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp, horizontal = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag(galleryButtonTag)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = MetallicGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isKtpCard) "Pilih Foto KTP" else "Pilih Foto Diri",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                OutlinedButton(
                    onClick = onUsePresetSample,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AntiqueGold),
                    contentPadding = PaddingValues(vertical = 10.dp, horizontal = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag(presetButtonTag)
                ) {
                    Icon(
                        imageVector = if (isKtpCard) Icons.Default.CreditCard else Icons.Default.AccountBox,
                        contentDescription = null,
                        tint = EspressoBrown,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isKtpCard) "Gunakan KTP Simulasi" else "Gunakan Foto Simulasi",
                        style = MaterialTheme.typography.labelSmall,
                        color = EspressoBrown
                    )
                }
            }
        }
    }
}

@Composable
fun AccountMediaPreviewBox(
    label: String,
    uriString: String,
    isKtp: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val loadedBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = uriString) {
        value = if (uriString.isNotBlank() && !uriString.startsWith("preset://")) {
            withContext(Dispatchers.IO) {
                try {
                    val uri = Uri.parse(uriString)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                } catch (_: Exception) {
                    null
                }
            }
        } else {
            null
        }
    }

    Box(
        modifier = modifier
            .height(82.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (uriString.isNotBlank()) {
                    Brush.linearGradient(listOf(EspressoBrown, RichLeatherBrown))
                } else {
                    Brush.linearGradient(listOf(VintageCreamBg, VintageParchmentSurface))
                }
            )
            .border(
                width = 1.5.dp,
                color = if (uriString.isNotBlank()) MetallicGold else VintageWarmBorder,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (loadedBitmap != null) {
            Image(
                bitmap = loadedBitmap!!,
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else if (uriString.isNotBlank()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(6.dp)
            ) {
                Icon(
                    imageVector = if (isKtp) Icons.Default.Badge else Icons.Default.Person,
                    contentDescription = label,
                    tint = MetallicGold,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isKtp) "KTP TERLAMPIR" else "FOTO TERLAMPIR",
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftGoldHighlight,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(6.dp)
            ) {
                Icon(
                    imageVector = if (isKtp) Icons.Default.CreditCard else Icons.Default.AddAPhoto,
                    contentDescription = label,
                    tint = SoftMochaText,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftMochaText,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun SummaryRowItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = SoftMochaText,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = DeepInkBrown,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}
