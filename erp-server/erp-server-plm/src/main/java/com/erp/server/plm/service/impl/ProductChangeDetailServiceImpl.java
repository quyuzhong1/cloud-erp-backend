package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;

import cn.hutool.core.util.StrUtil;
import com.erp.model.plm.entity.ProductChangeEntity;
import com.erp.model.plm.entity.ProductPackEntity;
import com.erp.model.plm.enums.ProductChangeFieldEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.plm.service.ProductChangeService;
import com.erp.server.plm.service.ProductPackService;
import com.erp.model.plm.entity.ProductChangeDetailEntity;
import com.erp.server.plm.mapper.ProductChangeDetailMapper;
import com.erp.server.plm.service.ProductChangeDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.plm.service.OperateLogService;
import com.common.core.exception.ServiceException;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.ProductChangeDetailDTO;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;

/**
 * <p>
 * 产品变更信息表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2026-02-03
 */
@Slf4j
@Service
public class ProductChangeDetailServiceImpl extends SuperServiceImpl<ProductChangeDetailMapper, ProductChangeDetailEntity> implements ProductChangeDetailService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private ProductPackService productPackService;

    @Resource
    private ProductChangeService productChangeService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(ProductChangeEntity productChangeEntity, List<ProductChangeDetailDTO.AddDTO> detailDTOList) {
        detailDTOList.forEach(v->v.setMainId(productChangeEntity.getId()));
        List<ProductChangeDetailEntity> detailEntityList = BeanMapperUtils.copyList(ProductChangeDetailEntity.class, detailDTOList);
        this.checkData(productChangeEntity,detailEntityList);
        productChangeService.buildOldValue(Collections.singletonList(productChangeEntity),detailEntityList);
        boolean save = super.saveBatch(detailEntityList);
        if(!save) {
            throw new ServiceException("产品变更信息明细单保存失败");
        }

        return true;
    }

    @Override
    public void checkData(ProductChangeEntity productChangeEntity, List<ProductChangeDetailEntity> detailEntityList) {
        //如果明细变更的字段包含产品尺寸或箱规，校验长≥宽≥高
        ProductPackEntity productPackEntity = productPackService.getBySkuId(productChangeEntity.getSkuId());
        boolean checkProductSize;
        checkProductSize = detailEntityList.stream().anyMatch(v->
                ProductChangeFieldEnum.PRODUCT_LENGTH.getCode().equals(v.getField())
        || ProductChangeFieldEnum.PRODUCT_WIDTH.getCode().equals(v.getField())
        ||  ProductChangeFieldEnum.PRODUCT_HEIGHT.getCode().equals(v.getField()));
        if(checkProductSize){
            BigDecimal newProductLength = detailEntityList.stream().filter(v->ProductChangeFieldEnum.PRODUCT_LENGTH.getCode().equals(v.getField())).map(ProductChangeDetailEntity::getNewValue).filter(ObjectUtil::isNotEmpty).map(BigDecimal::new).findFirst().orElse(productPackEntity.getProductLength());
            BigDecimal newProductWidth = detailEntityList.stream().filter(v->ProductChangeFieldEnum.PRODUCT_WIDTH.getCode().equals(v.getField())).map(ProductChangeDetailEntity::getNewValue).filter(ObjectUtil::isNotEmpty).map(BigDecimal::new).findFirst().orElse(productPackEntity.getProductWidth());
            BigDecimal newProductHeight = detailEntityList.stream().filter(v->ProductChangeFieldEnum.PRODUCT_HEIGHT.getCode().equals(v.getField())).map(ProductChangeDetailEntity::getNewValue).filter(ObjectUtil::isNotEmpty).map(BigDecimal::new).findFirst().orElse(productPackEntity.getProductHeight());
            if(newProductLength.compareTo(newProductWidth)<0 || newProductWidth.compareTo(newProductHeight)<0){
                throw new ServiceException(ApiError.PRODUCT_CHANGE_PRODUCT_SIZE_CHANGE);
            }

        }
        boolean checkBoxSize;
        checkBoxSize = detailEntityList.stream().anyMatch(v->
                ProductChangeFieldEnum.BOX_LENGTH.getCode().equals(v.getField())
                        || ProductChangeFieldEnum.BOX_WIDTH.getCode().equals(v.getField())
                        ||  ProductChangeFieldEnum.BOX_HEIGHT.getCode().equals(v.getField()));
        if(checkBoxSize){
            BigDecimal newBoxLength = detailEntityList.stream().filter(v->ProductChangeFieldEnum.BOX_LENGTH.getCode().equals(v.getField())).map(ProductChangeDetailEntity::getNewValue).filter(ObjectUtil::isNotEmpty).map(BigDecimal::new).findFirst().orElse(productPackEntity.getBoxLength());
            BigDecimal newBoxWidth = detailEntityList.stream().filter(v->ProductChangeFieldEnum.BOX_WIDTH.getCode().equals(v.getField())).map(ProductChangeDetailEntity::getNewValue).filter(ObjectUtil::isNotEmpty).map(BigDecimal::new).findFirst().orElse(productPackEntity.getBoxWidth());
            BigDecimal newBoxHeight = detailEntityList.stream().filter(v->ProductChangeFieldEnum.BOX_HEIGHT.getCode().equals(v.getField())).map(ProductChangeDetailEntity::getNewValue).filter(ObjectUtil::isNotEmpty).map(BigDecimal::new).findFirst().orElse(productPackEntity.getBoxHeight());
            if(newBoxLength.compareTo(newBoxWidth)<0 || newBoxWidth.compareTo(newBoxHeight)<0){
                throw new ServiceException(ApiError.PRODUCT_CHANGE_BOX_SIZE_CHANGE);
            }
        }

    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ProductChangeEntity productChangeEntity ,List<ProductChangeDetailDTO.UpdateDTO> updateDTOList) {
        //查询数据库数据
        List<ProductChangeDetailEntity> dbList = this.listByMains(Lists.newArrayList(productChangeEntity.getId()));
        List<String> dbIdList = dbList.stream().map(ProductChangeDetailEntity::getId).collect(Collectors.toList());
        List<ProductChangeDetailEntity> addOrUpdateList = new ArrayList<>();
        //删除
        List<String> updateIdList = updateDTOList.stream().filter(v-> ObjectUtil.isNotEmpty(v.getId())).map(ProductChangeDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> delIdList = dbIdList.stream().filter(v->!updateIdList.contains(v)).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(delIdList)){
            //记录日志
            String msg = StrUtil.format("用户【{}】删除了id为【{}】的【{}】 ", UserContext.getDefaultLoginUser().getUserName(),delIdList, "产品变更信息单明细");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PRODUCT_CHANGE.getCode(), productChangeEntity.getId(), "删除明细");

            boolean remove = this.removeByIds(delIdList);
            if(!remove) {
                throw new ServiceException("产品变更信息明细单删除失败");
            }
        }
        //新增
        List<ProductChangeDetailDTO.AddDTO> addDTOList = updateDTOList.stream().filter(v-> ObjectUtil.isEmpty(v.getId())).map(v->{
            ProductChangeDetailDTO.AddDTO addDTO = BeanUtil.toBean(v, ProductChangeDetailDTO.AddDTO.class);
            addDTO.setMainId(productChangeEntity.getId());
            return addDTO;
        }).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(addDTOList)){
            List<ProductChangeDetailEntity> addEntityList = BeanMapperUtils.copyList(ProductChangeDetailEntity.class, addDTOList);
            for (ProductChangeDetailEntity productChangeDetailEntity : addEntityList) {
                //记录日志
                String msg = StrUtil.format("用户【{}】新增了字段为【{}】的【{}】", UserContext.getDefaultLoginUser().getUserName(), Objects.requireNonNull(ProductChangeFieldEnum.getByEntityField(productChangeDetailEntity.getField())).getName(), "产品变更信息单明细");
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PRODUCT_CHANGE.getCode(), productChangeEntity.getId(), "新增明细");
            }
            addOrUpdateList.addAll(addEntityList);
        }
        //更新
        List<ProductChangeDetailDTO.UpdateDTO> needUpdateList = updateDTOList.stream().filter(v-> ObjectUtil.isNotEmpty(v.getId())).collect(Collectors.toList());
        for (ProductChangeDetailDTO.UpdateDTO updateDTO : needUpdateList) {
            ProductChangeDetailEntity productChangeDetailEntity = BeanUtil.toBean(updateDTO, ProductChangeDetailEntity.class);
            ProductChangeDetailEntity old = dbList.stream().filter(v->v.getId().equals(productChangeDetailEntity.getId())).findFirst().orElse(null);
            addOrUpdateList.add(productChangeDetailEntity);
            // 记录子单操作日志
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), productChangeDetailEntity.getId(), "产品变更信息单明细");
            operateLogService.addSysLogByUpdate(old, productChangeDetailEntity, String.valueOf(ProductChangeDetailEntity.class), productChangeEntity.getId(),productChangeDetailEntity.getId(), msg);
        }
        if(CollectionUtil.isNotEmpty(addOrUpdateList)){
            this.checkData(productChangeEntity,addOrUpdateList);
            super.saveOrUpdateBatch(addOrUpdateList);
        }
        return Boolean.TRUE;
    }

    @Override
    public List<ProductChangeDetailEntity> listByMains(List<String> mainIds) {
        if(CollectionUtil.isEmpty(mainIds)){
            return new ArrayList<>();
        }

        return lambdaQuery().in(ProductChangeDetailEntity::getMainId,mainIds).list();
    }

    @Override
    public void deleteByMainId(String mainId) {
        lambdaUpdate().eq(ProductChangeDetailEntity::getMainId,mainId).remove();
    }

}
