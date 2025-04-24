package com.erp.server.tms.service.impl;


import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.tms.dto.CfgLogisticsAuthFieldDTO;
import com.erp.model.tms.entity.CfgLogisticsAuthFieldEntity;
import com.erp.server.tms.mapper.CfgLogisticsAuthFieldMapper;
import com.erp.server.tms.service.CfgLogisticsAuthFieldService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 物流商授权字段配置表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
@Slf4j
@Service
public class CfgLogisticsAuthFieldServiceImpl extends SuperServiceImpl<CfgLogisticsAuthFieldMapper, CfgLogisticsAuthFieldEntity> implements CfgLogisticsAuthFieldService {


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgLogisticsAuthFieldDTO.AddDTO addDTO) {
        CfgLogisticsAuthFieldEntity cfgLogisticsAuthFieldEntity = new CfgLogisticsAuthFieldEntity();
        BeanMapperUtils.copy(addDTO, cfgLogisticsAuthFieldEntity);
        boolean save = super.save(cfgLogisticsAuthFieldEntity);
        if (!save) {
            throw new ServiceException("物流商授权字段配置单保存失败");
        }

        return new BaseResultDTO.AddDTO(cfgLogisticsAuthFieldEntity.getId(), cfgLogisticsAuthFieldEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgLogisticsAuthFieldDTO.UpdateDTO updateDTO) {
        CfgLogisticsAuthFieldEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流商授权字段配置单"));
        CfgLogisticsAuthFieldEntity cfgLogisticsAuthFieldEntity = BeanMapperUtils.map(CfgLogisticsAuthFieldEntity.class, updateDTO);

        boolean save = super.updateById(cfgLogisticsAuthFieldEntity);
        if (!save) {
            throw new ServiceException("物流商授权字段配置单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public List<CfgLogisticsAuthFieldDTO.ListDTO> listByLogisticsPlatform(String platform) {
        List<CfgLogisticsAuthFieldEntity> list = this.lambdaQuery().
                eq(CfgLogisticsAuthFieldEntity::getLogisticsPlatform, platform).list();
        List<CfgLogisticsAuthFieldDTO.ListDTO> resultList = BeanMapperUtils.copyList(CfgLogisticsAuthFieldDTO.ListDTO.class,list);
        return resultList;
    }

}
