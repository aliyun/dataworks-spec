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

package com.aliyun.dataworks.migrationx.domain.dataworks.standard.repository;

import com.aliyun.dataworks.migrationx.domain.dataworks.standard.service.PaginDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 聿剑
 * @date 2023/01/12
 */
public interface Repository<BO extends BaseObject, DO extends BaseObject, DTO extends BaseObject> {
    /**
     * 插入数据
     *
     * @param object data object
     * @return affected count
     */
    int insert(DO object);

    /**
     * 更新数据
     *
     * @param object Data object
     * @return affected count
     */
    int update(DO object);

    /**
     * 根据ID查询
     *
     * @param id object id
     * @return data object
     */
    DO findById(Long id);

    /**
     * 根据主键删除
     *
     * @param id object id
     * @return affected count
     */
    int deleteById(Long id);

    /**
     * 根据条件删除
     *
     * @param params Data object
     * @return affected count
     */
    int deleteByCondition(DO params);

    /**
     * 更新或保存
     *
     * @param dao Data Object
     * @return affected count
     */
    default Integer saveOrUpdate(DO dao) {
        if (dao.getId() != null) {
            return this.update(dao);
        } else {
            return this.insert(dao);
        }
    }

    /**
     * 分页条件查询
     * @param paginDTO 查询条件
     * @return 数量
     */
    List<DO> findByPagin(PaginDTO paginDTO);

    /**
     * 分页查询数量统计
     * @param paginDTO 查询条件
     * @return 数量
     */
    Integer countByPagin(PaginDTO paginDTO);

    /**
     * 根据ID批量查询
     * @param ids list of ids
     * @return list of data object
     */
    List<DO> findByIds(ArrayList<Long> ids);

    /**
     * 查询全部
     * @return full list of data object
     */
    List<DO> findAll();
}
