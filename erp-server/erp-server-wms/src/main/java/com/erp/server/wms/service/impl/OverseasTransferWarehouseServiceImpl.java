package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BaseSelectDTO;
import com.erp.model.wms.entity.OverseasTransferWarehouseEntity;
import com.erp.server.wms.mapper.OverseasTransferWarehouseMapper;
import com.erp.server.wms.service.OverseasTransferWarehouseService;
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
import com.erp.model.wms.dto.OverseasTransferWarehouseDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.util.CollectionUtils;

/**
 * <p>
 * 海外仓签收记录 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
@Slf4j
@Service
public class OverseasTransferWarehouseServiceImpl extends SuperServiceImpl<OverseasTransferWarehouseMapper, OverseasTransferWarehouseEntity> implements OverseasTransferWarehouseService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(OverseasTransferWarehouseDTO.AddDTO addDTO) {
        OverseasTransferWarehouseEntity overseasTransferWarehouseEntity = new OverseasTransferWarehouseEntity();
        BeanMapperUtils.copy(addDTO, overseasTransferWarehouseEntity);

        // 数据处理
        handleData(overseasTransferWarehouseEntity);

        log.info("开始新增海外仓签收记录");
        boolean save = super.save(overseasTransferWarehouseEntity);
        if(!save) {
            throw new ServiceException("海外仓签收记录保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "海外仓签收记录" , overseasTransferWarehouseEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, overseasTransferWarehouseEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(overseasTransferWarehouseEntity.getId(), overseasTransferWarehouseEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(OverseasTransferWarehouseDTO.UpdateDTO updateDTO) {
        OverseasTransferWarehouseEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "海外仓签收记录"));
        OverseasTransferWarehouseEntity overseasTransferWarehouseEntity =  BeanMapperUtils.map(OverseasTransferWarehouseEntity.class, updateDTO);

        // 数据处理
        handleData(overseasTransferWarehouseEntity);
        log.info("编辑 开始修改海外仓签收记录数据，id：【{}】", old.getId());
        boolean save = super.updateById(overseasTransferWarehouseEntity);
        if(!save) {
            throw new ServiceException("海外仓签收记录保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录海外仓签收记录日志数据，id：【{}】", overseasTransferWarehouseEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), overseasTransferWarehouseEntity.getId(), "海外仓签收记录");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, overseasTransferWarehouseEntity, null, overseasTransferWarehouseEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public Boolean saveOrUpdateByPlatform(OverseasTransferWarehouseEntity entity) {
        LambdaUpdateWrapper<OverseasTransferWarehouseEntity> updateWrapper = new LambdaUpdateWrapper<OverseasTransferWarehouseEntity>()
                .eq(OverseasTransferWarehouseEntity::getDictPlatform, entity.getDictPlatform())
                .eq(OverseasTransferWarehouseEntity::getLogisticsProductCode, entity.getLogisticsProductCode())
                .eq(OverseasTransferWarehouseEntity::getPlatformWarehouseCode, entity.getPlatformWarehouseCode())
                .eq(OverseasTransferWarehouseEntity::getPlatformToWarehouseCode, entity.getPlatformToWarehouseCode());
        return this.saveOrUpdate(entity, updateWrapper);
    }

    @Override
    public List<BaseSelectDTO> baseSelectlist(String dictPlatform) {
        List<OverseasTransferWarehouseEntity> list = lambdaQuery()
                .eq(StringUtils.isNotBlank(dictPlatform), OverseasTransferWarehouseEntity::getDictPlatform, dictPlatform)
                .list();
        if(CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        Map<String, List<OverseasTransferWarehouseEntity>> groupMap = list.stream().collect(Collectors.groupingBy(OverseasTransferWarehouseEntity::getPlatformToWarehouseCode));

        return groupMap.values().stream()
                .map(overseasTransferWarehouseEntities -> {
                    OverseasTransferWarehouseEntity e = overseasTransferWarehouseEntities.stream().findFirst().orElse(null);
                    return new BaseSelectDTO(e.getId(), e.getPlatformWarehouseCode(), e.getName());
                }).collect(Collectors.toList());
    }

    @Override
    public List<BaseSelectDTO> LogisticsProductList(String code) {
        List<OverseasTransferWarehouseEntity> list = lambdaQuery()
                .eq(OverseasTransferWarehouseEntity::getPlatformWarehouseCode, code)
                .list();
        if(CollectionUtils.isEmpty(list)){
            return Collections.emptyList();
        }
        return list.stream()
                .map(e-> new BaseSelectDTO(e.getId(), e.getLogisticsProductCode(), e.getLogisticsProductName()))
                .collect(Collectors.toList());
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(OverseasTransferWarehouseEntity overseasTransferWarehouseEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
