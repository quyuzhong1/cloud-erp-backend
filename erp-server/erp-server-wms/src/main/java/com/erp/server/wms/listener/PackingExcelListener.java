package com.erp.server.wms.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.enums.CellExtraTypeEnum;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.metadata.CellExtra;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.wms.dto.excel.PackingExcelDTO;
import com.erp.model.wms.dto.pickingstrategy.PickingListsDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.FmDeliveryLogisticsStatusEnum;
import com.erp.model.wms.enums.WmsDeclareStatusEnum;
import com.erp.server.wms.service.*;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 装箱导入监听器
 */
public class PackingExcelListener extends AnalysisEventListener<PackingExcelDTO> {

    private List<CellExtra> cellExtraList = new ArrayList<>();

    @Getter
    private List<PackingExcelDTO> packingExcelDTOList = new ArrayList<>();
    /**
     * 导入错误数据
     */
    @Getter
    private List<PackingExcelDTO> errorList = new ArrayList<>();

    private final RequisitionApplicationService requisitionApplicationService = SpringUtil.getBean(RequisitionApplicationService.class);
    private final RequisitionApplicationDetailService requisitionApplicationDetailService = SpringUtil.getBean(RequisitionApplicationDetailService.class);
    private final FirstMileDeliveryService firstMileDeliveryService = SpringUtil.getBean(FirstMileDeliveryService.class);
    private final SoDeliveryNoticeService soDeliveryNoticeService = SpringUtil.getBean(SoDeliveryNoticeService.class);
    private final SoDeliveryNoticeDetailService soDeliveryNoticeDetailService = SpringUtil.getBean(SoDeliveryNoticeDetailService.class);
    private final PickingListsService pickingListsService = SpringUtil.getBean(PickingListsService.class);
    private final FirstMileDeliveryDetailService firstMileDeliveryDetailService = SpringUtil.getBean(FirstMileDeliveryDetailService.class);

    @Override
    public void invoke(PackingExcelDTO data, AnalysisContext context) {
        //添加数据用于判断是否为空
        List<String> errorMsgList = new ArrayList<>();

        //注解验证信息
        List<String> msgList = FieldValidUtil.fieldValid(data);
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }else{
            int currentRowNumber = context.readRowHolder().getRowIndex();
            data.setRowNum(currentRowNumber);
            packingExcelDTOList.add(data);
        }

