package com.erp.server.tms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.tms.dto.excel.ImportLogisticsThirdChannelRefExcelDTO;
import com.erp.model.tms.entity.BasicQueryLogisticsProviderEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.enums.LogisticsThirdChannelRefPushTypeEnum;
import com.erp.model.tms.enums.TrackPlatformTypeEnum;
import com.erp.model.wms.dto.excel.SampleBorrowImportExcelDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.service.LogisticsThirdChannelRefService;
import lombok.Getter;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class LogisticsThirdChannelRefListener extends AnalysisEventListener<ImportLogisticsThirdChannelRefExcelDTO> {
    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;

    private List<LogisticsSupplierEntity> logisticsSupplierEntities;
    private List<LogisticsChannelEntity> logisticsChannelEntities;
    private Map<String, BasicQueryLogisticsProviderEntity> queryLogisticsProviderMap;
    private Map<String, String> shopMap ;
    private Map<String, String> dictMap;
    /**
     * 错误信息
     */
    @Getter
    private List<ImportLogisticsThirdChannelRefExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    private final List<ImportLogisticsThirdChannelRefExcelDTO> dataList = new ArrayList<>();

    /**
     * 成功信息
     */
    @Getter
    private List<ImportLogisticsThirdChannelRefExcelDTO> successList = new ArrayList<>();

    private final LogisticsThirdChannelRefService logisticsThirdChannelRefService = SpringUtil.getBean(LogisticsThirdChannelRefService.class);
    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    public LogisticsThirdChannelRefListener(String taskId,
                                            String importType,
                                            Integer importCount,
                                            List<LogisticsSupplierEntity> logisticsSupplierEntities,
                                            List<LogisticsChannelEntity> logisticsChannelEntities,
                                            Map<String, String> shopMap ,
                                            Map<String, String> dictMap,
                                            Map<String, BasicQueryLogisticsProviderEntity> queryLogisticsProviderMap) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
        this.logisticsSupplierEntities=logisticsSupplierEntities;
        this.logisticsChannelEntities=logisticsChannelEntities;
        this.shopMap=shopMap;
        this.dictMap=dictMap;
        this.queryLogisticsProviderMap=queryLogisticsProviderMap;

    }

   /**
    * @description: 每解析一行数据回调一遍
    * @author jack
    * @param excelDTO 导入信息
    * @param analysisContext
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(ImportLogisticsThirdChannelRefExcelDTO excelDTO, AnalysisContext analysisContext) {
        count += 1;
        //已经导入的数据跳过进度
        if (Objects.nonNull(importCount) && count < importCount) {
            return;
        }
        List<String> errorMsgList = new ArrayList<>();

        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        String mainDictPlatformName = excelDTO.getMainDictPlatformName();
        PlatformDictEnum platformDict = PlatformDictEnum.getByName(mainDictPlatformName);
        if (Objects.isNull(platformDict)) {
            errorMsgList.add("服务商不存在");
        } else {
            excelDTO.setMainDictPlatform(platformDict.getCode());
        }
        //查询服务商
        String platformType = TrackPlatformTypeEnum.getCode(excelDTO.getPlatformTypeName());
        if (CharSequenceUtil.isBlank(platformType)) {
            errorMsgList.add("服务商不存在");
        } else {
            excelDTO.setPlatformType(platformType);
        }

        //我司物流商合我司渠道
        String logisticsSupplierName = excelDTO.getLogisticsSupplierName();
        if (CharSequenceUtil.isBlank(logisticsSupplierName)) {
            errorMsgList.add("物流商不能为空");
        } else {
            if (Objects.equals("全部", logisticsSupplierName)) {
                excelDTO.setLogisticsChannelId("all");
            } else {
                LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierEntities.stream().filter(e -> Objects.equals(logisticsSupplierName, e.getSupplierName()) || Objects.equals(logisticsSupplierName, e.getShortName())).findFirst().orElse(null);
                if (Objects.isNull(logisticsSupplierEntity)) {
                    errorMsgList.add("物流商不存在");
                } else {
                    excelDTO.setLogisticsSupplierId(logisticsSupplierEntity.getId());
                }
            }
        }

        if (CharSequenceUtil.isNotBlank(excelDTO.getLogisticsSupplierId())) {
            String logisticsChannelName = excelDTO.getLogisticsChannelName();
            if (CharSequenceUtil.isBlank(logisticsChannelName)) {
                errorMsgList.add("物流商渠道不能为空");
            } else {
                if (Objects.equals("全部", logisticsChannelName)) {
                    excelDTO.setLogisticsChannelId("all");
                } else {
                    LogisticsChannelEntity logisticsChannelEntity =  logisticsChannelEntities.stream().filter(e ->e.getMainId().equals(excelDTO.getLogisticsSupplierId()) && e.getName().equals(logisticsChannelName)).findFirst().orElse(null);
                    if (Objects.isNull(logisticsChannelEntity)) {
                        errorMsgList.add("物流商渠道不存在");
                    }  else {
                        excelDTO.setLogisticsChannelId(logisticsChannelEntity.getId());
                    }
                }
            }
        }


        //查询物流商
        String thirdSupplierName = excelDTO.getThirdSupplierName();
        if (CharSequenceUtil.isBlank(thirdSupplierName)) {
            errorMsgList.add("查询物流商（中文）不能为空");
        } else if (CharSequenceUtil.isNotBlank(excelDTO.getPlatformType())) {
            BasicQueryLogisticsProviderEntity basicQueryLogisticsProviderEntity = queryLogisticsProviderMap.getOrDefault(thirdSupplierName + ":" + excelDTO.getPlatformType(), null);
            if (Objects.isNull(basicQueryLogisticsProviderEntity)) {
                errorMsgList.add("查询物流商不存在");
            } else if (basicQueryLogisticsProviderEntity.getIsRegisterPhone() && !Objects.equals(basicQueryLogisticsProviderEntity.getIsRegisterPhone(), excelDTO.getIsPushMobile())) {
                errorMsgList.add("该查询物流商【" + basicQueryLogisticsProviderEntity.getLogisticsNameCn() + "】必须填写手机号");
            } else if (!basicQueryLogisticsProviderEntity.getIsRegisterPhone()
                    && !Objects.equals(basicQueryLogisticsProviderEntity.getIsRegisterPhone(), excelDTO.getIsPushMobile())
                    && CharSequenceUtil.isBlank(excelDTO.getShopPhone())) {
                errorMsgList.add("推送电话为是则手机号不能为空");
            }
        }

        //推送类型
        String pushType = LogisticsThirdChannelRefPushTypeEnum.getCodeByName(excelDTO.getPushTypeName());
        if (Objects.nonNull(excelDTO.getIsPushMobile()) && CharSequenceUtil.isBlank(pushType)) {
            errorMsgList.add("推送类型不存在");
        } else {
            excelDTO.setPushType(pushType);
        }

        //是否推送电话
        if (CharSequenceUtil.isNotBlank(excelDTO.getPushMobileName()) && excelDTO.getPushMobileName().equals("是")) {
            excelDTO.setIsPushMobile(Boolean.TRUE);

            if (CharSequenceUtil.isNotBlank(excelDTO.getPushType()) && excelDTO.getIsPushMobile()) {
                if (LogisticsThirdChannelRefPushTypeEnum.SHOP_SENDER.getCode().equals(excelDTO.getPushType()) && excelDTO.getIsPushMobile()) {
                    String shopId = shopMap.get(excelDTO.getShopName());
                    if (CharSequenceUtil.isBlank(shopId)) {
                        errorMsgList.add(CharSequenceUtil.format("店铺【{}】未匹配到", excelDTO.getShopName()));
                    } else {
                        excelDTO.setShopId(shopId);
                    }
                } else if (LogisticsThirdChannelRefPushTypeEnum.PLATFORM_SENDER.getCode().equals(excelDTO.getPushType()) && excelDTO.getIsPushMobile()) {
                    if (CharSequenceUtil.isBlank(excelDTO.getShopName())) {
                        errorMsgList.add("平台不能为空");
                    }
                    String code = dictMap.get(excelDTO.getShopName());
                    if (CharSequenceUtil.isBlank(code)) {
                        errorMsgList.add(CharSequenceUtil.format("平台【{}】未匹配到", excelDTO.getShopName()));
                    } else {
                        excelDTO.setDictPlatform(code);
                    }
                } else if (LogisticsThirdChannelRefPushTypeEnum.SENDER.getCode().equals(excelDTO.getPushType()) || LogisticsThirdChannelRefPushTypeEnum.RECEIVER.getCode().equals(excelDTO.getPushType())) {
                    if (CharSequenceUtil.isBlank(excelDTO.getShopPhone()) && excelDTO.getIsPushMobile()) {
                        errorMsgList.add("手机号不能为空");
                    }
                }
            }
        } else if (CharSequenceUtil.isNotBlank(excelDTO.getPushMobileName()) && excelDTO.getPushMobileName().equals("否")) {
            excelDTO.setIsPushMobile(Boolean.FALSE);
        } else {
            errorMsgList.add("是否推送电话必须是是/否");
        }



        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        successList.add(excelDTO);
        dataList.add(excelDTO);
        if (successList.size() >= BATCH_COUNT) {
            try {
                List<String> errorNoList = errorList.stream().map(ImportLogisticsThirdChannelRefExcelDTO::getSerialNumber).distinct().collect(Collectors.toList());
                List<ImportLogisticsThirdChannelRefExcelDTO> errorList2 = new ArrayList<>();
                logisticsThirdChannelRefService.handleImportFile(successList, errorNoList, errorList2, importType);
                errorList.addAll(errorList2);
            } catch (Exception e) {
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

    public List<ImportLogisticsThirdChannelRefExcelDTO> getExcelDateList(){
        return dataList;
    }

    /**
     * @description: 数据全部解析完后删除明细
     * @param analysisContext
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!successList.isEmpty()){
            try {
                List<String> errorNoList = errorList.stream().map(ImportLogisticsThirdChannelRefExcelDTO::getSerialNumber).distinct().collect(Collectors.toList());
                List<ImportLogisticsThirdChannelRefExcelDTO> errorList2 = new ArrayList<>();
                logisticsThirdChannelRefService.handleImportFile(successList,errorNoList, errorList2,importType);
                errorList.addAll(errorList2);
            }catch (Exception e){
                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
    }
}
