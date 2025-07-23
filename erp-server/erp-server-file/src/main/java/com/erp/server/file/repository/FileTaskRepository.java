package com.erp.server.file.repository;


import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.BaseDTO;
import com.erp.server.file.dto.FileTaskParamsDTO;
import com.erp.server.file.entity.FileTask;
import com.common.business.enums.FileTaskStatusEnum;
import com.erp.server.file.mapper.FileTaskMapper;
import com.erp.server.file.vo.FileTaskVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
                .set(FileTask::getStatus, importResultDTO.getStatus())
                .set(CharSequenceUtil.isNotBlank(importResultDTO.getRemark()), FileTask::getRemark, importResultDTO.getRemark())
                .set(Objects.nonNull(importResultDTO.getStartTime()), FileTask::getStartTime, importResultDTO.getStartTime())
                .set(Objects.nonNull(importResultDTO.getFinishTime()), FileTask::getFinishTime, importResultDTO.getFinishTime())
                .eq(FileTask::getId, importResultDTO.getTaskId())
                .update();
    }
}