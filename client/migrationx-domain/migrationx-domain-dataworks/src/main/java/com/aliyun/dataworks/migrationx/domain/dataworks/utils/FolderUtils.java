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

package com.aliyun.dataworks.migrationx.domain.dataworks.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.aliyun.dataworks.common.spec.domain.dw.types.ModelTreeRoot;
import com.aliyun.dataworks.migrationx.domain.dataworks.constants.DataWorksConstants;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.Workflow;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.nodemarket.AppConfigPack;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.nodemarket.BusinessFolder;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.nodemarket.BusinessFolderConfig;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2.IdeBizInfo;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2.IdeFolder;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2.IdeFolderItemType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.entity.v2.IdeFolderSubType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.FolderType;
import com.aliyun.dataworks.migrationx.domain.dataworks.objects.types.NodeUseType;
import com.aliyun.migrationx.common.utils.GsonUtils;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author sam.liux
 * @date 2020/11/17
 */
@Slf4j
public class FolderUtils {
    private static final Map<String, String> folderItemEngineType = new HashMap<>();

    static {
        folderItemEngineType.put("folderFlink", "Flink");
        folderItemEngineType.put("folderGeneral", "General");
        folderItemEngineType.put("folderDi", "Data Integration");
        folderItemEngineType.put("folderUserDefined", "UserDefined");
        folderItemEngineType.put("folderEMR", "EMR");
        folderItemEngineType.put("folderMaxCompute", "MaxCompute");
        folderItemEngineType.put("folderAlgm", "Algorithm");
        folderItemEngineType.put("folderService", "DataService");
        folderItemEngineType.put("folderADB", "ADB");
        folderItemEngineType.put("folderHologres", "Hologres");
        folderItemEngineType.put("folderADBMysql", "ADBMYSQL");
        folderItemEngineType.put("folderMorse", "Morse");
        folderItemEngineType.put("folderJdbc", "Jdbc");
    }

