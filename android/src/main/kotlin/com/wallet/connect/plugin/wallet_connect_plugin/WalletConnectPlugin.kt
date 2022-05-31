package com.wallet.connect.plugin.wallet_connect_plugin

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import com.walletconnect.walletconnectv2.client.WalletConnect
import com.walletconnect.walletconnectv2.client.WalletConnectClient
import io.flutter.Log
import java.lang.Exception

/** FrameworkPlugin */
class FrameworkPlugin : FlutterPlugin, ActivityAware, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity

    private lateinit var channel: MethodChannel
    private var activity: Activity? = null
    private lateinit var context: Context

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "framework_plugin")
        channel.setMethodCallHandler(this)

        context = flutterPluginBinding.applicationContext
    }

    private fun init() {
        val initDapp = WalletConnect.Params.Init(
                application = this.activity!!.application,
                useTls = true,
                hostName = WALLET_CONNECT_URL,
                projectId = "4b82893a311ba4feed105a3ef410954e",
                isController = false,
                metadata = WalletConnect.Model.AppMetaData(
                        name = "Wallet Connect Plugin",
                        description = "Wallet Connect description",
                        url = "https://walletconnect.com/",
                        icons = listOf("https://avatars.githubusercontent.com/u/37784886")
                )
        )
        WalletConnectClient.initialize(initDapp)
    }

    private companion object {
        const val WALLET_CONNECT_URL = "relay.walletconnect.com"
        const val TAG = "WalletConnectPlugin"
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "connect" -> {
                Log.d(TAG, ">>> connect")
                connect(call, result)
            }
//            "getDeviceInfo" -> {
//                val deviceInfo = DeviceInfo(context)
//                result.success(deviceInfo.data)
//            }
//            "getAudioInfo" -> {
//                val tempActivity: Activity? = activity
//                if (tempActivity == null) {
//                    result.success(null)
//                } else {
//                    val audioInfo = AudioInfo(tempActivity)
//                    result.success(audioInfo.data)
//                }
//            }
//            "checkRootedDevice" -> {
//                val rootUtil = RootUtil(context)
//                result.success(rootUtil.check())
//            }
//            "lastBootTime" -> {
//                result.success(System.currentTimeMillis() - SystemClock.elapsedRealtime())
//            }
//            "getTelephonyInfo" -> {
//                val telephonyInfo = TelephonyInfo(context)
//                result.success(telephonyInfo.data)
//            }
//            "getBatteryInfo" -> {
//                val batteryInfo = BatteryInfo(context)
//                result.success(batteryInfo.data)
//            }
//            "getApplicationInfo" -> {
//                val applicationInfo = ApplicationInfo(context)
//                result.success(applicationInfo.data)
//            }
//            "isMockLocation" -> {
//                val mockChecker = MockChecker(context)
//                result.success(mockChecker.detectMockLocationApplication())
//            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun connect(call: MethodCall, result: Result) {
        try {
            val connectParams = WalletConnect.Params.Connect(
                    permissions = WalletConnect.Model.SessionPermissions(
                            WalletConnect.Model.Blockchain(DEFAULT_MAIN_CHAINS),
                            WalletConnect.Model.Jsonrpc(DEFAULT_EIP155_METHODS)
                    ), pairingTopic = null
            )
            val uri = WalletConnectClient.connect(connectParams)
            result.success(uri)
//            if (uri != null) {
//                val pair = WalletConnect.Params.Pair(uri)
//                WalletConnectClient.pair(pair)
//            }
        } catch (e: Exception) {
            e.printStackTrace()
            result.error("startConnect error", null, null)
        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        init()
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
