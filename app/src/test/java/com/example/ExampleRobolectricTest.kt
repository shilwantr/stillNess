package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("STILL", appName)
  }

  @Test
  fun `verify journal tab state and chart data points`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.viewmodel.WellnessViewModel(context)
    viewModel.setTab(com.example.ui.components.StillTab.TRENDS)
    assertEquals(com.example.ui.components.StillTab.TRENDS, viewModel.currentTab.value)

    val points = viewModel.getChartPoints()
    org.junit.Assert.assertEquals(7, points.size)
    org.junit.Assert.assertEquals("Mon", points.first().label)
    org.junit.Assert.assertEquals("Sun", points.last().label)

    org.junit.Assert.assertEquals(
        com.example.ui.glass.AmbientTheme.NIGHT_FERRIS_WHEEL,
        viewModel.ambientTheme.value
    )
  }

  @Test
  fun `verify quote rotates on each app open`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.ui.viewmodel.WellnessViewModel(context)
    val firstQuoteId = viewModel.currentQuote.value.id
    viewModel.rotateQuoteOnAppOpen(isFromActivityStart = false)
    val secondQuoteId = viewModel.currentQuote.value.id
    assertNotEquals(firstQuoteId, secondQuoteId)
  }
}
