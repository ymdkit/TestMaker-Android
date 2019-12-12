package jp.gr.java_conf.foobar.testmaker.service.view.main


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
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
    fun addTestWithCategory() {

        val buttonToEdit = onView(
                allOf(withId(R.id.button), withContentDescription("画像"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.edit),
                                        0),
                                0),
                        isDisplayed()))
        buttonToEdit.perform(click())





    }


    @Test
    fun uITest() {
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

        val appCompatButton = onView(
                allOf(withId(R.id.button_category), withText("カテゴリ"),
                        childAtPosition(
                                allOf(withId(R.id.test),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                2)),
                                1),
                        isDisplayed()))
        appCompatButton.perform(click())

        val appCompatEditText2 = onView(
                allOf(withId(R.id.set_cate),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_answer),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText2.perform(replaceText("カテゴリ"), closeSoftKeyboard())

        val appCompatImageView = onView(
                allOf(withId(R.id.imageView4), withContentDescription("画像"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.color_chooser),
                                        0),
                                3),
                        isDisplayed()))
        appCompatImageView.perform(click())

        val appCompatImageButton2 = onView(
                allOf(withId(R.id.add), withContentDescription("画像"),
                        childAtPosition(
                                allOf(withId(R.id.layout_dialog_category),
                                        childAtPosition(
                                                withId(R.id.custom),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatImageButton2.perform(click())

        val appCompatButton2 = onView(
                allOf(withId(R.id.button_add), withText("+ 追加して保存"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                3),
                        isDisplayed()))
        appCompatButton2.perform(click())

        val appCompatImageButton3 = onView(
                allOf(withId(R.id.button), withContentDescription("画像"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.open),
                                        0),
                                0),
                        isDisplayed()))
        appCompatImageButton3.perform(click())

        val appCompatImageButton4 = onView(
                allOf(withId(R.id.button), withContentDescription("画像"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.edit),
                                        0),
                                0),
                        isDisplayed()))
        appCompatImageButton4.perform(click())

        val appCompatImageButton5 = onView(
                allOf(withId(R.id.button_expand), withContentDescription("開く"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.cardView),
                                        0),
                                0),
                        isDisplayed()))
        appCompatImageButton5.perform(click())

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
                allOf(withId(R.id.set_problem),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_question_write),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText4.perform(replaceText("記述問題"), closeSoftKeyboard())

        val appCompatEditText5 = onView(
                allOf(withId(R.id.set_answer_write),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.edit_write_view),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText5.perform(replaceText("あ"), closeSoftKeyboard())

        val appCompatButton3 = onView(
                allOf(withId(R.id.button_add), withText("+ 追加して保存"),
                        childAtPosition(
                                allOf(withId(R.id.layout_body),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                4)),
                                7),
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

        val appCompatImageButton7 = onView(
                allOf(withId(R.id.button_detail),
                        childAtPosition(
                                allOf(withId(R.id.layout_body),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                4)),
                                8),
                        isDisplayed()))
        appCompatImageButton7.perform(click())

        val switchCompat2 = onView(
                allOf(withId(R.id.change_explanation),
                        childAtPosition(
                                allOf(withId(R.id.layout_config),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                3)),
                                11),
                        isDisplayed()))
        switchCompat2.perform(click())

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

