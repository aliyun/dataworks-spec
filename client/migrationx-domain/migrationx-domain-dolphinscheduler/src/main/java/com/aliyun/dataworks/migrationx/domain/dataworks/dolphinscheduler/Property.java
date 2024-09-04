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

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler;

import java.util.Objects;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.enums.DataType;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v1.v139.enums.Direct;

/**
 * @author 聿剑
 * @date 2022/10/12
 */
public class Property {

    private static final long serialVersionUID = -4045513703397452451L;
    /**
     * key
     */
    private String prop;

    /**
     * input/output
     */
    private Direct direct;

    /**
     * data type
     */
    private DataType type;

    /**
     * value
     */
    private String value;

    public Property() {
    }

    public Property(String prop, Direct direct, DataType type, String value) {
        this.prop = prop;
        this.direct = direct;
        this.type = type;
        this.value = value;
    }

    /**
     * getter method
     *
     * @return the prop
     * @see Property#prop
     */
    public String getProp() {
        return prop;
    }

    /**
     * setter method
     *
     * @param prop the prop to set
     * @see Property#prop
     */
    public void setProp(String prop) {
        this.prop = prop;
    }

    /**
     * getter method
     *
     * @return the value
     * @see Property#value
     */
    public String getValue() {
        return value;
    }

    /**
     * setter method
     *
     * @param value the value to set
     * @see Property#value
     */
    public void setValue(String value) {
        this.value = value;
    }

    public Direct getDirect() {
        return direct;
    }

    public void setDirect(Direct direct) {
        this.direct = direct;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Property property = (Property) o;
        return Objects.equals(prop, property.prop)
                && Objects.equals(value, property.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prop, value);
    }

    @Override
    public String toString() {
        return "Property{"
                + "prop='" + prop + '\''
                + ", direct=" + direct
                + ", type=" + type
                + ", value='" + value + '\''
                + '}';
    }
}
