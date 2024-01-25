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

package com.aliyun.dataworks.migrationx.domain.dataworks.standard.service;

import com.aliyun.dataworks.migrationx.domain.dataworks.standard.repository.BaseObject;
import com.aliyun.dataworks.migrationx.domain.dataworks.standard.repository.Repository;
import com.aliyun.migrationx.common.exception.BizException;
import com.aliyun.migrationx.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Abstract data object access service
 * @author 聿剑
 * @date 2023/01/12
 */
@Component
@Slf4j
public abstract class AbstractObjectAccessService<BO extends BaseObject, DTO extends BaseObject, DO extends BaseObject> {
    protected abstract Repository<BO, DO, DTO> getRepository();
    ParameterizedType pt = (ParameterizedType)this.getClass().getGenericSuperclass();

    public ResultList<BO> findByPagin(@NotNull PaginDTO pagin) {
        if (pagin == null) {
            return ResultList.ofError("save failed! object PaginDTO must not be null!");
        } else {
            List<BO> bos = this.listByPagin(pagin);
            Integer count = this.getRepository().countByPagin(pagin);
            return ResultList.ofSuccess(bos, pagin.getToPage(), pagin.getPageSize(), count);
        }
    }

    @SuppressWarnings("unchecked")
    public Result<Integer> drop(@NotNull Long id) {
        return Optional.ofNullable(id)
            .map(_id -> Result.ofSuccess(getRepository().deleteById(_id)))
            .orElse((Result<Integer>)Result.ofError(
                BizException.of(ErrorCode.PARAMETER_NOT_SET).with("id").getMessage(),
                ErrorCode.PARAMETER_NOT_SET.getCode()));
    }

    @SuppressWarnings("unchecked")
    public Result<BO> get(@NotNull Long id) {
        return Optional.ofNullable(id)
            .map(_id -> Result.ofSuccess(ofBO(getRepository().findById(_id))))
            .orElse(Result.ofError(
                BizException.of(ErrorCode.PARAMETER_NOT_SET).with("id").getMessage(),
                ErrorCode.PARAMETER_NOT_SET.getCode()));
    }

    @SuppressWarnings("unchecked")
    public Result<Integer> save(@NotNull DTO dto) {
        if (dto == null) {
            return Result.ofError("save failed! dto must not be null!");
        } else {
            DO dao = this.ofDO(dto);
            Integer count = this.getRepository().saveOrUpdate(dao);
            if (count > 0) {
                dto.setId(dao.getId());
                return Result.ofSuccess(count);
            } else {
                return Result.ofError("save failed!");
            }
        }
    }

    public ResultList<BO> findAll() {
        List<DO> list = this.getRepository().findAll();
        List<BO> bos = this.convertList(list);
        return ResultList.ofSuccess(bos, 1, bos.size(), bos.size());
    }

    public BO getByID(@NotNull Long id) {
        return get(id).getData();
    }

    public List<BO> listByPagin(@NotNull PaginDTO pagin) {
        if (pagin == null) {
            return null;
        } else {
            List<DO> list = this.getRepository().findByPagin(pagin);
            List<BO> bos = this.convertList(list);
            return bos;
        }
    }


    protected DO ofDO(@NotNull DTO dto) {
        if (dto == null) {
            return null;
        } else {
            DO dao = this.ofDO();
            BeanUtils.copyProperties(dto, dao);
            return dao;
        }
    }

    protected BO ofBO(@NotNull DO dao) {
        if (dao == null) {
            return null;
        } else {
            BO bo = this.ofBO();
            BeanUtils.copyProperties(dao, bo);
            return bo;
        }
    }

    protected List<BO> convertList(List<DO> list) {
        List<BO> bos = new ArrayList<>();
        if (list != null && list.size() != 0) {
            list.forEach((dao) -> {
                BO bo = this.ofBO(dao);
                bos.add(bo);
            });
            return bos;
        } else {
            return bos;
        }
    }

    @SuppressWarnings("unchecked")
    private BO ofBO() {
        Class<BO> boClass = (Class<BO>)this.pt.getActualTypeArguments()[0];

        try {
            return (BO)boClass.newInstance();
        } catch (InstantiationException | IllegalAccessException var3) {
            this.log.error("实体对象BO类型获取失败", var3);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private DO ofDO() {
        Class<DO> boClass = (Class<DO>)this.pt.getActualTypeArguments()[1];

        try {
            return boClass.newInstance();
        } catch (InstantiationException | IllegalAccessException var3) {
            log.error("实体对象DO类型获取失败", var3);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private DTO ofDTO() {
        Class<DTO> boClass = (Class<DTO>)this.pt.getActualTypeArguments()[2];

        try {
            return boClass.newInstance();
        } catch (InstantiationException | IllegalAccessException var3) {
            log.error("实体对象DTO类型获取失败", var3);
        }

        return null;
    }
}
