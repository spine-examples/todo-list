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

package io.spine.examples.todolist.model;

import android.graphics.Color;
import com.google.common.collect.ImmutableMap;
import io.spine.examples.todolist.LabelColor;

import java.lang.reflect.Field;
import java.util.Map;

import static io.spine.examples.todolist.LabelColor.GRAY;

public final class Colors {

    /**
     * The map of the {@link LabelColor} enum to the RGB values of the color.
     */
    private static final Map<LabelColor, Integer> COLORS;

    static {
        final int defaultColor = Color.GRAY;
        final Class<Color> colorConstants = Color.class;
        final ImmutableMap.Builder<LabelColor, Integer> colors = ImmutableMap.builder();
        for (LabelColor color : LabelColor.values()) {
            final String colorName = color.name();
            try {
                final Field field = colorConstants.getField(colorName);
                final int colorCode = (Integer) field.get(null);
                colors.put(color, colorCode);
            } catch (NoSuchFieldException ignored) {
                colors.put(color, defaultColor);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        COLORS = colors.build();
    }

    /** Prevent utility class instantiation */
    private Colors() {
    }

    public static int toRgb(LabelColor color) {
        Integer hex = COLORS.get(color);
        if (hex == null) {
            hex = COLORS.get(GRAY);
        }
        return hex;
    }
}
