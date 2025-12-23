package com.erp.server.oms.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.dto.excel.KolFeedbackExcelDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.oms.service.KolFeedbackService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * KOL回片列表Excel导入监听器
 * @author wuhaotian
 * @since 2025-12-01
 */
@Slf4j
public class KolFeedbackExcelListener extends AnalysisEventListener<KolFeedbackExcelDTO> {

    private static final int BATCH_COUNT = 1000;

    private final String taskId;
    private final String importType;
    private final Integer importCount;
    private final List<KolFeedbackExcelDTO> successList = new ArrayList<>();
    private final List<KolFeedbackExcelDTO> errorList = new ArrayList<>();
    private final List<String> errorNoList = new ArrayList<>();
    private int count = 0;

    private final KolFeedbackService kolFeedbackService = SpringUtil.getBean(KolFeedbackService.class);
    private final DownloadTaskFeign downloadTaskFeign = SpringUtil.getBean(DownloadTaskFeign.class);

    public KolFeedbackExcelListener(String taskId, String importType, Integer importCount) {
        this.taskId = taskId;
        this.importType = importType;
        this.importCount = importCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invoke(KolFeedbackExcelDTO data, AnalysisContext context) {
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
        
        // 日期转换处理
        convertDateFields(data, errorMsgList);
        
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
                List<KolFeedbackExcelDTO> errorList2 = new ArrayList<>();
                kolFeedbackService.handleImportSuccessList(successList, Collections.emptyList(), errorList2, importType);
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
        log.info("KOL回片列表Excel解析完成，总行数：{}，成功：{}，失败：{}", count, successList.size(), errorList.size());
        
        if (!successList.isEmpty()) {
            try {
                List<KolFeedbackExcelDTO> errorList2 = new ArrayList<>();
                kolFeedbackService.handleImportSuccessList(successList, Collections.emptyList(), errorList2, importType);
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
     * 日期转换处理
     */
    private void convertDateFields(KolFeedbackExcelDTO data, List<String> errorMsgList) {
        // 发布日期转换
        String publishDateStr = data.getPublishDateStr();
        if (StringUtils.isNotBlank(publishDateStr)) {
            try {
                // 支持两种日期格式：yyyy-MM-dd 和 yyyy/M/d
                LocalDate publishDate = null;
                try {
                    publishDate = LocalDate.parse(publishDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                } catch (Exception e1) {
                    try {
                        publishDate = LocalDate.parse(publishDateStr, DateTimeFormatter.ofPattern("yyyy/M/d"));
                    } catch (Exception e2) {
                        throw new IllegalArgumentException("日期格式错误");
                    }
                }
                data.setPublishDate(publishDate);
            } catch (Exception e) {
                errorMsgList.add("发布日期格式错误，请使用yyyy-MM-dd或yyyy/M/d格式");
            }
        }
    }

    /**
     * 设置创建人信息
     */
    private void setCreateUserInfo(KolFeedbackExcelDTO data) {
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

    public List<KolFeedbackExcelDTO> getSuccessList() {
        return successList;
    }

    public List<KolFeedbackExcelDTO> getErrorList() {
        return errorList;
    }

    public List<String> getErrorNoList() {
        return errorNoList;
    }

    public int getCount() {
        return count;
    }
}

