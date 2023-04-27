package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.service.SuperServiceImpl;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.dto.PurchaseChangeDetailDTO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.entity.PurchaseChangeDetailEntity;
import com.erp.model.scm.entity.PurchaseChangeEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.entity.PurchaseStockInDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.PurchaseChangeDetailMapper;
import com.erp.server.scm.service.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class PurchaseChangeDetailServiceImpl extends SuperServiceImpl<PurchaseChangeDetailMapper, PurchaseChangeDetailEntity> implements PurchaseChangeDetailService {

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;

    @Resource
    private PurchaseChangeService purchaseChangeService;

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(List<PurchaseChangeDetailDTO.AddDTO> details, String purchaseChangeId) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        List<PurchaseChangeDetailEntity> list = BeanMapperUtils.copyList(PurchaseChangeDetailEntity.class, details);
        //验证数量、单价是否符合供应商报价
        checkPurchasePrice(list,purchaseChangeId);
        //计算金额
        doOpCalculateAmount(list,purchaseChangeId);
        this.saveBatch(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(List<PurchaseChangeDetailDTO.UpdateDTO> details, String purchaseChangeId) {
        if (details == null) {
            details = new ArrayList<>();
        }
        //原明细数据
        List<PurchaseChangeDetailEntity> oldList = this.listByPurchaseChangeIds(Arrays.asList(purchaseChangeId));
        List<String> deleteIds = getDeleteIds(details, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {

            List<PurchaseChangeDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getPurchaseChangeId(), obj.getSkuNo())).collect(Collectors.toList());
            moduleOperateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.PURCHASE_CHANGE.getCode(), pairList, "编辑操作");
            this.removeByIds(deleteIds);
        }
        List<PurchaseChangeDetailEntity> newList = BeanMapperUtils.copyList(PurchaseChangeDetailEntity.class, details);
        //验证数量、单价是否符合供应商报价
        checkPurchasePrice(newList,purchaseChangeId);
        //计算金额
        doOpCalculateAmount(newList,purchaseChangeId);
        this.saveOrUpdateBatch(newList);
    }

    @Override
    public List<PurchaseChangeDetailEntity> listByPurchaseChangeIds(List<String> purchaseChangeIds) {
        return lambdaQuery().in(PurchaseChangeDetailEntity::getPurchaseChangeId, purchaseChangeIds).list();
    }


    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<PurchaseChangeDetailDTO.UpdateDTO> newList, List<PurchaseChangeDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(PurchaseChangeDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(PurchaseChangeDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 更新金额
     */
    private void doOpCalculateAmount(List<PurchaseChangeDetailEntity> list,String purchaseChangeId) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        //查询编辑前数据
        List<String> detailIds = list.stream().map(PurchaseChangeDetailEntity::getId).collect(Collectors.toList());
        List<PurchaseChangeDetailEntity> oldList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(detailIds)) {
             oldList = this.listByIds(detailIds);
        }

        for (PurchaseChangeDetailEntity entity : list) {
            entity.setPurchaseChangeId(purchaseChangeId);
            entity.setAmount(MathUtil.multiply(entity.getPrice(),entity.getQty()));
            //操作日志
            if (StringUtils.isBlank(entity.getId())) {
                moduleOperateLogService.addModuleOperateLog(String.format("新增了一条SKU【%s】",entity.getSkuNo()), ModuleTypeEnum.PURCHASE_CHANGE.getCode(),purchaseChangeId,"编辑操作");
            } else {
                PurchaseChangeDetailEntity old = oldList.stream().filter(obj -> obj.getId().equals(entity.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.ERROR_98043);
                }
                moduleOperateLogService.addModuleOperateLogByObj(old,entity, ModuleTypeEnum.PURCHASE_CHANGE.getCode(),purchaseChangeId,"",String.format("【%s】",old.getSkuNo()));
            }
        }
    }

    /**
     * @description: 验证是否存在供应商报价
     * @author Will
     * @date: 2023/4/3 16:44
     */
    private void checkPurchasePrice (List<PurchaseChangeDetailEntity> list,String purchaseChangeId) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        PurchaseChangeEntity purchaseChangeEntity = purchaseChangeService.getById(purchaseChangeId);
        if (ObjectUtils.isEmpty(purchaseChangeEntity)) {
            throw new ServiceException(ApiError.ERROR_98042);
        }
        List<String> purchaseOrderDetailIds = list.stream().map(PurchaseChangeDetailEntity::getPurchaseOrderDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = purchaseOrderDetailService.listByIds(purchaseOrderDetailIds);
        if (CollectionUtils.isEmpty(purchaseOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_98026);
        }
        List<String> podIds = list.stream().map(PurchaseChangeDetailEntity::getPurchaseOrderDetailId).distinct().collect(Collectors.toList());

        //收货信息
        List<WarehouseReceiveDetailEntity> receiveDetailList = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(podIds);

        //入库信息
        List<PurchaseStockInDetailEntity> purchaseStockInDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(podIds);


        for (PurchaseChangeDetailEntity purchaseChangeDetailEntity : list) {
            //变更后数量不能小于收货数量
            if (CollectionUtils.isNotEmpty(receiveDetailList)) {
                Integer receiveQty = receiveDetailList.stream()
                        .filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseChangeDetailEntity.getPurchaseOrderDetailId()))
                        .map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                if (receiveQty > purchaseChangeDetailEntity.getQty()) {
                    throw new ServiceException(new ApiResult(1,String.format("SKU【%s】数量不能小于收货数量【%s】",purchaseChangeDetailEntity.getSkuNo(),receiveQty)));
                }
            }
            //变更后数量不能小于入库数量
            if (CollectionUtils.isNotEmpty(purchaseStockInDetailList)) {
                Integer stockInQty = purchaseStockInDetailList.stream()
                        .filter(obj -> obj.getPurchaseOrderDetailId().equals(purchaseChangeDetailEntity.getPurchaseOrderDetailId()))
                        .map(PurchaseStockInDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
                if (stockInQty > purchaseChangeDetailEntity.getQty()) {
                    throw new ServiceException(new ApiResult(1,String.format("SKU【%s】数量不能小于入库数量【%s】",purchaseChangeDetailEntity.getSkuNo(),stockInQty)));
                }
            }

            //赠品无需判断供应商报价
            long count = purchaseOrderDetailList.stream().filter(obj -> obj.getId().equals(purchaseChangeDetailEntity.getPurchaseOrderDetailId()) && obj.getIsGift()).count();
            if (count > 0) {
                continue;
            }

            PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO dto = new PurchasePriceDetailDTO.PurchaseTaxPriceSearchDTO(purchaseChangeDetailEntity.getQty(),purchaseChangeDetailEntity.getSkuId(),purchaseChangeDetailEntity.getSkuNo(),purchaseChangeEntity.getSupplierId());
            List<PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO> taxPriceList = purchasePriceDetailService.getTaxPrice(dto);
            PurchasePriceDetailDTO.PurchaseTaxPriceViewDTO viewDTO = taxPriceList.get(0);
            //单价
            BigDecimal taxPrice = viewDTO.getTaxPrice();
            if (MathUtil.compareTo(taxPrice,purchaseChangeDetailEntity.getPrice()) != MathUtil.ZERO) {
                String error = String.format("SKU【%s】,数量【%s】录入单价与报价单价不匹配", purchaseChangeDetailEntity.getSkuNo(), purchaseChangeDetailEntity.getQty());
                throw new ServiceException(new ApiResult(1,error));
            }
        }
    }
}