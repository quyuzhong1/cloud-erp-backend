package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.ProjectTaskTimeRecordDTO;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.ProjectTaskTimeRecordEntity;
import com.erp.model.plm.vo.ProjectTaskTimeRecordPageVO;
import com.erp.server.plm.mapper.ProjectTaskTimeRecordMapper;
import com.erp.server.plm.service.ProjectTaskTimeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-02-23
 */
@Slf4j
@Service
public class ProjectTaskTimeRecordServiceImpl extends ServiceImpl<ProjectTaskTimeRecordMapper, ProjectTaskTimeRecordEntity> implements ProjectTaskTimeRecordService {

    @Override
    public PagingVO<ProjectTaskTimeRecordPageVO> pageRecord(PagingDTO<ProjectTaskTimeRecordDTO.PageRecordDto> dto) {
        // 查询 产品数据分组
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<ProjectTaskTimeRecordPageVO> recordPage  = baseMapper.pageTaskTimeRecord(query, dto.getParams(), dto.getParam());
        return new PagingVO(recordPage);
    }

    @Override
    public Boolean exportTaskTimeList(ProjectTaskTimeRecordDTO.PageRecordDto dto, HttpServletResponse response) {
        List<ProjectTaskTimeRecordPageVO> projectTaskTimeRecordList  = baseMapper.pageTaskTimeRecord(dto, dto.getParam());
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/taskTime.xlsx";
        String name = "工时统计";
        String dateStr = LocalDateTimeUtil.format(LocalDateTime.now(), DateUtil.fmt);
        try {
            new ExcelPrintUtils().patchExport(projectTaskTimeRecordList, response, StrUtil.format("{}-{}", name, dateStr), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdateByProjectTaskList(List<ProjectTaskEntity> taskList) {
        if (CollectionUtil.isEmpty(taskList)) {
            log.info("ProjectTaskTimeRecordServiceImpl>>>saveOrUpdateByProjectTaskList>>需要处理任务工时数据为空");
            return false;
        }
        List<ProjectTaskTimeRecordEntity> exitTaskTimeEntities = lambdaQuery()
                .in(ProjectTaskTimeRecordEntity::getProjectTaskId, taskList.stream().map(ProjectTaskEntity::getId).distinct().collect(Collectors.toList()))
                .list();
        List<ProjectTaskTimeRecordEntity> insertList = new ArrayList<>();
        List<ProjectTaskTimeRecordEntity> updateList = new ArrayList<>();
        Map<String, ProjectTaskTimeRecordEntity> exitEntityMap = exitTaskTimeEntities.stream()
                .collect(Collectors.toMap(ProjectTaskTimeRecordEntity::getId, e -> e));

        taskList.stream().forEach(entity -> {
            ProjectTaskTimeRecordEntity projectTaskTimeRecordEntity = exitEntityMap.get(entity.getId());
            if (null == projectTaskTimeRecordEntity) {
                insertList.add(new ProjectTaskTimeRecordEntity(entity));
            } else {
                ProjectTaskTimeRecordEntity updateEntity = new ProjectTaskTimeRecordEntity(entity);
                if(null == entity.getRealityStartTime()){
                    updateEntity.setRealityStartTime(projectTaskTimeRecordEntity.getRealityStartTime());
                }
                updateEntity.setId(entity.getId());
                updateList.add(updateEntity);
            }
        });
        if (CollectionUtil.isNotEmpty(insertList)) {
            if (!saveBatch(insertList)) {
                log.error("ProjectTaskTimeRecordServiceImpl>>>saveOrUpdateByProjectTaskList>>insertList更新/保存工时记录失败请重试！");
                throw new RuntimeException("保存工时记录失败请重试！");
            }
        }
        if (CollectionUtil.isNotEmpty(updateList)) {
            if (!updateBatchById(updateList)) {
                log.error("ProjectTaskTimeRecordServiceImpl>>>saveOrUpdateByProjectTaskList>>updateList更新/保存工时记录失败请重试！");
            throw new RuntimeException("更新工时记录失败请重试！");
            }
        }
        return true;
    }
}
