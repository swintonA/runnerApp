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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowDpSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
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
    val screenWidth = mutableStateOf(0f)
    val screenHeight = mutableStateOf(0f)

    val graphWidthStart = mutableStateOf(0f)
    val graphHeightStart = mutableStateOf(0f)
    val graphHeight = mutableStateOf(0f)
    val graphWidth = mutableStateOf(0f)

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
        step=step+((screenWidth.value * graphWidth.value) / 600)
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
        pointStartWidth = screenWidth.value * graphWidthStart.value
        pointStartHeight = screenHeight.value * (1-graphHeightStart.value)
        graphPoints[0] = Offset(pointStartWidth,pointStartHeight)
        step = pointStartWidth
        graphHeightStep = (screenHeight.value * graphHeight.value) / 200
        api.autoConnectToDevice(-50, null, null).subscribe()
        startHrStream()
        isHrStreaming = true
    }

    @OptIn(ExperimentalMaterial3AdaptiveApi::class)
    @Composable
    fun ButtonLayout() {

        val windowHeight = currentWindowDpSize().height

        Column(Modifier.height(height = windowHeight / 3).fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
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

            graphHeightStart.value = 0.95f
            val graphHeightEnd = 0.4f
            graphWidthStart.value = 0.05f
            val graphWidthEnd = 0.05f

            graphHeight.value = graphHeightStart.value - graphHeightEnd
            graphWidth.value = graphHeightStart.value - graphWidthEnd




            var hrGraphText = 200
            val timeSecText = mutableIntStateOf(120)

            screenWidth.value = canvasWidth * 1f
            screenHeight.value = canvasHeight * 1f

            drawRect(
                color = Color(0xFFA9A9A9),
                size = size
            )

            val timeMeasuredText =
                textMeasurer.measure(
                    AnnotatedString("Time",
                        (ParagraphStyle(textAlign = TextAlign.Center)),
                        ),
                    constraints = Constraints.fixedWidth((canvasWidth).toInt()),
                    style = TextStyle(fontSize = 15.sp)
                )

            val HrMeasuredText =
                textMeasurer.measure(
                    AnnotatedString("Heart Rate",
                        (ParagraphStyle(textAlign = TextAlign.Center))
                    ),
                    constraints = Constraints.fixedWidth(((canvasHeight * 0.6)-timeMeasuredText.size.height).toInt()),
                    style = TextStyle(fontSize = 15.sp)
                )

            val graphHeightStartPos = (canvasHeight - timeMeasuredText.size.height)
            val graphHeightEndPos = (canvasHeight - (canvasHeight * (1-graphHeightEnd)))
            val graphWidthStartPos = (HrMeasuredText.size.height * 1f)
            val graphWidthEndPos = (canvasWidth - HrMeasuredText.size.height)

            val graphHeightSize = graphHeightStartPos - graphHeightEndPos
            val graphWidthSize = graphWidthEndPos - graphWidthStartPos

            var lineHeight = graphHeightEndPos

            val graph0PosOffset = Offset(graphWidthStartPos,graphHeightStartPos)

            var textWidth = graphWidthStartPos + (graphWidthSize / 5)



            val timeTextOffset = Offset(0f, canvasHeight - timeMeasuredText.size.height)
//            drawRect(color = Color.Transparent, size = timeMeasuredText.size.toSize(), topLeft = timeTextOffset)
            drawText(timeMeasuredText, topLeft = timeTextOffset)


            for (i in 1..5) {
                val TimeIncrementMeasuredText =
                textMeasurer.measure(
                    AnnotatedString("${timeSecText.intValue / 60}m"
                    ),
                    style = TextStyle(fontSize = 15.sp)
                )

                drawText(TimeIncrementMeasuredText, topLeft = Offset(textWidth - (TimeIncrementMeasuredText.size.width/2), graphHeightStartPos-timeMeasuredText.size.height))
                textWidth = textWidth + (graphWidthSize / 5)
                timeSecText.intValue = timeSecText.intValue + 120
            }

            rotate(degrees = -90F, pivot = Offset(0f, canvasHeight - timeMeasuredText.size.height)) {
//                drawRect(color = Color.Transparent, size = HrMeasuredText.size.toSize(), topLeft = Offset(0f, canvasHeight - timeMeasuredText.size.height))
                drawText(HrMeasuredText, topLeft = Offset(0f, canvasHeight - timeMeasuredText.size.height))
            }

            scale(scaleX = 1f, scaleY= -1f){
                drawPoints(
                    pointMode = PointMode.Polygon,
                    color = Color(0xFF7E0D3B),
                    strokeWidth = 10f,
                    cap = StrokeCap.Round,
                    points = graphPoints
                )}

            for ( i in 1..5) {
                drawLine(
                    brush = SolidColor(Color(0xFFB9B9B9)),
                    start = Offset(graphWidthStartPos, lineHeight),
                    end = Offset(graphWidthEndPos,lineHeight),
                    strokeWidth = 5f,
                )
                drawText(textMeasurer, "$hrGraphText", topLeft = Offset(graphWidthStartPos + 5, lineHeight))
                lineHeight = lineHeight + (graphHeightSize / 5)
                hrGraphText = hrGraphText - 40
            }


            drawLine(
                brush = SolidColor(Color(0xFF000000)),
                start = graph0PosOffset,
                end = Offset(graphWidthStartPos,canvasHeight * graphHeightEnd),
                strokeWidth = 10f,
            )

            drawLine(
                brush = SolidColor(Color(0xFF000000)),
                start = graph0PosOffset,
                end = Offset(canvasWidth - HrMeasuredText.size.height ,graphHeightStartPos),
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



