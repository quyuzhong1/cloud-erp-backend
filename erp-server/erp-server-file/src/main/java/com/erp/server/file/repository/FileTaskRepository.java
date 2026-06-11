package com.erp.server.file.repository;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.BaseDTO;
import com.erp.model.file.dto.FileDTO;
import com.erp.server.file.dto.FileTaskParamsDTO;
import com.erp.server.file.entity.FileTask;
import com.common.business.enums.FileTaskStatusEnum;
import com.erp.server.file.mapper.FileTaskMapper;
import com.erp.server.file.vo.FileTaskVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileTaskRepository extends ServiceImpl<FileTaskMapper, FileTask> implements IFileTaskRepository {
    @Override
    public List<FileTask> getExpireByStatuses(LocalDateTime localDateTime, FileTaskStatusEnum... fileTaskStatusEnums) {
        return list(Wrappers.<FileTask>lambdaQuery()
                .in(FileTask::getStatus, Arrays.stream(fileTaskStatusEnums).map(Enum::name).collect(Collectors.toList())).lt(FileTask::getCreateTime, localDateTime));
    }

    @Override
    public IPage<FileTaskVO> getFileTasks(Page<FileTaskVO> page, FileTaskParamsDTO dto) {
        return baseMapper.getFileTasks(page, dto);
    }

    @Override
    public void updateTask(BaseDTO.ImportResultDTO importResultDTO) {
        this.lambdaUpdate()
                .set(CharSequenceUtil.isNotBlank(importResultDTO.getErrorUrl()), FileTask::getFileUrl, importResultDTO.getErrorUrl())
                .set(Objects.nonNull(importResultDTO.getCount()), FileTask::getCount,importResultDTO.getCount())
                .set(CharSequenceUtil.isNotBlank(importResultDTO.getStatus()), FileTask::getStatus, importResultDTO.getStatus())
                .set(CharSequenceUtil.isNotBlank(importResultDTO.getRemark()), FileTask::getRemark, importResultDTO.getRemark())
                .set(Objects.nonNull(importResultDTO.getStartTime()), FileTask::getStartTime, importResultDTO.getStartTime())
                .set(Objects.nonNull(importResultDTO.getFinishTime()), FileTask::getFinishTime, importResultDTO.getFinishTime())
                .eq(FileTask::getId, importResultDTO.getTaskId())
                .update();
    }

    @Override
    public List<FileDTO.FileTaskDTO> listLatestFileTask(List<String> fileUrlList) {
        if (CollUtil.isEmpty(fileUrlList)) {
            return Collections.emptyList();
        }
        return baseMapper.listLatestFileTask(fileUrlList);
    }

    @Override
    public List<FileTask> listTimeOutImportTask(String code,Integer hours) {
      return   lambdaQuery().eq(FileTask::getEvent,code)
                .eq(FileTask::getStatus, FileTaskStatusEnum.PROCESS.name())
                .lt(FileTask::getUpdateTime, LocalDateTime.now().minusHours(hours))
                .list();
    }

    @Override
    public void updateTaskStatus(List<String> taskIdList) {
        if (CollUtil.isEmpty(taskIdList)) {
            return;
        }
        this.lambdaUpdate()
                .set(FileTask::getStatus, FileTaskStatusEnum.PENDING.name())
                .in(FileTask::getId, taskIdList)
                .update();
    }

    @Override
    public List<FileTask> listCleanFileTask(LocalDateTime expireTime, int limit, int offset) {
        return lambdaQuery()
                .lt(FileTask::getCreateTime, expireTime)
                .isNotNull(FileTask::getFileUrl)
                .ne(FileTask::getFileUrl, "")
                .notIn(FileTask::getStatus, Arrays.asList(FileTaskStatusEnum.PROCESS.name(), FileTaskStatusEnum.PENDING.name()))
                .orderByAsc(FileTask::getCreateTime)
                .orderByAsc(FileTask::getId)
                .last("limit " + limit + " offset " + Math.max(0, offset))
                .list();
    }

    @Override
    public Set<String> listProcessingTaskIds() {
        List<FileTask> list = lambdaQuery()
                .select(FileTask::getId)
                .eq(FileTask::getStatus, FileTaskStatusEnum.PROCESS.name())
                .list();
        if (CollUtil.isEmpty(list)) {
            return Collections.emptySet();
        }
        return list.stream()
                .map(FileTask::getId)
                .collect(Collectors.toSet());
    }
}