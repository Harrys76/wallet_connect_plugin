package com.wallet.connect.plugin.wallet_connect_plugin

import android.app.Activity
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import io.flutter.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.nio.charset.Charset
import java.security.KeyStore
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/** FrameworkPlugin */
class WalletConnectPlugin : FlutterPlugin, ActivityAware, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity

    private lateinit var channel: MethodChannel
    private var activity: Activity? = null
    private lateinit var context: Context
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "wallet_connect_plugin")
        channel.setMethodCallHandler(this)

        context = flutterPluginBinding.applicationContext
    }

    private fun initWallet() {
        val initWallet = WalletConnect.Params.Init(
            application = activity!!.application,
            relayServerUrl = "wss://$WALLET_CONNECT_URL?projectId=4b82893a311ba4feed105a3ef410954e",
            isController = true,
            metadata = WalletConnect.Model.AppMetaData(
                name = "Wallet Connect Plugin",
                description = "Wallet Connect description",
                url = "https://walletconnect.com/",
                icons = listOf("https://avatars.githubusercontent.com/u/37784886")
            )
        )
        WalletConnectClient.initialize(initWallet)
        WalletConnectClient.setWalletDelegate(walletDelegate)
    }

    private val walletDelegate: WalletConnectClient.WalletDelegate =
        object : WalletConnectClient.WalletDelegate {
            override fun onSessionDelete(deletedSession: WalletConnect.Model.DeletedSession) {
                Log.d(TAG, "Deleted Session")
                Log.d(TAG, deletedSession.toString())
            }

            override fun onSessionNotification(sessionNotification: WalletConnect.Model.SessionNotification) {
                Log.d(TAG, "Session Notification")
                Log.d(TAG, sessionNotification.toString())
            }

            override fun onSessionProposal(sessionProposal: WalletConnect.Model.SessionProposal) {
                Log.d(TAG, "Session Proposal")
                Log.d(TAG, sessionProposal.toString())
            }

            override fun onSessionRequest(sessionRequest: WalletConnect.Model.SessionRequest) {
                Log.d(TAG, "Session Request")
                Log.d(TAG, sessionRequest.toString())
            }
        }

    private companion object {
        const val WALLET_CONNECT_URL = "relay.walletconnect.com"
        const val TAG = "WalletConnectPlugin"
        const val KEY_ALIAS = "WalletConnect"
        const val KEY_PROVIDER = "AndroidKeyStore"
        const val CIPHER_TRANSFORMATION = "AES/CBC/NoPadding"
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "connectWallet" -> {
                Log.d(TAG, ">>> connect wallet")
                connectWallet(call, result)
            }
            "showBiometricDialog" -> {
                Log.d(TAG, ">>> show Biometric Dialog")
                showBiometricDialog()
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun connectWallet(call: MethodCall, result: Result) {
        try {
            val pairListener = object : WalletConnect.Listeners.Pairing {
                override fun onSuccess(settledPairing: WalletConnect.Model.SettledPairing) {
                    Log.d(TAG, "Pairing Success!")
                    Log.d(TAG, settledPairing.toString())
                }

                override fun onError(error: Throwable) {
                    Log.d(TAG, "Pairing Failed!")
                    Log.d(TAG, error.toString())
                }
            }
            val uri = call.argument<String>("uri")!!
            Log.d(TAG, "URI: $uri")
            val pair = WalletConnect.Params.Pair(call.argument<String>("uri")!!)
            WalletConnectClient.pair(pair, pairListener)
        } catch (e: Exception) {
            e.printStackTrace()
            result.error("connect wallet error", null, null)
            Toast.makeText(
                activity?.applicationContext,
                "connect wallet error: ${e.message}", Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private fun getSecretKey(): SecretKey? {
        val keystore = KeyStore.getInstance(KEY_PROVIDER)
        keystore.load(null)

        val secretKeyEntry = keystore?.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return secretKeyEntry?.secretKey
    }

    private fun getCipher(): Cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)

    private fun createBiometricPrompt() {
        biometricPrompt = BiometricPrompt(
            activity as FragmentActivity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        activity?.applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    val encryptedInfo: ByteArray = result.cryptoObject?.cipher?.doFinal(
                        "walletconnect".toByteArray(Charset.defaultCharset())
                    )!!
                    Log.d(
                        TAG, "Encrypted information: " +
                                encryptedInfo.contentToString()
                    )
                    Toast.makeText(
                        activity?.applicationContext,
                        "Authentication succeeded!", Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        activity?.applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()
    }

    private fun showBiometricDialog() {
        val cipher = getCipher()
        val secretKey = getSecretKey()
        Log.d(TAG, ">>> secretKey: $secretKey")
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        } catch (e: Exception) {
            Toast.makeText(
                activity?.applicationContext, "Secret Key: $secretKey\n${e.message}",
                Toast.LENGTH_SHORT
            )
                .show()
        }
        biometricPrompt.authenticate(
            promptInfo,
            BiometricPrompt.CryptoObject(cipher)
        )
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        initWallet()
        executor = ContextCompat.getMainExecutor(activity?.applicationContext)
        createBiometricPrompt()
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEY_PROVIDER)
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
