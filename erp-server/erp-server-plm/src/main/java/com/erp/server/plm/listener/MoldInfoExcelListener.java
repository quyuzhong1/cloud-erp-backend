package com.erp.server.plm.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.dto.excel.MoldInfoImportExcelDTO;
import com.erp.model.plm.entity.BasicCategoryEntity;
import com.erp.model.plm.enums.MoldInfoTagEnum;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.plm.service.MoldInfoService;
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

/**
 * @author jack
 * @Classname MoldInfoExcelListener
 * @Date 2025-10-11
 */
public class MoldInfoExcelListener extends AnalysisEventListener<MoldInfoImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;


    //分类
    private Map<String, BasicCategoryEntity> categoryMap;
    //结算方式
    private Map<String, String> settleDictMap ;
    //付款条件
    private Map<String, String> paymentConditionMap ;
    //用户
    private List<FindUserDTO> userList ;
    //贷款供应商
    private Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap ;
    //模具类型
    private Map<String, String> cfgMouldSettingMap;

    private final MoldInfoService moldInfoService = SpringUtil.getBean(MoldInfoService.class);

    private final DocNoGenHelper docNoGenHelper = SpringUtil.getBean(DocNoGenHelper.class);

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    /**
     * 错误信息
     */
    @Getter
    private List<MoldInfoImportExcelDTO> errorList = new ArrayList<>();

    @Getter
    private List<MoldInfoImportExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    public MoldInfoExcelListener(String taskId,
                                 String importType,
                                 Integer importCount,
                                 List<FindUserDTO> userList,
                                 Map<String, SupplierDTO.SupplierSimpleDTO> supplierMap,
                                 Map<String, String> cfgMouldSettingMap,
                                 Map<String, BasicCategoryEntity> categoryMap,
                                 Map<String, String> settleDictMap,
                                 Map<String, String> paymentConditionMap) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
        this.userList = userList;
        this.cfgMouldSettingMap = cfgMouldSettingMap;
        this.supplierMap = supplierMap;
        this.categoryMap = categoryMap;
        this.settleDictMap = settleDictMap;
        this.paymentConditionMap = paymentConditionMap;
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
    public void invoke(MoldInfoImportExcelDTO excelDTO, AnalysisContext analysisContext) {
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

        String tagStr = excelDTO.getTagStr();
        if(StringUtils.isNotBlank(tagStr)){
            excelDTO.setTag(MoldInfoTagEnum.getCode(tagStr));
        }

        String chargeName = excelDTO.getChargeName();
        if(StringUtils.isNotBlank(chargeName)){
            FindUserDTO findUserDTO = userList.stream().filter(e -> Objects.equals(e.getUserName(), chargeName)).findFirst().orElse(null);
            if(Objects.isNull(findUserDTO)){
                errorMsgList.add("产品经理不存在");
            }else {
                excelDTO.setChargeId(findUserDTO.getUserId());
            }
        }

        String projectChargeName = excelDTO.getProjectChargeName();
        if(StringUtils.isNotBlank(projectChargeName)){
            FindUserDTO findUserDTO = userList.stream().filter(e -> Objects.equals(e.getUserName(), projectChargeName)).findFirst().orElse(null);
            if(Objects.isNull(findUserDTO)){
                errorMsgList.add("产品经理不存在");
            }else {
                excelDTO.setProjectChargeId(findUserDTO.getUserId());
            }
        }

        String categoryName = excelDTO.getCategoryName();
        if(StringUtils.isNotBlank(categoryName)){
            BasicCategoryEntity category = categoryMap.getOrDefault(categoryName, null);
            if( Objects.isNull(category)){
                errorMsgList.add("产品分类不存在");
            }else {
                excelDTO.setCategoryId(category.getId());

                //生成模具编号
                String code = docNoGenHelper.generateMouldCode(category.getCode());
                excelDTO.setCode(code);
            }
        }

        String typeName = excelDTO.getTypeName();
        if(StringUtils.isNotBlank(typeName)){
            String type = cfgMouldSettingMap.getOrDefault(typeName, "");
            if(StringUtils.isBlank(type)){
                errorMsgList.add("模具类型不存在");
            }else {
                excelDTO.setType(type);
            }
        }


        String activationDateStr = excelDTO.getActivationDateStr();
        if(StringUtils.isNotBlank(activationDateStr)){
            LocalDate activationDate = null;
            try {
                activationDate = LocalDate.parse(activationDateStr, dateTimeFormatter);
            } catch (Exception e1) {
                try {
                    activationDate = LocalDate.parse(activationDateStr, dateTimeFormatter2);
                } catch (Exception e2) {
                    try {
                        activationDate = LocalDate.parse(activationDateStr, dateTimeFormatter3);
                    } catch (Exception e3) {
                        errorMsgList.add("模具启用日期格式错误，请使用 yyyy-MM-dd、yyyy/M/d 或 yyyy/MM/dd 格式");
                    }
                }
            }
            excelDTO.setActivationDate(activationDate);
        }

        String supplierName = excelDTO.getSupplierName();
        if(StringUtils.isNotBlank(supplierName)){
            SupplierDTO.SupplierSimpleDTO suppler = supplierMap.getOrDefault(supplierName, null);
            if(Objects.isNull(suppler)){
                errorMsgList.add("供应商不存在");
            }else{
                excelDTO.setSupplierId(suppler.getId());
                excelDTO.setSupplierCode(suppler.getCode());
            }
        }

        String payMethodName = excelDTO.getPayMethodName();
        if(StringUtils.isNotBlank(payMethodName)){
            String payMethodId = settleDictMap.getOrDefault(payMethodName, "");
            if(StringUtils.isBlank(payMethodName)){
                errorMsgList.add("结算方式不存在");
            }else {
                excelDTO.setPayMethodId(payMethodId);
            }
        }

        String paymentConditionName = excelDTO.getPaymentConditionName();
        if(StringUtils.isNotBlank(paymentConditionName)){
            String paymentCondition = paymentConditionMap.getOrDefault(paymentConditionName, "");
            if(StringUtils.isBlank(paymentCondition)){
                errorMsgList.add("付款条件不存在");
            }else {
                excelDTO.setPaymentCondition(paymentCondition);
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
                List<MoldInfoImportExcelDTO> errorList2 = new ArrayList<>();
                moldInfoService.handleImportSuccessList(successList, errorList2,importType);
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
                List<MoldInfoImportExcelDTO> errorList2 = new ArrayList<>();
                moldInfoService.handleImportSuccessList(successList, errorList2,importType);
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
