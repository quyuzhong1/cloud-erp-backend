package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.wms.entity.FbaShipmentDetailEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.server.wms.mapper.FbaShipmentReceiveMapper;
import com.erp.server.wms.service.FbaShipmentReceiveService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FbaShipmentReceiveDTO;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.util.CollectionUtils;

/**
 * <p>
 * FBA货件签收信息 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-11-01
 */
@Slf4j
@Service
public class FbaShipmentReceiveServiceImpl extends SuperServiceImpl<FbaShipmentReceiveMapper, FbaShipmentReceiveEntity> implements FbaShipmentReceiveService {

    @Override
    public List<FbaShipmentReceiveEntity> listByDetailIds(List<String> detailIds) {
        if (CollectionUtils.isEmpty(detailIds)){
            return Collections.emptyList();
        }
        return lambdaQuery().in(FbaShipmentReceiveEntity::getDetailId, detailIds).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FbaShipmentReceiveEntity> checkAndSetReceiveSkuMapping(List<FbaShipmentDetailEntity> oldDetailEntityList, List<FbaShipmentReceiveEntity> sourceReceiveEntityList) {
        Map<Boolean, List<FbaShipmentReceiveEntity>> gourpMap = sourceReceiveEntityList.stream()
                .collect(Collectors.groupingBy(e -> StringUtils.isBlank(e.getSkuId()) || StringUtils.isBlank(e.getSkuNo())));
        // 签收记录丢失映射关系的
        List<FbaShipmentReceiveEntity> missingSkuMappingReceiveList = gourpMap.get(true);
        if (CollectionUtils.isEmpty(missingSkuMappingReceiveList)){
            return sourceReceiveEntityList;
        }

        // 检查详情是否都有映射
        FbaShipmentDetailEntity missingSkuMappingEntity = oldDetailEntityList.stream().filter(e -> StringUtils.isBlank(e.getSkuId()) || StringUtils.isBlank(e.getSkuNo())).findFirst().orElse(null);
        if (null != missingSkuMappingEntity){
            String msg = StrUtil.format("【FBA货件更新】未找到平台sku【{}】映射数据", missingSkuMappingEntity.getMsku());
            throw new ServiceException(msg);
        }
        Map<String, FbaShipmentDetailEntity> detailEntityMap = oldDetailEntityList.stream().collect(Collectors.toMap(FbaShipmentDetailEntity::getMsku, Function.identity()));

        missingSkuMappingReceiveList.forEach(e-> {
            FbaShipmentDetailEntity detailEntity = detailEntityMap.get(e.getMsku());
            if (null == detailEntity){
                throw new ServiceException("签收记录：丢失详情，msku=" +  e.getMsku());
            }
            e.setSkuId(detailEntity.getSkuId());
            e.setSkuNo(detailEntity.getSkuNo());
        });

        if (!this.updateBatchById(missingSkuMappingReceiveList)){
            throw new ServiceException("批量更新签收记录失败");
        }
        // 已匹配关系的签收记录
        List<FbaShipmentReceiveEntity> alreadySkuMappingReceiveEntityList = gourpMap.get(false);
        if (CollectionUtils.isEmpty(alreadySkuMappingReceiveEntityList)){
            return missingSkuMappingReceiveList;
        }
        missingSkuMappingReceiveList.addAll(alreadySkuMappingReceiveEntityList);
        return missingSkuMappingReceiveList;
    }
}
