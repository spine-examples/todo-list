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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import io.spine.examples.todolist.R;

import static com.google.common.base.Preconditions.checkState;

public abstract class BaseActivity<VM extends BaseViewModel> extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    private final Navigator navigator;
    private VM model;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(this).get(getViewModelClass());
        setContentView(getContentView());
        initToolbar();
        initializeView();
    }

    protected abstract Class<VM> getViewModelClass();

    @LayoutRes
    protected abstract int getContentView();

    protected abstract void initializeView();

    protected final VM model() {
        checkState(model != null, "ViewModel accessed before onCreate()");
        return model;
    }

    protected final Navigator navigator() {
        return navigator;
    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        } else {
            Log.w(TAG, "initToolbar: No support ActionBar present");
        }
    }
}
