/*
 * Copyright (c)  2024. Aliyun.com All right reserved. This software is the
 * confidential and proprietary information of Aliyun.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Aliyun.com.
 */

package com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.ProcessTaskRelation;
import com.aliyun.dataworks.migrationx.domain.dataworks.dolphinscheduler.v3.v320.Location;
import com.aliyun.migrationx.common.utils.JSONUtils;
import org.apache.commons.collections4.SetUtils;

/**
 * Desc:
 *
 * @author 莫泣
 * @date 2024-07-12
 */
public class LocationUtils {

    private LocationUtils() {

    }

    /**
     * build locations list as string value
     *
     * @param processTaskRelationList process task relation list
     * @param xStep                   x step
     * @param yStep                   y step
     * @return locations
     */
    public static String buildLocations(List<ProcessTaskRelation> processTaskRelationList, int xStep, int yStep) {
        Map<Long, Set<Long>> prePostTaskMap = processTaskRelationList.stream().collect(Collectors.groupingBy(ProcessTaskRelation::getPreTaskCode,
            Collectors.mapping(ProcessTaskRelation::getPostTaskCode, Collectors.toSet())));

        // The in-degrees of each node are recorded
        Map<Long, Long> inDegreeMap = processTaskRelationList.stream().collect(
            Collectors.groupingBy(ProcessTaskRelation::getPostTaskCode, Collectors.counting()));

        List<Location> locationList = buildLocationList(prePostTaskMap, inDegreeMap, xStep, yStep);
        return JSONUtils.toJsonString(locationList);
    }

    /**
     * build locations, use topological sort
     *
     * @param prePostTaskMap pre task code and post task code map
     * @param inDegreeMap    in degree for task code
     * @return locations
     */
    private static List<Location> buildLocationList(Map<Long, Set<Long>> prePostTaskMap, Map<Long, Long> inDegreeMap, int xStep, int yStep) {
        int x = 0;
        List<Location> locations = new ArrayList<>();
        Deque<Long> deque = new ArrayDeque<>(Collections.singletonList(0L));
        while (!deque.isEmpty()) {
            int size = deque.size();
            int y = 0;
            Set<Long> zeroInDegreeTaskNodeSet = new HashSet<>();
            for (int i = 0; i < size; i++) {
                Long preTaskCode = deque.poll();
                Set<Long> postTaskCodeSet = prePostTaskMap.get(preTaskCode);
                SetUtils.emptyIfNull(postTaskCodeSet).forEach(postTaskCode -> {
                    if (subAndRemoveZeroValue(inDegreeMap, postTaskCode)) {
                        zeroInDegreeTaskNodeSet.add(postTaskCode);
                    }
                });

                // skip zero code task, it is a start virtual node
                if (Objects.isNull(preTaskCode) || preTaskCode == 0) {
                    continue;
                }
                y = y + yStep;
                locations.add(new Location(preTaskCode, x, y));
            }
            deque.addAll(zeroInDegreeTaskNodeSet);
            zeroInDegreeTaskNodeSet.forEach(inDegreeMap::remove);

            // because the start virtual node is in the first level and not in the locations list, so we add the x step here
            x = x + xStep;
        }
        return locations;
    }

    /**
     * Subtract 1 from the value in the map. If the value is 0, remove the key from the map.
     *
     * @param inDegreeMap map
     * @param taskCode    task code
     * @return has removed
     */
    private static boolean subAndRemoveZeroValue(Map<Long, Long> inDegreeMap, Long taskCode) {
        Long inDegree = inDegreeMap.get(taskCode);
        if (Objects.isNull(inDegree)) {
            return false;
        }
        if (inDegree == 1) {
            inDegreeMap.remove(taskCode);
            return true;
        } else {
            inDegreeMap.put(taskCode, inDegree - 1);
            return false;
        }
    }
}