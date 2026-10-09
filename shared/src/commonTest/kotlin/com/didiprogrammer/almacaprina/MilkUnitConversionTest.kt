package com.didiprogrammer.almacaprina

import com.didiprogrammer.almacaprina.business.MilkEntryUnit
import com.didiprogrammer.almacaprina.business.fromMilliliters
import com.didiprogrammer.almacaprina.business.toMilliliters
import com.didiprogrammer.almacaprina.business.totalLitersDay
import com.didiprogrammer.almacaprina.business.totalMlDay
import com.didiprogrammer.almacaprina.domain.model.MilkProductionRecord
import kotlinx.datetime.LocalDate
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MilkUnitConversionTest {

    private fun assertClose(expected: Double, actual: Double, tolerance: Double = 0.01) =
        assertTrue(abs(expected - actual) <= tolerance, "esperado $expected, obtenido $actual")

    @Test
    fun millilitersAreStoredAsEntered() {
        assertEquals(750.0, MilkEntryUnit.MILLILITER.toMilliliters(750.0))
    }

    @Test
    fun ouncesAreConvertedByWeightUsingMilkDensity() {
        // 8 oz de peso = 226,796 g; ÷ 1,03 g/ml ≈ 220,19 ml (no 236,6 ml como una onza fluida de agua).
        assertClose(220.19, MilkEntryUnit.OUNCE.toMilliliters(8.0))
    }

    @Test
    fun ouncesRoundTripThroughMilliliters() {
        assertClose(8.0, MilkEntryUnit.OUNCE.fromMilliliters(MilkEntryUnit.OUNCE.toMilliliters(8.0)), 1e-9)
    }

    @Test
    fun recordTotalsAreInMillilitersAndLiters() {
        val record = MilkProductionRecord(
            id = "1",
            goatId = "g",
            date = LocalDate(2026, 10, 9),
            morningMilkingMl = 1250.0,
            eveningMilkingMl = 750.0
        )
        assertEquals(2000.0, record.totalMlDay())
        assertEquals(2.0, record.totalLitersDay())
    }
}
