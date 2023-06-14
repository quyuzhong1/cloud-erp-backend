package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.dto.SubcontractOrderDetailDTO;
import com.erp.model.scm.entity.SubcontractOrderDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.scm.mapper.SubcontractOrderDetailMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.PurchasePriceDetailService;
import com.erp.server.scm.service.SubcontractOrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 委外订单明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-06-08
 */
@Slf4j
@Service
public class SubcontractOrderDetailServiceImpl extends SuperServiceImpl<SubcontractOrderDetailMapper, SubcontractOrderDetailEntity> implements SubcontractOrderDetailService {

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;



    @Override
    public void updateArrivalStatusByIds(String arrivalStatus, List<String> ids) {
        lambdaUpdate()
                .in(SubcontractOrderDetailEntity::getId,ids)
                .set(SubcontractOrderDetailEntity::getArrivalStatus,arrivalStatus)
                .set(SubcontractOrderDetailEntity::getArrivalTime, LocalDateTime.now())
                .set(SubcontractOrderDetailEntity::getIsEndReceive,Boolean.TRUE)
                .update();
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {
        lambdaUpdate()
                .in(SubcontractOrderDetailEntity::getMainId,mainIds)
                .remove();
    }

    @Override
    public void add(List<SubcontractOrderDetailDTO.AddDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<SubcontractOrderDetailEntity> list = new ArrayList<>();
        for (SubcontractOrderDetailDTO.AddDTO addDTO : detailList) {
            SubcontractOrderDetailEntity entity = BeanMapperUtils.map(SubcontractOrderDetailEntity.class, addDTO);
            List<SubcontractOrderDetailDTO.AddDTO> addList = addDTO.getChildList();
            List<SubcontractOrderDetailDTO.UpdateDTO> updateList = BeanMapperUtils.copyList(SubcontractOrderDetailDTO.UpdateDTO.class, addList);
            entity.setChildList(updateList);
        }
        //处理父子级数据
        List<SubcontractOrderDetailEntity> resultList = generateResultDetail(list, mainId);
        this.saveBatch(resultList);
    }

    @Override
    public void update(List<SubcontractOrderDetailDTO.UpdateDTO> detailList, String mainId) {
        if (CollectionUtils.isEmpty(detailList)) {
            return;
        }
        List<SubcontractOrderDetailEntity> list = BeanMapperUtils.copyList(SubcontractOrderDetailEntity.class, detailList);

        //原明细数据
        List<SubcontractOrderDetailEntity> oldList = this.listParentByMainId(mainId);
        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SubcontractOrderDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("删除了一个父级SKU【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        //处理父子级数据
        List<SubcontractOrderDetailEntity> resultList = generateResultDetail(list, mainId);

        this.saveOrUpdateBatch(resultList);
    }

    @Override
    public List<SubcontractOrderDetailEntity> listByMainId(String mainId) {
       return lambdaQuery().eq(SubcontractOrderDetailEntity::getMainId,mainId).list();
    }

    @Override
    public List<SubcontractOrderDetailEntity> listByParentId(String detailId) {
        return lambdaQuery()
                .eq(SubcontractOrderDetailEntity::getParentId,detailId)
                .list();
    }

    @Override
    public List<SubcontractOrderDetailEntity> listByMainIdAndSku(String mainId, List<String> skuNoList) {
        return lambdaQuery()
                .eq(SubcontractOrderDetailEntity::getMainId,mainId)
                .in(CollectionUtils.isNotEmpty(skuNoList),SubcontractOrderDetailEntity::getSkuNo,skuNoList)
                .list();
    }

    @Override
    public List<SubcontractOrderDetailEntity> listBySourceDetailIds(List<String> sourceDetailIds) {
        return  baseMapper.listBySourceDetailIds(sourceDetailIds);
    }

    private List<SubcontractOrderDetailEntity> listParentByMainId(String mainId) {
        return lambdaQuery()
                .eq(SubcontractOrderDetailEntity::getMainId,mainId)
                .eq(SubcontractOrderDetailEntity::getParentId,"")
                .list();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<SubcontractOrderDetailDTO.UpdateDTO> newList, List<SubcontractOrderDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SubcontractOrderDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SubcontractOrderDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }


    /**
     * 处理明细中的数据id
     */
    private List<SubcontractOrderDetailEntity> generateResultDetail (List<SubcontractOrderDetailEntity> newList, String mainId) {
        List<SubcontractOrderDetailEntity> resultList = new ArrayList<>();
        List<String> skuIds = new ArrayList<>();
        newList.forEach(obj -> {
            skuIds.add(obj.getSkuId());
            List<String> skuIdList = obj.getChildList().stream().map(SubcontractOrderDetailDTO.UpdateDTO::getSkuId).collect(Collectors.toList());
            skuIds.addAll(skuIdList);
        });

        //BOM信息
        List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        if (CollectionUtils.isEmpty(bomChildrenList)) {
            throw new ServiceException(ApiError.ERROR_95163);
        }
        //产品信息
        List<SkuVO> skuList = plmTaskFeign.getSkuInfoByIds(skuIds);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException(ApiError.ERROR_95084);
        }

        for (SubcontractOrderDetailEntity detailEntity : newList) {
            //bom信息
            BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenList.stream().filter(obj -> obj.getParentSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(bomChildrenSkuDTO)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            //父级SKU信息
            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(skuVO)) {
                throw new ServiceException(ApiError.ERROR_95084);
            }
            //新增数据手动添加ID
            if (StringUtils.isBlank(detailEntity.getId())) {
                detailEntity.setId(IdWorker.getIdStr());
                detailEntity.setIsAdd(Boolean.TRUE);
            }
            detailEntity.setMainId(mainId);
            detailEntity.setVariantProperty(skuVO.getVariantProperty());
            detailEntity.setSkuNo(skuVO.getSkuNo());
            detailEntity.setBomVersion(bomChildrenSkuDTO.getBomVersion());
            handleSupplierTaxPrice(detailEntity,Boolean.FALSE);
            //子集SKU信息
            List<SubcontractOrderDetailEntity>   childList = BeanMapperUtils.copyList(SubcontractOrderDetailEntity.class, detailEntity.getChildList());
            for (SubcontractOrderDetailEntity childEntity : childList) {
                //产品信息
                SkuVO childSkuVO = skuList.stream().filter(obj -> obj.getSkuId().equals(childEntity.getSkuId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(childSkuVO)) {
                    throw new ServiceException(ApiError.ERROR_95084);
                }
                childEntity.setMainId(mainId);
                childEntity.setParentId(detailEntity.getId());
                childEntity.setVariantProperty(childSkuVO.getVariantProperty());
                childEntity.setSkuNo(childSkuVO.getSkuNo());
                childEntity.setBomVersion(bomChildrenSkuDTO.getBomVersion());
                handleSupplierTaxPrice(childEntity,Boolean.TRUE);
            }
            resultList.add(detailEntity);
            resultList.addAll(childList);

        }
        //添加修改操作日志
        for (SubcontractOrderDetailEntity resultEntity : resultList) {
            SubcontractOrderDetailEntity old = this.getById(resultEntity.getId());
            if (ObjectUtils.isNotEmpty(old)) {
                moduleOperateLogService.addModuleOperateLogByObj(old,resultEntity, ModuleTypeEnum.SUBCONTRACT_ORDER.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
        //新增SKU添加操作日志
        List<SubcontractOrderDetailEntity> addList = newList.stream().filter(c ->c.getIsAdd()).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList)) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("新增了一条父级SKU【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(), addPairList, "编辑操作");
        }
        return resultList;
    }

    /**
     * @description: 处理供应商报价
     * @author Will
     * @date: 2023/6/14 17:07
     * @param entity
     * @param isChild
     */
    private void handleSupplierTaxPrice(SubcontractOrderDetailEntity entity,Boolean isChild) {
        //供应商报价信息
        PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO searchDTO = new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO();
        searchDTO.setSkuId(entity.getSkuId());
        searchDTO.setSupplierId(entity.getSupplierId());
        searchDTO.setPurchaseQty(entity.getQty());
        searchDTO.setSkuNo(entity.getSkuNo());
        List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> taxPriceList = purchasePriceDetailService.getTaxPrice(searchDTO);
        PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO viewDTO = taxPriceList.get(0);
        entity.setCurrency(viewDTO.getCurrency());
        entity.setCurrencySymbol(viewDTO.getCurrencySymbol());
        //子件SKU默认取供应商报价
        if (isChild) {
            entity.setPrice(viewDTO.getTaxPrice());
        }
        entity.setAmount(MathUtil.multiply(entity.getPrice(),entity.getQty()));
    }
}
