package com.erp.server.oms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.dto.excel.KolFeedbackCostExcelDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.oms.service.KolFeedbackCostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * KOL回片费用Excel导入监听器
 * @author wuhaotian
 * @since 2025-12-03
 */
@Slf4j
public class KolFeedbackCostExcelListener extends AnalysisEventListener<KolFeedbackCostExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;
    private final String importType;
    private final Integer importCount;
    private final List<KolFeedbackCostExcelDTO> successList = new ArrayList<>();
    private final List<KolFeedbackCostExcelDTO> errorList = new ArrayList<>();
    private final List<String> errorNoList = new ArrayList<>();
    private int count = 0;

    private final KolFeedbackCostService kolFeedbackCostService = SpringUtil.getBean(KolFeedbackCostService.class);
    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    public KolFeedbackCostExcelListener(String taskId, String importType, Integer importCount) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(KolFeedbackCostExcelDTO data, AnalysisContext context) {
        count++;
        data.setRowNum(count);
        
        // 已经导入的数据跳过进度
        if (Objects.nonNull(importCount) && count < importCount) {
            return;
        }
        
        List<String> errorMsgList = new ArrayList<>();
        
        // 基础验证
        List<String> msgList = FieldValidUtil.fieldValid(data);
        if (CollUtil.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        
        // 设置创建人信息
        setCreateUserInfo(data);

        // 存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            data.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(data);
            return;
        }
        
        successList.add(data);
        if (successList.size() >= BATCH_COUNT) {
            try {
                List<KolFeedbackCostExcelDTO> errorList2 = new ArrayList<>();
                kolFeedbackCostService.handleImportSuccessList(successList, Collections.emptyList(), errorList2, importType);
                errorList.addAll(errorList2);
            } catch (Exception e) {
                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(
                        e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("KOL回片费用Excel解析完成，总行数：{}，成功：{}，失败：{}", count, successList.size(), errorList.size());
        
        if (!successList.isEmpty()) {
            try {
                List<KolFeedbackCostExcelDTO> errorList2 = new ArrayList<>();
                kolFeedbackCostService.handleImportSuccessList(successList, Collections.emptyList(), errorList2, importType);
                errorList.addAll(errorList2);
            } catch (Exception e) {
                successList.forEach(excelDTO1 -> excelDTO1.setErrorMsg(
                        e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage()));
                errorList.addAll(successList);
            }
            successList.clear();
            updateTask(count);
        }
    }

    /**
     * 设置创建人信息
     */
    private void setCreateUserInfo(KolFeedbackCostExcelDTO data) {
        try {
            LoginUser loginUser = UserContext.getLoginUser();
            if (loginUser != null) {
                data.setCreateUserId(loginUser.getUid());
                data.setCreateUserName(loginUser.getUserName());
            } else {
                // 如果获取不到当前用户，使用系统用户
                data.setCreateUserId("0");
                data.setCreateUserName("system");
            }
        } catch (Exception e) {
            log.warn("获取当前登录用户信息失败，使用系统用户：{}", e.getMessage());
            data.setCreateUserId("0");
            data.setCreateUserName("system");
        }
    }

    /**
     * 更新任务状态
     */
    private void updateTask(Integer count) {
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(taskId);
        importResultDTO.setStatus(FileTaskStatusEnum.PROCESS.getCode());
        importResultDTO.setRemark("处理中");
        importResultDTO.setCount(count);
        downloadTaskFeign.updateTask(importResultDTO);
    }

    public List<KolFeedbackCostExcelDTO> getSuccessList() {
        return successList;
    }

    public List<KolFeedbackCostExcelDTO> getErrorList() {
        return errorList;
    }

    public List<String> getErrorNoList() {
        return errorNoList;
    }

    public int getCount() {
        return count;
    }
}

