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

package io.spine.examples.todolist.mode.menu;

import io.spine.examples.todolist.client.TodoClient;
import io.spine.examples.todolist.mode.InteractiveMode;
import io.spine.examples.todolist.mode.Mode;
import jline.console.ConsoleReader;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static io.spine.util.Exceptions.newIllegalStateException;

/**
 * @author Dmytro Grankin
 */
public class Menu extends InteractiveMode {

    private final List<AbstractMenuItem> items = newLinkedList();

    protected Menu(Builder builder) {
        super(builder.reader, builder.client);
        for (Map.Entry<String, Mode> mode : builder.modeMap.entrySet()) {
            addMenuItem(mode.getKey(), mode.getValue());
        }
        items.add(builder.menuExit);
    }

    @Override
    public void start() {
        display();
        final Optional<AbstractMenuItem> selectedItem = selectItem();

        if (!selectedItem.isPresent()) {
            println("Invalid selected item.");
        }

        selectedItem.get()
                    .start();
    }

    private void addMenuItem(String name, Mode item) {
        final MenuItem menuItem = new MenuItem(name, item, this);
        items.add(menuItem);
    }

    private void display() {
        println();
        for (AbstractMenuItem item : items) {
            final String itemAsString = toString(item);
            println(itemAsString);
        }
    }

    private Optional<AbstractMenuItem> selectItem() {
        final String answer = readLine();
        for (AbstractMenuItem item : items) {
            final String itemOption = getOptionForItem(item);
            if (itemOption.equals(answer)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    private String toString(AbstractMenuItem item) {
        final String option = getOptionForItem(item);
        final String name = item.getName();
        return option + ". " + name;
    }

    private String getOptionForItem(AbstractMenuItem item) {
        final int index = items.indexOf(item);

        if (index == -1) {
            throw newIllegalStateException("Specified menu item does not belong to the menu.");
        }

        return String.valueOf(index);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {

        private ConsoleReader reader;
        private TodoClient client;
        private MenuExit menuExit;
        private final Map<String, Mode> modeMap = newLinkedHashMap();

        public Builder setReader(ConsoleReader reader) {
            this.reader = reader;
            return this;
        }

        public Builder setClient(TodoClient client) {
            this.client = client;
            return this;
        }

        public Builder setMenuExit(String name) {
            this.menuExit = new MenuExit(name);
            return this;
        }

        public Builder addMenuItem(String name, Mode mode) {
            modeMap.put(name, mode);
            return this;
        }
    }
}
