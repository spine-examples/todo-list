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

let uuid = require("uuid");

let knownTypes =  require("../generated/main/js/index");

let UserId = require("spine-web/proto/spine/core/user_id_pb").UserId;
let spineWeb = require("spine-web/index");
let spineWebTypes = require('spine-web/proto/index');


let TaskStatus = require("../generated/main/js/todolist/attributes_pb").TaskStatus;
let TaskId = require("../generated/main/js/todolist/identifiers_pb").TaskId;
let TaskDescription = require("../generated/main/js/todolist/values_pb").TaskDescription;
let CreateBasicTask = require("../generated/main/js/todolist/commands_pb").CreateBasicTask;
let TaskView = require("../generated/main/js/todolist/views_pb").TaskView;

let firebase = require("./firebase_client.js");

const logSuccess = () => {
    console.log("Command sent");
};

const errorCallback = (error) => {
    console.error(error);
};

const HOST = "http://localhost:8080";
const ACTOR = "TodoList-actor";

/**
 * The client of the TodoList application.
 */
export class Client {

    constructor() {
        this._spineWebClient = spineWeb.init({
            protoIndexFiles: [knownTypes, spineWebTypes],
            endpointUrl: HOST,
            firebaseDatabase: firebase.application.database(),
            actorProvider: Client._actorProvider()
        });
        this._activeTasks = [];
    }

    /**
     * Creates a new basic task with the given description.
     *
     * @param description the description of the new task
     */
    submitNewTask(description) {
        let command = Client._createTaskCommand(description);
        this._spineWebClient.command(command)
                            .onOk(logSuccess)
                            .onError(errorCallback)
                            .onRejection(errorCallback)
                            .post();
    }

    /**
     * Renders the task list to the given view.
     *
     * The task list is dynamically updated based on the changes that arrive from the server.
     *
     * @param table the view to display the tasks in
     */
    renderTasksTo(table) {
        this.loadTasks(table);
        this.subscribeToTaskChanges(table);
    }

    /**
     * Loads all existing tasks and shows them at the given view.
     *
     * @param table the view to display the tasks in
     */
    loadTasks(table) {
        this._spineWebClient.select(TaskView)
                            .run()
                            .then(tasks => tasks.forEach(task => this._addToTable(table, task)))
    }

    /**
     * Subscribes to all task list changes on the server and displays them on the UI.
     *
     * @param table the view to display the tasks in
     */
    subscribeToTaskChanges(table) {
        this._spineWebClient.subscribeTo(TaskView)
                            .post()
                            .then(({itemAdded, itemChanged, itemRemoved, unsubscribe}) => {
                                itemAdded.subscribe(task => this._addToTable(table, task));
                                itemChanged.subscribe(task => this._update(table, task));
                                itemRemoved.subscribe(task => this._removeFromTable(table, task));
                            })
                            .catch(errorCallback);
    }

    /**
     * Adds a task view to the displayed tasks.
     *
     * Only adds "active" tasks, i.e. those that have `OPEN` or `FINALIZED` status.
     *
     * @private
     */
    _addToTable(table, taskView) {
        if (!taskView) {
            return;
        }
        let status = taskView.getStatus();
        let index = this._findIndex(taskView);
        let alreadyBroadcast = index > -1;
        if ((status === TaskStatus.OPEN || status === TaskStatus.FINALIZED) && !alreadyBroadcast) {
            this._activeTasks.push(taskView);
            this._refreshTasks(table);
        }
    }

    /**
     * Updates a task view within the table.
     *
     * If the given task is not present in the table, adds it.
     *
     * @private
     */
    _update(table, taskView) {
        if (!taskView) {
            return;
        }
        let index = this._findIndex(taskView);
        if (index > -1) {
            this._activeTasks[index] = taskView;
        } else {
            this._activeTasks.push(taskView);
        }
        this._refreshTasks(table);
    }

    /**
     * Removes a given task view from the displayed task list.
     *
     * If the task is not present in the list, does nothing.
     *
     * @private
     */
    _removeFromTable(table, taskView) {
        if (!taskView) {
            return;
        }
        let index = this._findIndex(taskView);
        if (index > -1) {
            this._activeTasks.splice(index, 1);
            this._refreshTasks(table);
        }
    }

    /**
     * Refreshes the table of displayed tasks.
     *
     * @private
     */
    _refreshTasks(table) {
        table.innerHTML = '';
        this._activeTasks.forEach(task => {
            let description = task.getDescription().getValue();
            table.innerHTML += `<div class="task_item">${description}</div>`;
        });
    }

    _findIndex(taskView) {
        let matchesById = task => task.getId().getUuid() === taskView.getId().getUuid();
        return this._activeTasks.findIndex(matchesById);
    }

    static _actorProvider() {
        let userId = new UserId();
        userId.setValue(ACTOR);
        return new spineWeb.ActorProvider(userId);
    }

    static _createTaskCommand(description) {
        let id = Client._newId();
        let descriptionValue = Client._description(description);

        let command = new CreateBasicTask();
        command.setId(id);
        command.setDescription(descriptionValue);

        return command;
    }

    static _description(value) {
        let result = new TaskDescription();
        result.setValue(value);
        return result;
    }

    static _newId() {
        let id = new TaskId();
        let value = uuid.v4();
        id.setUuid(value);
        return id;
    }
}
