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
import android.util.Log;
import io.grpc.StatusRuntimeException;
import io.spine.examples.todolist.client.SubscribingTodoClient;
import io.spine.examples.todolist.connection.Clients;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.grpc.Status.Code.UNAVAILABLE;

public abstract class BaseViewModel extends ViewModel {

    private static final String TAG = BaseViewModel.class.getSimpleName();

    private final SubscribingTodoClient client = Clients.instance();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private ErrorObserver errorObserver;

    public BaseViewModel() {
    }

    void setErrorObserver(ErrorObserver errorObserver) {
        this.errorObserver = checkNotNull(errorObserver);
    }

    protected void execute(Runnable task) {
        checkNotNull(task);
        executor.execute(() -> {
            try {
                task.run();
            } catch (StatusRuntimeException e) {
                Log.d(TAG, "execute: error task");
                if (e.getStatus().getCode() == UNAVAILABLE && errorObserver != null) {
                    errorObserver.onNetworkError();
                } else {
                    throw e;
                }
            }
        });
    }

    protected SubscribingTodoClient client() {
        return client;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    public interface ErrorObserver {
        void onNetworkError();
    }
}
