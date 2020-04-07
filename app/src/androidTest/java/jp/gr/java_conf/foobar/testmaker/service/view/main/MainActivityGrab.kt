package jp.gr.java_conf.foobar.testmaker.service.view.main


import android.app.Activity
import android.graphics.Bitmap
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.jraska.falcon.Falcon
import jp.gr.java_conf.foobar.testmaker.service.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream


@LargeTest
@RunWith(JUnit4::class)
class MainActivityGrab {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        ActivityScenario.launch(MainActivity::class.java)
    }

    @Test
    fun mainActivityGrab() {

        addTest("古文単語")
        addTest("英語　重要語句")
        addTest("地理　気候")
        addTest("数学　公式")
        runBlocking {
            delay(500)
        }
        takeScreenshot("UITest1", mActivityTestRule.activity)
    }

    private fun addTest(title: String) {
        onView(withId(R.id.fab)).perform(click())
        onView(withId(R.id.edit_title)).perform(replaceText(title), closeSoftKeyboard())
        onView(withId(R.id.button_add)).perform(click())
    }

    private fun playTest(title: String) {
        onView(withId(R.id.play)).perform(click())
        onView(withId(R.id.edit_title)).perform(replaceText(title), closeSoftKeyboard())
        onView(withId(R.id.button_add)).perform(click())
    }

    private fun takeScreenshot(name: String, activity: Activity) {
        var bitmap: Bitmap? = null
        try {
            bitmap = Falcon.takeScreenshotBitmap(activity)

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, 1080, 2160)

            val imageFolder = activity.baseContext.cacheDir.also { it.mkdir() } // data/data/アプリ名/cache
            val imageFile = File(imageFolder, "$name.jpg")
            FileOutputStream(imageFile).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it)
                it.flush()
            }
        } catch (e: FileNotFoundException) {
        } finally {
            bitmap?.recycle()
        }
    }


}
