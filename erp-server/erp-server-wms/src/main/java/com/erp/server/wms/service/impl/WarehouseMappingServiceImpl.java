package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.wms.dto.WarehouseMappingDTO;
import com.erp.model.wms.entity.WarehouseMappingEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.mapper.WarehouseMappingMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.WarehouseMappingService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    @Resource
    private SysUserFeign sysUserFeign;

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

        return new BaseResultDTO.AddDTO(warehouseMappingEntity.getId(), warehouseMappingEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(WarehouseMappingDTO.UpdateDTO updateDTO) {
        WarehouseMappingEntity warehouseMappingEntity =  BeanMapperUtils.map(WarehouseMappingEntity.class, updateDTO);

        // 数据处理
        handleData(warehouseMappingEntity);
        boolean save = super.saveOrUpdate(warehouseMappingEntity);
        if(!save) {
            throw new ServiceException("仓库映射第三方平台单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public List<WarehouseMappingDTO.MappingViewDTO> listMappingViewByWarehouseIds(List<String> warehouseIdList) {
        if (CollectionUtils.isEmpty(warehouseIdList)) {
            return Collections.emptyList();
        }
        List<WarehouseMappingDTO.MappingViewDTO> resultList = baseMapper.listMappingViewByWarehouseIds(warehouseIdList);

        //组织信息
        List<String> orgIds = resultList.stream().map(req -> req.getWarehouseOrgId()).distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIds);
        for (WarehouseMappingDTO.MappingViewDTO mappingViewDTO : resultList) {
            BaseIdDTO.CodeDTO codeDTO = accountingCompanyList.stream().filter(req -> mappingViewDTO.getWarehouseOrgId().equals(req.getId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(codeDTO)) {
                mappingViewDTO.setWarehouseOrgName(codeDTO.getName());
            }
        }
        return resultList;
    }

    @Override
    public List<WarehouseMappingDTO.MappingViewDTO> listMappingViewByDictPlatform(String dictPlatform) {
        List<WarehouseMappingDTO.MappingViewDTO> resultList = baseMapper.listMappingViewByDictPlatform(dictPlatform);

        //组织信息
        List<String> orgIds = resultList.stream().map(req -> req.getWarehouseOrgId()).distinct().collect(Collectors.toList());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIds);
        for (WarehouseMappingDTO.MappingViewDTO mappingViewDTO : resultList) {
            BaseIdDTO.CodeDTO codeDTO = accountingCompanyList.stream().filter(req -> mappingViewDTO.getWarehouseOrgId().equals(req.getId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(codeDTO)) {
                mappingViewDTO.setWarehouseOrgName(codeDTO.getName());
            }
        }

        return resultList;
    }

    @Override
    public WarehouseMappingDTO.MappingViewDTO getMappingViewByDictPlatform(String warehouseId, String dictPlatform) {
        WarehouseMappingDTO.MappingViewDTO mappingViewByDictPlatform = baseMapper.getMappingViewByDictPlatform(warehouseId, dictPlatform);
        if (ObjectUtil.isEmpty(mappingViewByDictPlatform)) {
            return null;
        }
        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(mappingViewByDictPlatform.getWarehouseOrgId()));
        BaseIdDTO.CodeDTO codeDTO = accountingCompanyList.stream().filter(req -> mappingViewByDictPlatform.getWarehouseOrgId().equals(req.getId())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(codeDTO)) {
            mappingViewByDictPlatform.setWarehouseOrgName(codeDTO.getName());
        }
        return mappingViewByDictPlatform;
    }

    @Override
    public WarehouseMappingEntity checkThirdWarehouseNameExist(String thirdWarehouseName, String dictPlatform) {
        if (CharSequenceUtil.isBlank(thirdWarehouseName)) {
            return null;
        }
        WarehouseMappingEntity entity = lambdaQuery()
                .eq(WarehouseMappingEntity::getName, thirdWarehouseName)
                .eq(WarehouseMappingEntity::getDictPlatform, dictPlatform).last("LIMIT 1").one();
        if (ObjectUtil.isNotEmpty(entity)) {
            return entity;
        }
        return null;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(WarehouseMappingEntity warehouseMappingEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
