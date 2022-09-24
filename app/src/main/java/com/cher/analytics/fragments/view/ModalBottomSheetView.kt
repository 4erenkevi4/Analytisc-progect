package com.cher.analytics.fragments.view

import android.annotation.SuppressLint
import android.text.style.BackgroundColorSpan
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ModalBottomSheetView(isNeedHide: MutableState<Boolean>, backgroundColorSpan: Color, hide : Dp) {
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden
    )
    val coroutineScope = rememberCoroutineScope()

    BackHandler(sheetState.isVisible) {
        coroutineScope.launch { sheetState.show() }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = { BottomSheet(backgroundColorSpan, hide) },
        modifier = Modifier.fillMaxSize(),
    ) {}
    if (isNeedHide.value) {
        coroutineScope.launch {
            if (sheetState.isVisible) sheetState.hide()
            else {
                sheetState.animateTo(ModalBottomSheetValue.Expanded)
                isNeedHide.value = false
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
suspend fun ShowBottomSheet(sheetState: ModalBottomSheetState) {
    if (sheetState.isVisible) sheetState.hide()
    else sheetState.animateTo(ModalBottomSheetValue.Expanded)
}

@Composable
fun BottomSheet(backgroundColorSpan: Color, hide : Dp) {
    Column(
        modifier = Modifier.padding(32.dp).background(backgroundColorSpan)
    ) {
        Text(
            text = "Bottom sheet",
            style = MaterialTheme.typography.h6
        )
        Spacer(modifier = Modifier.height(hide))
        Text(
            text = "Click outside the bottom sheet to hide it",
            style = MaterialTheme.typography.body1
        )
    }
}
