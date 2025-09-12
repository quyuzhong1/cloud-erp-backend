package com.erp.server.oms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.dto.ExhibitionOrderImportExcelDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.SampleLedgerDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.oms.service.ExhibitionOrderService;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author jack
 * @Classname ExhibitionOrderExcelListener
 * @Date 2025-08-26
 */
public class ExhibitionOrderExcelListener extends AnalysisEventListener<ExhibitionOrderImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;

    private final ExhibitionOrderService exhibitionOrderService = SpringUtil.getBean(ExhibitionOrderService.class);

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    /**
     * 错误信息
     */
    @Getter
    private List<ExhibitionOrderImportExcelDTO> errorList = new ArrayList<>();

    @Getter
    private List<ExhibitionOrderImportExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    public ExhibitionOrderExcelListener(String taskId,
                                        String importType,
                                        Integer importCount) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
    }

    /**
     * 每解析一行数据回调一遍
     *
     * @param excelDTO
     * @param analysisContext
     * @return void
     * @author jack
     * @date 2025-08-26
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(ExhibitionOrderImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        count += 1;
        //已经导入的数据跳过进度
        if (Objects.nonNull(importCount) && count < importCount){
            return;
        }

        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        successList.add(excelDTO);
        if (successList.size() >= BATCH_COUNT){
            try {
                List<String> errorNoList = errorList.stream().map(ExhibitionOrderImportExcelDTO::getNo).distinct().collect(Collectors.toList());
                List<ExhibitionOrderImportExcelDTO> errorList2 = new ArrayList<>();
                exhibitionOrderService.handleImportSuccessList(successList,errorNoList, errorList2,importType);
                errorList.addAll(errorList2);
            }catch (Exception e){
                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
    }


    /**
     * 数据全部解析完成后执行
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!successList.isEmpty()){
            try {
                List<String> errorNoList = errorList.stream().map(ExhibitionOrderImportExcelDTO::getNo).distinct().collect(Collectors.toList());
                List<ExhibitionOrderImportExcelDTO> errorList2 = new ArrayList<>();
                exhibitionOrderService.handleImportSuccessList(successList,errorNoList, errorList2,importType);
                errorList.addAll(errorList2);
            }catch (Exception e){
                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
    }

    private void updateTask(Integer count){
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(taskId);
        importResultDTO.setStatus(FileTaskStatusEnum.PROCESS.getCode());
        importResultDTO.setRemark("处理中");
        importResultDTO.setCount(count);
        downloadTaskFeign.updateTask(importResultDTO);
    }

}
