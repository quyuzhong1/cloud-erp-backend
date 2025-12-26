package com.erp.server.oms.listener;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.dto.excel.KolB2cApplicationAddressImportExcelDTO;
import com.erp.model.oms.dto.excel.KolB2cApplicationDetailImportExcelDTO;
import com.erp.model.oms.dto.excel.KolB2cApplicationImportExcelDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.oms.service.KolB2cApplicationService;
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
 * @Classname KolB2cApplicationExcelListener
 * @Date 2025-12-08
 */
public class KolB2cApplicationExcelListener extends AnalysisEventListener<KolB2cApplicationImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;

    private Map<String, String> cfgKolOptionMap;
    private Map<String, String> shopMap;
    private Map<String, FindUserDTO> userMap;
    private Map<String, String> deptMap;
    private Map<String, String> warehouserMap;
    private Map<String, String> logisticsMap;
    private Map<String, String> currencyMap;
    private List<KolB2cApplicationDetailImportExcelDTO> detailSuccessList ;
    private List<KolB2cApplicationDetailImportExcelDTO> detailErrorList;
    private List<KolB2cApplicationAddressImportExcelDTO> addressSuccessList;
    private List<KolB2cApplicationAddressImportExcelDTO> addressErrorList ;


    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    /**
     * 错误信息
     */
    @Getter
    private List<KolB2cApplicationImportExcelDTO> errorList = new ArrayList<>();

    @Getter
    private List<KolB2cApplicationImportExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    public KolB2cApplicationExcelListener(String taskId, String importType, Integer importCount, Map<String, String> cfgKolOptionMap, Map<String, String> shopMap, Map<String, FindUserDTO> userMap,
                                          Map<String, String> deptMap, Map<String, String> warehouserMap, Map<String, String> logisticsMap,Map<String, String> currencyMap
//            ,List<KolB2cApplicationDetailImportExcelDTO> detailSuccessList,List<KolB2cApplicationDetailImportExcelDTO> detailErrorList,
//                                          List<KolB2cApplicationAddressImportExcelDTO> addressSuccessList,List<KolB2cApplicationAddressImportExcelDTO> addressErrorList
    ) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
        this.cfgKolOptionMap = cfgKolOptionMap;
        this.shopMap = shopMap;
        this.userMap = userMap;
        this.deptMap = deptMap;
        this.warehouserMap = warehouserMap;
        this.logisticsMap = logisticsMap;
        this.currencyMap = currencyMap;
//        this.detailSuccessList = detailSuccessList;
//        this.detailErrorList = detailErrorList;
//        this.addressSuccessList = addressSuccessList;
//        this.addressErrorList = addressErrorList;
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
    public void invoke(KolB2cApplicationImportExcelDTO excelDTO, AnalysisContext analysisContext) {
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

        //申请日期
        String applyDateStr = excelDTO.getApplyDateStr();
        if(StringUtils.isNotBlank(applyDateStr)){
            LocalDate applyDate = null;
            try {
                applyDate = LocalDate.parse(applyDateStr, dateTimeFormatter);
            } catch (Exception e1) {
                try {
                    applyDate = LocalDate.parse(applyDateStr, dateTimeFormatter2);
                } catch (Exception e2) {
                    try {
                        applyDate = LocalDate.parse(applyDateStr, dateTimeFormatter3);
                    } catch (Exception e3) {
                        errorMsgList.add("申请时间格式错误，请使用 yyyy-MM-dd、yyyy/M/d 或 yyyy/MM/dd 格式");
                    }
                }
            }
            excelDTO.setApplyDate(applyDate);
        }

        String sampleTypeName = excelDTO.getSampleTypeName();
        if(StringUtils.isNotBlank(sampleTypeName)){
            if(cfgKolOptionMap.containsKey(sampleTypeName)){
                excelDTO.setSampleType(cfgKolOptionMap.get(sampleTypeName));
            }else {
                errorMsgList.add("寄样类型【"+sampleTypeName+"】不存在");
            }
        }

        //店铺
        String shopName = excelDTO.getShopName();
        if(StringUtils.isNotBlank(shopName)){
            if(shopMap.containsKey(shopName)){
                excelDTO.setShopId(shopMap.get(shopName));
            }else {
                errorMsgList.add("店铺【"+shopName+"】不存在");
            }
        }
        //币别
        String currencyName = excelDTO.getCurrencyName();
        if(StringUtils.isNotBlank(currencyName)){
            if(currencyMap.containsKey(currencyName)){
                excelDTO.setCurrency(currencyMap.get(currencyName));
            } else {
                errorMsgList.add("币别【"+currencyName+"】不存在");
            }
        }

        //业务类型
        String isInternationalName = excelDTO.getIsInternationalName();
        if(StringUtils.isNotBlank(isInternationalName)){
            if("国外".equals(isInternationalName) || "国内".equals(isInternationalName)){
                excelDTO.setIsInternational(getIsInternational(isInternationalName));
            } else {
                errorMsgList.add("业务类型【"+isInternationalName+"】不存在");
            }
        }
        //发货仓库
        String deliveryWarehouse = excelDTO.getWarehouseName();
        if(StringUtils.isNotBlank(deliveryWarehouse)){
            if(warehouserMap.containsKey(deliveryWarehouse)){
                excelDTO.setWarehouseId(warehouserMap.get(deliveryWarehouse));
            } else {
                errorMsgList.add("发货仓库【"+deliveryWarehouse+"】不存在");
            }
        }
        //物流渠道
        String logisticsChannelName = excelDTO.getLogisticsChannelName();
        if(StringUtils.isNotBlank(logisticsChannelName)){
            if(logisticsMap.containsKey(logisticsChannelName)){
                excelDTO.setLogisticsChannelId(logisticsMap.get(logisticsChannelName));
            } else {
                errorMsgList.add("物流渠道【"+logisticsChannelName+"】不存在");
            }
        }
        //申请人
        String applicantName = excelDTO.getApplyUserName();
        if(StringUtils.isNotBlank(applicantName)){
            if(userMap.containsKey(applicantName)){
                excelDTO.setApplyUserId(userMap.get(applicantName).getUserId());
            } else {
                errorMsgList.add("申请人【"+applicantName+"】不存在");
            }
        }
        //申请部门
        String applyDeptName = excelDTO.getApplyDeptName();
        if(StringUtils.isNotBlank(applyDeptName)){
            if(deptMap.containsKey(applyDeptName)){
                excelDTO.setApplyDeptId(deptMap.get(applyDeptName));
            } else {
                errorMsgList.add("申请部门【"+applyDeptName+"】不存在");
            }
        }

        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        successList.add(excelDTO);
    }

    private Boolean getIsInternational(String isInternational) {
        return "国外".equals(isInternational) ? true : false;
    }


    /**
     * 数据全部解析完成后执行
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!successList.isEmpty()){
//            try {
//                List<String> errorNoList = errorList.stream().map(KolB2cApplicationImportExcelDTO::getNo).distinct().collect(Collectors.toList());
//                List<KolB2cApplicationImportExcelDTO> errorList2 = new ArrayList<>();
//                kolB2cApplicationService.handleImportSuccessList(successList,errorNoList, errorList2,importType);
//                errorList.addAll(errorList2);
//            }catch (Exception e){
//                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
//                errorList.addAll(successList);
//            }
//            successList.clear();
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
