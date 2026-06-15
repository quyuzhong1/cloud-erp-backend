package com.erp.server.file.repository;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
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
    public List<FileTask> listCleanFileTask(LocalDateTime expireTime, int limit, LocalDateTime lastCreateTime, String lastId) {
        LambdaQueryChainWrapper<FileTask> query = lambdaQuery()
                .lt(FileTask::getCreateTime, expireTime)
                .in(FileTask::getStatus, Arrays.asList(FileTaskStatusEnum.FINISH.name(),
                        FileTaskStatusEnum.FAIL.name(),
                        FileTaskStatusEnum.CANCEL.name(),
                        FileTaskStatusEnum.STOP.name())
                );
        // 游标分页：按 (create_time, id) 严格大于上一批已处理到的位置取下一批，替代纯 offset。
        // 软删成功记录会从结果集移除，offset 语义随之漂移，导致失败记录在同一轮内被反复跳过；
        // 游标单调推进，无论成功/失败都不回扫已处理记录，失败记录顺延到下次调度重试，不再积压在本轮反复 offset。
        if (lastCreateTime != null && CharSequenceUtil.isNotBlank(lastId)) {
            query.and(w -> w.gt(FileTask::getCreateTime, lastCreateTime)
                    .or(o -> o.eq(FileTask::getCreateTime, lastCreateTime).gt(FileTask::getId, lastId)));
        }
        // 用 Page 由框架参数化 LIMIT，替代 .last("limit "+limit) 拼接；关闭 count 查询（游标分页无需总数）。
        Page<FileTask> page = new Page<>(1, Math.max(1, limit), false);
        return query.orderByAsc(FileTask::getCreateTime)
                .orderByAsc(FileTask::getId)
                .page(page)
                .getRecords();
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