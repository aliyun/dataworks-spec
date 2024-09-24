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

package com.aliyun.migrationx.common.utils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author 聿剑
 * @date 2022/06/22
 */
@Slf4j
public class PaginateUtils {
    @Data
    @ToString
    @Accessors(chain = true)
    public static class Paginator {
        int pageNum = 1;
        int pageSize = 20;
    }

    @Data
    @ToString(callSuper = true, exclude = "data")
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class PaginateResult<T> extends Paginator {
        List<T> data;
        int totalCount;
    }

    public static <T> void doPaginate(Paginator paginator, Function<Paginator, PaginateResult<T>> doPaginate)
        throws InterruptedException {
        int pageNum = Optional.ofNullable(paginator).map(Paginator::getPageNum).orElse(1);
        int pageSize = Optional.ofNullable(paginator).map(Paginator::getPageSize).orElse(20);
        paginator = Optional.ofNullable(paginator).orElse(new Paginator());

        while (true) {
            if (Thread.interrupted()) {
                throw new InterruptedException("interrupted");
            }

            paginator.setPageNum(pageNum);
            paginator.setPageSize(pageSize);

            log.info("paginator: {}", paginator);
            PaginateResult<T> page = doPaginate.apply(paginator);
            log.info("paginate result: {}", page);

            if (CollectionUtils.isEmpty(page.getData()) || CollectionUtils.size(page.getData()) < pageSize) {
                log.info("paginator complete");
                break;
            }

            pageNum++;
        }
    }
}
