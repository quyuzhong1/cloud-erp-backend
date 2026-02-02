package com.erp.server.wms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.wms.dto.OperateLogDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.excel.StocktakingTaskDetailExcelDTO;
import com.erp.model.wms.entity.StocktakingTaskDetailEntity;
import com.erp.model.wms.entity.StocktakingTaskEntity;
import com.erp.model.wms.enums.StocktakingStatusEnum;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.StocktakingTaskDetailService;
import com.erp.server.wms.service.StocktakingTaskService;
import com.erp.server.wms.service.WarehouseService;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * @Author: wtr
 * @Date: 2026/2/2 10:08
 * @Param:
 * @Return:
 * @Description:
 **/
public class StocktakingTaskFirstQtyExcelListener extends AnalysisEventListener<StocktakingTaskDetailExcelDTO> {


    private StocktakingTaskDetailService stocktakingTaskDetailService;

    private StocktakingTaskService stocktakingTaskService;

    private WarehouseService warehouseService;

    private OperateLogService operateLogService;

    private List<StocktakingTaskDetailExcelDTO> errorList = new ArrayList<>();

    private List<StocktakingTaskDetailEntity> updateList = new ArrayList<>();

    List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList = new ArrayList<>();


    public StocktakingTaskFirstQtyExcelListener(StocktakingTaskService stocktakingTaskService,
                                        StocktakingTaskDetailService stocktakingTaskDetailService,
                                        WarehouseService warehouseService,
                                        OperateLogService operateLogService) {
        this.stocktakingTaskDetailService = stocktakingTaskDetailService;
        this.stocktakingTaskService = stocktakingTaskService;
        this.warehouseService = warehouseService;
        this.operateLogService = operateLogService;
    }

    /**
     * 每解析一行执行一次
     *
     * @param excelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-08-09 17:56
     */
    @Override
    public void invoke(StocktakingTaskDetailExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        //盘点数量
        Integer qty = 0;
        try {
            qty = Integer.valueOf(excelDTO.getQty());
            if (qty < 0) {
                errorMsgList.add("盘点数量不能为负数");
            }
        }catch (Exception e){
            errorMsgList.add("盘点数量不能为非整数");
        }
        //任务盘点单号
        String taskCode = excelDTO.getCode();
        StocktakingTaskEntity taskEntity = stocktakingTaskService.getByCode(taskCode);
        if (Objects.isNull(taskEntity)) {
            errorMsgList.add("盘点任务不存在");
        }

        //仓库名称
        String warehouseName = excelDTO.getWarehouseName();
        //仓库id
        List<WarehouseDTO.ListDTO> warehouseList = warehouseService.listByNames(Collections.singletonList(warehouseName));
        String warehouseId = warehouseList.stream().filter(w -> w.getName().equals(warehouseName)).
                findFirst().map(WarehouseDTO.ListDTO::getId).orElse("");
        if (CharSequenceUtil.isBlank(warehouseId)) {
            errorMsgList.add("仓库不存在或没有仓库权限");
        }
        String mainId = Objects.nonNull(taskEntity) ? taskEntity.getId() : "";
        //仓位
        String warehouseLocation = Objects.isNull(excelDTO.getWarehouseLocation()) ? "" : excelDTO.getWarehouseLocation();
        //sku
        String skuNo = excelDTO.getSkuNo();
        StocktakingTaskDetailEntity taskDetail = stocktakingTaskDetailService.getTaskDetail(mainId, skuNo, warehouseId, warehouseLocation);
        if (Objects.isNull(taskDetail)) {
            errorMsgList.add("未匹配到任务明细,请检查仓库,仓位,SKU");
        }
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        //旧盘点数量
        Integer oldQty = taskDetail.getQty();
        //状态
        StocktakingStatusEnum status = taskEntity.getStatus();
        List<StocktakingStatusEnum> statusList = Arrays.asList(StocktakingStatusEnum.NOT_STARTED, StocktakingStatusEnum.RECOUNT);
        if(!statusList.contains(status)){
            errorMsgList.add("只有复盘中,未开始的盘点任务才能修改盘点库存");
        }
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }

        OperateLogDTO.AddModuleOperateLogDTO addModuleOperateLog = new OperateLogDTO.AddModuleOperateLogDTO();
        StringBuffer sb = new StringBuffer("盘点任务单");
        sb.append(taskCode).append(" 修改");
        sb.append(skuNo);
        sb.append("盘点库存由原来的:");
        sb.append(oldQty).append("修改为:").append(qty);
        addModuleOperateLog.setContent(sb.toString());
        addModuleOperateLog.setBusinessId(mainId);
        addModuleOperateLog.setModuleType(ModuleTypeEnum.STOCKTAKING_TASK.getCode());
        addModuleOperateLog.setOperation("修改操作");
        operateLogList.add(addModuleOperateLog);
        //可用库存
        Integer usableQty = taskDetail.getUsableQty();
        //冻结数量
        Integer frozenQty = taskDetail.getFrozenQty();
        //差异数量 等于盘点库存-可用库存-冻结库存
        Integer diffQty = qty - usableQty - frozenQty;
        taskDetail.setDiffQty(diffQty);
        taskDetail.setQty(qty);
        updateList.add(taskDetail);


    }


    /**
     * 全部解析完成后执行
     *
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-08-09 17:57
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (CollectionUtils.isNotEmpty(updateList)) {
            stocktakingTaskDetailService.updateBatchById(updateList);
        }
        if (CollectionUtils.isNotEmpty(operateLogList)) {
            operateLogService.batchAddModuleOperateLog(operateLogList);
        }

    }

    public List<StocktakingTaskDetailExcelDTO> getErrorList() {
        return errorList;
    }

}