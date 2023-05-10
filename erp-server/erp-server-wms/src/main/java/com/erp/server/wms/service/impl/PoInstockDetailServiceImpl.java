package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.PoInstockDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.QcBillStatusEnum;
import com.erp.model.wms.enums.SourceTypeEnum;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.mapper.PoInstockDetailMapper;
import com.erp.server.wms.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 采购入库明细表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-04-10
 */
@Service
public class PoInstockDetailServiceImpl extends SuperServiceImpl<PoInstockDetailMapper, PoInstockDetailEntity> implements PoInstockDetailService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Resource
    private QcInfoService qcInfoService;

    @Resource
    private PoInstockService poInstockService;

    @Resource
    private PurchaseReturnOrderDetailService purchaseReturnOrderDetailService;



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(List<PoInstockDetailDTO.AddDTO> details, String mainId, String sourceType) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }

        List<PoInstockDetailEntity> list = BeanMapperUtils.copyList(PoInstockDetailEntity.class, details);

        //处理明细数据
        doOpHandleDetails(list,mainId,Boolean.FALSE);

        //验证关联数量
        checkStockInQty(list,sourceType,mainId);


        this.saveBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<PoInstockDetailDTO.UpdateDTO> details, String mainId, String sourceType) {
        if (details == null) {
            details = new ArrayList<>();
        }
        //原明细数据
        List<PoInstockDetailEntity> oldList = this.listByMainId(mainId);
        List<String> deleteIds = getDeleteIds(details, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<PoInstockDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PO_INSTOCK.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        List<PoInstockDetailEntity> newList = BeanMapperUtils.copyList(PoInstockDetailEntity.class, details);

        //处理明细id及操作日志
        doOpHandleDetails(newList,mainId,Boolean.TRUE);

        //验证关联数量
        checkStockInQty(newList,sourceType,mainId);

        //新增或修改明细
        this.saveOrUpdateBatch(newList);
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<PoInstockDetailDTO.UpdateDTO> newList, List<PoInstockDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(PoInstockDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(PoInstockDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {
        lambdaUpdate().in(PoInstockDetailEntity::getMainId,mainIds).remove();
    }

    @Override
    public List<PoInstockDetailEntity> listByMainId(String mainId) {
        return lambdaQuery().eq(PoInstockDetailEntity::getMainId,mainId).list();
    }

    @Override
    public List<PoInstockDetailEntity> listByMainIds(List<String> mainIds) {
        return lambdaQuery().in(PoInstockDetailEntity::getMainId,mainIds).list();
    }

    @Override
    public List<PoInstockDetailEntity> listDetailBySourceDetailIds(List<String> sourceDetailIds) {
        return baseMapper.listDetailBySourceDetailIds(sourceDetailIds);
    }


    @Override
    public  List<PoInstockDetailEntity> listDetailByPodIds(List<String> podIds) {
        return baseMapper.listDetailByPodIds(podIds);
    }

    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDetails (List<PoInstockDetailEntity> newList, String mainId, Boolean isUpdate) {

        List<PoInstockDetailEntity> addList = newList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //采购明细信息
        List<String> podIds = newList.stream().map(PoInstockDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = scmTaskFeign.listPurchaseOrderDetailById(podIds);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        for (PoInstockDetailEntity entity : newList) {
            PurchaseOrderDetailEntity detailEntity = purchaseOrderDetailList.stream().filter(obj -> obj.getId().equals(entity.getPurchaseOrderDetailId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(detailEntity)) {
                throw new ServiceException(ApiError.ERROR_98026);
            }
            entity.setSkuId(detailEntity.getSkuId());
            entity.setSkuNo(detailEntity.getSkuNo());
            entity.setPurchaseQty(detailEntity.getPurchaseQty());
            entity.setVariantProperty(detailEntity.getVariantProperty());
            entity.setMainId(mainId);
            //修改操作日志
            if (StringUtils.isNotBlank(entity.getId())) {
                PoInstockDetailEntity old = this.getById(entity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98002);
                }
                operateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.PO_INSTOCK.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList) && isUpdate) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.PO_INSTOCK.getCode(), addPairList, "编辑操作");
        }
    }

    /**
     * @description: 验证数量
     * @author Will
     * @date: 2023/4/17 16:04
     * @param list
     * @param sourceType
     */
    private void checkStockInQty (List<PoInstockDetailEntity> list , String sourceType, String mainId) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //来源ids
        List<String> ids = list.stream().map(PoInstockDetailEntity::getSourceDetailId).collect(Collectors.toList());
        //采购订单明细ids
        List<String> podIds = list.stream().map(PoInstockDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());


        PoInstockEntity entity = poInstockService.getById(mainId);
        if (ObjectUtils.isEmpty(entity)) {
            throw new ServiceException(ApiError.ERROR_98050);
        }
        //验证SKU是否重复
        Map<String, List<PoInstockDetailEntity>> map = list.stream().collect(Collectors.groupingBy(PoInstockDetailEntity::getSkuId));
        for (Map.Entry<String, List<PoInstockDetailEntity>> entry: map.entrySet()) {
            List<PoInstockDetailEntity> value = entry.getValue();
            if (value.size() > MathUtil.ONE) {
                throw new ServiceException(new ApiResult(1,"sku编码【".concat(value.get(0).getSkuNo()).concat("】不能重复")));
            }
        }

        //判断是否存在质检单、存在且未质检完成则不支持入库
        List<QcInfoEntity> qcList =  qcInfoService.listByPoIds(Arrays.asList(entity.getId()));
        if (CollectionUtils.isNotEmpty(qcList)) {
           Long count = qcList.stream().filter(obj -> QcBillStatusEnum.DRAFT.getCode().equals(obj.getQcStatus()) || QcBillStatusEnum.WAIT_QC.getCode().equals(obj.getQcStatus())).count();
            if (count > 0) {
                throw new ServiceException(ApiError.ERROR_99010);
            }
        }

        //采购订单
        List<PurchaseOrderDetailEntity> details = scmTaskFeign.listPurchaseOrderDetailById(podIds);
        if (CollectionUtils.isEmpty(details)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }

        //下推单据明细id查询
        List<PoInstockDetailEntity> stockInDetails = this.listDetailByPodIds(ids);

        //查收货单明细
        List<WarehouseReceiveDetailEntity> receiveDetails = warehouseReceiveDetailService.listByIds(ids);
        if (CollectionUtils.isEmpty(details)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        //查询退货明细
        List<PurchaseReturnOrderDetailEntity> returnOrderDetailList = purchaseReturnOrderDetailService.listReturnOrderDetailByPodIds(ids);


        for (PoInstockDetailEntity detailEntity : list) {

            //采购订单数量
            Integer purchaseQty = details.stream().filter(obj -> obj.getId().equals(detailEntity.getPurchaseOrderDetailId())).map(obj -> obj.getPurchaseQty()).findFirst().orElse(MathUtil.ZERO);

            //本次入库数量
            Integer thisStockInQty = detailEntity.getStockInQty();

            //下推入库单数量
            Integer stockInQty = MathUtil.ZERO;

            if (CollectionUtils.isNotEmpty(stockInDetails)) {
                stockInQty = stockInDetails.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(detailEntity.getPurchaseOrderDetailId()) && !obj.getId().equals(detailEntity.getId())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
            }

            //退货单数量
            Integer returnQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(returnOrderDetailList)) {
                returnQty = returnOrderDetailList.stream().filter(obj -> obj.getSourceDetailId().equals(detailEntity.getId())).map(PurchaseReturnOrderDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            }


            if (thisStockInQty > purchaseQty - stockInQty + returnQty) {
                throw new ServiceException(new ApiResult(MathUtil.ONE,String.format("SKU【%s】入库数量不能大于",detailEntity.getSkuNo()) + (purchaseQty - stockInQty + returnQty)));
            }
            //来源收货单
            if (SourceTypeEnum.WAREHOUSE_RECEIVE.getCode().equals(sourceType)) {
                //收货数量
                Integer receiveQty = receiveDetails.stream().filter(obj -> obj.getId().equals(detailEntity.getSourceDetailId())).map(WarehouseReceiveDetailEntity::getReceiveQty).findFirst().orElse(MathUtil.ZERO);

                //数量验证
                if (thisStockInQty > receiveQty) {
                    throw new ServiceException(new ApiResult(1,String.format("SKU【%s】入库数量不能大于",detailEntity.getSkuNo()) + receiveQty));
                }
            }
        }
    }
}
