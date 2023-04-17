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
import com.erp.model.wms.dto.PurchaseStockInDetailDTO;
import com.erp.model.wms.entity.PurchaseStockInDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.enums.SourceTypeEnum;
import com.erp.rpc.wms.feign.ProductOrderFeign;
import com.erp.server.wms.mapper.PurchaseStorageDetailMapper;
import com.erp.server.wms.service.ModuleOperateLogService;
import com.erp.server.wms.service.PurchaseStockInDetailService;
import com.erp.server.wms.service.WarehouseReceiveDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
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
public class PurchaseStockInDetailServiceImpl extends SuperServiceImpl<PurchaseStorageDetailMapper, PurchaseStockInDetailEntity> implements PurchaseStockInDetailService {

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private ProductOrderFeign productOrderFeign;

    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(List<PurchaseStockInDetailDTO.AddDTO> details, String mainId,String sourceType) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }

        List<PurchaseStockInDetailEntity> list = BeanMapperUtils.copyList(PurchaseStockInDetailEntity.class, details);

        //验证关联数量
        checkStockInQty(list,sourceType);

        //处理明细数据
        doOpHandleDetails(list,mainId);
        this.saveBatch(list);
    }

    private void checkStockInQty (List<PurchaseStockInDetailEntity> list ,String sourceType) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<String> ids = list.stream().map(PurchaseStockInDetailEntity::getSourceDetailId).collect(Collectors.toList());

        //下推单据明细id查询
        List<PurchaseStockInDetailEntity> stockInDetails = this.listDetailBySourceDetailIds(ids);
        //来源采购订单
        if (SourceTypeEnum.PURCHASE_ORDER.getType().equals(sourceType)) {
            List<PurchaseOrderDetailEntity> details = productOrderFeign.listPurchaseOrderDetailById(ids);
            if (CollectionUtils.isEmpty(details)) {
                throw new ServiceException(ApiError.ERROR_98026);
            }
           //下推收货单
            List<WarehouseReceiveDetailEntity> receiveDetails = warehouseReceiveDetailService.listDetailByPodIds(ids);

            for (PurchaseStockInDetailEntity detailEntity : list) {
                //采购订单数量
              Integer purchaseQty = details.stream().filter(obj -> obj.getId().equals(detailEntity.getPurchaseOrderDetailId())).map(obj -> obj.getPurchaseQty()).findFirst().orElse(MathUtil.ZERO);

                //本次入库数量
                Integer thisStockInQty = detailEntity.getStockInQty();

                //下推收货单数量
                Integer receiveQty = MathUtil.ZERO;
                if (CollectionUtils.isNotEmpty(receiveDetails)) {
                     receiveQty = receiveDetails.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(detailEntity.getId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                }
                //下推入库单数量
                Integer stockInQty = MathUtil.ZERO;
                if (CollectionUtils.isNotEmpty(stockInDetails)) {
                    stockInQty = stockInDetails.stream().filter(obj -> obj.getSourceDetailId().equals(detailEntity.getId()) && !obj.getId().equals(detailEntity.getId())).map(PurchaseStockInDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                }

                if (thisStockInQty > purchaseQty - receiveQty - stockInQty) {
                    throw new ServiceException(new ApiResult(MathUtil.ONE,String.format("%s入库数量不能大于",detailEntity.getSkuNo()) + (receiveQty - stockInQty)));
                }
            }
        }
        //来源收货单
        if (SourceTypeEnum.WAREHOUSE_RECEIVE.getType().equals(sourceType)) {
            //查收货单明细
            List<WarehouseReceiveDetailEntity> details = warehouseReceiveDetailService.listByIds(ids);
            if (CollectionUtils.isEmpty(details)) {
                throw new ServiceException(ApiError.ERROR_98026);
            }
            for (PurchaseStockInDetailEntity detailEntity : list) {
                //收货单下推入库单数量
                Integer stockInQty = MathUtil.ZERO;
                if (CollectionUtils.isNotEmpty(stockInDetails)) {
                    stockInQty = stockInDetails.stream().filter(obj -> obj.getSourceDetailId().equals(detailEntity.getId()) && !obj.getId().equals(detailEntity.getId())).map(PurchaseStockInDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                }

            }


        }


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<PurchaseStockInDetailDTO.UpdateDTO> details, String mainId,String sourceType) {
        if (details == null) {
            details = new ArrayList<>();
        }
        //原明细数据
        List<PurchaseStockInDetailEntity> oldList = this.listByMainId(mainId);
        List<String> deleteIds = getDeleteIds(details, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<PurchaseStockInDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(),pairList,"编辑操作");
            this.removeByIds(deleteIds);
        }
        List<PurchaseStockInDetailEntity> newList = BeanMapperUtils.copyList(PurchaseStockInDetailEntity.class, details);

        //处理明细id及操作日志
        doOpHandleDetails(newList,mainId);

        //新增或修改明细
        this.saveOrUpdateBatch(newList);
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<PurchaseStockInDetailDTO.UpdateDTO> newList, List<PurchaseStockInDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(PurchaseStockInDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(PurchaseStockInDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    @Override
    public void removeByMainIds(List<String> mainIds) {
        lambdaUpdate().in(PurchaseStockInDetailEntity::getMainId,mainIds).remove();
    }

    @Override
    public List<PurchaseStockInDetailEntity> listByMainId(String mainId) {
        return lambdaQuery().eq(PurchaseStockInDetailEntity::getMainId,mainId).list();
    }

    @Override
    public List<PurchaseStockInDetailEntity> listDetailBySourceDetailIds(List<String> sourceDetailIds) {
        return lambdaQuery().in(PurchaseStockInDetailEntity::getSourceDetailId,sourceDetailIds).list();
    }

    /**
     * 根据采购订单详情表Id查询已入库数量
     * @Author Luo_WG
     * @Date 2023/4/17 15:58
     * @param purchaseOrderDetailId purchaseOrderDetailId
     * @return java.util.List<com.erp.model.wms.entity.PurchaseStockInDetailEntity>
     **/
    @Override
    public Integer getStockInQty(String purchaseOrderDetailId) {
        return baseMapper.getStockInQty(purchaseOrderDetailId);
    }

    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDetails (List<PurchaseStockInDetailEntity> newList, String mainId) {

        List<PurchaseStockInDetailEntity> addList = newList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //添加操作日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(mainId, obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("添加了一个SKU【%s】", ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(), addPairList, "编辑操作");
        }
        for (PurchaseStockInDetailEntity entity : newList) {
            //修改操作日志
            if (StringUtils.isNotBlank(entity.getId())) {
                PurchaseStockInDetailEntity old = this.getById(entity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98002);
                }
                moduleOperateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.PURCHASE_STOCK_IN.getCode(),mainId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
    }
}
