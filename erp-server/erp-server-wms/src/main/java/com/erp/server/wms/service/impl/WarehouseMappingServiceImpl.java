package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.WarehouseMappingDTO;
import com.erp.model.wms.entity.WarehouseMappingEntity;
import com.erp.server.wms.mapper.WarehouseMappingMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WarehouseMappingService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
/**
 * <p>
 * 仓库映射第三方平台表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-30
 */
@Slf4j
@Service
public class WarehouseMappingServiceImpl extends SuperServiceImpl<WarehouseMappingMapper, WarehouseMappingEntity> implements WarehouseMappingService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(WarehouseMappingDTO.AddDTO addDTO) {
        WarehouseMappingEntity warehouseMappingEntity = new WarehouseMappingEntity();
        BeanMapperUtils.copy(addDTO, warehouseMappingEntity);

        // 数据处理
        handleData(warehouseMappingEntity);

        log.info("开始新增仓库映射第三方平台单");
        boolean save = super.save(warehouseMappingEntity);
        if(!save) {
            throw new ServiceException("仓库映射第三方平台单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "仓库映射第三方平台单" , warehouseMappingEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, warehouseMappingEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(warehouseMappingEntity.getId(), warehouseMappingEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(WarehouseMappingDTO.UpdateDTO updateDTO) {
        WarehouseMappingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "仓库映射第三方平台单"));
        WarehouseMappingEntity warehouseMappingEntity =  BeanMapperUtils.map(WarehouseMappingEntity.class, updateDTO);

        // 数据处理
        handleData(warehouseMappingEntity);
        log.info("编辑 开始修改仓库映射第三方平台单数据，id：【{}】", old.getId());
        boolean save = super.updateById(warehouseMappingEntity);
        if(!save) {
            throw new ServiceException("仓库映射第三方平台单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录仓库映射第三方平台单日志数据，id：【{}】", warehouseMappingEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), warehouseMappingEntity.getId(), "仓库映射第三方平台单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, warehouseMappingEntity, null, warehouseMappingEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<WarehouseMappingDTO.MappingViewDTO> listMappingViewByWarehouseIds(List<String> warehouseIdList) {
        if (CollectionUtils.isEmpty(warehouseIdList)) {
            return Collections.emptyList();
        }

        List<WarehouseMappingDTO.MappingViewDTO> resultList = baseMapper.listMappingViewByWarehouseIds(warehouseIdList);
        return resultList;
    }

    @Override
    public List<WarehouseMappingDTO.MappingViewDTO> listMappingViewByDictPlatform(String dictPlatform) {
        List<WarehouseMappingDTO.MappingViewDTO> resultList = baseMapper.listMappingViewByDictPlatform(dictPlatform);
        return resultList;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(WarehouseMappingEntity warehouseMappingEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
