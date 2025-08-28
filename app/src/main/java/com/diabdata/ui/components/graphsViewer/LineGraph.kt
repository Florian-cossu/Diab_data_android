package com.diabdata.ui.components.graphsViewer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.diabdata.R
import com.diabdata.models.AddableType
import com.diabdata.models.PlotPoint
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.dashed
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.common.component.shapeComponent
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun LineGraph(
    points: List<PlotPoint>,
    modifier: Modifier = Modifier,
    label: String,
    primaryColor: Int = MaterialTheme.colorScheme.primary.toArgb(),
    showTrendLine: Boolean = false
) {
    val hsv = FloatArray(3)
    ColorUtils.colorToHSL(primaryColor, hsv)

    hsv[0] = (hsv[0] + 80f) % 360f
    hsv[1] *= 0.8f

    val adjusted = ColorUtils.HSLToColor(hsv)
    val trendCurveColor = Color(adjusted).copy(alpha = 0.8f).toArgb()

    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.homescreen_no_data_text))
        }
        return
    }

    val dataFormatter: CartesianValueFormatter = when (label) {
        stringResource(AddableType.WEIGHT.displayNameRes) -> CartesianValueFormatter { _, value, _ ->
            "${NumberFormat.getNumberInstance(Locale.getDefault()).format(value)} kg"
        }

        stringResource(AddableType.HBA1C.displayNameRes) -> CartesianValueFormatter { _, value, _ ->
            "${NumberFormat.getNumberInstance(Locale.getDefault()).format(value)} %"
        }

        else -> CartesianValueFormatter.decimal(DecimalFormat("#.##"))
    }


    ProvideVicoTheme(rememberM3VicoTheme()) {
        val modelProducer = remember { CartesianChartModelProducer() }

        val dateFormatter = remember {
            DateTimeFormatter.ofPattern("dd/MM/yy", Locale.getDefault())
        }

        val xValues = points.mapIndexed { index, _ -> index.toDouble() }
        val yValues = points.map { it.value.toDouble() }

        LaunchedEffect(points) {
            modelProducer.runTransaction {
                lineSeries {
                    series(x = xValues, y = yValues)
                    if (showTrendLine && points.size > 1) {
                        val regression = linearRegression(xValues, yValues)
                        val trendY = xValues.map { regression(it) }
                        series(x = xValues, y = trendY)
                    }
                }
            }
        }


        val zoomState = rememberVicoZoomState(
            zoomEnabled = true,
            initialZoom = Zoom.x(-1.0)
        )
        val scrollState = rememberVicoScrollState(scrollEnabled = true)

        val lines = mutableListOf(
            LineCartesianLayer.Line(
                fill = LineCartesianLayer.LineFill.single(Fill(primaryColor)),
                areaFill = LineCartesianLayer.AreaFill.single(
                    Fill(
                        ShaderProvider.verticalGradient(
                            ColorUtils.setAlphaComponent(primaryColor, 60),
                            Color.Transparent.toArgb()
                        )
                    )
                ),
                pointConnector = LineCartesianLayer.PointConnector.cubic(0.5f),
                pointProvider = LineCartesianLayer.PointProvider.single(
                    LineCartesianLayer.Point(
                        component = shapeComponent(
                            shape = CorneredShape.Pill,
                            fill = Fill(primaryColor)
                        ),
                        sizeDp = 8f,
                    )
                )
            )
        )

        if (showTrendLine) {
            lines += LineCartesianLayer.Line(
                fill = LineCartesianLayer.LineFill.single(
                    Fill(trendCurveColor)
                ),
                areaFill = null,
                pointProvider = null,
                pointConnector = LineCartesianLayer.PointConnector.Sharp,
                stroke = LineCartesianLayer.LineStroke.dashed(
                    cap = StrokeCap.Butt,
                    dashLength = 12.dp,
                    gapLength = 5.dp
                )
            )
        }

        val layer = rememberLineCartesianLayer(
            verticalAxisPosition = Axis.Position.Vertical.Start,
            lineProvider = LineCartesianLayer.LineProvider.series(*lines.toTypedArray())
        )

        Column {
            Text(
                text = label.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
                color = MaterialTheme.colorScheme.surfaceTint
            )
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                tonalElevation = 4.dp,
            ) {

                CartesianChartHost(
                    chart = rememberCartesianChart(
                        layer,
                        startAxis = VerticalAxis.rememberStart(
                            valueFormatter = dataFormatter,
                        ),
                        bottomAxis = HorizontalAxis.rememberBottom(
                            valueFormatter = CartesianValueFormatter { _, x, _ ->
                                points.getOrNull(x.toInt())?.date?.format(dateFormatter) ?: ""
                            },
                            itemPlacer = HorizontalAxis.ItemPlacer.aligned()
                        ),
                    ),
                    modelProducer = modelProducer,
                    zoomState = zoomState,
                    scrollState = scrollState,
                    modifier = modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(start = 16.dp, top = 20.dp, bottom = 20.dp, end = 30.dp)
                )
            }
        }
    }
}