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

package com.aliyun.dataworks.common.spec.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import com.aliyun.dataworks.common.spec.domain.interfaces.LabelEnum;
import com.aliyun.dataworks.common.spec.exception.SpecErrorCode;
import com.aliyun.dataworks.common.spec.exception.SpecException;
import com.aliyun.dataworks.common.spec.parser.Parser;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext;
import com.aliyun.dataworks.common.spec.parser.SpecParserContext.SpecEntityContext;
import com.aliyun.dataworks.common.spec.parser.SpecParserFactory;
import lombok.Data;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Spec developer utils
 *
 * @author yiwei.qyw
 * @date 2023/7/7
 */
@SuppressWarnings("rawtypes")
public class SpecDevUtil {

    /**
     * Set reference entity or local spec entity to ownerObject and add reference message to SpecParserContext, then it
     * can auto find parser and set value in refParse function. This method does not support parsing of simple types.
     *
     * @param ownerObject   Owner object
     * @param fieldName     Field name
     * @param value         Context value
     * @param parserContext SpecParserContext
     */
    public static void setSpecObject(Object ownerObject, String fieldName, Object value,
        SpecParserContext parserContext) {
        // find declaredField
        Field declaredField = getField(ownerObject, fieldName);
        // get clz simpleName
        Class<?> fieldClz = getFieldClz(declaredField);
        String clzName = fieldClz.getSimpleName();

        Object resEntity = null;
        if (value instanceof List) {
            resEntity = getListObj(clzName, (List)value, parserContext, declaredField);
        } else if (value instanceof Map) {
            resEntity = getSpecEntity(clzName, value, parserContext);
        } else if (value instanceof String) {
            // reference string
            setRefEntity(ownerObject, clzName, value, parserContext, declaredField);
            return;
        }
        if (resEntity == null) {
            return;
        }
        // set value
        setValue(ownerObject, declaredField, resEntity);

    }

    /**
     * Only set the same field name as the json key. Automatically sets the key in json to match the field name in the
     * domain model.
     * For example, if a field is named "name", then the corresponding key in JSON will also be "name".
     *
     * @param ctxMap        Context value map
     * @param specObj       Instance to be set
     * @param parserContext SpecParserContext
     */
    public static void setSameKeyField(Map<String, Object> ctxMap, Object specObj, SpecParserContext parserContext) {
        SpecDevUtil.setSimpleField(ctxMap, specObj);
        // set enum value
        SpecDevUtil.setEnumField(ctxMap, specObj);
        // set map value
        SpecDevUtil.setMapField(ctxMap, specObj);
        // set simple List
        SpecDevUtil.setSimpleListField(ctxMap, specObj);
        // set spec object
        parserSpecFields(parserContext, ctxMap, specObj);
    }

    /**
     * Set all simple field, including primitive type, String, Integer, Long, Double, Boolean, Character, Byte, Short,
     * Float and BigDecimal. Only set the same field name as the json key.
     *
     * @param ctxMap ContextMap
     * @param object New instance
     */
    public static void setSimpleField(Map<String, Object> ctxMap, Object object) {
        List<Field> declaredFields = getPropertyFields(object);
        setSimpleField(ctxMap, object, declaredFields);
        // get super class field
        if (object.getClass().getSuperclass() != null && object.getClass().getSuperclass() == SpecRefEntity.class) {
            Class<?> entityClass = object.getClass().getSuperclass();
            Field[] entityFields = entityClass.getDeclaredFields();
            setSimpleField(ctxMap, object, Arrays.asList(entityFields));
        }
    }

    /**
     * Set all enum type field. Only set the same field name as the json key.
     *
     * @param ctxMap ContextMap
     * @param object New instance to be set
     */

