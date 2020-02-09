package jp.gr.java_conf.foobar.testmaker.service.view.main


import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.jraska.falcon.Falcon
import jp.gr.java_conf.foobar.testmaker.service.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.lang.Thread.sleep

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

        val appCompatImageButton = onView(
                allOf(withId(R.id.button_expand), withContentDescription(mActivityTestRule.activity.baseContext.getString(R.string.image)),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                0),
                        isDisplayed()))
        appCompatImageButton.perform(click())

        val appCompatButton = onView(
                allOf(withId(R.id.button_category), withText(mActivityTestRule.activity.baseContext.getString(R.string.category)),
                        childAtPosition(
                                allOf(withId(R.id.test),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                2)),
                                1),
                        isDisplayed()))
        appCompatButton.perform(click())

        val appCompatEditText = onView(
                allOf(withId(R.id.set_cate),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_answer),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText.perform(replaceText("古文"), closeSoftKeyboard())

        val appCompatImageButton2 = onView(
                allOf(withId(R.id.add), withContentDescription(mActivityTestRule.activity.baseContext.getString(R.string.image)),
                        childAtPosition(
                                allOf(withId(R.id.layout_dialog_category),
                                        childAtPosition(
                                                withId(R.id.custom),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageButton2.perform(click())

        val appCompatEditText2 = onView(
                allOf(withId(R.id.edit_title),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_title),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText2.perform(click())

        val appCompatEditText3 = onView(
                allOf(withId(R.id.edit_title),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_title),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText3.perform(replaceText("要注意単語"), closeSoftKeyboard())

        val appCompatButton2 = onView(
                allOf(withId(R.id.button_add), withText(mActivityTestRule.activity.baseContext.getString(R.string.action_add)),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                3),
                        isDisplayed()))
        appCompatButton2.perform(click())

        addTest("ドイツ語 期末", R.id.imageView2, 1)

        editActivityTest()
        playActivityTest()

        addTest("地理 農業", R.id.imageView4, 3)
        addTest("数学 公式", R.id.imageView7, 6)
        addTest("英単語 復習", R.id.imageView8, 7)

        sleep(10000)

        takeScreenshot("MainActivity", mActivityTestRule.activity)
    }

    private fun editActivityTest() {

        val appCompatImageButton = onView(
                allOf(withId(R.id.button), withContentDescription("画像"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.edit),
                                        0),
                                0),
                        isDisplayed()))
        appCompatImageButton.perform(click())

        val appCompatImageButton3 = onView(
                allOf(withId(R.id.button_expand), withContentDescription("開く"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                0),
                        isDisplayed()))
        appCompatImageButton3.perform(click())

        val appCompatEditText3 = onView(
                allOf(withId(R.id.set_problem),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_question_write),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText3.perform(click())

        val appCompatEditText4 = onView(
                allOf(withId(R.id.set_answer_write),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.edit_write_view),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText4.perform(replaceText("暗記メーカー"), closeSoftKeyboard())

        val appCompatEditText5 = onView(
                allOf(withId(R.id.set_problem),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_question_write),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText5.perform(replaceText("このアプリの名前は"), closeSoftKeyboard())

        val appCompatButton2 = onView(
                allOf(withId(R.id.button_add), withText("+ 追加して保存"),
                        childAtPosition(
                                allOf(withId(R.id.layout_body),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                4)),
                                7),
                        isDisplayed()))
        appCompatButton2.perform(click())

        val appCompatImageButton4 = onView(
                allOf(withId(R.id.button_detail),
                        childAtPosition(
                                allOf(withId(R.id.layout_body),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                4)),
                                8),
                        isDisplayed()))
        appCompatImageButton4.perform(click())

        val appCompatRadioButton = onView(
                allOf(withId(R.id.radio_select), withText("選択"),
                        childAtPosition(
                                allOf(withId(R.id.radio_question),
                                        childAtPosition(
                                                withId(R.id.layout_config),
                                                1)),
                                1),
                        isDisplayed()))
        appCompatRadioButton.perform(click())

        val appCompatSpinner = onView(
                allOf(withId(R.id.spinner_size_select),
                        childAtPosition(
                                allOf(withId(R.id.layout_config),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                3)),
                                5),
                        isDisplayed()))
        appCompatSpinner.perform(click())

        onData(anything()).atPosition(4).perform(click());

        val appCompatButton3 = onView(
                allOf(withId(R.id.button_finish_config), withText("OK"),
                        childAtPosition(
                                allOf(withId(R.id.layout_config),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                3)),
                                12),
                        isDisplayed()))
        appCompatButton3.perform(click())

        val appCompatEditText6 = onView(
                allOf(withId(R.id.set_problem),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_question_write),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText6.perform(replaceText("不可算名詞はどれ"), closeSoftKeyboard())

        val appCompatEditText7 = onView(
                allOf(withId(R.id.set_answer_choose),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_answer_choose),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText7.perform(replaceText("information"), closeSoftKeyboard())

        val appCompatEditText8 = onView(
                allOf(withId(R.id.set_other1),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_other_1),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText8.perform(replaceText("dog"), closeSoftKeyboard())

        val appCompatEditText9 = onView(
                allOf(withId(R.id.set_other2),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_other_2),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText9.perform(replaceText("fee"), closeSoftKeyboard())

        val appCompatEditText10 = onView(
                allOf(withId(R.id.set_other3),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_other_3),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText10.perform(replaceText("idea"), closeSoftKeyboard())

        val appCompatEditText11 = onView(
                allOf(withId(R.id.set_other4),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_other_4),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText11.perform(replaceText("apple"), closeSoftKeyboard())

        val appCompatEditText12 = onView(
                allOf(withId(R.id.set_other5),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_other_5),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText12.perform(replaceText("game"), closeSoftKeyboard())

        takeScreenshot("EditActivity_select", mActivityTestRule.activity)


        val appCompatButton4 = onView(
                allOf(withId(R.id.button_add), withText("+ 追加して保存"),
                        childAtPosition(
                                allOf(withId(R.id.layout_body),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                4)),
                                7),
                        isDisplayed()))
        appCompatButton4.perform(click())

        val appCompatImageButton5 = onView(
                allOf(withId(R.id.button_detail),
                        childAtPosition(
                                allOf(withId(R.id.layout_body),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                4)),
                                8),
                        isDisplayed()))
        appCompatImageButton5.perform(click())

        val appCompatRadioButton2 = onView(
                allOf(withId(R.id.radio_complete), withText("完答"),
                        childAtPosition(
                                allOf(withId(R.id.radio_question),
                                        childAtPosition(
                                                withId(R.id.layout_config),
                                                1)),
                                2),
                        isDisplayed()))
        appCompatRadioButton2.perform(click())

        val appCompatSpinner2 = onView(
                allOf(withId(R.id.spinner_size_answer),
                        childAtPosition(
                                allOf(withId(R.id.layout_config),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                3)),
                                3),
                        isDisplayed()))
        appCompatSpinner2.perform(click())

        onData(anything()).atPosition(2).perform(click());


        val appCompatButton5 = onView(
                allOf(withId(R.id.button_finish_config), withText("OK"),
                        childAtPosition(
                                allOf(withId(R.id.layout_config),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                3)),
                                12),
                        isDisplayed()))
        appCompatButton5.perform(click())

        val appCompatEditText13 = onView(
                allOf(withId(R.id.set_problem),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_question_write),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText13.perform(replaceText("自然数かつ1桁の偶数を全て答えよ"), closeSoftKeyboard())

        val appCompatEditText14 = onView(
                allOf(withId(R.id.set_answer_write_1),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_answer_complete_1),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText14.perform(replaceText("2"), closeSoftKeyboard())

        val appCompatEditText15 = onView(
                allOf(withId(R.id.set_answer_write_2),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_answer_complete_2),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText15.perform(replaceText("4"), closeSoftKeyboard())

        val appCompatEditText16 = onView(
                allOf(withId(R.id.set_answer_write_3),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_answer_complete_3),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText16.perform(replaceText("6"), closeSoftKeyboard())

        val appCompatEditText17 = onView(
                allOf(withId(R.id.set_answer_write_4),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_answer_complete_4),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText17.perform(replaceText("8"), closeSoftKeyboard())

        takeScreenshot("EditActivity_complete", mActivityTestRule.activity)


        val appCompatButton6 = onView(
                allOf(withId(R.id.button_add), withText("+ 追加して保存"),
                        childAtPosition(
                                allOf(withId(R.id.layout_body),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                4)),
                                7),
                        isDisplayed()))
        appCompatButton6.perform(click())

        val appCompatImageButton6 = onView(
                allOf(withId(R.id.button_detail),
                        childAtPosition(
                                allOf(withId(R.id.layout_body),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                4)),
                                8),
                        isDisplayed()))
        appCompatImageButton6.perform(click())

        val appCompatRadioButton3 = onView(
                allOf(withId(R.id.radio_select_complete), withText("選択完答"),
                        childAtPosition(
                                allOf(withId(R.id.radio_question),
                                        childAtPosition(
                                                withId(R.id.layout_config),
                                                1)),
                                3),
                        isDisplayed()))
        appCompatRadioButton3.perform(click())

        val appCompatSpinner3 = onView(
                allOf(withId(R.id.spinner_size_answer),
                        childAtPosition(
                                allOf(withId(R.id.layout_config),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                3)),
                                3),
                        isDisplayed()))
        appCompatSpinner3.perform(click())

        onData(anything()).atPosition(2).perform(click());

        val appCompatButton7 = onView(
                allOf(withId(R.id.button_finish_config), withText("OK"),
                        childAtPosition(
                                allOf(withId(R.id.layout_config),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                3)),
                                12),
                        isDisplayed()))
        appCompatButton7.perform(click())

        val appCompatEditText18 = onView(
                allOf(withId(R.id.set_problem),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_question_write),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText18.perform(replaceText("正しいものを全て選べ"), closeSoftKeyboard())

        val appCompatEditText19 = onView(
                allOf(withId(R.id.set_select_complete_1),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_select_complete_1),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText19.perform(replaceText("AはBである"), closeSoftKeyboard())

        val appCompatEditText20 = onView(
                allOf(withId(R.id.set_select_complete_2),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_select_complete_2),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText20.perform(replaceText("CはDではない"), closeSoftKeyboard())

        val appCompatEditText21 = onView(
                allOf(withId(R.id.set_select_complete_3),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_select_complete_3),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText21.perform(replaceText("EとFはGである"), closeSoftKeyboard())

        val appCompatEditText22 = onView(
                allOf(withId(R.id.set_select_complete_4),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_select_complete_4),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText22.perform(replaceText("HとIはJである"), closeSoftKeyboard())

        val appCompatEditText23 = onView(
                allOf(withId(R.id.set_select_complete_5),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_select_complete_5),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText23.perform(replaceText("KはLかつMである"), closeSoftKeyboard())

        val appCompatEditText24 = onView(
                allOf(withId(R.id.set_select_complete_6),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_select_complete_6),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText24.perform(replaceText("NとOはPである"), closeSoftKeyboard())

        takeScreenshot("EditActivity_select_complete", mActivityTestRule.activity)

        val appCompatButton8 = onView(
                allOf(withId(R.id.button_add), withText("+ 追加して保存"),
                        childAtPosition(
                                allOf(withId(R.id.layout_body),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                4)),
                                7),
                        isDisplayed()))
        appCompatButton8.perform(click())

        val appCompatImageButton7 = onView(
                allOf(withContentDescription("前に戻る"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageButton7.perform(click())
    }

    private fun playActivityTest() {

        val appCompatImageButton = onView(
                allOf(withId(R.id.button), withContentDescription("画像"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.play),
                                        0),
                                0),
                        isDisplayed()))
        appCompatImageButton.perform(click())

        onView(withId(R.id.button_start)).perform(click())

        takeScreenshot("PlayActivity_write", mActivityTestRule.activity)

        onView(allOf(withId(R.id.button_judge), isDisplayed())).perform(click())
        onView(withId(R.id.button_next)).perform(click())

        takeScreenshot("PlayActivity_select", mActivityTestRule.activity)

        onView(withId(R.id.button_pass)).perform(click())
        onView(withId(R.id.button_next)).perform(click())

        takeScreenshot("PlayActivity_complete", mActivityTestRule.activity)

        onView(allOf(withId(R.id.button_judge), isDisplayed())).perform(click())
        onView(withId(R.id.button_next)).perform(click())
        onView(withId(R.id.check_select_3)).perform(click())
        onView(withId(R.id.check_select_6)).perform(click())

        takeScreenshot("PlayActivity_select_complete", mActivityTestRule.activity)

        onView(withId(R.id.button_ok)).perform(click())
        onView(withId(R.id.button_next)).perform(click())

        takeScreenshot("ResultActivity_result", mActivityTestRule.activity)

        sleep(3000)

        onView(withId(R.id.top)).perform(click())

    }

    private fun addTest(title: String, color: Int, position: Int) {
        val appCompatImageButton4 = onView(
                allOf(withId(R.id.button_expand), withContentDescription(mActivityTestRule.activity.baseContext.getString(R.string.image)),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                0),
                        isDisplayed()))
        appCompatImageButton4.perform(click())

        val appCompatImageView2 = onView(
                allOf(withId(color), withContentDescription(mActivityTestRule.activity.baseContext.getString(R.string.image)),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.color_chooser),
                                        0),
                                position),
                        isDisplayed()))
        appCompatImageView2.perform(click())

        val appCompatEditText5 = onView(
                allOf(withId(R.id.edit_title),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_title),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText5.perform(replaceText(title), closeSoftKeyboard())

        val appCompatButton4 = onView(
                allOf(withId(R.id.button_add), withText(mActivityTestRule.activity.baseContext.getString(R.string.action_add)),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                3),
                        isDisplayed()))
        appCompatButton4.perform(click())

    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }

    fun takeScreenshot(name: String, activity: Activity) {
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
