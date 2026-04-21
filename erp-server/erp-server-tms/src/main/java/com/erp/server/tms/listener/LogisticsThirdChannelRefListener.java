package com.erp.server.tms.listener;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
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
    private Map<String, String> shopMap;
    private Map<String, String> dictMap;
    /**
     * 错误信息
     */
    @Getter
    private List<ImportLogisticsThirdChannelRefExcelDTO> errorList = new ArrayList<>();
    /**
     * 全部数据（用于判断导入是否为空）
     */
    // private final List<ImportLogisticsThirdChannelRefExcelDTO> dataList = new
    // ArrayList<>();

    /**
     * 成功信息
     */
    @Getter
    private List<ImportLogisticsThirdChannelRefExcelDTO> successList = new ArrayList<>();

    private final LogisticsThirdChannelRefService logisticsThirdChannelRefService = SpringUtil
            .getBean(LogisticsThirdChannelRefService.class);
    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    public LogisticsThirdChannelRefListener(String taskId,
            String importType,
            Integer importCount,
            List<LogisticsSupplierEntity> logisticsSupplierEntities,
            List<LogisticsChannelEntity> logisticsChannelEntities,
            Map<String, String> shopMap,
            Map<String, String> dictMap,
            Map<String, BasicQueryLogisticsProviderEntity> queryLogisticsProviderMap) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
        this.logisticsSupplierEntities = logisticsSupplierEntities;
        this.logisticsChannelEntities = logisticsChannelEntities;
        this.shopMap = shopMap;
        this.dictMap = dictMap;
        this.queryLogisticsProviderMap = queryLogisticsProviderMap;

    }

    /**
     * @description: 每解析一行数据回调一遍
     * @author jack
     * @param excelDTO        导入信息
     * @param analysisContext
     */
    @Override
    public void invoke(ImportLogisticsThirdChannelRefExcelDTO excelDTO, AnalysisContext analysisContext) {
        count += 1;
        // 已经导入的数据跳过进度
        if (Objects.nonNull(importCount) && count < importCount) {
            return;
        }
        List<String> errorMsgList = new ArrayList<>();

        // 1. 基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        // 2. 平台与字典校验
        validatePlatform(excelDTO, errorMsgList);

        // 3. 查询服务商校验
        String platformType = TrackPlatformTypeEnum.getCode(excelDTO.getPlatformTypeName());
        if (CharSequenceUtil.isBlank(platformType)) {
            errorMsgList.add("服务商不存在");
        } else {
            excelDTO.setPlatformType(platformType);
        }

        // 4. 物流商与渠道校验 (处理 "全部" 情况)
        validateLogisticsAndChannel(excelDTO, errorMsgList);

        // 5. 查询物流商实体查找
        BasicQueryLogisticsProviderEntity provider = findLogisticsProvider(excelDTO, errorMsgList);

        // 6. 推送设置与手机号校验 (修复 NPE 和逻辑顺序)
        validatePushAndPhoneSettings(excelDTO, provider, errorMsgList);

        // 7. 处理校验结果
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }

        successList.add(excelDTO);
        // 分批处理
        if (successList.size() >= BATCH_COUNT) {
            handleBatchImport();
        }
    }

    private void validatePlatform(ImportLogisticsThirdChannelRefExcelDTO excelDTO, List<String> errorMsgList) {
        String mainDictPlatformName = excelDTO.getMainDictPlatformName();
        String platformDict = dictMap.getOrDefault(mainDictPlatformName,"");

        if (StringUtils.isBlank(platformDict)) {
            errorMsgList.add("平台不存在");
        } else {
            excelDTO.setMainDictPlatform(platformDict);
        }
    }

    private void validateLogisticsAndChannel(ImportLogisticsThirdChannelRefExcelDTO excelDTO, List<String> errorMsgList) {
        String supplierName = excelDTO.getLogisticsSupplierName();
        if (CharSequenceUtil.isBlank(supplierName)) {
            errorMsgList.add("物流商不能为空");
        } else if ("全部".equals(supplierName)) {
            excelDTO.setLogisticsSupplierId("all");
        } else {
            logisticsSupplierEntities.stream()
                    .filter(e -> Objects.equals(supplierName, e.getSupplierName())
                            || Objects.equals(supplierName, e.getShortName()))
                    .findFirst()
                    .ifPresent(e -> excelDTO.setLogisticsSupplierId(e.getId()));
            if (excelDTO.getLogisticsSupplierId() == null) {
                errorMsgList.add("物流商不存在");
            }
        }

        if (CharSequenceUtil.isNotBlank(excelDTO.getLogisticsSupplierId())) {
            String channelName = excelDTO.getLogisticsChannelName();
            if (CharSequenceUtil.isBlank(channelName)) {
                errorMsgList.add("物流商渠道不能为空");
            } else if ("全部".equals(channelName)) {
                excelDTO.setLogisticsChannelId("all");
            } else {
                logisticsChannelEntities.stream()
                        .filter(e -> e.getMainId().equals(excelDTO.getLogisticsSupplierId()) && e.getName().equals(channelName))
                        .findFirst()
                        .ifPresent(e -> excelDTO.setLogisticsChannelId(e.getId()));
                if (excelDTO.getLogisticsChannelId() == null) {
                    errorMsgList.add("物流商渠道不存在");
                }
            }
        }
    }

    private BasicQueryLogisticsProviderEntity findLogisticsProvider(ImportLogisticsThirdChannelRefExcelDTO excelDTO,
            List<String> errorMsgList) {
        String thirdSupplierName = excelDTO.getThirdSupplierName();
        if (CharSequenceUtil.isBlank(thirdSupplierName)) {
            errorMsgList.add("查询物流商（中文）不能为空");
            return null;
        }
        if (CharSequenceUtil.isNotBlank(excelDTO.getPlatformType())) {
            BasicQueryLogisticsProviderEntity provider = queryLogisticsProviderMap
                    .get(thirdSupplierName + ":" + excelDTO.getPlatformType());
            if (Objects.isNull(provider)) {
                errorMsgList.add("查询物流商不存在");
            }
            return provider;
        }
        return null;
    }

    private void validatePushAndPhoneSettings(ImportLogisticsThirdChannelRefExcelDTO excelDTO,
            BasicQueryLogisticsProviderEntity provider, List<String> errorMsgList) {
        // 先初始化 IsPushMobile 状态，解决后续判断失效问题
        if ("是".equals(excelDTO.getPushMobileName())) {
            excelDTO.setIsPushMobile(Boolean.TRUE);
        } else if ("否".equals(excelDTO.getPushMobileName())) {
            excelDTO.setIsPushMobile(Boolean.FALSE);
        } else {
            errorMsgList.add("是否推送电话必须是是/否");
            return;
        }

        // 推送类型解析
        String pushType = LogisticsThirdChannelRefPushTypeEnum.getCodeByName(excelDTO.getPushTypeName());
        if (CharSequenceUtil.isNotBlank(pushType)) {
            errorMsgList.add("推送类型不存在");
        }
        excelDTO.setPushType(pushType);

        if (Boolean.TRUE.equals(excelDTO.getIsPushMobile())) {
            // 推送开启时的业务校验
            if (CharSequenceUtil.isNotBlank(excelDTO.getPushType())) {
                if (LogisticsThirdChannelRefPushTypeEnum.SHOP_SENDER.getCode().equals(excelDTO.getPushType())) {
                    String shopId = shopMap.get(excelDTO.getShopName());
                    if (CharSequenceUtil.isBlank(shopId)) {
                        errorMsgList.add(CharSequenceUtil.format("店铺【{}】未匹配到", excelDTO.getShopName()));
                    } else {
                        excelDTO.setShopId(shopId);
                    }
                } else if (LogisticsThirdChannelRefPushTypeEnum.SENDER.getCode().equals(excelDTO.getPushType())
                        || LogisticsThirdChannelRefPushTypeEnum.RECEIVER.getCode().equals(excelDTO.getPushType())) {
                    if (CharSequenceUtil.isBlank(excelDTO.getShopPhone())) {
                        errorMsgList.add("手机号不能为空");
                    }
                }
            }
            // 兜底手机号校验 (即使推送类型未知，只要开启推送就要求手机号)
            if (Objects.nonNull(provider) && StringUtils.isBlank(excelDTO.getShopPhone())) {
                errorMsgList.add("该查询物流商【" + provider.getLogisticsNameCn() + "】必须填写手机号");
            }
        } else {
            // “否”的情况：仅在该查询物流商强制要求注册手机时校验
            if (Objects.nonNull(provider) && provider.getIsRegisterPhone() && CharSequenceUtil.isBlank(excelDTO.getShopPhone())) {
                errorMsgList.add("该查询物流商【" + provider.getLogisticsNameCn() + "】必须填写手机号");
            }
        }
    }

    private void handleBatchImport() {
        try {
            List<String> errorNoList = errorList.stream()
                    .map(ImportLogisticsThirdChannelRefExcelDTO::getSerialNumber).distinct()
                    .collect(Collectors.toList());
            List<ImportLogisticsThirdChannelRefExcelDTO> errorList2 = new ArrayList<>();
            logisticsThirdChannelRefService.handleImportFile(successList, errorNoList, errorList2, importType);
            errorList.addAll(errorList2);
        } catch (Exception e) {
            successList.forEach(
                    item -> item.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
            errorList.addAll(successList);
        }
        successList.clear();
        updateTask(count);
    }

    private void updateTask(Integer count) {
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(taskId);
        importResultDTO.setStatus(FileTaskStatusEnum.PROCESS.getCode());
        importResultDTO.setRemark("处理中");
        importResultDTO.setCount(count);
        downloadTaskFeign.updateTask(importResultDTO);
    }

    // public List<ImportLogisticsThirdChannelRefExcelDTO> getExcelDateList(){
    // return dataList;
    // }

    /**
     * @description: 数据全部解析完后删除明细
     * @param analysisContext
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!successList.isEmpty()) {
            try {
                List<String> errorNoList = errorList.stream()
                        .map(ImportLogisticsThirdChannelRefExcelDTO::getSerialNumber).distinct()
                        .collect(Collectors.toList());
                List<ImportLogisticsThirdChannelRefExcelDTO> errorList2 = new ArrayList<>();
                logisticsThirdChannelRefService.handleImportFile(successList, errorNoList, errorList2, importType);
                errorList.addAll(errorList2);
            } catch (Exception e) {
                successList.forEach(excelDTO1 -> excelDTO1
                        .setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
    }
}
