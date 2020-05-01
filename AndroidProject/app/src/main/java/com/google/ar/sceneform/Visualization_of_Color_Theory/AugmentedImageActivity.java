/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ar.sceneform.Visualization_of_Color_Theory;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Visualization_of_Color_Theory.helpers.SnackbarHelper;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This application demonstrates using augmented images to place anchor nodes. app to include image
 * tracking functionality.
 *
 * <p>In this example, we assume all images are static or moving slowly with a large occupation of
 * the screen. If the target is actively moving, we recommend to check
 * ArAugmentedImage_getTrackingMethod() and render only when the tracking method equals to
 * AR_AUGMENTED_IMAGE_TRACKING_METHOD_FULL_TRACKING. See details in <a
 * href="https://developers.google.com/ar/develop/c/augmented-images/">Recognize and Augment
 * Images</a>.
 */
public class AugmentedImageActivity extends AppCompatActivity {
    private static final String TAG = "AugmentedImageActivity";

    static final String EXTRA_I_PAINTING = "EXTRA_I_PAINTING";

    private enum STATE_CHANNEL {
        NONE, RED, YELLOW, BLUE
    }

    private ArFragment arFragment;
    private ImageView fitToScanView;

    private ViewGroup layoutButtons;
    private ViewGroup layoutOptions;
    private Switch switchRenderable;
    private Switch switchRed;
    private Switch switchYellow;
    private Switch switchBlue;
    private AugmentedImageNode[] nodes;
    private boolean channelChanged;
    private boolean renderChanged;
    private STATE_CHANNEL stateChannel;
    private STATE_CHANNEL stateChannelPrev;
    private boolean[] onChannels;
    private int curr_img;

    // Augmented image and its associated center pose anchor, keyed by the augmented image in
    // the database.
    private final Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        fitToScanView = findViewById(R.id.image_view_fit_to_scan);

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);

        layoutOptions = findViewById(R.id.layout_options);
        layoutOptions.setVisibility(View.INVISIBLE);
        switchRenderable = findViewById(R.id.switch_renderable);
        switchRed = findViewById(R.id.switch_red);
        switchYellow = findViewById(R.id.switch_yellow);
        switchBlue = findViewById(R.id.switch_blue);
        channelChanged = false;
        renderChanged = false;
        onChannels = new boolean[3];
        for (int i = 0; i < onChannels.length; i++) {
            onChannels[i] = false;
        }
        stateChannel = STATE_CHANNEL.NONE;
        stateChannelPrev = stateChannel;
        nodes = new AugmentedImageNode[AugmentedImageNode.NUM_IMAGE];
        curr_img = -1; /* can try recognize all image targets */
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (augmentedImageMap.isEmpty()) {
            fitToScanView.setVisibility(View.VISIBLE);
        }
        channelChanged = false;
        renderChanged = false;
    }

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();

        // If there is no frame, just return.
        if (frame == null) {
            return;
        }

        Collection<AugmentedImage> updatedAugmentedImages =
                frame.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            int i = augmentedImage.getIndex();
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    String text = "Detected Painting " + (i+1) + ": " + getPaintingsName(i);
                    SnackbarHelper.getInstance().showMessage(this, text);
//                    augmentedImageMap.remove(augmentedImage);
//                    if (nodes[i] != null) {
//                        nodes[i].removeNode();
//                        Log.i(TAG, "Augmented Image node [" + i + "] on PAUSED and removed. ");
//                    }
//                    Log.i(TAG, "Augmented Image [" + i + "] on PAUSED and removed. ");
                    break;

                case TRACKING:
                    // Have to switch to UI Thread to update View.
                    fitToScanView.setVisibility(View.GONE);
                    if (layoutOptions.getVisibility() != View.VISIBLE) {
                        layoutOptions.setVisibility(View.VISIBLE);
                    }
                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        nodes[i] = new AugmentedImageNode(this);
                        Log.i(TAG, "New Augmented Image [" + i + "] renderable created. ");
                        nodes[i].setImage(augmentedImage, i);
                        augmentedImageMap.put(augmentedImage, nodes[i]);
                        arFragment.getArSceneView().getScene().addChild(nodes[i]);
                    } else { // The node must be present
                        if (channelChanged) { // Checks every frame, only runs when toggled
                            nodes[i].setCanvasMaterial(i, onChannels);
                            Log.i(TAG, "Augmented Image [" + i + "] channel changed. ");
                        }
                        if (renderChanged) {
                            nodes[i].toggleView(switchRenderable.isChecked(), onChannels);
                            Log.i(TAG, "Augmented Image [" + i + "] render status changed. ");
                        }
                    }
                    curr_img = i;
                    break;

                case STOPPED:
                    augmentedImageMap.remove(augmentedImage);
                    nodes[i].removeNode();
                    Log.i(TAG, "Augmented Image [" + i + "] on STOPPED and removed. ");
                    curr_img = -1; /* Start look for new target */
                    break;
            }
            if (curr_img != augmentedImage.getIndex()) {
                augmentedImageMap.remove(augmentedImage);
                if (nodes[i] != null) {
                    nodes[i].removeNode();
                    Log.i(TAG, "Augmented Image node [" + i + "] on globally removed. ");
                }
                Log.i(TAG, "Augmented Image [" + i + "] on globally removed. ");
            }
        }
        if (channelChanged) {
            channelChanged = false;
        }
        if (renderChanged) {
            renderChanged = false;
        }
    }

    public void toggleChannel(View view) {
        channelChanged = true;
        switch (view.getId()) {
            case R.id.switch_red:
                toggleOnChannels(0);
                break;
            case R.id.switch_yellow:
                toggleOnChannels(1);
                break;
            case R.id.switch_blue:
                toggleOnChannels(2);
                break;
        }
    }

    private void toggleOnChannels(int i) {
        onChannels[i] = !onChannels[i];
    }

    public void reset(View view) {
        augmentedImageMap.clear();
        for (int i= 0; i < AugmentedImageNode.NUM_IMAGE; i++) {
            if (nodes[i] != null) {
                nodes[i].removeNode();
            }
        }
        Log.i(TAG, "Reset models and augmented image ran. ");
    }

    private int getChannel() {
        return stateChannel.ordinal() - 1;
    }

    private String getPaintingsName(int iImage) {
        String ori= AugmentedImageFragment.NAMES_IMAGE[iImage];
        return ori.substring(0, ori.length() - 4);
    }

    public void toggleRenderable(View view) {
        renderChanged = true;
    }

    public void invokeChart(View view) {
        Intent intent = new Intent(this, ChartActivity.class);
        intent.putExtra(EXTRA_I_PAINTING, curr_img);
        startActivity(intent);
//        overridePendingTransition(R.anim.move_right_in_activity, R.anim.move_left_out_activity);
    }
}
