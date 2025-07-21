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
        FileTask old = this.getById(importResultDTO.getTaskId());
        if (Objects.isNull(old)) {
            log.error("文件任务[{}]不存在", importResultDTO.getTaskId());
            return;
        }
        //文件已取消已删除不做更新
        if (old.getStatus().equals(FileTaskStatusEnum.STOP.getCode()) || old.getStatus().equals(FileTaskStatusEnum.CANCEL.getCode())) {
            log.error("文件任务[{}]已取消或已停止", importResultDTO.getTaskId());
            return;
        }
        this.lambdaUpdate().set(FileTask::getFileUrl, CharSequenceUtil.isNotBlank(importResultDTO.getErrorUrl()) ? importResultDTO.getErrorUrl() : "")
                .set(FileTask::getCount, Objects.nonNull(importResultDTO.getCount()) ? importResultDTO.getCount() : 0)
                .set(FileTask::getStatus, importResultDTO.getStatus())
                .set(FileTask::getRemark, importResultDTO.getMsg())
                .set(FileTask::getFinishTime, LocalDateTime.now())
                .eq(FileTask::getId, importResultDTO.getTaskId())
                .update();
    }
}