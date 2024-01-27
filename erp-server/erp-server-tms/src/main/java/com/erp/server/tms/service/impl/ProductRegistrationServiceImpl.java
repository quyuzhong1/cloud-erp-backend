package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.server.tms.mapper.ProductRegistrationMapper;
import com.erp.server.tms.service.ProductRegistrationService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.ProductRegistrationDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 产品备案表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2024-01-19
 */
@Slf4j
@Service
public class ProductRegistrationServiceImpl extends SuperServiceImpl<ProductRegistrationMapper, ProductRegistrationEntity> implements ProductRegistrationService {


    @Override
    public BaseResultDTO.AddDTO add(ProductRegistrationDTO.AddDTO addDTO) {
        ProductRegistrationEntity productRegistrationEntity = new ProductRegistrationEntity();
        BeanMapperUtils.copy(addDTO, productRegistrationEntity);

        // 数据处理
        handleData(productRegistrationEntity);

        log.info("开始新增产品备案单");
        boolean save = super.save(productRegistrationEntity);
        if (!save) {
            throw new ServiceException("产品备案单保存失败");
        }

        return new BaseResultDTO.AddDTO(productRegistrationEntity.getId(), productRegistrationEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ProductRegistrationDTO.UpdateDTO updateDTO) {
        ProductRegistrationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "产品备案单"));
        ProductRegistrationEntity productRegistrationEntity = BeanMapperUtils.map(ProductRegistrationEntity.class, updateDTO);

        // 数据处理
        handleData(productRegistrationEntity);
        log.info("编辑 开始修改产品备案单数据，id：【{}】", old.getId());
        boolean save = super.updateById(productRegistrationEntity);
        if (!save) {
            throw new ServiceException("产品备案单保存失败");
        }

        return Boolean.TRUE;
    }

    @Override
    public List<ProductRegistrationEntity> listBySkuNoList(List<String> skuNoList) {
        if (CollectionUtils.isEmpty(skuNoList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(ProductRegistrationEntity::getSkuNo, skuNoList).list();
    }


    /**
     * 查询是否备案
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean getIsRegistrationByParam(SettingForecastDTO.CheckRegistrationDTO dto) {
        String declarePlatform = dto.getDeclarePlatform();
        List<String> skuNoList = dto.getSkuNoList();
        List<ProductRegistrationEntity> dbList=this.lambdaQuery().
                eq(ProductRegistrationEntity::getDeclarePlatform,declarePlatform).
                in(ProductRegistrationEntity::getSkuNo,skuNoList).list();
        if(CollectionUtils.isEmpty(dbList)){
            return Boolean.FALSE;
        }
        List<String> dbSkuNoList = dbList.stream().map(ProductRegistrationEntity::getSkuNo).collect(Collectors.toList());
        if(dbSkuNoList.containsAll(skuNoList)){
            return  Boolean.TRUE;
        }
        return Boolean.FALSE;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(ProductRegistrationEntity productRegistrationEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
