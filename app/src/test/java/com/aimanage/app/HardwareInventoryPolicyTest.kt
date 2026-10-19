package com.aimanage.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HardwareInventoryPolicyTest {
    @Test fun validPercent() {
        assertEquals(50, HardwareInventoryPolicy.percent(5, 10))
        assertEquals(0, HardwareInventoryPolicy.percent(0, 10))
        assertEquals(100, HardwareInventoryPolicy.percent(10, 10))
    }
    @Test fun invalidPercent() {
        assertNull(HardwareInventoryPolicy.percent(11, 10))
        assertNull(HardwareInventoryPolicy.percent(-1, 10))
        assertNull(HardwareInventoryPolicy.percent(1, 0))
        assertNull(HardwareInventoryPolicy.percent(null, 10))
    }
    @Test fun negativeByteValuesAreUnavailable() {
        assertNull(HardwareInventoryPolicy.validBytes(-1))
        assertEquals(0L, HardwareInventoryPolicy.validBytes(0))
    }
}
