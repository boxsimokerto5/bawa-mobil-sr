package com.example.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

data class DriverAuthProfile(
    val uid: String,
    val displayName: String,
    val email: String?,
    val photoUrl: String?,
    val authMethod: String
)

sealed class DriverAuthResult {
    data class Success(val profile: DriverAuthProfile) : DriverAuthResult()
    data class Error(val message: String) : DriverAuthResult()
    data object Cancelled : DriverAuthResult()
}

object DriverAuthManager {

    private fun getFirebaseAuthOrNull(context: Context): FirebaseAuth? {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseAuth.getInstance()
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun getCurrentSignedInDriver(context: Context): DriverAuthProfile? {
        val user = getFirebaseAuthOrNull(context)?.currentUser ?: return null
        val name = user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Pengemudi Terverifikasi"
        return DriverAuthProfile(
            uid = user.uid,
            displayName = name,
            email = user.email,
            photoUrl = user.photoUrl?.toString(),
            authMethod = if (user.isAnonymous) "Firebase Anonymous Auth" else "Google & Firebase Auth"
        )
    }

    suspend fun signInWithGoogle(
        activityContext: Context,
        fallbackDivision: String
    ): DriverAuthResult {
        val credentialManager = CredentialManager.create(activityContext)
        val rawWebClientId = resolveWebClientId(activityContext)

        if (rawWebClientId.isBlank() ||
            rawWebClientId == "YOUR_GOOGLE_WEB_CLIENT_ID.apps.googleusercontent.com"
        ) {
            // Try Firebase Anonymous Auth if Firebase is configured, or explain Web Client ID setup
            val firebaseAuth = getFirebaseAuthOrNull(activityContext)
            if (firebaseAuth != null) {
                return try {
                    val authResult = firebaseAuth.signInAnonymously().await()
                    val user = authResult.user
                    DriverAuthResult.Success(
                        DriverAuthProfile(
                            uid = user?.uid ?: "firebase-driver",
                            displayName = user?.displayName ?: "Pengemudi Terverifikasi",
                            email = user?.email,
                            photoUrl = null,
                            authMethod = "Firebase Auth"
                        )
                    )
                } catch (e: Exception) {
                    DriverAuthResult.Error(
                        "Konfigurasi GOOGLE_WEB_CLIENT_ID di panel Secrets atau tambahkan google-services.json untuk mengaktifkan Google Sign-In penuh. (${e.localizedMessage ?: "Firebase belum dikonfigurasi"})"
                    )
                }
            }
            return DriverAuthResult.Error(
                "Untuk Google Sign-In penuh, isi GOOGLE_WEB_CLIENT_ID di panel Secrets AI Studio atau sertakan google-services.json."
            )
        }

        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(rawWebClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseAuth = getFirebaseAuthOrNull(activityContext)
                if (firebaseAuth != null) {
                    val firebaseCred = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = firebaseAuth.signInWithCredential(firebaseCred).await()
                    val firebaseUser = authResult.user
                    val resolvedName = firebaseUser?.displayName?.takeIf { it.isNotBlank() }
                        ?: googleIdTokenCredential.displayName?.takeIf { it.isNotBlank() }
                        ?: googleIdTokenCredential.id.substringBefore("@")

                    DriverAuthResult.Success(
                        DriverAuthProfile(
                            uid = firebaseUser?.uid ?: googleIdTokenCredential.id,
                            displayName = resolvedName,
                            email = firebaseUser?.email ?: googleIdTokenCredential.id,
                            photoUrl = firebaseUser?.photoUrl?.toString()
                                ?: googleIdTokenCredential.profilePictureUri?.toString(),
                            authMethod = "Google & Firebase Auth"
                        )
                    )
                } else {
                    val resolvedName = googleIdTokenCredential.displayName?.takeIf { it.isNotBlank() }
                        ?: googleIdTokenCredential.id.substringBefore("@")
                    DriverAuthResult.Success(
                        DriverAuthProfile(
                            uid = googleIdTokenCredential.id,
                            displayName = resolvedName,
                            email = googleIdTokenCredential.id,
                            photoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                            authMethod = "Google Credential Manager"
                        )
                    )
                }
            } else {
                DriverAuthResult.Error("Format kredensial Google tidak dikenali.")
            }
        } catch (_: GetCredentialCancellationException) {
            DriverAuthResult.Cancelled
        } catch (_: NoCredentialException) {
            DriverAuthResult.Error(
                "Tidak ditemukan akun Google yang aktif di perangkat ini. Pastikan akun Google sudah login di pengaturan Android atau gunakan form nama pengemudi."
            )
        } catch (e: GetCredentialException) {
            DriverAuthResult.Error(
                "Gagal memuat kredensial Google Sign-In: ${e.localizedMessage ?: "Periksa konfigurasi SHA-1 & Web Client ID"}"
            )
        } catch (e: Exception) {
            DriverAuthResult.Error(
                "Autentikasi Firebase gagal: ${e.localizedMessage ?: "Kesalahan tidak diketahui"}"
            )
        }
    }

