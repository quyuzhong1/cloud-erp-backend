package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.LoginUser;

import cn.hutool.core.util.StrUtil;
import com.erp.model.plm.entity.BasicProductBuEntity;
import com.erp.server.plm.service.BasicProductBuService;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.model.plm.entity.ProductRefBuEntity;
import com.erp.server.plm.mapper.ProductRefBuMapper;
import com.erp.server.plm.service.ProductRefBuService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.plm.service.OperateLogService;
import com.common.core.exception.ServiceException;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.ProductRefBuDTO;
import javax.annotation.Resource;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Sets;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.SysCodeDTO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.date.DateUtil;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 产品bu信息关联表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2026-01-16
 */
@Slf4j
@Service
public class ProductRefBuServiceImpl extends SuperServiceImpl<ProductRefBuMapper, ProductRefBuEntity> implements ProductRefBuService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private BasicProductBuService basicProductBuService;

    @Override
    public List<ProductRefBuEntity> listByBuId(String id) {
        if(StrUtil.isEmpty(id)) {
            return Collections.emptyList();
        }
        return lambdaQuery().eq(ProductRefBuEntity::getBuId,id).list();
    }

    @Override
    public void addOrUpdate(String productId, String buId) {
        //查询是否存在
        ProductRefBuEntity existing = lambdaQuery()
                .eq(ProductRefBuEntity::getProductId, productId)
                .one();
        if (existing != null) {
            if(!existing.getBuId().equals(buId)) {
                existing.setBuId(buId);
                updateById(existing);
            }
        }else{
            ProductRefBuEntity newEntity = new ProductRefBuEntity();
            newEntity.setProductId(productId);
            newEntity.setBuId(buId);
            save(newEntity);
        }
    }

    @Override
    public List<ProductRefBuEntity> listByProductIds(List<String> productIdList) {
        if(CollUtil.isNotEmpty(productIdList)) {
            List<ProductRefBuEntity> productRefBuEntities = lambdaQuery().in(ProductRefBuEntity::getProductId, productIdList).list();
            if(CollUtil.isEmpty(productRefBuEntities)) {
                return Collections.emptyList();
            }
            List<String> buIds = productRefBuEntities.stream().map(ProductRefBuEntity::getBuId).distinct().collect(Collectors.toList());
            List<BasicProductBuEntity> basicProductBuEntities = basicProductBuService.listByIds(buIds);
            Map<String, String> buIdNameMap = basicProductBuEntities.stream()
                    .collect(Collectors.toMap(BasicProductBuEntity::getId, BasicProductBuEntity::getName));
            for (ProductRefBuEntity productRefBuEntity : productRefBuEntities) {
                productRefBuEntity.setBuName(buIdNameMap.get(productRefBuEntity.getBuId()));
            }
            return productRefBuEntities;
        }

        return Collections.emptyList();
    }

    @Override
    public ProductRefBuEntity getByProductIds(String productId) {
        if(StrUtil.isEmpty(productId)) {
            return null;
        }
        List<ProductRefBuEntity> productRefBuEntities = this.listByProductIds(Collections.singletonList(productId));
        if(CollectionUtils.isEmpty(productRefBuEntities)){
            return null;
        }
        return productRefBuEntities.get(0);
    }

    @Override
    public void removeByProductId(String productId) {
        if (StrUtil.isEmpty(productId)) {
            return;
        }
        remove(new LambdaQueryWrapper<ProductRefBuEntity>().eq(ProductRefBuEntity::getProductId, productId));
    }

    @Override
    public void removeByProductIds(List<String> productIds) {
        if (CollUtil.isEmpty(productIds)) {
            return;
        }
        remove(new LambdaQueryWrapper<ProductRefBuEntity>().in(ProductRefBuEntity::getProductId, productIds));
    }

}
