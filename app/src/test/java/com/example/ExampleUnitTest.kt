package com.example

import com.example.model.DeliveryArea
import com.example.util.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testBengaliDigitsConversion() {
        val english = "1250"
        val expected = "১২৫০"
        assertEquals(expected, FormatUtils.toBengaliDigits(english))
    }

    @Test
    fun testTakaCurrencyFormatting() {
        val amount = 1250
        val formatted = FormatUtils.formatTaka(amount, inBengaliNumeral = true)
        assertEquals("৳১,২৫০", formatted)
    }

    @Test
    fun testDeliveryCharges() {
        assertEquals(60, DeliveryArea.INSIDE_DHAKA.charge)
        assertEquals(120, DeliveryArea.OUTSIDE_DHAKA.charge)

        val subtotal = 1500
        val totalInsideDhaka = subtotal + DeliveryArea.INSIDE_DHAKA.charge
        val totalOutsideDhaka = subtotal + DeliveryArea.OUTSIDE_DHAKA.charge

        assertEquals(1560, totalInsideDhaka)
        assertEquals(1620, totalOutsideDhaka)
    }

    @Test
    fun testBangladeshPhoneValidation() {
        val validPhone1 = "01712345678"
        val validPhone2 = "01987654321"
        val invalidPhoneShort = "01712345"
        val invalidPhonePrefix = "02712345678"

        val regex = Regex("^01[3-9]\\d{8}$")
        assertTrue(regex.matches(validPhone1))
        assertTrue(regex.matches(validPhone2))
        assertFalse(regex.matches(invalidPhoneShort))
        assertFalse(regex.matches(invalidPhonePrefix))
    }
}
