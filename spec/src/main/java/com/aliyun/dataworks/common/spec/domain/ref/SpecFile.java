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

package com.aliyun.dataworks.common.spec.domain.ref;

import com.aliyun.dataworks.common.spec.domain.SpecRefEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * The comments for the difference between "file" and "script":
 * <ul>
 *     <li>The word "script" typically refers to a written text that contains dialogue and instructions for a play, movie, or other performance. It
 *     can also refer to a set of instructions or code written for a computer program or software.,`</li>
 *     <li>The word "file" refers to a collection of data or information that is stored together and can be accessed by a computer. It can be a
 *     document, image, video, or any other type of digital content. Files are often organized and stored in folders on a computer or other storage
 *     device.</li>
 * </ul>
 *
 * @author 聿剑
 * @date 2023/11/29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SpecFile extends SpecRefEntity {
    /**
     * File path on file storage system
     */
    private String path;
    /**
     * File extension
     */
    private String extension;
}