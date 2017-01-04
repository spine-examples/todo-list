/*
 * Copyright 2016, TeamDev Ltd. All rights reserved.
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

package org.spine3.examples.todolist.projection;

import org.spine3.examples.todolist.LabelColor;

/**
 * Supplies {@link LabelColor} with hexadecimal representation of color.
 */
/* package */ enum LabelColorView {
    RED_COLOR(LabelColor.RED, "#ff0000"),
    BLUE_COLOR(LabelColor.BLUE, "#0000ff"),
    GREEN_COLOR(LabelColor.GREEN, "#008000"),
    GRAY_COLOR(LabelColor.GRAY, "#808080");

    private static final String WRONG_LABEL_COLOR_EXCEPTION_MESSAGE = "No enum constant by specified label color: ";

    private final LabelColor labelColor;
    private final String hexColor;

    LabelColorView(LabelColor labelColor, String hexColor) {
        this.labelColor = labelColor;
        this.hexColor = hexColor;
    }

    /* package */ static String valueOf(LabelColor labelColor) {
        for (LabelColorView colorView : values()) {
            if (colorView.labelColor == labelColor) {
                return colorView.hexColor;
            }
        }
        throw new IllegalArgumentException(WRONG_LABEL_COLOR_EXCEPTION_MESSAGE + labelColor);
    }
}
