package com.example.ui.core

import com.airbnb.epoxy.EpoxyController
import com.example.ui.dialogMenu

class ListDialogController(private val onItemSelected: () -> Unit) : EpoxyController() {

    var menus = emptyList<DialogMenuItem>()
        set(value) {
            field = value
            requestModelBuild()
        }

    override fun buildModels() {

        menus.forEachIndexed { index, it ->

            dialogMenu {
                id(index)
                text(it.title)
                iconRes(it.iconRes)
                onClick { _, _, _, _ ->
                    it.action()
                    this@ListDialogController.onItemSelected()
                }
            }
        }
    }
}