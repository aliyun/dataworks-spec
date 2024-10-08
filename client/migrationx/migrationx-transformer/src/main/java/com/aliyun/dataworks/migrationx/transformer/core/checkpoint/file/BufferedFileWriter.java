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

package com.aliyun.dataworks.migrationx.transformer.core.checkpoint.file;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import com.aliyun.dataworks.migrationx.transformer.core.checkpoint.StoreWriter;

public class BufferedFileWriter extends BufferedWriter implements StoreWriter {

    public BufferedFileWriter(Writer out) {
        super(out);
    }

    @Override
    public void writeLine(String data, String processName, String taskName) throws IOException {
        write(processName.length());
        write(processName);
        write(taskName.length());
        write(taskName);
        write(data);
        write('\n');
        flush();
    }
}
