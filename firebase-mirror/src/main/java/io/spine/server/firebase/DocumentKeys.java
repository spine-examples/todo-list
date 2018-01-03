/*
 * Copyright 2018, TeamDev Ltd. All rights reserved.
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

package io.spine.server.firebase;

import com.google.common.base.CharMatcher;

import java.util.regex.Pattern;

import static com.google.common.base.CharMatcher.is;
import static com.google.common.base.Preconditions.checkArgument;
import static java.util.regex.Pattern.compile;

/**
 * A utility for working with the Firestore document/collection keys.
 *
 * @author Dmytro Dashenkov
 */
final class DocumentKeys {

    private static final Pattern INVALID_KEY_CHARS = compile("[^\\w\\d]");
    private static final char INVALID_KEY_LEADING_CHAR = '_';

    /**
     * The {@code private} constructor prevents the utility class instantiation.
     */
    private DocumentKeys() {}

    /**
     * Transforms the given string to a <a target="_blank"
     * href="https://firebase.google.com/docs/firestore/quotas">valid^</a> Firestore key.
     *
     * <p>A valid key should contain only English letters, digits and underscore symbols and NOT
     * start with an underscore.
     *
     * <p>All the illegal chars are deleted from the source string. If no characters are left after
     * this operation, an {@link IllegalArgumentException} is thrown.
     *
     * @param dirtyKey the string to transform
     * @return a valid Firestore key
     */
    static String escape(String dirtyKey) {
        final String trimmedKey = trimUnderscore(dirtyKey);
        final String result = INVALID_KEY_CHARS.matcher(trimmedKey)
                                               .replaceAll("");
        checkArgument(!result.isEmpty(), "Key `%s` is invalid.", dirtyKey);
        return result;
    }

    private static String trimUnderscore(String key) {
        final CharMatcher matcher = is(INVALID_KEY_LEADING_CHAR);
        final String trimmed = matcher.trimLeadingFrom(key);
        return trimmed;
    }
}
