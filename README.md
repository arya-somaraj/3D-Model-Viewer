# 3D Model Viewer

## Overview

This application is a single-activity Android app that allows users to load, view, manipulate, and manage multiple 3D GLB models simultaneously. Users can add multiple models, drag them across the screen, resize them, switch between movement and interaction modes, and remove models when no longer needed.

---

## Technologies

* Language: Kotlin
* Minimum SDK: 24
* UI Framework: Android Views (XML)
* 3D Library: SceneView (built on top of Google Filament)


## 3D Library Used

**SceneView (built on top of Google Filament)**

The application uses SceneView as the primary 3D rendering library. SceneView provides a high-level Android API for working with 3D models while leveraging Google's Filament rendering engine underneath.

### Why SceneView?

* Native support for GLB model loading.
* Built on top of Google's high-performance Filament engine.
* Simplifies camera, scene, and model management.
* Faster development compared to using raw Filament APIs directly.
* Good performance for rendering multiple models simultaneously.

---

## Performance Optimizations

Several optimizations were applied to keep rendering smooth:

* **Single Activity Architecture** to minimize navigation and lifecycle overhead.
* **Single SceneView Instance** shared by all models.
* **Single Filament Engine** used throughout the application.
* **Multiple ModelNode Objects** instead of creating multiple rendering surfaces.
* Reuse of the same **ModelLoader** instance.
* Lightweight overlay controls for model actions.
* Models are loaded from bundled assets, avoiding network delays.
* Tested with multiple models loaded simultaneously.

---

## Trade-offs

To keep the implementation simple and reliable within the given timeframe:

* Model selection is based on the currently active model rather than advanced hit-testing.
* Overlay controls are implemented as Android views rather than fully integrated 3D UI elements.
* Container resizing and model zoom are represented through scaling operations on the model node.
* Focus was placed on functionality and performance over advanced visual effects.

---

## Improvements With More Time

If additional development time were available, the following enhancements would be implemented:

* Accurate model hit-testing and selection.
* Overlay controls that dynamically follow model positions on screen.
* Smooth animated transitions between interaction states.
* Model loading cache and preloading mechanism.
* Undo/redo support for transformations.
* Improved visual feedback for selected models.
* Advanced gesture handling and multimodel interactions.
* Better adaptive layouts for tablets and large screens.

---

## Known Limitations

* Complex or very large GLB models may take noticeable time to load.
* Overlay controls do not currently track the exact projected screen position of models.
* Different GLB assets may have different origins, scales, and orientations, requiring adjustment.
* Device-specific launcher animation errors may appear in Logcat on some OEM devices (OPPO/Realme/OnePlus); these are system launcher issues and do not affect application functionality.

---