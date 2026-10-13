package com.yubico.labs.pqc.checker.ui

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

data class AppInfo(
    val packageName: String,
    val appName: String,
    val signingAlgorithms: List<String>,
    val wasSignedWithPqcAlgorithm: Boolean,
    val error: String? = null
)

class ApkCheckerViewModel(application: Application) : AndroidViewModel(application) {

    private val _status = MutableStateFlow<List<AppInfo>>(emptyList())
    val status: StateFlow<List<AppInfo>> = _status.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    fun checkApks() {
        if (_isScanning.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _isScanning.value = true

            val pm = getApplication<Application>().packageManager
            val flags = PackageManager.GET_META_DATA or PackageManager.GET_SIGNING_CERTIFICATES
            val installedApps = pm.getInstalledPackages(flags)
            val results = mutableListOf<AppInfo>()

            for (app in installedApps) {
                val appName = app.applicationInfo?.let { pm.getApplicationLabel(it).toString() }
                    ?: app.packageName

                try {
                    val algorithms = getSigningAlgorithms(app)
                    results.add(
                        AppInfo(
                            packageName = app.packageName,
                            appName = appName,
                            signingAlgorithms = algorithms,
                            wasSignedWithPqcAlgorithm = algorithms.containsPqcAlgorithmName()
                        )
                    )
                } catch (e: Exception) {
                    results.add(
                        AppInfo(
                            packageName = app.packageName,
                            appName = appName,
                            signingAlgorithms = listOf(),
                            wasSignedWithPqcAlgorithm = false,
                            error = e.message
                        )
                    )
                } finally {
                    _status.value = results.sortedByDescending { it.wasSignedWithPqcAlgorithm }
                }
            }

            _isScanning.value = false
        }
    }

    private fun getSigningAlgorithms(packageInfo: PackageInfo): List<String> {
        val signingInfo = packageInfo.signingInfo ?: return listOf()
        val signatures = if (signingInfo.hasMultipleSigners()) {
            signingInfo.apkContentsSigners
        } else {
            signingInfo.signingCertificateHistory
        }

        if (signatures.isNullOrEmpty()) return listOf()

        val certFactory = CertificateFactory.getInstance("X.509")

        return signatures.mapNotNull { signature ->
            val certBytes = signature.toByteArray()
            val cert = certFactory.generateCertificate(certBytes.inputStream()) as? X509Certificate

            cert?.publicKey?.algorithm
        }
    }

    fun launchApp(apk: AppInfo) {
        val pm = getApplication<Application>().packageManager
        val intent = pm.getLaunchIntentForPackage(apk.packageName)
        application.startActivity(intent)
    }
}

private fun List<String>.containsPqcAlgorithmName(): Boolean = any { algorithm ->
    algorithm.contains("ML-DSA", ignoreCase = true) ||
            algorithm.contains("Dilithium", ignoreCase = true) ||
            algorithm.startsWith("2.16.840.1.101.3.4.3")
}
