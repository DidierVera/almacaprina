package com.didiprogrammer.almacaprina.ui.components

import almacaprina.shared.generated.resources.Res
import almacaprina.shared.generated.resources.care_task_animal_group_all
import almacaprina.shared.generated.resources.care_task_animal_group_breeding_bucks
import almacaprina.shared.generated.resources.care_task_animal_group_dry
import almacaprina.shared.generated.resources.care_task_animal_group_general
import almacaprina.shared.generated.resources.care_task_animal_group_lactating
import almacaprina.shared.generated.resources.care_task_animal_group_pregnant
import almacaprina.shared.generated.resources.care_task_animal_group_young_does
import almacaprina.shared.generated.resources.care_task_frequency_daily
import almacaprina.shared.generated.resources.care_task_frequency_one_time
import almacaprina.shared.generated.resources.care_task_frequency_specific_days
import almacaprina.shared.generated.resources.care_task_frequency_weekly
import almacaprina.shared.generated.resources.care_task_type_feeding
import almacaprina.shared.generated.resources.care_task_type_medication
import almacaprina.shared.generated.resources.care_task_type_milking
import almacaprina.shared.generated.resources.care_task_type_other
import almacaprina.shared.generated.resources.care_task_type_weighing
import almacaprina.shared.generated.resources.time_of_day_afternoon
import almacaprina.shared.generated.resources.time_of_day_any
import almacaprina.shared.generated.resources.time_of_day_both
import almacaprina.shared.generated.resources.time_of_day_morning
import androidx.compose.runtime.Composable
import com.didiprogrammer.almacaprina.domain.model.CareTaskAnimalGroup
import com.didiprogrammer.almacaprina.domain.model.CareTaskFrequency
import com.didiprogrammer.almacaprina.domain.model.CareTaskType
import com.didiprogrammer.almacaprina.domain.model.TimeOfDay
import org.jetbrains.compose.resources.stringResource

@Composable
fun CareTaskType.label(): String = stringResource(
    when (this) {
        CareTaskType.MILKING -> Res.string.care_task_type_milking
        CareTaskType.FEEDING -> Res.string.care_task_type_feeding
        CareTaskType.MEDICATION -> Res.string.care_task_type_medication
        CareTaskType.WEIGHING -> Res.string.care_task_type_weighing
        CareTaskType.OTHER -> Res.string.care_task_type_other
    }
)

@Composable
fun CareTaskFrequency.label(): String = stringResource(
    when (this) {
        CareTaskFrequency.DAILY -> Res.string.care_task_frequency_daily
        CareTaskFrequency.SPECIFIC_DAYS -> Res.string.care_task_frequency_specific_days
        CareTaskFrequency.WEEKLY -> Res.string.care_task_frequency_weekly
        CareTaskFrequency.ONE_TIME -> Res.string.care_task_frequency_one_time
    }
)

@Composable
fun CareTaskAnimalGroup.label(): String = stringResource(
    when (this) {
        CareTaskAnimalGroup.LACTATING -> Res.string.care_task_animal_group_lactating
        CareTaskAnimalGroup.PREGNANT -> Res.string.care_task_animal_group_pregnant
        CareTaskAnimalGroup.YOUNG_DOES -> Res.string.care_task_animal_group_young_does
        CareTaskAnimalGroup.DRY -> Res.string.care_task_animal_group_dry
        CareTaskAnimalGroup.BREEDING_BUCKS -> Res.string.care_task_animal_group_breeding_bucks
        CareTaskAnimalGroup.GENERAL -> Res.string.care_task_animal_group_general
        CareTaskAnimalGroup.ALL -> Res.string.care_task_animal_group_all
    }
)

@Composable
fun TimeOfDay.label(): String = stringResource(
    when (this) {
        TimeOfDay.MORNING -> Res.string.time_of_day_morning
        TimeOfDay.AFTERNOON -> Res.string.time_of_day_afternoon
        TimeOfDay.BOTH -> Res.string.time_of_day_both
        TimeOfDay.ANY -> Res.string.time_of_day_any
    }
)
