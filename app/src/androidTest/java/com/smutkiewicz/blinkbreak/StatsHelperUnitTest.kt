package com.smutkiewicz.blinkbreak

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import com.smutkiewicz.blinkbreak.util.StatsHelper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class StatsHelperUnitTest {

    private var statsHelper: StatsHelper? = null

    @Before
    fun init() {
        statsHelper = StatsHelper(InstrumentationRegistry.getTargetContext())
    }

    @Test
    fun dateDifference_isPositive() {
        statsHelper?.lastBreak = "23:59:59"
        val differenceString = statsHelper?.calculateTimeDifference()
        Log.d("TAG", differenceString)
        assert(differenceString!!.contains("-"))
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.smutkiewicz.blinkbreak", appContext.packageName)
    }
}
