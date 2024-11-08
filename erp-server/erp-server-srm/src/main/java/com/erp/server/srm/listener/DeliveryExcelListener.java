package com.erp.server.srm.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.entity.BaseEntity;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.model.srm.dto.DeliveryOrderDetailDTO;
import com.erp.model.srm.dto.excel.DeliveryOrderImportExcelDTO;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.model.srm.entity.DeliveryOrderEntity;
import com.erp.model.srm.enums.DeliveryOrderEnum;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.PoInstockDetailEntity;
import com.erp.model.wms.entity.PoReturnDetailEntity;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.srm.convert.DeliveryOrderConverter;
import com.erp.server.srm.service.DeliveryOrderDetailService;
import com.erp.server.srm.service.DeliveryOrderService;
import com.erp.server.srm.service.UserService;
import jnr.ffi.annotations.In;
import org.apache.commons.math3.util.Pair;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author liuruipeng
 * @date 2024年04月10日 9:50
 */
public class DeliveryExcelListener extends AnalysisEventListener<DeliveryOrderImportExcelDTO> {

    /**
     * 错误信息
     */
    @Getter
    private List<DeliveryOrderImportExcelDTO> errorList = new ArrayList<>();

    /**
     * 全部数据
     */
    @Getter
    private List<DeliveryOrderImportExcelDTO> dataList = new ArrayList<>();

    private List<Pair<DeliveryOrderEntity,List<DeliveryOrderDetailEntity>>> addList = new ArrayList<>();

    private ScmTaskFeign scmTaskFeign = SpringUtil.getBean(ScmTaskFeign.class);

    private WmsTaskFeign wmsTaskFeign = SpringUtil.getBean(WmsTaskFeign.class);

    private UserService userService = SpringUtil.getBean(UserService.class);;

    private DeliveryOrderDetailService deliveryOrderDetailService = SpringUtil.getBean(DeliveryOrderDetailService.class);

    private DeliveryOrderService deliveryOrderService = SpringUtil.getBean(DeliveryOrderService.class);

    @Override
    public void invoke(DeliveryOrderImportExcelDTO data, AnalysisContext context) {
        //注解验证信息
        List<String> errorMsgList = FieldValidUtil.fieldValid(data);
        //存在错误数据则直接返回
        if (!errorMsgList.isEmpty()) {
            data.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(data);
            return;
        }
        BigInteger bigDeliveryQty = new BigInteger(data.getDeliveryQtyStr());
        BigInteger bigGiftQty = new BigInteger(data.getGiftQtyStr());
        if(bigDeliveryQty.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0){
            data.setErrorMsg("送货数量过大");
            errorList.add(data);
            return;
        }
        if(bigGiftQty.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0){
            data.setErrorMsg("赠品数量过大");
            errorList.add(data);
            return;
        }
        data.setDeliveryQty(Integer.valueOf(data.getDeliveryQtyStr()));
        data.setGiftQty(Integer.valueOf(data.getGiftQtyStr()));
        if(data.getGiftQty() < 0){
            data.setErrorMsg("赠品数量不能小于0");
            errorList.add(data);
            return;
        }
        dataList.add(data);
    }

    public static void main(String[] args) {
        System.out.println(Integer.MAX_VALUE);
        System.out.println(new BigInteger("15235656656565600"));
    }

