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

package com.aliyun.dataworks.migrationx.domain.dataworks.datago;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qiwei.hqw
 * @version 1.0.0
 * @description DSF算法判断环
 * @createTime 2020-04-27
 */
public class DsfCycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(DsfCycle.class);

    /**
     * 限制node最大数
     */
    private static final int MAX_NODE_COUNT = 10000;
    /**
     * node集合
     */
    public List<String> nodes = new ArrayList<>();

    /**
     * 有向图的邻接矩阵
     */
    private int[][] adjacencyMatrix = new int[MAX_NODE_COUNT][MAX_NODE_COUNT];

    private int addNode(String nodeName) {
        if (!nodes.contains(nodeName)) {
            if (nodes.size() >= MAX_NODE_COUNT) {
                LOGGER.warn("nodes超长: {}, max node name length: {}", nodeName, MAX_NODE_COUNT);
                return -1;
            }
            nodes.add(nodeName);
            return nodes.size() - 1;
        }
        return nodes.indexOf(nodeName);
    }

    public void addLine(String startNode, String endNode) {
        int startIndex = addNode(startNode);
        int endIndex = addNode(endNode);
        if (startIndex >= 0 && endIndex >= 0) {
            adjacencyMatrix[startIndex][endIndex] = 1;
        }
    }

    public List<String> findCycle() {
        List<Integer> trace = new ArrayList<>();
        List<String> result = new ArrayList<>();
        if (adjacencyMatrix.length > 0) {
            findCycle(0, trace, result);
        }
        return result;
    }

    private void findCycle(int v, List<Integer> trace, List<String> result) {
        int j;
        if ((j = trace.indexOf(v)) != -1) {
            StringBuilder sb = new StringBuilder();
            String startNode = nodes.get(trace.get(j));
            while (j < trace.size()) {
                sb.append(nodes.get(trace.get(j))).append("-");
                j++;
            }
            result.add("cycle:" + sb.toString() + startNode);
            return;
        }
        trace.add(v);
        for (int i = 0; i < nodes.size(); i++) {
            if (adjacencyMatrix[v][i] == 1) {
                findCycle(i, trace, result);
            }
        }
        trace.remove(trace.size() - 1);
    }
}
