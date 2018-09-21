package com.smutkiewicz.blinkbreak

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.smutkiewicz.blinkbreak.util.StatsHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for utility class StatsHelper used by app for calculating statistics data.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class StatsHelperUnitTest
{
    private var statsHelper: StatsHelper? = null

    @Before
    fun init()
    {
        statsHelper = StatsHelper(InstrumentationRegistry.getTargetContext())
    }

    @Test
    fun dateDifference_isPositive()
    {
        val differenceString = statsHelper?.calculateTimeDifferenceString(
                    "01/01/2018 23:59:59",
                    "01/02/2018 00:00:01"
        ) // lastBreak, currentDate

        assertTrue(!differenceString!!.contains("-"))
    }

    @Test
    fun dateDifferenceSeconds_isCorrect()
    {
        val differenceString = statsHelper?.calculateTimeDifferenceString(
            "01/01/2018 23:59:59",
            "01/02/2018 00:00:01"
        )

        assertTrue(differenceString!!.contentEquals( "2 second(s) ago."))
    }

    @Test
    fun dateDifferenceMinutes_isCorrect()
    {
        val differenceString = statsHelper?.calculateTimeDifferenceString(
            "01/01/2018 23:58:59",
            "01/02/2018 00:00:01"
        )

        assertTrue(differenceString!!.contentEquals( "1 min(s) ago."))
    }

    @Test
    fun dateDifferenceHours_isCorrect()
    {
        val differenceString = statsHelper?.calculateTimeDifferenceString(
            "01/01/2018 22:58:59",
            "01/02/2018 00:00:01"
        )

        assertTrue(differenceString!!.contentEquals( "1 hour(s) ago."))
    }

    @Test
    fun dateDifferenceDays_isCorrect()
    {
        val differenceString = statsHelper?.calculateTimeDifferenceString(
            "01/01/2018 22:58:59",
            "01/03/2018 00:00:01"
        )

        assertTrue(differenceString!! == "1 day(s) ago.")
    }

    @Test
    fun useAppContext()
    {
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.smutkiewicz.blinkbreak", appContext.packageName)
    }
}
