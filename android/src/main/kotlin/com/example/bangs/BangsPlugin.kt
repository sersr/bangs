package com.example.bangs

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.NonNull
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** BangsPlugin */
class BangsPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
//    companion object {
//        private const val TAG = "BangsPlugin"
//    }

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var activity: Activity? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "bangs")
        channel.setMethodCallHandler(this)
    }

    private fun onNavigationBarChanged(activity: Activity, remove: Boolean = false) {
        val height = getBottomHeight(activity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            if(remove){
                ViewCompat.setOnApplyWindowInsetsListener(activity.window.decorView, null)
                return
            }
            ViewCompat.setOnApplyWindowInsetsListener(
                activity.window.decorView,
                OnApplyWindowInsetsListener {

                        view: View, insets: WindowInsetsCompat ->

                    val isShowing: Boolean

                    val naviHeight = insets.systemWindowInsetBottom

                    isShowing = naviHeight == height

                    if (naviHeight <= height) {
                        Log.d("onNavigationBarChanged", "导航栏是否显示：$isShowing")
                        channel.invokeMethod(
                            "navigationChange",
                            mapOf("isShowing" to isShowing, "height" to naviHeight)
                        )
                    }

                    return@OnApplyWindowInsetsListener ViewCompat.onApplyWindowInsets(view, insets)


                })
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${Build.VERSION.RELEASE}")
            }
            "safePadding" -> {
                val padding = hasNotchInScreen(activity)
                result.success(padding)
            }
            "bottomHeight" -> {
                val height = getBottomHeight(activity)
                result.success(height)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun getBottomHeight(activity: Activity?): Int {
        val height = activity?.resources?.getDimensionPixelSize(
            activity.resources.getIdentifier("navigation_bar_height", "dimen", "android")
        )

        if (height != null) {
            return height
        }
        return 0
    }

    private fun hasNotchInScreen(activity: Activity?): Map<String, Double> {
        if (activity == null) {
            return mapOf()
        }
        // android  P 以上有标准 API 来判断是否有刘海屏
        val decorView = activity.window.decorView
        val decorViewWidth = decorView.width
        val decorViewHeight = decorView.height
        // Log.i(TAG, "decorView Height  $decorViewHeight")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val windowInsets = decorView.rootWindowInsets
            if (windowInsets != null) {
                val displayCutout = windowInsets.displayCutout
                if (displayCutout != null) {
                    val left = displayCutout.safeInsetLeft
                    val top = displayCutout.safeInsetTop
                    val right = displayCutout.safeInsetRight
                    val bottom = displayCutout.safeInsetBottom
                    // Log.i(TAG, displayCutout.boundingRects.toString())
                    return mapOf(
                        "left" to left.toDouble(), "top" to top.toDouble(),
                        "right" to right.toDouble(), "bottom" to bottom.toDouble(),
                        "height" to decorViewHeight.toDouble(), "width" to decorViewWidth.toDouble()
                    )
                }
            }
        }
        return mapOf("height" to decorViewHeight.toDouble(), "width" to decorViewWidth.toDouble())
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        if(activity != null){
            onNavigationBarChanged(activity!!,true)
        }
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        onNavigationBarChanged(activity!!)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        onNavigationBarChanged(activity!!,true)
        activity = null
    }
}
