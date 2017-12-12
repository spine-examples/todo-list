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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static java.lang.String.valueOf;

final class WizardAdapter extends FragmentPagerAdapter {

    private final FragmentFactory factory = new FragmentFactory();

    WizardAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public PagerFragment getItem(int position) {
        final PagerFragment result = factory.get(position);
        return result;
    }

    @Override
    public int getCount() {
        return FragmentFactory.FRAGMENTS_MAX_COUNT;
    }

    private static final class FragmentFactory {

        private static final int FRAGMENTS_MAX_COUNT = 3;

        private final Map<Integer, PagerFragment> fragments =
                newHashMapWithExpectedSize(FRAGMENTS_MAX_COUNT);

        private PagerFragment get(int position) {
            final PagerFragment cached = fragments.get(position);
            if (cached != null) {
                return cached;
            }
            final PagerFragment result = create(position);
            fragments.put(position, result);
            return result;
        }

        private static PagerFragment create(int position) {
            final PagerFragment result;
            switch (position) {
                case NewTaskDescriptionFragment.POSITION_IN_WIZARD:
                    result = new NewTaskDescriptionFragment();
                    break;
                case NewTaskLabelsFragment.POSITION_IN_WIZARD:
                    result = new NewTaskLabelsFragment();
                    break;
                case NewTaskConfirmationFragment.POSITION_IN_WIZARD:
                    result = new NewTaskConfirmationFragment();
                    break;
                default:
                    throw new IndexOutOfBoundsException(valueOf(position));
            }
            return result;
        }
    }
}
