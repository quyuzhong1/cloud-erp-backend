package com.erp.server.wms.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.enums.CellExtraTypeEnum;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.metadata.CellExtra;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.wms.dto.excel.PackingExcelDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import com.erp.server.wms.service.FirstMileDeliveryDetailService;
import com.erp.server.wms.service.FirstMileDeliveryService;
import com.erp.server.wms.service.OverseasProviderService;
import com.erp.server.wms.service.OverseasWarehouseInboundService;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

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

    private final FirstMileDeliveryService firstMileDeliveryService = SpringUtil.getBean(FirstMileDeliveryService.class);

    private final FirstMileDeliveryDetailService firstMileDeliveryDetailService = SpringUtil.getBean(FirstMileDeliveryDetailService.class);

    @Override
    public void invoke(PackingExcelDTO data, AnalysisContext context) {
        int currentRowNumber = context.readRowHolder().getRowIndex();
        data.setRowNum(currentRowNumber);
        packingExcelDTOList.add(data);
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
            int column = convertException.getColumnIndex();
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
        Map<String,FirstMileDeliveryEntity> firstMileDeliveryServiceMap = firstMileDeliveryEntities.stream().collect(Collectors.toMap(FirstMileDeliveryEntity::getCode, Function.identity()));
        List<String> firstMileDeliveryId = firstMileDeliveryEntities.stream().map(FirstMileDeliveryEntity::getId).distinct().collect(Collectors.toList());
        //发货单明细
        List<FirstMileDeliveryDetailEntity> firstMileDeliveryDetailEntityList = firstMileDeliveryDetailService.listByMainIds(firstMileDeliveryId);
        Map<String,List<FirstMileDeliveryDetailEntity>> firstMileDeliveryDetailEntityMap = firstMileDeliveryDetailEntityList.stream().collect(Collectors.groupingBy(FirstMileDeliveryDetailEntity::getMainId));
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
            if(Objects.isNull(firstMileDeliveryEntity)){
                packingExcelDTO.setErrorMsg("发货单号不存在");
                errorList.add(packingExcelDTO);
                it.remove();
                continue;
            }
            //检查发货单是否已审核
            if(firstMileDeliveryEntity.getApproveStatus().equals(ApproveStatusEnum.APPROVE.getCode())){
                packingExcelDTO.setErrorMsg(" 发货单下推海外仓入库单已审核，无法更改装箱");
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
            if(firstMileDeliveryDetailEntity.getDeliveryQty() < 0){
                packingExcelDTO.setErrorMsg("发货单SKU装箱数量超过待装箱数量");
                errorList.add(packingExcelDTO);
                it.remove();
            }
        }
    }
}
