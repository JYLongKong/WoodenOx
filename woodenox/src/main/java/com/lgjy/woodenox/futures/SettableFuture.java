/*
 * Copyright 2018 The Android Open Source Project
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

package com.lgjy.woodenox.futures;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

/**
 * Cloned from concurrent-futures package to avoid AndroidX namespace issues since there is no
 * supportlib 28.* equivalent of this class.
 *
 * A {@link ListenableFuture} whose result can be set by a {@link #set(Object)}, {@link
 * #setException(Throwable)} or {@link #setFuture(ListenableFuture)} call. It can also, like any
 * other {@code Future}, be {@linkplain #cancel cancelled}.
 *
 * If your needs are more complex than {@code SettableFuture} supports, use {@link
 * AbstractFuture}, which offers an extensible version of the API.
 *
 * @author Sven Mawson
 * @since 9.0 (in 1.0 as {@code ValueFuture})
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class SettableFuture<V> extends AbstractFuture<V> {
    /**
     * Creates a new {@code SettableFuture} that can be completed or cancelled by a later method
     * call.
     */
    public static <V> SettableFuture<V> create() {
        return new SettableFuture<V>();
    }

    @Override
    public boolean set(@Nullable V value) {
        return super.set(value);
    }

    @Override
    public boolean setException(Throwable throwable) {
        return super.setException(throwable);
    }

    @Override
    public boolean setFuture(ListenableFuture<? extends V> future) {
        return super.setFuture(future);
    }

    private SettableFuture() {
    }
}

