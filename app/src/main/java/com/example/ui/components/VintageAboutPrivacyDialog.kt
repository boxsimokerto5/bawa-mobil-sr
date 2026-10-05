package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepInkBrown
import com.example.ui.theme.EspressoBrown
import com.example.ui.theme.MetallicGold
import com.example.ui.theme.RichLeatherBrown
import com.example.ui.theme.SoftGoldHighlight
import com.example.ui.theme.SoftMochaText
import com.example.ui.theme.VintageCardCream
import com.example.ui.theme.VintageCreamBg
import com.example.ui.theme.VintageGreenBg
import com.example.ui.theme.VintageGreenSuccess
import com.example.ui.theme.VintageParchmentSurface
import com.example.ui.theme.VintageWarmBorder

/**
 * Compact, proportional footer card placed at the bottom of User, Security, and School Admin lists
 * so users can open "Tentang Kami" or "Privacy Policy" directly in addition to the top bar icon.
 */
@Composable
fun VintageAboutPrivacyFooterCard(
    onOpenAboutUs: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("about_privacy_footer_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VintageParchmentSurface),
        border = BorderStroke(1.dp, AntiqueGold),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(EspressoBrown),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Informasi & Privasi",
                            tint = MetallicGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Informasi Sistem & Kebijakan Privasi SR",
                            style = MaterialTheme.typography.titleSmall,
                            color = DeepInkBrown,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Profil Bawa Mobil SR, SOP 3 Peran, & Perlindungan Data Personel",
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftMochaText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenAboutUs,
                    border = BorderStroke(1.2.dp, EspressoBrown),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("footer_open_about_us_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = EspressoBrown,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tentang Kami",
                        style = MaterialTheme.typography.labelMedium,
                        color = EspressoBrown,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(
                    onClick = onOpenPrivacyPolicy,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EspressoBrown,
                        contentColor = SoftGoldHighlight
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("footer_open_privacy_policy_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Policy,
                        contentDescription = null,
                        tint = MetallicGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.labelMedium,
                        color = SoftGoldHighlight,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Full-featured Vintage Dialog with 2 Tabs:
 * Tab 0 = Tentang Kami (Profil Sistem Bawa Mobil SR, Armada, SOP 3 Peran)
 * Tab 1 = Privacy Policy (Kebijakan Privasi Lengkap & Perlindungan Data Personel)
 */
@Composable
fun VintageAboutAndPrivacyDialog(
    initialTab: Int = 0,
    onDismiss: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab.coerceIn(0, 1)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.58f))
                .padding(horizontal = 12.dp, vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .fillMaxHeight(0.92f)
                    .testTag("about_privacy_dialog"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = VintageCreamBg),
                border = BorderStroke(2.5.dp, MetallicGold),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Classic Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(EspressoBrown, RichLeatherBrown)
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                        .background(MetallicGold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (selectedTab == 0) Icons.Default.Info else Icons.Default.Policy,
                                        contentDescription = null,
                                        tint = DeepInkBrown,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (selectedTab == 0) {
                                            "Tentang Kami • Bawa Mobil SR"
                                        } else {
                                            "Privacy Policy • Kebijakan Privasi"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        color = SoftGoldHighlight,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Sistem Manajemen & Pemantauan Armada Operasional SR",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VintageCreamBg.copy(alpha = 0.88f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Surface(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(50),
                                color = SoftGoldHighlight,
                                border = BorderStroke(1.dp, MetallicGold),
                                modifier = Modifier.testTag("close_about_privacy_dialog_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Tutup",
                                        tint = DeepInkBrown,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tutup",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DeepInkBrown,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2-Tab Selector Pill Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DialogTabPill(
                                title = "Tentang Kami",
                                icon = Icons.Default.Info,
                                isSelected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                testTag = "tab_about_us",
                                modifier = Modifier.weight(1f)
                            )
                            DialogTabPill(
                                title = "Privacy Policy",
                                icon = Icons.Default.Policy,
                                isSelected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                testTag = "tab_privacy_policy",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Scrollable Content Area
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (selectedTab == 0) {
                            AboutUsContentSection()
                        } else {
                            PrivacyPolicyContentSection()
                        }
                    }

                    // Bottom Footer Bar inside Dialog
                    Surface(
                        color = VintageParchmentSurface,
                        border = BorderStroke(1.dp, VintageWarmBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Bawa Mobil SR • Edisi Klasik Terpadu",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = EspressoBrown,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Berlaku untuk Pengguna, Pos Keamanan, & Admin Sekolah",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SoftMochaText
                                )
                            }
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EspressoBrown,
                                    contentColor = SoftGoldHighlight
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Mengerti",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogTabPill(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (isSelected) MetallicGold else SoftGoldHighlight.copy(alpha = 0.14f),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) SoftGoldHighlight else MetallicGold.copy(alpha = 0.7f)
        ),
        modifier = modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) DeepInkBrown else SoftGoldHighlight,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) DeepInkBrown else SoftGoldHighlight,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AboutUsContentSection() {
    VintageOrnamentalDivider(label = "PROFIL & VISI BAWA MOBIL SR")

    InfoSectionCard(
        icon = Icons.Default.DirectionsCar,
        title = "Apa Itu Aplikasi Bawa Mobil SR?",
        badgeText = "Profil Sistem",
        body = "Bawa Mobil SR adalah sistem informasi manajemen izin keluar-masuk, pemantauan peta jalan real-time (OpenStreetMap), serta rekapitulasi jarak tempuh armada operasional di lingkungan Sekolah & Asrama SR.\n\n" +
            "Aplikasi ini dirancang dengan tata kelola satu pintu agar setiap pemakaian kendaraan tercatat tertib mulai dari pengajuan rencana, verifikasi di gerbang Pos Keamanan, pelacakan garis perjalanan langsung, hingga penutupan log saat kendaraan kembali ke Pos Utama SR."
    )

    VintageOrnamentalDivider(label = "3 UNIT ARMADA OPERASIONAL SR")

    InfoSectionCard(
        icon = Icons.Default.Route,
        title = "Daftar Kendaraan & Peruntukan Operasional",
        badgeText = "3 Unit Aktif",
        body = "1. Daihatsu Gran Max (AG 8821 SR)\n" +
            "   • Kategori: Mobil Logistik & Dapur Umum\n" +
            "   • Peruntukan: Belanja logistik dapur, pengangkutan barang asrama, dan perlengkapan sekolah.\n\n" +
            "2. Yamaha N-Max 155 (AG 4419 SR)\n" +
            "   • Kategori: Motor Kurir & Operasional Cepat\n" +
            "   • Peruntukan: Pengiriman dokumen mendesak TU/Tendik, pembelian obat/klinik, dan mobilitas cepat.\n\n" +
            "3. Toyota Avanza Veloz (AG 1029 SR)\n" +
            "   • Kategori: Mobil Dinas & Tamu Sekolah\n" +
            "   • Peruntukan: Kunjungan dinas Guru/Kepala Sekolah, pendampingan santri/siswa, dan penjemputan tamu resmi."
    )

    VintageOrnamentalDivider(label = "SINERGI 3 PERAN OTORITAS SR")

    InfoSectionCard(
        icon = Icons.Default.VerifiedUser,
        title = "1. Peran Pengguna & Pemantau (Personel SR)",
        badgeText = "Pengguna",
        body = "• Mencakup divisi tugas: Guru, TU / Tendik, Wali Asrama, Wali Asuh, Dapur, dan Kebersihan.\n" +
            "• Mengisi Formulir Rencana Bawa Mobil (memilih unit yang tersedia, memilih/menambah titik tujuan baru dengan auto-suggest peta, jam berangkat-kembali, serta odometer awal).\n" +
            "• Memantau posisi armada secara transparan agar mengetahui ketersediaan unit di Pos Utama SR."
    )

    InfoSectionCard(
        icon = Icons.Default.Shield,
        title = "2. Peran Pos Keamanan (Petugas Piket Gerbang)",
        badgeText = "Keamanan",
        body = "• Memeriksa permohonan izin keluar mobil yang diajukan oleh pengguna.\n" +
            "• Memberikan persetujuan ('Setujui Jalan') atau penolakan beserta catatan resmi Pos Keamanan.\n" +
            "• Begitu disetujui, sistem otomatis menggambar garis perjalanan real-time dari Pos Utama SR menuju titik-titik tujuan hingga mobil kembali ke SR.\n" +
            "• Mengakhiri perjalanan ('Akhiri Kembali di SR') saat kendaraan telah masuk kembali ke gerbang Pos Utama SR."
    )

    InfoSectionCard(
        icon = Icons.Default.AdminPanelSettings,
        title = "3. Peran Admin Sekolah SR",
        badgeText = "Otoritas Admin",
        body = "• Memverifikasi pendaftaran akun personel baru setelah pengguna melakukan verifikasi email dan mengunggah kelengkapan biodata (Foto Diri, Nama Lengkap, Alamat, Nomor HP, Foto KTP, dan Divisi Tugas).\n" +
            "• Menyetujui atau menolak pengajuan akun personel demi menjaga keamanan akses internal.\n" +
            "• Mengawasi direktori seluruh personel aktif serta mengaudit rekapitulasi rute dan jarak tempuh seluruh armada sekolah."
    )
}

@Composable
private fun PrivacyPolicyContentSection() {
    VintageOrnamentalDivider(label = "KEBIJAKAN PRIVASI & PERLINDUNGAN DATA")

    InfoSectionCard(
        icon = Icons.Default.Policy,
        title = "1. Komitmen Perlindungan Privasi Personel SR",
        badgeText = "Resmi & Mengikat",
        body = "Manajemen Sekolah & Asrama SR berkomitmen penuh untuk melindungi privasi, keamanan identitas, serta kerahasiaan data pribadi seluruh personel (Pengguna, Petugas Keamanan, dan Staf Sekolah) yang menggunakan aplikasi Bawa Mobil SR. Kebijakan Privasi ini menjelaskan bagaimana data dikumpulkan, digunakan, disimpan, dan dilindungi."
    )

    InfoSectionCard(
        icon = Icons.Default.VerifiedUser,
        title = "2. Data yang Kami Kumpulkan",
        badgeText = "Verifikasi & Operasional",
        body = "Aplikasi mengumpulkan data terbatas yang mutlak diperlukan untuk tata kelola izin armada sekolah:\n" +
            "• Data Akun & Identitas: Nama lengkap, alamat email terverifikasi, nomor telepon/WhatsApp aktif, alamat tempat tinggal, dan divisi penugasan (Keamanan, Dapur, Kebersihan, Wali Asuh, Wali Asrama, Guru, atau TU/Tendik).\n" +
            "• Dokumen Validasi Kepegawaian: Unggahan Foto Diri dan Foto Kartu Tanda Penduduk (KTP) saat pendaftaran akun baru.\n" +
            "• Data Operasional Kendaraan: Rencana tujuan perjalanan, keperluan dinas, waktu keberangkatan & kembali, serta angka odometer awal dan akhir."
    )

    InfoSectionCard(
        icon = Icons.Default.GpsFixed,
        title = "3. Penggunaan Lokasi GPS & Pelacakan Rute Peta",
        badgeText = "Hanya Saat Dinas",
        body = "• Izin Lokasi (ACCESS_FINE_LOCATION & ACCESS_COARSE_LOCATION) digunakan semata-mata untuk menyinkronkan posisi kendaraan pada peta OpenStreetMap dan membentuk garis rekap perjalanan (route trail) serta menghitung jarak tempuh (km).\n" +
            "• Batasan Waktu Pelacakan: Perekaman jejak rute hanya berjalan sejak permohonan mobil disetujui oleh Pos Keamanan ('Sedang Jalan') dan otomatis berhenti sepenuhnya begitu petugas Keamanan menekan tombol 'Akhiri Kembali di SR'.\n" +
            "• Aplikasi tidak melacak lokasi pribadi pengguna di luar jam peminjaman armada sekolah."
    )

    InfoSectionCard(
        icon = Icons.Default.Lock,
        title = "4. Penyimpanan, Keamanan & Pembatasan Akses KTP",
        badgeText = "Enkripsi & Terbatas",
        body = "• Dokumen Foto Diri dan Foto KTP yang diunggah oleh pendaftar hanya dapat dilihat dan diverifikasi oleh otoritas resmi Admin Sekolah SR.\n" +
            "• Kami menerapkan prinsip hak akses minimum (least-privilege): sesama pengguna biasa tidak dapat melihat dokumen KTP milik personel lain.\n" +
            "• Seluruh data disimpan secara aman dan tidak pernah diperjualbelikan, disewakan, atau dibagikan kepada pihak ketiga komersial mana pun di luar institusi Sekolah SR."
    )

    InfoSectionCard(
        icon = Icons.Default.CheckCircle,
        title = "5. Hak Pengguna atas Data Pribadi",
        badgeText = "Hak Personel",
        body = "Setiap personel yang terdaftar memiliki hak penuh untuk:\n" +
            "• Meninjau status verifikasi akun dan riwayat pemakaian kendaraan yang pernah diajukan.\n" +
            "• Meminta pembaruan data biodata, pergantian divisi tugas, atau pembaruan dokumen kepada Admin Sekolah.\n" +
            "• Mengajukan penonaktifan atau penghapusan akun serta dokumen KTP apabila sudah tidak lagi bertugas di lingkungan Sekolah & Asrama SR."
    )
}

@Composable
private fun InfoSectionCard(
    icon: ImageVector,
    title: String,
    badgeText: String,
    body: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = VintageCardCream),
        border = BorderStroke(1.5.dp, AntiqueGold),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EspressoBrown),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MetallicGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = DeepInkBrown,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = VintageGreenBg,
                    border = BorderStroke(1.dp, VintageGreenSuccess)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        color = VintageGreenSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = VintageParchmentSurface,
                border = BorderStroke(1.dp, VintageWarmBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = DeepInkBrown,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}
