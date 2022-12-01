package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.common.core.utils.BeanMapper;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.dto.ProductLogisticsDTO;
import com.erp.model.plm.dto.ProductLogisticsShowDTO;
import com.erp.model.plm.entity.ProductLogisticsEntity;
import com.erp.server.plm.mapper.ProductLogisticsMapper;
import com.erp.server.plm.service.ProductLogisticsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 产品物流信息服务类
 * @Author Luo_WG
 * @Date 2022/9/23 15:35
 **/
@Service
public class ProductLogisticsServiceImpl extends ServiceImpl<ProductLogisticsMapper, ProductLogisticsEntity>
    implements ProductLogisticsService {

    @Resource
    private ProductLogisticsMapper productLogisticsMapper;

    /**
     * @Description 产品物流信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductLogisticsShowDTO>
     **/
    @Override
    public List<ProductLogisticsShowDTO> list(String productId) {
        return productLogisticsMapper.list(productId);
    }

    /**
     * @Description 保存/修改产品物流信息
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productLogisticsDTO 产品物流信息表
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdate(ProductLogisticsDTO productLogisticsDTO) {
        ProductLogisticsEntity logisticsEntity = new ProductLogisticsEntity();
        BeanMapper.copy(productLogisticsDTO, logisticsEntity);
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        if (ObjectUtils.isNotEmpty(loginUser)) {
            if (StringUtils.isBlank(productLogisticsDTO.getId())) {
                logisticsEntity.setCreateUserId(loginUser.getUid());
                logisticsEntity.setCreateUserName(loginUser.getUserName());
            } else {
                logisticsEntity.setUpdateUserId(loginUser.getUid());
                logisticsEntity.setUpdateUserName(loginUser.getUserName());
            }
        }
        return this.saveOrUpdate(logisticsEntity);
    }

    /**
     * @Description 保存/修改产品物流信息-批量操作
     * @Author Luo_WG
     * @Date 2022/9/26 18:15
     * @param productLogisticsList 产品物流信息表
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateBatch(List<ProductLogisticsDTO> productLogisticsList) {
        List<ProductLogisticsEntity> list = BeanMapper.copyList(productLogisticsList, ProductLogisticsEntity.class);
        return this.saveOrUpdateBatch(list);
    }

    /**
     * @Description 删除产品物流信息
     * @Author Luo_WG
     * @Date 2022/9/26 18:42
     * @param skuId 产品sku明细表id
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean removeLogistics(String skuId) {
        LambdaQueryWrapper<ProductLogisticsEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(ProductLogisticsEntity::getSkuId, skuId);
        return this.remove(queryWrapper);
    }
}




