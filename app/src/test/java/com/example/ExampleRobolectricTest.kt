package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.MathRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context matches app name`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SANI MATH GUIDE", appName)
  }

  @Test
  fun `verify sample math problems are available and accurate`() {
    val samples = MathRepository.SAMPLE_PROBLEMS
    assertTrue("Should have multiple sample math problems", samples.isNotEmpty())
    val quadratic = samples.find { it.category == "বীজগণিত" }
    assertNotNull(quadratic)
    assertTrue(quadratic!!.solution.isMathQuestion)
    assertEquals("x = 2 অথবা x = 3", quadratic.solution.shortAnswer)
    assertEquals(4, quadratic.solution.steps.size)
  }
}
