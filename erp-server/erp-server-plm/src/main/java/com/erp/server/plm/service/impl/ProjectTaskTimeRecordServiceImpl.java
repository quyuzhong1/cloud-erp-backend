package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.plm.dto.ProjectTaskTimeRecordDTO;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.ProjectTaskTimeRecordEntity;
import com.erp.model.plm.vo.ProjectTaskTimeRecordPageVO;
import com.erp.model.sys.dto.SysCalendarDTO;
import com.erp.model.sys.vo.SysCalendarListVO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.mapper.ProjectTaskTimeRecordMapper;
import com.erp.server.plm.service.ProjectTaskService;
import com.erp.server.plm.service.ProjectTaskTimeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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

    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private ProjectTaskService projectTaskService;

    @Override
    public PagingVO<ProjectTaskTimeRecordPageVO> pageRecord(PagingDTO<ProjectTaskTimeRecordDTO.PageRecordDto> dto) {
        // 查询 产品数据分组
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<ProjectTaskTimeRecordPageVO> recordPage  = baseMapper.pageTaskTimeRecord(query, dto.getParams(), dto.getParam());
        // 根据task id查询日期数据进行处理
        List<ProjectTaskTimeRecordPageVO> records = recordPage.getRecords();
        if(CollectionUtil.isEmpty(records)){
            return new PagingVO(recordPage);
        }
        initPlanWorkTime(records);

        return new PagingVO(recordPage);
    }

    private void initPlanWorkTime(List<ProjectTaskTimeRecordPageVO> records) {
        SysCalendarDTO.ListDTO listDTO = new SysCalendarDTO.ListDTO();
        listDTO.setIsWorkDay(Boolean.FALSE);
        List<SysCalendarListVO> holidayList = sysUserFeign.listCalendar(listDTO);
        List<LocalDate> holidays = holidayList.stream().map(SysCalendarListVO::getCalendarDate).collect(Collectors.toList());
        records.stream().forEach(record -> {
            // 根据id查询
            List<ProjectTaskEntity> taskEntityList = projectTaskService.getByTaskIds(Arrays.asList(record.getTaskIds().split(",")));
            // 计算计划工时
            Integer planWorkDay = taskEntityList.stream().mapToInt(task -> {
                Date planEndTime = task.getPlanEndTime();
                Date planStartTime = task.getPlanStartTime();
                if (null == planStartTime || null == planEndTime) {
                    return 0;
                }
                return LocalDateUtil.countDaysForLocalDate(LocalDateUtil.date2LocalDate(planStartTime), LocalDateUtil.date2LocalDate(planEndTime), holidays);
            }).sum();
            // 赋值
            record.setPlanTaskTime(planWorkDay);
        });
    }

    @Override
    public Boolean exportTaskTimeList(ProjectTaskTimeRecordDTO.PageRecordDto dto, HttpServletResponse response) {
        List<ProjectTaskTimeRecordPageVO> projectTaskTimeRecordList  = baseMapper.pageTaskTimeRecord(dto, dto.getParam());
        initPlanWorkTime(projectTaskTimeRecordList);
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
        log.info("ProjectTaskTimeRecordServiceImpl>>>saveOrUpdateByProjectTaskList>>{}", JSONUtil.toJsonStr(taskList));
        if (CollectionUtil.isEmpty(taskList)) {
            log.info("ProjectTaskTimeRecordServiceImpl>>>saveOrUpdateByProjectTaskList>>需要处理任务工时数据为空");
            return false;
        }
        List<ProjectTaskTimeRecordEntity> exitTaskTimeEntities = lambdaQuery()
                .in(ProjectTaskTimeRecordEntity::getProjectTaskId, taskList.stream().map(ProjectTaskEntity::getId).distinct().collect(Collectors.toList()))
                .list();
        log.info("exitTaskTimeEntities >>>{}", JSONUtil.toJsonStr(exitTaskTimeEntities));
        List<ProjectTaskTimeRecordEntity> insertList = new ArrayList<>();
        List<ProjectTaskTimeRecordEntity> updateList = new ArrayList<>();
        Map<String, ProjectTaskTimeRecordEntity> exitEntityMap = exitTaskTimeEntities.stream()
                .collect(Collectors.toMap(ProjectTaskTimeRecordEntity::getProjectTaskId, e -> e));
        SysCalendarDTO.ListDTO listDTO = new SysCalendarDTO.ListDTO();
        listDTO.setIsWorkDay(Boolean.FALSE);
        List<SysCalendarListVO> sysCalendarList = sysUserFeign.listCalendar(listDTO);
        List<LocalDate> holidayDateList = sysCalendarList.stream().map(SysCalendarListVO::getCalendarDate).collect(Collectors.toList());
        taskList.stream().forEach(entity -> {
            ProjectTaskTimeRecordEntity projectTaskTimeRecordEntity = exitEntityMap.get(entity.getId());
            log.info("projectTaskTimeRecordEntity >>>{} exitEntityMap={}", JSONUtil.toJsonStr(projectTaskTimeRecordEntity), JSONUtil.toJsonStr(exitEntityMap));
            if (null == projectTaskTimeRecordEntity) {
                insertList.add(new ProjectTaskTimeRecordEntity(entity,holidayDateList));
            } else {
                ProjectTaskTimeRecordEntity updateEntity = new ProjectTaskTimeRecordEntity(entity,holidayDateList);
                if(null == entity.getRealityStartTime()){
                    updateEntity.setRealityStartTime(projectTaskTimeRecordEntity.getRealityStartTime());
                }
                updateEntity.setId(projectTaskTimeRecordEntity.getId());
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
