package com.erp.server.wms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.excel.SampleAdjustmentImportExcelDTO;
import com.erp.model.wms.enums.SampleAdjustmentTypeEnum;
import com.erp.model.wms.enums.SampleUsageScopeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.service.SampleAdjustmentInfoService;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 样品调整单Excel导入监听器
 * @author wuhaotian
 * @Classname SampleAdjustmentExcelListener
 * @Date 2025-11-14
 */
@Slf4j
public class SampleAdjustmentExcelListener extends AnalysisEventListener<SampleAdjustmentImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;

    //sku信息
    private Map<String, SkuVO> skuMap;
    //用户
    private List<FindUserDTO> userList;
    //部门
    private List<SysDepartmentDTO> deptList;

    private final SampleAdjustmentInfoService sampleAdjustmentInfoService = SpringUtil.getBean(SampleAdjustmentInfoService.class);

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    private final SysUserFeign sysUserFeign = SpringUtil.getBean(SysUserFeign.class);

    /**
     * 错误信息
     */
    @Getter
    private List<SampleAdjustmentImportExcelDTO> errorList = new ArrayList<>();

    @Getter
    private List<SampleAdjustmentImportExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    public SampleAdjustmentExcelListener(String taskId,
                                         String importType,
                                         Integer importCount,
                                         List<SysDepartmentDTO> deptList,
                                         Map<String, SkuVO> skuMap,
                                         List<FindUserDTO> userList) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
        this.skuMap = skuMap;
        this.deptList = deptList;
        this.userList = userList;
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
     * @author wuhaotian
     * @date 2025-11-14
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(SampleAdjustmentImportExcelDTO excelDTO, AnalysisContext analysisContext) {
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

        //调整人
        String adjustmentUserName = excelDTO.getAdjustmentUserName();
        if (StringUtils.isNotBlank(adjustmentUserName)) {
            FindUserDTO findUserDTO = userList.stream().filter(e -> adjustmentUserName.equals(e.getUserName())).findFirst().orElse(null);
            if (Objects.isNull(findUserDTO)) {
                errorMsgList.add("调整人不存在");
            } else {
                excelDTO.setAdjustmentUserId(findUserDTO.getUserId());
                excelDTO.setAdjustmentUserName(findUserDTO.getUserName());
            }
        }

        //调整部门
        String adjustmentDeptName = excelDTO.getAdjustmentDeptName();
        if (StringUtils.isNotBlank(adjustmentDeptName)) {
            SysDepartmentDTO sysDepartmentDTO = deptList.stream().filter(e -> adjustmentDeptName.equals(e.getName())).findFirst().orElse(null);
            if (Objects.isNull(sysDepartmentDTO)) {
                errorMsgList.add("调整部门不存在");
            } else {
                excelDTO.setAdjustmentDeptId(sysDepartmentDTO.getId());
                excelDTO.setAdjustmentDeptName(sysDepartmentDTO.getName());
            }
        }

        //调整日期
        String adjustmentDateStr = excelDTO.getAdjustmentDateStr();
        if (StringUtils.isNotBlank(adjustmentDateStr)) {
            LocalDate adjustmentDate = null;
            try {
                adjustmentDate = LocalDate.parse(adjustmentDateStr, dateTimeFormatter);
            } catch (Exception e1) {
                try {
                    adjustmentDate = LocalDate.parse(adjustmentDateStr, dateTimeFormatter2);
                } catch (Exception e2) {
                    try {
                        adjustmentDate = LocalDate.parse(adjustmentDateStr, dateTimeFormatter3);
                    } catch (Exception e3) {
                        errorMsgList.add("调整日期格式错误，请使用 yyyy-MM-dd、yyyy/M/d 或 yyyy/MM/dd 格式");
                    }
                }
            }
            excelDTO.setAdjustmentDate(adjustmentDate);
        }

        //调整类型
        String adjustmentTypeName = excelDTO.getAdjustmentTypeName();
        if (StringUtils.isNotBlank(adjustmentTypeName)) {
            SampleAdjustmentTypeEnum adjustmentTypeEnum = null;
            for (SampleAdjustmentTypeEnum typeEnum : SampleAdjustmentTypeEnum.values()) {
                if (adjustmentTypeName.equals(typeEnum.getName())) {
                    adjustmentTypeEnum = typeEnum;
                    break;
                }
            }
            if (Objects.isNull(adjustmentTypeEnum)) {
                errorMsgList.add("调整类型不存在，可选值：盘盈、盘亏、其他");
            } else {
                excelDTO.setAdjustmentType(adjustmentTypeEnum.getCode());
            }
        }

        //sku
        String skuNo = excelDTO.getSkuNo();
        if (StringUtils.isNotBlank(skuNo)) {
            SkuVO skuVO = skuMap.getOrDefault(skuNo, null);
            if (Objects.isNull(skuVO)) {
                errorMsgList.add("SKU不存在");
            } else {
                excelDTO.setSkuId(skuVO.getSkuId());
                excelDTO.setSkuNo(skuVO.getSkuNo());
                excelDTO.setProductName(skuVO.getSkuName());
            }
        }

        //实际数量
        String actualQty = excelDTO.getActualQty();
        if (StringUtils.isNotBlank(actualQty)) {
            try {
                excelDTO.setActualQtyInt(Integer.valueOf(actualQty));
            } catch (Exception e) {
                errorMsgList.add("实际数量格式错误，必须为整数");
            }
        }

        //处理使用方逻辑
        String useUserName = excelDTO.getUseUserName();
        if (StringUtils.isNotBlank(useUserName)) {
            // 先尝试从普通用户中查找（内部使用）
            FindUserDTO useUser = userList.stream()
                    .filter(e -> useUserName.equals(e.getUserName()))
                    .findFirst()
                    .orElse(null);
            
            if (useUser != null) {
                // 公司内部使用：使用方名称和ID都用调整人的
                excelDTO.setUseUserName(excelDTO.getAdjustmentUserName());
                excelDTO.setUseUserId(excelDTO.getAdjustmentUserId());
            } else {
                // 如果普通用户中找不到，查询外部使用方
                String useUserId = getSampleUseUserIdByName(useUserName);
                if (StringUtils.isBlank(useUserId)) {
                    errorMsgList.add(CharSequenceUtil.format("未知使用方:{}", useUserName));
                } else {
                    excelDTO.setUseUserId(useUserId);
                }
            }
        }

        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        successList.add(excelDTO);
        if (successList.size() >= BATCH_COUNT) {
            try {
                List<String> errorNoList = errorList.stream().map(SampleAdjustmentImportExcelDTO::getNo).distinct().collect(Collectors.toList());
                List<SampleAdjustmentImportExcelDTO> errorList2 = new ArrayList<>();
                sampleAdjustmentInfoService.handleImportSuccessList(successList, errorNoList, errorList2, importType);
                errorList.addAll(errorList2);
            } catch (Exception e) {
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
        if (!successList.isEmpty()) {
            try {
                List<String> errorNoList = errorList.stream().map(SampleAdjustmentImportExcelDTO::getNo).distinct().collect(Collectors.toList());
                List<SampleAdjustmentImportExcelDTO> errorList2 = new ArrayList<>();
                sampleAdjustmentInfoService.handleImportSuccessList(successList, errorNoList, errorList2, importType);
                errorList.addAll(errorList2);
            } catch (Exception e) {
                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
    }

    /**
     * 根据使用方名称查询使用方ID（外部使用）
     */
    private String getSampleUseUserIdByName(String useUserName) {
        if (StringUtils.isBlank(useUserName)) {
            return null;
        }

        try {
            // 调用feign接口查询
            List<String> nameList = Collections.singletonList(useUserName);
            com.common.core.controller.vo.ApiResult<List<com.erp.model.sys.dto.SampleUseUserDTO.ViewDTO>> result = 
                sysUserFeign.getSampleUseUserListByNameList(nameList);
            
            if (result != null && result.isSuccess() && CollectionUtils.isNotEmpty(result.getData())) {
                com.erp.model.sys.dto.SampleUseUserDTO.ViewDTO user = result.getData().get(0);
                return user.getId();
            }
            
            return null;
        } catch (Exception e) {
            log.error("查询使用方ID失败，使用方名称：{}，错误：{}", useUserName, e.getMessage(), e);
            return null;
        }
    }

    private void updateTask(Integer count) {
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(taskId);
        importResultDTO.setStatus(FileTaskStatusEnum.PROCESS.getCode());
        importResultDTO.setRemark("处理中");
        importResultDTO.setCount(count);
        downloadTaskFeign.updateTask(importResultDTO);
    }
}