    public static boolean isFolderRootItemPath(String folderItemPath) {
        for (ModelTreeRoot root : ModelTreeRoot.values()) {
            if (root.getRootKey().equalsIgnoreCase(folderItemPath)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFolderRootName(String name) {
        for (ModelTreeRoot root : ModelTreeRoot.values()) {
            if (root.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFolderRootEnglishName(String englishName) {
        for (ModelTreeRoot root : ModelTreeRoot.values()) {
            if (root.getEnglishName().equalsIgnoreCase(englishName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFolderRoot(String name) {
        return isFolderRootEnglishName(name) || isFolderRootItemPath(name) || isFolderRootName(name);
    }

    public static List<IdeFolder> getBasicV3Folders(IdeBizInfo ideBizInfo) {
        List<String> folderItemNames = Arrays.asList("folderGeneral", "folderDi", "folderService", "folderAlgm",
            "folderUserDefined");
        IdeFolder bizFolder = new IdeFolder();
        bizFolder.setBizId(ideBizInfo.getBizName());
        bizFolder.setVersion(ideBizInfo.getVersion());
        bizFolder.setType(FolderType.BUSINESS.getCode());
        bizFolder.setSubType(IdeFolderSubType.NORMAL.getCode());
        bizFolder.setSourceApp("ide");
        bizFolder.setBizUseType(ideBizInfo.getUseType());
        bizFolder.setFolderItemName(bizFolder.getBizId());
        bizFolder.setFolderItemType(IdeFolderItemType.CODE.getCode());
        bizFolder.setFolderItemPath(Joiner.on(File.separator).join(
                Integer.valueOf(NodeUseType.SCHEDULED.getValue()).equals(ideBizInfo.getUseType()) ?
                        ModelTreeRoot.BIZ_ROOT.getRootKey() : ModelTreeRoot.MANUAL_BIZ_ROOT.getRootKey(),
                ideBizInfo.getBizName()));

        List<IdeFolder> list = folderItemNames.stream().map(folderItemName -> {
            IdeFolder ideFolder = new IdeFolder();
            ideFolder.setFolderItemType(IdeFolderItemType.CODE.getCode());
            ideFolder.setFolderItemName(folderItemName);
            ideFolder.setFolderItemPath(Joiner.on(File.separator).join(
                    ModelTreeRoot.BIZ_ROOT.getRootKey(), ideBizInfo.getBizName(), folderItemName));
            ideFolder.setBizId(ideBizInfo.getBizName());
            ideFolder.setBizUseType(ideBizInfo.getUseType());
            ideFolder.setType(FolderType.ENGINE_TYPE.getCode());
            ideFolder.setSubType(IdeFolderSubType.NORMAL.getCode());
            ideFolder.setSourceApp("ide");
            ideFolder.setVersion(ideBizInfo.getVersion());
            ideFolder.setEngineType(folderItemEngineType.get(folderItemName));
            ideFolder.setLabelFlags(GsonUtils.toJsonString(new ArrayList<>()));
            return ideFolder;
        }).collect(Collectors.toList());
        list.add(bizFolder);
        return list;
    }

    public static String getEngineType(String folderItemPath) {
        if (StringUtils.isBlank(folderItemPath)) {
            return null;
        }

        String[] parts = folderItemPath.split("/");
        for (String part : parts) {
            if (folderItemEngineType.containsKey(part)) {
                return folderItemEngineType.get(part);
            }
        }
        return null;
    }

    public static boolean isEngineFolder(String folderItemName) {
        return folderItemEngineType.keySet().contains(folderItemName);
    }

    public static ModelTreeRoot getModelTreeRoot(Workflow workflow) {
        if (DataWorksConstants.OLD_VERSION_WORKFLOW_NAME.equalsIgnoreCase(workflow.getName())) {
            return ModelTreeRoot.WORK_FLOW_ROOT_NEW;
        }

        return BooleanUtils.isTrue(workflow.getScheduled()) ? ModelTreeRoot.BIZ_ROOT : ModelTreeRoot.MANUAL_BIZ_ROOT;
    }

    public static ModelTreeRoot getModelTreeRoot(String folder) {
        if (StringUtils.isBlank(folder)) {
            return null;
        }

        String[] parts = StringUtils.split(folder, "/");
        return Arrays.stream(parts).map(ModelTreeRoot::searchModelTreeRoot)
            .filter(Objects::nonNull).findFirst().orElse(null);
    }

    public static boolean isTheSameFolder(String folderA, String folderB) {
        if (StringUtils.isBlank(folderA) || StringUtils.isBlank(folderB)) {
            return false;
        }

        ModelTreeRoot rootA = getModelTreeRoot(folderA);
        ModelTreeRoot rootB = getModelTreeRoot(folderB);
        if (rootA == null || rootB == null) {
            return false;
        }

        if (!Objects.equals(rootA, rootB)) {
            return false;
        }

        folderA = RegExUtils.replaceFirst(folderA, "^/", "");
        folderA = RegExUtils.replaceFirst(folderA, "/$", "");
        folderA = RegExUtils.replaceFirst(folderA, "^" + rootA.getRootKey(), "");
        folderA = RegExUtils.replaceFirst(folderA, "^" + rootA.getName(), "");
        folderA = RegExUtils.replaceFirst(folderA, "^" + rootA.getEnglishName(), "");
        folderA = RegExUtils.replaceFirst(folderA, "^" + rootA.getModule(), "");

        folderB = RegExUtils.replaceFirst(folderB, "^/", "");
        folderB = RegExUtils.replaceFirst(folderB, "/$", "");
        folderB = RegExUtils.replaceFirst(folderB, "^" + rootB.getRootKey(), "");
        folderB = RegExUtils.replaceFirst(folderB, "^" + rootB.getName(), "");
        folderB = RegExUtils.replaceFirst(folderB, "^" + rootB.getEnglishName(), "");
        folderB = RegExUtils.replaceFirst(folderB, "^" + rootB.getModule(), "");

        return StringUtils.equals(folderA, folderB);
    }

    public static String normalizeFolder(String folder) {
        if (StringUtils.isBlank(folder)) {
            return folder;
        }

        ModelTreeRoot root = getModelTreeRoot(folder);
        if (root == null) {
            return folder;
        }

        folder = RegExUtils.replaceFirst(folder, "^/", "");
        folder = RegExUtils.replaceFirst(folder, "/$", "");

        if (StringUtils.startsWith(folder, root.getRootKey())) {
            folder = RegExUtils.replaceFirst(folder, "^" + root.getRootKey(), root.getEnglishName());
        } else if (StringUtils.startsWith(folder, root.getName())) {
            folder = RegExUtils.replaceFirst(folder, "^" + root.getName(), root.getEnglishName());
        }

        return Arrays.stream(StringUtils.split(folder, "/"))
                .map(StringUtils::trimToEmpty)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("/"));
    }

    public static boolean isUnderFolder(String childFolder, String parentFolder) {
        childFolder = normalizeFolder(childFolder);
        parentFolder = normalizeFolder(parentFolder);

        List<String> childPaths = Arrays.asList(StringUtils.split(StringUtils.defaultIfBlank(childFolder, ""), "/"));
        List<String> parentPaths = Arrays.asList(StringUtils.split(StringUtils.defaultIfBlank(parentFolder, ""), "/"));

        if (CollectionUtils.size(parentPaths) >= CollectionUtils.size(childPaths)) {
            return false;
        }

        // match all child path parts in parent path
        boolean match = true;
        for (int i = 0; i < CollectionUtils.size(parentPaths); i++) {
            if (!StringUtils.equals(childPaths.get(i), parentPaths.get(i))) {
                match = false;
                break;
            }
        }

        if (match) {
            return true;
        }

        return false;
    }

    /**
     * 规范化配置包路径，将其转换为规范的spec路径
     * 例如：
     * 1. 业务流程/test_biz/MaxCompute => 业务流程/test_biz/MaxCompute/数据开发
     * 2. 业务流程/test_biz/MaxCompute/test => 业务流程/test_biz/MaxCompute/数据开发/test
     *
     * @param nodeTypeId 节点类型
     * @param folderPath 文件夹路径
     * @param configPack 配置包
     * @param locale     语言
     * @return 规范化后的路径
     */
    public static String normalizeConfigPackPathToSpec(Integer nodeTypeId, String folderPath, Map<String, AppConfigPack> configPack, Locale locale) {
        if (StringUtils.isBlank(folderPath) || nodeTypeId == null || configPack == null || locale == null) {
            log.warn("invalid input parameters, folderPath: {}, nodeTypeId: {},  locale: {}",
                    folderPath, nodeTypeId, locale);
            return folderPath;
        }

        List<String> path = new ArrayList<>(Arrays.asList(StringUtils.split(folderPath, File.separator)));
        List<String> relativePath = addFolderLabels(nodeTypeId, path, configPack, locale);
        return Joiner.on("/").join(relativePath);
    }

    private static List<String> addFolderLabels(Integer nodeTypeId, List<String> paths, Map<String, AppConfigPack> configPack, Locale locale) {
        if (CollectionUtils.isEmpty(paths)) {
            return paths;
        }

        // 业务流程/Workflow/...
        String modelRoot = paths.get(0);
        Arrays.stream(com.aliyun.dataworks.common.spec.domain.dw.types.ModelTreeRoot.values()).filter(v -> v.matches(modelRoot)).findAny()
                .ifPresent(root -> paths.set(0, root.getDisplayName(locale)));

        // 例如: 业务流程/test_biz/MaxCompute
        if (CollectionUtils.size(paths) >= 3) {
            // MaxCompute
            String engineFolderName = paths.get(2);
            Optional.ofNullable(configPack)
                    .map(Map::values).flatMap(values -> values.stream()
                            .map(AppConfigPack::getConfigValue)
                            .map(value -> (BusinessFolderConfig) GsonUtils.fromJsonString(value, BusinessFolderConfig.class))
                            .filter(Objects::nonNull)
                            .filter(conf -> {
                                boolean displayNameMatches = MapUtils.emptyIfNull(conf.getDisplayName()).values().stream()
                                        .anyMatch(displayName -> StringUtils.equalsIgnoreCase(engineFolderName, displayName));
                                boolean folderMatches = Optional.ofNullable(conf.getFolders()).map(BusinessFolder::getName)
                                        .map(folderName -> StringUtils.equalsIgnoreCase(folderName, engineFolderName)).orElse(false);
                                return displayNameMatches || folderMatches;
                            })
                            .findAny())
                    .ifPresent(businessFolderConfig -> {
                        // 例如: 业务流程/test_biz/MaxCompute/数据开发/test, 添加匹配"数据开发"这一级Label
                        if (CollectionUtils.size(paths) >= 3) {
                            AtomicInteger indexAt = new AtomicInteger(3);
                            AtomicBoolean replace = new AtomicBoolean(false);
                            BusinessFolder folderConf = Optional.ofNullable(businessFolderConfig.getFolders())
                                    .map(BusinessFolder::getChildrens)
                                    .orElse(ListUtils.emptyIfNull(null)).stream()
                                    .map(label -> ListUtils.emptyIfNull(label.getChildrens()).stream().filter(lc ->
                                            ListUtils.emptyIfNull(lc.getNodes()).stream().anyMatch(node -> Objects.equals(node.getId(), nodeTypeId))).findAny())
                                    .findAny().flatMap(label -> label)
                                    .orElseGet(() -> {
                                        BusinessFolder r = Optional.ofNullable(businessFolderConfig.getFolders())
                                                .filter(folder -> ListUtils.emptyIfNull(folder.getNodes()).stream()
                                                        .anyMatch(node -> Objects.equals(node.getId(), nodeTypeId))).orElse(null);
                                        indexAt.set(2);
                                        replace.set(true);
                                        return r;
                                    });

                            if (folderConf != null) {
                                MapUtils.emptyIfNull(folderConf.getDisplayName()).entrySet().stream()
                                        .filter(ent -> StringUtils.containsIgnoreCase(locale.toString(), ent.getKey())).map(Entry::getValue).findAny()
                                        .ifPresent(label -> {
                                            if (replace.get()) {
                                                paths.set(indexAt.get(), label);
                                            } else {
                                                paths.add(indexAt.get(), label);
                                            }
                                        });
                            }
                            MapUtils.emptyIfNull(businessFolderConfig.getDisplayName()).entrySet().stream()
                                    .filter(ent -> StringUtils.containsIgnoreCase(locale.toString(), ent.getKey())).map(Entry::getValue).findAny()
                                    .ifPresent(label -> paths.set(2, label));
                        }
                    });
        }
        return paths;
    }

    /**
     * 规范化spec路径，将其转换为满足config pack规范的路径
     *
     * @param folderPath 待规范化的路径
     * @param configPack 配置包
     * @return 规范化后的路径
     */
    public static String normalizeSpecPathToConfigPack(String folderPath, Map<String, AppConfigPack> configPack) {
        List<String> path = Arrays.asList(StringUtils.split(folderPath, File.separator));
        String modelTreeRootDir = path.stream().filter(p -> ModelTreeRoot.searchModelTreeRoot(p) != null).findFirst().orElseThrow(
                () -> new RuntimeException("model tree root not found at path: " + folderPath));
        int modelTreeRootDirIdx = path.indexOf(modelTreeRootDir);
        List<String> relativePath = removeFolderLabels(new ArrayList<>(path.subList(modelTreeRootDirIdx, path.size() - 1)), configPack);
        return Joiner.on("/").join(relativePath);
    }

    private static List<String> removeFolderLabels(List<String> paths, Map<String, AppConfigPack> configPack) {
        // 例如: 业务流程/test_biz/MaxCompute
        if (CollectionUtils.size(paths) >= 3) {
            String engineFolderName = paths.get(2);
            Boolean isEngineFolder = Optional.ofNullable(configPack)
                    .map(Map::values)
                    .map(values -> values.stream()
                            .map(AppConfigPack::getConfigValue)
                            .map(value -> (BusinessFolderConfig) GsonUtils.fromJsonString(value, BusinessFolderConfig.class))
                            .filter(Objects::nonNull)
                            .anyMatch(conf -> MapUtils.emptyIfNull(conf.getDisplayName()).values().stream()
                                    .anyMatch(displayName -> StringUtils.equalsIgnoreCase(engineFolderName, displayName))))
                    .orElse(false);
            // 例如: 业务流程/test_biz/MaxCompute/数据开发/test, 删除"数据开发"这一级叫Label
            if (isEngineFolder && CollectionUtils.size(paths) > 3) {
                paths.remove(3);
            }
        }
        return paths;
    }
}
