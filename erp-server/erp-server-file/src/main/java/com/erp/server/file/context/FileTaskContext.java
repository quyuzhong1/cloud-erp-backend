package com.erp.server.file.context;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.server.file.core.FileEventHandler;
import com.erp.server.file.dto.FileTaskDTO;
import com.erp.server.file.dto.FileTaskParamsDTO;
import com.erp.server.file.entity.FileTask;
import com.common.business.enums.FileTaskStatusEnum;
import com.erp.server.file.enums.FileTaskTypeEnum;
import com.erp.server.file.repository.IFileTaskRepository;
import com.erp.server.file.service.FileService;
import com.erp.server.file.utils.ExceptionUtils;
import com.erp.server.file.vo.FileTaskVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class FileTaskContext {
    @Resource
    private IFileTaskRepository fileTaskRepository;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private FileTaskFactory fileTaskFactory;
    @Resource
    private FileService fileService;
    @Resource(name = "fileExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 创建文件任务
     */
    @Transactional(rollbackFor = Exception.class)
    public String addExport(FileTaskDTO fileTaskDTO) {
        // 创建文件任务
        FileTask fileTask = FileTask.create(fileTaskDTO.getEvent(), fileTaskDTO.getFileName(), writeValueAsString(fileTaskDTO.getMetaInfo()));
        LoginUser loginUser = UserContext.getLoginUser();
        fileTask.setType(FileTaskTypeEnum.ASYNC_EXPORT.getCode());
        // 保存文件任务
        fileTaskRepository.save(fileTask);
        log.info("文件任务[{}]创建成功,类型为[{}],状态[PENDING]", fileTask.getId(), fileTaskDTO.getEvent());
        // 完成新增数据事务提交之后,异步执行
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                CompletableFuture.runAsync(() -> exportProcess(fileTask.getId(), loginUser), threadPoolTaskExecutor);
                log.info("文件任务[{}]消息已投递,事务已提交", fileTask.getId());
            }
        });
        return fileTask.getId();
    }
    /**
     * 创建文件任务
     */
    @Transactional(rollbackFor = Exception.class)
    public String addImport(FileTaskDTO fileTaskDTO) {
        // 创建文件任务
        FileTask fileTask = FileTask.create(fileTaskDTO.getEvent(), fileTaskDTO.getFileName(), writeValueAsString(fileTaskDTO.getMetaInfo()));
        LoginUser loginUser = UserContext.getLoginUser();
        fileTask.setType(FileTaskTypeEnum.ASYNC_IMPORT.getCode());
        // 保存文件任务
        fileTaskRepository.save(fileTask);
        log.info("文件任务[{}]创建成功,类型为[{}],状态[PENDING]", fileTask.getId(), fileTaskDTO.getEvent());
        // 完成新增数据事务提交之后,异步执行
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                CompletableFuture.runAsync(() -> importProcess(fileTask.getId(), loginUser, false), threadPoolTaskExecutor);
                log.info("文件任务[{}]消息已投递,事务已提交", fileTask.getId());
            }
        });
        return fileTask.getId();
    }

    /**
     * 文件任务删除
     * 基于乐观锁版本，多服务器需优化为分布式锁
     */
    public void delete(String id) {
        FileTask fileTask = fileTaskRepository.getById(id);
        ExceptionUtils.emptyThrow(fileTask, String.format("文件任务不存在[%s],请联系IT检查请求", id));
        LoginUser currentUser = UserContext.getNonLoginUser();
        ExceptionUtils.conditionThrow(() -> !String.valueOf(fileTask.getCreateUserId()).equals(currentUser.getUid()), "非数据创建人不可删除!");
        // 处于PENDING状态的任务无法被删除
        ExceptionUtils.conditionThrow(fileTask::volatileStatus, String.format("当前任务[%s]正在处理中,无法被删除,请稍后尝试!", id));
        // 加锁执行删除
        fileTaskRepository.removeById(id);
        // 删除文件
        boolean exist = fileService.exist(fileTask.getFileUrl());
        if (exist) {
            fileService.deleteFile(fileTask.getFileUrl());
        }
    }

    /**
     * 文件任务删除
     * 基于乐观锁版本，多服务器需优化为分布式锁
     */
    @Transactional(rollbackFor = Exception.class)
    public void retry(String id) {
        FileTask fileTask = fileTaskRepository.getById(id);
        ExceptionUtils.emptyThrow(fileTask, String.format("文件任务不存在[%s],请联系IT检查请求", id));
        LoginUser currentUser = UserContext.getNonLoginUser();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                CompletableFuture.runAsync(() -> importProcess(fileTask.getId(), currentUser, true), threadPoolTaskExecutor);
                log.info("文件任务[{}]消息已投递,事务已提交", fileTask.getId());
            }
        });
    }


    /**
     * 文件任务处理
     * 基于乐观锁版本，多服务器需优化为分布式锁
     *
     * @param id 文件任务Id
     */
    public void exportProcess(String id, LoginUser user) {
        FileTask fileTask = fileTaskRepository.getById(id);
        if (!ObjectUtils.isEmpty(fileTask) && fileTask.isPending()) {
            // 设置任务状态为处理中
            fileTask.setStatus(FileTaskStatusEnum.PROCESS.name());
            // 设置任务开始时间
            fileTask.setStartTime(LocalDateTime.now());
            // 更新任务状态
            fileTaskRepository.updateById(fileTask);
            // 文件任务执行
            try {
                log.info("文件任务[{}]获取成功,状态[PROCESS]", id);
                // 获取文件任务处理器
                FileEventHandler eventHandler = fileTaskFactory.getFileHandler(fileTask.getEvent());
                ExceptionUtils.emptyThrow(eventHandler, String.format("事件类型[%s]不存在,请联系IT人员检查配置", fileTask.getEvent()));
                UserContext.setLoginUser(user);
                // 处理文件
                eventHandler.handle(fileTask);
                // 设置任务状态为 全部成功
                fileTask.setStatus(FileTaskStatusEnum.FINISH.name());
            } catch (Exception e) {
                log.error("文件任务[{}]处理失败", fileTask.getId(), e);
                // 更新任务状态为失败
                fileTask.setStatus(FileTaskStatusEnum.FAIL.name());
                String remark = String.format("文件任务[%s]失败: %s", fileTask.getId(), e.getMessage());
                fileTask.setRemark(remark.length() > 490 ? remark.substring(0, 490) : remark);
                fileTaskRepository.updateById(fileTask);
            } finally {
                // 设置任务完成时间
                fileTask.setFinishTime(LocalDateTime.now());
                log.info("文件任务[{}]已完成,状态为[{}],总耗时[{}]", fileTask.getId(), fileTask.getStatus(),
                        Duration.between(fileTask.getStartTime(), fileTask.getFinishTime()).getSeconds());
                fileTaskRepository.updateById(fileTask);
            }
        } else {
            log.info("文件任务[{}]获取失败", id);
        }
    }
    /**
     * 文件任务处理
     * 基于乐观锁版本，多服务器需优化为分布式锁
     *
     * @param id 文件任务Id
     */
    public void importProcess(String id, LoginUser user, Boolean isRetry) {
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(id);
        FileTask fileTask = fileTaskRepository.getById(id);
        if ((!ObjectUtils.isEmpty(fileTask) && fileTask.isPending()) || isRetry) {
            // 设置任务状态为处理中
            importResultDTO.setStatus(FileTaskStatusEnum.PROCESS.name());
            // 设置任务开始时间
            importResultDTO.setStartTime(LocalDateTime.now());
            // 更新任务状态
            fileTaskRepository.updateTask(importResultDTO);
            // 文件任务执行
            try {
                log.info("文件任务[{}]获取成功,状态[PROCESS]", id);
                // 获取文件任务处理器
                FileTaskEventEnum eventEnum = FileTaskEventEnum.getByCode(fileTask.getEvent());
                ExceptionUtils.emptyThrow(eventEnum, String.format("事件类型[%s]不存在,请联系IT人员检查配置", fileTask.getEvent()));
                UserContext.setLoginUser(user);
                // 处理文件 直接分发调用方法
                if (CharSequenceUtil.isNotBlank(eventEnum.getHandler())){
                    FileEventHandler eventHandler = fileTaskFactory.getFileHandler(fileTask.getEvent());
                    ExceptionUtils.emptyThrow(eventHandler, String.format("事件Hanlder[%s]不存在,请联系IT人员检查配置", eventEnum.getHandler()));
                    eventHandler.handle(fileTask);
                }else {
                    BaseDTO.ImportDTO dto = readValue(fileTask.getMetaInfo(), new TypeReference<BaseDTO.ImportDTO>() {});
                    dto.setTaskId(fileTask.getId());
                    dto.setImportCount(fileTask.getCount());
                    FeignQuery.invoke(eventEnum.getClassName(), eventEnum.getMethodName(), Collections.singletonList(dto));
                }
                // 设置任务状态为 全部成功
                importResultDTO.setStatus(FileTaskStatusEnum.FINISH.name());
            } catch (Exception e) {
                log.error("文件任务[{}]处理失败", fileTask.getId(), e);
                // 更新任务状态为失败
                importResultDTO.setStatus(FileTaskStatusEnum.FAIL.name());
                String remark = String.format("文件任务[%s]失败: %s", fileTask.getId(), e.getMessage());
                importResultDTO.setRemark(remark.length() > 490 ? remark.substring(0, 490) : remark);
                fileTaskRepository.updateTask(importResultDTO);
            } finally {
                // 设置任务完成时间
                importResultDTO.setFinishTime(LocalDateTime.now());
                log.info("文件任务[{}]已完成,状态为[{}],总耗时[{}]", fileTask.getId(), fileTask.getStatus(),
                        Duration.between(fileTask.getStartTime(), fileTask.getFinishTime()).getSeconds());
                fileTaskRepository.updateTask(importResultDTO);
            }
        } else {
            log.info("文件任务[{}]获取失败", id);
        }
    }


    public String writeValueAsString(Object target) {
        try {
            return objectMapper.writeValueAsString(target);
        } catch (JsonProcessingException e) {
            throw new UnsupportedOperationException(e);
        }
    }


    /**
     * 文件任务列表
     */
    @Transactional(readOnly = true)
    public IPage<FileTaskVO> paging(PagingDTO<FileTaskParamsDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        return fileTaskRepository.getFileTasks(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
    }


    /**
     * 定时清除 过期的文件任务
     * <p>
     * 等待超时的任务保留2天 / 成功的任务保留3天 /失败的任务保留5天
     */
//    @Scheduled(cron = "0 0 0 * * ?")
    public void removeTasks() {
        // 等待(失效)的任务超过2天 删除
        removeFileTasks(fileTaskRepository.getExpireByStatuses(LocalDateTime.now().minusDays(2), FileTaskStatusEnum.PENDING));
        // 成功的任务保留3天
        removeFileTasks(fileTaskRepository.getExpireByStatuses(LocalDateTime.now().minusDays(3), FileTaskStatusEnum.FINISH));
        // 失败的任务保留5天
        removeFileTasks(fileTaskRepository.getExpireByStatuses(LocalDateTime.now().minusDays(5), FileTaskStatusEnum.FAIL));
    }

    private void removeFileTasks(List<FileTask> fileTasks) {
        for (FileTask task : fileTasks) {
            String id = task.getId();
            try {
                fileTaskRepository.removeById(id);
                boolean exist = fileService.exist(task.getFileUrl());
                if (exist) {
                    fileService.deleteFile(task.getFileUrl());
                }
            } catch (Exception e) {
                log.error("文件任务[{}]删除失败", id, e);
            }
        }
    }

    @Transactional(readOnly = true)
    public FileTask view(String id) {
        return fileTaskRepository.getById(id);
    }

    public void updateTask(BaseDTO.ImportResultDTO importResultDTO) {
        if (CharSequenceUtil.isBlank(importResultDTO.getTaskId())) {
            log.error("文件任务[{}]不存在", importResultDTO.getTaskId());
            return;
        }
        FileTask old = fileTaskRepository.getById(importResultDTO.getTaskId());
        if (Objects.isNull(old)) {
            log.error("文件任务[{}]不存在", importResultDTO.getTaskId());
            return;
        }
        //文件已取消已删除不做更新
        if (old.getStatus().equals(FileTaskStatusEnum.STOP.getCode()) || old.getStatus().equals(FileTaskStatusEnum.CANCEL.getCode())) {
            log.error("文件任务[{}]已取消或已停止", importResultDTO.getTaskId());
            return;
        }
        fileTaskRepository.updateTask(importResultDTO);
    }
    public <P> P readValue(String params, TypeReference<P> type) {
        try {
            return objectMapper.readValue(params, type);
        } catch (JsonProcessingException e) {
            throw new ServiceException(e.getMessage());
        }
    }
}
