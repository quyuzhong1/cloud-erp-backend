package com.erp.server.plm.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.common.business.utils.FastDFSClientUtil;
import com.common.core.utils.FileUtil;
import com.common.core.utils.MathUtil;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.*;
import com.erp.model.workflow.dto.ProcessNodeDTO;
import com.erp.model.workflow.dto.StartProcessDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.constant.TaskConstant;
import com.erp.server.plm.enums.*;
import com.erp.server.plm.mapper.TaskDocsFinishMapper;
import com.erp.server.plm.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
    @Lazy
    private NoticeMessageService noticeMessageService;
    
    @Autowired
    private SysLogService sysLogService;

    @Autowired
    private TaskChargeDistributionService taskChargeDistributionService;

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
        List<String> fileNames = new ArrayList<>();
        //文件后缀
        String fileSuffix = "";
        double fileSize = 0.0;
        TaskDocsFinishEntity finishEntity = new TaskDocsFinishEntity();
        finishEntity.setCreateUserName(loginUser.getUserName());
        finishEntity.setFileName(fileName);
        finishEntity.setProductId(dto.getProductId());
        finishEntity.setTaskId(dto.getTaskId());
        finishEntity.setCreateUserId(loginUser.getUid());
        finishEntity.setFileType(TaskConstant.FILE_TYPE);
        finishEntity.setFileSuffix(fileSuffix);
        finishEntity.setFileSize(fileSize);
        finishEntity.setOldUploadType(dto.getUploadType());
        finishEntity.setOldFileName(fileName);
        List<TaskDocsFinishEntity> resultList = new ArrayList<>();
        List<UploadMultipartFileDTO> list = dto.getList();
        for (UploadMultipartFileDTO uploadMultipartFileDTO: list) {
            //上传文件
            List<MultipartFile> files = uploadMultipartFileDTO.getFiles();
            //飞书链接
            List<String> fileUrls = uploadMultipartFileDTO.getFileUrls();
            //文件信息保存
            if (CollectionUtils.isNotEmpty(files)) {
                for (MultipartFile multipartFile: files) {
                    TaskDocsFinishEntity entity = new TaskDocsFinishEntity();
                    BeanMapperUtils.copy(finishEntity,entity);
                    double size = multipartFile.getSize();
                    fileSize = size / (1024 * 1024);
                    fileSize = (double) Math.round(fileSize * 100) / 100;
                    fileName = multipartFile.getOriginalFilename().toLowerCase();
                    fileSuffix = FilenameUtils.getExtension(fileName).toLowerCase();
                    File file = FileUtil.multiToFile(multipartFile);
                    String fileUrl = FastDFSClientUtil.uploadFile(file, fileName);
                    if (StringUtils.isBlank(fileUrl)) {
                        throw new ServiceException(ApiError.ERROR_95018);
                    }
                    finishEntity.setUploadType(IsConstant.NO);
                    entity.setTaskDocsId(uploadMultipartFileDTO.getTaskDocsId());
                    entity.setFileSuffix(fileSuffix);
                    entity.setFileSize(fileSize);
                    entity.setFileName(fileName);
                    entity.setFileUrl(fileUrl);
                    entity.setOldFileName(fileName);
                    entity.setOldFileUrl(fileUrl);
                    fileNames.add(fileName);
                    resultList.add(entity);
                }
            }
            //飞书链接信息保存
            if (CollectionUtils.isNotEmpty(fileUrls)) {
                for (String fileUrl: fileUrls) {
                    TaskDocsFinishEntity entity = new TaskDocsFinishEntity();
                    BeanMapperUtils.copy(finishEntity,entity);
                    entity.setUploadType(IsConstant.YES);
                    entity.setTaskDocsId(uploadMultipartFileDTO.getTaskDocsId());
                    entity.setFileUrl(fileUrl);
                    entity.setOldFileUrl(fileUrl);
                    resultList.add(entity);
                }
            }

        }
        if (CollectionUtils.isNotEmpty(resultList)) {
            //删除飞书通知
            deleteByUploadType(dto.getProductId(),dto.getTaskId(),IsConstant.YES);
            boolean flag = this.saveOrUpdateBatch(resultList);
            if (flag) {
                //新增上传交付物操作日志
                SysLogEntity sysLogEntity = new SysLogEntity().setContent(String.format("上传了一个文件[%s]",String.join(",",fileNames)))
                        .setBusinessId(taskEntity.getId())
                        .setOperation("文档操作")
                        .setClassPath(SysLogClassPathEnum.PROJECTTASKENTITY.getDesc());
                sysLogService.addSysLogByOther(sysLogEntity);
            }
        }

      return Boolean.TRUE;
    }

    /**
     * 删除已存在的交付文档
     *
     * @param productId
     * @param taskDocsId
     * @param taskId
     * @return com.erp.model.plm.entity.TaskDocsFinishEntity
     * @author yl
     * @date 2022-11-14 17:48
     */
    private void deleteByDocsId(String productId, String taskDocsId, String taskId) {
        LambdaQueryWrapper<TaskDocsFinishEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskDocsFinishEntity::getTaskId, taskId);
        queryWrapper.eq(TaskDocsFinishEntity::getProductId, productId);
        queryWrapper.eq(TaskDocsFinishEntity::getTaskDocsId, taskDocsId);
         this.remove(queryWrapper);
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
            throw new ServiceException(ApiError.ERROR_95027);
        }
        Integer taskState = taskEntity.getStatus();
        //如果已完成了 或者有人审核了 就不能删除
        Integer taskProperty = projectTaskService.getTaskProperty(taskEntity);
        Integer generalApproval = TaskProcessTypeEnum.GENERAL_APPROVAL_TASK.getCode();
        Integer reviewTask = TaskProcessTypeEnum.REVIEW_TASK.getCode();
        //当是流程的时候
        if (generalApproval.equals(taskProperty) || reviewTask.equals(taskProperty)) {
            if (TaskStateEnum.FINISH.getCode().equals(taskState)) {
                throw new ServiceException(ApiError.ERROR_95040);
            }
        }
        //新增删除交付物操作日志
        SysLogEntity sysLogEntity = new SysLogEntity().setContent(String.format("删除了一个文件[%s]", entity.getFileName()))
                .setBusinessId(taskEntity.getId())
                .setOperation("文档操作")
                .setClassPath(SysLogClassPathEnum.PROJECTTASKENTITY.getDesc());
        sysLogService.addSysLogByOther(sysLogEntity);
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
                && !approvalNoPassCode.equals(taskState)) {
            throw new ServiceException(ApiError.ERROR_95039);
        }

        LoginUser loginUser = commonService.getUserInfo();

        //文件名
        String fileName = "";
        List<String> fileNames = new ArrayList<>();
        //文件后缀
        String fileSuffix = "";
        double fileSize = 0.0;
        TaskDocsFinishEntity finishEntity = new TaskDocsFinishEntity();
        finishEntity.setCreateUserName(loginUser.getUserName());
        finishEntity.setFileName(fileName);
        finishEntity.setProductId(dto.getProductId());
        finishEntity.setTaskId(dto.getTaskId());
        finishEntity.setCreateUserId(loginUser.getUid());
        finishEntity.setFileType(TaskConstant.FILE_TYPE);
        finishEntity.setFileSuffix(fileSuffix);
        finishEntity.setFileSize(fileSize);
        finishEntity.setOldUploadType(dto.getUploadType());
        finishEntity.setOldFileName(fileName);
        List<TaskDocsFinishEntity> resultList = new ArrayList<>();
        List<UploadMultipartFileDTO> list = dto.getList();
        for (UploadMultipartFileDTO uploadMultipartFileDTO: list) {
            //上传文件
            List<MultipartFile> files = uploadMultipartFileDTO.getFiles();
            //飞书链接
            List<String> fileUrls = uploadMultipartFileDTO.getFileUrls();
            if (CollectionUtils.isEmpty(files) && CollectionUtils.isEmpty(fileUrls)) {
                throw new ServiceException(ApiError.ERROR_95028);
            }
            //文件信息保存
            if (CollectionUtils.isNotEmpty(files)) {
                for (MultipartFile multipartFile: files) {
                    TaskDocsFinishEntity entity = new TaskDocsFinishEntity();
                    BeanMapperUtils.copy(finishEntity,entity);
                    double size = multipartFile.getSize();
                    fileSize = size / (1024 * 1024);
                    fileSize = (double) Math.round(fileSize * 100) / 100;
                    fileName = multipartFile.getOriginalFilename().toLowerCase();
                    fileSuffix = FilenameUtils.getExtension(fileName).toLowerCase();
                    File file = FileUtil.multiToFile(multipartFile);
                    String fileUrl = FastDFSClientUtil.uploadFile(file, fileName);
                    if (StringUtils.isBlank(fileUrl)) {
                        throw new ServiceException(ApiError.ERROR_95018);
                    }
                    finishEntity.setUploadType(IsConstant.NO);
                    entity.setTaskDocsId(uploadMultipartFileDTO.getTaskDocsId());
                    entity.setFileSuffix(fileSuffix);
                    entity.setFileSize(fileSize);
                    entity.setFileName(fileName);
                    entity.setFileUrl(fileUrl);
                    entity.setOldFileName(fileName);
                    entity.setOldFileUrl(fileUrl);
                    fileNames.add(fileName);
                    resultList.add(entity);
                }
            }
            //飞书链接信息保存
            if (CollectionUtils.isNotEmpty(fileUrls)) {
                for (String fileUrl: fileUrls) {
                    TaskDocsFinishEntity entity = new TaskDocsFinishEntity();
                    BeanMapperUtils.copy(finishEntity,entity);
                    entity.setUploadType(IsConstant.YES);
                    entity.setTaskDocsId(uploadMultipartFileDTO.getTaskDocsId());
                    entity.setFileUrl(fileUrl);
                    entity.setOldFileUrl(fileUrl);
                    resultList.add(entity);
                }
            }

        }

        StringBuffer sb = new StringBuffer();
        if (CollectionUtils.isNotEmpty(resultList)) {
            //删除飞书通知
            deleteByUploadType(dto.getProductId(),dto.getTaskId(),IsConstant.YES);
            Boolean flag = this.saveBatch(resultList);
            //当更新成功后 保存记录
            if (flag) {
                noticeMessageService.docChangesNotice(loginUser.getUserName(), taskEntity.getProductId(), taskId, String.join(",",fileNames));
                sb.append("变更为").append(String.join(",",fileNames));
                for (TaskDocsFinishEntity taskDocsFinishEntity: resultList) {
                    docsChangeRecordService.addRecord(sb.toString(), finishEntity.getTaskId(), taskDocsFinishEntity.getId(), "");
                }
            }
            //新增变更文档操作日志
            SysLogEntity sysLogEntity = new SysLogEntity().setContent(String.format("变更了一个文件[%s]", String.join(",",fileNames)))
                    .setBusinessId(taskEntity.getId())
                    .setOperation("文档操作")
                    .setClassPath(SysLogClassPathEnum.PROJECTTASKENTITY.getDesc());
            sysLogService.addSysLogByOther(sysLogEntity);
        }
        startChangeDocsProcess(loginUser.getUid(), taskEntity);

        return Boolean.TRUE;
    }

    /**
     * 发起变更稳定流程
     *
     * @param
     * @return void
     * @author yl
     * @date 2022-11-14 18:29
     */
    public Boolean startChangeDocsProcess(String userId, ProjectTaskEntity taskEntity) {
        //任务类型
        Integer taskType = taskEntity.getType();
        List<List<String>> membersIds = new ArrayList<>();
        BusinessProcessEntity businessProcess ;
        //这里需要启动一个变更流程,如果是评审任务则调用文档变更流程，一般任务采用原有流程
        if (TaskTypeEnum.GENERAL_TASK.getCode().equals(taskType)) {
            //查询任务下审核人
            List<TaskChargeDistributionEntity> taskChargeDistributionList = taskChargeDistributionService.listBySourceAndTaskId(MathUtil.THREE, taskEntity.getId());
            if (CollectionUtils.isEmpty(taskChargeDistributionList)) {
                throw new ServiceException(ApiError.ERROR_95045);
            }
            for (TaskChargeDistributionEntity taskChargeDistributionEntity:taskChargeDistributionList) {
                String chargeIds = taskChargeDistributionEntity.getChargeIds();
                if (StringUtils.isNotBlank(chargeIds)) {
                    List<String> userIdList = Arrays.stream(chargeIds.split(",")).collect(Collectors.toList());
                    membersIds.add(userIdList);
                }
            }
            businessProcess = businessProcessService.getById(taskEntity.getBusinessProcessId());
        } else {
            //如果是 评审任务 就是任务负责人
            String approvalUserId = taskEntity.getChargeId();
            if (StringUtils.isEmpty(approvalUserId)) {
                throw new ServiceException(ApiError.ERROR_95045);
            }
            List<String> userIdList = Arrays.asList(approvalUserId.split(","));
            membersIds.add(userIdList);

            businessProcess = businessProcessService.getProcessByBusinessKey(BusinessProcessEnum.DOCS_CHANGE.getBusinessKey());
        }
        StartProcessDTO startProcess = new StartProcessDTO();
        startProcess.setUserId(userId);
        startProcess.setProcessDefinitionKey(businessProcess.getProcessDefinitionKey());
        startProcess.setBusinessKey(businessProcess.getBusinessType());
        Map<String, Object> parameterMap = new HashMap<>();
        String param = businessProcess.getParam();
        List<String> paramList = Arrays.stream(param.split(",")).collect(Collectors.toList());
        for (int i = 0; i < paramList.size(); i++) {
            parameterMap.put(paramList.get(i), membersIds.get(i));
        }
        startProcess.setParameterMap(parameterMap);
        ProcessNodeDTO processResult = workflowFeign.startProcess(startProcess);
        String processId = processResult.getProcessId();
        if (StringUtils.isNotBlank(processId)) {
            //更改任务的状态为未待审核 以及流程id
            taskEntity.setProcessId(processId);
            taskEntity.setStatus(TaskStateEnum.WAIT_CONFIRM.getCode());
            taskEntity.setBusinessProcessId(businessProcess.getId());
            return projectTaskService.updateById(taskEntity);
        }
        return false;
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

    @Override
    public void removeByTaskId(String taskId) {
        LambdaQueryWrapper<TaskDocsFinishEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskDocsFinishEntity::getTaskId, taskId);
        this.remove(queryWrapper);
    }

    /**
     * 发起变更文档流程
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-14 18:42
     */
    @Override
    public Boolean startChangeDocsProcess(BaseIdDTO dto) {
        //任务id
        String taskId = dto.getId();
        String userId = commonService.getUserInfo().getUid();
        ProjectTaskEntity taskEntity = projectTaskService.getById(taskId);
        if (Objects.isNull(taskEntity)) {
            throw new ServiceException(ApiError.ERROR_95027);
        }
        return startChangeDocsProcess(userId, taskEntity);

    }

    /**
     * 任务列表 -变更文档
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-11-15 9:22
     */
    @Override
    public Boolean updateFile(TaskChangeFileDTO dto) {
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
                && !approvalNoPassCode.equals(taskState)) {
            throw new ServiceException(ApiError.ERROR_95039);
        }
        LoginUser loginUser = commonService.getUserInfo();

        //文件名
        String fileName = "";
        List<String> fileNames = new ArrayList<>();
        //文件后缀
        String fileSuffix = "";
        double fileSize = 0.0;
        TaskDocsFinishEntity finishEntity = new TaskDocsFinishEntity();
        finishEntity.setCreateUserName(loginUser.getUserName());
        finishEntity.setFileName(fileName);
        finishEntity.setProductId(dto.getProductId());
        finishEntity.setTaskId(dto.getTaskId());
        finishEntity.setCreateUserId(loginUser.getUid());
        finishEntity.setFileType(TaskConstant.FILE_TYPE);
        finishEntity.setFileSuffix(fileSuffix);
        finishEntity.setFileSize(fileSize);
        finishEntity.setOldUploadType(dto.getUploadType());
        finishEntity.setOldFileName(fileName);
        List<TaskDocsFinishEntity> resultList = new ArrayList<>();
        List<UploadMultipartFileDTO> list = dto.getList();
        for (UploadMultipartFileDTO uploadMultipartFileDTO: list) {
            //上传文件
            List<MultipartFile> files = uploadMultipartFileDTO.getFiles();
            //飞书链接
            List<String> fileUrls = uploadMultipartFileDTO.getFileUrls();
            if (CollectionUtils.isEmpty(files) && CollectionUtils.isEmpty(fileUrls)) {
                throw new ServiceException(ApiError.ERROR_95028);
            }
            //文件信息保存
            if (CollectionUtils.isNotEmpty(files)) {
                for (MultipartFile multipartFile: files) {
                    TaskDocsFinishEntity entity = new TaskDocsFinishEntity();
                    BeanMapperUtils.copy(finishEntity,entity);
                    double size = multipartFile.getSize();
                    fileSize = size / (1024 * 1024);
                    fileSize = (double) Math.round(fileSize * 100) / 100;
                    fileName = multipartFile.getOriginalFilename().toLowerCase();
                    fileSuffix = FilenameUtils.getExtension(fileName).toLowerCase();
                    File file = FileUtil.multiToFile(multipartFile);
                    String fileUrl = FastDFSClientUtil.uploadFile(file, fileName);
                    if (StringUtils.isBlank(fileUrl)) {
                        throw new ServiceException(ApiError.ERROR_95018);
                    }
                    finishEntity.setUploadType(IsConstant.NO);
                    entity.setTaskDocsId(uploadMultipartFileDTO.getTaskDocsId());
                    entity.setFileSuffix(fileSuffix);
                    entity.setFileSize(fileSize);
                    entity.setFileName(fileName);
                    entity.setFileUrl(fileUrl);
                    entity.setOldFileName(fileName);
                    entity.setOldFileUrl(fileUrl);
                    fileNames.add(fileName);
                    resultList.add(entity);
                }
            }
            //飞书链接信息保存
            if (CollectionUtils.isNotEmpty(fileUrls)) {
                for (String fileUrl: fileUrls) {
                    TaskDocsFinishEntity entity = new TaskDocsFinishEntity();
                    BeanMapperUtils.copy(finishEntity,entity);
                    entity.setUploadType(IsConstant.YES);
                    entity.setTaskDocsId(uploadMultipartFileDTO.getTaskDocsId());
                    entity.setFileUrl(fileUrl);
                    entity.setOldFileUrl(fileUrl);
                    resultList.add(entity);
                }
            }

        }

        StringBuffer sb = new StringBuffer();
        if (CollectionUtils.isNotEmpty(resultList)) {
            //删除飞书通知
            deleteByUploadType(dto.getProductId(),dto.getTaskId(),IsConstant.YES);
            Boolean flag = this.saveBatch(resultList);
            //当更新成功后 保存记录
            if (flag) {
                noticeMessageService.docChangesNotice(loginUser.getUserName(), taskEntity.getProductId(), taskId, String.join(",",fileNames));
                sb.append("新增文档").append(String.join(",",fileNames));
                for (TaskDocsFinishEntity taskDocsFinishEntity:resultList) {
                    docsChangeRecordService.addRecord(sb.toString(), finishEntity.getTaskId(), taskDocsFinishEntity.getId(), "");
                }
                //新增产品操作日志
                ProductOperateRecordDTO productOperateRecordDTO = new ProductOperateRecordDTO();
                productOperateRecordDTO.setProductId(finishEntity.getProductId());
                List<String> remarkList = new ArrayList<>();
                remarkList.add("变更文档：[" + fileNames + "]");
                productOperateRecordDTO.setRemark(JSONObject.toJSONString(remarkList));
                productOperateRecordService.saveOrUpdate(productOperateRecordDTO);
            }
        }
        return Boolean.TRUE;
    }

    private void deleteByUploadType(String productId, String taskId,Integer uploadType) {
        LambdaQueryWrapper<TaskDocsFinishEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskDocsFinishEntity::getTaskId, taskId);
        queryWrapper.eq(TaskDocsFinishEntity::getProductId, productId);
        queryWrapper.eq(TaskDocsFinishEntity::getUploadType,uploadType);
        this.remove(queryWrapper);
    }

    public int getFinishDocsNum(String taskId) {
        LambdaQueryWrapper<TaskDocsFinishEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskDocsFinishEntity::getTaskId, taskId);
        return this.count(queryWrapper);

    }


}
