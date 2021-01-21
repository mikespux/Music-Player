// Copyright 2018-2020 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.wachi.musicplayer.ui.activities;

import androidx.annotation.NonNull;

import com.mopub.common.Preconditions;
import com.mopub.common.SdkConfiguration;

public class SampleActivityUtils {
    public static void addDefaultNetworkConfiguration(@NonNull final SdkConfiguration.Builder builder) {
        Preconditions.checkNotNull(builder);

        // We have no default networks to initialize
    }
}
