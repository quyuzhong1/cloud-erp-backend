package com.erp.server.plm.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FileUtil;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.BusinessProcessEntity;
import com.erp.model.plm.entity.ProjectMembersEntity;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.TaskDocsFinishEntity;
import com.erp.model.workflow.dto.ProcessNodeDTO;
import com.erp.model.workflow.dto.StartProcessDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.enums.BusinessProcessEnum;
import com.erp.server.plm.enums.TaskProcessTypeEnum;
import com.erp.server.plm.enums.TaskStateEnum;
import com.erp.server.plm.enums.TaskTypeEnum;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.mapper.TaskDocsFinishMapper;
import com.erp.server.plm.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 任务文档交付表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Service
@Slf4j
public class TaskDocsFinishServiceImpl extends ServiceImpl<TaskDocsFinishMapper, TaskDocsFinishEntity> implements TaskDocsFinishService {


    @Autowired
    private CommonService commonService;

    @Autowired
    private ProjectTaskService projectTaskService;

    @Autowired
    private DocsChangeRecordService docsChangeRecordService;

    @Autowired
    private ProductOperateRecordService productOperateRecordService;

    @Autowired
    private BusinessProcessService businessProcessService;

    @Autowired
    private WorkflowFeign workflowFeign;

    @Autowired
    private TaskDeliveryService taskDeliveryService;

    @Autowired
    private ProjectMembersService projectMembersService;

    /**
     * 根据任务id 集合获取对应数据
     *
     * @param taskIds
     * @return java.util.List<com.erp.model.plm.entity.TaskDocsFinishEntity>
     * @author yl
     * @date 2022-09-22 10:45
     */
    @Override
    public List<TaskDocsFinishEntity> getByTaskIds(List<String> taskIds) {
        LambdaQueryWrapper<TaskDocsFinishEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (CollectionUtils.isNotEmpty(taskIds)) {
            queryWrapper.in(TaskDocsFinishEntity::getTaskId, taskIds);
            return list(queryWrapper);
        }
        return new ArrayList<>();
    }


    /**
     * 交付文档 上传文件
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-23 17:15
     */
    @Override
    @Transactional
    public Boolean uploadFile(TaskUploadFileDTO dto) {
        //根据任务id 获取任务信息
        ProjectTaskEntity taskEntity = projectTaskService.getById(dto.getTaskId());
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        LoginUser loginUser = commonService.getUserInfo();
        //文件名
        String fileName = "";
        //文件地址
        String fileUrl = "";
        //文件后缀
        String fileSuffix = "";
        double fileSize = 0.0;
        //本地上传
        if (IsConstant.NO.equals(dto.getUploadType())) {
            MultipartFile multipartFile = dto.getFile();
            double size = multipartFile.getSize();
            fileSize = size / (1024 * 1024);
            fileSize = (double) Math.round(fileSize * 100) / 100;
            fileName = dto.getFile().getOriginalFilename().toLowerCase();
            fileSuffix = FilenameUtils.getExtension(fileName).toLowerCase();
            File file = FileUtil.multiToFile(multipartFile);
            fileUrl = FastDFSClientUtil.uploadFile(file, fileName);
            if (StringUtils.isBlank(fileUrl)) {
                throw new ServiceException(ApiError.ERROR_95018);
            }
        } else {
            fileUrl = dto.getFileUrl();
        }

        TaskDocsFinishEntity finishEntity = new TaskDocsFinishEntity();
        finishEntity.setCreateUserName(loginUser.getUserName());
        finishEntity.setFileName(fileName);
        finishEntity.setProductId(dto.getProductId());
        finishEntity.setTaskDocsId(dto.getTaskDocsId());
        finishEntity.setTaskId(dto.getTaskId());
        finishEntity.setFileUrl(fileUrl);
        finishEntity.setCreateUserId(loginUser.getUid());
        finishEntity.setFileType(TaskConstant.FILE_TYPE);
        finishEntity.setFileSuffix(fileSuffix);
        finishEntity.setFileSize(fileSize);
        finishEntity.setUploadType(dto.getUploadType());
        finishEntity.setOldFileUrl(fileUrl);
        finishEntity.setOldUploadType(dto.getUploadType());
        //新增产品操作日志
        ProductOperateRecordDTO productOperateRecordDTO = new ProductOperateRecordDTO();
        productOperateRecordDTO.setProductId(dto.getProductId());
        List<String> remarkList = new ArrayList<>();
        remarkList.add("上传文件：[" + fileName + "]");
        productOperateRecordDTO.setRemark(JSONObject.toJSONString(remarkList));
        productOperateRecordService.saveOrUpdate(productOperateRecordDTO);


        return this.save(finishEntity);
    }

