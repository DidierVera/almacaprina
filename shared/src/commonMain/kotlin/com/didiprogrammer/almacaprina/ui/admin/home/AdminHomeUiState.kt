package com.didiprogrammer.almacaprina.ui.admin.home

/** Período seleccionable para el resumen financiero de Inicio. */
enum class FinancialPeriod(val label: String) {
    TODAY("Hoy"),
    WEEK("Semana"),
    MONTH("Mes")
}

/** Tipo de alerta — determina el ícono/color en la lista de Alertas. */
enum class HomeAlertType {
    VACCINE,
    WEIGHING,
    BIRTH,
    INSUMO
}

data class HomeAlert(
    val id: String,
    val type: HomeAlertType,
    val message: String
)

data class HerdStatusCounts(
    val inProduction: Int = 0,
    val pregnant: Int = 0,
    val dry: Int = 0,
    val youngDoes: Int = 0
)

data class AdminHomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val farmName: String = "",
    val currency: String = "COP",
    // Producción vs. meta
    val todayLiters: Double = 0.0,
    val targetLiters: Double = 0.0,
    val availableLiters: Double = 0.0,
    // Estado del hato
    val herdCounts: HerdStatusCounts = HerdStatusCounts(),
    // Alertas
    val alerts: List<HomeAlert> = emptyList(),
    // Resumen financiero
    val financialPeriod: FinancialPeriod = FinancialPeriod.TODAY,
    val revenue: Double = 0.0,
    val pendingReceivable: Double = 0.0,
    val costPerLiter: Double? = null
) {
    val progressFraction: Float
        get() = if (targetLiters <= 0.0) 0f else (todayLiters / targetLiters).toFloat().coerceIn(0f, 1f)
}
