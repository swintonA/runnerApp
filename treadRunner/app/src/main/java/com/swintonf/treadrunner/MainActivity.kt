package com.swintonf.treadrunner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.toSize
import com.polar.androidcommunications.api.ble.model.DisInfo
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarHealthThermometerData
import com.polar.sdk.api.model.PolarHrBroadcastData
import com.swintonf.treadrunner.ui.theme.TreadRunnerTheme
import kotlin.properties.Delegates


class MainActivity : ComponentActivity() {

    var DeviceId = "deviceID"
    val HeartR = mutableStateOf(0)
    var screenWidth = mutableStateOf(0f)
    var screenHeight = mutableStateOf(0f)
    var pointStartWidth = 0f
    var pointStartHeight = 0f
    val graphPoints = mutableStateListOf(Offset(0f,0f))
    var step = 0f
    var graphHeightStep = 0f
    var DeviceConnected : Boolean = false
    var isHrStreaming : Boolean = false

    var Heartr : Int by Delegates.observable(0) { prop, old, new ->
        HeartR.value = Heartr
        graphPoints.add(Offset(step,pointStartHeight + (Heartr * graphHeightStep)))
        step=step+10f
    }

    val api: PolarBleApi by lazy { PolarBleApiDefaultImpl.defaultImplementation(applicationContext,
        setOf(PolarBleApi.PolarBleSdkFeature.FEATURE_HR))}

    fun startHrStream() {
        api.startHrStreaming(DeviceId)
        api.startListenForPolarHrBroadcasts(null).subscribe{
                polarBroadcastData: PolarHrBroadcastData ->
            Heartr = polarBroadcastData.hr
        }
    }

    fun PolarConnect() {
        pointStartWidth = screenWidth.value * 0.1f
        pointStartHeight = screenHeight.value * 0.1f
        graphPoints.set(0,Offset(pointStartWidth,pointStartHeight))
        step = pointStartWidth
        graphHeightStep = (screenHeight.value * 0.35f) / 200
        api.autoConnectToDevice(-50, null, null).subscribe()
        startHrStream()
        isHrStreaming = true
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


    @Composable
    fun DrawCanvas() {
        val textMeasurer = rememberTextMeasurer()
        Canvas(modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars)
        ) {

            val canvasWidth = size.width
            val canvasHeight = size.height

            val graphHeightStart = 0.95f
            val graphHeightEnd = 0.4f
            val graphWidthStart = 0.05f
            val graphWidthEnd = 0.05f

            val graphHeight = graphHeightStart - graphHeightEnd
            val graphWidth = graphHeightStart - graphWidthEnd

            var lineHeight = graphHeightEnd
            var timeText = 200

            screenWidth.value = canvasWidth * 1f
            screenHeight.value = canvasHeight * 1f

            drawRect(
                color = Color(0xFFA9A9A9),
                size = size
            )

            val timeMeasuredText =
                textMeasurer.measure(
                    AnnotatedString("Time",
                        (ParagraphStyle(textAlign = TextAlign.Center))
                        ),
                    constraints = Constraints.fixedWidth((canvasWidth * graphWidth).toInt()),
                )

            drawRect(color = Color.Transparent, size = timeMeasuredText.size.toSize(),
                topLeft = Offset(canvasWidth * graphWidthStart , canvasHeight * (graphHeightStart * 1.001f)))
            drawText(timeMeasuredText, topLeft = Offset(canvasWidth * graphWidthStart , canvasHeight * (graphHeightStart * 1.001f)))


            val HrMeasuredText =
                textMeasurer.measure(
                    AnnotatedString("Heart Rate",
                        (ParagraphStyle(textAlign = TextAlign.Center))
                    ),
                    constraints = Constraints.fixedWidth((canvasHeight * graphHeight).toInt()),
                )


            val hrBox = HrMeasuredText.size.height

            rotate(degrees = -90F, pivot= Offset((canvasWidth * graphWidthStart) - hrBox , canvasHeight * graphHeightStart)) {
                drawRect(color = Color.Transparent, size = HrMeasuredText.size.toSize(), topLeft = Offset((canvasWidth * graphWidthStart) - hrBox , canvasHeight * graphHeightStart))
                drawText(HrMeasuredText, topLeft = Offset((canvasWidth * graphWidthStart) - hrBox , canvasHeight * graphHeightStart))
            }

            scale(scaleX = 1f, scaleY= -1f){
                drawPoints(
                    pointMode = PointMode.Polygon,
                    color = Color(0xFF7E0D3B),
                    strokeWidth = 10f,
                    cap = StrokeCap.Square,
                    points = graphPoints
                )}

            for ( i in 1..5) {
                drawLine(
                    brush = SolidColor(Color(0xFFB9B9B9)),
                    start = Offset(canvasWidth * graphWidthStart, canvasHeight * lineHeight),
                    end = Offset(canvasWidth * graphHeightStart,canvasHeight * lineHeight),
                    strokeWidth = 5f,
                )
                drawText(textMeasurer, "$timeText", topLeft = Offset(canvasWidth * graphWidthStart, canvasHeight * lineHeight))
                lineHeight = lineHeight + (graphHeight / 5)
                timeText = timeText - 40
            }

            drawLine(
                brush = SolidColor(Color(0xFF5B5B5B)),
                start = Offset(canvasWidth * graphWidthStart,canvasHeight * graphHeightStart),
                end = Offset(canvasWidth * graphWidthEnd,canvasHeight * graphHeightEnd),
                strokeWidth = 10f,
            )

            drawLine(
                brush = SolidColor(Color(0xFF000000)),
                start = Offset(canvasWidth * graphWidthStart,canvasHeight * graphHeightStart),
                end = Offset(canvasWidth * graphHeightStart,canvasHeight * graphHeightStart),
                strokeWidth = 10f,
            )
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TreadRunnerTheme {
                DrawCanvas()
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



