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

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.Visualization_of_Color_Theory.helpers.SnackbarHelper;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Extend the ArFragment to customize the ARCore session configuration to include Augmented Images.
 */
public class AugmentedImageFragment extends ArFragment {
    private static final String TAG = "AugmentedImageFragment";
    static final String
            PATH_PROJECT = "D:/UMD/Research/Visualization-of-Color-Theory-2.0/AndroidProject",
            FOLDER_IMAGES = "images",
            FOLDER_ASSETS = "app/src/main/assets",
            PATH_ASSETS = PATH_PROJECT + "/" + FOLDER_ASSETS;
    static final String[] NAMES_IMAGE = new String[]
            {"Forest.jpg", "Reflection.jpg", "Still Life.jpg"};

    // This is the name of the image in the sample database.  A copy of the image is in the assets
    // directory.  Opening this image on your computer is a good quick way to test the augmented image
    // matching.
    private static final String DEFAULT_IMAGE_NAME = "default.jpg";

    // This is a pre-created database containing the sample image.
    private static final String SAMPLE_IMAGE_DATABASE = "sample_database.imgdb";

    // Augmented image configuration and rendering.
    // Load a single image (true) or a pre-generated image database (false).
    private static final boolean isAddImageToDatabase = true;

    // Do a runtime check for the OpenGL level available at runtime to avoid Sceneform crashing the
    // application.
    private static final double MIN_OPENGL_VERSION = 3.0;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        String openGlVersionString =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 or later");
            SnackbarHelper.getInstance()
                    .showError(getActivity(), "Sceneform requires OpenGL ES 3.0 or later");
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // Turn off the plane discovery since we're only looking for images
        getPlaneDiscoveryController().hide();
        getPlaneDiscoveryController().setInstructionView(null);
        getArSceneView().getPlaneRenderer().setEnabled(false);
        return view;
    }

    @Override
    protected Config getSessionConfiguration(Session session) {
        Config config = super.getSessionConfiguration(session);

        config.setFocusMode(Config.FocusMode.AUTO);

        if (!setupAugmentedImageDatabase(config, session)) {
            SnackbarHelper.getInstance()
                    .showError(getActivity(), "Could not setup augmented image database");
        }
        return config;
    }

    private boolean setupAugmentedImageDatabase(Config config, Session session) {
        AugmentedImageDatabase augmentedImageDatabase;
        AssetManager assetManager = getContext() != null ? getContext().getAssets() : null;
        if (assetManager == null) {
            Log.e(TAG, "Context is null, cannot intitialize image database.");
            return false;
        }
        // There are two ways to configure an AugmentedImageDatabase:
        // 1. Add Bitmap to DB directly
        // 2. Load a pre-built AugmentedImageDatabase
        // Option 2) has
        // * shorter setup time
        // * doesn't require images to be packaged in apk.
        if (isAddImageToDatabase) {
            Bitmap[] bitmapsAugmentedImage = loadAugmentedImageBitmaps(assetManager, NAMES_IMAGE);
            for (Bitmap bitmap : bitmapsAugmentedImage) {
                if (bitmap == null) {
                    return false;
                }
            }
            augmentedImageDatabase = new AugmentedImageDatabase(session);
            for (int i = 0; i < NAMES_IMAGE.length; i++) {
                augmentedImageDatabase.addImage(NAMES_IMAGE[i], bitmapsAugmentedImage[i]);
            }
            // If the physical size of the image is known, you can instead use:
            //     augmentedImageDatabase.addImage("image_name", augmentedImageBitmap, widthInMeters);
            // This will improve the initial detection speed. ARCore will still actively estimate the
            // physical size of the image as it is viewed from multiple viewpoints.
            serializeAugmentedImageDatabase(augmentedImageDatabase, "paintings.imgdb");
            // This is an alternative way to initlialize an AugmentedImageDatabase instance,
            // load a pre-existing augmented image database.
        } else {
            try (InputStream is = getContext().getAssets().open(SAMPLE_IMAGE_DATABASE)) {
                augmentedImageDatabase = AugmentedImageDatabase.deserialize(session, is);
            } catch (IOException e) {
                Log.e(TAG, "IO exception loading augmented image database.", e);
                return false;
            }
        }
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }

    private Bitmap loadAugmentedImageBitmaps(AssetManager assetManager) {
        return loadAugmentedImageBitmap(assetManager, DEFAULT_IMAGE_NAME);
    }

    private Bitmap loadAugmentedImageBitmap(AssetManager assetManager, String nameImage) {
        try (InputStream is = assetManager.open(nameImage)) {
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            Log.e(TAG, "IO exception loading augmented image bitmap.", e);
        }
        return null;
    }


    private Bitmap[] loadAugmentedImageBitmaps(AssetManager assetManager, String[] namesImage) {
        Bitmap[] bitmaps = new Bitmap[namesImage.length];
        for (int i = 0; i < namesImage.length; i++) {
            bitmaps[i] = loadAugmentedImageBitmap(
                    assetManager, FOLDER_IMAGES + "/" + namesImage[i]);
        }
        return bitmaps;
    }

    private boolean serializeAugmentedImageDatabase(AugmentedImageDatabase database,
                                                    String nameDatabase) {
        try (OutputStream outputStream = new FileOutputStream(
                PATH_ASSETS + "/" + nameDatabase)) {
            database.serialize(outputStream);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
