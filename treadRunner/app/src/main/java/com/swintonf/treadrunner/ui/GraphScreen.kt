package com.swintonf.treadrunner.ui

import android.content.Context
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.polar.androidcommunications.api.ble.model.DisInfo
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarHealthThermometerData
import com.polar.sdk.api.model.PolarHrBroadcastData
import com.polar.sdk.impl.BDBleApiImpl
import kotlin.properties.Delegates

@Composable
fun GraphScreen(graphViewModel: GraphViewModel = viewModel(), context: Context)
{
    val graphUiState by graphViewModel.uiState.collectAsState()
    DrawCanvas(graphUiState.graphPoints)
    ButtonLayout(context, graphUiState.graphPoints)
}

var graphHeightStartPos by mutableStateOf(0f)
var graphWidthStartPos by mutableStateOf(0f)

var graphHeightSize by mutableStateOf(0f)
var graphWidthSize by mutableStateOf(0f)

var graphHeightStep by mutableStateOf(0f)
var graphWidthStep by mutableStateOf(0f)

@Composable
fun DrawCanvas(
    graphPoints : MutableList<Offset>
) {
    val textMeasurer = rememberTextMeasurer()

    var graphHeightStart by remember {mutableStateOf(0f)}
    var graphWidthStart by remember {mutableStateOf(0f)}

    Canvas(modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        graphHeightStart = 0.95f
        val graphHeightEnd = 0.4f
        graphWidthStart = 0.05f


        var hrGraphText = 200
        val timeSecText = mutableIntStateOf(120)

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

        graphHeightStartPos = (canvasHeight - timeMeasuredText.size.height)
        val graphHeightEndPos = (canvasHeight - (canvasHeight * (1-graphHeightEnd)))
        graphWidthStartPos = (HrMeasuredText.size.height * 1f)
        val graphWidthEndPos = (canvasWidth - HrMeasuredText.size.height)

        graphHeightSize = graphHeightStartPos - graphHeightEndPos
        graphWidthSize = graphWidthEndPos - graphWidthStartPos

        var lineHeight = graphHeightEndPos

        val graph0PosOffset = Offset(graphWidthStartPos,graphHeightStartPos)

        var textWidth = graphWidthStartPos + (graphWidthSize / 5)

        val timeTextOffset = Offset(0f, canvasHeight - timeMeasuredText.size.height)
        drawText(timeMeasuredText, topLeft = timeTextOffset)

        graphHeightStep = graphHeightSize / 200
        graphWidthStep = graphWidthSize / 600


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
            drawText(HrMeasuredText, topLeft = Offset(0f, canvasHeight - timeMeasuredText.size.height))
        }


        drawPoints(
            pointMode = PointMode.Polygon,
            color = Color(0xFF7E0D3B),
            strokeWidth = 10f,
            cap = StrokeCap.Round,
            points = graphPoints
        )

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

fun PolarConnect(context: Context, graphPoints: MutableList<Offset>){
    val context = context
    var DeviceId = "deviceId"

    val api by lazy { PolarBleApiDefaultImpl.defaultImplementation(context,
        setOf(PolarBleApi.PolarBleSdkFeature.FEATURE_HR))}

    api.autoConnectToDevice(-50, null, null).subscribe()

    api.setApiCallback(object : PolarBleApiCallback() {
        override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
            DeviceId = polarDeviceInfo.deviceId
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

    startHrStream(api, DeviceId, graphPoints)

}

var HeartR = mutableStateOf(0)


fun startHrStream(api: BDBleApiImpl, deviceID : String, graphPoints: MutableList<Offset>) {
    var widthStep = graphWidthStartPos
    graphPoints.removeAt(0)

    var heartr : Int by Delegates.observable(0) { prop, old, new ->
        HeartR.value = new
        graphPoints.add(Offset(widthStep,graphHeightStartPos - (new * graphHeightStep)))
        widthStep=widthStep+(graphWidthStep)
    }
    api.startHrStreaming(deviceID)
    api.startListenForPolarHrBroadcasts(null).subscribe{
            polarBroadcastData: PolarHrBroadcastData ->
        heartr = polarBroadcastData.hr
    }


}



@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ButtonLayout(context: Context, graphPoints: MutableList<Offset>) {
    val windowHeight = currentWindowDpSize().height

    Column(Modifier.height(height = windowHeight / 3).fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val heartR by rememberSaveable { HeartR }
        Text("$heartR")
        PermissionsButton { PolarConnect(context, graphPoints) }
    }
}

@Composable
fun PermissionsButton(onClick: () -> Unit) {
    Button(onClick = {onClick()}) {
        Text("Start")
    }
}