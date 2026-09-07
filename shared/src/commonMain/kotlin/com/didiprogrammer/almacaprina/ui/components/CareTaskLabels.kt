package com.didiprogrammer.almacaprina.ui.components

import com.didiprogrammer.almacaprina.domain.model.CareTaskAnimalGroup
import com.didiprogrammer.almacaprina.domain.model.CareTaskFrequency
import com.didiprogrammer.almacaprina.domain.model.CareTaskType
import com.didiprogrammer.almacaprina.domain.model.TimeOfDay

fun CareTaskType.label(): String = when (this) {
    CareTaskType.MILKING -> "Ordeño"
    CareTaskType.FEEDING -> "Alimentación"
    CareTaskType.MEDICATION -> "Medicamento"
    CareTaskType.WEIGHING -> "Pesada"
    CareTaskType.OTHER -> "Otro"
}

fun CareTaskFrequency.label(): String = when (this) {
    CareTaskFrequency.DAILY -> "Diaria"
    CareTaskFrequency.SPECIFIC_DAYS -> "Días específicos"
    CareTaskFrequency.WEEKLY -> "Semanal"
    CareTaskFrequency.ONE_TIME -> "Una vez"
}

fun CareTaskAnimalGroup.label(): String = when (this) {
    CareTaskAnimalGroup.LACTATING -> "En producción"
    CareTaskAnimalGroup.PREGNANT -> "Gestantes"
    CareTaskAnimalGroup.YOUNG_DOES -> "Cabretonas"
    CareTaskAnimalGroup.DRY -> "Secas"
    CareTaskAnimalGroup.BREEDING_BUCKS -> "Sementales"
    CareTaskAnimalGroup.GENERAL -> "General"
    CareTaskAnimalGroup.ALL -> "Todas"
}

fun TimeOfDay.label(): String = when (this) {
    TimeOfDay.MORNING -> "Mañana"
    TimeOfDay.AFTERNOON -> "Tarde"
    TimeOfDay.BOTH -> "Ambos"
    TimeOfDay.ANY -> "Cualquiera"
}