    suspend fun signInWithEmailAndPassword(
        context: Context,
        email: String,
        password: String
    ): DriverAuthResult {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || password.isBlank()) {
            return DriverAuthResult.Error("Email dan kata sandi pengemudi wajib diisi.")
        }
        val firebaseAuth = getFirebaseAuthOrNull(context)
            ?: return DriverAuthResult.Error(
                "Layanan Firebase Auth memerlukan file google-services.json yang aktif pada proyek."
            )

        return try {
            val result = try {
                firebaseAuth.signInWithEmailAndPassword(cleanEmail, password).await()
            } catch (_: Exception) {
                firebaseAuth.createUserWithEmailAndPassword(cleanEmail, password).await()
            }
            val user = result.user
            val displayName = user?.displayName?.takeIf { it.isNotBlank() }
                ?: cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
            DriverAuthResult.Success(
                DriverAuthProfile(
                    uid = user?.uid ?: cleanEmail,
                    displayName = displayName,
                    email = user?.email ?: cleanEmail,
                    photoUrl = user?.photoUrl?.toString(),
                    authMethod = "Firebase Email Auth"
                )
            )
        } catch (e: Exception) {
            DriverAuthResult.Error(
                "Autentikasi Firebase gagal: ${e.localizedMessage ?: "Periksa email/password atau konfigurasi Firebase."}"
            )
        }
    }

    suspend fun signOut(context: Context) {
        try {
            getFirebaseAuthOrNull(context)?.signOut()
        } catch (_: Exception) {
        }
        try {
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {
        }
    }

    suspend fun tryRegisterAndSendFirebaseVerificationEmail(
        context: Context,
        email: String,
        password: String
    ): Boolean {
        val firebaseAuth = getFirebaseAuthOrNull(context) ?: return false
        return try {
            val authResult = try {
                firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            } catch (_: Exception) {
                firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await()
            }
            authResult.user?.sendEmailVerification()?.await()
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun checkFirebaseEmailVerified(context: Context): Boolean {
        val user = getFirebaseAuthOrNull(context)?.currentUser ?: return false
        return try {
            user.reload().await()
            user.isEmailVerified
        } catch (_: Exception) {
            false
        }
    }

    private fun resolveWebClientId(context: Context): String {
        val fromBuildConfig = try {
            BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
        } catch (_: Exception) {
            ""
        }
        if (fromBuildConfig.isNotBlank() &&
            fromBuildConfig != "YOUR_GOOGLE_WEB_CLIENT_ID.apps.googleusercontent.com"
        ) {
            return fromBuildConfig
        }
        val resId = context.resources.getIdentifier(
            "default_web_client_id",
            "string",
            context.packageName
        )
        return if (resId != 0) {
            context.getString(resId).trim()
        } else {
            fromBuildConfig
        }
    }
}
