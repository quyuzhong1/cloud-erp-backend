package com.erp.server.wms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.PurchaseReturnOrderDetailDTO;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.PurchaseReturnOrderDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.mapper.PurchaseReturnOrderDetailMapper;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.PoInstockDetailService;
import com.erp.server.wms.service.PurchaseReturnOrderDetailService;
import com.erp.server.wms.service.WarehouseReceiveDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 采购退货单明细 服务实现类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-07
 */
@Service
public class PurchaseReturnOrderDetailServiceImpl extends SuperServiceImpl<PurchaseReturnOrderDetailMapper, PurchaseReturnOrderDetailEntity> implements PurchaseReturnOrderDetailService {


    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private PoInstockDetailService poInstockDetailService;

    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Resource
    private OperateLogService operateLogService;

    /**
     * @param sourceDetailIds
     * @return List<PurchaseReturnOrderDetailEntity>
     * @description: 根据来源明细ids查询退货明细
     * @author Will
     * @date: 2023/4/14 11:54
     */
    @Override
    public List<PurchaseReturnOrderDetailEntity> listBySourceDetailIds(List<String> sourceDetailIds) {
        return baseMapper.listBySourceDetailIds(sourceDetailIds);
    }

    /**
     * 新增
     *
     * @param dto dto
     * @param id  id:主表id
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/13 14:43
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(PurchaseReturnOrderDTO.AddDTO dto, String id) {
        Integer returnQty = 0;
        //创建保存详情的集合
        List<PurchaseReturnOrderDetailEntity> listDetail = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getPurchaseOrderId())) {
            //获取界面传过来的采购单详情表id集合
            List<String> orderDetailIds = dto.getPurchasePriceDetailList().stream().map(PurchaseReturnOrderDetailDTO.AddDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
            //根据ids查询采购单详情
            List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);
            // 关闭SKU重复检查
            // checkAddDetailsRepeatSku(purchaseOrderDetailEntities);
            //遍历需要保存的采购收货单详情信息，并赋值采购单信息
            List<PurchaseReturnOrderDetailDTO.AddDTO> detailList = dto.getPurchasePriceDetailList();
            List<PoInstockDetailEntity> stockInDetailEntityList = poInstockDetailService.listDetailByPodIds(orderDetailIds);
            List<WarehouseReceiveDetailEntity> detailEntityList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(orderDetailIds);
            List<PurchaseReturnOrderDetailEntity> purchaseReturnOrderDetailEntities = listReturnOrderDetailByPodIds(orderDetailIds);
            for (PurchaseReturnOrderDetailDTO.AddDTO addDTO : detailList) {
                PurchaseReturnOrderDetailEntity purchaseReturnOrderDetailEntity = new PurchaseReturnOrderDetailEntity();
                purchaseReturnOrderDetailEntity.setMainId(id);
                PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(detail -> detail.getId().equals(addDTO.getPurchaseOrderDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(purchaseOrderDetailEntity)) {
                    purchaseReturnOrderDetailEntity.setSkuId(purchaseOrderDetailEntity.getSkuId());
                    purchaseReturnOrderDetailEntity.setSkuNo(purchaseOrderDetailEntity.getSkuNo());
                    returnQty = purchaseReturnOrderDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(addDTO.getPurchaseOrderDetailId())).map(PurchaseReturnOrderDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                    if (dto.getSourceType().equals(SourceTypeEnum.QC_BILL.getCode())) {
                        Integer receiveQty = detailEntityList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(addDTO.getPurchaseOrderDetailId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                        returnQty = purchaseReturnOrderDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(addDTO.getPurchaseOrderDetailId())).map(PurchaseReturnOrderDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                        if (addDTO.getReturnQty() + returnQty > receiveQty) {
                            throw new ServiceException(ApiError.ERROR_99030.code, String.format(ApiError.ERROR_99030.msg, purchaseOrderDetailEntity.getSkuNo()));
                        }
                    } else {
                        Integer stockInQty = stockInDetailEntityList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(addDTO.getPurchaseOrderDetailId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                        if (addDTO.getReturnQty() > stockInQty) {
                            throw new ServiceException(ApiError.ERROR_99026.code, String.format(ApiError.ERROR_99026.msg, purchaseOrderDetailEntity.getSkuNo()));
                        }

                        if (addDTO.getReturnQty() + returnQty > stockInQty) {
                            throw new ServiceException(ApiError.ERROR_99031.code, String.format(ApiError.ERROR_99031.msg, purchaseOrderDetailEntity.getSkuNo()));
                        }
                    }
                    purchaseReturnOrderDetailEntity.setReturnQty(addDTO.getReturnQty());
                    purchaseReturnOrderDetailEntity.setReplenishQty(addDTO.getReplenishQty());
                    purchaseReturnOrderDetailEntity.setDeductAmountQty(addDTO.getDeductAmountQty());
                    purchaseReturnOrderDetailEntity.setReturnPrice(addDTO.getReturnPrice());
                    purchaseReturnOrderDetailEntity.setRemark(addDTO.getRemark());
                    purchaseReturnOrderDetailEntity.setPurchaseOrderDetailId(addDTO.getPurchaseOrderDetailId());
                    purchaseReturnOrderDetailEntity.setCurrency(addDTO.getCurrency());
                    purchaseReturnOrderDetailEntity.setSourceDetailId(addDTO.getSourceDetailId());
                    purchaseReturnOrderDetailEntity.setWarehouseLocation(addDTO.getWarehouseLocation());
                    purchaseReturnOrderDetailEntity.setWarehouseLocation(addDTO.getWarehouseLocation());
                } else {
                    throw new ServiceException(ApiError.ERROR_99006);
                }
                listDetail.add(purchaseReturnOrderDetailEntity);
            }
        } else {
            notProductOrderAdd(dto, id, listDetail);
        }

        //保存详情信息
        return this.saveBatch(listDetail);
    }

    /**
     * 无采购单新增
     *
     * @param dto id
     * @return void
     * @Author Luo_WG
     * @Date 2023/4/25 14:39
     **/
    private List<PurchaseReturnOrderDetailEntity> notProductOrderAdd(PurchaseReturnOrderDTO.AddDTO dto, String id, List<PurchaseReturnOrderDetailEntity> listDetail) {
        //遍历需要保存的采购收货单详情信息，并赋值采购单信息
        List<PurchaseReturnOrderDetailDTO.AddDTO> detailList = dto.getPurchasePriceDetailList();
        // 关闭SKU重复检查
        // checkAddDetailsRepeat(detailList);

        List<String> skuIdList = dto.getPurchasePriceDetailList().stream().map(PurchaseReturnOrderDetailDTO.AddDTO::getSkuId).collect(Collectors.toList());
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);

