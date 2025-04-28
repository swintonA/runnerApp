package com.swintonf.treadrunner

import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.swintonf.treadrunner.ui.theme.TreadRunnerTheme
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.polar.androidcommunications.api.ble.model.DisInfo
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarHealthThermometerData
import com.polar.sdk.api.model.PolarHrBroadcastData

class MainActivity : ComponentActivity() {



    var DeviceId = "deviceID"
    val HeartR = mutableStateOf(0)
    var DeviceConnected : Boolean = false


    val api: PolarBleApi by lazy { PolarBleApiDefaultImpl.defaultImplementation(applicationContext,
        setOf(PolarBleApi.PolarBleSdkFeature.FEATURE_HR))}

    fun startHrStream() {
        api.startHrStreaming(DeviceId)
        api.startListenForPolarHrBroadcasts(null).subscribe{
                polarBroadcastData: PolarHrBroadcastData ->
            HeartR.value = polarBroadcastData.hr
        }
    }

    fun PolarConnect() {
        api.autoConnectToDevice(-50, null, null).subscribe()
        startHrStream()
    }


    @Composable
    fun ButtonLayout() {
        Column(Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            val heartR by HeartR
            Text("$heartR")
            PermissionsButton { PolarConnect() }
        }
    }

    @Composable
    fun PermissionsButton(onClick: () -> Unit) {
        Button(onClick = {onClick()}) {
            Text("Start")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TreadRunnerTheme {
                ButtonLayout()
            }
        }

        api.setApiCallback(object : PolarBleApiCallback() {
            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                DeviceId = polarDeviceInfo.deviceId
                DeviceConnected = true
            }

            override fun disInformationReceived(
                identifier: String,
                disInfo: DisInfo
            ) {
            }

            override fun htsNotificationReceived(
                identifier: String,
                data: PolarHealthThermometerData
            ) {
            }

        })
    }
}

