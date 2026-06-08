package com.erp.server.wms.service;

import cn.hutool.core.collection.CollUtil;
import com.common.business.service.SuperService;
import com.erp.model.wms.dto.B2bCustomerPackingDTO;
import com.erp.model.wms.entity.B2bCustomerPackingEntity;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * B2B客户装箱 服务类
 */
public interface B2bCustomerPackingService extends SuperService<B2bCustomerPackingEntity> {

    List<B2bCustomerPackingEntity> listByMainIds(List<String> mainIds);

    List<B2bCustomerPackingEntity> batchSave(String mainId, List<B2bCustomerPackingDTO.AddDTO> packingList);

    void deleteByMainIds(List<String> mainIds);

    default Optional<B2bCustomerPackingEntity> getBoxHead(List<B2bCustomerPackingEntity> entityList, Integer boxSeq) {
        if (CollUtil.isEmpty(entityList)) {
            return Optional.empty();
        }
        return entityList.stream()
                .filter(entity -> Objects.equals(boxSeq, entity.getBoxSeq()))
                .min(Comparator.comparing(B2bCustomerPackingEntity::getSort, Comparator.nullsLast(Integer::compareTo)));
    }
}