    /**
     * 项目任务-任务详情-删除文件
     *
     * @param id 主键
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2022/10/14 16:08
     **/
    @Override
    public Boolean removeDocs(String id) {
        //删除文档
        //需要判断能否删除
        TaskDocsFinishEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.ERROR_95028);
        }
        String taskId = entity.getTaskId();
        ProjectTaskEntity taskEntity = projectTaskService.getById(taskId);
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException(ApiError.ERROR_95040);
        }
        Integer taskState = taskEntity.getStatus();
        //如果已完成了 或者有人审核了 就不能删除
        Integer taskProperty = projectTaskService.getTaskProperty(taskEntity);
        Integer generalApproval = TaskProcessTypeEnum.GENERAL_APPROVAL_TASK.getCode();
        Integer reviewTask = TaskProcessTypeEnum.REVIEW_TASK.getCode();
        //当是流程的时候
        if (generalApproval.equals(taskProperty) || reviewTask.equals(taskProperty)) {
            if (TaskStateEnum.APPROVAL_PASS.getCode().equals(taskState)) {
                throw new ServiceException(ApiError.ERROR_95007);
            }
        }

        //新增产品操作日志
        ProductOperateRecordDTO productOperateRecordDTO = new ProductOperateRecordDTO();
        productOperateRecordDTO.setProductId(entity.getProductId());
        List<String> remarkList = new ArrayList<>();
        remarkList.add("删除文件：[" + entity.getFileName() + "]");
        productOperateRecordDTO.setRemark(JSONObject.toJSONString(remarkList));
        productOperateRecordService.saveOrUpdate(productOperateRecordDTO);
        return this.removeById(id);
    }

    @Override
    public List<CountDTO> getTaskDocsCountByProductId() {
        return baseMapper.getTaskDocsCountByProductId();
    }

    /**
     * 变更文档
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-10-14 11:52
     */
    @Override
    @Transactional
    public Boolean changeFile(TaskChangeFileDTO dto) {
        String taskId = dto.getTaskId();
        ProjectTaskEntity taskEntity = projectTaskService.getById(taskId);
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        //只有任务完成了 或者 审核通过了  或者审核不通过才能变更流程
        Integer finishCode = TaskStateEnum.FINISH.getCode();
        Integer approvalPassCode = TaskStateEnum.APPROVAL_PASS.getCode();
        Integer approvalNoPassCode = TaskStateEnum.APPROVAL_NO_PASS.getCode();
        Integer taskState = taskEntity.getStatus();
        //当不为这两个的时候是不能变更的
        if (!taskState.equals(finishCode) && !approvalPassCode.equals(taskState)
        &&!approvalNoPassCode.equals(taskState)) {
            throw new ServiceException(ApiError.ERROR_95039);
        }
        String finishDocsId = dto.getFinishDocsId();
        TaskDocsFinishEntity finishEntity = this.getById(finishDocsId);
        if (Objects.isNull(finishEntity)) {
            throw new ServiceException(ApiError.ERROR_95028);
        }
        LoginUser loginUser = commonService.getUserInfo();
        String originalFileName = finishEntity.getFileName();

        //文件名
        String fileName = "";
        //文件地址
        String fileUrl = "";
        //文件后缀
        String fileSuffix = "";
        double fileSize = 0.0;
        //本地上传
        if (IsConstant.NO.equals(dto.getUploadType())) {
            MultipartFile multipartFile = dto.getFile();
            double size = multipartFile.getSize();
            fileSize = size / (1024 * 1024);
            fileSize = (double) Math.round(fileSize * 100) / 100;
            fileName = dto.getFile().getOriginalFilename().toLowerCase();
            fileSuffix = FilenameUtils.getExtension(fileName).toLowerCase();
            File file = FileUtil.multiToFile(multipartFile);
            fileUrl = FastDFSClientUtil.uploadFile(file, fileName);
            if (StringUtils.isBlank(fileUrl)) {
                throw new ServiceException(ApiError.ERROR_95018);
            }
        } else {
            fileUrl = dto.getFileUrl();
        }
        finishEntity.setOldFileUrl(finishEntity.getFileUrl());
        finishEntity.setOldUploadType(finishEntity.getUploadType());

        finishEntity.setFileName(fileName);
        finishEntity.setFileUrl(fileUrl);
        finishEntity.setUpdateUserId(loginUser.getUid());
        finishEntity.setFileSuffix(fileSuffix);
        finishEntity.setUpdateUserName(loginUser.getUserName());
        finishEntity.setFileSize(fileSize);
        finishEntity.setUpdateUserId(loginUser.getUid());
        finishEntity.setUpdateUserName(loginUser.getUserName());

        StringBuffer sb = new StringBuffer(originalFileName);
        Boolean flag = this.updateById(finishEntity);
        //当更新成功后 保存记录
        if (flag) {
            sb.append("变更为").append(fileName);
            docsChangeRecordService.addRecord(sb.toString(), finishEntity.getTaskId(), finishDocsId, "");
        }

        //新增产品操作日志
        ProductOperateRecordDTO productOperateRecordDTO = new ProductOperateRecordDTO();
        productOperateRecordDTO.setProductId(finishEntity.getProductId());
        List<String> remarkList = new ArrayList<>();
        remarkList.add("变更文档：[" + fileName + "]");
        productOperateRecordDTO.setRemark(JSONObject.toJSONString(remarkList));
        productOperateRecordService.saveOrUpdate(productOperateRecordDTO);

        //这里需要启动一个变更流程
        BusinessProcessEntity businessProcess = businessProcessService.getProcessByBusinessType(BusinessProcessEnum.DOCS_CHANGE.getBusinessType());
        StartProcessDTO startProcess = new StartProcessDTO();
        startProcess.setUserId(loginUser.getUid());
        startProcess.setProcessDefinitionKey(businessProcess.getProcessDefinitionKey());
        startProcess.setBusinessKey(businessProcess.getBusinessType());
        Map<String, Object> parameterMap = new HashMap<>();
        List<ProjectMembersEntity> projectMembersList = projectMembersService.getChargeList(taskEntity.getProductId());
        List<String> membersIds = projectMembersList.stream().map(ProjectMembersEntity::getMemberId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(membersIds)) {
            throw new ServiceException(ApiError.ERROR_95045);
        }
        parameterMap.put("memberChargeList", membersIds);
        startProcess.setParameterMap(parameterMap);
        ProcessNodeDTO processResult = workflowFeign.startProcess(startProcess);
        String processId = processResult.getProcessId();
        if (StringUtils.isNotBlank(processId)) {
            //更改任务的状态为未待审核 以及流程id
            taskEntity.setProcessId(processId);
            if(taskEntity.getType().equals(TaskTypeEnum.GENERAL_TASK.getCode())){
                taskEntity.setStatus(TaskStateEnum.FINISH_WAIT_CONFIRM.getCode());
            }else{
                taskEntity.setStatus(TaskStateEnum.WAIT_CONFIRM.getCode());
            }
            projectTaskService.updateById(taskEntity);
        }
        return flag;
    }


    /**
     * 检查任务是否有上传文档
     *
     * @param allTaskIds
     * @return void
     * @author yl
     * @date 2022-10-24 18:09
     */
    @Override
    public void checkTaskDocsUpload(List<String> allTaskIds) {
        for (String taskId : allTaskIds) {
            //获取到该任务要上交的文档
            List<DocsDTO> docsList = taskDeliveryService.getDocsByTaskId(taskId);
            //获取到该任务完成的文档数
            int finishDocsNum = getFinishDocsNum(taskId);
            if (docsList.size() != finishDocsNum) {
                throw new ServiceException(ApiError.ERROR_95047);
            }

        }

    }


    /**
     * 刪除完成的文档
     *
     * @param existDocsIds
     * @return void
     * @author yl
     * @date 2022-10-25 11:06
     */
    @Override
    public void removeByDocsIds(String taskId, List<String> existDocsIds) {
        if (CollectionUtils.isNotEmpty(existDocsIds)) {
            LambdaQueryWrapper<TaskDocsFinishEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(TaskDocsFinishEntity::getTaskDocsId, existDocsIds);
            queryWrapper.eq(TaskDocsFinishEntity::getTaskId, taskId);
            this.remove(queryWrapper);
        }


    }


    public int getFinishDocsNum(String taskId) {
        LambdaQueryWrapper<TaskDocsFinishEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskDocsFinishEntity::getTaskId, taskId);
        return this.count(queryWrapper);

    }


}
