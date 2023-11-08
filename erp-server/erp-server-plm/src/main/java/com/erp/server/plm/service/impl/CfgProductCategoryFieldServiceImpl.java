package com.erp.server.plm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.entity.CfgProductCategoryFieldEntity;
import com.erp.server.plm.mapper.CfgProductCategoryFieldMapper;
import com.erp.server.plm.service.CfgProductCategoryFieldService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.plm.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.CfgProductCategoryFieldDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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

    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgProductCategoryFieldDTO.AddDTO addDTO) {
        CfgProductCategoryFieldEntity cfgProductCategoryFieldEntity = new CfgProductCategoryFieldEntity();
        BeanMapperUtils.copy(addDTO, cfgProductCategoryFieldEntity);

        // 数据处理
        handleData(cfgProductCategoryFieldEntity);

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
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "产品分类字段配置单"));
        CfgProductCategoryFieldEntity cfgProductCategoryFieldEntity =  BeanMapperUtils.map(CfgProductCategoryFieldEntity.class, updateDTO);

        // 数据处理
        handleData(cfgProductCategoryFieldEntity);
        log.info("编辑 开始修改产品分类字段配置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgProductCategoryFieldEntity);
        if(!save) {
            throw new ServiceException("产品分类字段配置单保存失败");
        }

        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgProductCategoryFieldEntity cfgProductCategoryFieldEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
