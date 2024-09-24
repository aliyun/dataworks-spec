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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aliyun.migrationx.common.utils.JSONUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.LoggerFactory;

/**
 * DIJsonProcessor 提供多级JSON配置信息无损存储 warn: getConfiguration getListConfiguration 和父DIJsonProcessor是统一的子元素引用；
 * 修改子Configuration会同时修改父DIJsonProcessor；
 */
public class DIJsonProcessor {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DIJsonProcessor.class);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("(\\$)\\{?(\\w+)\\}?");

    private Object root = null;

    /**
     * 初始化空白的JsonProcessor
     */
    public static DIJsonProcessor newDefault() {
        return DIJsonProcessor.from("{}");
    }

    /**
     * 从JSON字符串加载JsonProcessor
     */
    public static DIJsonProcessor from(String json) {
        json = replaceVariable(json);
        checkJSON(json);

        try {
            return new DIJsonProcessor(json);
        } catch (Exception e) {
            throw DataSyncException.asDataSyncException(e.getMessage(), e);
        }
    }

    private static String replaceVariable(final String param) {
        Map<String, String> mapping = new HashMap<String, String>();

        Matcher matcher = VARIABLE_PATTERN.matcher(param);
        while (matcher.find()) {
            String variable = matcher.group(2);
            String value = System.getProperty(variable);
            if (StringUtils.isBlank(value)) {
                value = matcher.group();
            }
            mapping.put(matcher.group(), value);
        }

        String retString = param;
        for (Entry<String, String> entry : mapping.entrySet()) {
            retString = retString.replace(entry.getKey(), entry.getValue());
        }
        return retString;
    }

    /**
     * 从包括json的File对象加载JsonProcessor
     */
    public static DIJsonProcessor from(File file) {
        try {
            return DIJsonProcessor.from(IOUtils.toString(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            LOGGER.warn(ExceptionTracker.trace(e));
            throw DataSyncException.asDataSyncException(e.getMessage(), e);
        } catch (IOException e) {
            throw DataSyncException.asDataSyncException(e.getMessage(), e);
        }
    }

    /**
     * 从包括json的InputStream对象加载JsonProcessor
     */
    public static DIJsonProcessor from(InputStream is) {
        try {
            return DIJsonProcessor.from(IOUtils.toString(is));
        } catch (IOException e) {
            throw DataSyncException.asDataSyncException(e.getMessage(), e);
        }
    }

    /**
     * 从Map对象加载JsonProcessor
     */
    public static <T> DIJsonProcessor from(final Map<String, T> object) {
        return DIJsonProcessor.from(DIJsonProcessor.toJSONString(object));
    }

    /**
     * 从List对象加载JsonProcessor
     */
    public static <T> DIJsonProcessor from(final List<T> object) {
        return DIJsonProcessor.from(DIJsonProcessor.toJSONString(object));
    }

    public String getNecessaryValue(String key) {
        String value = this.getString(key, null);
        if (StringUtils.isBlank(value)) {
            throw DataSyncException.asDataSyncException(key + " 's value is blank");
        }

        return value;
    }

    /**
     * 根据用户提供的json path，寻址具体的对象。
     * <p/>
     * <br>
     * <p/>
     * NOTE: 目前仅支持Map以及List下标寻址, 例如:
     * <p/>
     * <br />
     * <p/>
     * 对于如下JSON
     * <p/>
     * {"a": {"b": {"c": [0,1,2,3]}}}
     * <p/>
     * config.get("") 返回整个Map <br> config.get("a") 返回a下属整个Map <br> config.get("a.b.c") 返回c对应的数组List <br>
     * config.get("a.b.c[0]") 返回数字0
     *
     * @return Java表示的JSON对象，如果path不存在或者对象不存在，均返回null。
     */
    public Object get(final String path) {
        this.checkPath(path);
        return this.findObject(path);
    }

    /**
     * 用户指定部分path，获取JsonProcessor的子集
     * <p/>
     * <br>
     * 如果path获取的路径或者对象不存在，返回null
     */
    public DIJsonProcessor getConfiguration(final String path) {
        Object object = this.get(path);
        if (null == object) {
            return null;
        }
        DIJsonProcessor subJsonProcessor = new DIJsonProcessor(object);
        return subJsonProcessor;
    }

    /**
     * 根据用户提供的json path，寻址String对象
     *
     * @return String对象，如果path不存在或者String不存在，返回null
     */
    public String getString(final String path) {
        Object string = this.get(path);
        if (null == string) {
            return null;
        }
        return String.valueOf(string);
    }

    /**
     * 根据用户提供的json path，寻址String对象，如果对象不存在，返回默认字符串
     *
     * @return String对象，如果path不存在或者String不存在，返回默认字符串
     */
    public String getString(final String path, final String defaultValue) {
        String result = this.getString(path);

        if (null == result) {
            return defaultValue;
        }

        return result;
    }

    /**
     * 根据用户提供的json path，寻址Character对象
     *
     * @return Character对象，如果path不存在或者Character不存在，返回null
     */
    public Character getChar(final String path) {
        String result = this.getString(path);
        if (null == result) {
            return null;
        }

        try {
            return CharUtils.toChar(result);
        } catch (Exception e) {
            LOGGER.warn(ExceptionTracker.trace(e));
            throw DataSyncException.asDataSyncException(e.getMessage(), e);
        }
    }

    /**
     * 根据用户提供的json path，寻址Boolean对象，如果对象不存在，返回默认Character对象
     *
     * @return Character对象，如果path不存在或者Character不存在，返回默认Character对象
     */
    public Character getChar(final String path, char defaultValue) {
        Character result = this.getChar(path);
        if (null == result) {
            return defaultValue;
        }
        return result;
    }

    /**
     * 根据用户提供的json path，寻址Boolean对象
     *
     * @return Boolean对象，如果path值非true,false ，将报错.特别注意：当 path 不存在时，会返回：null.
     */
    public Boolean getBool(final String path) {
        String result = this.getString(path);
        Boolean nullResult = null;
        if (null == result) {
            return nullResult;
        } else if ("true".equalsIgnoreCase(result)) {
            return Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(result)) {
            return Boolean.FALSE;
        } else {
            throw DataSyncException.asDataSyncException("error:" + path + result);
        }
    }

    /**
     * 根据用户提供的json path，寻址Boolean对象，如果对象不存在，返回默认Boolean对象
     *
     * @return Boolean对象，如果path不存在或者Boolean不存在，返回默认Boolean对象
     */
    public Boolean getBool(final String path, boolean defaultValue) {
        Boolean result = this.getBool(path);
        if (null == result) {
            return defaultValue;
        }
        return result;
    }

    /**
     * 根据用户提供的json path，寻址Integer对象
     *
     * @return Integer对象，如果path不存在或者Integer不存在，返回null
     */
    public Integer getInt(final String path) {
        String result = this.getString(path);
        if (null == result) {
            return null;
        }

        try {
            return Integer.valueOf(result);
        } catch (Exception e) {
            LOGGER.warn(ExceptionTracker.trace(e));

            throw DataSyncException.asDataSyncException(e.getMessage(), e);
        }
    }

    /**
     * 根据用户提供的json path，寻址Integer对象，如果对象不存在，返回默认Integer对象
     *
     * @return Integer对象，如果path不存在或者Integer不存在，返回默认Integer对象
     */
    public Integer getInt(final String path, int defaultValue) {
        Integer object = this.getInt(path);
        if (null == object) {
            return defaultValue;
        }
        return object;
    }

    /**
     * 根据用户提供的json path，寻址Long对象
     *
     * @return Long对象，如果path不存在或者Long不存在，返回null
     */
    public Long getLong(final String path) {
        String result = this.getString(path);
        if (null == result) {
            return null;
        }

        try {
            return Long.valueOf(result);
        } catch (Exception e) {
            LOGGER.warn(ExceptionTracker.trace(e));

            throw DataSyncException.asDataSyncException(e.getMessage(), e);
        }
    }

    /**
     * 根据用户提供的json path，寻址Long对象，如果对象不存在，返回默认Long对象
     *
     * @return Long对象，如果path不存在或者Integer不存在，返回默认Long对象
     */
    public Long getLong(final String path, long defaultValue) {
        Long result = this.getLong(path);
        if (null == result) {
            return defaultValue;
        }
        return result;
    }

    /**
     * 根据用户提供的json path，寻址Double对象
     *
     * @return Double对象，如果path不存在或者Double不存在，返回null
     */
    public Double getDouble(final String path) {
        String result = this.getString(path);
        if (null == result) {
            return null;
        }

        try {
            return Double.valueOf(result);
        } catch (Exception e) {
            LOGGER.warn(ExceptionTracker.trace(e));
            throw DataSyncException.asDataSyncException(e.getMessage());
        }
    }

    /**
     * 根据用户提供的json path，寻址Double对象，如果对象不存在，返回默认Double对象
     *
     * @return Double对象，如果path不存在或者Double不存在，返回默认Double对象
     */
    public Double getDouble(final String path, double defaultValue) {
        Double result = this.getDouble(path);
        if (null == result) {
            return defaultValue;
        }
        return result;
    }

    /**
     * 根据用户提供的json path，寻址List对象，如果对象不存在，返回null
     */
    @SuppressWarnings("unchecked")
    public List<Object> getList(final String path) {
        List<Object> list = this.get(path, List.class);
        if (null == list) {
            return null;
        }
        return list;
    }

    /**
     * 根据用户提供的json path，寻址List对象，如果对象不存在，返回null
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(final String path, Class<T> t) {
        Object object = this.get(path, List.class);
        if (null == object) {
            return null;
        }

        List<T> result = new ArrayList<T>();

        List<Object> origin = (List<Object>) object;
        for (final Object each : origin) {
            result.add((T) each);
        }

        return result;
    }

    /**
     * 根据用户提供的json path，寻址List对象，如果对象不存在，返回默认List
     */
    @SuppressWarnings("unchecked")
    public List<Object> getList(final String path, final List<Object> defaultList) {
        Object object = this.getList(path);
        if (null == object) {
            return defaultList;
        }
        return (List<Object>) object;
    }

    /**
     * 根据用户提供的json path，寻址List对象，如果对象不存在，返回默认List
     */
    public <T> List<T> getList(final String path, final List<T> defaultList, Class<T> t) {
        List<T> list = this.getList(path, t);
        if (null == list) {
            return defaultList;
        }
        return list;
    }

    /**
     * 根据用户提供的json path，寻址包含JsonProcessor的List，如果对象不存在，返回默认null
     */
    public List<DIJsonProcessor> getListConfiguration(final String path) {
        List<Object> lists = getList(path);
        if (lists == null) {
            return null;
        }

        List<DIJsonProcessor> result = new ArrayList<DIJsonProcessor>();
        for (final Object object : lists) {
            DIJsonProcessor subJsonProcessor = new DIJsonProcessor(object);
            result.add(subJsonProcessor);
        }
        return result;
    }

    /**
     * 根据用户提供的json path，寻址Map对象，如果对象不存在，返回null
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap(final String path) {
        Map<String, Object> result = this.get(path, Map.class);
        if (null == result) {
            return null;
        }
        return result;
    }

    /**
     * 根据用户提供的json path，寻址Map对象，如果对象不存在，返回null;
     */
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getMap(final String path, Class<T> t) {
        Map<String, T> map = this.get(path, Map.class);
        if (null == map) {
            return null;
        }

        Map<String, T> result = new HashMap<String, T>();
        for (Entry<String, T> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * 根据用户提供的json path，寻址Map对象，如果对象不存在，返回默认map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap(final String path, final Map<String, Object> defaultMap) {
        Object object = this.getMap(path);
        if (null == object) {
            return defaultMap;
        }
        return (Map<String, Object>) object;
    }

    /**
     * 根据用户提供的json path，寻址Map对象，如果对象不存在，返回默认map
     */
    public <T> Map<String, T> getMap(final String path, final Map<String, T> defaultMap, Class<T> t) {
        Map<String, T> result = getMap(path, t);
        if (null == result) {
            return defaultMap;
        }
        return result;
    }

    /**
     * 根据用户提供的json path，寻址包含Configuration的Map，如果对象不存在，返回默认null
     */
    @SuppressWarnings("unchecked")
    public Map<String, DIJsonProcessor> getMapConfiguration(final String path) {
        Map<String, Object> map = this.get(path, Map.class);
        if (null == map) {
            return null;
        }

        Map<String, DIJsonProcessor> result = new HashMap<String, DIJsonProcessor>();
        for (Entry<String, Object> entry : map.entrySet()) {
            result.put(entry.getKey(), DIJsonProcessor.from(DIJsonProcessor.toJSONString(entry.getValue())));
        }
        return result;
    }

    /**
     * 根据用户提供的json path，寻址具体的对象，并转为用户提供的类型
     * <p/>
     * <br>
     * <p/>
     * NOTE: 目前仅支持Map以及List下标寻址, 例如:
     * <p/>
     * <br />
     * <p/>
     * 对于如下JSON
     * <p/>
     * {"a": {"b": {"c": [0,1,2,3]}}}
     * <p/>
     * config.get("") 返回整个Map <br> config.get("a") 返回a下属整个Map <br> config.get("a.b.c") 返回c对应的数组List <br>
     * config.get("a.b.c[0]") 返回数字0
     *
     * @return Java表示的JSON对象，如果转型失败，将抛出异常
     */
    @SuppressWarnings("unchecked")
    public <T> T get(final String path, Class<T> clazz) {
        this.checkPath(path);
        return (T) this.get(path);
    }

    /**
     * 格式化JsonProcessor输出
     */
    public String beautify() {
        return JSONUtils.toPrettyString(this.getInternal());
    }

    /**
     * 根据用户提供的json path，插入指定对象，并返回之前存在的对象(如果存在)
     * <p/>
     * <br>
     * <p/>
     * 目前仅支持.以及数组下标寻址, 例如:
     * <p/>
     * <br />
     * <p/>
     * config.set("a.b.c[3]", object);
     * <p/>
     * <br>
     * 对于插入对象，JsonProcessor不做任何限制，但是请务必保证该对象是简单对象(包括Map<String,
     * Object>、List<Object>)，不要使用自定义对象，否则后续对于JSON序列化等情况会出现未定义行为。
     *
     * @param path   JSON path对象
     * @param object 需要插入的对象
     * @return Java表示的JSON对象
     */
    public Object set(final String path, final Object object) {
        checkPath(path);

        Object result = this.get(path);

        setObject(path, extractConfiguration(object));

        return result;
    }

    /**
     * 获取JsonProcessor下所有叶子节点的key
     * <p/>
     * <br>
     * <p/>
     * 对于<br>
     * <p/>
     * {"a": {"b": {"c": [0,1,2,3]}}, "x": "y"}
     * <p/>
     * 下属的key包括: a.b.c[0],a.b.c[1],a.b.c[2],a.b.c[3],x
     */
    public Set<String> getKeys() {
        Set<String> collect = new HashSet<String>();
        this.getKeysRecursive(this.getInternal(), "", collect);
        return collect;
    }

    public Set<String> getMapDirectChildrenKeys() {
        Object current = this.getInternal();
        Set<String> collect = new HashSet<String>();
        if (current instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mapping = (Map<String, Object>) current;
            for (Entry<String, Object> entry : mapping.entrySet()) {
                collect.add(entry.getKey());
            }
        } else {
            throw DataSyncException.asDataSyncException("your parameter is not map type");
        }
        return collect;
    }

    /**
     * 删除path对应的值，如果path不存在，将抛出异常。
     */
    public Object remove(final String path) {
        final Object result = this.get(path);
        if (null == result) {
            return null;
            // throw DataSyncException.asDataSyncException(
            // DIServiceErrorCode.RUNTIME_ERROR,
            // DiServiceLocator.message("com.alibaba.cdp.server.utils.JsonProcessor.programingError",
            // path));
        }
        this.set(path, null);
        return result;
    }

    /**
     * 合并其他JsonProcessor，并修改两者冲突的KV配置
     *
     * @param another            合并加入的第三方JsonProcessor
     * @param updateWhenConflict 当合并双方出现KV冲突时候，选择更新当前KV，或者忽略该KV
     * @return 返回合并后对象
     */
    public DIJsonProcessor merge(final DIJsonProcessor another, boolean updateWhenConflict) {
        Set<String> keys = another.getKeys();

        for (final String key : keys) {
            // 如果使用更新策略，凡是another存在的key，均需要更新
            if (updateWhenConflict) {
                this.set(key, another.get(key));
                continue;
            }

            // 使用忽略策略，只有another Configuration存在但是当前Configuration不存在的key，才需要更新
            boolean isCurrentExists = this.get(key) != null;
            if (isCurrentExists) {
                continue;
            }

            this.set(key, another.get(key));
        }
        return this;
    }

    public boolean containsKey(String path) {
        Object obj = this.get(path);
        if (null == obj) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isListParameter(String path) {
        return this.get(path) instanceof List;
    }

    public boolean isStringParameter(String path) {
        return this.get(path) instanceof String;
    }

    @Override
    public String toString() {
        return this.toJSON();
    }

    /**
     * 将JsonProcessor作为JSON输出
     */
    public String toJSON() {
        return DIJsonProcessor.toJSONString(this.getInternal());
    }

    /**
     * 拷贝当前JsonProcessor，注意，这里使用了深拷贝，避免冲突
     */
    public DIJsonProcessor copy() {
        return DIJsonProcessor.from(DIJsonProcessor.toJSONString(this.getInternal()));
    }

    @SuppressWarnings("unchecked")
    void getKeysRecursive(final Object current, String path, Set<String> collect) {
        boolean isRegularElement = !(current instanceof Map || current instanceof List);
        if (isRegularElement) {
            collect.add(path);
            return;
        }

        boolean isMap = current instanceof Map;
        if (isMap) {
            Map<String, Object> mapping = (Map<String, Object>) current;
            for (Entry<String, Object> entry : mapping.entrySet()) {
                if (StringUtils.isBlank(path)) {
                    getKeysRecursive(entry.getValue(), entry.getKey().trim(), collect);
                } else {
                    getKeysRecursive(entry.getValue(), path + "." + entry.getKey().trim(), collect);
                }
            }
            return;
        }

        boolean isList = current instanceof List;
        if (isList) {
            List<Object> lists = (List<Object>) current;
            for (int i = 0; i < lists.size(); i++) {
                getKeysRecursive(lists.get(i), path + String.format("[%d]", i), collect);
            }
            return;
        }

        return;
    }

    public Object getInternal() {
        return this.root;
    }

    public void movePath(String oldKey, String newKey) {
        Object oldValue = this.get(oldKey);
        if (null != oldValue) {
            if ("".equalsIgnoreCase(oldKey)) {
                throw DataSyncException.asDataSyncException("not support root move");
            } else {
                this.set(newKey, oldValue);
                this.remove(oldKey);
            }
        }
    }

    private void setObject(final String path, final Object object) {
        Object newRoot = setObjectRecursive(this.root, split2List(path), 0, object);

        if (isSuitForRoot(newRoot)) {
            this.root = newRoot;
            return;
        }
        throw DataSyncException.asDataSyncException(ToStringBuilder.reflectionToString(object));
    }

    @SuppressWarnings("unchecked")
    private Object extractConfiguration(final Object object) {
        if (object instanceof DIJsonProcessor) {
            return extractFromConfiguration(object);
        }

        if (object instanceof List) {
            List<Object> result = new ArrayList<Object>();
            for (final Object each : (List<Object>) object) {
                result.add(extractFromConfiguration(each));
            }
            return result;
        }

        if (object instanceof Map) {
            Map<String, Object> result = new HashMap<String, Object>();
            for (final String key : ((Map<String, Object>) object).keySet()) {
                result.put(key, extractFromConfiguration(((Map<String, Object>) object).get(key)));
            }
            return result;
        }

        return object;
    }

    private Object extractFromConfiguration(final Object object) {
        if (object instanceof DIJsonProcessor) {
            return ((DIJsonProcessor) object).getInternal();
        }

        return object;
    }

    Object buildObject(final List<String> paths, final Object object) {
        if (null == paths) {
            throw DataSyncException.asDataSyncException("paths is null");
        }

        if (1 == paths.size() && StringUtils.isBlank(paths.get(0))) {
            return object;
        }

        Object child = object;
        for (int i = paths.size() - 1; i >= 0; i--) {
            String path = paths.get(i);

            if (isPathMap(path)) {
                Map<String, Object> mapping = new HashMap<String, Object>();
                mapping.put(path, child);
                child = mapping;
                continue;
            }

            if (isPathList(path)) {
                List<Object> lists = new ArrayList<Object>(this.getIndex(path) + 1);
                expand(lists, this.getIndex(path) + 1);
                lists.set(this.getIndex(path), child);
                child = lists;
                continue;
            }
            throw DataSyncException.asDataSyncException(StringUtils.join(paths, "."));
        }

        return child;
    }

    @SuppressWarnings("unchecked")
    Object setObjectRecursive(Object current, final List<String> paths, int index, final Object value) {

        // 如果是已经超出path，我们就返回value即可，作为最底层叶子节点
        boolean isLastIndex = index == paths.size();
        if (isLastIndex) {
            return value;
        }

        String path = paths.get(index).trim();
        boolean isNeedMap = isPathMap(path);
        if (isNeedMap) {
            Map<String, Object> mapping;

            // 当前不是map，因此全部替换为map，并返回新建的map对象
            boolean isCurrentMap = current instanceof Map;
            if (!isCurrentMap) {
                mapping = new HashMap<String, Object>();
                mapping.put(path, buildObject(paths.subList(index + 1, paths.size()), value));
                return mapping;
            }

            // 当前是map，但是没有对应的key，也就是我们需要新建对象插入该map，并返回该map
            mapping = (Map<String, Object>) current;
            boolean hasSameKey = mapping.containsKey(path);
            if (!hasSameKey) {
                mapping.put(path, buildObject(paths.subList(index + 1, paths.size()), value));
                return mapping;
            }

            // 当前是map，而且还竟然存在这个值，好吧，继续递归遍历
            current = mapping.get(path);
            mapping.put(path, setObjectRecursive(current, paths, index + 1, value));
            return mapping;
        }

        boolean isNeedList = isPathList(path);
        if (isNeedList) {
            List<Object> lists;
            int listIndexer = getIndex(path);

            // 当前是list，直接新建并返回即可
            boolean isCurrentList = current instanceof List;
            if (!isCurrentList) {
                lists = expand(new ArrayList<Object>(), listIndexer + 1);
                lists.set(listIndexer, buildObject(paths.subList(index + 1, paths.size()), value));
                return lists;
            }

            // 当前是list，但是对应的indexer是没有具体的值，也就是我们新建对象然后插入到该list，并返回该List
            lists = (List<Object>) current;
            lists = expand(lists, listIndexer + 1);

            boolean hasSameIndex = lists.get(listIndexer) != null;
            if (!hasSameIndex) {
                lists.set(listIndexer, buildObject(paths.subList(index + 1, paths.size()), value));
                return lists;
            }

            // 当前是list，并且存在对应的index，没有办法继续递归寻找
            current = lists.get(listIndexer);
            lists.set(listIndexer, setObjectRecursive(current, paths, index + 1, value));
            return lists;
        }

        throw DataSyncException.asDataSyncException("programing error");
    }

    private Object findObject(final String path) {
        boolean isRootQuery = StringUtils.isBlank(path);
        if (isRootQuery) {
            return this.root;
        }

        Object target = this.root;

        for (final String each : split2List(path)) {
            if (isPathMap(each)) {
                target = findObjectInMap(target, each);
            } else {
                target = findObjectInList(target, each);
            }
            if (null == target) {
                break;
            }
        }

        return target;
    }

    @SuppressWarnings("unchecked")
    private Object findObjectInMap(final Object target, final String index) {
        boolean isMap = target instanceof Map;
        if (!isMap) {
            throw new IllegalArgumentException("not map");
        }
        Object result = ((Map<String, Object>) target).get(index);
        return result;
    }

    @SuppressWarnings({"unchecked"})
    private Object findObjectInList(final Object target, final String each) {
        boolean isList = target instanceof List;
        if (!isList) {
            throw new IllegalArgumentException("not list");
        }

        String index = each.replace("[", "").replace("]", "");
        if (!StringUtils.isNumeric(index)) {
            throw new IllegalArgumentException("not number " + index);
        }

        return ((List<Object>) target).get(Integer.parseInt(index));
    }

    private List<Object> expand(List<Object> list, int size) {
        int expand = size - list.size();
        while (expand-- > 0) {
            list.add(null);
        }
        return list;
    }

    private boolean isPathList(final String path) {
        return path.contains("[") && path.contains("]");
    }

    private boolean isPathMap(final String path) {
        return StringUtils.isNotBlank(path) && !isPathList(path);
    }

    private int getIndex(final String index) {
        return Integer.parseInt(index.replace("[", "").replace("]", ""));
    }

    private boolean isSuitForRoot(final Object object) {
        if (null != object && (object instanceof List || object instanceof Map)) {
            return true;
        }

        return false;
    }

    private String split(final String path) {
        return StringUtils.replace(path, "[", ".[");
    }

    private List<String> split2List(final String path) {
        return Arrays.asList(StringUtils.split(split(path), "."));
    }

    private void checkPath(final String path) {
        if (null == path) {
            throw new IllegalArgumentException("path is null");
        }

        for (final String each : StringUtils.split(".")) {
            if (StringUtils.isBlank(each)) {
                throw new IllegalArgumentException(each + " is blank");
            }
        }
    }

    @SuppressWarnings("unused")
    private String toJSONPath(final String path) {
        return (StringUtils.isBlank(path) ? "$" : "$." + path).replace("$.[", "$[");
    }

    private static void checkJSON(final String json) {
        if (StringUtils.isBlank(json)) {
            throw new IllegalArgumentException("json is blank");
        }
    }

    /**
     * root should be JSONObject or JSONArray
     */
    private DIJsonProcessor(final Object root) {
        this.root = root;
    }

    private DIJsonProcessor(final String json) {
        try {
            this.root = JSONUtils.parseObject(json, Map.class);
        } catch (Exception e) {
            LOGGER.warn(ExceptionTracker.trace(e));
            throw DataSyncException.asDataSyncException(e.getMessage(), e);
        }
    }

    public static DIJsonProcessor filterSensitiveConfiguration(DIJsonProcessor dIJsonProcessor) {
        Set<String> keys = dIJsonProcessor.getKeys();
        for (final String key : keys) {
            boolean isSensitive = StringUtils.endsWithIgnoreCase(key, "password")
                    || StringUtils.endsWithIgnoreCase(key, "accessKey");
            if (isSensitive && dIJsonProcessor.get(key) instanceof String) {
                dIJsonProcessor.set(key, dIJsonProcessor.getString(key).replaceAll(".", "*"));
            }
        }
        return dIJsonProcessor;
    }

    private static String toJSONString(final Object object) {
        return JSONUtils.toPrettyString(object);
    }
}
