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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.google.ar.core.AugmentedImage;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.Texture;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Node for rendering an augmented image. The image is framed by placing the virtual picture frame
 * at the corners of the augmented image trackable.
 */
@SuppressWarnings({"AndroidApiChecker"})
public class AugmentedImageNode extends AnchorNode {
    private static final String TAG = "AugmentedImageNode";

    // The augmented image represented by this node.
    private AugmentedImage image;
    private int iImage;

    // Models of the 4 corners.  We use completable futures here to simplify
    // the error handling and asynchronous loading.  The loading is started with the
    // first construction of an instance, and then used when the image is set.
    private static CompletableFuture<ModelRenderable> ulCorner;
    private static CompletableFuture<ModelRenderable> urCorner;
    private static CompletableFuture<ModelRenderable> lrCorner;
    private static CompletableFuture<ModelRenderable> llCorner;

    static final int NUM_IMAGE = 3;
    private static final int NUM_CHANNEL = 3;
    private static final int NUM_CHANNELS = 6;
    private static final int iAll = -1;
    public static final int iRed = 0, iYellow = 1, iBlue = 2, iGreen = 3, iOrange = 4, iPurple = 5;
    private static final String
            FOLDER_IMAGES = AugmentedImageFragment.FOLDER_IMAGES,
            FOLDER_IMAGES_FILTERED = "filtered_images";
    private static final String[] NAMES_IMAGE = AugmentedImageFragment.NAMES_IMAGE;
    private static final String[][] NAMES_IMAGE_CHANNEL = new String[][] {
            {"Forest_R.png",        "Forest_Y.png",         "Forest_B.png",     "Forest_G.png",        "Forest_O.png",         "Forest_P.png"},
            {"Reflection_R.png",    "Reflection_Y.png",     "Reflection_B.png", "Reflection_G.png",    "Reflection_O.png",     "Reflection_P.png"},
            {"Still Life_R.png",    "Still Life_Y.png",     "Still Life_B.png", "Still Life_G.png",    "Still Life_O.png",     "Still Life_P.png"}};
    private static final float
            WIDTH_IMAGE = 0.0768F; // Get the ratio right, the actual size displayed will be scaled appropriately
    private static final float[] HEIGHTS_IMAGE = new float[] {0.0596F, 0.0763F, 0.0622F};

    private static ModelRenderable[] renderablesCanvas;
    private static Node nodeCanvas;
    private static Material materialCurr;
    private static Material[] materialsOri;
    private static Material[][] materialsChannel;

    AugmentedImageNode(Context context) {
        // Upon construction, start loading the models for the corners of the frame.
        if (ulCorner == null) {
            ulCorner =
                    ModelRenderable.builder()
                            .setSource(context, Uri.parse("models/frame_upper_left.sfb"))
                            .build();
            urCorner =
                    ModelRenderable.builder()
                            .setSource(context, Uri.parse("models/frame_upper_right.sfb"))
                            .build();
            llCorner =
                    ModelRenderable.builder()
                            .setSource(context, Uri.parse("models/frame_lower_left.sfb"))
                            .build();
            lrCorner =
                    ModelRenderable.builder()
                            .setSource(context, Uri.parse("models/frame_lower_right.sfb"))
                            .build();
        }

        if (renderablesCanvas == null) { // If models not loaded
            materialsOri = new Material[NUM_IMAGE];
            materialsChannel = new Material[NUM_IMAGE][NUM_CHANNELS]; // Include secondary
            renderablesCanvas = new ModelRenderable[NUM_IMAGE];
            // Renderable built for the shape, material is dummy, will get changed
            for (int i = 0; i < renderablesCanvas.length; i++) {
                final int iFinal = i;
                MaterialFactory.makeTransparentWithColor(context, new Color(0, 0, 255))
                        .thenAccept(
                                material -> {
                                    materialCurr = material;
                                    renderablesCanvas[iFinal] = ShapeFactory.makeCube(
                                            new Vector3(WIDTH_IMAGE, 0.0001f, HEIGHTS_IMAGE[iFinal]),
                                            new Vector3(0f, 0f, 0f), material);
                                });
            }

            // Load all possible materials
            for (int i = 0; i < NAMES_IMAGE.length; i++) {
                final int iFinal = i;
                String nameOri = NAMES_IMAGE[i];
                Texture.builder().setSource(
                        getBitmapFromAsset(context, FOLDER_IMAGES + "/" + nameOri)).build()
                        .thenAccept(texture ->
                            MaterialFactory.makeOpaqueWithTexture(context, texture)
                                    .thenAccept(
                                            material ->
                                                materialsOri[iFinal] = material
                                            )
                        );
                for (int j = 0; j < NAMES_IMAGE_CHANNEL[i].length; j++) {
                    final int jFinal = j;
                    String nameChannel = NAMES_IMAGE_CHANNEL[i][j];
                    Texture.builder().setSource(
                            getBitmapFromAsset(context, FOLDER_IMAGES_FILTERED + "/" + nameChannel))
                            .build()
                            .thenAccept(texture ->
                                MaterialFactory.makeOpaqueWithTexture(context, texture)
                                        .thenAccept(
                                                material ->
                                                    materialsChannel[iFinal][jFinal] = material
                                                )
                            );
                }
            }
        }
    }