//        val appCompatCheckedTextView = onData(anything())
//                .inAdapterView(childAtPosition(
//                        withClassName(`is`("android.widget.PopupWindow$PopupBackgroundView")),
//                        0))
//                .atPosition(2)
//        appCompatCheckedTextView.perform(click())

        val appCompatButton6 = onView(
                allOf(withId(R.id.button_finish_config), withText("OK"),
                        childAtPosition(
                                allOf(withId(R.id.layout_config),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                3)),
                                12),
                        isDisplayed()))
        appCompatButton6.perform(click())

        val appCompatEditText9 = onView(
                allOf(withId(R.id.set_problem),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_question_write),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText9.perform(replaceText("あ"), closeSoftKeyboard())

        val appCompatEditText10 = onView(
                allOf(withId(R.id.set_answer_choose),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_answer_choose),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText10.perform(replaceText("か"), closeSoftKeyboard())

        val appCompatEditText11 = onView(
                allOf(withId(R.id.set_other1),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_other_1),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText11.perform(replaceText("さ"), closeSoftKeyboard())

        val appCompatEditText12 = onView(
                allOf(withId(R.id.set_other2),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_other_2),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText12.perform(replaceText("た"), closeSoftKeyboard())

        val appCompatEditText13 = onView(
                allOf(withId(R.id.set_other3),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_other_3),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText13.perform(replaceText("な"), closeSoftKeyboard())

        val appCompatButton7 = onView(
                allOf(withId(R.id.button_add), withText("+ 追加して保存"),
                        childAtPosition(
                                allOf(withId(R.id.layout_body),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                4)),
                                7),
                        isDisplayed()))
        appCompatButton7.perform(click())

        val appCompatImageButton8 = onView(
                allOf(withId(R.id.button_detail),
                        childAtPosition(
                                allOf(withId(R.id.layout_body),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                4)),
                                8),
                        isDisplayed()))
        appCompatImageButton8.perform(click())

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

        val appCompatButton8 = onView(
                allOf(withId(R.id.button_finish_config), withText("OK"),
                        childAtPosition(
                                allOf(withId(R.id.layout_config),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                3)),
                                12),
                        isDisplayed()))
        appCompatButton8.perform(click())

        val appCompatEditText14 = onView(
                allOf(withId(R.id.set_problem),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_question_write),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText14.perform(replaceText("あ"), closeSoftKeyboard())

        val appCompatEditText15 = onView(
                allOf(withId(R.id.set_answer_write_1),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_answer_complete_1),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText15.perform(replaceText("か"), closeSoftKeyboard())

        val appCompatEditText16 = onView(
                allOf(withId(R.id.set_answer_write_2),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_answer_complete_2),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText16.perform(replaceText("さ"), closeSoftKeyboard())

        val appCompatButton9 = onView(
                allOf(withId(R.id.button_add), withText("+ 追加して保存"),
                        childAtPosition(
                                allOf(withId(R.id.layout_body),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                4)),
                                7),
                        isDisplayed()))
        appCompatButton9.perform(click())

        val appCompatImageButton9 = onView(
                allOf(withId(R.id.button_detail),
                        childAtPosition(
                                allOf(withId(R.id.layout_body),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                4)),
                                8),
                        isDisplayed()))
        appCompatImageButton9.perform(click())

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

//        val appCompatCheckedTextView2 = onData(anything())
//                .inAdapterView(childAtPosition(
//                        withClassName(`is`("android.widget.PopupWindow$PopupBackgroundView")),
//                        0))
//                .atPosition(2)
//        appCompatCheckedTextView2.perform(click())

        val appCompatButton10 = onView(
                allOf(withId(R.id.button_finish_config), withText("OK"),
                        childAtPosition(
                                allOf(withId(R.id.layout_config),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                3)),
                                12),
                        isDisplayed()))
        appCompatButton10.perform(click())

        val appCompatEditText17 = onView(
                allOf(withId(R.id.set_problem),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_question_write),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText17.perform(replaceText("あ"), closeSoftKeyboard())

        val appCompatEditText18 = onView(
                allOf(withId(R.id.set_select_complete_1),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_select_complete_1),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText18.perform(replaceText("か"), closeSoftKeyboard())

        val appCompatEditText19 = onView(
                allOf(withId(R.id.set_select_complete_2),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_select_complete_2),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText19.perform(replaceText("さ"), closeSoftKeyboard())

        val appCompatEditText20 = onView(
                allOf(withId(R.id.set_select_complete_3),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_select_complete_3),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText20.perform(replaceText("た"), closeSoftKeyboard())

        val appCompatEditText21 = onView(
                allOf(withId(R.id.set_select_complete_4),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_select_complete_4),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText21.perform(replaceText("な"), closeSoftKeyboard())

        val appCompatEditText22 = onView(
                allOf(withId(R.id.set_select_complete_5),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_select_complete_5),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText22.perform(replaceText("は"), closeSoftKeyboard())

        val appCompatEditText23 = onView(
                allOf(withId(R.id.set_select_complete_6),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_select_complete_6),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText23.perform(replaceText("ま"), closeSoftKeyboard())

        val appCompatButton11 = onView(
                allOf(withId(R.id.button_add), withText("+ 追加して保存"),
                        childAtPosition(
                                allOf(withId(R.id.layout_body),
                                        childAtPosition(
                                                withClassName(`is`("androidx.constraintlayout.widget.ConstraintLayout")),
                                                4)),
                                7),
                        isDisplayed()))
        appCompatButton11.perform(click())

        pressBack()

        pressBack()

        val appCompatImageButton10 = onView(
                allOf(withId(R.id.button), withContentDescription("画像"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.play),
                                        0),
                                0),
                        isDisplayed()))
        appCompatImageButton10.perform(click())

        val appCompatCheckBox = onView(
                allOf(withId(R.id.check_random), withText("出題順をランダムにする"),
                        childAtPosition(
                                allOf(withId(R.id.layout_dialog_start),
                                        childAtPosition(
                                                withId(R.id.custom),
                                                0)),
                                1),
                        isDisplayed()))
        appCompatCheckBox.perform(click())

        val appCompatButton12 = onView(
                allOf(withId(R.id.button_start), withText("スタート"),
                        childAtPosition(
                                allOf(withId(R.id.layout_dialog_start),
                                        childAtPosition(
                                                withId(R.id.custom),
                                                0)),
                                8),
                        isDisplayed()))
        appCompatButton12.perform(click())

        val appCompatButton13 = onView(
                allOf(withId(R.id.button),
                        childAtPosition(
                                allOf(withId(R.id.button1),
                                        childAtPosition(
                                                withId(R.id.layout_play_select),
                                                0)),
                                0),
                        isDisplayed()))
        appCompatButton13.perform(click())

        val appCompatEditText24 = onView(
                allOf(withId(R.id.edit_answer),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_answer),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText24.perform(replaceText("か"), closeSoftKeyboard())

        val appCompatButton14 = onView(
                allOf(withId(R.id.button_judge), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.play_write_view),
                                        0),
                                1)))
        appCompatButton14.perform(scrollTo(), click())

        val appCompatCheckBox2 = onView(
                allOf(withId(R.id.check_select_2), withText("か"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.play_select_complete_view),
                                        0),
                                1)))
        appCompatCheckBox2.perform(scrollTo(), click())

        val appCompatCheckBox3 = onView(
                allOf(withId(R.id.check_select_3), withText("た"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.play_select_complete_view),
                                        0),
                                2)))
        appCompatCheckBox3.perform(scrollTo(), click())

        val appCompatButton15 = onView(
                allOf(withId(R.id.button_ok), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.play_select_complete_view),
                                        0),
                                6)))
        appCompatButton15.perform(scrollTo(), click())

        val appCompatButton16 = onView(
                allOf(withId(R.id.button_next), withText("次へ"),
                        childAtPosition(
                                allOf(withId(R.id.layout_play_mistake),
                                        childAtPosition(
                                                withId(R.id.play_mistake_view),
                                                0)),
                                1)))
        appCompatButton16.perform(scrollTo(), click())

        val appCompatButton17 = onView(
                allOf(withId(R.id.button_judge), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.play_write_view),
                                        0),
                                1)))
        appCompatButton17.perform(scrollTo(), click())

        val appCompatButton18 = onView(
                allOf(withId(R.id.button_next), withText("次へ"),
                        childAtPosition(
                                allOf(withId(R.id.layout_play_mistake),
                                        childAtPosition(
                                                withId(R.id.play_mistake_view),
                                                0)),
                                1)))
        appCompatButton18.perform(scrollTo(), click())

        val appCompatEditText25 = onView(
                allOf(withId(R.id.set_answer_1),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.textInputLayout_answer_1),
                                        0),
                                0),
                        isDisplayed()))
        appCompatEditText25.perform(replaceText("あ"), closeSoftKeyboard())

        val appCompatButton19 = onView(
                allOf(withId(R.id.button_judge), withText("OK"),
                        childAtPosition(
                                allOf(withId(R.id.answer_write_and),
                                        childAtPosition(
                                                withId(R.id.play_complete_view),
                                                0)),
                                5)))
        appCompatButton19.perform(scrollTo(), click())

        val appCompatButton20 = onView(
                allOf(withId(R.id.button_next), withText("次へ"),
                        childAtPosition(
                                allOf(withId(R.id.layout_play_mistake),
                                        childAtPosition(
                                                withId(R.id.play_mistake_view),
                                                0)),
                                1)))
        appCompatButton20.perform(scrollTo(), click())

        val appCompatImageButton11 = onView(
                allOf(withId(R.id.top), withContentDescription("画像"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(`is`("android.widget.LinearLayout")),
                                        1),
                                0),
                        isDisplayed()))
        appCompatImageButton11.perform(click())
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
