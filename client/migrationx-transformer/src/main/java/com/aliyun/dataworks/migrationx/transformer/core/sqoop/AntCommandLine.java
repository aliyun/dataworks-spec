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

package com.aliyun.dataworks.migrationx.transformer.core.sqoop;

import com.alibaba.fastjson.JSON;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.taskdefs.condition.Os;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

/**
 * Commandline objects help handling command lines specifying processes to execute.
 * <p>
 * The class can be used to define a command line as nested elements or as a helper to define a command line by an
 * application.
 * </p>
 * <pre>
 * &lt;someelement&gt;
 *   &lt;acommandline executable="/executable/to/run"&gt;
 *     &lt;argument value="argument 1"/&gt;
 *     &lt;argument line="argument_1 argument_2 argument_3"/&gt;
 *     &lt;argument value="argument 4"/&gt;
 *   &lt;/acommandline&gt;
 * &lt;/someelement&gt;
 * </pre>
 * The element <code>someelement</code> must provide a method
 * <code>createAcommandline</code> which returns an instance of this class.
 */
public class AntCommandLine implements Cloneable {
    /**
     * win9x uses a (shudder) bat file (antRun.bat) for executing commands
     */
    private static final boolean IS_WIN_9X = Os.isFamily("win9x");

    /**
     * The arguments of the command
     */
    private List<Argument> arguments = new ArrayList<>();

    /**
     * the program to execute
     */
    private String executable = null;

    protected static final String DISCLAIMER = String.format(
        "%nThe ' characters around the executable and arguments are%nnot part of the command.%n");

    /**
     * Create a command line from a string.
     *
     * @param toProcess the line: the first element becomes the executable, the rest the arguments.
     */
    public AntCommandLine(String toProcess) {
        super();
        String[] tmp = translateCommandline(toProcess);
        if (tmp != null && tmp.length > 0) {
            setExecutable(tmp[0]);
            for (int i = 1; i < tmp.length; i++) {
                createArgument().setValue(tmp[i]);
            }
        }
    }

    /**
     * Create an empty command line.
     */
    public AntCommandLine() {
        super();
    }

    /**
     * Used for nested xml command line definitions.
     */
    public static class Argument extends ProjectComponent {

        private String[] parts;

        private String prefix = "";
        private String suffix = "";

        /**
         * Set a single commandline argument.
         *
         * @param value a single commandline argument.
         */
        public void setValue(String value) {
            parts = new String[] {value};
        }

        /**
         * Set the line to split into several commandline arguments.
         *
         * @param line line to split into several commandline arguments.
         */
        public void setLine(String line) {
            if (line == null) {
                return;
            }
            parts = translateCommandline(line);
        }

        /**
         * Set a single commandline argument and treats it like a
         * PATH--ensuring the right separator for the local platform
         * is used.
         *
         * @param value a single commandline argument.
         */
        //        public void setPath(Path value) {
        //            parts = new String[] {value.toString()};
        //        }

        /**
         * Set a single commandline argument from a reference to a
         * path--ensuring the right separator for the local platform
         * is used.
         *
         * @param value a single commandline argument.
         */
        //        public void setPathref(Reference value) {
        //            Path p = new Path(getProject());
        //            p.setRefid(value);
        //            parts = new String[] {p.toString()};
        //        }

        /**
         * Set a single commandline argument to the absolute filename of the given file.
         *
         * @param value a single commandline argument.
         */
        public void setFile(File value) {
            parts = new String[] {value.getAbsolutePath()};
        }

        /**
         * Set the prefix to be placed in front of every part of the argument.
         *
         * @param prefix fixed prefix string.
         * @since Ant 1.8.0
         */
        public void setPrefix(String prefix) {
            this.prefix = prefix != null ? prefix : "";
        }

        /**
         * Set the suffix to be placed at the end of every part of the argument.
         *
         * @param suffix fixed suffix string.
         * @since Ant 1.8.0
         */
        public void setSuffix(String suffix) {
            this.suffix = suffix != null ? suffix : "";
        }

