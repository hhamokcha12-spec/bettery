package com.example

import android.content.Context
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.ChargeViewModel
import com.example.ui.MainChargeScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("HyperCharge Enterprise", appName)
  }

  @Test
  fun `charge viewmodel and screen initialization`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = ChargeViewModel(application)
    assertNotNull(viewModel)
    
    composeTestRule.setContent {
      MyApplicationTheme {
        MainChargeScreen(viewModel = viewModel)
      }
    }
    composeTestRule.waitForIdle()
  }
}
