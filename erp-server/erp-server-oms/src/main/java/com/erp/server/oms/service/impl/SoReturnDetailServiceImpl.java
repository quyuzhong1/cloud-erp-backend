package com.erp.server.oms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoReturnDetailEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoReturnReceiveDetailEntity;
import com.erp.model.wms.enums.ReturnReasonEnum;
import com.erp.model.wms.enums.ReturnTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.plm.feign.BomSkuFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.*;
import com.erp.server.oms.mapper.SoReturnDetailMapper;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 退货单详情 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-05-10
 */
@Slf4j
@Service
public class SoReturnDetailServiceImpl extends SuperServiceImpl<SoReturnDetailMapper, SoReturnDetailEntity> implements SoReturnDetailService {

    @Resource
    private SoDetailService soDetailService;

    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private InventoryFeign inventoryFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoReturnReceiveFeign soReturnReceiveFeign;

    @Resource
    private SoDeliveryNoticeFeign soDeliveryNoticeFeign;

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SoReturnService soReturnService;

    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private SkuMappingService skuMappingService;
    @Resource
    private BomSkuFeign bomSkuFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(SoReturnDTO.Add dto, String id) {
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySoIds(Arrays.asList(dto.getSourceId()));
        //获取退货单明细表ids
        List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnDetailDTO.Add::getSourceDetailId).collect(Collectors.toList());
        //获取退货详情
        List<SoDetailEntity> soDetailEntitieList = soDetailService.listSoDetailByIds(returnDetailIds);
        if (CollectionUtils.isEmpty(soDetailEntitieList)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        List<SoReturnDetailEntity> soReturnDetailEntities = this.listDetailBySourceId(Arrays.asList(dto.getSourceId()));
        List<SoReturnDetailEntity> list = new ArrayList<>();
        for (SoReturnDetailDTO.Add detailDto : dto.getDetailList()) {
            SoReturnDetailEntity soReturnDetailEntity = new SoReturnDetailEntity();
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> soDetailEntity.getMainId().equals(detail.getSoId()) && detail.getSkuId().equals(soDetailEntity.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);

            if (actualQty < detailDto.getReturnQty() + returnQty) {
                throw new ServiceException(ApiError.ERROR_92009);
            }
            soReturnDetailEntity.setMainId(id);
            soReturnDetailEntity.setSkuId(soDetailEntity.getSkuId());
            soReturnDetailEntity.setSkuNo(soDetailEntity.getSkuNo());
            soReturnDetailEntity.setReturnQty(detailDto.getReturnQty());
            soReturnDetailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            soReturnDetailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
            soReturnDetailEntity.setRemark(detailDto.getRemark());
            soReturnDetailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            soReturnDetailEntity.setListingId(detailDto.getListingId());
            soReturnDetailEntity.setPlatformSkuNo(detailDto.getPlatformSkuNo());
            soReturnDetailEntity.setPlatformSkuName(detailDto.getPlatformSkuName());
            soReturnDetailEntity.setExchangeRate(detailDto.getExchangeRate());
            soReturnDetailEntity.setReturnAmount(detailDto.getReturnAmount());
            soReturnDetailEntity.setTaxReturnAmount(detailDto.getTaxReturnAmount());
            soReturnDetailEntity.setTaxReturnAmountLocalCurrency(detailDto.getTaxReturnAmountLocalCurrency());
            soReturnDetailEntity.setReturnAmountLocalCurrency(detailDto.getReturnAmountLocalCurrency());
            list.add(soReturnDetailEntity);
        }
        return this.saveBatch(list);
    }