    static Bitmap getBitmapFromAsset(Context context, String uri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(context.getAssets().open(uri));
        } catch (IOException e) {
            Log.e(TAG, "Exception loading, file not found at: " + uri);
        }
        return bitmap;
    }
    /**
     * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
     * created based on an Anchor created from the image. The corners are then positioned based on the
     * extents of the image. There is no need to worry about world coordinates since everything is
     * relative to the center of the image, which is the parent node of the corners.
     */
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    void setImage(AugmentedImage image, int iImage) {
        this.image = image;
        this.iImage = iImage;

        // If any of the models are not loaded, then recurse when all are loaded.
        if (!ulCorner.isDone() || !urCorner.isDone() || !llCorner.isDone() || !lrCorner.isDone()) {
            CompletableFuture.allOf(ulCorner, urCorner, llCorner, lrCorner)
                    .thenAccept((Void aVoid) -> setImage(image, iImage))
                    .exceptionally(
                            throwable -> {
                                Log.e(TAG, "Exception loading", throwable);
                                return null;
                            });
        }

        initNode(image, iImage);
    }

    private void initNode(AugmentedImage image, int iImage) {
        setAnchor(image.createAnchor(image.getCenterPose()));
        float scaleWidth = image.getExtentX() / WIDTH_IMAGE;
        float scaleHeight = image.getExtentZ() / HEIGHTS_IMAGE[iImage];

        nodeCanvas = new Node();
        nodeCanvas.setParent(this);
        nodeCanvas.setRenderable(renderablesCanvas[iImage]);
        nodeCanvas.setLocalPosition(new Vector3(0, 0.0001f, 0));
        nodeCanvas.setLocalScale(new Vector3(scaleWidth, 1f, scaleHeight));
        setCanvasMaterial(iImage);
    }

    void toggleView(boolean on, boolean[] onChannels) {
        if (on) {
            // Center pose is lost when set parent to null
            initNode(image, iImage);
            setCanvasMaterial(iImage, onChannels);
            Log.i(TAG, "Resume rendering. New anchor set");
            nodeCanvas.setParent(this);
        } else {
            removeNode();
        }
    }

    void removeNode() {
        nodeCanvas.setParent(null);
    }

    private void setCanvasMaterial(int iImage) {
        if (nodeCanvas.getRenderable() != null) {
            setCanvasMaterial(materialsOri[iImage]);
        }
    }

    void setCanvasMaterial(int iImage, int iChannel) { // Valid indices -1 to 2
        if (nodeCanvas.getRenderable() != null) {
            if (iChannel == iAll) { //For channelState = None passed in
                setCanvasMaterial(iImage);
            } else {
                setCanvasMaterial(materialsChannel[iImage][iChannel]);
            }
        }
    }

    void setCanvasMaterial(int iImage, boolean[] onChannels) {
        int iChannel = getChannelsIndex(onChannels);
        setCanvasMaterial(iImage, iChannel);
    }

    private int getChannelsIndex(boolean[] onChannels) {
        if (onChannels[iRed]) {
            if (onChannels[iYellow]) {
                if (onChannels[iBlue]) {
                    return iAll;
                } else {
                    return iOrange;
                }
            } else {
                if (onChannels[iBlue]) {
                    return iPurple;
                } else {
                    return iRed;
                }
            }
        } else {
            if (onChannels[iYellow]) {
                if (onChannels[iBlue]) {
                    return iGreen;
                } else {
                    return iYellow;
                }
            } else {
                if (onChannels[iBlue]) {
                    return iBlue;
                } else {
                    return iAll;
                }
            }
        }
    }

    private void setCanvasMaterial(Material material) {
        Log.i(TAG, "setCanvasMaterial(Material) called. materialCurr= " + materialCurr
                + "; material= " + material);
        // For efficiency, not setting it to the same thing if aliased
        if (nodeCanvas.getRenderable() != null && materialCurr != material) {
            nodeCanvas.getRenderable().setMaterial(material);
            Log.i(TAG, "Material changed .");
        }
    }

    public AugmentedImage getImage() {
        return image;
    }
}