        /**
         * Copies settings from a different argument.
         *
         * @param other the argument to copy setting from
         * @since Ant 1.10.6
         */
        public void copyFrom(Argument other) {
            this.parts = other.parts;
            this.prefix = other.prefix;
            this.suffix = other.suffix;
        }

        /**
         * Return the constituent parts of this Argument.
         *
         * @return an array of strings.
         */
        public String[] getParts() {
            if (parts == null || parts.length == 0 || (prefix.isEmpty() && suffix.isEmpty())) {
                return parts;
            }
            String[] fullParts = new String[parts.length];
            for (int i = 0; i < fullParts.length; ++i) {
                fullParts[i] = prefix + parts[i] + suffix;
            }
            return fullParts;
        }
    }

    /**
     * Class to keep track of the position of an Argument.
     *
     * <p>This class is there to support the srcfile and targetfile
     * elements of &lt;apply&gt;.</p>
     */
    public class Marker {

        private int position;
        private int realPos = -1;
        private String prefix = "";
        private String suffix = "";

        /**
         * Construct a marker for the specified position.
         *
         * @param position the position to mark.
         */
        Marker(int position) {
            this.position = position;
        }

        /**
         * Return the number of arguments that preceded this marker.
         *
         * <p>The name of the executable -- if set -- is counted as the
         * first argument.</p>
         *
         * @return the position of this marker.
         */
        public int getPosition() {
            if (realPos == -1) {
                realPos = (executable == null ? 0 : 1)
                    + (int)arguments.stream().limit(position)
                    .map(Argument::getParts).flatMap(Stream::of).count();
            }
            return realPos;
        }

        /**
         * Set the prefix to be placed in front of the inserted argument.
         *
         * @param prefix fixed prefix string.
         * @since Ant 1.8.0
         */
        public void setPrefix(String prefix) {
            this.prefix = prefix != null ? prefix : "";
        }

        /**
         * Get the prefix to be placed in front of the inserted argument.
         *
         * @return String
         * @since Ant 1.8.0
         */
        public String getPrefix() {
            return prefix;
        }

        /**
         * Set the suffix to be placed at the end of the inserted argument.
         *
         * @param suffix fixed suffix string.
         * @since Ant 1.8.0
         */
        public void setSuffix(String suffix) {
            this.suffix = suffix != null ? suffix : "";
        }

        /**
         * Get the suffix to be placed at the end of the inserted argument.
         *
         * @return String
         * @since Ant 1.8.0
         */
        public String getSuffix() {
            return suffix;
        }

    }

    /**
     * Create an argument object.
     *
     * <p>Each commandline object has at most one instance of the
     * argument class.  This method calls
     * <code>this.createArgument(false)</code>.</p>
     *
     * @return the argument object.
     * @see #createArgument(boolean)
     */
    public Argument createArgument() {
        return this.createArgument(false);
    }

    /**
     * Create an argument object and add it to our list of args.
     *
     * <p>Each commandline object has at most one instance of the
     * argument class.</p>
     *
     * @param insertAtStart if true, the argument is inserted at the beginning of the list of args, otherwise it is
     *                      appended.
     * @return an argument to be configured
     */
    public Argument createArgument(boolean insertAtStart) {
        Argument argument = new Argument();
        if (insertAtStart) {
            arguments.add(0, argument);
        } else {
            arguments.add(argument);
        }
        return argument;
    }

    /**
     * Set the executable to run. All file separators in the string are converted to the platform specific value.
     *
     * @param executable the String executable name.
     */
    public void setExecutable(String executable) {
        setExecutable(executable, true);
    }

    /**
     * Set the executable to run.
     *
     * @param executable             the String executable name.
     * @param translateFileSeparator if {@code true} all file separators in the string are converted to the platform
     *                               specific value.
     * @since Ant 1.9.7
     */
    public void setExecutable(String executable, boolean translateFileSeparator) {
        if (executable == null || executable.isEmpty()) {
            return;
        }
        this.executable = translateFileSeparator
            ? executable.replace('/', File.separatorChar).replace('\\', File.separatorChar)
            : executable;
    }

