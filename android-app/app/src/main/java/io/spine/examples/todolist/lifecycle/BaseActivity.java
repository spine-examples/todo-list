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

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import io.spine.examples.todolist.R;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static com.google.common.base.Preconditions.checkState;

/**
 * The implementation base for all the {@link android.app.Activity Activity} classes of the app.
 *
 * <p>Any {@code Activity} in the app has an associated {@link BaseViewModel ViewModel} instance.
 *
 * @param <VM> the type of the {@code BaseViewModel} associated with this {@code Activity}
 */
public abstract class BaseActivity<VM extends BaseViewModel> extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    private final Navigator navigator;
    private VM model;

    private Toolbar toolbar;

    /**
     * Creates an instance of {@code BaseActivity}.
     *
     * <p>A {@code public} constructor is required by the Android framework. Do not instantiate
     * this class directly.
     */
    @SuppressWarnings({
            "ThisEscapedInObjectConstruction",
                // OK since is the last statement in the constructor.
            "ConstructorNotProtectedInAbstractClass"
                // `public` ctor is required for each implementation.
    })
    public BaseActivity() {
        super();
        this.navigator = Navigator.from(this);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The {@code BaseActivity} implementation defines the generic flow for the activities
     * to be initialized:
     * <ol>
     *     <li>Associates an instance of {@link BaseViewModel} with this {@code Activity}.
     *     <li>{@linkplain #setContentView(View) Sets the content view} to this {@code Activity}.
     *     <li>{@linkplain #initToolbar() Initializes} the screen toolbar.
     *     <li>{@linkplain #initializeView() Initializes} the views upon this {@code Activity}.
     * </ol>
     */
    @OverridingMethodsMustInvokeSuper
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(this).get(getViewModelClass());
        setContentView(getContentViewResource());
        initToolbar();
        initializeView();
        model.setErrorCallback(this::reportNetworkFailure);
    }

    /**
     * {@inheritDoc}
     *
     * <p>If this {@code Activity} is not {@linkplain #isTaskRoot() the last one in the stack},
     * overrides the closing animation setting it to slide right.
     */
    @Override
    public void finish() {
        super.finish();
        if (!isTaskRoot()) {
            overridePendingTransition(0, R.anim.slide_right);
        }
    }

    /**
     * Retrieves the resource ID for the title displayed in this {@code Activity} action bar.
     *
     * <p>By default, returns the ID of the application name string. Override this method to change
     * the title.
     *
     * @return the string resource ID for the {@code Activity} title
     */
    @StringRes
    protected int getTitleResource() {
        return R.string.app_name;
    }

    /**
     * Retrieves the class of the {@link BaseViewModel} associated with this {@code Activity}.
     *
     * @return the class of {@code VM}
     */
    protected abstract Class<VM> getViewModelClass();

    /**
     * Retrieves the layout ID for the view put into the root of this {@code Activity} on
     * initialization.
     *
     * <p>The references layout should contain a {@link Toolbar} with ID {@code R.id.toolbar}.
     *
     * @return the {@code Activity} content view
     */
    @LayoutRes
    protected abstract int getContentViewResource();

    /**
     * Initializes the views upon this {@code Activity}.
     *
     * <p>This method is called upon the {@code Activity} {@linkplain #onCreate(Bundle) creation}.
     * Initialize the UI in this method rather then in {@code onCreate()} method.
     */
    protected abstract void initializeView();

    /**
     * Retrieves the {@linkplain BaseViewModel} associated with this {@code Activity}.
     *
     * <p>Make sure to call this method <b>after</b>
     * the {@link #onCreate(Bundle) Activity.onCreate(Bundle)} method, or an
     * {@link IllegalStateException} may be thrown.
     *
     * @return an instance of {@code VM}
     */
    protected final VM model() {
        checkState(model != null, "ViewModel accessed before onCreate()");
        return model;
    }

    /**
     * @return an instance of {@link Navigator} for navigating over the application screens
     */
    protected final Navigator navigator() {
        return navigator;
    }

    /**
     * Shows a {@link Snackbar} with a network error message.
     */
    protected void reportNetworkFailure() {
        Snackbar.make(toolbar, R.string.network_err_msg, LENGTH_LONG).show();
    }

    /**
     * Initializes the screen action bar as the {@link Toolbar} with ID {@code R.id.toolbar}.
     */
    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getTitleResource());
        } else {
            Log.w(TAG, "initToolbar: No support ActionBar present");
        }
    }
}
