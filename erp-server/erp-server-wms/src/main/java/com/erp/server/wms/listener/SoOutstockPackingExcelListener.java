package com.erp.server.wms.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.enums.CellExtraTypeEnum;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.metadata.CellExtra;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.wms.dto.excel.SoOutstockPackingExcelDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.enums.PackingTaskStatusEnum;
import com.erp.model.wms.enums.WmsDeclareStatusEnum;
import com.erp.server.wms.service.SoOutstockDetailService;
import com.erp.server.wms.service.SoOutstockService;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 装箱导入监听器
 */
public class SoOutstockPackingExcelListener extends AnalysisEventListener<SoOutstockPackingExcelDTO> {

    private List<CellExtra> cellExtraList = new ArrayList<>();

    @Getter
    private List<SoOutstockPackingExcelDTO> packingExcelDTOList = new ArrayList<>();
    /**
     * 导入错误数据
     */
    @Getter
    private List<SoOutstockPackingExcelDTO> errorList = new ArrayList<>();

    private final SoOutstockService soOutstockService = SpringUtil.getBean(SoOutstockService.class);

    private final SoOutstockDetailService soOutstockDetailService = SpringUtil.getBean(SoOutstockDetailService.class);

    @Override
    public void invoke(SoOutstockPackingExcelDTO data, AnalysisContext context) {
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
                        .comparing(SoOutstockPackingExcelDTO::getCode, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(SoOutstockPackingExcelDTO::getBoxNo, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        Iterator<SoOutstockPackingExcelDTO> it = packingExcelDTOList.iterator();
        List<String> codes = packingExcelDTOList.stream().map(SoOutstockPackingExcelDTO::getCode).distinct().collect(Collectors.toList());
        //发货单主记录
        List<SoOutstockEntity> soOutstockEntities = soOutstockService.listByCodes(codes);
        Map<String,SoOutstockEntity> soOutstockEntityMap = soOutstockEntities.stream().collect(Collectors.toMap(SoOutstockEntity::getCode, Function.identity()));
        List<String> firstMileDeliveryId = soOutstockEntities.stream().map(SoOutstockEntity::getId).distinct().collect(Collectors.toList());
        //发货单明细
        List<SoOutstockDetailEntity> soOutstockDetailEntities = soOutstockDetailService.listByMainIds(firstMileDeliveryId);
        Map<String, List<SoOutstockDetailEntity>> soOutstockDetailEntityMap = soOutstockDetailEntities.stream().collect(Collectors.groupingBy(SoOutstockDetailEntity::getMainId));
        //记录遍历时最大箱号
        Map<String,Integer> boxMap = new HashMap<>();
        while (it.hasNext()) {
            SoOutstockPackingExcelDTO packingExcelDTO = it.next();
            List<String> errFieldMsg = FieldValidUtil.fieldValid(packingExcelDTO);
            //检查字段空值
            if(CollectionUtils.isNotEmpty(errFieldMsg)){
                packingExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errFieldMsg));
                errorList.add(packingExcelDTO);
                it.remove();
                continue;
            }
            //检查发货单是否存在
            SoOutstockEntity soOutstockEntity = soOutstockEntityMap.get(packingExcelDTO.getCode());
            if(Objects.isNull(soOutstockEntity)){
                packingExcelDTO.setErrorMsg("出库单号不存在");
                errorList.add(packingExcelDTO);
                it.remove();
                continue;
            }
            if (!ApproveStatusEnum.APPROVE_ING.equals(soOutstockEntity.getApproveStatus())) {
                packingExcelDTO.setErrorMsg(ApiError.APPROVE_ING_IS_PACKING.msg);
                errorList.add(packingExcelDTO);
                it.remove();
                continue;
            }

            //只允许B2B订单装箱
            if (!OrderTypeEnum.B2B.getCode().equals(soOutstockEntity.getOrderType())) {
                packingExcelDTO.setErrorMsg(ApiError.B2B_ORDER_IS_PACK.msg);
                errorList.add(packingExcelDTO);
                it.remove();
                continue;
            }

            //检查发货单是否已审核
            if(soOutstockEntity.getApproveStatus().getCode().equals(ApproveStatusEnum.APPROVE.getCode())){
                packingExcelDTO.setErrorMsg("出库单已审核，无法更改装箱");
                errorList.add(packingExcelDTO);
                it.remove();
                continue;
            }

            //检查发货单是否已装箱
            if(soOutstockEntity.getPackingStatus().equals(PackingTaskStatusEnum.PACKED.getCode()) && WmsDeclareStatusEnum.FINISH.getCode().equals(soOutstockEntity.getDeclareStatus())){
                packingExcelDTO.setErrorMsg("出库单已装箱并且生成报关单，无法更改装箱信息");
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
            List<SoOutstockDetailEntity> currentDetailList = soOutstockDetailEntityMap.get(soOutstockEntity.getId());
            if(currentDetailList.stream().noneMatch(v->v.getSkuNo().equals(packingExcelDTO.getSku()))){
                packingExcelDTO.setErrorMsg("SKU在出库单不存在");
                errorList.add(packingExcelDTO);
                it.remove();
                continue;
            }
            // 发货单SKU装箱数量超过待装箱数量
            SoOutstockDetailEntity soOutstockDetailEntity = currentDetailList.stream().filter(v -> v.getSkuNo().equals(packingExcelDTO.getSku())).findFirst().orElse(new SoOutstockDetailEntity());
            packingExcelDTO.setSkuId(soOutstockDetailEntity.getSkuId());
            soOutstockDetailEntity.setActualQty(soOutstockDetailEntity.getActualQty() - packingExcelDTO.getSingleBoxQuantity());
            if(soOutstockDetailEntity.getActualQty() < 0){
                packingExcelDTO.setErrorMsg("出库单SKU装箱数量超过出库单数量");
                errorList.add(packingExcelDTO);
                it.remove();
            }
        }
    }
}
