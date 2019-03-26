/*
 * Copyright 2019, TeamDev. All rights reserved.
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

import {Pipe, PipeTransform} from '@angular/core';

import {LabelColor} from 'generated/main/js/todolist/attributes_pb';

/**
 * Obtains the string value of the `LabelColor` for proper display in the HTML page.
 *
 * Usage:
 *   label.getColor() | labelColorView
 *
 * Example:
 *   {{ LabelColor.RED | labelColorView }}
 *   resolves to: '#ff0000'
 */
@Pipe({
  name: 'labelColorView'
})
export class LabelColorView implements PipeTransform {

  private static readonly COLORS: Map<LabelColor, string> = new Map([
    [LabelColor.RED, '#ff0000'],
    [LabelColor.GREEN, '#008000'],
    [LabelColor.BLUE, '#0000ff'],
    [LabelColor.GRAY, '#808080']
  ]);

  /**
   * Does `LabelColor`-to-string transformation.
   *
   * In case unknown `LabelColor` is specified, throws an error.
   */
  transform(value: LabelColor): string {
    const color = LabelColorView.COLORS.get(value);
    if (!color) {
      throw new Error(`There is no known string representation for color ${value}`);
    }
    return color;
  }
}
