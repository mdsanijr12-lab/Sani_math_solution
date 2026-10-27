package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.engine.SmartMathSolverEngine
import com.example.data.repository.MathRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

  @Test
  fun `verify standalone math solver resolves equations without Firebase`() {
    // Quadratic equation
    val quadSolution = SmartMathSolverEngine.solve("x^2 - 5x + 6 = 0")
    assertTrue(quadSolution.isMathQuestion)
    assertFalse(quadSolution.isAmbiguous)
    assertTrue(quadSolution.shortAnswer.contains("2") && quadSolution.shortAnswer.contains("3"))

    // Linear equation
    val linearSolution = SmartMathSolverEngine.solve("2x + 5 = 15")
    assertTrue(linearSolution.isMathQuestion)
    assertEquals("x = 5", linearSolution.shortAnswer)

    // Fraction arithmetic
    val fractionSolution = SmartMathSolverEngine.solve("1/2 + 3/4")
    assertTrue(fractionSolution.isMathQuestion)
    assertEquals("5/4", fractionSolution.shortAnswer)

    // Non-math question guard
    val nonMathSolution = SmartMathSolverEngine.solve("hello, who are you?")
    assertFalse("Non-math questions should be rejected", nonMathSolution.isMathQuestion)
    assertTrue(nonMathSolution.nonMathExplanation.contains("SANI MATH GUIDE"))
  }
}
