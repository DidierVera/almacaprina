package com.didiprogrammer.almacaprina.ui.admin.calendario

/** Rutas de la sub-navegación de Calendario de tareas, colgadas del NavHost de AdminRootScreen. */
object CareTaskRoutes {
    const val LIST = "admin/mas/calendario"
    const val NEW_TASK = "admin/mas/calendario/nueva"
    const val EDIT_TASK_PATTERN = "admin/mas/calendario/editar/{taskId}"

    fun editTask(taskId: String): String = "admin/mas/calendario/editar/$taskId"
}
