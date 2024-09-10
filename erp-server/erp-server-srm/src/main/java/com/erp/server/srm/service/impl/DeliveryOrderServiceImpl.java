package com.erp.server.srm.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DataIdempotent;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchaseOrderSrmDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.scm.enums.WaitDeliveryCycleEnum;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.dto.excel.DeliveryOrderExportExcelDTO;
import com.erp.model.srm.dto.excel.DeliveryOrderImportExcelDTO;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.model.srm.entity.DeliveryOrderEntity;
import com.erp.model.srm.enums.DeliveryOrderEnum;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.PoReturnDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveDetailEntity;
import com.erp.model.wms.entity.WarehouseReceiveEntity;
import com.erp.model.wms.enums.PoReceiveSourceTypeEnum;
import com.erp.model.wms.enums.PoReturnConfirmStatusEnum;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.PurchaseOrderFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.srm.convert.DeliveryOrderConverter;
import com.erp.server.srm.listener.DeliveryExcelListener;
import com.erp.server.srm.mapper.DeliveryOrderMapper;
import com.erp.server.srm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 送货单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
 */
@Slf4j
@Service
public class DeliveryOrderServiceImpl extends SuperServiceImpl<DeliveryOrderMapper, DeliveryOrderEntity> implements DeliveryOrderService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private DeliveryOrderDetailService detailService;

    @Resource
    private SupplierFeign supplierFeign;
    @Resource
    private PurchaseOrderFeign purchaseOrderFeign;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private UserService userService;

    @Resource
    private ScmTaskFeign scmTaskFeign;
    @Resource
    private PoReconciliationDetailScmService poReconciliationDetailScmService;
    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    @Override
    public PagingVO<DeliveryOrderDTO.ListDTO> paging(PagingDTO<DeliveryOrderDTO.ParamDTO> dto) {
        Page<DeliveryOrderDTO.ListDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<DeliveryOrderDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        this.fillData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    private void fillData(List<DeliveryOrderDTO.ListDTO> dataList){
        if(CollectionUtils.isEmpty(dataList)){
            return;
        }
        List<String> purchaseIds = dataList.stream().map(DeliveryOrderDTO.ListDTO::getSourceId).distinct().collect(Collectors.toList());
        //查询采购签收信息
        List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> receiveList = wmsTaskFeign.getReceiveListByPurchaseOrderIdsAll(purchaseIds);

        Map<String, SupplierDTO.SupplierSimpleDTO> supplierSimpleDTOMap = supplierFeign.getSupplierSimpleInfo(dataList.stream().map(DeliveryOrderDTO.ListDTO::getSupplierId).distinct().collect(Collectors.toList()));
        List<String> purchaseDetailIds = dataList.stream().filter(v->StringUtils.isNotBlank(v.getReceiveCode())).map(DeliveryOrderDTO.ListDTO::getPurchaseDetailId).distinct().collect(Collectors.toList());
        List<QcInfoDTO.QcReceiveResultDTO> qcReceiveResultDTOList = wmsTaskFeign.getQcReceiveResult(purchaseDetailIds);
        WarehouseReceiveDTO.SourceParamDTO sourceParamDTO = new WarehouseReceiveDTO.SourceParamDTO();
        sourceParamDTO.setSourceIds(dataList.stream().map(DeliveryOrderDTO.ListDTO::getId).distinct().collect(Collectors.toList()));
        sourceParamDTO.setSourceType(PoReceiveSourceTypeEnum.DELIVERY_ORDER.getCode());
        List<WarehouseReceiveEntity> warehouseReceiveEntityList = wmsTaskFeign.listReceiveBySourceTypeAndIds(sourceParamDTO);
        dataList.forEach(v->{
            Integer receiveQty = 0;
            Integer giftReceiveQty = 0;
            //收货数量
            if (CollectionUtils.isNotEmpty(receiveList)) {
                receiveQty = receiveList.stream().filter(e -> e.getSourceDetailId().equals(v.getDetailId()) )
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                giftReceiveQty = receiveList.stream().filter(e -> e.getSourceDetailId().equals(v.getDetailId()) )
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getGiftReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            v.setReceiveQty(receiveQty);
            v.setGiftReceiveQty(giftReceiveQty);

            v.setReceiptStatusName(EnumMessage.getNameByCode(DeliveryOrderEnum.ReceiptStatusEnum.class,v.getReceiptStatus()));
            v.setDetailReceiptStatusName(EnumMessage.getNameByCode(DeliveryOrderEnum.ReceiptStatusEnum.class,v.getDetailReceiptStatus()));
            v.setSupplierName(supplierSimpleDTOMap.containsKey(v.getSupplierId())?supplierSimpleDTOMap.get(v.getSupplierId()).getName():"");
            QcInfoDTO.QcReceiveResultDTO qcReceiveResultDTO = qcReceiveResultDTOList.stream().filter(t->t.getPurchaseDetailId().equals(v.getPurchaseDetailId()) && t.getReceiveCode().equals(v.getReceiveCode())).findFirst().orElse(null);
            if(Objects.nonNull(qcReceiveResultDTO)){
                v.setQcGoodQty(qcReceiveResultDTO.getQcGoodQty());
            }
            WarehouseReceiveEntity warehouseReceiveEntity = warehouseReceiveEntityList.stream().filter(t->t.getSourceId().equals(v.getId())).findFirst().orElse(new WarehouseReceiveEntity());
            v.setReceiveUserName(warehouseReceiveEntity.getReceiveUserName());
        });
    }

    @Override
    public List<DeliveryOrderDTO.TabListDTO> tabList(List<String> supplierIdList) {
        List<DeliveryOrderDTO.TabListDTO> result = new ArrayList<>();
        List<DeliveryOrderDTO.StatusListDTO> statusListDTOList =  this.baseMapper.tabList(supplierIdList);
        //ALL
        DeliveryOrderDTO.TabListDTO allDto = DeliveryOrderDTO.TabListDTO.builder()
                .searchType(DeliveryOrderEnum.SearchTypeEnum.ALL.getCode())
                .count(statusListDTOList.stream().mapToInt(DeliveryOrderDTO.StatusListDTO::getCount).sum())
                .build();
        result.add(allDto);
        //待收货-未打印
        DeliveryOrderDTO.TabListDTO dto2 = DeliveryOrderDTO.TabListDTO.builder()
                .searchType(DeliveryOrderEnum.SearchTypeEnum.WAIT_RECEIVE_AND_PRINT.getCode())
                .count(statusListDTOList.stream().filter(v->(v.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.WAIT_CONFIRMED.getCode())||StringUtils.isBlank(v.getReceiptStatus()))
                && !v.getIsPrint()).mapToInt(DeliveryOrderDTO.StatusListDTO::getCount).sum())
                .build();
        result.add(dto2);
        //待收货-已打印
        DeliveryOrderDTO.TabListDTO dto3 = DeliveryOrderDTO.TabListDTO.builder()
                .searchType(DeliveryOrderEnum.SearchTypeEnum.WAIT_RECEIVE_AND_PRINTED.getCode())
                .count(statusListDTOList.stream().filter(v->(v.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.WAIT_CONFIRMED.getCode())||StringUtils.isBlank(v.getReceiptStatus()))
                        && v.getIsPrint()).mapToInt(DeliveryOrderDTO.StatusListDTO::getCount).sum())
                .build();
        result.add(dto3);
        //待收货
        DeliveryOrderDTO.TabListDTO dto6 = DeliveryOrderDTO.TabListDTO.builder()
                .searchType(DeliveryOrderEnum.SearchTypeEnum.WAIT_RECEIVE.getCode())
                .count(statusListDTOList.stream().filter(v->(v.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.WAIT_CONFIRMED.getCode())||StringUtils.isBlank(v.getReceiptStatus()))).mapToInt(DeliveryOrderDTO.StatusListDTO::getCount).sum())
                .build();
        result.add(dto6);
        //已收货
        DeliveryOrderDTO.TabListDTO dto4 = DeliveryOrderDTO.TabListDTO.builder()
                .searchType(DeliveryOrderEnum.SearchTypeEnum.RECEIVED.getCode())
                .count(statusListDTOList.stream().filter(v->v.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode())).mapToInt(DeliveryOrderDTO.StatusListDTO::getCount).sum())
                .build();
        result.add(dto4);
        //收发差异
        DeliveryOrderDTO.TabListDTO dto5 = DeliveryOrderDTO.TabListDTO.builder()
                .searchType(DeliveryOrderEnum.SearchTypeEnum.QTY_DIFFERENCE.getCode())
                .count(statusListDTOList.stream().filter(v->v.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode())).mapToInt(DeliveryOrderDTO.StatusListDTO::getQtyDifferences).sum())
                .build();
        result.add(dto5);
        return result;
    }

    @Override
    public DeliveryOrderDTO.ViewDTO view(String id) {

        DeliveryOrderEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "送货单"));
        List<DeliveryOrderDetailEntity> detailEntityList = detailService.listByMainId(entity.getId());
        DeliveryOrderDTO.ViewDTO viewDTO = DeliveryOrderConverter.INSTANCE.viewConvert(entity,detailEntityList);
        fillView(viewDTO);
        return viewDTO;
    }

    private void fillView(DeliveryOrderDTO.ViewDTO viewDTO) {
        List<String> purchaseIds = Arrays.asList(viewDTO.getSourceId());
        //查询采购签收信息
        List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> receiveList = wmsTaskFeign.getReceiveListByPurchaseOrderIdsAll(purchaseIds);
        for (DeliveryOrderDetailDTO.ViewDTO dto : viewDTO.getDetailList()) {
            Integer receiveQty = 0;
            Integer giftReceiveQty = 0;
            //收货数量
            if (CollectionUtils.isNotEmpty(receiveList)) {
                receiveQty = receiveList.stream().filter(e -> e.getSourceDetailId().equals(dto.getDetailId()) )
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                giftReceiveQty = receiveList.stream().filter(e -> e.getSourceDetailId().equals(dto.getDetailId()) )
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getGiftReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            dto.setReceiveQty(receiveQty);
            dto.setGiftReceiveQty(giftReceiveQty);
        }
    }

    @Override
    public List<DeliveryOrderDTO.PrintDTO> print(List<String> ids) {
        List<DeliveryOrderEntity> entityList = this.lambdaQuery().in(DeliveryOrderEntity::getId, ids).list();
        List<DeliveryOrderDTO.PrintDTO> printDTOList = BeanMapperUtils.copyList(DeliveryOrderDTO.PrintDTO.class, entityList);
        Map<String,List<DeliveryOrderDetailDTO.PrintDTO>> detailEntityMap = detailService.mapPrintByMainIds(ids);
        printDTOList.forEach(v-> Optional.ofNullable(detailEntityMap.get(v.getId()))
                .ifPresent(detailList -> {
                    detailList.forEach(detail -> detail.setCode(v.getSourceCode()));
                    v.setDetailPrintList(detailList);
                }));
        Map<String, SupplierDTO.SupplierSimpleDTO> supplierSimpleDTOMap = supplierFeign.getSupplierSimpleInfo(entityList.stream().map(DeliveryOrderEntity::getSupplierId).distinct().collect(Collectors.toList()));
        for (DeliveryOrderDTO.PrintDTO printDTO : printDTOList) {
            printDTO.setSupplierName(supplierSimpleDTOMap.containsKey(printDTO.getSupplierId())?supplierSimpleDTOMap.get(printDTO.getSupplierId()).getName():"");
        }
        return printDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> cancelPrint(List<String> ids) {
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        List<DeliveryOrderEntity> deliveryOrderEntityList = this.lambdaQuery().in(DeliveryOrderEntity::getId, ids).list();
        List<DeliveryOrderEntity> updateList = new ArrayList<>();
        for(DeliveryOrderEntity deliveryOrderEntity : deliveryOrderEntityList){
            BatchResultDTO resultDTO = new BatchResultDTO();
            resultDTO.setCode(deliveryOrderEntity.getCode());
            resultDTO.setId(deliveryOrderEntity.getId());
            if(!deliveryOrderEntity.getIsPrint() || StringUtils.isNotBlank(deliveryOrderEntity.getReceiptStatus())){
                resultDTO.setSuccess(false);
                resultDTO.setMsg("存在未打印或者收货状态不为空的送货单，取消打印失败");
            }else{
                resultDTO.setSuccess(true);
                deliveryOrderEntity.setIsPrint(false);
                deliveryOrderEntity.setPrintDate(null);
                updateList.add(deliveryOrderEntity);
            }
            resultDTOList.add(resultDTO);
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            if(!this.updateBatchById(updateList)){
                throw new ServiceException("更新送货单打印状态失败");
            }
        }
        return resultDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(List<String> ids) {
        if(CollectionUtils.isEmpty(ids)){
            return true;
        }
        List<DeliveryOrderDetailEntity> detailEntityList = detailService.listByMainIdList(ids);
        if(detailEntityList.stream().anyMatch(v->StringUtils.isNotBlank(v.getReceiveCode()))){
            throw new ServiceException("已有送货单生成收货单，无法删除");
        }
        if(!this.removeByIds(ids)){
            throw new ServiceException("送货单删除失败");
        }
        if(!detailService.deleteByMainIds(ids)){
            throw new ServiceException("送货单明细删除失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmPrint(List<String> ids) {
        List<DeliveryOrderEntity> entityList = this.lambdaQuery().in(DeliveryOrderEntity::getId, ids).list();
        entityList.forEach(v->{
            v.setIsPrint(true);
            v.setPrintDate(LocalDate.now());
            // 操作日志
            String msg = StrUtil.format("用户【{}】打印【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "送货单", v.getCode());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_ORDER.getCode(), v.getId(), "打印操作");
        });

        if(!this.updateBatchById(entityList)){
            throw new ServiceException("更新打印状态失败");
        }

        return true;
    }

    @Override
    public List<DeliveryOrderExportExcelDTO> getExportList(DeliveryOrderDTO.ParamDTO dto) {
        List<DeliveryOrderExportExcelDTO> list = this.baseMapper.getExportList(dto);
        Map<String, SupplierDTO.SupplierSimpleDTO> supplierSimpleDTOMap = supplierFeign.getSupplierSimpleInfo(list.stream().map(DeliveryOrderExportExcelDTO::getSupplierId).distinct().collect(Collectors.toList()));
        List<String> purchaseDetailIds = list.stream().filter(v->StringUtils.isNotBlank(v.getReceiveCode())).map(DeliveryOrderExportExcelDTO::getPurchaseDetailId).distinct().collect(Collectors.toList());
        List<QcInfoDTO.QcReceiveResultDTO> qcReceiveResultDTOList = wmsTaskFeign.getQcReceiveResult(purchaseDetailIds);
        WarehouseReceiveDTO.SourceParamDTO sourceParamDTO = new WarehouseReceiveDTO.SourceParamDTO();
        sourceParamDTO.setSourceIds(list.stream().map(DeliveryOrderExportExcelDTO::getId).distinct().collect(Collectors.toList()));
        sourceParamDTO.setSourceType(PoReceiveSourceTypeEnum.DELIVERY_ORDER.getCode());
        List<WarehouseReceiveEntity> warehouseReceiveEntityList = wmsTaskFeign.listReceiveBySourceTypeAndIds(sourceParamDTO);

        List<String> purchaseIds = list.stream().map(DeliveryOrderExportExcelDTO::getSourceId).distinct().collect(Collectors.toList());
        //查询采购签收信息
        List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> receiveList = wmsTaskFeign.getReceiveListByPurchaseOrderIdsAll(purchaseIds);

        list.forEach(v->{
            Integer receiveQty = 0;
            Integer giftReceiveQty = 0;
            //收货数量
            if (CollectionUtils.isNotEmpty(receiveList)) {
                receiveQty = receiveList.stream().filter(e -> e.getSourceDetailId().equals(v.getDetailId()) )
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                giftReceiveQty = receiveList.stream().filter(e -> e.getSourceDetailId().equals(v.getDetailId()) )
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getGiftReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            v.setReceiveQty(receiveQty);
            v.setGiftReceiveQty(giftReceiveQty);

            v.setReceiptStatus(EnumMessage.getNameByCode(DeliveryOrderEnum.ReceiptStatusEnum.class,v.getReceiptStatus()));
            v.setDetailReceiptStatus(EnumMessage.getNameByCode(DeliveryOrderEnum.ReceiptStatusEnum.class,v.getDetailReceiptStatus()));
            v.setPrintStatus(v.getIsPrint()?"已打印":"未打印");
            v.setSupplierName(supplierSimpleDTOMap.containsKey(v.getSupplierId())?supplierSimpleDTOMap.get(v.getSupplierId()).getName():"");
            WarehouseReceiveEntity warehouseReceiveEntity = warehouseReceiveEntityList.stream().filter(t->t.getSourceId().equals(v.getId())).findFirst().orElse(new WarehouseReceiveEntity());
            v.setReceiveUserName(warehouseReceiveEntity.getReceiveUserName());
            QcInfoDTO.QcReceiveResultDTO qcReceiveResultDTO = qcReceiveResultDTOList.stream().filter(t->t.getPurchaseDetailId().equals(v.getPurchaseDetailId()) && t.getReceiveCode().equals(v.getReceiveCode())).findFirst().orElse(null);
            if(Objects.nonNull(qcReceiveResultDTO)){
                v.setQcGoodQty(qcReceiveResultDTO.getQcGoodQty());
            }
        });
        return list;
    }

    @Override
    public List<DeliveryOrderDTO.GenerateReceiveListDTO> listGenerateReceive(BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        if(CollectionUtils.isEmpty(ids)){
            return new ArrayList<>();
        }
        List<DeliveryOrderDTO.GenerateReceiveListDTO> receiveListDTOList = baseMapper.listGenerateReceive(ids);
        //过滤掉明细收货状态不是空的
        receiveListDTOList = receiveListDTOList.stream().filter(v->StringUtils.isEmpty(v.getDetailReceiptStatus())).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(receiveListDTOList)){
            Map<String, SupplierDTO.SupplierSimpleDTO> supplierSimpleDTOMap = supplierFeign.getSupplierSimpleInfo(receiveListDTOList.stream().map(DeliveryOrderDTO.GenerateReceiveListDTO::getSupplierId).distinct().collect(Collectors.toList()));
            receiveListDTOList.forEach(v->{
                v.setSupplierName(supplierSimpleDTOMap.containsKey(v.getSupplierId())?supplierSimpleDTOMap.get(v.getSupplierId()).getName():"");
            });
        }
        return receiveListDTOList;
    }

    @Override
    public DeliveryOrderDTO.TotalInfo pagingTotal(DeliveryOrderDTO.ParamDTO dto) {
        List<DeliveryOrderDTO.TotalDetail> totalDetailList = this.baseMapper.pagingTotal(dto);
        List<String> purchaseIds = totalDetailList.stream().map(DeliveryOrderDTO.TotalDetail::getPurchaseId).distinct().collect(Collectors.toList());
        //查询采购签收信息
        List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> receiveList = wmsTaskFeign.getReceiveListByPurchaseOrderIdsAll(purchaseIds);
        for (DeliveryOrderDTO.TotalDetail totalDetail : totalDetailList) {
            Integer receiveQty = 0;
            Integer giftReceiveQty = 0;
            //收货数量
            if (CollectionUtils.isNotEmpty(receiveList)) {
                receiveQty = receiveList.stream().filter(e -> e.getSourceDetailId().equals(totalDetail.getDetailId()) )
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                giftReceiveQty = receiveList.stream().filter(e -> e.getSourceDetailId().equals(totalDetail.getDetailId()) )
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getGiftReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            totalDetail.setReceiveQty(receiveQty);
            totalDetail.setGiftReceiveQty(giftReceiveQty);
        }
        return DeliveryOrderDTO.TotalInfo.builder()
                .totalDeliveryQty(totalDetailList.stream().mapToInt(DeliveryOrderDTO.TotalDetail::getDeliveryQty).sum())
                .totalOrderQty(totalDetailList.stream().mapToInt(DeliveryOrderDTO.TotalDetail::getOrderQty).sum())
                .totalGiftQty(totalDetailList.stream().mapToInt(DeliveryOrderDTO.TotalDetail::getGiftQty).sum())
                .totalReceiveQty(totalDetailList.stream().mapToInt(DeliveryOrderDTO.TotalDetail::getReceiveQty).sum())
                .totalGiftReceiveQty(totalDetailList.stream().mapToInt(DeliveryOrderDTO.TotalDetail::getGiftReceiveQty).sum())
                .build();
    }

    @Override
    public List<BatchResultDTO> addDeliveryOrder(List<DeliveryOrderDTO.AddDeliveryDTO> addDeliveryDTOS) {
        //根据采购订单进行分组
        if (CollectionUtils.isEmpty(addDeliveryDTOS)){
            return Collections.emptyList();
        }
        List<BatchResultDTO> dtos = new ArrayList<>();
        Map<String, List<DeliveryOrderDTO.AddDeliveryDTO>> purchaseMap = addDeliveryDTOS.stream().collect(Collectors.groupingBy(DeliveryOrderDTO.AddDeliveryDTO::getId));
        Set<String> orderIds = addDeliveryDTOS.stream().map(DeliveryOrderDTO.AddDeliveryDTO::getId).collect(Collectors.toSet());
        List<PurchaseOrderEntity> purchaseOrderEntityList = purchaseOrderFeign.getPurchaseOrderByIds(orderIds);
        //订单数据为空直接返回
        if (CollectionUtils.isEmpty(purchaseOrderEntityList)){
            dtos.add(BatchResultDTO.fail(String.join(",",orderIds),"",ApiError.ERROR_98025.msg));
            return dtos;
        }
        List<String> detailIds = addDeliveryDTOS.stream().map(DeliveryOrderDTO.AddDeliveryDTO::getPurchaseDetailId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> purchaseOrderDetailList = purchaseOrderFeign.getPurchaseOrderDetailByIds(detailIds);
        //送货信息
        List<DeliveryOrderDetailDTO.ListDTO> deliveryOrderDetailList = detailService.listDetailDTOByDetailSourceIds(detailIds);
        //查询采购签收信息
        List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> receiveList = wmsTaskFeign.getReceiveListByPurchaseOrderIds(new ArrayList<>(orderIds));
        //入库信息
        List<PoInstockDetailEntity> stockInDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(detailIds);
        //退货信息
        List<PoReturnDetailEntity> returnOrderDetailList = wmsTaskFeign.listReturnOrderDetailByPodIds(detailIds);

        if (CollectionUtils.isEmpty(purchaseOrderEntityList)){
            dtos.add(BatchResultDTO.fail(String.join(",",orderIds),"",ApiError.ERROR_98026.msg));
            return dtos;
        }
        for (Map.Entry<String, List<DeliveryOrderDTO.AddDeliveryDTO>> entry  :purchaseMap.entrySet()) {
            String orderId = entry.getKey();
            //明细记录
            List<DeliveryOrderDTO.AddDeliveryDTO> deliveryDTOS = entry.getValue();
            PurchaseOrderEntity purchaseOrderEntity = purchaseOrderEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(orderId)).findFirst().orElse(null);
            if (Objects.isNull(purchaseOrderEntity)){
                dtos.add(BatchResultDTO.fail(orderId,"",ApiError.ERROR_98025.msg));
                continue;
            }
            try {
                DeliveryOrderEntity deliveryOrderEntity = builderDeliveryOrder(purchaseOrderEntity, deliveryDTOS);
                //处理明细列表
                handleDeliverOrderDetailList(deliveryOrderEntity.getId(), deliveryDTOS, purchaseOrderDetailList, dtos,
                        deliveryOrderDetailList,receiveList,stockInDetailList,returnOrderDetailList);
            }catch (Exception e){
                dtos.add(BatchResultDTO.fail(orderId,purchaseOrderEntity.getCode(),e.getMessage()));
            }
        }
        return dtos;
    }

    /**
     * 校验待发货明细
     *
     * @param mainId                  主表id
     * @param deliveryDTOS            新增或更新主表明细时，以订单维度锁表，这时不允许修改明细（在变更明细表记录时，增加订单维度数据幂等）
     * @param deliveryOrderDetailList
     * @param receiveList
     * @param stockInDetailList
     * @param returnOrderDetailList
     */
    @DataIdempotent(keyIdName = "deliveryOrderEntity.id")
    private void handleDeliverOrderDetailList(String mainId, List<DeliveryOrderDTO.AddDeliveryDTO> deliveryDTOS,
                                              List<PurchaseOrderDetailEntity> purchaseOrderDetailList, List<BatchResultDTO> dtos,
                                              List<DeliveryOrderDetailDTO.ListDTO> deliveryOrderDetailList, List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> receiveList,
                                              List<PoInstockDetailEntity> stockInDetailList, List<PoReturnDetailEntity> returnOrderDetailList) {
        if (CollectionUtils.isEmpty(deliveryDTOS)){
            return;
        }
        //循环增加明细
        for (DeliveryOrderDTO.AddDeliveryDTO addDeliveryDTO : deliveryDTOS) {
            try {
                PurchaseOrderDetailEntity detailEntity = purchaseOrderDetailList.stream().filter(e -> e.getId().equalsIgnoreCase(addDeliveryDTO.getPurchaseDetailId())).findFirst().orElse(null);
                if(StringUtils.isEmpty(addDeliveryDTO.getSupplierId())){
                    addDeliveryDTO.setSupplierId(userService.getSupplierId());
                }
                //订单明细校验
                checkPurchaseOrderDetail(addDeliveryDTO,detailEntity,deliveryOrderDetailList, receiveList, stockInDetailList, returnOrderDetailList);
                //发货单明细新增
                addDeliveryOrderDetail(mainId,addDeliveryDTO, detailEntity);
                dtos.add(BatchResultDTO.success(mainId,addDeliveryDTO.getSkuNo(), OperationTypeEnum.SUBMIT));
            }catch (Exception e){
                dtos.add(BatchResultDTO.fail(mainId,addDeliveryDTO.getSkuNo(),e.getMessage()));
            }
        }
    }

    /**
     * 根据送货单主单和发货明细新增记录
     * @param mainId
     * @param addDeliveryDTO
     */
    private void addDeliveryOrderDetail(String mainId, DeliveryOrderDTO.AddDeliveryDTO addDeliveryDTO, PurchaseOrderDetailEntity detailEntity) {
        DeliveryOrderDetailEntity deliveryOrderDetailEntity = DeliveryOrderConverter.INSTANCE
                .purchaseOrderDetailToDeliveryOrderDetail(mainId,addDeliveryDTO, detailEntity);
        detailService.save(deliveryOrderDetailEntity);
        String msg = StrUtil.format("用户【{}】新增sku为【{}】的送货单明细 ", UserContext.getDefaultLoginUser().getUserName(),deliveryOrderDetailEntity.getSkuNo());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_ORDER.getCode(), deliveryOrderDetailEntity.getId(), "新增操作");
    }

    /**
     * 校验数量不可超过【待交货量】
     *
     * @param addDeliveryDTO
     * @param detailEntity
     * @param deliveryOrderDetailList
     * @param receiveList
     * @param stockInDetailList
     * @param returnOrderDetailList
     */
    private void checkPurchaseOrderDetail(DeliveryOrderDTO.AddDeliveryDTO addDeliveryDTO, PurchaseOrderDetailEntity detailEntity,
                                          List<DeliveryOrderDetailDTO.ListDTO> deliveryOrderDetailList, List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> receiveList,
                                          List<PoInstockDetailEntity> stockInDetailList, List<PoReturnDetailEntity> returnOrderDetailList) {
        //没有采购明细记录
        if (Objects.isNull(detailEntity)){
            throw new ServiceException(ApiError.ERROR_98026);
        }
        //已收货数量
        Integer receiveQty;
        //已送货数量
        Integer deliveryQty = MathUtil.ZERO;
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
        //订单总数
        Integer orderQty = detailEntity.getPurchaseQty();
        //收货数量
        if (CollectionUtils.isNotEmpty(receiveList)) {
            receiveQty = receiveList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(addDeliveryDTO.getPurchaseDetailId()) )
                    .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            hasDeliveryReceiveQty = receiveList.stream().filter(e ->
                            org.apache.commons.lang3.StringUtils.isNotEmpty(e.getSourceType())
                                    && e.getSourceType().equalsIgnoreCase(SourceTypeEnum.DELIVERY_ORDER.getCode())
                                    && e.getPurchaseOrderDetailId().equals(addDeliveryDTO.getPurchaseDetailId()))
                    .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            unDeliveryReceiveQty = receiveList.stream().filter(e -> StringUtils.isEmpty(e.getSourceId())
                            && e.getPurchaseOrderDetailId().equals(addDeliveryDTO.getPurchaseDetailId()))
                    .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
        } else {
            receiveQty = MathUtil.ZERO;
        }
        //无收货单的入库数量
        if (CollectionUtils.isNotEmpty(stockInDetailList)){
            // 采购入库单（无收货单），只有审核通过的才占用库存数量
            unReceiveInstockQty = stockInDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(addDeliveryDTO.getPurchaseDetailId())
                            && Objects.equals(e.getSourceDetailId(), addDeliveryDTO.getPurchaseDetailId())
                            && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()) )
                    .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);

        }
        // 退货单（退货补货的才会导致在途数量变化）
        if (CollectionUtils.isNotEmpty(returnOrderDetailList)){
            returnQty = returnOrderDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(addDeliveryDTO.getPurchaseDetailId())
                            && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())
                            && StrUtils.isNotEmpty(e.getPurchaseOrderDetailId())
                            && Objects.equals(e.getReturnMode(), ReturnModeEnum.REPLENISHMENT.getCode()))
                    .map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
        }
        //已送货数量
        if (CollectionUtils.isNotEmpty(deliveryOrderDetailList)) {
            deliveryQty = deliveryOrderDetailList.stream().filter(e -> e.getSourceDetailId().equals(addDeliveryDTO.getPurchaseDetailId()) )
                    .map(DeliveryOrderDetailDTO.ListDTO::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            //收发差异
//            diffSendAndReceive = deliveryOrderDetailList.stream().filter(e -> e.getSourceDetailId().equals(addDeliveryDTO.getPurchaseDetailId())
//                            && StrUtils.isNotEmpty(e.getReceiptStatus()) && e.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode()) )
//                    .map(deliveryOrderDetailEntity -> deliveryOrderDetailEntity.getDeliveryQty() - receiveQty)
//                    .reduce(MathUtil.ZERO, Integer::sum);
            //发货数量 - 已审核收货数量
            Integer srmDeliveryQty = deliveryOrderDetailList.stream().filter(e -> e.getSourceDetailId().equals(addDeliveryDTO.getPurchaseDetailId())
                            && com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(e.getReceiptStatus()) && e.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode()) )
                    .map(DeliveryOrderDetailDTO.ListDTO::getDeliveryQty)
                    .reduce(MathUtil.ZERO, Integer::sum);
            diffSendAndReceive = srmDeliveryQty - hasDeliveryReceiveQty;
        }
        //剩余送货量/可下推量=采购订单-送货单数量-无送货单收货数量-无收货单的入库数量+[收发差异]+退货补货数量[库存退货/质检退货]
        int waitDeliveryQty = orderQty - deliveryQty - unDeliveryReceiveQty - unReceiveInstockQty + diffSendAndReceive + returnQty;
        if (addDeliveryDTO.getPlanDeliveryQty() > waitDeliveryQty){
            throw new ServiceException(ApiError.ERROR_PURCHASE_DETAIL_ORDER_MORE_THEN_DELIVERY_QTY, addDeliveryDTO.getPurchaseDetailId());
        }
    }

    /**
     * 校验是否存在送货单 不存在则新增
     * @param purchaseOrderEntity
     * @param deliveryDTOS
     * @return
     */
    private DeliveryOrderEntity builderDeliveryOrder(PurchaseOrderEntity purchaseOrderEntity, List<DeliveryOrderDTO.AddDeliveryDTO> deliveryDTOS) {
        DeliveryOrderDTO.AddDeliveryDTO addDeliveryDTO = deliveryDTOS.stream().filter(Objects::nonNull).findFirst().orElse(null);
        if(StringUtils.isEmpty(addDeliveryDTO.getSupplierId())){
            addDeliveryDTO.setSupplierId(userService.getSupplierId());
        }
        DeliveryOrderEntity deliveryOrderEntity = DeliveryOrderConverter.INSTANCE.purchaseOrderToDeliveryOrder(purchaseOrderEntity,addDeliveryDTO);
        deliveryOrderEntity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SHD));
        this.handleData(deliveryOrderEntity,false);
        this.save(deliveryOrderEntity);
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "送货单", deliveryOrderEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_ORDER.getCode(), deliveryOrderEntity.getId(), "新增操作");
        return deliveryOrderEntity;
    }

    @Override
    public DeliveryOrderDTO.ViewDTO viewByCode(String code) {
        DeliveryOrderEntity  entity = lambdaQuery()
                .eq(DeliveryOrderEntity :: getCode,code)
                .last("limit 1")
                .one();
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "送货单"));
        SupplierEntity supplier = supplierFeign.getSupplierById(entity.getSupplierId());
        if(Objects.isNull(supplier)){
            throw new ServiceException(ApiError.ERROR_96001);
        }
        if(!supplier.getId().equals(entity.getSupplierId())){
            throw new ServiceException(ApiError.ERROR_96002);
        }
        List<DeliveryOrderDetailEntity> detailEntityList = detailService.listByMainId(entity.getId());
        DeliveryOrderDTO.ViewDTO viewDTO = DeliveryOrderConverter.INSTANCE.viewConvert(entity,detailEntityList);
        viewDTO.setSupplierName(supplier.getName());
        List<DeliveryOrderDetailDTO.ViewDTO> detailList = viewDTO.getDetailList();
        Map<String,SkuVO> skuVOMap = plmTaskFeign.listSkuProductByIds(detailList.stream().map(DeliveryOrderDetailDTO.ViewDTO::getSkuId).distinct().collect(Collectors.toList())).stream().collect(Collectors.toMap(SkuVO::getSkuId, Function.identity(),(v1, v2)->v1));
        List<String> purchaseDetailIds = detailList.stream().map(DeliveryOrderDetailDTO.ViewDTO::getSourceDetailId).collect(Collectors.toList());
        //查询退货数据
        List<PoReturnDetailEntity> purchaseReturnOrderDetailEntities = wmsTaskFeign.listReturnOrderDetailByPodIds(purchaseDetailIds);
        //查询收货数据
        List<WarehouseReceiveDetailEntity> receiveDetails = wmsTaskFeign.listWarehouseReceiveDetailByPodIds(purchaseDetailIds);
        for(DeliveryOrderDetailDTO.ViewDTO detailViewDTO : detailList){
            SkuVO skuVO = skuVOMap.get(detailViewDTO.getSkuId());
            if(Objects.nonNull(skuVO)){
                detailViewDTO.setUnitName(skuVO.getUnitName());
            }

            Integer receiveQty = MathUtil.ZERO;
            if (CollectionUtils.isNotEmpty(receiveDetails)) {
                receiveQty = receiveDetails.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(detailViewDTO.getSourceDetailId())).map(WarehouseReceiveDetailEntity::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }

            Integer returnQty = purchaseReturnOrderDetailEntities.stream().filter(obj -> obj.getPurchaseOrderDetailId().equals(detailViewDTO.getSourceDetailId()) && obj.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getStatus()) && obj.getReturnMode().equals(ReturnModeEnum.REPLENISHMENT.getCode())).map(PoReturnDetailEntity::getReplenishQty).reduce(MathUtil.ZERO, Integer::sum);
            //未收货数量
            detailViewDTO.setUnReceiveQty(detailViewDTO.getOrderQty() + returnQty - receiveQty);
        }
        return viewDTO;
    }

    @Override
    public List<DeliveryOrderDTO.WaitDeliveryCountDTO> buildSrmWaitDeliveryCount() {
        WaitDeliveryCycleEnum[] values = WaitDeliveryCycleEnum.values();
        List<DeliveryOrderDTO.WaitDeliveryCountDTO> dtos = new ArrayList<>(values.length);
        String supplierId = userService.getSupplierId();
        for (WaitDeliveryCycleEnum item : values) {
            dtos.add(new DeliveryOrderDTO.WaitDeliveryCountDTO(item.getCode(),item.getName(),purchaseOrderDetailService.srmWaitDeliveryCount(supplierId, item.getCode())));
        }
        return dtos;
    }

    @Override
    public List<DeliveryOrderDTO.WaitDeliveryCountDTO> buildSrmWaitDeliveryCount(PurchaseOrderSrmDTO.WaitDeliveryCountDTO waitDeliveryCountDTO) {
        List<DeliveryOrderDTO.WaitDeliveryCountDTO> dtos = new ArrayList<>();
        dtos.add(new DeliveryOrderDTO.WaitDeliveryCountDTO(WaitDeliveryCycleEnum.EXPIRED.getCode(),WaitDeliveryCycleEnum.EXPIRED.getName(),waitDeliveryCountDTO.getExpiredCount()));
        dtos.add(new DeliveryOrderDTO.WaitDeliveryCountDTO(WaitDeliveryCycleEnum.ALMOST_OVERDUE.getCode(),WaitDeliveryCycleEnum.ALMOST_OVERDUE.getName(),waitDeliveryCountDTO.getAlmostOverdueCount()));
        dtos.add(new DeliveryOrderDTO.WaitDeliveryCountDTO(WaitDeliveryCycleEnum.IN_ONE_MONTH.getCode(),WaitDeliveryCycleEnum.IN_ONE_MONTH.getName(),waitDeliveryCountDTO.getInOneMonthCount()));
        dtos.add(new DeliveryOrderDTO.WaitDeliveryCountDTO(WaitDeliveryCycleEnum.IN_TWO_MONTH.getCode(),WaitDeliveryCycleEnum.IN_TWO_MONTH.getName(),waitDeliveryCountDTO.getInTwoMonthCount()));
        dtos.add(new DeliveryOrderDTO.WaitDeliveryCountDTO(WaitDeliveryCycleEnum.TWO_MONTH_LATER.getCode(),WaitDeliveryCycleEnum.TWO_MONTH_LATER.getName(),waitDeliveryCountDTO.getTwoMonthLaterCount()));
        return dtos;
    }

    @Override
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) {
        DeliveryExcelListener excelListenerUtil = new DeliveryExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), DeliveryOrderImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("SRM送货单导入错误！>>>>{}", e);
            return Boolean.FALSE;
        }
        List<DeliveryOrderImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        if (!errorList.isEmpty()) {
            String fileName = "SRM送货单错误信息";
            ExcelUtil.export(fileName, "error", errorList, DeliveryOrderImportExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveImport(List<Pair<DeliveryOrderEntity, List<DeliveryOrderDetailEntity>>> addList) {
        if(CollectionUtils.isEmpty(addList)){
            return;
        }
        for (Pair<DeliveryOrderEntity, List<DeliveryOrderDetailEntity>> pair : addList) {
            DeliveryOrderEntity deliveryOrderEntity = pair.getKey();
            List<DeliveryOrderDetailEntity> detailEntityList = pair.getValue();
            deliveryOrderEntity.setCode(docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SHD));
            this.handleData(deliveryOrderEntity,false);
            this.save(deliveryOrderEntity);
            detailEntityList.forEach(v->v.setMainId(deliveryOrderEntity.getId()));
            detailService.saveBatch(detailEntityList);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DeliveryOrderDTO.AddDTO addDTO) {
        DeliveryOrderEntity deliveryOrderEntity = new DeliveryOrderEntity();
        BeanMapperUtils.copy(addDTO, deliveryOrderEntity);

        // 数据处理
        handleData(deliveryOrderEntity,false);

        log.info("开始新增送货单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_SHD);
        deliveryOrderEntity.setCode(code);
        boolean save = super.save(deliveryOrderEntity);
        if (!save) {
            throw new ServiceException("送货单保存失败");
        }
        //保存明细
        detailService.add(addDTO.getDetailList(),deliveryOrderEntity.getId());
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "送货单", deliveryOrderEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.DELIVERY_ORDER.getCode(), deliveryOrderEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(deliveryOrderEntity.getId(), code);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DeliveryOrderDTO.UpdateDTO updateDTO) {
        DeliveryOrderEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "送货单"));
        DeliveryOrderEntity deliveryOrderEntity = BeanMapperUtils.map(DeliveryOrderEntity.class, updateDTO);
        // 数据处理
        handleData(deliveryOrderEntity,true);
        log.info("编辑 开始修改送货单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(deliveryOrderEntity);
        if (!save) {
            throw new ServiceException("送货单保存失败");
        }
        //更新明细
        if(!detailService.update(updateDTO.getDetailList(),deliveryOrderEntity.getId())){
            throw new ServiceException("送货单明细保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录送货单日志数据，单号：【{}】", deliveryOrderEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getCode(), "送货单");
        operateLogService.addModuleOperateLogByObj(old, deliveryOrderEntity, ModuleTypeEnum.DELIVERY_ORDER.getCode(), deliveryOrderEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public Integer countByPrint(String supplierId, boolean isPrint) {
        LambdaQueryWrapper<DeliveryOrderEntity> queryWrapper = this.getDefaultWrapper(supplierId);
        queryWrapper.eq(DeliveryOrderEntity::getIsPrint, isPrint);
        return this.count(queryWrapper);
    }

    @Override
    public Integer countByReceiveStatus(String supplierId, String status) {
        LambdaQueryWrapper<DeliveryOrderEntity> queryWrapper = this.getDefaultWrapper(supplierId);
        queryWrapper.eq(DeliveryOrderEntity::getReceiptStatus, status);
        return this.count(queryWrapper);
    }

    private LambdaQueryWrapper<DeliveryOrderEntity> getDefaultWrapper(String supplierId) {
        return new LambdaQueryWrapper<DeliveryOrderEntity>()
                .eq(DeliveryOrderEntity::getSupplierId, supplierId);
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(DeliveryOrderEntity deliveryOrderEntity,Boolean isUpdate) {
        if(!isUpdate){
            List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(deliveryOrderEntity.getToWarehouseId()));
            if(CollectionUtils.isEmpty(warehouseList)){
                throw new ServiceException("仓库信息为空");
            }
            WarehouseDTO.UpdateDTO warehouseInfo = warehouseList.get(0);
            deliveryOrderEntity.setReceiveUserId(warehouseInfo.getChargeId());
            deliveryOrderEntity.setReceiveUserName(warehouseInfo.getContacts());
            deliveryOrderEntity.setReceivePhone(warehouseInfo.getContactTelNumber());
            deliveryOrderEntity.setReceiveAddress(warehouseInfo.getAddress());
        }
    }

    /**
     * @description: 添加对账明细
     * @author Will
     * @date: 2024/1/26 9:24
     * @param idList
     */
    private void addPoReconciliationDetail (List<String> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        //送货单
        List<DeliveryOrderEntity> deliveryOrderList = this.listByIds(idList);

        //送货明细
        List<String> deliveryOrderIdList = deliveryOrderList.stream().map(DeliveryOrderEntity::getId).distinct().collect(Collectors.toList());
        List<DeliveryOrderDetailEntity> deliveryOrderDetailList = detailService.listByMainIdList(deliveryOrderIdList);

        //采购订单
        List<String> sourceIdList = deliveryOrderList.stream().map(DeliveryOrderEntity::getSourceId).collect(Collectors.toList());
        List<PurchaseOrderEntity> purchaseOrderList = scmTaskFeign.listPurchaseOrderByIds(sourceIdList);

        //添加信息
        List<PoReconciliationDetailDTO.AddDTO> addList = new ArrayList<>();
        for (DeliveryOrderDetailEntity detailEntity :deliveryOrderDetailList) {
            PoReconciliationDetailDTO.AddDTO addDTO = new PoReconciliationDetailDTO.AddDTO();
            //送货单主表
            DeliveryOrderEntity deliveryOrderEntity = deliveryOrderList.stream().filter(obj -> StrUtil.equals(obj.getId(), detailEntity.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(deliveryOrderEntity)) {
                throw new ServiceException(ApiError.ERROR_DELIVERY_ORDER_NOT_EXIST);
            }

            addDTO.setPoId(deliveryOrderEntity.getSourceId());
            addDTO.setPoCode(deliveryOrderEntity.getSourceCode());
            addDTO.setPoDetailId(detailEntity.getSourceDetailId());
            addDTO.setSupplierId(deliveryOrderEntity.getSupplierId());
            addDTO.setSourceId(deliveryOrderEntity.getId());
            addDTO.setSourceDetailId(detailEntity.getId());
            addDTO.setSourceCode(deliveryOrderEntity.getCode());
            addDTO.setSourceType(SourceTypeEnum.DELIVERY_ORDER.getCode());
            addDTO.setConfirmDate(detailEntity.getConfirmReceiveDate());
            addDTO.setSkuId(detailEntity.getSkuId());
            addDTO.setDeliveryQty(detailEntity.getDeliveryQty());
            addDTO.setReceiveQty(detailEntity.getReceiveQty());
            addDTO.setBusinessStatus(PoReturnConfirmStatusEnum.CONFIRM.getCode());

            //采购订单
            PurchaseOrderEntity purchaseOrderEntity = purchaseOrderList.stream().filter(obj -> StrUtil.equals(deliveryOrderEntity.getSourceId(), obj.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(purchaseOrderEntity)) {
                throw new ServiceException(ApiError.ERROR_98025);
            }
            addDTO.setSettleOrgId(purchaseOrderEntity.getPurchaseOrgId());

            addList.add(addDTO);
        }
        poReconciliationDetailScmService.add(addList);
    }

    /**
     * @description: 添加对账明细
     * @author Will
     * @date: 2024/1/26 9:24
     * @param idList
     */
    @Override
    public void removePoReconciliationDetail (List<String> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return;
        }
        //送货单
        List<DeliveryOrderEntity> deliveryOrderList = this.listByIds(idList);
        if (CollectionUtils.isEmpty(deliveryOrderList)) {
            throw new ServiceException(ApiError.ERROR_DELIVERY_ORDER_NOT_EXIST);
        }
        //送货明细
        List<String> deliveryOrderIdList = deliveryOrderList.stream().map(DeliveryOrderEntity::getId).distinct().collect(Collectors.toList());
        List<DeliveryOrderDetailEntity> deliveryOrderDetailList = detailService.listByMainIdList(deliveryOrderIdList);
        if (CollectionUtils.isEmpty(deliveryOrderDetailList)) {
            throw new ServiceException(ApiError.ERROR_DELIVERY_ORDER_DETAIL_NOT_EXIST);
        }
        List<String> detailIdList = deliveryOrderDetailList.stream().map(DeliveryOrderDetailEntity::getId).collect(Collectors.toList());
        poReconciliationDetailScmService.deleteDetailBySourceDetailIdList(detailIdList,true);
    }

    @Override
    public PagingVO<DeliveryOrderExportExcelDTO> exportSupplierDeliveryOrder(PagingDTO<DeliveryOrderDTO.ParamDTO> dto) {
        Page<DeliveryOrderExportExcelDTO> page = this.baseMapper.getExportList(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        Map<String, SupplierDTO.SupplierSimpleDTO> supplierSimpleDTOMap = supplierFeign.getSupplierSimpleInfo(page.getRecords().stream().map(DeliveryOrderExportExcelDTO::getSupplierId).distinct().collect(Collectors.toList()));
        List<String> purchaseDetailIds = page.getRecords().stream().filter(v->StringUtils.isNotBlank(v.getReceiveCode())).map(DeliveryOrderExportExcelDTO::getPurchaseDetailId).distinct().collect(Collectors.toList());
        List<QcInfoDTO.QcReceiveResultDTO> qcReceiveResultDTOList = wmsTaskFeign.getQcReceiveResult(purchaseDetailIds);
        WarehouseReceiveDTO.SourceParamDTO sourceParamDTO = new WarehouseReceiveDTO.SourceParamDTO();
        sourceParamDTO.setSourceIds(page.getRecords().stream().map(DeliveryOrderExportExcelDTO::getId).distinct().collect(Collectors.toList()));
        sourceParamDTO.setSourceType(PoReceiveSourceTypeEnum.DELIVERY_ORDER.getCode());
        List<WarehouseReceiveEntity> warehouseReceiveEntityList = wmsTaskFeign.listReceiveBySourceTypeAndIds(sourceParamDTO);

        List<String> purchaseIds = page.getRecords().stream().map(DeliveryOrderExportExcelDTO::getSourceId).distinct().collect(Collectors.toList());
        //查询采购签收信息
        List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> receiveList = wmsTaskFeign.getReceiveListByPurchaseOrderIdsAll(purchaseIds);

        page.getRecords().forEach(v->{
            Integer receiveQty = 0;
            Integer giftReceiveQty = 0;
            //收货数量
            if (CollectionUtils.isNotEmpty(receiveList)) {
                receiveQty = receiveList.stream().filter(e -> e.getSourceDetailId().equals(v.getDetailId()) )
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                giftReceiveQty = receiveList.stream().filter(e -> e.getSourceDetailId().equals(v.getDetailId()) )
                        .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getGiftReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
            }
            v.setReceiveQty(receiveQty);
            v.setGiftReceiveQty(giftReceiveQty);

            v.setReceiptStatus(EnumMessage.getNameByCode(DeliveryOrderEnum.ReceiptStatusEnum.class,v.getReceiptStatus()));
            v.setDetailReceiptStatus(EnumMessage.getNameByCode(DeliveryOrderEnum.ReceiptStatusEnum.class,v.getDetailReceiptStatus()));
            v.setPrintStatus(v.getIsPrint()?"已打印":"未打印");
            v.setSupplierName(supplierSimpleDTOMap.containsKey(v.getSupplierId())?supplierSimpleDTOMap.get(v.getSupplierId()).getName():"");
            WarehouseReceiveEntity warehouseReceiveEntity = warehouseReceiveEntityList.stream().filter(t->t.getSourceId().equals(v.getId())).findFirst().orElse(new WarehouseReceiveEntity());
            v.setReceiveUserName(warehouseReceiveEntity.getReceiveUserName());
            QcInfoDTO.QcReceiveResultDTO qcReceiveResultDTO = qcReceiveResultDTOList.stream().filter(t->t.getPurchaseDetailId().equals(v.getPurchaseDetailId()) && t.getReceiveCode().equals(v.getReceiveCode())).findFirst().orElse(null);
            if(Objects.nonNull(qcReceiveResultDTO)){
                v.setQcGoodQty(qcReceiveResultDTO.getQcGoodQty());
            }
        });
        return new PagingVO<>(page);
    }

    /**
     * 更新主表状态
     * @param ids
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateReceiveStatus(List<String> ids){
        List<DeliveryOrderEntity> deliveryOrderEntityList = this.listByIds(ids);
        if(CollectionUtils.isEmpty(deliveryOrderEntityList)){
            return false;
        }
        List<DeliveryOrderDetailEntity> allDetailEntityList = detailService.listByMainIdList(ids);
        if(CollectionUtils.isEmpty(allDetailEntityList)){
            return false;
        }
        for (DeliveryOrderEntity orderEntity : deliveryOrderEntityList) {
            List<DeliveryOrderDetailEntity> detailEntityList = allDetailEntityList.stream().filter(v->v.getMainId().equals(orderEntity.getId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(detailEntityList)){
                continue;
            }
            if(detailEntityList.stream().allMatch(v->StringUtils.isBlank(v.getReceiptStatus()))){
                orderEntity.setReceiptStatus("");
            }else if(detailEntityList.stream().allMatch(v->DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode().equals(v.getReceiptStatus()))){
                orderEntity.setReceiptStatus(DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode());
            }else if(detailEntityList.stream().allMatch(v->DeliveryOrderEnum.ReceiptStatusEnum.WAIT_CONFIRMED.getCode().equals(v.getReceiptStatus()))){
                orderEntity.setReceiptStatus(DeliveryOrderEnum.ReceiptStatusEnum.WAIT_CONFIRMED.getCode());
            }else if(detailEntityList.stream().anyMatch(v->DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode().equals(v.getReceiptStatus()))){
                orderEntity.setReceiptStatus(DeliveryOrderEnum.ReceiptStatusEnum.PART_CONFIRMED.getCode());
            }else if(detailEntityList.stream().anyMatch(v->DeliveryOrderEnum.ReceiptStatusEnum.WAIT_CONFIRMED.getCode().equals(v.getReceiptStatus()))){
                orderEntity.setReceiptStatus(DeliveryOrderEnum.ReceiptStatusEnum.WAIT_CONFIRMED.getCode());
            }
        }
        List<String> confirmIds = deliveryOrderEntityList.stream().filter(v->DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode().equals(v.getReceiptStatus())).map(BaseEntity::getId).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(confirmIds)){
            this.addPoReconciliationDetail(confirmIds);
        }
        return this.updateBatchById(deliveryOrderEntityList);
    }
}
