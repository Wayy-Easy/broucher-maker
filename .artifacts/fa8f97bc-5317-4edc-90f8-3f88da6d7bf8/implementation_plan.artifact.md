# HTML Template Editor Implementation Plan

Implement comprehensive editing capabilities for HTML-based design templates using a WebView-based canvas and a JavaScript bridge.

## Proposed Changes

### [Component] JavaScript Editor Bridge
#### [NEW] [editor_bridge.js](file:///C:/Users/gabuy/MyOwn/BrochureCraft/app/src/main/res/assets/editor_bridge.js)
Create a comprehensive script to be injected into the HTML templates. It will:
- Make text nodes editable using `contenteditable`.
- Implement selection logic with visual highlighting.
- Extract computed styles and send them to the Android app.
- Provide APIs for Android to manipulate the DOM (styles, text, images, layout).
- Handle element reordering, duplication, and deletion.

### [Component] UI Components
#### [NEW] [HtmlDesignCanvas.kt](file:///C:/Users/gabuy/MyOwn/BrochureCraft/app/src/main/java/com/brochurecraft/app/ui/components/HtmlDesignCanvas.kt)
Create a Compose wrapper for `WebView` that:
- Loads the HTML template.
- Injects the `editor_bridge.js`.
- Sets up a `JavascriptInterface` to communicate with `EditorViewModel`.
- Handles selection and property update callbacks.

#### [MODIFY] [ElementPropertiesPanel.kt](file:///C:/Users/gabuy/MyOwn/BrochureCraft/app/src/main/java/com/brochurecraft/app/ui/components/EditorPanel.kt)
Extend the properties panel to support HTML-specific properties like:
- Line height, letter spacing.
- Margin and padding.
- Border radius and shadows.
- Object-fit for images.

### [Component] ViewModel Logic
#### [MODIFY] [EditorViewModel.kt](file:///C:/Users/gabuy/MyOwn/BrochureCraft/app/src/main/java/com/brochurecraft/app/ui/viewmodel/EditorViewModel.kt)
- Add `isHtmlMode` state.
- Add `htmlContent` and `selectedHtmlElement` states.
- Implement methods to send JS commands to the WebView (e.g., `updateHtmlStyle`, `updateHtmlText`, `replaceHtmlImage`).
- Handle the `onElementSelected` callback from JS to update the UI.

### [Component] Screen Integration
#### [MODIFY] [DesignEditorScreen.kt](file:///C:/Users/gabuy/MyOwn/BrochureCraft/app/src/main/java/com/brochurecraft/app/ui/screens/DesignEditorScreen.kt)
- Update the screen to toggle between `DesignCanvas` (native) and `HtmlDesignCanvas` (HTML) based on the template type.
- Connect the `HtmlDesignCanvas` to the `EditorViewModel`.

## Verification Plan

### Automated Tests
- Unit tests for `EditorViewModel` to verify command generation for WebView.

### Manual Verification
- Deploy the app and open `template_1.html`.
- Verify clicking any element highlights it and opens the inspector.
- Verify changing text, colors, and fonts reflects immediately.
- Verify image replacement and resizing.
- Verify reordering and duplication of sections.
- Export the final HTML and check if changes are persisted correctly.
