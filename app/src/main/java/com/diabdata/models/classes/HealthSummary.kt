package com.diabdata.models.classes

data class HealthSummary(
    val weightData: List<PlotPoint>,
    val hba1cData: List<PlotPoint>,
    val appointments: List<AppointmentSummary>
)