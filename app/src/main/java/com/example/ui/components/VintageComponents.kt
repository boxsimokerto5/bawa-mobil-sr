package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TripStatus
import com.example.data.UserRole
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepBronzeGold
import com.example.ui.theme.DeepInkBrown
import com.example.ui.theme.EspressoBrown
import com.example.ui.theme.MetallicGold
import com.example.ui.theme.RichLeatherBrown
import com.example.ui.theme.SoftGoldHighlight
import com.example.ui.theme.SoftMochaText
import com.example.ui.theme.VintageAmberBg
import com.example.ui.theme.VintageAmberPending
import com.example.ui.theme.VintageCardCream
import com.example.ui.theme.VintageCreamBg
import com.example.ui.theme.VintageCrimsonBg
import com.example.ui.theme.VintageCrimsonReject
import com.example.ui.theme.VintageGreenBg
import com.example.ui.theme.VintageGreenSuccess
import com.example.ui.theme.VintageParchmentSurface
import com.example.ui.theme.VintageWarmBorder

@Composable
fun VintageTopBar(
    title: String,
    subtitle: String,
    currentRole: UserRole,
    pendingCountForBadge: Int,
    onQuickSwitchRole: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = EspressoBrown,
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(EspressoBrown, RichLeatherBrown)
                    )
                )
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(MetallicGold, AntiqueGold)
                                )
                            )
                            .border(1.5.dp, VintageCreamBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (currentRole) {
                                UserRole.SECURITY -> Icons.Default.Shield
                                UserRole.SCHOOL_ADMIN -> Icons.Default.AdminPanelSettings
                                else -> Icons.Default.DirectionsCar
                            },
                            contentDescription = "Logo Peran Bawa Mobil SR",
                            tint = DeepInkBrown,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = SoftGoldHighlight,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = VintageCreamBg.copy(alpha = 0.88f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Compact Quick Role Switch Pill so title & user name have plenty of room
                    Surface(
                        onClick = onQuickSwitchRole,
                        shape = RoundedCornerShape(50),
                        color = SoftGoldHighlight.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, MetallicGold),
                        modifier = Modifier.testTag("switch_role_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Ganti Peran",
                                tint = MetallicGold,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = when (currentRole) {
                                    UserRole.USER_MONITOR -> "Keamanan"
                                    UserRole.SECURITY -> "Admin"
                                    UserRole.SCHOOL_ADMIN -> "Pengguna"
                                    else -> "Peran"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftGoldHighlight,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            if (currentRole == UserRole.USER_MONITOR && pendingCountForBadge > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(MetallicGold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = pendingCountForBadge.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DeepInkBrown,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        onClick = onLogout,
                        shape = RoundedCornerShape(50),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, VintageWarmBorder.copy(alpha = 0.55f)),
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Keluar",
                                tint = VintageCreamBg,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Keluar",
                                style = MaterialTheme.typography.labelSmall,
                                color = VintageCreamBg,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Ornamental Gold Hairline Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                DeepBronzeGold,
                                MetallicGold,
                                SoftGoldHighlight,
                                MetallicGold,
                                DeepBronzeGold
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun VintageNotificationBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("notification_banner"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SoftGoldHighlight),
        border = BorderStroke(1.5.dp, AntiqueGold),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Info",
                    tint = EspressoBrown,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = DeepInkBrown
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Tutup Notifikasi",
                    tint = EspressoBrown,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun TripStatusBadge(
    status: TripStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor, labelText, icon) = when (status) {
        TripStatus.PENDING_APPROVAL -> Quintuple(
            VintageAmberBg,
            AntiqueGold,
            VintageAmberPending,
            "Menunggu Izin",
            Icons.Default.Schedule
        )
        TripStatus.IN_TRANSIT -> Quintuple(
            VintageGreenBg,
            VintageGreenSuccess,
            VintageGreenSuccess,
            "Sedang Jalan",
            Icons.Default.DirectionsCar
        )
        TripStatus.COMPLETED -> Quintuple(
            VintageParchmentSurface,
            VintageWarmBorder,
            EspressoBrown,
            "Selesai Kembali",
            Icons.Default.CheckCircle
        )
        TripStatus.REJECTED -> Quintuple(
            VintageCrimsonBg,
            VintageCrimsonReject,
            VintageCrimsonReject,
            "Ditolak Pos",
            Icons.Default.Close
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = labelText,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = labelText,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

fun vehicleIconFor(vehicleId: String): ImageVector {
    return when (vehicleId) {
        "VH_GRANMAX" -> Icons.Default.LocalShipping
        "VH_NMAX" -> Icons.Default.TwoWheeler
        else -> Icons.Default.DirectionsCar
    }
}

@Composable
fun VintageOrnamentalDivider(
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.weight(1f).height(2.dp)) {
            drawLine(
                color = AntiqueGold.copy(alpha = 0.6f),
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 2f
            )
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = VintageParchmentSurface,
            border = BorderStroke(1.dp, AntiqueGold),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = EspressoBrown,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
        Canvas(modifier = Modifier.weight(1f).height(2.dp)) {
            drawLine(
                color = AntiqueGold.copy(alpha = 0.6f),
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 2f
            )
        }
    }
}
