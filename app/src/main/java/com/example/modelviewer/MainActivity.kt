package com.example.modelviewer

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.SceneView
import io.github.sceneview.loaders.ModelLoader
import io.github.sceneview.node.ModelNode
import io.github.sceneview.loaders.EnvironmentLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var sceneView: SceneView
    private lateinit var modelLoader: ModelLoader
    private lateinit var scaleDetector: ScaleGestureDetector
    private lateinit var overlayContainer: FrameLayout

    private lateinit var progressBar: ProgressBar

    private var selectedModel: ModelData? = null

    private var lastX = 0f
    private var lastY = 0f

    private val models = mutableListOf<ModelData>()
    private var modelCounter = 0L


    private val modelFiles = arrayOf(
        "ABeautifulGame.glb",
        "CarConcept.glb",
        "CommercialRefrigerator.glb",
        "DiffuseTransmissionPlant.glb"
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        sceneView = findViewById(R.id.sceneView)
        overlayContainer = findViewById(R.id.overlayContainer)
        progressBar = findViewById(R.id.progressBar)

        modelLoader = ModelLoader(
            engine = sceneView.engine,
            context = this
        )

        sceneView.cameraNode.position = Float3(
            0f,
            0f,
            5f
        )

        findViewById<Button>(R.id.btnAddModel)
            .setOnClickListener {
                showModelPicker()
            }

        setupScaleDetector()
        setupTouchListener()
    }

    private fun setupScaleDetector() {

        scaleDetector = ScaleGestureDetector(
            this,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

                override fun onScale(detector: ScaleGestureDetector): Boolean {

                    selectedModel?.let { model ->

                        if (model.interactionMode) {

                            model.modelZoomScale *= detector.scaleFactor

                            model.node.scale = Float3(
                                model.modelZoomScale,
                                model.modelZoomScale,
                                model.modelZoomScale
                            )

                        } else {

                            model.containerScale *= detector.scaleFactor

                            model.node.scale = Float3(
                                model.containerScale,
                                model.containerScale,
                                model.containerScale
                            )
                        }
                    }

                    return true
                }
            }
        )
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {

        sceneView.setOnTouchListener { _, event ->

            scaleDetector.onTouchEvent(event)

            selectedModel?.let { model ->

                when (event.actionMasked) {

                    MotionEvent.ACTION_DOWN -> {

                        lastX = event.x
                        lastY = event.y
                    }

                    MotionEvent.ACTION_MOVE -> {

                        val dx = event.x - lastX
                        val dy = event.y - lastY

                        if (!model.interactionMode) {

                            model.node.position =
                                model.node.position.copy(
                                    x = model.node.position.x + dx * 0.01f,
                                    y = model.node.position.y - dy * 0.01f,
                                    z = model.node.position.z
                                )

                        } else {

                            model.node.rotation =
                                model.node.rotation.copy(
                                    y = model.node.rotation.y + dx * 0.5f,
                                    x = model.node.rotation.x + dy * 0.2f
                                )
                        }

                        lastX = event.x
                        lastY = event.y
                    }
                }
            }

            true
        }
    }

    private fun showModelPicker() {
        AlertDialog.Builder(this)
            .setTitle("Select Model")
            .setItems(modelFiles) { _, which ->
                loadModel(modelFiles[which])
            }
            .show()
    }

    private fun loadModel(fileName: String) {

        progressBar.visibility = View.VISIBLE

        progressBar.post {

            try {

                val modelInstance = modelLoader.createModelInstance(
                    assetFileLocation = "models/$fileName"
                )

                val modelNode = ModelNode(
                    modelInstance = modelInstance,
                    scaleToUnits = 1.0f
                )

                val spacing = 0.8f

                modelNode.position = Float3(
                    0f,
                    -(models.size * spacing),
                    -4f
                )

                if (models.isEmpty()) {
                    sceneView.cameraNode.lookAt(modelNode.position)
                }

                sceneView.addChildNode(modelNode)

                val overlayView = layoutInflater.inflate(
                    R.layout.model_overlay,
                    overlayContainer,
                    false
                )

                val interactBtn =
                    overlayView.findViewById<ImageButton>(R.id.btnInteract)

                val closeBtn =
                    overlayView.findViewById<ImageButton>(R.id.btnClose)

                val txtMode =
                    overlayView.findViewById<TextView>(R.id.txtMode)

                val modelData = ModelData(
                    id = ++modelCounter,
                    node = modelNode,
                    overlayView = overlayView
                )

                interactBtn.setOnClickListener {

                    modelData.interactionMode =
                        !modelData.interactionMode

                    txtMode.text =
                        if (modelData.interactionMode)
                            "INTERACTION"
                        else
                            "MOVE"

                    overlayView.animate()
                        .scaleX(
                            if (modelData.interactionMode) 1.1f else 1f
                        )
                        .scaleY(
                            if (modelData.interactionMode) 1.1f else 1f
                        )
                        .setDuration(150)
                        .start()

                    models.forEach {
                        it.overlayView.alpha = 0.6f
                        it.overlayView.scaleX = 1f
                        it.overlayView.scaleY = 1f
                    }

                    overlayView.alpha = 1f

                    selectedModel = modelData
                }

                closeBtn.setOnClickListener {

                    sceneView.removeChildNode(modelNode)

                    overlayContainer.removeView(overlayView)

                    models.remove(modelData)

                    if (selectedModel == modelData) {
                        selectedModel =
                            models.firstOrNull { it != modelData }
                    }
                }

                overlayContainer.addView(overlayView)

                overlayView.alpha = 1f
                overlayView.elevation = 12f

                overlayView.x = 24f
                overlayView.y = 80f + (models.size * 140)
                models.add(modelData)

                selectedModel = modelData

            } catch (e: Exception) {

                Log.e("MODEL_LOAD", "Error loading model", e)

            } finally {

                progressBar.visibility = View.GONE
            }
        }
    }
}