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

package io.spine.examples.todolist.newtask;

import android.support.v4.app.Fragment;

/**
 * The abstract base for the {@link Fragment}s used as {@code ViewPager} pages.
 *
 * @author Dmytro Dashenkov
 */
abstract class PagerFragment extends Fragment {

    /**
     * Performs the preparation for the fragment to be opened.
     *
     * <p>This method is called when the current fragment is to be opened. Prefer this method to
     * the {@link Fragment} lifecycle methods in order to prepare the page just before it's opened.
     *
     * <p>By default, the method performs no action. Since not all the fragments need to have
     * a preparation stage, the method is left non-abstract.
     */
    @SuppressWarnings("NoopMethodInAbstractClass") // NoOp by default.
    void prepare() {}

    /**
     * Performs the completion operations when the page is closed.
     *
     * <p>This method is called when the user navigates to the next page of the {@code ViewPager}.
     */
    abstract void complete();
}
