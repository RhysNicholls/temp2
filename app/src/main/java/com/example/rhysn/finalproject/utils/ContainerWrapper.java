// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR 'CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.rhysn.finalproject.utils;

import android.support.annotation.NonNull;

import org.fourthline.cling.support.model.container.Container;

public class ContainerWrapper {
    public static String ROOT_CONTAINER_ID = "0";
    public static ContainerWrapper ROOT_CONTAINER = new ContainerWrapper(null, true);

    private final Container mContainer;
    private final boolean mIsRootContainer;

    public ContainerWrapper(@NonNull Container container) {
        this(container, false);
    }

    private ContainerWrapper(Container container, boolean isRootContainer) {
        mContainer = container;
        mIsRootContainer = isRootContainer;
    }

    public String getId() {
        return mIsRootContainer ? ROOT_CONTAINER_ID : mContainer.getId();
    }

    String getParentID() {
        return mIsRootContainer ? "" : mContainer.getParentID();
    }

    public String getTitle() {
        if (mIsRootContainer)
            throw new UnsupportedOperationException("The root container does not have a title");
        return mContainer.getTitle();
    }
}
