package com.erp.server.wms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.excel.SampleTransferImportExcelDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.service.SampleTransferInfoService;
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
 * 样品转移单Excel监听器
 * @author wuhaotian
 * @Date 2025-10-28
 */
public class SampleTransferInfoExcelListener extends AnalysisEventListener<SampleTransferImportExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;

    private final String importType;

    private final Integer importCount;

    @Getter
    private Integer count = 0;

    // SKU信息
    private Map<String, SkuVO> skuMap;
    // 用户
    private List<FindUserDTO> userList;
    // 部门
    private List<SysDepartmentDTO> deptList;

    private final SampleTransferInfoService sampleTransferInfoService = SpringUtil.getBean(SampleTransferInfoService.class);

    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    /**
     * 错误信息
     */
    @Getter
    private List<SampleTransferImportExcelDTO> errorList = new ArrayList<>();

    @Getter
    private List<SampleTransferImportExcelDTO> successList = new ArrayList<>(BATCH_COUNT);

    public SampleTransferInfoExcelListener(String taskId,
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
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(SampleTransferImportExcelDTO excelDTO, AnalysisContext analysisContext) {
        count += 1;
        // 已经导入的数据跳过进度
        if (Objects.nonNull(importCount) && count < importCount) {
            return;
        }

        List<String> errorMsgList = new ArrayList<>();
        // 基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }

        // 转入人
        String transferInUserName = excelDTO.getTransferInUserName();
        if (StringUtils.isNotBlank(transferInUserName)) {
            FindUserDTO findUserDTO = userList.stream()
                    .filter(e -> transferInUserName.equals(e.getUserName()))
                    .findFirst()
                    .orElse(null);
            if (Objects.isNull(findUserDTO)) {
                errorMsgList.add("转入人不存在");
            } else {
                excelDTO.setTransferInUserId(findUserDTO.getUserId());
                excelDTO.setTransferInUserName(findUserDTO.getUserName());
            }
        }

        // 转出人
        String transferOutUserName = excelDTO.getTransferOutUserName();
        if (StringUtils.isNotBlank(transferOutUserName)) {
            FindUserDTO transferOutUser = userList.stream()
                    .filter(e -> transferOutUserName.equals(e.getUserName()))
                    .findFirst()
                    .orElse(null);
            if (Objects.isNull(transferOutUser)) {
                errorMsgList.add("转出人不存在");
            } else {
                excelDTO.setTransferOutUserId(transferOutUser.getUserId());
                excelDTO.setTransferOutUserName(transferOutUser.getUserName());
            }
        }

        // 转移日期
        String transferDateStr = excelDTO.getTransferDateStr();
        if (StringUtils.isNotBlank(transferDateStr)) {
            LocalDate transferDate = null;
            try {
                transferDate = LocalDate.parse(transferDateStr, dateTimeFormatter);
            } catch (Exception e1) {
                try {
                    transferDate = LocalDate.parse(transferDateStr, dateTimeFormatter2);
                } catch (Exception e2) {
                    try {
                        transferDate = LocalDate.parse(transferDateStr, dateTimeFormatter3);
                    } catch (Exception e3) {
                        errorMsgList.add("转移日期格式错误，请使用 yyyy-MM-dd、yyyy/M/d 或 yyyy/MM/dd 格式");
                    }
                }
            }
            excelDTO.setTransferDate(transferDate);
        }

        // 转入部门
        String transferInDeptName = excelDTO.getTransferInDeptName();
        if (StringUtils.isNotBlank(transferInDeptName)) {
            SysDepartmentDTO sysDepartmentDTO = deptList.stream()
                    .filter(e -> transferInDeptName.equals(e.getName()))
                    .findFirst()
                    .orElse(null);
            if (Objects.isNull(sysDepartmentDTO)) {
                errorMsgList.add("转入部门不存在");
            } else {
                excelDTO.setTransferInDeptId(sysDepartmentDTO.getId());
                excelDTO.setTransferInDeptName(sysDepartmentDTO.getName());
            }
        }

        // 转出部门
        String transferOutDeptName = excelDTO.getTransferOutDeptName();
        if (StringUtils.isNotBlank(transferOutDeptName)) {
            SysDepartmentDTO transferOutDept = deptList.stream()
                    .filter(e -> transferOutDeptName.equals(e.getName()))
                    .findFirst()
                    .orElse(null);
            if (Objects.isNull(transferOutDept)) {
                errorMsgList.add("转出部门不存在");
            } else {
                excelDTO.setTransferOutDeptId(transferOutDept.getId());
                excelDTO.setTransferOutDeptName(transferOutDept.getName());
            }
        }

        // SKU
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

        // 存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
        
        successList.add(excelDTO);
        if (successList.size() >= BATCH_COUNT) {
            try {
                List<String> errorNoList = errorList.stream()
                        .map(SampleTransferImportExcelDTO::getNo)
                        .distinct()
                        .collect(Collectors.toList());
                List<SampleTransferImportExcelDTO> errorList2 = new ArrayList<>();
                sampleTransferInfoService.handleImportSuccessList(successList, errorNoList, errorList2, importType);
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
                List<String> errorNoList = errorList.stream()
                        .map(SampleTransferImportExcelDTO::getNo)
                        .distinct()
                        .collect(Collectors.toList());
                List<SampleTransferImportExcelDTO> errorList2 = new ArrayList<>();
                sampleTransferInfoService.handleImportSuccessList(successList, errorNoList, errorList2, importType);
                errorList.addAll(errorList2);
            } catch (Exception e) {
                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
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

