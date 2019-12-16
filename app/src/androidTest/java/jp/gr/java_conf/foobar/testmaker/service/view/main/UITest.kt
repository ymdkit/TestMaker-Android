package jp.gr.java_conf.foobar.testmaker.service.view.main


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import jp.gr.java_conf.foobar.testmaker.service.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UITest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun addTest() {

        val appCompatImageButton = onView(
                allOf(withId(R.id.button_expand), withContentDescription("画像"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                0),
                        isDisplayed()))
        appCompatImageButton.perform(click())

        val appCompatEditText = onView(
                allOf(withId(R.id.edit_title),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_title),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText.perform(replaceText("問題集"), closeSoftKeyboard())

        val appCompatButton2 = onView(
                allOf(withId(R.id.button_add), withText("+ 追加して保存"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                3),
                        isDisplayed()))
        appCompatButton2.perform(click())

    }

    @Test
    fun editTest() {

        val buttonToEdit = onView(
                allOf(withId(R.id.button), withContentDescription("画像"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.edit),
                                        0),
                                0),
                        isDisplayed()))
        buttonToEdit.perform(click())


        val appCompatImageButton5 = onView(
                allOf(withId(R.id.button_expand), withContentDescription("開く"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                0),
                        isDisplayed()))
        appCompatImageButton5.perform(click())


        val appCompatEditText6 = onView(
                allOf(withId(R.id.set_problem),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_question_write),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText6.perform(replaceText("あ"), closeSoftKeyboard())

        val appCompatEditText7 = onView(
                allOf(withId(R.id.set_answer_write),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.edit_write_view),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText7.perform(replaceText("か"), closeSoftKeyboard())

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

        val switchCompat = onView(
                allOf(withId(R.id.change_explanation),
                        childAtPosition(
                                allOf(withId(R.id.layout_config),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                3)),
                                11),
                        isDisplayed()))
        switchCompat.perform(click())

        val appCompatButton4 = onView(
                allOf(withId(R.id.button_finish_config), withText("OK"),
                        childAtPosition(
                                allOf(withId(R.id.layout_config),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                3)),
                                12),
                        isDisplayed()))
        appCompatButton4.perform(click())

        val appCompatEditText8 = onView(
                allOf(withId(R.id.set_explanation),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_explanation),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText8.perform(replaceText("さ"), closeSoftKeyboard())

        val appCompatButton5 = onView(
                allOf(withId(R.id.button_add), withText("+ 追加して保存"),
                        childAtPosition(
                                allOf(withId(R.id.layout_body),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                4)),
                                7),
                        isDisplayed()))
        appCompatButton5.perform(click())
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
}