    @SuppressWarnings("unchecked")
    public static void setEnumField(Map<String, Object> ctxMap, Object object) {
        Field[] declaredFields = object.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            Class<?> type = field.getType();
            if (type.isEnum() && LabelEnum.class.isAssignableFrom(type)) {
                // get enum object
                String value = (String)ctxMap.get(field.getName());
                if (value == null) {
                    continue;
                }
                LabelEnum label = (LabelEnum)LabelEnum.getByLabel((Class)type, value);
                if (label != null) {
                    setValue(object, field, label);
                } else {
                    throw new SpecException(SpecErrorCode.ENUM_NOT_EXIST,
                        "Enum not found.\n" + "Enum: " + type.getSimpleName() + "\nValue:" + value);
                }
            }
        }
    }

    /**
     * Set Map type field. Only set the same field name as the json key.
     *
     * @param ctxMap context
     * @param object new instance
     */
    public static void setMapField(Map ctxMap, Object object) {
        List<Field> declaredFields = getPropertyFields(object);
        for (Field field : declaredFields) {
            if (!Map.class.isAssignableFrom(field.getType())) {
                continue;
            }
            Object value = ctxMap.get(field.getName());
            setValue(object, field, value);
        }
    }

    /**
     * Set value to field in object by reflect.
     *
     * @param ownerObject  Object to be set
     * @param clzField     Field to be set
     * @param targetObject TargetObject
     */

    public static void setValue(Object ownerObject, Field clzField, Object targetObject) {
        try {
            clzField.setAccessible(true);
            clzField.set(ownerObject, targetObject);
            clzField.setAccessible(false);
        } catch (Exception e) {
            throw new SpecException(e, SpecErrorCode.FIELD_SET_FAIL,
                "Field set fail. Please check your spec content and field type.\n" + "field:"
                    + clzField + "\nvalue:" + targetObject
                    + "\nownerObject:" + ownerObject.getClass().getSimpleName());
        }
    }

    /**
     * Set reference entity context, and save message to SpecParserContext
     *
     * @param ownerObject   Which object the filed is belong to
     * @param value         Entity context
     * @param parserContext Context meta
     * @param clzField      Field in owner class
     * @param entityName    Entity name
     */
    public static void setRefContext(Object ownerObject, Object value, SpecParserContext parserContext, Field clzField,
        String entityName) {
        SpecEntityContext specEntityContext = new SpecEntityContext();
        specEntityContext.setOwnerObject(ownerObject);
        specEntityContext.setEntityName(entityName);
        specEntityContext.setEntityField(clzField);
        specEntityContext.setEntityValue(value);
        parserContext.getRefEntityList().add(specEntityContext);
    }

    /**
     * Parse ref to get type and id like {{aaa.bbb}} or {bbb}
     *
     * @param refId Reference string
     * @return Reference message
     */

    public static RefMsg parseRefId(String refId) {
        RefMsg refMsg = new RefMsg();
        boolean validRef = refId.startsWith("{{") && refId.endsWith("}}");
        if (!validRef) {
            refMsg.setId(refId);
            return refMsg;
        }

        refId = refId.substring(2, refId.length() - 2);
        String[] split = refId.split("\\.");
        if (split.length == 2) {
            refMsg.setType(split[0]);
            refMsg.setId(split[1]);
        } else if (split.length == 1) {
            refMsg.setId(split[0]);
        } else {
            throw new SpecException(SpecErrorCode.REFID_PARSER_ERROR, "The reference id format is incorrect: " + refId);
        }
        return refMsg;
    }

    /**
     * 递归获取父类的成员
     *
     * @param obj Object
     * @return List
     */
    public static List<Field> getPropertyFields(Object obj) {
        List<Field> fields = new ArrayList<>();
        if (obj == null || ClassUtils.isPrimitiveOrWrapper(obj.getClass()) || obj instanceof String) {
            return fields;
        }

        getPropertyFields(fields, obj.getClass());
        return fields;
    }

    private static void getPropertyFields(List<Field> fields, Class<?> clz) {
        if (clz.equals(Object.class) || ClassUtils.isPrimitiveOrWrapper(clz) || clz.equals(String.class)) {
            return;
        }

        ArrayList<Field> declared = new ArrayList<>(Arrays.asList(clz.getDeclaredFields()));
        fields.addAll(declared.stream()
            .filter(field -> !field.getName().startsWith("this$"))
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            //.filter(field -> checkGetter(clz, field))
            //.filter(field -> checkSetter(clz, field))
            .collect(Collectors.toList())
        );
        getPropertyFields(fields, clz.getSuperclass());
    }

    /**
     * Save entity message to context map. If entity is a reference type class, then you need to use this method to set
     * context. However, the referenced object will not be found in refParser function.
     *
     * @param object        Entity to be set
     * @param parserContext Context meta
     */

    public static void setEntityToCtx(Object object, SpecParserContext parserContext) {
        if (!(SpecRefEntity.class.isAssignableFrom(object.getClass()))) {
            return;
        }

        Map<String, SpecRefEntity> entityMap = parserContext.getEntityMap();
        String id;
        try {
            Field idField = getPropertyFields(object).stream().filter(f -> StringUtils.equals(f.getName(), "id")).findAny().orElseThrow(() ->
                new SpecException(SpecErrorCode.GET_ID_ERROR, "Failed to get id" + object.getClass().getSimpleName()));
            idField.setAccessible(true);
            id = (String)idField.get(object);
            idField.setAccessible(false);
        } catch (Exception e) {
            throw new SpecException(e, SpecErrorCode.GET_ID_ERROR,
                "Failed to get id" + object.getClass().getSimpleName());
        }
        if (id == null) {
            return;
        }

        String key = object.getClass().getSimpleName() + "#" + id;

        if (entityMap.containsKey(key)) {
            throw new SpecException(SpecErrorCode.DUPLICATE_ID,
                "Duplicate id:" + id + "\n" + "Type:" + object.getClass().getSimpleName());
        }
        entityMap.put(key, (SpecRefEntity)object);
    }

    /**
     * Get entity by corresponding parser, find by clz type.
     *
     * @param clz           Class object you want to generate
     * @param ctxObject     Context map
     * @param parserContext parserContext
     * @return Object entity
     */
    @SuppressWarnings("unchecked")
    public static Object getObjectByParser(Class<?> clz, Object ctxObject, SpecParserContext parserContext) {
        return SpecParserFactory.getParser(clz.getSimpleName()).parse((Map<String, Object>)ctxObject, parserContext);
    }

    /**
     * Check whether the class is a simple type. Simple type includes all primitive types, String, Enum and Map
     *
     * @param fieldClz Field class
     * @return boolean true/false
     */
    public static boolean isSimpleType(Class<?> fieldClz) {
        return fieldClz.isPrimitive() || fieldClz == Boolean.class || fieldClz == Byte.class || fieldClz == Short.class
            || fieldClz == Integer.class || fieldClz == Long.class || fieldClz == Float.class
            || fieldClz == Double.class || fieldClz == Character.class || fieldClz == String.class
            || fieldClz == Map.class || fieldClz.isEnum();
    }

    private static Field getField(Object holdObject, String fieldName) {
        Field declaredField;
        try {
            declaredField = holdObject.getClass().getDeclaredField(fieldName);
        } catch (Exception e) {
            SpecException ex = new SpecException(SpecErrorCode.FIELD_NOT_FOUND,
                "Cannot find field.\n" + "Class:" + holdObject.getClass().getSimpleName()
                    + " Object value:" + holdObject.getClass().getSimpleName());
            declaredField = Optional.ofNullable(holdObject.getClass().getSuperclass()).map(superClz -> {
                try {
                    return superClz.getDeclaredField(fieldName);
                } catch (NoSuchFieldException ignored) {

                }
                return null;
            }).orElseThrow(() -> ex);
        }
        return declaredField;
    }

    /**
     * Get field class. If it is a generic, get the type of the first element.
     *
     * @param declaredField Field
     * @return Class
     */

    public static Class<?> getFieldClz(Field declaredField) {
        Class<?> clz = declaredField.getType();

        Type genericType = declaredField.getGenericType();
        Type elementType = null;
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)genericType;
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length > 0) {
                elementType = typeArguments[0];
            }
        }
        if (elementType != null) {
            clz = (Class<?>)elementType;
        }
        return clz;
    }

    /**
     * Set simple type List. It does not use a parser to parse generic types. Set list value directly.
     *
     * @param ctxMap  ctxMap
     * @param specObj specObj
     */
    public static void setSimpleListField(Map<String, Object> ctxMap, Object specObj) {
        Field[] declaredFields = specObj.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (!List.class.isAssignableFrom(field.getType())) {
                continue;
            }
            Class<?> fieldClz = getFieldClz(field);

            if (!isSimpleType(fieldClz)) {
                continue;
            }

            List listValue = (List)ctxMap.get(field.getName());
            if (listValue != null) {
                List<Object> result = new ArrayList<>(listValue.size());
                result.addAll(listValue);
                setValue(specObj, field, result);
            }
        }
    }

    private static void setRefEntity(Object holdObject, String simpleName, Object value, SpecParserContext contextMeta,
        Field declaredField) {
        value = parseRefId((String)value).getId();
        setRefContext(holdObject, parseRefId((String)value).getId(), contextMeta, declaredField, simpleName);
    }

    @SuppressWarnings("unchecked")
    private static Object getSpecEntity(String simpleName, Object value, SpecParserContext parserContext) {
        Parser parser = SpecParserFactory.getParser(simpleName);
        // generate Object by Parser
        if (parser == null) {
            return null;
        }

        return parser.parse((Map<String, Object>)value, parserContext);
    }

    @SuppressWarnings("unchecked")
    private static Object getListObj(String simpleName, List value, SpecParserContext parserContext,
        Field declaredField) {
        Class<?> fieldClz = getFieldClz(declaredField);
        if (isSimpleType(fieldClz)) {
            return null;
        }
        ArrayList<Object> list = new ArrayList<>();
        Parser parser = SpecParserFactory.getParser(simpleName);
        for (Object elmCtx : value) {
            if (elmCtx instanceof String) {
                // reference type
                elmCtx = parseRefId((String)elmCtx).getId();
                setRefContext(list, elmCtx, parserContext, declaredField, simpleName);
                continue;
            }
            Object parse = parser.parse((Map<String, Object>)elmCtx, parserContext);
            setEntityToCtx(parse, parserContext);
            list.add(parse);
        }
        return list;
    }

    /**
     * reference massage class
     */
    @Data
    public static class RefMsg {
        private String type;
        private String id;
    }

    private static void setSimpleField(Map<String, Object> ctxMap, Object object, List<Field> entityFields) {
        // parse simple type
        for (Field field : entityFields) {
            Object value = ctxMap.get(field.getName());
            if (value == null) {
                continue;
            }
            Class<?> fieldType = field.getType();
            try {
                if (fieldType.equals(Integer.TYPE) || fieldType.equals(Integer.class)) {
                    setValue(object, field, Integer.valueOf(value.toString()));
                } else if (fieldType.equals(Long.TYPE) || fieldType.equals(Long.class)) {
                    setValue(object, field, Long.valueOf(value.toString()));
                } else if (fieldType.equals(Float.TYPE) || fieldType.equals(Float.class)) {
                    setValue(object, field, Float.valueOf(value.toString()));
                } else if (fieldType.equals(Double.TYPE) || fieldType.equals(Double.class)) {
                    setValue(object, field, Double.valueOf(value.toString()));
                } else if (fieldType.equals(Boolean.TYPE) || fieldType.equals(Boolean.class)) {
                    setValue(object, field, Boolean.valueOf(value.toString()));
                } else if (fieldType.equals(Character.TYPE) || fieldType.equals(Character.class)) {
                    setValue(object, field, value.toString().charAt(0));
                } else if (fieldType.equals(String.class)) {
                    setValue(object, field, value.toString());
                } else if (fieldType.equals(BigDecimal.class)) {
                    setValue(object, field, value.toString());
                }
            } catch (Exception e) {
                throw new SpecException(e, SpecErrorCode.FIELD_SET_FAIL,
                    "Type conversion error.\n" + "Expect type: " + fieldType.getSimpleName()
                        + "\nActual value:"
                        + value);
            }

        }
    }

    private static void parserSpecFields(SpecParserContext contextMeta, Map<String, Object> ctxMap, Object specObj) {
        for (Field field : specObj.getClass().getDeclaredFields()) {
            Class<?> fieldType = field.getType();
            if (isSimpleType(fieldType)) {
                continue;
            }
            if (!ctxMap.containsKey(field.getName())) {
                continue;
            }
            SpecDevUtil.setSpecObject(specObj, field.getName(), ctxMap.get(field.getName()), contextMeta);
        }
    }

}