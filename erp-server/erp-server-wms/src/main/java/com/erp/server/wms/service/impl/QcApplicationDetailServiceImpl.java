package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderSupplierEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;
import com.erp.model.srm.enums.DeliveryOrderEnum;
import com.erp.model.wms.dto.QcApplicationDetailDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.dto.excel.QcApplicationImportExcelDTO;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.PoReturnDetailEntity;
import com.erp.model.wms.entity.QcApplicationDetailEntity;
import com.erp.model.wms.entity.QcApplicationEntity;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.srm.feign.SrmDeliveryOrderFeign;
import com.erp.server.wms.listener.QcApplicationExcelListener;
import com.erp.server.wms.mapper.QcApplicationDetailMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


/**
 * <p>
 * 质检申请单明细表 服务实现类
 * </p>
 *
 * @author will
 * @since 2026-03-20
 */
@Slf4j
@Service
public class QcApplicationDetailServiceImpl extends SuperServiceImpl<QcApplicationDetailMapper, QcApplicationDetailEntity> implements QcApplicationDetailService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private FileFeign fileFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private PoReturnDetailService poReturnDetailService;
    @Resource
    private PoInstockDetailService poInstockDetailService;
    @Resource
    private WarehouseReceiveService warehouseReceiveService;
    @Resource
    private SrmDeliveryOrderFeign srmDeliveryOrderFeign;


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(List<QcApplicationDetailDTO.AddDTO> detailList, QcApplicationEntity qcApplicationEntity) {
        List<QcApplicationDetailEntity> qcApplicationDetailList = BeanUtil.copyToList(detailList, QcApplicationDetailEntity.class);

        //数据校验
        checkSourceQty(qcApplicationDetailList,qcApplicationEntity);

        // 数据处理
        handleData(qcApplicationDetailList,qcApplicationEntity);

        log.info("开始新增质检申请单明细单");
        boolean save = super.saveBatch(qcApplicationDetailList);
        if(!save) {
            throw new ServiceException("质检申请单明细单保存失败");
        }
        return Boolean.TRUE;
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<QcApplicationDetailDTO.UpdateDTO> detailList, QcApplicationEntity qcApplicationEntity) {
        if (CollUtil.isNotEmpty(detailList)) {
            throw new ServiceException(ApiError.QC_APPLICATION_DETAIL_NOT_EXIST);
        }
        List<QcApplicationDetailEntity> qcApplicationDetailList = BeanUtil.copyToList(detailList, QcApplicationDetailEntity.class);

        //原明细数据
        List<QcApplicationDetailEntity> oldList = this.listByMainId(qcApplicationEntity.getId());
        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<QcApplicationDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个SKU【%s】", ModuleTypeEnum.QC_APPLICATION.getCode(),pairList,"编辑操作");
            List<QcApplicationDetailEntity> list = lambdaQuery().in(QcApplicationDetailEntity::getMainId, deleteIds).list();
            list.forEach(req -> req.setIsDeleted(Boolean.TRUE));
            this.removeByIds(deleteIds);
        }

        // 数据处理
        handleData(qcApplicationDetailList,qcApplicationEntity);
        log.info("编辑 开始修改质检申请单明细单数据，id：【{}】", qcApplicationEntity.getId());
        boolean save = super.updateBatchById(qcApplicationDetailList);
        if(!save) {
            throw new ServiceException("质检申请单明细单保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public QcApplicationDetailDTO.ImportDTO importExcel(QcApplicationDetailDTO.ImportParamDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());

        QcApplicationExcelListener excelListenerUtil = new QcApplicationExcelListener();

        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes),  QcApplicationImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }
        //验证导入数据是否为空
        List<QcApplicationImportExcelDTO> excelDateList = excelListenerUtil.getAllList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.FILE_DATA_REQUIRED);
        }
        QcApplicationDetailDTO.ImportDTO importDTO = new QcApplicationDetailDTO.ImportDTO();
        //导入数据处理
        List<QcApplicationImportExcelDTO> successList = excelListenerUtil.getSuccessList();
        //导出错误数据
        List<QcApplicationImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        //处理数据
        List<QcApplicationDetailDTO.ImportResultDTO> importResultList = handleImportSuccessList(successList, errorList,dto.getSourceId());

        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "质检申请单错误数据.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, QcApplicationImportExcelDTO.class);
            if (file != null && !file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importDTO.setSuccessList(importResultList);
        importDTO.setErrorUrl(url);
        return importDTO;
    }

    @Override
    public List<QcApplicationDetailEntity> listByMainId(String mainId) {
        return lambdaQuery().eq(QcApplicationDetailEntity::getMainId, mainId).list();
    }


    @Override
    public List<QcApplicationDetailDTO.ImportResultDTO> handleImportSuccessList(List<QcApplicationImportExcelDTO> successList, List<QcApplicationImportExcelDTO> errorList,String sourceId) {
        if (CollectionUtils.isEmpty(successList)) {
            return Collections.emptyList();
        }
        List<QcApplicationDetailDTO.ImportResultDTO> resultList = new ArrayList<>();

        List<PurchaseOrderDetailEntity> detailList = new ArrayList<>();
        if (CharSequenceUtil.isNotBlank(sourceId)) {
            //查询采购订单数据
            detailList = FeignQuery.create(PurchaseOrderDetailEntity.class).eq(PurchaseOrderDetailEntity::getPurchaseOrderId, sourceId).list();
        }

        //SKU信息
        List<String> skuNoList = successList.stream().map(QcApplicationImportExcelDTO::getSkuNo).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOS = plmTaskFeign.listSkuPurchaseBySkuNos(skuNoList);
        Map<String, SkuVO> skuMap = CollUtil.isEmpty(skuVOS) ? new HashMap<>() : skuVOS.stream().collect(Collectors.toMap(SkuVO::getSkuNo, obj -> obj));

        //供应商信息
        List<String> supplierNameList = successList.stream().map(QcApplicationImportExcelDTO::getSupplierName).distinct().collect(Collectors.toList());
        List<SupplierEntity> supplierList = FeignQuery.create(SupplierEntity.class).in(SupplierEntity::getName, supplierNameList).list();
        Map<String, SupplierEntity> supplierMap = CollUtil.isEmpty(supplierList) ? new HashMap<>() : supplierList.stream().collect(Collectors.toMap(SupplierEntity::getName, obj -> obj));


        for ( QcApplicationImportExcelDTO data : successList) {
            QcApplicationDetailDTO.ImportResultDTO resultDTO = new QcApplicationDetailDTO.ImportResultDTO();
            BeanUtil.copyProperties(data, resultDTO);

            PurchaseOrderDetailEntity purchaseOrderDetailEntity = detailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSkuNo(), data.getSkuNo())).findFirst().orElse(null);
            if (CharSequenceUtil.isNotBlank(sourceId) && ObjectUtils.isEmpty(purchaseOrderDetailEntity)) {
                data.setErrorMsg("采购订单中不存在该SKU");
                errorList.add(data);
                continue;
            }
            //sku信息
            SkuVO skuVO = skuMap.get(data.getSkuNo());
            if (ObjectUtil.isEmpty(skuVO)) {
                data.setErrorMsg("未找到SKU信息");
                errorList.add(data);
                continue;
            }
            if ( !ProductDetailStatusEnum.APPROVAL_PASS.getCode().equals(skuVO.getStatus()))  {
                data.setErrorMsg("SKU信息未审核通过");
                errorList.add(data);
                continue;
            }
            //供应商信息
            SupplierEntity supplierEntity = supplierMap.get(data.getSupplierName());
            if (CharSequenceUtil.isNotBlank(data.getSupplierName()) && ObjectUtil.isEmpty(supplierEntity)) {
                data.setErrorMsg("未找到供应商信息");
                errorList.add(data);
                continue;
            }
            resultDTO.setSkuId(skuVO.getSkuId());
            resultDTO.setProductName(skuVO.getSkuName());
            resultDTO.setEan(skuVO.getEan());
            resultDTO.setSourceDetailId(ObjectUtil.isEmpty(purchaseOrderDetailEntity) ? "" : purchaseOrderDetailEntity.getId());
            resultDTO.setQty(MathUtil.valueOfInteger(data.getQtyStr()));
            if (ObjectUtil.isNotEmpty(supplierEntity)) {
                resultDTO.setSupplierId(supplierEntity.getId());
                resultDTO.setSupplierName(supplierEntity.getName());
            }
            resultList.add(resultDTO);
        }
        return resultList;
    }

    @Override
    public void removeByMainId(String mainId) {
        lambdaUpdate().eq(QcApplicationDetailEntity::getMainId,mainId).remove();
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<QcApplicationDetailDTO.UpdateDTO> newList, List<QcApplicationDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(QcApplicationDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(QcApplicationDetailEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
     * 校验来源数量
     * @author will
     * @date 2026/3/25 16:00
     * @param qcApplicationDetailList
     * @return void
     */
    private void checkSourceQty (List<QcApplicationDetailEntity> qcApplicationDetailList,QcApplicationEntity qcApplicationEntity) {
        //无来源类型不校验数量
        if (CharSequenceUtil.isBlank(qcApplicationEntity.getSourceType())) {
            return;
        }
        //采购订单来源校验数量
        if (CharSequenceUtil.equals(qcApplicationEntity.getSourceType(), SourceTypeEnum.PURCHASE_ORDER.getCode())) {
            checkSourcePoQty(qcApplicationDetailList);
        } else if (CharSequenceUtil.equals(qcApplicationEntity.getSourceType(),SourceTypeEnum.WAIT_DELIVERY.getCode())) {
            //待发货来源校验数量
            checkSourceWaitDeliveryQty(qcApplicationDetailList,qcApplicationEntity);
        }
    }

    /**
     * 校验待发货来源数量
     * @author will
     * @date 2026/3/25 16:05
     * @param qcApplicationDetailList
     * @return void
     */
    private void checkSourceWaitDeliveryQty(List<QcApplicationDetailEntity> qcApplicationDetailList,QcApplicationEntity qcApplicationEntity) {

        List<String> podIdList = qcApplicationDetailList.stream().map(QcApplicationDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        //采购订单明细集合
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = FeignQuery.create(PurchaseOrderDetailEntity.class).in(PurchaseOrderDetailEntity::getId, podIdList).list();
        Map<String, PurchaseOrderDetailEntity> podMap = purchaseOrderDetailList.stream().collect(Collectors.toMap(BaseEntity::getId, obj ->obj));

        //送货信息
        List<DeliveryOrderDetailDTO.ListDTO> deliveryOrderDetailList = srmDeliveryOrderFeign.listDetailDTOByDetailSourceIds(podIdList);
        //查询采购签收信息
        List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> receiveList = warehouseReceiveService.getReceiveListByPurchaseOrderIds(Collections.singletonList(qcApplicationEntity.getId()));
        //入库信息
        List<PoInstockDetailEntity> stockInDetailList = poInstockDetailService.listDetailByPodIds(podIdList);
        //退货信息
        List<PoReturnDetailEntity> returnOrderDetailList = poReturnDetailService.listReturnOrderDetailByPodIds(podIdList);

        //根据来源明细id查询质检申请
        List<QcApplicationDetailEntity> oldDetailList = this.listDetailBySourceDetailIds(podIdList);

        for ( QcApplicationDetailEntity detailEntity : qcApplicationDetailList) {
            //已送货数量
            Integer deliveredQty = MathUtil.ZERO;
            //有送货单的收货数量
            Integer hasDeliveryReceiveQty = MathUtil.ZERO;
            //无送货单收货数量
            Integer unDeliveryReceiveQty = MathUtil.ZERO;
            //无收货单的入库数量
            Integer unReceiveInstockQty = MathUtil.ZERO;
            //收发差异
            Integer diffSendAndReceive = MathUtil.ZERO;
            //退货补货数量
            Integer returnQty = MathUtil.ZERO;
            //采购订单数量
            PurchaseOrderDetailEntity poDetailEntity = podMap.get(detailEntity.getSourceDetailId());
            if (ObjectUtil.isEmpty(poDetailEntity)) {
                throw new ServiceException(ApiError.PO_DETAIL_NOT_FOUND);
            }

            //收货数量
            if (CollectionUtils.isNotEmpty(receiveList)) {
                hasDeliveryReceiveQty = receiveList.stream().filter(e -> StringUtils.isNotEmpty(e.getSourceType())
                                && e.getSourceType().equalsIgnoreCase(SourceTypeEnum.DELIVERY_ORDER.getCode())
                                && e.getPurchaseOrderDetailId().equals(detailEntity.getSourceDetailId()))
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                unDeliveryReceiveQty = receiveList.stream().filter(e -> StringUtils.isEmpty(e.getSourceId()) && e.getPurchaseOrderDetailId().equals(detailEntity.getSourceDetailId()))
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //无收货单的入库数量
            if (CollectionUtils.isNotEmpty(stockInDetailList)) {
                // 采购入库单（无收货单），只有审核通过的才占用库存数量
                unReceiveInstockQty = stockInDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(detailEntity.getSourceDetailId())
                                && Objects.equals(e.getSourceDetailId(), detailEntity.getSourceDetailId())
                                && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()))
                        .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);

            }
            // 退货单（退货补货的才会导致在途数量变化）
            if (CollectionUtils.isNotEmpty(returnOrderDetailList)) {
                returnQty = returnOrderDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(detailEntity.getSourceDetailId())
                                && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())
                                && StrUtils.isNotEmpty(e.getPurchaseOrderDetailId())
                                && Objects.equals(e.getReturnMode(), ReturnModeEnum.REPLENISHMENT.getCode()))
                        .map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
            }

            //已送货数量
            if (CollectionUtils.isNotEmpty(deliveryOrderDetailList)) {
                deliveredQty = deliveryOrderDetailList.stream().filter(e -> e.getSourceDetailId().equals(detailEntity.getSourceDetailId()))
                        .map(DeliveryOrderDetailDTO.ListDTO::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);

                //发货数量 - 已审核收货数量
                Integer srmDeliveryQty = deliveryOrderDetailList.stream().filter(e -> e.getSourceDetailId().equals(detailEntity.getSourceDetailId())
                                && com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(e.getReceiptStatus()) && e.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode()))
                        .map(DeliveryOrderDetailDTO.ListDTO::getDeliveryQty)
                        .reduce(MathUtil.ZERO, Integer::sum);
                diffSendAndReceive = srmDeliveryQty - hasDeliveryReceiveQty;
                //剩余送货量/可下推量=采购订单-送货单数量-无送货单收货数量-无收货单的入库数量+[收发差异]+退货补货数量[库存退货/质检退货]
                Integer waitPushQty = poDetailEntity.getPurchaseQty() - deliveredQty - unDeliveryReceiveQty - unReceiveInstockQty + diffSendAndReceive + returnQty;

                //已申请数量（审核通过的质检申请单数量）
                Integer hasPushQty = oldDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceDetailId(), detailEntity.getSourceDetailId()) && !CharSequenceUtil.equals(obj.getId(), detailEntity.getId())).map(QcApplicationDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);
                //数量校验
                if ( detailEntity.getQty() > waitPushQty - hasPushQty) {
                    throw new ServiceException(ApiError.QC_APPLICATION_DETAIL_QTY_NOT_GREATER_THAN_WAIT_DELIVERY_QTY, waitPushQty - hasPushQty);
                }
            }
        }
    }


    /**
     * 校验采购订单来源数量
     * @author will
     * @date 2026/3/25 16:05
     * @param qcApplicationDetailList
     * @return void
     */
    private void checkSourcePoQty(List<QcApplicationDetailEntity> qcApplicationDetailList) {
        List<String> podIdList = qcApplicationDetailList.stream().map(QcApplicationDetailEntity::getSourceDetailId).distinct().collect(Collectors.toList());
        //查询采购订单明细数据
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = FeignQuery.create(PurchaseOrderDetailEntity.class).in(PurchaseOrderDetailEntity::getId, podIdList).list();
        //查询退货数据
        List<PoReturnDetailEntity> purchaseReturnOrderDetailList = poReturnDetailService.listReturnOrderDetailByPodIds(podIdList);
        //查询入库数据
        List<PoInstockDetailEntity> stockInDetailList = poInstockDetailService.listDetailByPodIds(podIdList);
        //根据来源明细id查询质检申请
        List<QcApplicationDetailEntity> oldDetailList = this.listDetailBySourceDetailIds(podIdList);
        for (QcApplicationDetailEntity detailEntity : qcApplicationDetailList) {
            //采购明细
            PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), detailEntity.getSourceDetailId())).findFirst().orElse(null);
            if ( ObjectUtil.isEmpty(purchaseOrderDetailEntity)) {
                throw new ServiceException(ApiError.PO_DETAIL_NOT_FOUND);
            }
            //已申请数量（审核通过的质检申请单数量）
            Integer hasPushQty = oldDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getSourceDetailId(), detailEntity.getSourceDetailId()) && !CharSequenceUtil.equals(obj.getId(), detailEntity.getId())).map(QcApplicationDetailEntity::getQty).reduce(MathUtil.ZERO, Integer::sum);

            //退货补数量（审核通过的补货退货单数量）
            Integer returnQty = purchaseReturnOrderDetailList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(detailEntity.getSourceDetailId()) && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())).map(PoReturnDetailEntity::getReplenishQty).reduce(MathUtil.ZERO, Integer::sum);
            //有效入库数量（未审核通过）
            Integer effectiveStockInQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(stockInDetailList)) {
                effectiveStockInQty = stockInDetailList.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(detailEntity.getSourceDetailId())).map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            //未入库数量
            Integer notPushQty = purchaseOrderDetailEntity.getPurchaseQty() - effectiveStockInQty + returnQty - hasPushQty;
            if (detailEntity.getQty() > notPushQty) {
                throw new ServiceException(ApiError.QC_APPLICATION_DETAIL_QTY_NOT_GREATER_THAN_PO_QTY, notPushQty);
            }
        }
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(List<QcApplicationDetailEntity> qcApplicationDetailList,QcApplicationEntity qcApplicationEntity) {
        if (CollUtil.isEmpty(qcApplicationDetailList)) {
            return;
        }

        //添加操作日志
        List<QcApplicationDetailEntity> addList = qcApplicationDetailList.stream().filter(c -> StringUtils.isBlank(c.getId())).collect(Collectors.toList());
        //新增不需要添加新增SKU的日志
        if (CollectionUtils.isNotEmpty(addList)) {
            List<Pair<String, String>> addPairList = addList.stream().map(obj -> new Pair<>(qcApplicationEntity.getId(), obj.getSkuNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("新增了一条SKU【%s】", ModuleTypeEnum.QC_APPLICATION.getCode(), addPairList, "编辑操作");
        }

        //sku信息
        List<String> skuIdList = qcApplicationDetailList.stream().map(QcApplicationDetailEntity::getSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> skuList = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList);
        Map<String, String> skuMap = CollUtil.isEmpty(skuList) ? new HashMap<>() : skuList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, ProductDetailEntity::getSkuNo));

        //查询来源采购订单数据
        PurchaseOrderSupplierEntity poSupplierEntity = CharSequenceUtil.isBlank(qcApplicationEntity.getSourceId()) ? new PurchaseOrderSupplierEntity() : FeignQuery.getById(PurchaseOrderSupplierEntity.class, qcApplicationEntity.getSourceId());

        for (QcApplicationDetailEntity data : qcApplicationDetailList) {
            //校验供应商信息
            if (CharSequenceUtil.isNotBlank(data.getSourceDetailId()) && CharSequenceUtil.isNotBlank(data.getSupplierId()) && !CharSequenceUtil.equals(data.getSupplierId(),poSupplierEntity.getSupplierId())) {
                throw new ServiceException(ApiError.QC_APPLICATION_SUPPLIER_NOT_DIFF);
            }
            //sku编码
            data.setSkuNo(skuMap.get(data.getSkuId()));
            data.setMainId(qcApplicationEntity.getId());
            //操作日志
            if (StringUtils.isNotBlank(data.getId())) {
                QcApplicationDetailEntity old = this.getById(data.getId());
                if (ObjectUtils.isEmpty(old)) {
                    throw new ServiceException(ApiError.QC_APPLICATION_NOT_EXIST);
                }
                operateLogService.addModuleOperateLogByObj(old,data, ModuleTypeEnum.QC_APPLICATION.getCode(),qcApplicationEntity.getId(),"",String.format("【%s】",old.getSkuNo()));
            }
        }
    }

    /**
     * 根据来源明细id查询质检申请单明细数据
     * @author will
     * @date 2026/3/25 15:50
     * @param podIdList
     * @return List<QcApplicationDetailEntity>
     */
    private List<QcApplicationDetailEntity> listDetailBySourceDetailIds (List<String> podIdList) {
        if (CollUtil.isEmpty(podIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(QcApplicationDetailEntity::getSourceDetailId, podIdList).list();
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<QcApplicationDetailDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for(QcApplicationDetailDTO.ListDTO data : list) {
            // TODO 其他如需要显示名称的字段赋值
        }
    }
}