        String errStr = "";
        if (errorMsgList.size() > 0) {
            for (int i = 0; i < errorMsgList.size(); i++) {
                Integer indexTemp = i + 1;
                errStr = errStr + indexTemp + "、" + errorMsgList.get(i) + "；";
            }
            data.setErrorMsg(errStr);
            errorList.add(data);
        }
    }

    @Override
    public void extra(CellExtra extra, AnalysisContext context) {
        CellExtraTypeEnum type = extra.getType();
        switch (type) {
            case MERGE: {
                if(extra.getRowIndex() > 0){
                    cellExtraList.add(extra);
                }
                break;
            }
            default:{
            }
        }
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        if (exception instanceof ExcelDataConvertException) {
            ExcelDataConvertException convertException = (ExcelDataConvertException) exception;
            int row = convertException.getRowIndex();
            int column = convertException.getColumnIndex()+1;
            throw new ServiceException(ApiError.EXCEL_ILLEGAL_FIELDS,row,column);
        }
        throw exception;
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 读取完成后的操作
        ExcelUtil.explainMergeData(packingExcelDTOList,cellExtraList,1);
        //按照单号，箱号排序
        packingExcelDTOList = packingExcelDTOList.stream()
                .sorted(Comparator
                        .comparing(PackingExcelDTO::getCode, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(PackingExcelDTO::getBoxNo, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        Iterator<PackingExcelDTO> it = packingExcelDTOList.iterator();
        List<String> codes = packingExcelDTOList.stream().map(PackingExcelDTO::getCode).distinct().collect(Collectors.toList());
        //发货单主记录
        List<FirstMileDeliveryEntity> firstMileDeliveryEntities = firstMileDeliveryService.listByCodes(codes);
        List<SoDeliveryNoticeEntity> soDeliveryNoticeEntities =  soDeliveryNoticeService.listByCodes(codes);
        List<RequisitionApplicationEntity> requisitionApplicationEntityList = requisitionApplicationService.listByCodes(codes);
        Map<String,FirstMileDeliveryEntity> firstMileDeliveryServiceMap = firstMileDeliveryEntities.stream().collect(Collectors.toMap(FirstMileDeliveryEntity::getCode, Function.identity()));
        Map<String,SoDeliveryNoticeEntity> soDeliveryNoticeEntityMap = soDeliveryNoticeEntities.stream().collect(Collectors.toMap(SoDeliveryNoticeEntity::getCode, Function.identity()));
        Map<String,RequisitionApplicationEntity> requisitionApplicationMap = requisitionApplicationEntityList.stream().collect(Collectors.toMap(RequisitionApplicationEntity::getCode, Function.identity()));
        List<String> firstMileDeliveryId = firstMileDeliveryEntities.stream().map(FirstMileDeliveryEntity::getId).distinct().collect(Collectors.toList());
        List<String> soDeliveryNoticeIds = soDeliveryNoticeEntities.stream().map(SoDeliveryNoticeEntity::getId).distinct().collect(Collectors.toList());
        List<String> requisitionApplicationIds = requisitionApplicationEntityList.stream().map(RequisitionApplicationEntity::getId).distinct().collect(Collectors.toList());

        List<PickingListsDTO.SourceView> pickingList = pickingListsService.listBySourceIds(requisitionApplicationIds);
        //发货单明细
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList = firstMileDeliveryDetailService.listByMainIds(firstMileDeliveryId);
        List<SoDeliveryNoticeDetailEntity> soDeliveryNoticeDetailEntities = soDeliveryNoticeDetailService.listDetailByMainIds(soDeliveryNoticeIds);
        List<RequisitionApplicationDetailEntity> requisitionApplicationDetailList = requisitionApplicationDetailService.listByMainIds(requisitionApplicationIds);
        Map<String,List<FirstMileDeliveryDetailEntity>> firstMileDeliveryDetailEntityMap = firstMileDeliveryDetailEntityList.stream().collect(Collectors.groupingBy(FirstMileDeliveryDetailEntity::getMainId));
        Map<String,List<SoDeliveryNoticeDetailEntity>> soDeliveryDetailMap = soDeliveryNoticeDetailEntities.stream().collect(Collectors.groupingBy(SoDeliveryNoticeDetailEntity::getMainId));
        Map<String,List<RequisitionApplicationDetailEntity>> requisitionApplicationDetailMap = requisitionApplicationDetailList.stream().collect(Collectors.groupingBy(RequisitionApplicationDetailEntity::getMainId));

        Map<String,Integer> skuFirstMileSummaryMap = firstMileDeliveryDetailEntityList.stream().collect(Collectors.toMap(v->v.getMainId()+v.getSkuNo(),FirstMileDeliveryDetailEntity::getDeliveryQty, Integer::sum));
        Map<String,Integer> skuDeliverySummaryMap = soDeliveryNoticeDetailEntities.stream().collect(Collectors.toMap(v->v.getMainId()+v.getSkuNo(),SoDeliveryNoticeDetailEntity::getDeliveryQty, Integer::sum));
        Map<String,Integer> skuRequisitionSummaryMap = requisitionApplicationDetailList.stream().collect(Collectors.toMap(v->v.getMainId()+v.getSkuNo(),RequisitionApplicationDetailEntity::getPickingQty, Integer::sum));
        //记录遍历时最大箱号
        Map<String,Integer> boxMap = new HashMap<>();
        while (it.hasNext()) {
            PackingExcelDTO packingExcelDTO = it.next();
            List<String> errFieldMsg = FieldValidUtil.fieldValid(packingExcelDTO);
            //检查字段空值
            if(CollectionUtils.isNotEmpty(errFieldMsg)){
                packingExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errFieldMsg));
                errorList.add(packingExcelDTO);
                it.remove();
                continue;
            }
            //检查发货单是否存在
            FirstMileDeliveryEntity firstMileDeliveryEntity = firstMileDeliveryServiceMap.get(packingExcelDTO.getCode());
            SoDeliveryNoticeEntity soDeliveryNoticeEntity = soDeliveryNoticeEntityMap.get(packingExcelDTO.getCode());
            RequisitionApplicationEntity requisitionApplicationEntity = requisitionApplicationMap.get(packingExcelDTO.getCode());
            if(Objects.isNull(firstMileDeliveryEntity) && Objects.isNull(soDeliveryNoticeEntity) && Objects.isNull(requisitionApplicationEntity)){
                packingExcelDTO.setErrorMsg("来源单号不存在");
                errorList.add(packingExcelDTO);
                it.remove();
                continue;
            }
            if(Objects.nonNull(requisitionApplicationEntity)){
                PickingListsDTO.SourceView sourceView = pickingList.stream().filter(v->v.getSourceId().equals(requisitionApplicationEntity.getId())).findFirst().orElse(null);
                if(Objects.isNull(sourceView)){
                    packingExcelDTO.setErrorMsg("要货申请未生成拣货单，不能生成装箱任务");
                    errorList.add(packingExcelDTO);
                    it.remove();
                    continue;
                }
            }
            if(Objects.nonNull(firstMileDeliveryEntity) && (FmDeliveryLogisticsStatusEnum.FINISH.equals(firstMileDeliveryEntity.getLogisticsStatus()) || WmsDeclareStatusEnum.FINISH.equals(firstMileDeliveryEntity.getDeclareStatus()))){
                packingExcelDTO.setErrorMsg("物流单/报关单已生成，不支持修改");
                errorList.add(packingExcelDTO);
                it.remove();
                continue;
            }
            //检查发货单是否已审核
            if(Objects.nonNull(firstMileDeliveryEntity) && firstMileDeliveryEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getCode())){
                packingExcelDTO.setErrorMsg(" 发货单已审核，无法更改装箱");
                errorList.add(packingExcelDTO);
                it.remove();
                continue;
            }
            if(Objects.nonNull(soDeliveryNoticeEntity) && soDeliveryNoticeEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getCode())){
                packingExcelDTO.setErrorMsg(" 发货通知已审核，无法更改装箱");
                errorList.add(packingExcelDTO);
                it.remove();
                continue;
            }
            // 装箱号不连续
            Integer currentMaxBoxNo = boxMap.get(packingExcelDTO.getCode());
            if(Objects.isNull(currentMaxBoxNo)){
                if(packingExcelDTO.getBoxNo() != 1){
                    packingExcelDTO.setErrorMsg("装箱号不连续");
                    errorList.add(packingExcelDTO);
                    it.remove();
                    continue;
                }
                boxMap.put(packingExcelDTO.getCode(), packingExcelDTO.getBoxNo());
            }else{
                if(!currentMaxBoxNo.equals(packingExcelDTO.getBoxNo()) && !currentMaxBoxNo.equals(packingExcelDTO.getBoxNo() - 1)){
                    packingExcelDTO.setErrorMsg("装箱号不连续");
                    errorList.add(packingExcelDTO);
                    it.remove();
                    continue;
                }else{
                    boxMap.put(packingExcelDTO.getCode(), packingExcelDTO.getBoxNo());
                }
            }
            //SKU是否存在
            if (Objects.nonNull(firstMileDeliveryEntity)){
                List<FirstMileDeliveryDetailEntity> currentDetailList = firstMileDeliveryDetailEntityMap.get(firstMileDeliveryEntity.getId());
                if(currentDetailList.stream().noneMatch(v->v.getSkuNo().equals(packingExcelDTO.getSku()))){
                    packingExcelDTO.setErrorMsg("SKU在发货单不存在");
                    errorList.add(packingExcelDTO);
                    it.remove();
                    continue;
                }
                // 发货单SKU装箱数量超过待装箱数量
                FirstMileDeliveryDetailEntity firstMileDeliveryDetailEntity = currentDetailList.stream().filter(v->v.getSkuNo().equals(packingExcelDTO.getSku())).findFirst().orElse(new FirstMileDeliveryDetailEntity());
                packingExcelDTO.setSkuId(firstMileDeliveryDetailEntity.getSkuId());
                firstMileDeliveryDetailEntity.setDeliveryQty(firstMileDeliveryDetailEntity.getDeliveryQty() - packingExcelDTO.getSingleBoxQuantity());
                Integer skuNum = skuFirstMileSummaryMap.get(firstMileDeliveryEntity.getId()+packingExcelDTO.getSku());
                if(skuNum < packingExcelDTO.getSingleBoxQuantity()){
                    packingExcelDTO.setErrorMsg("发货单SKU装箱数量超过待装箱数量");
                    errorList.add(packingExcelDTO);
                    it.remove();
                }
                skuFirstMileSummaryMap.put(firstMileDeliveryEntity.getId()+packingExcelDTO.getSku(),skuNum-packingExcelDTO.getSingleBoxQuantity());
            }
            if (Objects.nonNull(soDeliveryNoticeEntity)){
                List<SoDeliveryNoticeDetailEntity> currentDetailList = soDeliveryDetailMap.get(soDeliveryNoticeEntity.getId());
                if(currentDetailList.stream().noneMatch(v->v.getSkuNo().equals(packingExcelDTO.getSku()))){
                    packingExcelDTO.setErrorMsg("SKU在发货通知单不存在");
                    errorList.add(packingExcelDTO);
                    it.remove();
                    continue;
                }
                // 发货单SKU装箱数量超过待装箱数量
                SoDeliveryNoticeDetailEntity soDeliveryNoticeDetailEntity = currentDetailList.stream().filter(v->v.getSkuNo().equals(packingExcelDTO.getSku())).findFirst().orElse(new SoDeliveryNoticeDetailEntity());
                packingExcelDTO.setSkuId(soDeliveryNoticeDetailEntity.getSkuId());
                soDeliveryNoticeDetailEntity.setDeliveryQty(soDeliveryNoticeDetailEntity.getDeliveryQty() - packingExcelDTO.getSingleBoxQuantity());
                Integer skuNum = skuDeliverySummaryMap.get(soDeliveryNoticeEntity.getId()+packingExcelDTO.getSku());
                if(skuNum < packingExcelDTO.getSingleBoxQuantity()){
                    packingExcelDTO.setErrorMsg("发货通知单SKU装箱数量超过待装箱数量");
                    errorList.add(packingExcelDTO);
                    it.remove();
                }
                skuDeliverySummaryMap.put(soDeliveryNoticeEntity.getId()+packingExcelDTO.getSku(),skuNum-packingExcelDTO.getSingleBoxQuantity());
            }
            if (Objects.nonNull(requisitionApplicationEntity)){
                List<RequisitionApplicationDetailEntity> currentDetailList = requisitionApplicationDetailMap.get(requisitionApplicationEntity.getId());
                if(currentDetailList.stream().noneMatch(v->v.getSkuNo().equals(packingExcelDTO.getSku()))){
                    packingExcelDTO.setErrorMsg("SKU在要货申请单不存在");
                    errorList.add(packingExcelDTO);
                    it.remove();
                    continue;
                }
                // 发货单SKU装箱数量超过待装箱数量
                RequisitionApplicationDetailEntity requisitionApplicationDetailEntity = currentDetailList.stream().filter(v->v.getSkuNo().equals(packingExcelDTO.getSku())).findFirst().orElse(new RequisitionApplicationDetailEntity());
                packingExcelDTO.setSkuId(requisitionApplicationDetailEntity.getSkuId());
                requisitionApplicationDetailEntity.setPickingQty(requisitionApplicationDetailEntity.getPickingQty() - packingExcelDTO.getSingleBoxQuantity());
                Integer skuNum = skuRequisitionSummaryMap.get(requisitionApplicationEntity.getId()+packingExcelDTO.getSku());
                if(skuNum < packingExcelDTO.getSingleBoxQuantity()){
                    packingExcelDTO.setErrorMsg("发货单SKU装箱数量超过待装箱数量");
                    errorList.add(packingExcelDTO);
                    it.remove();
                }
                skuRequisitionSummaryMap.put(requisitionApplicationEntity.getId()+packingExcelDTO.getSku(),skuNum-packingExcelDTO.getSingleBoxQuantity());
            }
        }
    }
}
