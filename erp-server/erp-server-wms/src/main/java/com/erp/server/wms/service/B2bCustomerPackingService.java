package com.erp.server.wms.service;

import cn.hutool.core.collection.CollUtil;
import com.common.business.service.SuperService;
import com.erp.model.wms.dto.B2bCustomerPackingDTO;
import com.erp.model.wms.entity.B2bCustomerPackingEntity;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * B2B客户装箱 服务类
 */
public interface B2bCustomerPackingService extends SuperService<B2bCustomerPackingEntity> {

    List<B2bCustomerPackingEntity> listByMainIds(List<String> mainIds);

    List<B2bCustomerPackingEntity> batchSave(String mainId, List<B2bCustomerPackingDTO.AddDTO> packingList);

    void deleteByMainIds(List<String> mainIds);

    default Map<Integer, B2bCustomerPackingEntity> getBoxHeadMap(List<B2bCustomerPackingEntity> entityList) {
        if (CollUtil.isEmpty(entityList)) {
            return Collections.emptyMap();
        }
        Map<Integer, B2bCustomerPackingEntity> boxHeadMap = new HashMap<>();
        Comparator<B2bCustomerPackingEntity> sortComparator = Comparator.comparing(
                B2bCustomerPackingEntity::getSort, Comparator.nullsLast(Integer::compareTo));
        for (B2bCustomerPackingEntity entity : entityList) {
            if (entity.getBoxSeq() == null) {
                continue;
            }
            boxHeadMap.merge(entity.getBoxSeq(), entity,
                    (head, candidate) -> sortComparator.compare(candidate, head) < 0 ? candidate : head);
        }
        return boxHeadMap;
    }

    default Optional<B2bCustomerPackingEntity> getBoxHead(List<B2bCustomerPackingEntity> entityList, Integer boxSeq) {
        return Optional.ofNullable(getBoxHeadMap(entityList).get(boxSeq));
    }
}