    /**
     * 不管导入的数据同个订单号相同SKU有多少行，送货单明细需要小于等于采购明细的行数
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if(CollectionUtils.isEmpty(dataList)){
            return;
        }
        //数据准备
        List<String> sourceCodes = dataList.stream().map(DeliveryOrderImportExcelDTO::getSourceCode).distinct().collect(Collectors.toList());
        List<PurchaseOrderEntity> allPurchaseOrderList = scmTaskFeign.getPurchaseOrderByCodes(sourceCodes);
        List<String> allPurchaseIds = allPurchaseOrderList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<PurchaseOrderDetailEntity> allPurchaseDetailList = scmTaskFeign.listByPurchaseOrderIds(allPurchaseIds);
        List<String> allPurchaseDetailIds = allPurchaseDetailList.stream().map(PurchaseOrderDetailEntity::getId).collect(Collectors.toList());
        //送货信息
        List<DeliveryOrderDetailDTO.ListDTO> deliveryOrderDetailList = deliveryOrderDetailService.listDetailDTOByDetailSourceIds(allPurchaseDetailIds);
        //入库信息
        List<PoInstockDetailEntity> stockInDetailList = wmsTaskFeign.listPurchaseStockInDetailByPodIds(allPurchaseDetailIds);
        //退货信息
        List<PoReturnDetailEntity> returnOrderDetailList = wmsTaskFeign.listReturnOrderDetailByPodIds(allPurchaseDetailIds);

        List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> receiveList = wmsTaskFeign.getReceiveListByPurchaseOrderIds(allPurchaseIds);

        List<WarehouseReceiveDTO.PurchaseOrderDetailDTO> receiveAllList = wmsTaskFeign.getReceiveListByPurchaseOrderIdsAll(allPurchaseIds);

        Map<String,List<DeliveryOrderImportExcelDTO>> map = dataList.stream().collect(Collectors.groupingBy(DeliveryOrderImportExcelDTO::getSourceCode));
        map.forEach((key,value)->{
            PurchaseOrderEntity purchaseOrderEntity = allPurchaseOrderList.stream().filter(v -> v.getCode().equals(key)).findFirst().orElse(null);
            //校验采购单存在
            if(Objects.isNull(purchaseOrderEntity)){
                value.forEach(v->{
                    v.setErrorMsg("采购单不存在");
                    errorList.add(v);
                });
                return;
            }
            List<PurchaseOrderDetailEntity> purchaseOrderDetailEntityList = allPurchaseDetailList.stream().filter(v->v.getPurchaseOrderId().equals(purchaseOrderEntity.getId())).collect(Collectors.toList());

            //校验SKU存在
            AtomicBoolean isSkuExist = new AtomicBoolean(true);
            value.forEach(v->{
                PurchaseOrderDetailEntity purchaseOrderDetailEntity = purchaseOrderDetailEntityList.stream().filter(v1 -> v1.getSkuNo().equals(v.getSkuNo())).findFirst().orElse(null);
                if(Objects.isNull(purchaseOrderDetailEntity)){
                    v.setErrorMsg("sku不存在");
                    errorList.add(v);
                    isSkuExist.set(false);
                }
            });
            if(!isSkuExist.get()){
                return;
            }

            //采购明细送货数量map key:明细id，value:可送货数量
            Map<String,Integer> remainQtyMap = new HashMap<>();

            //采购明细汇总可送货数量map key:sku，value:可送货数量
            Map<String,Integer> remainSummaryQtyMap = new HashMap<>();

            for (PurchaseOrderDetailEntity purchaseOrderDetailEntity : purchaseOrderDetailEntityList) {
                //已送货数量
                Integer deliveryQty = MathUtil.ZERO;
                //无送货单收货数量
                Integer unDeliveryReceiveQty = MathUtil.ZERO;
                //无收货单的入库数量
                Integer unReceiveInstockQty = MathUtil.ZERO;
                //收发差异
                Integer diffSendAndReceive = MathUtil.ZERO;
                //退货补货数量
                Integer returnQty = MathUtil.ZERO;
                //无送货单收货数量
                Integer receiveQty;
                //有送货单的收货数量
                Integer hasDeliveryReceiveQty = MathUtil.ZERO;
                //收货数量(已审核的)
                if (CollectionUtils.isNotEmpty(receiveList)) {
                    unDeliveryReceiveQty = receiveList.stream().filter(e -> StringUtils.isEmpty(e.getSourceId()) && e.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId()))
                            .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                }
                //收货数量(全部)
                if (CollectionUtils.isNotEmpty(receiveAllList)) {
                    hasDeliveryReceiveQty = receiveAllList.stream().filter(e -> StringUtils.isNotEmpty(e.getSourceType())
                            && e.getSourceType().equalsIgnoreCase(SourceTypeEnum.DELIVERY_ORDER.getCode())
                                    && e.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId()))
                            .map(WarehouseReceiveDTO.PurchaseOrderDetailDTO::getReceiveQty).reduce(MathUtil.ZERO, Integer::sum);
                } else {
                    hasDeliveryReceiveQty = MathUtil.ZERO;
                }
                //无收货单的入库数量
                if (CollectionUtils.isNotEmpty(stockInDetailList)){
                    // 采购入库单（无收货单），只有审核通过的才占用库存数量
                    unReceiveInstockQty = stockInDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId())
                                    && Objects.equals(e.getSourceDetailId(), purchaseOrderDetailEntity.getId())
                                    && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus()) )
                            .map(PoInstockDetailEntity::getStockInQty).reduce(MathUtil.ZERO, Integer::sum);

                }
                // 退货单（退货补货的才会导致在途数量变化）
                if (CollectionUtils.isNotEmpty(returnOrderDetailList)){
                    returnQty = returnOrderDetailList.stream().filter(e -> e.getPurchaseOrderDetailId().equals(purchaseOrderDetailEntity.getId())
                                    && Objects.equals(e.getApproveStatus(), ApproveStatusEnum.APPROVE.getStatus())
                                    && StrUtils.isNotEmpty(e.getPurchaseOrderDetailId())
                                    && Objects.equals(e.getReturnMode(), ReturnModeEnum.REPLENISHMENT.getCode()))
                            .map(PoReturnDetailEntity::getReturnQty).reduce(MathUtil.ZERO, Integer::sum);
                }
                //已送货数量
                if (CollectionUtils.isNotEmpty(deliveryOrderDetailList)) {
                    deliveryQty = deliveryOrderDetailList.stream().filter(e -> e.getSourceDetailId().equals(purchaseOrderDetailEntity.getId()) )
                            .map(DeliveryOrderDetailDTO.ListDTO::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
                    //收发差异
//                    diffSendAndReceive = deliveryOrderDetailList.stream().filter(e -> e.getSourceDetailId().equals(purchaseOrderDetailEntity.getId())
//                                    && com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(e.getReceiptStatus())
//                                    && e.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode()) )
//                            .map(deliveryOrderDetailEntity -> deliveryOrderDetailEntity.getDeliveryQty() - receiveQty)
//                            .reduce(MathUtil.ZERO, Integer::sum);
                    //发货数量 - 已审核收货数量
                    Integer srmDeliveryQty = deliveryOrderDetailList.stream().filter(e -> e.getSourceDetailId().equals(purchaseOrderDetailEntity.getId())
                                    && com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(e.getReceiptStatus())
                                    && e.getReceiptStatus().equals(DeliveryOrderEnum.ReceiptStatusEnum.CONFIRMED.getCode()) )
                            .map(DeliveryOrderDetailDTO.ListDTO::getDeliveryQty)
                            .reduce(MathUtil.ZERO, Integer::sum);
                    diffSendAndReceive = srmDeliveryQty - hasDeliveryReceiveQty;
                }

                //剩余送货量/可下推量=采购订单-送货单数量-无送货单收货数量-无收货单的入库数量+[收发差异]+退货补货数量[库存退货/质检退货]
                Integer remainingQty = purchaseOrderDetailEntity.getPurchaseQty() - deliveryQty - unDeliveryReceiveQty - unReceiveInstockQty + diffSendAndReceive + returnQty;
                remainSummaryQtyMap.compute(purchaseOrderDetailEntity.getSkuNo(), (k, v) -> (v == null) ? remainingQty : v + remainingQty);
                remainQtyMap.put(purchaseOrderDetailEntity.getId(), remainingQty);
            }

            //校验SKU的送货数量不可大于可交货量  可交货量 =采购订单-送货单数量-无送货单收货数量-无收货单的入库数量+[收发差异]+退货补货数量[库存退货/质检退货]
            boolean isQtyValid = true;
            for (DeliveryOrderImportExcelDTO excelData : value) {
                if(excelData.getDeliveryQty() > remainSummaryQtyMap.get(excelData.getSkuNo())){
                    excelData.setErrorMsg("送货数量不可大于可交货量" +remainSummaryQtyMap.get(excelData.getSkuNo()));
                    errorList.add(excelData);
                    isQtyValid = false;
                }else{
                    remainSummaryQtyMap.put(excelData.getSkuNo(),remainSummaryQtyMap.get(excelData.getSkuNo()) - excelData.getDeliveryQty());
                }
            }
            if(!isQtyValid){
                return;
            }
            
            //走到这一步说明数据正确，可以导入
            //封装主记录
            DeliveryOrderDTO.AddDeliveryDTO addDeliveryDTO = new DeliveryOrderDTO.AddDeliveryDTO();
            addDeliveryDTO.setSupplierId(userService.getSupplierId());
            addDeliveryDTO.setExpectDeliveryDate(ObjectUtil.isEmpty(value.get(0).getPlanDeliveryDate()) ? null : LocalDate.parse(value.get(0).getPlanDeliveryDate(), DateTimeFormatter.ofPattern("yyyy/M/d")));
            DeliveryOrderEntity deliveryOrderEntity = DeliveryOrderConverter.INSTANCE.purchaseOrderToDeliveryOrder(purchaseOrderEntity,addDeliveryDTO);
            List<DeliveryOrderDetailEntity> detailEntityList = new ArrayList<>();
            //封装明细
            for (DeliveryOrderImportExcelDTO excelData : value) {
                //可能采购明细相同sku有两条 数量为10，20 导入的时候有1条，数量25，则生成两条送货单，一条10，一条15
                List<PurchaseOrderDetailEntity> sameSkuPurchasedetailList = purchaseOrderDetailEntityList.stream().filter(v->v.getSkuNo().equals(excelData.getSkuNo())).collect(Collectors.toList());
                for (PurchaseOrderDetailEntity purchaseOrderDetailEntity : sameSkuPurchasedetailList) {
                    Integer remainQty = remainQtyMap.get(purchaseOrderDetailEntity.getId());
                    if(remainQty == 0){
                        continue;
                    }
                    //如果导入的数量大于当前明细数量，循环继续，否则退出循环
                    if(excelData.getDeliveryQty() > remainQty){
                        remainQtyMap.put(purchaseOrderDetailEntity.getId(),0);
                        DeliveryOrderDetailEntity detail = DeliveryOrderConverter.INSTANCE.importConvertDeatil(purchaseOrderDetailEntity,excelData,remainQty);
                        detail.setPlanDeliveryDate(LocalDate.parse(value.get(0).getPlanDeliveryDate(), DateTimeFormatter.ofPattern("yyyy/M/d")));
                        detailEntityList.add(detail);
                    }else{
                        remainQtyMap.put(purchaseOrderDetailEntity.getId(),remainQty - excelData.getDeliveryQty());
                        DeliveryOrderDetailEntity detail = DeliveryOrderConverter.INSTANCE.importConvertDeatil(purchaseOrderDetailEntity,excelData,excelData.getDeliveryQty());
                        detail.setPlanDeliveryDate(LocalDate.parse(value.get(0).getPlanDeliveryDate(), DateTimeFormatter.ofPattern("yyyy/M/d")));
                        detailEntityList.add(detail);
                        break;
                    }
                }
            }
            Pair<DeliveryOrderEntity, List<DeliveryOrderDetailEntity>> pair = new Pair<>(deliveryOrderEntity, detailEntityList);
            addList.add(pair);
        });

        if(CollectionUtils.isNotEmpty(addList)){
            deliveryOrderService.saveImport(addList);
        }
    }
}
