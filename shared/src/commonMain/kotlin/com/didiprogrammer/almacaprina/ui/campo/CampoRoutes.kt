package com.didiprogrammer.almacaprina.ui.campo

/** Rutas del módulo Campo — ver tabla de convención de rutas en CLAUDE.md. */
object CampoRoutes {
    const val CHECKLIST = "campo/checklist"

    const val ORDENO_SESION = "campo/ordeno"
    const val ORDENO_REGISTRO_PATTERN = "campo/ordeno/registro/{goatId}"
    const val ORDENO_RESUMEN = "campo/ordeno/resumen"

    /** No estaba en la tabla original — punto de entrada natural para el recordatorio de
     * pesada del checklist, listando las cabras con pesada vencida (reutiliza `overdueWeighings`). */
    const val PESADA_LISTA = "campo/pesada"
    const val PESADA_PATTERN = "campo/pesada/{goatId}"

    const val NOVEDAD = "campo/novedad"

    const val CABRAS_LISTA = "campo/cabras"
    const val CABRAS_DETALLE_PATTERN = "campo/cabras/{goatId}"

    const val ALERTAS = "campo/alertas"

    fun ordenoRegistro(goatId: String): String = "campo/ordeno/registro/$goatId"
    fun pesada(goatId: String): String = "campo/pesada/$goatId"
    fun cabraDetalle(goatId: String): String = "campo/cabras/$goatId"
}
