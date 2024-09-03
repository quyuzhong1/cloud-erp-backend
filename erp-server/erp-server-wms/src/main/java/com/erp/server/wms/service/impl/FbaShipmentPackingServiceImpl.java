package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.common.business.annotation.DataIdempotent;
import com.common.business.utils.CollectionUtils;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentPackingEntity;
import com.erp.server.wms.mapper.FbaShipmentPackingMapper;
import com.erp.server.wms.service.FbaShipmentPackingService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.FbaShipmentService;
import com.erp.server.wms.service.OperateLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FbaShipmentPackingDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

/**
 * <p>
 * fba货件装箱信息 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-09-03
 */
@Slf4j
@Service
public class FbaShipmentPackingServiceImpl extends SuperServiceImpl<FbaShipmentPackingMapper, FbaShipmentPackingEntity> implements FbaShipmentPackingService {
    @Autowired
    private OperateLogService operateLogService;

    @Resource
    private FbaShipmentService fbaShipmentService;

    @Override
    @DataIdempotent(keyIdName = "data.fbaShipmentCode")
    @Transactional(rollbackFor = Exception.class)
    public void handle(FbaShipmentPackingDTO.PackingDTO data) {
        if(Objects.isNull(data) || StringUtils.isBlank(data.getFbaShipmentCode())|| StringUtils.isBlank(data.getBoxNo())){
            return;
        }
        FbaShipmentEntity fbaShipmentEntity = fbaShipmentService.getByCode(data.getFbaShipmentCode());
        if(Objects.isNull(fbaShipmentEntity)){
            return;
        }
        String mainId = fbaShipmentEntity.getId();
        List<FbaShipmentPackingEntity> existList = getByMainIdAndBoxNo(data.getFbaShipmentCode(), data.getBoxNo());
        //已存在，删除后新增
        if(CollectionUtil.isNotEmpty(existList)){
            List<String> removeIds = existList.stream().map(v->v.getId()).collect(Collectors.toList());
            this.removeByIds(removeIds);
        }
    }

    @Override
    public List<FbaShipmentPackingEntity> getByMainIdAndBoxNo(String mainId, String boxNo) {
        if(StringUtils.isBlank(mainId) || StringUtils.isBlank(boxNo)){
            return new ArrayList<>();
        }
        return lambdaQuery().eq(FbaShipmentPackingEntity::getMainId, mainId).eq(FbaShipmentPackingEntity::getBoxNo, boxNo).list();
    }
}
