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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
    onLoginAsUserMonitor: (String, String) -> Unit,
    onLoginAsSecurity: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 0 = Pengguna & Pemantau Mobil, 1 = Keamanan (Security)
    var selectedTab by remember { mutableIntStateOf(0) }

    var userName by remember { mutableStateOf(defaultUserName) }
    var userDivision by remember { mutableStateOf(defaultUserDivision) }
    var securityOfficer by remember { mutableStateOf(defaultSecurityOfficer) }
    var securityGateCode by remember { mutableStateOf("SR-POS-01") }

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

            Spacer(modifier = Modifier.height(18.dp))

            VintageOrnamentalDivider(label = "PILIH AKSES MASUK")

            Spacer(modifier = Modifier.height(16.dp))

            // Role Selector Cards (Pengguna & Pemantau vs Keamanan)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RoleSelectionCard(
                    title = "Pengguna & Pemantau",
                    subtitle = "Isi rencana bawa mobil & pantau peta live",
                    icon = Icons.Default.Person,
                    badgeText = "$activeTripsCount Jalan",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    testTag = "role_card_user_monitor",
                    modifier = Modifier.weight(1f)
                )

                RoleSelectionCard(
                    title = "Keamanan (Security)",
                    subtitle = "Setujui / tolak izin & pantau armada",
                    icon = Icons.Default.Shield,
                    badgeText = "$pendingTripsCount Menunggu",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    testTag = "role_card_security",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

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
                        // Login Pengguna & Pemantau Mobil
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = EspressoBrown,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Login Pengguna & Pemantau Mobil",
                                style = MaterialTheme.typography.titleLarge,
                                color = EspressoBrown
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Digunakan bersama oleh pembawa mobil maupun rekan pemantau. Pilih nama Anda atau ketik langsung.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftMochaText
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Preset Names for Shared Multi-User Convenience
                        Text(
                            text = "PILIH CEPAT PROFIL PENGGUNA:",
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
                                label = "Andi (Logistik)",
                                isSelected = userName.contains("Andi"),
                                onClick = {
                                    userName = "Bapak Andi Pratama"
                                    userDivision = "Divisi Operasional & Logistik"
                                },
                                modifier = Modifier.weight(1f)
                            )
                            PresetProfileChip(
                                label = "Budi (Teknisi)",
                                isSelected = userName.contains("Budi"),
                                onClick = {
                                    userName = "Mas Budi Santoso"
                                    userDivision = "Divisi Teknik & Lapangan"
                                },
                                modifier = Modifier.weight(1f)
                            )
                            PresetProfileChip(
                                label = "Siti (Pemantau)",
                                isSelected = userName.contains("Siti"),
                                onClick = {
                                    userName = "Ibu Siti Rahma"
                                    userDivision = "Koordinator Armada & GA"
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

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

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = userDivision,
                            onValueChange = { userDivision = it },
                            label = { Text("Divisi / Bagian Keperluan") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = "Divisi",
                                    tint = EspressoBrown
                                )
                            },
                            singleLine = true,
                            colors = vintageTextFieldColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_user_division_input")
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { onLoginAsUserMonitor(userName, userDivision) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EspressoBrown,
                                contentColor = SoftGoldHighlight
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 14.dp),
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
                                text = "Masuk sebagai Pengguna & Pemantau",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else {
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
