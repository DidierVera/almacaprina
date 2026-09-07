package com.didiprogrammer.almacaprina.business

import com.didiprogrammer.almacaprina.domain.model.Goat
import com.didiprogrammer.almacaprina.domain.model.GoatStatus

/** CurrentHerdStatus — conteo de cabras activas por current_status. */
fun currentHerdStatusCounts(goats: List<Goat>): Map<GoatStatus, Int> =
    goats.filter { it.exitDate == null }.groupingBy { it.currentStatus }.eachCount()
