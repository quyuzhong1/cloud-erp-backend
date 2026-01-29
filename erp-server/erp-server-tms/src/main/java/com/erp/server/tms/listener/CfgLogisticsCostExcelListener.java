package com.erp.server.tms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.tms.dto.excel.CfgLogisticsCostExcelDTO;
import com.erp.model.tms.entity.CfgLogisticsCostImportFieldEntity;
import com.erp.model.tms.entity.TmsCfgCostEntity;
import com.erp.model.tms.enums.*;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.service.CfgLogisticsCostImportDetailService;
import com.erp.server.tms.service.CfgLogisticsCostImportService;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author jack
 * @Classname CfgLogisticsCostExcelListener
 * @Date 2026-01-21
 */
public class CfgLogisticsCostExcelListener extends AnalysisEventListener<CfgLogisticsCostExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;

    //用户
    private List<FindUserDTO> userList ;

    private Map<String, String> dictBasicMap;

    private Map<String, String> logisticsSupplierMap;

    private Map<String, String> salesPlatformMap;

    private Map<String, List<CfgLogisticsCostImportFieldEntity>> fieldMap;

    private Map<String, List<TmsCfgCostEntity>> tmsCfgCostGroup;


    private final CfgLogisticsCostImportService cfgLogisticsCostImportService = SpringUtil.getBean(CfgLogisticsCostImportService.class);

    private final CfgLogisticsCostImportDetailService cfgLogisticsCostImportDetailService = SpringUtil.getBean(CfgLogisticsCostImportDetailService.class);

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    /**
     * 错误信息
     */
    @Getter
    private List<CfgLogisticsCostExcelDTO> errorList = new ArrayList<>();

    @Getter
    private List<CfgLogisticsCostExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    public CfgLogisticsCostExcelListener(String taskId,
                                         String importType,
                                         Integer importCount,
                                         Map<String, String> dictBasicMap,
                                         Map<String, String> logisticsSupplierMap,
                                         Map<String, String> salesPlatformMap,
                                         Map<String, List<CfgLogisticsCostImportFieldEntity>> fieldMap,
                                         Map<String, List<TmsCfgCostEntity>> tmsCfgCostGroup,
                                         List<FindUserDTO> userList) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
        this.userList = userList;
        this.dictBasicMap = dictBasicMap;
        this.logisticsSupplierMap = logisticsSupplierMap;
        this.salesPlatformMap = salesPlatformMap;
        this.fieldMap = fieldMap;
        this.tmsCfgCostGroup = tmsCfgCostGroup;
    }

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter dateTimeFormatter2 = DateTimeFormatter.ofPattern("yyyy/M/d");
    private final DateTimeFormatter dateTimeFormatter3 = DateTimeFormatter.ofPattern("yyyy/MM/dd");
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
    public void invoke(CfgLogisticsCostExcelDTO excelDTO, AnalysisContext analysisContext) {
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


        String businessTypeName = excelDTO.getBusinessTypeName();
        if(StringUtils.isNotBlank(businessTypeName)){
            String businessType = dictBasicMap.get(businessTypeName);
            if(StringUtils.isBlank(businessType)){
                errorMsgList.add("配置单据不存在");
            }else {
                excelDTO.setBusinessType(businessType);

                String targetFieldName = excelDTO.getTargetFieldName().trim();
                if(StringUtils.isNotBlank(targetFieldName)){
                    List<CfgLogisticsCostImportFieldEntity> fieldEntities = fieldMap.get(businessType);
                    if(CollUtil.isEmpty(fieldEntities)){
                        errorMsgList.add("【"+businessTypeName+"】配置单据类型不存在字段基础数据");
                    }else {
                        CfgLogisticsCostImportFieldEntity fieldEntity = fieldEntities.stream().filter(e -> e.getFieldName().equals(targetFieldName)).findFirst().orElse(null);
                        if(Objects.isNull(fieldEntity)){
                            errorMsgList.add("【"+targetFieldName+"】字段基础数据不存在");
                        }else {
                            excelDTO.setTargetFieldId(fieldEntity.getId());
                        }
                    }

                    if(Objects.equals(targetFieldName,"费用项明细")){
                        String targetDetailFieldName = excelDTO.getTargetDetailFieldName().trim();
                        if(StringUtils.isBlank(targetDetailFieldName)){
                            errorMsgList.add(ApiError.LOGISTICS_BILL_DETAIL_FIELD_REQUIRED.getMsg());
                        }else if(!targetDetailFieldName.contains("/") || targetDetailFieldName.split("/").length !=2){
                            errorMsgList.add("格式错误，正确格式如：运费/物流费用");
                        }else {
                            String type ="";
                            if(Objects.equals(businessType, SourceTypeEnum.LOGISTICS_BILL_COST.getCode())){
                                type = DictCostAttributionEnum.SELF_DELIVER.getCode();
                            }else {
                                type = DictCostAttributionEnum.LAST_MILE.getCode();
                            }
                            List<TmsCfgCostEntity> tmsCfgCostEntities = tmsCfgCostGroup.get(type);

                            List<String> list = Arrays.asList(targetDetailFieldName.split("/"));
                            AllocationFeeTypeEnum allocationFeeTypeCode = AllocationFeeTypeEnum.getByName(list.get(0));
                            TmsCfgCostEntity tmsCfgCostEntity = tmsCfgCostEntities.stream()
                                    .filter(e -> Objects.equals(allocationFeeTypeCode, e.getDictCostCategory()) && Objects.equals(list.get(1), e.getCostName()))
                                    .findFirst().orElse(null);
                            if(Objects.isNull(tmsCfgCostEntity)){
                                errorMsgList.add(StrUtil.format("【{}】费用项不存在",targetDetailFieldName));
                            }else {
                                excelDTO.setTargetDetailFieldId(tmsCfgCostEntity.getId());
                            }
                        }
                    }
                }
            }
        }

        String cfgTypeName = excelDTO.getCfgTypeName();
        if(StringUtils.isNotBlank(cfgTypeName)){
            String cfgType = CfgLogisticsCostImportCfgTypeEnum.getCode(cfgTypeName);
            if(StringUtils.isBlank(cfgType)){
                errorMsgList.add("配置类型不存在");
            }else {
                excelDTO.setCfgType(cfgType);

                String dictPlatformName = excelDTO.getDictPlatformName();
                if(StringUtils.isNotBlank(dictPlatformName)){
                    String dictPlatform ="";
                    if(Objects.equals(CfgLogisticsCostImportCfgTypeEnum.LOGISTICS_SUPPLIER.getCode(),cfgType)){
                        dictPlatform = logisticsSupplierMap.get(dictPlatformName);
                    }else {
                        dictPlatform = salesPlatformMap.get(dictPlatformName);
                    }
                    if(StringUtils.isBlank(dictPlatform)){
                        errorMsgList.add("配置平台不存在");
                    }else {
                        excelDTO.setDictPlatform(dictPlatform);
                    }
                }
            }
        }

        String costTypeName = excelDTO.getCostTypeName();
        if(StringUtils.isNotBlank(costTypeName)){
            String costType = CfgLogisticsCostImportCostTypeEnum.getCode(costTypeName);
            if(StringUtils.isBlank(costType)){
                errorMsgList.add("费用来源不存在");
            }else {
                excelDTO.setCostType(costType);
            }
        }

        String importTypeName = excelDTO.getImportTypeName();
        if(StringUtils.isNotBlank(importTypeName)){
            String[] split = importType.split(",");
            List<String> importTypeList = Arrays.asList(split);
            for (String s : importTypeList) {
                String code = CfgLogisticsCostImportImportTypeEnum.getCode(s);
                if(StringUtils.isBlank(code)){
                    errorMsgList.add("【"+s+"】不存在");
                }
            }
            excelDTO.setImportType(Arrays.stream(split)
                    .map(CfgLogisticsCostImportImportTypeEnum::getCode)
                    .collect(Collectors.joining(",")));
        }

        String disabledName = excelDTO.getDisabledName();
        if(StringUtils.isNotBlank(disabledName)){
            Boolean disabled = disabledName.equals("启用") ? false : true;
            excelDTO.setDisabled(disabled);
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
                List<String> errorNoList = errorList.stream().map(CfgLogisticsCostExcelDTO::getNo).distinct().collect(Collectors.toList());
                List<CfgLogisticsCostExcelDTO> errorList2 = new ArrayList<>();
                cfgLogisticsCostImportService.handleImportSuccessList(successList,errorNoList, errorList2,importType);
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
                List<String> errorNoList = errorList.stream().map(CfgLogisticsCostExcelDTO::getNo).distinct().collect(Collectors.toList());
                List<CfgLogisticsCostExcelDTO> errorList2 = new ArrayList<>();
                cfgLogisticsCostImportService.handleImportSuccessList(successList,errorNoList, errorList2,importType);
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
