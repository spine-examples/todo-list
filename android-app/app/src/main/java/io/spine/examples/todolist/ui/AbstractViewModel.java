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

package io.spine.examples.todolist.ui;

import android.arch.lifecycle.ViewModel;
import android.os.Handler;
import android.os.Looper;
import io.grpc.StatusRuntimeException;
import io.spine.examples.todolist.c.commands.TodoCommand;
import io.spine.examples.todolist.client.Clients;
import io.spine.examples.todolist.client.SubscribingTodoClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.grpc.Status.Code.UNAVAILABLE;

/**
 * An implementation base for all the {@code ViewModel}s in the app.
 *
 * <p>The {@code ViewModel} classes hold data, perform network connections, etc. on the behalf of
 * the associated view elements (such as {@code Activity}).
 */
@SuppressWarnings("AbstractClassWithoutAbstractMethods") // Prevent direct instantiation.
public abstract class AbstractViewModel extends ViewModel {

    /**
     * The TodoList gRPC client.
     */
    private final SubscribingTodoClient client = Clients.subscribingInstance();

    /**
     * The {@link ExecutorService} performing the asynchronous operations, such as networking.
     *
     * @see #execute(Runnable) for the application
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * A callback notifying the view about the errors.
     */
    private ErrorCallback errorCallback;

    /**
     * Creates an instance of {@code AbstractViewModel}.
     *
     * <p>A {@code public} constructor is required by the Android framework. Do not instantiate
     * this class directly.
     *
     * @see AbstractActivity#model() for instance access
     */
    public AbstractViewModel() {}

    /**
     * Sets the {@code errorCallback} to react on the errors.
     *
     * <p>The thread of the callback invocation is unknown.
     */
    void setErrorCallback(ErrorCallback errorCallback) {
        this.errorCallback = checkNotNull(errorCallback);
    }

    /**
     * Executes the given {@code task} asynchronously.
     *
     * <p>If an error happened during the {@code task} execution it may be either sent to
     * the {@link #errorCallback} if it is <i>supported</i> by the callback, or rethrown otherwise.
     *
     * @param task the operation to execute
     * @see ErrorCallback for the list of <i>supported</i> errors
     */
    protected void execute(Runnable task) {
        checkNotNull(task);
        executor.execute(() -> {
            try {
                task.run();
            } catch (StatusRuntimeException e) {
                if (e.getStatus()
                     .getCode() == UNAVAILABLE && errorCallback != null) {
                    errorCallback.onNetworkError();
                } else {
                    throw e;
                }
            }
        });
    }

    /**
     * Performs the given operation in the main thread (a.k.a. UI thread).
     *
     * <p>The method does not await the operation, but only schedules the execution.
     *
     * @param task the operation to perform in the main thread
     */
    protected void inMainThread(Runnable task) {
        checkNotNull(task);
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(task);
    }

    protected void post(TodoCommand command) {
        execute(() -> client.postCommand(command));
    }

    /**
     * @return the TodoList client
     */
    protected SubscribingTodoClient client() {
        return client;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Shuts the {@link #executor} down.
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    /**
     * The callback called upon some errors in the {@code ViewModel}.
     *
     * <p>Currently, only one type of error is <i>supported</i> - the gRPC network error, a.k.a
     * {@link StatusRuntimeException} with the {@code Status.Code.UNAVAILABLE} status code.
     */
    public interface ErrorCallback {

        /**
         * The callback called upon the service unavailable error.
         */
        void onNetworkError();
    }
}
