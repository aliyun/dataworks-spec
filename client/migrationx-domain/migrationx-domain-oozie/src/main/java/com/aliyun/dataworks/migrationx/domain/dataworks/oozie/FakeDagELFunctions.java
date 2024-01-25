/*
 * Copyright (c) 2024, Alibaba Cloud;
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aliyun.dataworks.migrationx.domain.dataworks.oozie;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sam.liux
 * @date 2019/04/24 DAG EL functions.
 */
public class FakeDagELFunctions {
    /**
     * Return the job Id.
     *
     * @return the job Id.
     */
    public static String wf_id() {
        return "";
    }

    /**
     * Return the application name.
     *
     * @return the application name.
     */
    public static String wf_name() {
        return "";
    }

    /**
     * Return the application path.
     *
     * @return the application path.
     */
    public static String wf_appPath() {
        return "";
    }

    /**
     * Return a job configuration property.
     *
     * @param property property name.
     * @return the value of the property, <code>null</code> if the property is undefined.
     */
    public static String wf_conf(String property) {
        return "";
    }

    /**
     * Return the job owner user name.
     *
     * @return the job owner user name.
     */
    public static String wf_user() {
        return "test_user";
    }

    /**
     * Return the job owner group name.
     *
     * @return the job owner group name.
     */
    public static String wf_group() {
        return "";
    }

    /**
     * Create a callback URL for the current action.
     *
     * @param externalStatusVar variable for the caller to inject the external status.
     * @return the callback URL for the current action.
     */
    public static String wf_callback(String externalStatusVar) {
        return "";
    }

    /**
     * Return the transition taken by a workflow job action/decision action.
     *
     * @param actionName action/decision action name.
     * @return the transition taken, <code>null</code> if the action has not completed yet.
     */
    public static String wf_transition(String actionName) {
        return "";
    }

    /**
     * Return the name of the last action that ended in error.
     *
     * @return the name of the last action that ended in error, <code>null</code> if no action in the workflow job has
     * ended in error.
     */
    public static String wf_lastErrorNode() {
        return "";
    }

    /**
     * Return the error code for an action.
     *
     * @param actionName action name.
     * @return the error code for the action, <code>null</code> if the action has not ended in error.
     */
    public static String wf_errorCode(String actionName) {
        return "";
    }

    /**
     * Return the error message for an action.
     *
     * @param actionName action name.
     * @return the error message for the action, <code>null</code> if the action has not ended in error.
     */
    public static String wf_errorMessage(String actionName) {
        return "";
    }

    /**
     * Return the workflow run number, unless a rerun it is always 1.
     *
     * @return the workflow run number, unless a rerun it is always 1.
     */
    public static int wf_run() {
        return 0;
    }

    /**
     * Return the action data for an action.
     *
     * @param actionName action name.
     * @return value of the property.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> wf_actionData(String actionName) {
        return new HashMap<>();
    }

    /**
     * Return the external ID of an action.
     *
     * @param actionName action name.
     * @return the external ID of an action.
     */
    public static String wf_actionExternalId(String actionName) {
        return "";
    }

    /**
     * Return the tracker URI of an action.
     *
     * @param actionName action name.
     * @return the tracker URI of an action.
     */
    public static String wf_actionTrackerUri(String actionName) {
        return "";
    }

    /**
     * Return the action external status.
     *
     * @param actionName action/decision action name.
     * @return the action external status.
     */
    public static String wf_actionExternalStatus(String actionName) {
        return "";
    }

    public static String getActionVar(String actionName, String varName) {
        return "";
    }
}
