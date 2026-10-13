package com.yubico.labs.pqc.checker.ui

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.security.KeyPairGenerator
import java.security.Signature

class PkiCheckerViewModel : ViewModel() {
    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status

    data class Algorithm(
        val name: String,
        val strongbox: Boolean,
    )

    private val algorithms = listOf(
        Algorithm("ML-DSA", false),
        Algorithm("ML-DSA", true),
        Algorithm("ML-DSA-44", false),
        Algorithm("ML-DSA-44", true),
        Algorithm("ML-DSA-65", false),
        Algorithm("ML-DSA-65", true),
        Algorithm("ML-DSA-87", false),
        Algorithm("ML-DSA-87", true),
    )

    fun checkPqc(plainText: String) {
        _status.value = ""
        viewModelScope.launch(Dispatchers.IO) {
            for ((name, useStrongbox) in algorithms) {
                try {
                    _status.value += "Trying $name${if (useStrongbox) "(strongboxed)" else ""}\n"
                    val kpg = KeyPairGenerator.getInstance(
                        name,
                        "AndroidKeyStore"
                    )
                    val spec = KeyGenParameterSpec.Builder(
                        "alias_${name.lowercase()}",
                        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                    )
                        .setIsStrongBoxBacked(useStrongbox)
                        .build()

                    kpg.initialize(spec)
                    val kp = kpg.generateKeyPair()
                    _status.value += ".. initialized.\n"
                    _status.value += ".. public key size: ${kp.public.encoded.size} bytes encoded.\n"

                    val signer = Signature.getInstance(name)
                    signer.initSign(kp.private)
                    signer.update(plainText.toByteArray())

                    val signature = signer.sign()
                    _status.value += ".. signed \"$plainText\"\n"
                    _status.value += ".. signature: ${signature.toHexString()}\n"
                    _status.value += "..            (${signature.size} bytes)\n"

                    val verifier = Signature.getInstance(name)
                    verifier.initVerify(kp.public)
                    verifier.update(plainText.toByteArray())
                    val verified = verifier.verify(signature)
                    _status.value += ".. verified: $verified.\n"

                    _status.value += ".. ✅ Supported ✅\n"

                } catch (e: Exception) {
                    _status.value += ".. ❌ Not Supported ❌\n"
                    _status.value += ".. error: ${e.javaClass.simpleName}\n"
                } finally {
                    _status.value += "\n"
                }
            }
        }
    }
}