    @Override
    public Boolean addByCutomer(SoReturnDTO.Add dto, String id) {
        List<SoReturnDetailEntity> list = new ArrayList<>();
        for (SoReturnDetailDTO.Add detailDto : dto.getDetailList()) {
            SoReturnDetailEntity soReturnDetailEntity = new SoReturnDetailEntity();
            soReturnDetailEntity.setMainId(id);
            soReturnDetailEntity.setSkuId(detailDto.getSkuId());
            soReturnDetailEntity.setSkuNo(detailDto.getSkuNo());
            soReturnDetailEntity.setReturnQty(detailDto.getReturnQty());
            soReturnDetailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            soReturnDetailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
            soReturnDetailEntity.setRemark(detailDto.getRemark());
            soReturnDetailEntity.setListingId(detailDto.getListingId());
            soReturnDetailEntity.setPlatformSkuNo(detailDto.getPlatformSkuNo());
            soReturnDetailEntity.setPlatformSkuName(detailDto.getPlatformSkuName());
            soReturnDetailEntity.setExchangeRate(detailDto.getExchangeRate());
            soReturnDetailEntity.setReturnAmount(detailDto.getReturnAmount());
            soReturnDetailEntity.setTaxReturnAmount(detailDto.getTaxReturnAmount());
            soReturnDetailEntity.setTaxReturnAmountLocalCurrency(detailDto.getTaxReturnAmountLocalCurrency());
            soReturnDetailEntity.setReturnAmountLocalCurrency(detailDto.getReturnAmountLocalCurrency());
            list.add(soReturnDetailEntity);
        }
        return this.saveBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(SoReturnDTO.Update dto) {
        List<String> addList = dto.getDetailList().stream().filter(c -> StringUtils.isBlank(c.getId())).map(SoReturnDetailDTO.Update::getId).collect(Collectors.toList());
        //根据销售单获取出库单
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySoIds(Arrays.asList(dto.getSourceId()));
        //获取退货单明细表ids
        List<String> returnDetailIds = dto.getDetailList().stream().map(SoReturnDetailDTO.Update::getSourceDetailId).collect(Collectors.toList());
        //获取退货详情
        List<SoDetailEntity> soDetailEntitieList = soDetailService.listSoDetailByIds(returnDetailIds);
        if (CollectionUtils.isEmpty(soDetailEntitieList)) {
            throw new ServiceException(ApiError.ERROR_92003);
        }
        //原明细数据
        List<SoReturnDetailEntity> oldList = this.listDetailByMainId(dto.getId());
        List<String> deleteIds = getDeleteIds(dto.getDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SoReturnDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_RETURN.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }

        List<SoReturnDetailEntity> soReturnDetailEntities = this.listDetailBySourceId(Arrays.asList(dto.getSourceId()));
        List<SoReturnDetailEntity> list = new ArrayList<>();
        for (SoReturnDetailDTO.Update detailDto : dto.getDetailList()) {
            SoReturnDetailEntity soReturnDetailEntity = new SoReturnDetailEntity();
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailDto.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            Integer actualQty = soOutstockDetailEntities.stream().filter(detail -> soDetailEntity.getMainId().equals(detail.getSoId()) && detail.getSkuId().equals(soDetailEntity.getSkuId()) && detail.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            if (StringUtils.isNotBlank(detailDto.getId())) {
                soReturnDetailEntity.setId(detailDto.getId());
                returnQty = soReturnDetailEntities.stream().filter(req -> req.getSourceDetailId().equals(detailDto.getSourceDetailId()) && !req.getId().equals(detailDto.getId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            }

            if (actualQty < detailDto.getReturnQty() + returnQty) {
                throw new ServiceException(ApiError.ERROR_92009);
            }

            soReturnDetailEntity.setMainId(dto.getId());
            soReturnDetailEntity.setSkuId(soDetailEntity.getSkuId());
            soReturnDetailEntity.setSkuNo(soDetailEntity.getSkuNo());
            soReturnDetailEntity.setReturnQty(detailDto.getReturnQty());
            soReturnDetailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            soReturnDetailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
            soReturnDetailEntity.setRemark(detailDto.getRemark());
            soReturnDetailEntity.setSourceDetailId(detailDto.getSourceDetailId());
            soReturnDetailEntity.setListingId(detailDto.getListingId());
            soReturnDetailEntity.setPlatformSkuNo(detailDto.getPlatformSkuNo());
            soReturnDetailEntity.setPlatformSkuName(detailDto.getPlatformSkuName());
            soReturnDetailEntity.setExchangeRate(detailDto.getExchangeRate());
            soReturnDetailEntity.setReturnAmount(detailDto.getReturnAmount());
            soReturnDetailEntity.setTaxReturnAmount(detailDto.getTaxReturnAmount());
            soReturnDetailEntity.setTaxReturnAmountLocalCurrency(detailDto.getTaxReturnAmountLocalCurrency());
            soReturnDetailEntity.setReturnAmountLocalCurrency(detailDto.getReturnAmountLocalCurrency());
            list.add(soReturnDetailEntity);
            detailDto.setSkuNo(soDetailEntity.getSkuNo());
            //修改操作日志
            if (StringUtils.isNotBlank(soReturnDetailEntity.getId())) {
                SoReturnDetailEntity old = this.getById(soReturnDetailEntity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98002);
                }
                operateLogService.addModuleOperateLogByObj(old,soReturnDetailEntity, ModuleTypeEnum.SO_RETURN.getCode(),dto.getId(),"",String.format("【%s】",old.getSkuNo()));
            }
        }
        boolean falg = this.saveOrUpdateBatch(list);
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<SoReturnDetailEntity> returnDetailEntityList = this.listByIds(addList);
            List<Pair<String, String>> addPairList = returnDetailEntityList.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), addPairList, "编辑操作");
        }
        return falg;
    }