        for (PurchaseReturnOrderDetailDTO.AddDTO addDTO : detailList) {
            PurchaseReturnOrderDetailEntity purchaseReturnOrderDetailEntity = new PurchaseReturnOrderDetailEntity();
            BeanMapperUtils.copy(addDTO, purchaseReturnOrderDetailEntity);
            purchaseReturnOrderDetailEntity.setMainId(id);
            purchaseReturnOrderDetailEntity.setReturnQty(addDTO.getReturnQty());
            //获取sku信息
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(entityClass -> entityClass.getId().equals(addDTO.getSkuId())).findFirst().orElse(new ProductDetailEntity());
            purchaseReturnOrderDetailEntity.setSkuNo(productDetailEntity.getSkuNo());
            listDetail.add(purchaseReturnOrderDetailEntity);
        }
        return listDetail;
    }


    /**
     * 新增验证sku是否重复
     */
    private void checkAddDetailsRepeat(List<PurchaseReturnOrderDetailDTO.AddDTO> list) {
        Map<String, List<PurchaseReturnOrderDetailDTO.AddDTO>> map = list.stream().collect(Collectors.groupingBy(PurchaseReturnOrderDetailDTO.AddDTO::getSkuId));
        for (Map.Entry<String, List<PurchaseReturnOrderDetailDTO.AddDTO>> entry : map.entrySet()) {
            List<PurchaseReturnOrderDetailDTO.AddDTO> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1, "sku编码【".concat(value.get(0).getSkuNo()).concat("】不能重复")));
            }
        }
    }

    /**
     * 新增验证sku是否重复
     */
    private void checkAddDetailsRepeatSku(List<PurchaseOrderDetailEntity> list) {
        Map<String, List<PurchaseOrderDetailEntity>> map = list.stream().collect(Collectors.groupingBy(PurchaseOrderDetailEntity::getSkuId));
        for (Map.Entry<String, List<PurchaseOrderDetailEntity>> entry : map.entrySet()) {
            List<PurchaseOrderDetailEntity> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1, "sku编码【".concat(value.get(0).getSkuNo()).concat("】不能重复")));
            }
        }
    }

    /**
     * 修改
     *
     * @param dto dto
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/13 15:22
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(PurchaseReturnOrderDTO.UpdateDTO dto, String id) {
        List<String> addList = dto.getPurchasePriceDetailList().stream().filter(c -> StringUtils.isBlank(c.getId())).map(PurchaseReturnOrderDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        //原明细数据
        List<PurchaseReturnOrderDetailEntity> oldList = this.getDetailByMainId(dto.getId());
        List<String> deleteIds = getDeleteIds(dto.getPurchasePriceDetailList(), oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<PurchaseReturnOrderDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), pairList, "编辑操作");
            this.removeByIds(deleteIds);
        }
        Integer returnQty = 0;
        //创建保存详情的集合
        List<PurchaseReturnOrderDetailEntity> listDetail = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getPurchaseOrderId())) {
            //获取界面传过来的采购单详情表id集合
            List<String> orderDetailIds = dto.getPurchasePriceDetailList().stream().map(PurchaseReturnOrderDetailDTO.UpdateDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
            //根据ids查询采购单详情
            List<PurchaseOrderDetailEntity> purchaseOrderDetailEntities = scmTaskFeign.listPurchaseOrderDetailById(orderDetailIds);
            //遍历需要保存的采购收货单详情信息，并赋值采购单信息
            List<PurchaseReturnOrderDetailDTO.UpdateDTO> detailList = dto.getPurchasePriceDetailList();
            List<PoInstockDetailEntity> stockInDetailEntityList = poInstockDetailService.listDetailByPodIds(orderDetailIds);
            List<WarehouseReceiveDetailEntity> detailEntityList = warehouseReceiveDetailService.listWarehouseReceiveByPodIds(orderDetailIds);
            List<PurchaseReturnOrderDetailEntity> purchaseReturnOrderDetailEntities = listReturnOrderDetailByPodIds(orderDetailIds);
            for (PurchaseReturnOrderDetailDTO.UpdateDTO updateDTO : detailList) {
                PurchaseReturnOrderDetailEntity purchaseReturnOrderDetailEntity = new PurchaseReturnOrderDetailEntity();
                BeanMapperUtils.copy(updateDTO, purchaseReturnOrderDetailEntity);
                purchaseReturnOrderDetailEntity.setMainId(id);
                PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntities.stream().filter(detail -> detail.getId().equals(updateDTO.getPurchaseOrderDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(purchaseOrderDetailEntity)) {
                    purchaseReturnOrderDetailEntity.setSkuId(purchaseOrderDetailEntity.getSkuId());
                    purchaseReturnOrderDetailEntity.setSkuNo(purchaseOrderDetailEntity.getSkuNo());

                    if (dto.getSourceType().equals(SourceTypeEnum.WAREHOUSE_RECEIVE.getCode())) {
                        Integer receiveQty = detailEntityList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(updateDTO.getPurchaseOrderDetailId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                        if (updateDTO.getReturnQty() > receiveQty) {
                            throw new ServiceException(ApiError.ERROR_99030.code, String.format(ApiError.ERROR_99030.msg, purchaseOrderDetailEntity.getSkuNo()));
                        }
                    } else {
                        Integer stockInQty = stockInDetailEntityList.stream().filter(req -> req.getPurchaseOrderDetailId().equals(updateDTO.getPurchaseOrderDetailId()) && req.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                        if (updateDTO.getReturnQty() > stockInQty) {
                            throw new ServiceException(ApiError.ERROR_99026.code, String.format(ApiError.ERROR_99026.msg, purchaseOrderDetailEntity.getSkuNo()));
                        }

                        returnQty = purchaseReturnOrderDetailEntities.stream().filter(req -> req.getPurchaseOrderDetailId().equals(updateDTO.getPurchaseOrderDetailId()) && !req.getId().equals(updateDTO.getId())).map(PurchaseReturnOrderDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                        if (updateDTO.getReturnQty() + returnQty > stockInQty) {
                            throw new ServiceException(ApiError.ERROR_99031.code, String.format(ApiError.ERROR_99031.msg, purchaseOrderDetailEntity.getSkuNo()));
                        }
                    }
                } else {
                    throw new ServiceException(ApiError.ERROR_99006);
                }
                //修改操作日志
                if (StringUtils.isNotBlank(purchaseReturnOrderDetailEntity.getId())) {
                    PurchaseReturnOrderDetailEntity old = this.getById(purchaseReturnOrderDetailEntity.getId());
                    operateLogService.addModuleOperateLogByObj(old, purchaseReturnOrderDetailEntity, ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), dto.getId(), "", String.format("【%s】", old.getSkuNo()));
                }
                listDetail.add(purchaseReturnOrderDetailEntity);
            }
        } else {
            notProductOrderUpdate(dto, id, listDetail);
        }
        boolean flag = this.saveOrUpdateBatch(listDetail);
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<PurchaseReturnOrderDetailEntity> returnOrderDetailEntities = this.listByIds(addList);
            List<Pair<String, String>> addPairList = returnOrderDetailEntities.stream().map(obj -> new Pair<>(dto.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), addPairList, "编辑操作");
        }
        return flag;
    }

    private List<String> getDeleteIds(List<PurchaseReturnOrderDetailDTO.UpdateDTO> newList, List<PurchaseReturnOrderDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(PurchaseReturnOrderDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(PurchaseReturnOrderDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 无采购单新增
     *
     * @param dto id
     * @return void
     * @Author Luo_WG
     * @Date 2023/4/25 14:39
     **/
    private List<PurchaseReturnOrderDetailEntity> notProductOrderUpdate(PurchaseReturnOrderDTO.UpdateDTO dto, String id, List<PurchaseReturnOrderDetailEntity> listDetail) {
        //遍历需要保存的采购收货单详情信息，并赋值采购单信息
        List<PurchaseReturnOrderDetailDTO.UpdateDTO> detailList = dto.getPurchasePriceDetailList();
        for (PurchaseReturnOrderDetailDTO.UpdateDTO updateDTO : detailList) {
            PurchaseReturnOrderDetailEntity purchaseReturnOrderDetailEntity = new PurchaseReturnOrderDetailEntity();
            BeanMapperUtils.copy(updateDTO, purchaseReturnOrderDetailEntity);
            purchaseReturnOrderDetailEntity.setMainId(id);
            purchaseReturnOrderDetailEntity.setReturnQty(updateDTO.getReturnQty());
            listDetail.add(purchaseReturnOrderDetailEntity);
            //修改操作日志
            if (StringUtils.isNotBlank(purchaseReturnOrderDetailEntity.getId())) {
                PurchaseReturnOrderDetailEntity old = this.getById(purchaseReturnOrderDetailEntity.getId());
                operateLogService.addModuleOperateLogByObj(old, purchaseReturnOrderDetailEntity, ModuleTypeEnum.PURCHASE_RETURN_ORDER.getCode(), dto.getId(), "", String.format("【%s】", old.getSkuNo()));
            }
        }
        return listDetail;
    }

    /**
     * 根据主表id删除
     *
     * @param mainIds mainIds
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     **/
    @Override
    public Boolean delete(List<String> mainIds) {
        return lambdaUpdate().set(PurchaseReturnOrderDetailEntity::getIsDeleted, Boolean.TRUE)
                .in(PurchaseReturnOrderDetailEntity::getMainId, mainIds)
                .remove();
    }

    /**
     * 根据主表id查询详情表信息
     *
     * @param mainId mainId
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/4/13 17:44
     **/
    @Override
    public List<PurchaseReturnOrderDetailEntity> getDetailByMainId(String mainId) {
        LambdaQueryWrapper<PurchaseReturnOrderDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PurchaseReturnOrderDetailEntity::getMainId, mainId);
        return this.list(queryWrapper);
    }

    @Override
    public List<PurchaseReturnOrderDetailEntity> listReturnOrderDetailByPodIds(List<String> podIds) {
        return baseMapper.listReturnOrderDetailByPodIds(podIds);
    }

    @Override
    public List<PurchaseReturnOrderDetailEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(PurchaseReturnOrderDetailEntity::getMainId, mainIds).list();
    }
}
