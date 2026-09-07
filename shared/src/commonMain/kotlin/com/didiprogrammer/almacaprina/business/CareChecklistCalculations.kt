package com.didiprogrammer.almacaprina.business

import com.didiprogrammer.almacaprina.domain.model.CareTask
import com.didiprogrammer.almacaprina.domain.model.CareTaskFrequency
import com.didiprogrammer.almacaprina.domain.model.CareTaskLog
import com.didiprogrammer.almacaprina.domain.model.CareTaskType
import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.GoatStatus
import com.didiprogrammer.almacaprina.domain.model.HealthRecord
import com.didiprogrammer.almacaprina.domain.model.MilkProductionRecord
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

/** DailyCareChecklist — ver CLAUDE.md § Calendario de tareas y docs/data_model.md § 20. */

data class ChecklistCareTaskItem(val careTask: CareTask, val completed: Boolean, val todayLog: CareTaskLog?)

data class ChecklistHealthReminder(val healthRecord: HealthRecord, val goat: Goat?, val completed: Boolean)

data class MilkingSessionProgress(val registeredCount: Int, val totalCount: Int) {
    val isComplete: Boolean get() = totalCount > 0 && registeredCount >= totalCount
}

data class DailyCareChecklist(
    val totalCount: Int,
    val completedCount: Int,
    val milkingTask: ChecklistCareTaskItem?,
    val milkingProgress: MilkingSessionProgress,
    val otherCareTasks: List<ChecklistCareTaskItem>,
    val healthReminders: List<ChecklistHealthReminder>
)

/**
 * CareTask activos "aplicables hoy", cruzados con su CareTaskLog de hoy (si existe).
 *
 * `specific_days` no tiene todavía un campo de días de la semana en el modelo — hasta que se
 * agregue, se trata igual que `daily` (aparece todos los días). Es una simplificación
 * explícita, no un olvido — hay que confirmar con el dueño si `specific_days` necesita ese
 * campo nuevo.
 */
fun dailyCareTaskItems(activeCareTasks: List<CareTask>, careTaskLogs: List<CareTaskLog>, today: LocalDate): List<ChecklistCareTaskItem> =
    activeCareTasks.filter { it.active }.mapNotNull { task ->
        val logsForTask = careTaskLogs.filter { it.careTaskId == task.id }
        val todayLog = logsForTask.firstOrNull { it.date == today }
        val lastCompletedLog = logsForTask.filter { it.completed }.maxByOrNull { it.date }

        val applicableToday = when (task.frequency) {
            CareTaskFrequency.DAILY, CareTaskFrequency.SPECIFIC_DAYS -> true
            CareTaskFrequency.WEEKLY ->
                lastCompletedLog == null || lastCompletedLog.date == today || lastCompletedLog.date.daysUntil(today) >= 7
            CareTaskFrequency.ONE_TIME -> lastCompletedLog == null || lastCompletedLog.date == today
        }
        if (!applicableToday) return@mapNotNull null

        ChecklistCareTaskItem(careTask = task, completed = todayLog?.completed == true, todayLog = todayLog)
    }

/**
 * HealthRecord individuales (goat_id no nulo) con next_suggested_date ≤ hoy — ver regla de
 * resolución en docs/data_model.md § 5. HealthRecord. Un HealthRecord posterior (misma
 * goat_id, mismo type o mismo insumo_id) es la señal de que ya se atendió:
 * - Si el registro que resuelve es de una fecha anterior a hoy, el recordatorio ya no aplica.
 * - Si es de HOY, se muestra igual pero marcado como completado (ver mockup de checklist).
 *
 * Los HealthRecord de GRUPO (goat_id nulo, generados al completar una CareTask de tipo
 * medication) no participan de este mecanismo — su ejecución ya la controla CareTask/CareTaskLog.
 */
fun dailyHealthReminders(healthRecords: List<HealthRecord>, goatsById: Map<String, Goat>, today: LocalDate): List<ChecklistHealthReminder> =
    healthRecords
        .filter { hr -> hr.goatId != null && hr.nextSuggestedDate?.let { it <= today } == true }
        .mapNotNull { candidate ->
            val resolvingRecord = healthRecords.firstOrNull { other ->
                other.id != candidate.id &&
                    other.goatId == candidate.goatId &&
                    (other.type == candidate.type || (candidate.insumoId != null && other.insumoId == candidate.insumoId)) &&
                    other.date > candidate.date
            }
            val goat = candidate.goatId?.let { goatsById[it] }
            when {
                resolvingRecord == null -> ChecklistHealthReminder(candidate, goat, completed = false)
                resolvingRecord.date == today -> ChecklistHealthReminder(candidate, goat, completed = true)
                else -> null
            }
        }

/**
 * Progreso de la sesión de ordeño de hoy — cuántas cabras en producción ya tienen su
 * MilkProductionRecord de hoy. `milking` es solo un recordatorio (ver CLAUDE.md): no genera
 * su propio registro, así que su estado de "completado" sale de esto, nunca de un CareTaskLog.
 */
fun milkingSessionProgress(activeGoats: List<Goat>, todayMilkRecords: List<MilkProductionRecord>): MilkingSessionProgress {
    val eligible = activeGoats.filter { it.exitDate == null && it.currentStatus == GoatStatus.IN_PRODUCTION }
    val registeredGoatIds = todayMilkRecords.map { it.goatId }.toSet()
    return MilkingSessionProgress(
        registeredCount = eligible.count { it.id in registeredGoatIds },
        totalCount = eligible.size
    )
}

/** Arma el DailyCareChecklist completo — unión de tareas de grupo y recordatorios de salud individuales. */
fun buildDailyCareChecklist(
    activeCareTasks: List<CareTask>,
    careTaskLogs: List<CareTaskLog>,
    healthRecords: List<HealthRecord>,
    goatsById: Map<String, Goat>,
    activeGoats: List<Goat>,
    todayMilkRecords: List<MilkProductionRecord>,
    today: LocalDate
): DailyCareChecklist {
    val careItems = dailyCareTaskItems(activeCareTasks, careTaskLogs, today)
    val milkingItem = careItems.firstOrNull { it.careTask.taskType == CareTaskType.MILKING }
    val otherItems = careItems.filterNot { it.careTask.taskType == CareTaskType.MILKING }
    val healthItems = dailyHealthReminders(healthRecords, goatsById, today)
    val milkingProgress = milkingSessionProgress(activeGoats, todayMilkRecords)
    val milkingCompleted = milkingItem != null && milkingProgress.isComplete

    return DailyCareChecklist(
        totalCount = (if (milkingItem != null) 1 else 0) + otherItems.size + healthItems.size,
        completedCount = (if (milkingCompleted) 1 else 0) + otherItems.count { it.completed } + healthItems.count { it.completed },
        milkingTask = milkingItem,
        milkingProgress = milkingProgress,
        otherCareTasks = otherItems,
        healthReminders = healthItems
    )
}
