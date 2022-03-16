package com.example.firestickdemo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.amazon.whisperplay.fling.media.controller.DiscoveryController
import com.amazon.whisperplay.fling.media.controller.DiscoveryController.IDiscoveryListener
import com.amazon.whisperplay.fling.media.controller.RemoteMediaPlayer
import com.amazon.whisperplay.fling.media.service.CustomMediaPlayer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future


class MainActivity : AppCompatActivity() {

    private val MONITOR_INTERVAL = 1000L
    private var mDeviceList: LinkedList<RemoteMediaPlayer?> = LinkedList()

    private var mController: DiscoveryController? = null

    val TAG = "AmazonFlingModule"
    private var targetUuid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mController = DiscoveryController(this);

        findViewById<Button>(R.id.btStart).setOnClickListener {
            startSearch()
        }
        findViewById<Button>(R.id.btStopSearch).setOnClickListener {
            stopSearch()
        }
        findViewById<Button>(R.id.btConnect).setOnClickListener {
            fling()
        }
        findViewById<Button>(R.id.btPlay).setOnClickListener {
            doPlay()

        }
        findViewById<Button>(R.id.btnPause).setOnClickListener {
            doPause()
        }
        findViewById<Button>(R.id.btnStop).setOnClickListener {
            doStop()
        }
        findViewById<Button>(R.id.btnSeek).setOnClickListener {
            doSeek("30000")
        }
    }

    private val mDiscovery: IDiscoveryListener = object : IDiscoveryListener {
        override fun playerDiscovered(player: RemoteMediaPlayer) {
            Log.d(TAG, "playerDiscovered$player")
            //add media player to the application’s player list.
            updateDeviceList(player)
        }

        override fun playerLost(player: RemoteMediaPlayer) {
            Log.d(TAG, "jed2")
            //remove media player from the application’s player list.
        }

        override fun discoveryFailure() {
            Log.d(TAG, "jed3")
        }
    }

    private fun startSearch() {
        Log.v(TAG, "startSearch")
        mController?.start("amzn.thin.pl", mDiscovery)
    }

    private fun stopSearch() {
        Log.v(TAG, "stopSearch")
        if (mController != null) {
            mController?.stop()
        }
    }

    private fun updateDeviceList(device: RemoteMediaPlayer?) {
        Log.v(TAG, "updateDeviceList")
        if (mDeviceList.contains(device)) {
            mDeviceList.remove(device)
        }
        mDeviceList.add(device)
        val arrOfDevices = JSONArray()
        for (dev in mDeviceList) {
            val json = JSONObject()
            try {
                json.put("name", dev?.name)
                json.put("uuid", dev?.uniqueIdentifier)

                targetUuid = dev?.uniqueIdentifier ?: ""
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            arrOfDevices.put(json)
        }

        findViewById<TextView>(R.id.tvDeviceFound).text = arrOfDevices.toString()
    }

    private fun getRemoteMediaPlayerFromUUID(targetUuid: String): RemoteMediaPlayer? {
        var target: RemoteMediaPlayer? = null
        for (device in mDeviceList) {
            Log.v(TAG, device.toString())
            if (device?.uniqueIdentifier == targetUuid) {
                target = device
                Log.v(TAG, device.toString())
            }
        }
        return target
    }

    private fun fling() {
        val target = getRemoteMediaPlayerFromUUID(targetUuid)
        Log.v(TAG, target.toString())
        if (target != null) {
            Log.i(TAG, "try setPositionUpdateInterval: $MONITOR_INTERVAL")
            target.setPositionUpdateInterval(MONITOR_INTERVAL).getAsync(
                ErrorResultHandler(
                    "setPositionUpdateInterval",
                    "Error attempting set update interval, ignoring", true
                )
            )
            Log.i(TAG, "try setMediaSource: url - ")
            target.setMediaSource(
                "http://d1fb1b55vzzqwl.cloudfront.net/en-us/torahclass/video/ot/genesis/video-genesis-l07-ch6-ch7.mp4",
                "",
                true,
                false
            ).getAsync(
                ErrorResultHandler("setMediaSource", "Error attempting to Play:", true)
            )
        }
    }

    private fun doPlay() {
        val target = getRemoteMediaPlayerFromUUID(targetUuid)
        if (target != null) {
            Log.i(TAG, "try doPlay...")
            target.play().getAsync(ErrorResultHandler("doPlay", "Error Playing"))
        }
    }

    private fun doPause() {
        val target = getRemoteMediaPlayerFromUUID(targetUuid)
        if (target != null) {
            Log.i(TAG, "try doPause...")
            target.pause().getAsync(ErrorResultHandler("doPause", "Error Pausing"))
        }
    }

    private fun doStop() {
        val target = getRemoteMediaPlayerFromUUID(targetUuid)
        if (target != null) {
            Log.i(TAG, "try doStop...")
            target.stop().getAsync(ErrorResultHandler("doStop", "Error Stopping"))
        }
    }

    private fun doSeek(position: String) {
        val target = getRemoteMediaPlayerFromUUID(targetUuid)
        if (target != null) {
            Log.i(TAG, "try doSeek...")
            val positionLong = position.toLong()
            target.seek(CustomMediaPlayer.PlayerSeekMode.Absolute, positionLong)
                .getAsync(ErrorResultHandler("doStop", "Error Stopping"))
        }
    }

    private class ErrorResultHandler @JvmOverloads internal constructor(
        private val mCommand: String,
        private val mMsg: String,
        private val mExtend: Boolean = false
    ) :
        RemoteMediaPlayer.FutureListener<Void?> {
        override fun futureIsNow(result: Future<Void?>) {
            try {
                result.get()
                //                showToast(mCommand);
//                mErrorCount = 0;
                Log.i("TAG", "$mCommand: successful")
            } catch (e: ExecutionException) {
//                handleFailure(e.getCause(), mMsg, mExtend);
            } catch (e: Exception) {
//                handleFailure(e, mMsg, mExtend);
            }
        }
    }
}