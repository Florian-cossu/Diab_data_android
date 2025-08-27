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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import com.diabdata.R
import com.diabdata.models.PlotPoint
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.ProvideVicoTheme
import com.patrykandpatrick.vico.compose.m3.common.rememberM3VicoTheme
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun LineGraph(
    points: List<PlotPoint>,
    modifier: Modifier = Modifier,
    label: String,
) {
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
                }
            }
        }

        val zoomState = rememberVicoZoomState(false)
        val scrollState = rememberVicoScrollState(scrollEnabled = false)

        val primaryColor = MaterialTheme.colorScheme.primary.toArgb()

        val layer = rememberLineCartesianLayer(
            verticalAxisPosition = Axis.Position.Vertical.Start,
            lineProvider = LineCartesianLayer.LineProvider.series(
                LineCartesianLayer.Line(
                    fill = LineCartesianLayer.LineFill.single(
                        fill = Fill(primaryColor)
                    ),
                    areaFill = LineCartesianLayer.AreaFill.single(
                        Fill(
                            ShaderProvider.verticalGradient(
                                ColorUtils.setAlphaComponent(
                                    primaryColor,
                                    90
                                ), Color.Transparent.toArgb()
                            )
                        )
                    ),
                    pointConnector = LineCartesianLayer.PointConnector.cubic(0.5f)
                )
            )
        )

        Column {
            Text(
                text = label.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
                color = MaterialTheme.colorScheme.surfaceTint
            )
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 4.dp,
            ) {

                CartesianChartHost(
                    chart = rememberCartesianChart(
                        layer,
                        startAxis = VerticalAxis.rememberStart(
                            valueFormatter = CartesianValueFormatter.decimal(DecimalFormat("#.##")),
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
                        .padding(start = 12.dp, top = 16.dp, bottom = 16.dp, end = 26.dp)
                )
            }
        }
    }
}