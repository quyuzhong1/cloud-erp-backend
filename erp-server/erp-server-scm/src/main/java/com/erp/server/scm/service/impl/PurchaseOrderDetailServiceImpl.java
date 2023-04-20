package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.dto.PurchaseApplicationRefPoDTO;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.*;
import com.erp.model.scm.enums.CreatePoTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.PurchaseReturnOrderDetailEntity;
import com.erp.model.wms.entity.PurchaseStockInDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.PurchaseOrderDetailMapper;
import com.erp.server.scm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
@Service
public class PurchaseOrderDetailServiceImpl extends SuperServiceImpl<PurchaseOrderDetailMapper, PurchaseOrderDetailEntity> implements PurchaseOrderDetailService {

    @Resource
    private PurchaseOrderService purchaseOrderService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private PurchaseApplicationRefPoService purchaseApplicationRefPoService;

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchaseApplicationDetailService purchaseApplicationDetailService;

    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(List<PurchaseOrderDetailDTO.AddDTO> details, String purchaseOrderId) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        //验证报价信息
        checkPurchasePrice(details,purchaseOrderId);

        List<PurchaseOrderDetailEntity> list = BeanMapperUtils.copyList(PurchaseOrderDetailEntity.class, details);
        //处理明细中的数据id
        doOpHandleDetails(list,purchaseOrderId);
        //批量新增
        boolean flag = this.saveBatch(list);
        if (flag) {
            //新增关联关系
            List<PurchaseApplicationRefPoEntity> refList = new ArrayList<>();

            for (PurchaseOrderDetailEntity entity : list) {
                if (StringUtils.isBlank(entity.getPurchaseApplicationDetailId())) {
                    continue;
                }
                PurchaseApplicationRefPoEntity refPoEntity = new PurchaseApplicationRefPoEntity();
                refPoEntity.setPurchaseOrderId(purchaseOrderId);
                refPoEntity.setPurchaseOrderDetailId(entity.getId());
                refPoEntity.setPurchaseApplicationId(entity.getPurchaseApplicationId());
                refPoEntity.setPurchaseApplicationDetailId(entity.getPurchaseApplicationDetailId());
                refList.add(refPoEntity);
            }
            if (CollectionUtils.isNotEmpty(refList)) {
                //新增关联关系
                purchaseApplicationRefPoService.saveBatch(refList);

                PurchaseOrderEntity purchaseOrderEntity = purchaseOrderService.getById(purchaseOrderId);
                if (ObjectUtils.isEmpty(purchaseOrderEntity)) {
                    throw new ServiceException(ApiError.ERROR_98025);
                }
                //采购申请单生成日志
                List<Pair<String, String>> pairList = refList.stream().map(obj -> new Pair<>(obj.getPurchaseApplicationId(), purchaseOrderEntity.getCode())).distinct().collect(Collectors.toList());
                moduleOperateLogService.batchAddModuleOperateLog("生成采购单【%s】", ModuleTypeEnum.PURCHASE_APPLICATION.getCode(),pairList,"生成采购单");
            }

        }
    }

    @Override
    public PurchaseOrderDetailEntity getByPurchaseOrderIdAndSkuId(String purchaseOrderId, String skuId) {
        return lambdaQuery()
                .eq(PurchaseOrderDetailEntity::getPurchaseOrderId,purchaseOrderId)
                .eq(PurchaseOrderDetailEntity::getSkuId,skuId)
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<PurchaseOrderDetailDTO.UpdateDTO> details, String purchaseOrderId) {
        if (details == null) {
            details = new ArrayList<>();
        }
        List<PurchaseOrderDetailDTO.AddDTO> addList = BeanMapperUtils.copyList(PurchaseOrderDetailDTO.AddDTO.class, details);
        //验证报价信息
        checkPurchasePrice(addList,purchaseOrderId);

        //原明细数据
        List<PurchaseOrderDetailEntity> oldList = this.listByPurchaseOrderId(purchaseOrderId);
        List<String> deleteIds = getDeleteIds(details, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<PurchaseOrderDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getPurchaseOrderId(), obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(),pairList,"编辑操作");
            //删除关联关系
            purchaseApplicationRefPoService.removeByPurchaseOrderDetailIds(deleteIds);
            this.removeByIds(deleteIds);
        }
        List<PurchaseOrderDetailEntity> newList = BeanMapperUtils.copyList(PurchaseOrderDetailEntity.class, details);

        //处理明细id及操作日志
        doOpHandleDetails(newList,purchaseOrderId);

        //新增或修改采购订单明细
        this.saveOrUpdateBatch(newList);

        //更新采购申请单生成类型
        updateCreatePoType(purchaseOrderId);
    }


    @Override
    public List<PurchaseOrderDetailEntity> listByPurchaseOrderId(String purchaseOrderId) {
        return  lambdaQuery().eq(PurchaseOrderDetailEntity::getPurchaseOrderId,purchaseOrderId).list();
    }

    @Override
    public List<PurchaseOrderDetailEntity> listByPurchaseOrderIds(List<String> purchaseOrderIds) {
        return  lambdaQuery().in(PurchaseOrderDetailEntity::getPurchaseOrderId,purchaseOrderIds).list();
    }

    @Override
    public void removeByPurchaseOrderIds(List<String> purchaseOrderIds) {
        lambdaUpdate().in(PurchaseOrderDetailEntity::getPurchaseOrderId,purchaseOrderIds).remove();
    }

    @Override
    public void updateArrivalStatusByIds(String arrivalStatus, List<String> ids) {
        lambdaUpdate()
                .in(PurchaseOrderDetailEntity::getId,ids)
                .set(PurchaseOrderDetailEntity::getArrivalStatus,arrivalStatus)
                .set(PurchaseOrderDetailEntity::getArrivalTime, LocalDateTime.now())
                .update();
    }

    /**
     * 更新生成状态
     */
    @Override
    public void updateCreatePoType (String purchaseOrderId) {
        List<PurchaseApplicationRefPoDTO.ListDTO> refList = purchaseApplicationRefPoService.list(new PurchaseApplicationRefPoDTO.SearchParamDTO().setPurchaseOrderIds(Arrays.asList(purchaseOrderId)));
        //无关联数据则不处理
        if (CollectionUtils.isEmpty(refList)) {
            return;
        }
        List<String> purchaseApplicationDetailIds = refList.stream().map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseApplicationDetailId).collect(Collectors.toList());

        //采购申请明细下已采购数据
        List<PurchaseApplicationRefPoDTO.ListDTO> list = purchaseApplicationRefPoService.list(new PurchaseApplicationRefPoDTO.SearchParamDTO().setPurchaseApplicationDetailIds(purchaseApplicationDetailIds));
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //采购申请单明细数据
        List<PurchaseApplicationDetailEntity> purchaseApplicationDetailList = purchaseApplicationDetailService.listByIds(purchaseApplicationDetailIds);
        if (CollectionUtils.isEmpty(purchaseApplicationDetailList)) {
            throw new ServiceException(ApiError.ERROR_98017);
        }
        List<PurchaseApplicationDetailEntity> resultList = new ArrayList<>();
        Map<String, List<PurchaseApplicationRefPoDTO.ListDTO>> map = list.stream().collect(Collectors.groupingBy(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseApplicationDetailId));
        for (Map.Entry<String, List<PurchaseApplicationRefPoDTO.ListDTO>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<PurchaseApplicationRefPoDTO.ListDTO> value = entry.getValue();

            PurchaseApplicationDetailEntity entity = new PurchaseApplicationDetailEntity();
            entity.setId(key);
            //申请数量
            PurchaseApplicationDetailEntity applicationDetail = purchaseApplicationDetailList.stream().filter(obj -> obj.getId().equals(key)).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(applicationDetail)) {
                throw new ServiceException(ApiError.ERROR_98017);
            }
            Integer applyQty = applicationDetail.getApplyQty();
            //采购数量
            Integer purchaseQty = value.stream().map(PurchaseApplicationRefPoDTO.ListDTO::getPurchaseQty).reduce(MathUtil.ZERO, Integer::sum);
            if (MathUtil.compareTo(purchaseQty,MathUtil.ZERO) == MathUtil.ZERO) {
                entity.setCreatePoType(CreatePoTypeEnum.NOT_GENERATED.getStatus());
            }
            if (MathUtil.compareTo(applyQty,purchaseQty) == MathUtil.ZERO) {
                entity.setCreatePoType(CreatePoTypeEnum.ALL_GENERATED.getStatus());
            }
            if (MathUtil.compareTo(applyQty,purchaseQty) > MathUtil.ZERO) {
                entity.setCreatePoType(CreatePoTypeEnum.PARTIAL_GENERATED.getStatus());
            }
            if (MathUtil.compareTo(purchaseQty,applyQty) > MathUtil.ZERO) {
                throw new ServiceException(new ApiResult(1,String.format("采购申请明细SKU【%s】采购数量【%s】不能大于【%s】",applicationDetail.getSkuNo(),purchaseQty,applyQty)));
            }
            resultList.add(entity);
        }
        purchaseApplicationDetailService.updateBatchById(resultList);
    }


    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<PurchaseOrderDetailDTO.UpdateDTO> newList, List<PurchaseOrderDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(PurchaseOrderDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 处理明细中的数据id
     */
    private void doOpHandleDetails (List<PurchaseOrderDetailEntity> newList, String purchaseOrderId) {

        //添加操作日志
        List<PurchaseOrderDetailEntity> addList = newList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(addList)) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(purchaseOrderId, obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.PURCHASE_ORDER.getCode(), addPairList, "编辑操作");
        }

        for (PurchaseOrderDetailEntity entity : newList) {
            entity.setPurchaseOrderId(purchaseOrderId);
            entity.setTaxRate(MathUtil.divide(entity.getTaxRate(), MathUtil.BigDecimal_100));
            entity.setPurchaseAmount(MathUtil.multiply(entity.getTaxPrice(),entity.getPurchaseQty()));
            //操作日志
            if (StringUtils.isNotBlank(entity.getId())) {
                PurchaseOrderDetailEntity old = this.getById(entity.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98026);
                }
                moduleOperateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.PURCHASE_ORDER.getCode(),purchaseOrderId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
    }


    /**
     * 供应商报价验证
     */
    private void checkPurchasePrice (List<PurchaseOrderDetailDTO.AddDTO> details,String purchaseOrderId) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }

        PurchaseOrderSupplierEntity supplierEntity = purchaseOrderSupplierService.getByPurchaseOrderId(purchaseOrderId);
        if (ObjectUtils.isEmpty(supplierEntity)) {
            throw new ServiceException(ApiError.ERROR_98036);
        }
        //验证录入的SKU明细报价信息是否正确
        List<PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO> priceList = details.stream().filter(obj -> !Boolean.TRUE.equals(obj.getIsGift())).map(obj -> new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO(obj.getPurchaseQty(), obj.getSkuId(), obj.getSkuNo(), supplierEntity.getSupplierId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(priceList)) {
            return;
        }
        for (PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO priceDTO: priceList) {

            List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> taxPriceList = purchasePriceDetailService.getTaxPrice(priceDTO);
            PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO viewDTO = taxPriceList.get(0);
            //汇率
            BigDecimal taxRate = viewDTO.getTaxRate();
            //单价
            BigDecimal taxPrice = viewDTO.getTaxPrice();

            PurchaseOrderDetailDTO.AddDTO addDTO = details.stream().filter(obj -> obj.getSkuId().equals(priceDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtils.isEmpty(addDTO)) {
                String error = String.format("SKU【%s】未找到数量【%s】的供应商报价信息", priceDTO.getSkuNo(), priceDTO.getPurchaseQty());
                throw new ServiceException(new ApiResult(1,error));
            }
            if (MathUtil.compareTo(taxPrice,addDTO.getTaxPrice()) != MathUtil.ZERO) {
                String error = String.format("SKU【%s】,数量【%s】录入单价与报价单价不匹配", priceDTO.getSkuNo(), priceDTO.getPurchaseQty());
                throw new ServiceException(new ApiResult(1,error));
            }
            addDTO.setTaxRate(taxRate);
        }


    }

    /**
     * 根据明细id查询明细
     * @Author Luo_WG
     * @Date 2023/4/13 14:00
     * @param ids ids
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderDetailEntity>
     **/
    @Override
    public List<PurchaseOrderDetailEntity> listDetailByIds(List<String> ids){
        LambdaQueryWrapper<PurchaseOrderDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(PurchaseOrderDetailEntity::getId, ids);
        return this.list(queryWrapper);
    }

    /**
     * 根据主表Id查询明细
     * @Author Luo_WG
     * @Date 2023/4/20 18:37
     * @param id id
     * @return java.util.List<com.erp.model.scm.entity.PurchaseOrderDetailEntity>
     **/
    @Override
    public List<PurchaseOrderDetailEntity> listPurchaseOrderDetailByOrderId(String id) {
        LambdaQueryWrapper<PurchaseOrderDetailEntity> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(PurchaseOrderDetailEntity::getPurchaseOrderId, id);
        return this.list(queryWrapper);
    }


    @Override
    public List<PurchaseOrderDetailDTO.ViewProductDTO> viewProduct(PurchaseOrderDetailDTO.ProductSearchParamDTO dto) {
        List<PurchaseOrderDetailDTO.ViewProductDTO> list = baseMapper.viewProduct(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Collections.EMPTY_LIST;
        }
        List<String> purchaseDetailIds = list.stream().map(PurchaseOrderDetailDTO.ViewProductDTO::getPurchaseOrderDetailId).collect(Collectors.toList());
        //查询收货数据
        List<WarehouseReceiveDetailEntity> receiveDetails = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(purchaseDetailIds);

        //查询退货数据
        List<PurchaseReturnOrderDetailEntity> returnOrderDetails = wmsTaskFeign.listPurchaseReturnOrderDetailBySourceDetailIds(purchaseDetailIds);

        //查询入库数据
        List<PurchaseStockInDetailEntity> stockInDetails = wmsTaskFeign.listPurchaseStockInDetailByPodIds(purchaseDetailIds);

        for (PurchaseOrderDetailDTO.ViewProductDTO viewProductDTO : list) {

            Integer receiveQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(receiveDetails)) {
                 receiveQty = receiveDetails.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(viewProductDTO.getPurchaseOrderDetailId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //收货数量
            viewProductDTO.setReceiveQty(receiveQty);
            //未收货数量
            viewProductDTO.setUnReceiveQty(viewProductDTO.getPurchaseQty() - receiveQty);

            //超收数量
            Integer exceedQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(receiveDetails)) {
                exceedQty = receiveDetails.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(viewProductDTO.getPurchaseOrderDetailId())).map(WarehouseReceiveDetailEntity::getExceedQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            viewProductDTO.setExceedQty(exceedQty);

            //退货数量
            Integer realityReturnQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(returnOrderDetails)) {
                 realityReturnQty = returnOrderDetails.stream().filter(obj -> obj.getSourceDetailId().equals(viewProductDTO.getPurchaseOrderDetailId())).map(PurchaseReturnOrderDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            viewProductDTO.setRealityReturnQty(realityReturnQty);
            //已入库数量
            Integer stockInQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(stockInDetails)) {
                stockInQty = stockInDetails.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(viewProductDTO.getPurchaseOrderDetailId())).map(PurchaseStockInDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            viewProductDTO.setHasStockInQty(stockInQty);
            //未入库数量
            viewProductDTO.setUnStockInQty(viewProductDTO.getPurchaseQty() - stockInQty);
        }
        return list;
    }
}
