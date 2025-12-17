package com.erp.server.plm.service.impl;


import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.plm.dto.CfgProductCategoryFieldDTO;
import com.erp.model.plm.entity.CfgProductCategoryFieldEntity;
import com.erp.server.plm.mapper.CfgProductCategoryFieldMapper;
import com.erp.server.plm.service.CfgProductCategoryFieldService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 * 产品分类字段配置表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-06
 */
@Slf4j
@Service
public class CfgProductCategoryFieldServiceImpl extends SuperServiceImpl<CfgProductCategoryFieldMapper, CfgProductCategoryFieldEntity> implements CfgProductCategoryFieldService {


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgProductCategoryFieldDTO.AddDTO addDTO) {
        CfgProductCategoryFieldEntity cfgProductCategoryFieldEntity = new CfgProductCategoryFieldEntity();
        BeanMapperUtils.copy(addDTO, cfgProductCategoryFieldEntity);

        boolean save = super.save(cfgProductCategoryFieldEntity);
        if(!save) {
            throw new ServiceException("产品分类字段配置单保存失败");
        }

        return new BaseResultDTO.AddDTO(cfgProductCategoryFieldEntity.getId(), cfgProductCategoryFieldEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgProductCategoryFieldDTO.UpdateDTO updateDTO) {
        CfgProductCategoryFieldEntity old = super.getById(updateDTO.getId());
        CfgProductCategoryFieldEntity oldEntity = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "产品分类字段配置单"));
        CfgProductCategoryFieldEntity cfgProductCategoryFieldEntity =  BeanMapperUtils.map(CfgProductCategoryFieldEntity.class, updateDTO);

        log.info("编辑 开始修改产品分类字段配置单数据，id：【{}】", oldEntity.getId());
        boolean save = super.updateById(cfgProductCategoryFieldEntity);
        if(!save) {
            throw new ServiceException("产品分类字段配置单保存失败");
        }

        return Boolean.TRUE;
    }

}
