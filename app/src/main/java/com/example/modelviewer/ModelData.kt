package com.example.modelviewer

import android.view.View
import io.github.sceneview.node.ModelNode

data class ModelData(
    val id: Long,
    val node: ModelNode,
    val overlayView: View,

    var interactionMode: Boolean = false,

    var containerScale: Float = 1f,
    var modelZoomScale: Float = 1f
)