    /**
     * Get the executable.
     *
     * @return the program to run--null if not yet set.
     */
    public String getExecutable() {
        return executable;
    }

    /**
     * Append the arguments to the existing command.
     *
     * @param line an array of arguments to append.
     */
    public void addArguments(String[] line) {
        for (String l : line) {
            createArgument().setValue(l);
        }
    }

    /**
     * Return the executable and all defined arguments.
     *
     * @return the commandline as an array of strings.
     */
    public String[] getCommandline() {
        final List<String> commands = new LinkedList<>();
        addCommandToList(commands.listIterator());
        return commands.toArray(new String[commands.size()]);
    }

    /**
     * Add the entire command, including (optional) executable to a list.
     *
     * @param list the list to add to.
     * @since Ant 1.6
     */
    public void addCommandToList(ListIterator<String> list) {
        if (executable != null) {
            list.add(executable);
        }
        addArgumentsToList(list);
    }

    /**
     * Returns all arguments defined by <code>addLine</code>,
     * <code>addValue</code> or the argument object.
     *
     * @return the arguments as an array of strings.
     */
    public String[] getArguments() {
        List<String> result = new ArrayList<>(arguments.size() * 2);
        addArgumentsToList(result.listIterator());
        return result.toArray(new String[result.size()]);
    }

    /**
     * Append all the arguments to the tail of a supplied list.
     *
     * @param list the list of arguments.
     * @since Ant 1.6
     */
    public void addArgumentsToList(ListIterator<String> list) {
        for (Argument arg : arguments) {
            String[] s = arg.getParts();
            if (s != null) {
                for (String value : s) {
                    list.add(value);
                }
            }
        }
    }

    /**
     * Return the command line as a string.
     *
     * @return the command line.
     */
    @Override
    public String toString() {
        return toString(getCommandline());
    }

    /**
     * Put quotes around the given String if necessary.
     *
     * <p>If the argument doesn't include spaces or quotes, return it
     * as is. If it contains double quotes, use single quotes - else surround the argument by double quotes.</p>
     *
     * @param argument the argument to quote if necessary.
     * @return the quoted argument.
     * @throws BuildException if the argument contains both, single and double quotes.
     */
    public static String quoteArgument(String argument) {
        if (argument.contains("\"")) {
            if (argument.contains("\'")) {
                throw new BuildException("Can\'t handle single and double"
                    + " quotes in same argument");
            }
            return '\'' + argument + '\'';
        }
        if (argument.contains("\'") || argument.contains(" ")
            // WIN9x uses a bat file for executing commands
            || (IS_WIN_9X && argument.contains(";"))) {
            return '\"' + argument + '\"';
        }
        return argument;
    }

