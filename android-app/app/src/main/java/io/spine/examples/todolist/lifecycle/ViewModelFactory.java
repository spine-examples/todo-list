/*
 * Copyright 2017, TeamDev Ltd. All rights reserved.
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.examples.todolist.lifecycle;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider.Factory;
import android.arch.lifecycle.ViewModelProvider.NewInstanceFactory;
import android.support.annotation.NonNull;

import java.util.Map;

import static com.google.common.collect.Maps.newConcurrentMap;

/**
 * An implementation of
 * {@link android.arch.lifecycle.ViewModelProvider.Factory ViewModelProvider.Factory}.
 *
 * <p>This implementation provides two basic options for the {@link ViewModel} instantiation:
 * <ul>
 *     <li>The {@link #ALWAYS_NEW} factory, which always creates new instances of {@link ViewModel}.
 *     <li>The {@link #CACHING} factory, which serves cached instances of {@code ViewModel} and
 *         creates new instances only if a cached instance for of the required type is unavailable.
 * </ul>
 *
 * <p>If several components should share the same instance of {@code ViewModel}, it is useful to use
 * the caching factory. And when the {@code ViewModel} should be reset, the non-caching factory
 * should be used.
 *
 * <p>Both {@link #ALWAYS_NEW} and {@link #CACHING} implementations always cache the resulting
 * {@code ViewModel}s, but only {@link #CACHING} uses the cache.
 *
 * <p>Each use of {@link #ALWAYS_NEW} factory resets the instance cache for the given
 * {@code ViewModel} type.
 *
 * <p>The {@link NewInstanceFactory} is used for the actual {@code ViewModel} instantiation.
 *
 * @author Dmytro Dashenkov
 */
public enum ViewModelFactory implements Factory {

    /**
     * A factory always creating new instances of {@link ViewModel}.
     */
    ALWAYS_NEW {
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            final T vm = delegate.create(modelClass);
            cache.put(modelClass, vm);
            return vm;
        }
    },

    /**
     * A factory serving cached instances of {@link ViewModel}.
     */
    CACHING {
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (cache.containsKey(modelClass)) {
                @SuppressWarnings("unchecked") // Logically checked.
                final T result = (T) cache.get(modelClass);
                return result;
            } else {
                return ALWAYS_NEW.create(modelClass);
            }
        }
    };

    private static final Map<Class, ViewModel> cache = newConcurrentMap();
    private static final Factory delegate = new NewInstanceFactory();

}
