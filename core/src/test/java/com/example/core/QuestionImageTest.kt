package com.example.core

import org.junit.Assert
import org.junit.Test

class QuestionImageTest {

    @Test
    fun testFromRawString() {
        Assert.assertEquals(QuestionImage.Empty, QuestionImage.fromRawString(""))
        Assert.assertEquals(
            QuestionImage.LocalImage("2022_5_15_22_48_40_125.png"),
            QuestionImage.fromRawString("2022_5_15_22_48_40_125.png")
        )
        Assert.assertEquals(
            QuestionImage.FireStoreImage("BzacvV0gVxepKEy8n58x8GS99cR2/2022_5_15_22_48_40_125.png"),
            QuestionImage.fromRawString("BzacvV0gVxepKEy8n58x8GS99cR2/2022_5_15_22_48_40_125.png")
        )
    }

}