package com.erp.server.plm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.erp.model.plm.entity.*;
import com.erp.model.plm.enums.ProductChangeFieldEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.service.*;
import com.erp.server.plm.mapper.ProductChangeDetailMapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
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

    @Resource
    private BasicProductBuService basicProductBuService;

    @Resource
    private BasicDictService basicDictService;

    @Resource
    private BasicCategoryService basicCategoryService;

    @Resource
    private ProductRDTTeamService productRDTTeamService;

    @Resource
    private ProductBrandService productBrandService;

    @Resource
    private ApplicationCategoryService applicationCategoryService;

    @Resource
    private SysFeign sysFeign;

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
            List<ProductChangeDetailEntity> delList = dbList.stream().filter(v->delIdList.contains(v.getId())).collect(Collectors.toList());
            List<String> delFieldList = delList.stream().map(v-> Objects.requireNonNull(ProductChangeFieldEnum.getByEntityField(v.getField())).getName()).collect(Collectors.toList());
            //记录日志
            String msg = StrUtil.format("用户【{}】删除了字段为【{}】的【{}】 ", UserContext.getDefaultLoginUser().getUserName(),delFieldList, "产品变更信息单明细");
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
        if(CollectionUtil.isNotEmpty(needUpdateList)){
            List<BasicDictEntity> basicDictList = basicDictService.list();
            List<BasicCategoryEntity> basicCategoryEntities = basicCategoryService.list();
            List<ProductRDTTeamEntity> productRDTTeamEntities = productRDTTeamService.list();
            List<ProductBrandEntity> productBrandEntities = productBrandService.list();
            List<ApplicationCategoryEntity> applicationCategoryEntities = applicationCategoryService.list();
            List<BasicProductBuEntity> basicProductBuEntities = basicProductBuService.list();
            List<DictCountryDTO.ListDTO> countryList = sysFeign.countryList().getData();


            StringBuilder msg = new StringBuilder();
            for (ProductChangeDetailDTO.UpdateDTO updateDTO : needUpdateList) {
                ProductChangeDetailEntity productChangeDetailEntity = BeanUtil.toBean(updateDTO, ProductChangeDetailEntity.class);
                ProductChangeDetailEntity old = dbList.stream().filter(v->v.getId().equals(productChangeDetailEntity.getId())).findFirst().orElse(new ProductChangeDetailEntity());
                addOrUpdateList.add(productChangeDetailEntity);
                String fieldName = Objects.requireNonNull(ProductChangeFieldEnum.getByEntityField(productChangeDetailEntity.getField())).getName();
                if(!productChangeDetailEntity.getField().equals(old.getField())){
                    String oldFieldName = Objects.requireNonNull(ProductChangeFieldEnum.getByEntityField(old.getField())).getName();
                    msg.append("编辑了变更字段由【").append(oldFieldName).append("】改为【").append(fieldName).append("】。");
                }else if (!productChangeDetailEntity.getNewValue().equals(old.getNewValue())){
                    ProductChangeFieldEnum productChangeFieldEnum = Objects.requireNonNull(ProductChangeFieldEnum.getByEntityField(productChangeDetailEntity.getField()));
                    String[] convertedValues = productChangeService.convertFieldValue(
                            productChangeFieldEnum,
                            old.getNewValue(),
                            productChangeDetailEntity.getNewValue(),
                            basicDictList,
                            basicCategoryEntities,
                            productRDTTeamEntities,
                            productBrandEntities,
                            applicationCategoryEntities,
                            basicProductBuEntities,
                            countryList
                    );
                    msg.append("编辑了变更字段:").append(fieldName).append("，变更新值由【").append(convertedValues[0]).append("】改为【").append(convertedValues[1]).append("】。");
                }
                if(!productChangeDetailEntity.getRemark().equals(old.getRemark())){
                    msg.append("编辑了备注由【").append(old.getRemark()).append("】改为【").append(productChangeDetailEntity.getRemark()).append("】。");
                }

            }
            if(StringUtils.isNotBlank(msg)){
                operateLogService.addModuleOperateLog(msg.toString(), ModuleTypeEnum.PRODUCT_CHANGE.getCode(), productChangeEntity.getId(), "编辑明细");
            }
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
