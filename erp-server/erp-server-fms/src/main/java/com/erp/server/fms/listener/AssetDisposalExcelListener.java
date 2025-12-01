package com.erp.server.fms.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.fms.dto.excel.AssetDisposalImportExcelDTO;
import com.erp.model.fms.entity.AssetCardDetailEntity;
import com.erp.model.fms.enums.AssetDisposalDetailInvoiceTypeEnum;
import com.erp.model.fms.enums.AssetDisposalDisposalMethodEnum;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.wms.dto.excel.SampleBorrowImportExcelDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.fms.service.AssetDisposalService;
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
 * @Classname AssetDisposalExcelListener
 * @Date 2025-10-11
 */
public class AssetDisposalExcelListener extends AnalysisEventListener<AssetDisposalImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;

    //组织
    private Map<String, String> companyMap;
    //币种
    private List<DictCurrencyEntity> dictCurrencyEntities;
    //资产位置
    private Map<String, String> assetLocationMap ;
    //资产卡片
    private Map<String, String> assetCardMap;
    //资产卡片明细
    private Map<String, List<AssetCardDetailEntity>> assetCardDetailMap;

    private final AssetDisposalService assetDisposalService = SpringUtil.getBean(AssetDisposalService.class);

    private final DocNoGenHelper docNoGenHelper = SpringUtil.getBean(DocNoGenHelper.class);

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    /**
     * 错误信息
     */
    @Getter
    private List<AssetDisposalImportExcelDTO> errorList = new ArrayList<>();

    @Getter
    private List<AssetDisposalImportExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    public AssetDisposalExcelListener(String taskId,
                                      String importType,
                                      Integer importCount,
                                      Map<String, String> companyMap,
                                      List<DictCurrencyEntity> dictCurrencyEntities,
                                      Map<String, String> assetLocationMap,
                                      Map<String, String> assetCardMap,
                                      Map<String, List<AssetCardDetailEntity>> assetCardDetailMap) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
        this.companyMap = companyMap;
        this.dictCurrencyEntities = dictCurrencyEntities;
        this.assetLocationMap = assetLocationMap;
        this.assetCardMap = assetCardMap;
        this.assetCardDetailMap = assetCardDetailMap;
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
    public void invoke(AssetDisposalImportExcelDTO excelDTO, AnalysisContext analysisContext) {
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

        //业务日期
        String businessDateStr = excelDTO.getBusinessDateStr();
        if(StringUtils.isNotBlank(businessDateStr)){
            LocalDate businessDate = null;
            try {
                businessDate = LocalDate.parse(businessDateStr, dateTimeFormatter);
            } catch (Exception e1) {
                try {
                    businessDate = LocalDate.parse(businessDateStr, dateTimeFormatter2);
                } catch (Exception e2) {
                    try {
                        businessDate = LocalDate.parse(businessDateStr, dateTimeFormatter3);
                    } catch (Exception e3) {
                        errorMsgList.add("业务日期格式错误，请使用 yyyy-MM-dd、yyyy/M/d 或 yyyy/MM/dd 格式");
                    }
                }
            }
            excelDTO.setBusinessDate(businessDate);
        }


        //资产组织
        String assetOrgName = excelDTO.getAssetOrgName();
        if(StringUtils.isNotBlank(assetOrgName)){
            String assetOrgId = companyMap.getOrDefault(assetOrgName, "");
            if(StringUtils.isBlank(assetOrgId)){
                errorMsgList.add("资产组织不存在");
            }else {
                excelDTO.setAssetOrgId(assetOrgId);
            }
        }
        //处置方式
        String disposalMethodName = excelDTO.getDisposalMethodName();
        if(StringUtils.isNotBlank(disposalMethodName)){
            AssetDisposalDisposalMethodEnum disposalMethodEnum = AssetDisposalDisposalMethodEnum.getEnumByName(disposalMethodName);
            if(Objects.nonNull(disposalMethodEnum)){
                excelDTO.setDisposalMethod(disposalMethodEnum.getCode());
                excelDTO.setSourceType(disposalMethodEnum.getSourceType());
            }else {
                errorMsgList.add("处置方式不存在");
            }
        }

        String sourceCode = excelDTO.getSourceCode();
        String sourceId  = "";
        if(StringUtils.isNotBlank(sourceCode)){
            sourceId = assetCardMap.get(sourceCode);
            if(StringUtils.isNotBlank(sourceId)){
                excelDTO.setSourceId(sourceId);
            }else {
                errorMsgList.add("资产卡片不存在");
            }
        }

        //处置币别
        if(StringUtils.isNotBlank(excelDTO.getDisposalCurrency())){
            //无视大小写比较
            DictCurrencyEntity currencyEntity = dictCurrencyEntities.stream()
                    .filter(e -> StringUtils.equals(e.getName(), excelDTO.getDisposalCurrency())|| StringUtils.equalsIgnoreCase(e.getId(), excelDTO.getDisposalCurrency()))
                    .findFirst()
                    .orElse(null);
            if(Objects.nonNull(currencyEntity)){
                excelDTO.setDisposalCurrency(currencyEntity.getId());
            }else {
                errorMsgList.add("币种不存在");
            }
        }

        String invoiceTypeName = excelDTO.getInvoiceTypeName();
        if(StringUtils.isNotBlank(invoiceTypeName)){
            String invoiceType = AssetDisposalDetailInvoiceTypeEnum.getCode(invoiceTypeName);
            if(StringUtils.isBlank(invoiceType)){
                errorMsgList.add("发票类型不存在");
            }else {
                excelDTO.setInvoiceType(invoiceType);
            }
        }

        //资产编码
        if(StringUtils.isNotBlank(sourceId) && StringUtils.isNotBlank(excelDTO.getAssetCode())){
            List<AssetCardDetailEntity> assetCardDetailEntities = assetCardDetailMap.get(sourceId);

            AssetCardDetailEntity assetCardDetailEntity = assetCardDetailEntities.stream().filter(e -> Objects.equals(e.getAssetCode(), excelDTO.getAssetCode())).findFirst().orElse(null);
            if(Objects.nonNull(assetCardDetailEntity)){
                excelDTO.setAssetLocationId(assetCardDetailEntity.getAssetLocationId());
                excelDTO.setAssetCode(assetCardDetailEntity.getAssetCode());
                excelDTO.setSourceDetailId(assetCardDetailEntity.getId());
                excelDTO.setAssetLocationName(assetLocationMap.getOrDefault(assetCardDetailEntity.getAssetLocationId(),""));
            }else {
                errorMsgList.add("卡片编码下的资产编码不存在");
            }
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
                List<String> errorNoList = errorList.stream().map(AssetDisposalImportExcelDTO::getNo).distinct().collect(Collectors.toList());

                List<AssetDisposalImportExcelDTO> errorList2 = new ArrayList<>();
                assetDisposalService.handleImportSuccessList(successList,errorNoList, errorList2,importType);
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
                List<String> errorNoList = errorList.stream().map(AssetDisposalImportExcelDTO::getNo).distinct().collect(Collectors.toList());

                List<AssetDisposalImportExcelDTO> errorList2 = new ArrayList<>();
                assetDisposalService.handleImportSuccessList(successList,errorNoList, errorList2,importType);
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