    /**
     * Quote the parts of the given array in way that makes them usable as command line arguments.
     *
     * @param line the list of arguments to quote.
     * @return empty string for null or no command, else every argument split by spaces and quoted by quoting rules.
     */
    public static String toString(String[] line) {
        // empty path return empty string
        if (line == null || line.length == 0) {
            return "";
        }
        // path containing one or more elements
        final StringBuilder result = new StringBuilder();
        for (String l : line) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(quoteArgument(l));
        }
        return result.toString();
    }

    /**
     * Crack a command line.
     *
     * @param toProcess the command line to process.
     * @return the command line broken into strings. An empty or null toProcess parameter results in a zero sized array.
     */
    public static String[] translateCommandline(String toProcess) {
        if (toProcess == null || toProcess.isEmpty()) {
            // no command? no string
            return new String[0];
        }
        // parse with a simple finite state machine

        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        int state = normal;
        final StringTokenizer tok = new StringTokenizer(toProcess, "\"\' ", true);
        final ArrayList<String> result = new ArrayList<>();
        final StringBuilder current = new StringBuilder();
        boolean lastTokenHasBeenQuoted = false;

        while (tok.hasMoreTokens()) {
            String nextTok = tok.nextToken();
            switch (state) {
                case inQuote:
                    if ("\'".equals(nextTok)) {
                        lastTokenHasBeenQuoted = true;
                        state = normal;
                    } else {
                        current.append(nextTok);
                    }
                    break;
                case inDoubleQuote:
                    if ("\"".equals(nextTok)) {
                        lastTokenHasBeenQuoted = true;
                        state = normal;
                    } else {
                        current.append(nextTok);
                    }
                    break;
                default:
                    if ("\'".equals(nextTok)) {
                        state = inQuote;
                    } else if ("\"".equals(nextTok)) {
                        state = inDoubleQuote;
                    } else if (" ".equals(nextTok)) {
                        if (lastTokenHasBeenQuoted || current.length() > 0) {
                            result.add(current.toString());
                            current.setLength(0);
                        }
                    } else {
                        current.append(nextTok);
                    }
                    lastTokenHasBeenQuoted = false;
                    break;
            }
        }
        if (lastTokenHasBeenQuoted || current.length() > 0) {
            result.add(current.toString());
        }
        if (state == inQuote || state == inDoubleQuote) {
            throw new BuildException("unbalanced quotes in " + toProcess);
        }
        return result.toArray(new String[result.size()]);
    }

    /**
     * Size operator. This actually creates the command line, so it is not a zero cost operation.
     *
     * @return number of elements in the command, including the executable.
     */
    public int size() {
        return getCommandline().length;
    }

    /**
     * Generate a deep clone of the contained object.
     *
     * @return a clone of the contained object
     */
    @Override
    public Object clone() {
        try {
            AntCommandLine c = (AntCommandLine)super.clone();
            c.arguments = new ArrayList<>(arguments);
            return c;
        } catch (CloneNotSupportedException e) {
            throw new BuildException(e);
        }
    }

    /**
     * Clear out the whole command line.
     */
    public void clear() {
        executable = null;
        arguments.clear();
    }

    /**
     * Clear out the arguments but leave the executable in place for another operation.
     */
    public void clearArgs() {
        arguments.clear();
    }

    /**
     * Return a marker.
     *
     * <p>This marker can be used to locate a position on the
     * commandline--to insert something for example--when all parameters have been set.</p>
     *
     * @return a marker
     */
    public Marker createMarker() {
        return new Marker(arguments.size());
    }

    /**
     * Return a String that describes the command and arguments suitable for verbose output before a call to
     * <code>Runtime.exec(String[])</code>.
     *
     * @return a string that describes the command and arguments.
     * @since Ant 1.5
     */
    public String describeCommand() {
        return describeCommand(this);
    }

    /**
     * Return a String that describes the arguments suitable for verbose output before a call to
     * <code>Runtime.exec(String[])</code>.
     *
     * @return a string that describes the arguments.
     * @since Ant 1.5
     */
    public String describeArguments() {
        return describeArguments(this);
    }

    /**
     * Return a String that describes the command and arguments suitable for verbose output before a call to
     * <code>Runtime.exec(String[])</code>.
     *
     * @param line the Commandline to describe.
     * @return a string that describes the command and arguments.
     * @since Ant 1.5
     */
    public static String describeCommand(AntCommandLine line) {
        return describeCommand(line.getCommandline());
    }

    /**
     * Return a String that describes the arguments suitable for verbose output before a call to
     * <code>Runtime.exec(String[])</code>.
     *
     * @param line the Commandline whose arguments to describe.
     * @return a string that describes the arguments.
     * @since Ant 1.5
     */
    public static String describeArguments(AntCommandLine line) {
        return describeArguments(line.getArguments());
    }

    /**
     * Return a String that describes the command and arguments suitable for verbose output before a call to
     * <code>Runtime.exec(String[])</code>.
     *
     * <p>This method assumes that the first entry in the array is the
     * executable to run.</p>
     *
     * @param args the command line to describe as an array of strings
     * @return a string that describes the command and arguments.
     * @since Ant 1.5
     */
    public static String describeCommand(String[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder buf = new StringBuilder("Executing \'").append(args[0]).append("\'");
        if (args.length > 1) {
            buf.append(" with ");
            buf.append(describeArguments(args, 1));
        } else {
            buf.append(DISCLAIMER);
        }
        return buf.toString();
    }

    /**
     * Return a String that describes the arguments suitable for verbose output before a call to
     * <code>Runtime.exec(String[])</code>.
     *
     * @param args the command line to describe as an array of strings.
     * @return a string that describes the arguments.
     * @since Ant 1.5
     */
    public static String describeArguments(String[] args) {
        return describeArguments(args, 0);
    }

    /**
     * Return a String that describes the arguments suitable for verbose output before a call to
     * <code>Runtime.exec(String[])</code>.
     *
     * @param args   the command line to describe as an array of strings.
     * @param offset ignore entries before this index.
     * @return a string that describes the arguments
     * @since Ant 1.5
     */
    protected static String describeArguments(String[] args, int offset) {
        if (args == null || args.length <= offset) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        buf.append(String.format("argument%s:%n", args.length > offset ? "s" : ""));
        for (int i = offset; i < args.length; i++) {
            buf.append(String.format("\'%s\'%n", args[i]));
        }
        buf.append(DISCLAIMER);
        return buf.toString();
    }

    /**
     * Get an iterator to the arguments list.
     *
     * @return an Iterator.
     * @since Ant 1.7
     */
    public Iterator<Argument> iterator() {
        return arguments.iterator();
    }

    public static void main(String[] args) {
        String str
            =
            "sqoop import -D mapred.job.name=[pangmeiling]_[6057]_[60dbe95604f843918ebc8bd201e3f58c]_schedule-batch"
                + "-413-hour-2019051410"
                + "-28a828f195d542e58584f07c9e5595ef -D mapred.job.queue.name=root.suyun.batch -D   "
                + "CONF_JOB_BATCH_ID=413 -D mapred.job.priority=NORMAL"
                + " --driver com.mysql.jdbc.Driver --connect jdbc:mysql://rdsfq0wd3r071m26r5c1.mysql.rds.aliyuncs"
                + ".com:3306/dbdj_suyun_crm?             "
                + "                     tinyInt1isBit=false\\&autoReconnect=true\\&failOverReadOnly=false"
                + "\\&zeroDateTimeBehavior=convertToNull"
                + "\\&dontTrackOpenResources=true\\&defaultFetchSize=1000\\&useCursorFetch=true  --username 'bi_read'"
                + " --       password ****** --query "
                + "\"select id,order_id,driver_id,city_id,car_type_id,rule_config_id,rule_config_name,"
                + "rule_config_value,rule_code,order_create_time,"
                + "order_service_time,create_time,begin_time,end_time, cheat_order_num,audit_person,cheat_status,"
                + "appeal_status,punish_result,remark,"
                + "rollback_result from t_suyun_cheat_log  where (create_time >= '2019-05-14' ) and \\$CONDITIONS\" "
                + "-m 1 --hive-drop-import-        "
                + "delims  --hive-database tmp --hive-table o_t_suyun_cheat_log_hour_1557802800803 "
                + "--fields-terminated-by \"\\001\" --input-null-string"
                + " '\\\\N' --input-null-non-string '\\\\N'  --delete-target-dir  --target-dir \"/ "
                + "bj_daojia/warehouse/tmp"
                + ".db/o_t_suyun_cheat_log_hour_1557802800803/dt=2019-05-14\"";
        AntCommandLine antCommantLine = new AntCommandLine(str);
        System.out.println(JSON.toJSONString(antCommantLine.getArguments()));
    }
}