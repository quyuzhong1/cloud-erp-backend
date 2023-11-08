package com.erp.server.plm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.entity.ProductPropertiesEntity;
import com.erp.server.plm.mapper.ProductPropertiesMapper;
import com.erp.server.plm.service.ProductPropertiesService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.plm.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.ProductPropertiesDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * sku与配置字段关系表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-08
 */
@Slf4j
@Service
public class ProductPropertiesServiceImpl extends SuperServiceImpl<ProductPropertiesMapper, ProductPropertiesEntity> implements ProductPropertiesService {

    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ProductPropertiesDTO.AddDTO addDTO) {
        ProductPropertiesEntity productPropertiesEntity = new ProductPropertiesEntity();
        BeanMapperUtils.copy(addDTO, productPropertiesEntity);

        // 数据处理
        handleData(productPropertiesEntity);

        log.info("开始新增sku与配置字段关系单");
        boolean save = super.save(productPropertiesEntity);
        if(!save) {
            throw new ServiceException("sku与配置字段关系单保存失败");
        }


        return new BaseResultDTO.AddDTO(productPropertiesEntity.getId(), productPropertiesEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ProductPropertiesDTO.UpdateDTO updateDTO) {
        ProductPropertiesEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "sku与配置字段关系单"));
        ProductPropertiesEntity productPropertiesEntity =  BeanMapperUtils.map(ProductPropertiesEntity.class, updateDTO);

        // 数据处理
        handleData(productPropertiesEntity);
        log.info("编辑 开始修改sku与配置字段关系单数据，id：【{}】", old.getId());
        boolean save = super.updateById(productPropertiesEntity);
        if(!save) {
            throw new ServiceException("sku与配置字段关系单保存失败");
        }


        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ProductPropertiesEntity productPropertiesEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