    @Override
    public Boolean updateByCutomer(SoReturnDTO.Update dto) {
        List<String> addList = dto.getDetailList().stream().filter(c -> StringUtils.isBlank(c.getId())).map(SoReturnDetailDTO.Update::getId).collect(Collectors.toList());
        //原明细数据
        List<SoReturnDetailEntity> oldList = this.listDetailByMainId(dto.getId());
        List<String> deleteIds = getDeleteIds(dto.getDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<SoReturnDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.SO_RETURN.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        List<String> detailIds = dto.getDetailList().stream().filter(c -> StringUtils.isNotBlank(c.getId())).map(SoReturnDetailDTO.Update::getId).collect(Collectors.toList());
        List<SoReturnDetailEntity> list = new ArrayList<>();
        for (SoReturnDetailDTO.Update detailDto : dto.getDetailList()) {
            SoReturnDetailEntity soReturnDetailEntity = new SoReturnDetailEntity();
            if (StringUtils.isNotBlank(detailDto.getId())) {
                soReturnDetailEntity.setId(detailDto.getId());
            }
            soReturnDetailEntity.setId(detailDto.getId());
            soReturnDetailEntity.setMainId(dto.getId());
            soReturnDetailEntity.setSkuId(detailDto.getSkuId());
            soReturnDetailEntity.setSkuNo(detailDto.getSkuNo());
            soReturnDetailEntity.setReturnQty(detailDto.getReturnQty());
            soReturnDetailEntity.setReturnTypeDict(detailDto.getReturnTypeDict());
            soReturnDetailEntity.setReturnReasonDict(detailDto.getReturnReasonDict());
            soReturnDetailEntity.setRemark(detailDto.getRemark());
            soReturnDetailEntity.setListingId(detailDto.getListingId());
            soReturnDetailEntity.setPlatformSkuNo(detailDto.getPlatformSkuNo());
            soReturnDetailEntity.setPlatformSkuName(detailDto.getPlatformSkuName());
            soReturnDetailEntity.setExchangeRate(detailDto.getExchangeRate());
            soReturnDetailEntity.setReturnAmount(detailDto.getReturnAmount());
            soReturnDetailEntity.setTaxReturnAmount(detailDto.getTaxReturnAmount());
            soReturnDetailEntity.setTaxReturnAmountLocalCurrency(detailDto.getTaxReturnAmountLocalCurrency());
            soReturnDetailEntity.setReturnAmountLocalCurrency(detailDto.getReturnAmountLocalCurrency());
            list.add(soReturnDetailEntity);
            //修改操作日志
            if (StringUtils.isNotBlank(soReturnDetailEntity.getId())) {
                SoReturnDetailEntity old = this.getById(soReturnDetailEntity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98002);
                }
                operateLogService.addModuleOperateLogByObj(old,soReturnDetailEntity, ModuleTypeEnum.SO_RETURN.getCode(),dto.getId(),"",String.format("【%s】",old.getSkuNo()));
            }
        }
        boolean falg = this.saveOrUpdateBatch(list);
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<SoReturnDetailEntity> returnDetailEntityList = this.listByIds(addList);
            List<Pair<String, String>> addPairList = returnDetailEntityList.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.SO_DELIVERY_NOTICE.getCode(), addPairList, "编辑操作");
        }
        return falg;
    }


    private List<String> getDeleteIds(List<SoReturnDetailDTO.Update> newList, List<SoReturnDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(SoReturnDetailDTO.Update::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(SoReturnDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> mainIds) {
        return lambdaUpdate().set(SoReturnDetailEntity::getIsDeleted, Boolean.TRUE)
                .in(SoReturnDetailEntity::getMainId, mainIds)
                .remove();
    }

    @Override
    public List<SoReturnDetailEntity> listDetailByMainId(String mainId) {
        return lambdaQuery().eq(SoReturnDetailEntity::getMainId, mainId).list();
    }

    @Override
    public List<SoReturnDetailEntity> listDetailByMainIds(List<String> mainIds) {
        return lambdaQuery().in(SoReturnDetailEntity::getMainId, mainIds).list();
    }

    @Override
    public List<SoReturnDetailEntity> listDetailBySourceId(List<String> sourceIds) {
        return baseMapper.listDetailBySourceId(sourceIds);
    }

    @Override
    public List<SoReturnDetailEntity> listDetailByIds(List<String> ids) {
        return baseMapper.listDetailByIds(ids);
    }

    @Override
    public List<SoDetailDTO.AddDetailView> listAddDetailView(listAddDetailViewDTO dto) {
        List<SoDetailDTO.AddDetailView> list = baseMapper.listAddDetailView(dto);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<String> soIds = list.stream().map(SoDetailDTO.AddDetailView::getSourceId).distinct().collect(Collectors.toList());
        List<SoReturnDetailEntity> soReturnDetailEntities = this.listDetailByMainId(dto.getId());
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockFeign.listDetailBySoIds(soIds);
        List<String> skuIdList = list.stream().map(SoDetailDTO.AddDetailView::getSkuId).distinct().collect(Collectors.toList());
        //根据ids查询sku信息
        List<ProductDetailEntity> productDetailEntitys = plmTaskFeign.getByIdList(skuIdList);
        InventoryQtyDTO.FindSkuInventoryParamDTO paramDTO = new InventoryQtyDTO.FindSkuInventoryParamDTO();
        paramDTO.setSkuIds(skuIdList);
        paramDTO.setWarehouseId(list.get(MathUtil.ZERO).getWarehouseId());
        paramDTO.setInventoryStatus(InventoryStatusEnum.USABLE.getCode());
        //从wms 获取到sku 的即时库存信息
        List<InventoryQtyDTO.SkuInventoryTotalDTO> skuInventoryTotalList = inventoryFeign.listSkuInventory(paramDTO);
        //获取退货单id
        List<String> returnMainIds = list.stream().map(SoDetailDTO.AddDetailView::getMainId).distinct().collect(Collectors.toList());
        List<SoReturnReceiveDetailEntity> soReturnReceiveDetailEntities = soReturnReceiveFeign.listDetailBySourceIds(returnMainIds);
        List<String> orgIds = list.stream().map(SoDetailDTO.AddDetailView::getInventoryOrgId).collect(Collectors.toList());

        List<String> warehouseIdList = list.stream().map(SoDetailDTO.AddDetailView::getWarehouseId).collect(Collectors.toList());
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(warehouseIdList);

        //组织列表
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(orgIds);

        //sku映射表
        SoReturnEntity soReturnEntity = soReturnService.getById(dto.getId());
        List<String> skuNoList = list.stream().map(SoDetailDTO.AddDetailView::getSkuNo).distinct().collect(Collectors.toList());
        SkuMappingDTO.SkuParamDTO skuParamDTO = new SkuMappingDTO.SkuParamDTO();
        skuParamDTO.setCutomerId(soReturnEntity.getCustomerId());
        skuParamDTO.setSkuNoList(skuNoList);
        List<SkuMappingDTO.ProductSkuInfoDTO> productSkuInfoList = skuMappingService.listSkuBySkuNos(skuParamDTO);

        for (SoDetailDTO.AddDetailView addDetailView : list) {
            String warehouseName = warehouseList.stream().filter(w -> w.getId().equals(addDetailView.getWarehouseId())).
                    findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            addDetailView.setWarehouseName(warehouseName);
            BaseIdDTO.CodeDTO codeDTO = orgList.stream().filter(req -> addDetailView.getInventoryOrgId().equals(req.getId())).findFirst().orElse(new BaseIdDTO.CodeDTO());
            addDetailView.setInventoryOrgName(codeDTO.getName());
            ProductDetailEntity productDetailEntity = productDetailEntitys.stream().filter(entityClass -> entityClass.getId().equals(addDetailView.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            addDetailView.setVariantProperty(productDetailEntity.getVariantProperty());
            addDetailView.setProductName(productDetailEntity.getName());
            //获取退货数量
            Integer returnQty = soReturnDetailEntities.stream().filter(req -> req.getId().equals(addDetailView.getId())).map(SoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            //获取已出库数量
            Integer actualQty = soOutstockDetailEntities.stream().filter(req -> addDetailView.getSoId().equals(req.getSoId()) && req.getSkuId().equals(addDetailView.getSkuId()) && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).map(SoOutstockDetailEntity::getActualQty).reduce(MathUtil.ZERO, Integer::sum);
            //即时库存
            Integer curInventoryQty = skuInventoryTotalList.stream().filter(s -> s.getSkuId().equals(addDetailView.getSkuId())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getInventoryTotal())).orElse(0);
            addDetailView.setAvailableQty(getAvailableQty(curInventoryQty, addDetailView.getSalesQty()));
            addDetailView.setDeliveryQty(actualQty);
            addDetailView.setUnDeliveryQty(addDetailView.getSalesQty() - actualQty);
            addDetailView.setReturnQty(returnQty);
            Integer receiveQty = soReturnReceiveDetailEntities.stream().filter(req -> addDetailView.getId().equals(req.getSourceDetailId()) && req.getSkuId().equals(addDetailView.getSkuId()) && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus())).map(SoReturnReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            addDetailView.setReceiveQty(receiveQty);
            addDetailView.setMustQty(returnQty);
            addDetailView.setReturnTypeDictName(ReturnTypeEnum.getName(addDetailView.getReturnTypeDict()));
            addDetailView.setReturnReasonDictName(ReturnReasonEnum.getName(addDetailView.getReturnReasonDict()));
            //平台sku
            if(StringUtils.isBlank(addDetailView.getPlatformSkuNo())){
                SkuMappingDTO.ProductSkuInfoDTO productSkuInfoDTO = productSkuInfoList.stream().filter(v -> v.getSkuNo().equals(addDetailView.getSkuNo())).findFirst().orElse(new SkuMappingDTO.ProductSkuInfoDTO());
                addDetailView.setPlatformSkuNo(productSkuInfoDTO.getPlatformSkuNo());
            }
        }
        return list;
    }

    @Override
    public SoDetailDTO.ListAddDetailNoBomViewDTO listAddDetailWithNoBomView(SoReturnDTO.PlatformSkuDTO dto) {
        if(StringUtils.isNotBlank(dto.getId())){
            return generateAddDetailBySoReturn(dto);
        }else {
            return generateAddDetailByCustomerId(dto);
        }
    }

    @Override
    public List<SoReturnDTO.SoReturnAmoutDTO> getReturnAmount(SoReturnDTO.SkuParamDTO dto) {
        List<SoReturnDTO.SoReturnAmoutDTO> result = new ArrayList<>();
        for (SoReturnDTO.SkuDTO skuDTO : dto.getSkuDTOList()) {
            SoReturnDTO.SoReturnAmoutDTO view = new SoReturnDTO.SoReturnAmoutDTO();
            view.setSkuId(skuDTO.getSkuId());
            view.setCustomerId(skuDTO.getCustomerId());
            if(StringUtils.isNotBlank(skuDTO.getSoDetailId())){
                //有销售订单情况
                getReturnAmountBySo(skuDTO, view);
            }else {
                //无销售订单情况
                getReturnAmoutByCustomer(skuDTO, view);
            }
            result.add(view);
        }
        return result;
    }

    //有销售订单情况
    private void getReturnAmountBySo(SoReturnDTO.SkuDTO dto, SoReturnDTO.SoReturnAmoutDTO view) {
        SoDetailEntity soDetailEntity = soDetailService.getById(dto.getSoDetailId());
        if (null != soDetailEntity) {
            //退货金额
            BigDecimal returnAmount = soDetailEntity.getAmount()
                    .divide(BigDecimal.valueOf(soDetailEntity.getQty()), 4, RoundingMode.DOWN)
                    .multiply(BigDecimal.valueOf(dto.getReturnQty()))
                    .stripTrailingZeros();
            //含税退货金额
            BigDecimal taxReturnAmount = soDetailEntity.getTaxAmount()
                    .divide(BigDecimal.valueOf(soDetailEntity.getQty()), 4, RoundingMode.DOWN)
                    .multiply(BigDecimal.valueOf(dto.getReturnQty()))
                    .stripTrailingZeros();
            //退货金额（本位币）
            BigDecimal returnAmountLocalCurrency = returnAmount
                    .multiply(dto.getExchangeRate())
                    .setScale(4, RoundingMode.DOWN)
                    .stripTrailingZeros();
            //含税退货金额（本位币）
            BigDecimal taxReturnAmountLocalCurrency = taxReturnAmount
                    .multiply(dto.getExchangeRate())
                    .setScale(4, RoundingMode.DOWN)
                    .stripTrailingZeros();
            view.setReturnAmount(returnAmount);
            view.setTaxReturnAmount(taxReturnAmount);
            view.setReturnAmountLocalCurrency(returnAmountLocalCurrency);
            view.setTaxReturnAmountLocalCurrency(taxReturnAmountLocalCurrency);
        }
    }
    //无销售订单情况
    private void getReturnAmoutByCustomer(SoReturnDTO.SkuDTO dto, SoReturnDTO.SoReturnAmoutDTO view) {
        LocalDate returnCeateDate = LocalDate.now();
        if(StringUtils.isNotBlank(dto.getReturnId())){
            SoReturnEntity soReturnEntity = soReturnService.getById(dto.getReturnId());
            if(null != soReturnEntity){
                returnCeateDate = soReturnEntity.getCreateTime().toLocalDate();
            }
        }
        SoOutstockDTO.ListAmountParamDTO params = new SoOutstockDTO.ListAmountParamDTO();
        params.setCustomerId(dto.getCustomerId());
        params.setSkuIds(Collections.singletonList(dto.getSkuId()));
        params.setReturnCreateDate(returnCeateDate);
        List<SoOutstockDTO.AmountDTO> amountDTOS = soOutstockFeign.listAmountBySkuIds(params);

        if(CollectionUtils.isNotEmpty(amountDTOS)){
            //退货金额
            BigDecimal returnAmount = amountDTOS.get(0).getAmount()
                    .divide(BigDecimal.valueOf(amountDTOS.get(0).getQty()), 4, RoundingMode.DOWN)
                    .multiply(BigDecimal.valueOf(dto.getReturnQty()))
                    .stripTrailingZeros();
            //含税退货金额
            BigDecimal taxReturnAmount = amountDTOS.get(0).getTaxAmount()
                    .divide(BigDecimal.valueOf(amountDTOS.get(0).getQty()), 4, RoundingMode.DOWN)
                    .multiply(BigDecimal.valueOf(dto.getReturnQty()))
                    .stripTrailingZeros();
            //退货金额（本位币）
            BigDecimal returnAmountLocalCurrency = returnAmount
                    .multiply(dto.getExchangeRate())
                    .setScale(4, RoundingMode.DOWN)
                    .stripTrailingZeros();
            //含税退货金额（本位币）
            BigDecimal taxReturnAmountLocalCurrency = taxReturnAmount
                    .multiply(dto.getExchangeRate())
                    .setScale(4, RoundingMode.DOWN)
                    .stripTrailingZeros();
            view.setReturnAmount(returnAmount);
            view.setTaxReturnAmount(taxReturnAmount);
            view.setReturnAmountLocalCurrency(returnAmountLocalCurrency);
            view.setTaxReturnAmountLocalCurrency(taxReturnAmountLocalCurrency);
        }
    }


    //快粘贴 -- 根据客户id生成
    private SoDetailDTO.ListAddDetailNoBomViewDTO generateAddDetailByCustomerId (SoReturnDTO.PlatformSkuDTO dto){
        SoDetailDTO.ListAddDetailNoBomViewDTO view = new  SoDetailDTO.ListAddDetailNoBomViewDTO();
        List<SoDetailDTO.AddDetailView> bomList = new ArrayList<>();
        List<SoDetailDTO.AddDetailView> noBomList = new ArrayList<>();
        List<String> parentSkuNoList = new ArrayList<>();

        //没销售订单id，只有客户id
        if(StringUtils.isBlank(dto.getCustomerId())){
            throw new ServiceException("客户id不能为空");
        }
        if(CollectionUtils.isEmpty(dto.getPlatformSkuNoList())){
            throw new ServiceException("平台sku不能为空");
        }
        //sku映射表
        SkuMappingDTO.SkuParamDTO skuParamDTO = new SkuMappingDTO.SkuParamDTO();
        skuParamDTO.setCutomerId(dto.getCustomerId());
        skuParamDTO.setPlatformSkuNoList(dto.getPlatformSkuNoList());
        List<SkuMappingDTO.ProductSkuInfoDTO> productSkuInfoList = skuMappingService.listSkuBySkuNos(skuParamDTO);
        if(CollectionUtils.isEmpty(productSkuInfoList)){
            return new SoDetailDTO.ListAddDetailNoBomViewDTO();
        }
        for (SkuMappingDTO.ProductSkuInfoDTO productSku : productSkuInfoList) {
            SoDetailDTO.AddDetailView detailView = new SoDetailDTO.AddDetailView();
            detailView.setSkuId(productSku.getSkuId());
            detailView.setSkuNo(productSku.getSkuNo());
            detailView.setPlatformSkuNo(productSku.getPlatformSkuNo());
            detailView.setProductName(productSku.getSkuName());
            detailView.setCustomerId(productSku.getCustomerId());
            noBomList.add(detailView);
        }

        List<String> skuNOs = noBomList.stream().map(SoDetailDTO.AddDetailView::getSkuNo).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = bomSkuFeign.checkExistAndListCombinationSku(skuNOs);
        if(CollectionUtils.isNotEmpty(bomChildrenSkuList)){
            //存在套装SKU
            view.setExistBom(Boolean.TRUE);
            //根据父skuno 分组
            Map<String, List<BomChildrenSkuDTO>> collect = bomChildrenSkuList.stream().collect(Collectors.groupingBy(BomChildrenSkuDTO::getParentSkuNo));
            List<String> childSkuNoList = new ArrayList<>();
            for (SoDetailDTO.AddDetailView addDetailView : noBomList) {
                String skuNo = addDetailView.getSkuNo();
                if(collect.containsKey(skuNo)){
                    //把子件添加到结果集
                    List<BomChildrenSkuDTO> bomChildrenSkuDTOS = collect.get(skuNo);
                    for (BomChildrenSkuDTO bomChildrenSkuDTO : bomChildrenSkuDTOS) {
                        childSkuNoList.add(bomChildrenSkuDTO.getSkuNo());
                    }
                }
            }
            //sku映射表
            skuParamDTO = new SkuMappingDTO.SkuParamDTO();
            skuParamDTO.setCutomerId(dto.getCustomerId());
            skuParamDTO.setSkuNoList(childSkuNoList);
            productSkuInfoList = skuMappingService.listSkuBySkuNos(skuParamDTO);
            for (SoDetailDTO.AddDetailView addDetailView : noBomList) {
                String skuNo = addDetailView.getSkuNo();
                if(collect.containsKey(skuNo)){
                    //父sku
                    parentSkuNoList.add(skuNo);
                    //把子件添加到结果集
                    List<BomChildrenSkuDTO> bomChildrenSkuDTOS = collect.get(skuNo);
                    for (BomChildrenSkuDTO bomChildrenSkuDTO : bomChildrenSkuDTOS) {
                        SoDetailDTO.AddDetailView addChildDetailView = new SoDetailDTO.AddDetailView();
                        addChildDetailView.setSkuId(bomChildrenSkuDTO.getSkuId());
                        addChildDetailView.setSkuNo(bomChildrenSkuDTO.getSkuNo());
                        addChildDetailView.setProductName(bomChildrenSkuDTO.getSkuName());
                        String platformSkuNo = productSkuInfoList.stream().filter(v -> v.getSkuNo().equals(bomChildrenSkuDTO.getSkuNo())).map(SkuMappingDTO.ProductSkuInfoDTO::getPlatformSkuNo).findFirst().orElse("");
                        addChildDetailView.setPlatformSkuNo(platformSkuNo);
                        addChildDetailView.setCustomerId(dto.getCustomerId());
                        addChildDetailView.setIsChildSkuNo(Boolean.TRUE);
                        bomList.add(addChildDetailView);
                    }
                }else {
                    bomList.add(addDetailView);
                }
            }
        }
        view.setNobomList(noBomList);
        view.setBomList(bomList);
        view.setParentSkuNoList(parentSkuNoList);
        return view;
    }

    //快粘贴 -- 根据销售退货单id生成
    private SoDetailDTO.ListAddDetailNoBomViewDTO generateAddDetailBySoReturn (SoReturnDTO.PlatformSkuDTO dto){
        SoDetailDTO.ListAddDetailNoBomViewDTO view = new  SoDetailDTO.ListAddDetailNoBomViewDTO();
        List<SoDetailDTO.AddDetailView> bomList = new ArrayList<>();
        List<SoDetailDTO.AddDetailView> noBomList = new ArrayList<>();
        List<String> parentSkuNoList = new ArrayList<>();
        //获取sku产品明细
        listAddDetailViewDTO viewDTO = new listAddDetailViewDTO();
        viewDTO.setId(dto.getId());
        List<SoDetailDTO.AddDetailView> addDetailViews = listAddDetailView(viewDTO);
        //过滤对应的平台sku
        addDetailViews.stream().forEach(r ->{
            boolean isPresent = dto.getPlatformSkuNoList().stream().anyMatch(v -> v.equals(r.getPlatformSkuNo()));
            if(isPresent){
                noBomList.add(r);
            }
        });
        view.setNobomList(noBomList);
        if(CollectionUtils.isNotEmpty(noBomList)){
            List<String> skuNOs = noBomList.stream().map(SoDetailDTO.AddDetailView::getSkuNo).filter(StringUtils::isNotBlank).collect(Collectors.toList());
            List<BomChildrenSkuDTO> bomChildrenSkuList = bomSkuFeign.checkExistAndListCombinationSku(skuNOs);
            if(CollectionUtils.isNotEmpty(bomChildrenSkuList)){
                //存在套装SKU
                view.setExistBom(Boolean.TRUE);
                //根据父skuno 分组
                Map<String, List<BomChildrenSkuDTO>> collect = bomChildrenSkuList.stream().collect(Collectors.groupingBy(BomChildrenSkuDTO::getParentSkuNo));
                List<String> childSkuNoList = new ArrayList<>();
                for (SoDetailDTO.AddDetailView addDetailView : noBomList) {
                    String skuNo = addDetailView.getSkuNo();
                    if(collect.containsKey(skuNo)){
                        //把子件添加到结果集
                        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = collect.get(skuNo);
                        for (BomChildrenSkuDTO bomChildrenSkuDTO : bomChildrenSkuDTOS) {
                            childSkuNoList.add(bomChildrenSkuDTO.getSkuNo());
                        }
                    }
                }
                //sku映射表
                SkuMappingDTO.SkuParamDTO skuParamDTO = new SkuMappingDTO.SkuParamDTO();
                skuParamDTO.setCutomerId(dto.getCustomerId());
                skuParamDTO.setSkuNoList(childSkuNoList);
                List<SkuMappingDTO.ProductSkuInfoDTO> productSkuInfoList = skuMappingService.listSkuBySkuNos(skuParamDTO);
                for (SoDetailDTO.AddDetailView addDetailView : noBomList) {
                    String skuNo = addDetailView.getSkuNo();
                    if(collect.containsKey(skuNo)){
                        //父sku
                        parentSkuNoList.add(skuNo);
                        //把子件添加到结果集
                        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = collect.get(skuNo);
                        for (BomChildrenSkuDTO bomChildrenSkuDTO : bomChildrenSkuDTOS) {
                            SoDetailDTO.AddDetailView addChildDetailView = new SoDetailDTO.AddDetailView();
                            addChildDetailView.setSkuId(bomChildrenSkuDTO.getSkuId());
                            addChildDetailView.setSkuNo(bomChildrenSkuDTO.getSkuNo());
                            addChildDetailView.setProductName(bomChildrenSkuDTO.getSkuName());
                            String platformSkuNo = productSkuInfoList.stream().filter(v -> v.getSkuNo().equals(bomChildrenSkuDTO.getSkuNo())).map(SkuMappingDTO.ProductSkuInfoDTO::getPlatformSkuNo).findFirst().orElse("");
                            addChildDetailView.setPlatformSkuNo(platformSkuNo);
                            addChildDetailView.setReturnReasonDictName(addDetailView.getReturnReasonDictName());
                            addChildDetailView.setReturnReasonDict(addDetailView.getReturnReasonDict());
                            addChildDetailView.setReturnTypeDictName(addDetailView.getReturnTypeDictName());
                            addChildDetailView.setReturnTypeDict(addDetailView.getReturnTypeDict());
                            addChildDetailView.setIsChildSkuNo(Boolean.TRUE);
                            bomList.add(addChildDetailView);
                        }
                    }else {
                        bomList.add(addDetailView);
                    }
                }
            }
        }
        view.setBomList(bomList);
        view.setParentSkuNoList(parentSkuNoList);
        return view;
    }


    /**
     * 获取可用数量
     *
     * @param curInventoryQty 即时库存数量
     * @param salesQty        销售数量
     * @return java.lang.Integer
     * @Author Luo_WG
     * @Date 2023/5/17 11:06
     **/
    private Integer getAvailableQty(Integer curInventoryQty, Integer salesQty) {
        /**
         * 可出数量
         * 根据可用即时库存计算可出数量，
         * 当可用即时库存数量大于销售数量时 可出数量=销售数量；
         * 若可用即时库存数量小于销售数量，可出数量=即时可用库存数量
         */
        Integer availableQty = 0;
        Boolean isGre = curInventoryQty > salesQty;
        if (isGre) {
            availableQty = salesQty;
        } else {
            availableQty = curInventoryQty;
        }
        return availableQty;
    }

    @Override
    public List<String> listBySkuNo(SoReturnDTO.PdaSoReturnParam dto) {
        LambdaQueryWrapper<SoReturnDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(SoReturnDetailEntity::getMainId);
        queryWrapper.eq(StringUtils.isNotBlank(dto.getSkuNo()), SoReturnDetailEntity::getSkuNo, dto.getSkuNo());
        queryWrapper.eq(SoReturnDetailEntity::getIsDeleted, Boolean.FALSE);
        queryWrapper.groupBy(SoReturnDetailEntity::getMainId);
        return listObjs(queryWrapper, Object::toString);
    }

    @Override
    public List<SoReturnDetailEntity> listDetailByReturnType(List<String> returnType) {
        return lambdaQuery().in(SoReturnDetailEntity::getReturnTypeDict, returnType).list();
    }
}
