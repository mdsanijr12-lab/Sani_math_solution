package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.engine.SmartMathSolverEngine
import com.example.data.repository.MathRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
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
  fun `verify critical basic calculations do not return generic completion message`() {
    // Test 1: 1+2 -> 3
    val sol1 = SmartMathSolverEngine.solve("1+2")
    assertTrue(sol1.isMathQuestion)
    assertNotEquals("সমাধান সম্পন্ন", sol1.shortAnswer)
    assertNotEquals("সমাধানসম্পন্ন", sol1.shortAnswer)
    assertTrue("Should contain 3", sol1.shortAnswer.contains("3"))
    assertTrue("Final answer should contain 3", sol1.finalAnswer.contains("3"))

    // Test 2: 10-4 -> 6
    val sol2 = SmartMathSolverEngine.solve("10-4")
    assertTrue(sol2.isMathQuestion)
    assertTrue("Should contain 6", sol2.shortAnswer.contains("6"))

    // Test 3: 5*6 -> 30
    val sol3 = SmartMathSolverEngine.solve("5*6")
    assertTrue(sol3.isMathQuestion)
    assertTrue("Should contain 30", sol3.shortAnswer.contains("30"))

    // Test 4: 12/3 -> 4
    val sol4 = SmartMathSolverEngine.solve("12/3")
    assertTrue(sol4.isMathQuestion)
    assertTrue("Should contain 4", sol4.shortAnswer.contains("4"))

    // Test 5: x^2 - 5x + 6 = 0 সমাধান কর -> x = 2 অথবা x = 3
    val sol5 = SmartMathSolverEngine.solve("x^2 - 5x + 6 = 0 সমাধান কর")
    assertTrue(sol5.isMathQuestion)
    assertTrue("Should contain 2", sol5.shortAnswer.contains("2"))
    assertTrue("Should contain 3", sol5.shortAnswer.contains("3"))

    // Test Bengali input: ১+২
    val solBengali = SmartMathSolverEngine.solve("১+২")
    assertTrue(solBengali.isMathQuestion)
    assertTrue(solBengali.shortAnswer.contains("3"))
  }

  @Test
  fun `verify non-math questions are gracefully rejected with explanation`() {
    val nonMath = SmartMathSolverEngine.solve("What is the capital of Bangladesh?")
    assertFalse(nonMath.isMathQuestion)
    assertTrue(nonMath.nonMathExplanation.contains("SANI MATH GUIDE"))
  }
}
