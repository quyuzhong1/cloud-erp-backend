package com.erp.server.plm.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.plm.dto.ProjectTaskTimeRecordDTO;
import com.erp.model.plm.entity.ProjectTaskEntity;
import com.erp.model.plm.entity.ProjectTaskTimeRecordEntity;
import com.erp.model.plm.vo.ProjectTaskTimeRecordPageVO;
import com.erp.model.sys.dto.SysCalendarDTO;
import com.erp.model.sys.vo.SysCalendarListVO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.mapper.ProjectTaskTimeRecordMapper;
import com.erp.server.plm.service.ProjectTaskService;
import com.erp.server.plm.service.ProjectTaskTimeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.core.collection.CollUtil.isNotEmpty;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_TASK_TIME_RECORD;

/**
 * <p>
 * 服务实现类
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
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    public PagingVO<ProjectTaskTimeRecordPageVO> pageRecord(PagingDTO<ProjectTaskTimeRecordDTO.PageRecordDto> dto) {
        // 查询 产品数据分组
        Page<ProjectTaskTimeRecordDTO.PageRecordDto> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        if (null != dto.getParams().getEndDate()) {
            dto.getParams().setEndDate(dto.getParams().getEndDate().plusDays(1));
        }

        IPage<ProjectTaskTimeRecordPageVO> recordPage = baseMapper.pageTaskTimeRecord(query, dto.getParams(), dto.getPermissionSql());
        // 根据task id查询日期数据进行处理
        List<ProjectTaskTimeRecordPageVO> records = recordPage.getRecords();
        if (isEmpty(records)) {
            return new PagingVO<>(recordPage);
        }
        initPlanWorkTime(records);

        return new PagingVO<>(recordPage);
    }

    private void initPlanWorkTime(List<ProjectTaskTimeRecordPageVO> records) {
        SysCalendarDTO.ListDTO listDTO = new SysCalendarDTO.ListDTO();
        listDTO.setIsWorkDay(Boolean.FALSE);
        List<SysCalendarListVO> holidayList = sysUserFeign.listCalendar(listDTO);
        List<LocalDate> holidays = holidayList.stream().map(SysCalendarListVO::getCalendarDate).collect(Collectors.toList());
        records.stream().forEach(item -> {
            // 根据id查询
            List<ProjectTaskEntity> taskEntityList = projectTaskService.getByTaskIds(Arrays.asList(item.getTaskIds().split(",")));
            // 计算计划工时
            Integer planWorkDay = taskEntityList.stream().mapToInt(task -> {
                LocalDate planEndTime = task.getPlanEndTime();
                LocalDate planStartTime = task.getPlanStartTime();
                if (null == planStartTime || null == planEndTime) {
                    return 0;
                }
                return LocalDateUtil.countDaysForLocalDate(planStartTime, planEndTime, holidays);
            }).sum();
            // 赋值
            item.setPlanTaskTime(planWorkDay);
        });
    }

    @Override
    public Boolean exportTaskTimeList(ProjectTaskTimeRecordDTO.PageRecordDto dto) {
        downloadTaskFeign.saveDownloadTask("工时统计", EXPORT_PLM_TASK_TIME_RECORD.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdateByProjectTaskList(List<ProjectTaskEntity> taskList) {
        log.info("ProjectTaskTimeRecordServiceImpl>>>saveOrUpdateByProjectTaskList>>{}", JSONUtil.toJsonStr(taskList));
        if (isEmpty(taskList)) {
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
                insertList.add(new ProjectTaskTimeRecordEntity(entity, holidayDateList));
            } else {
                if (null == entity.getRealityStartTime()) {
                    entity.setRealityStartTime(projectTaskTimeRecordEntity.getRealityStartTime());
                }
                ProjectTaskTimeRecordEntity updateEntity = new ProjectTaskTimeRecordEntity(entity, holidayDateList);

                updateEntity.setId(projectTaskTimeRecordEntity.getId());
                updateList.add(updateEntity);
            }
        });
        if (isNotEmpty(insertList) && !saveBatch(insertList)) {
                log.error("ProjectTaskTimeRecordServiceImpl>>>saveOrUpdateByProjectTaskList>>insertList更新/保存工时记录失败请重试！");
                throw new ServiceException("保存工时记录失败请重试！");
        }
        if (isNotEmpty(updateList) && !updateBatchById(updateList)) {
                log.error("ProjectTaskTimeRecordServiceImpl>>>saveOrUpdateByProjectTaskList>>updateList更新/保存工时记录失败请重试！");
                throw new ServiceException("更新工时记录失败请重试！");
        }
        return true;
    }


    /**
     * 根据产品id 获取到工时信息
     *
     * @param productIds
     * @return java.util.List<com.erp.model.plm.dto.ProjectTaskTimeRecordDTO.TaskWorkTimeDTO>
     * @author yl
     * @date 2023-06-12 18:28
     */
    @Override
    public List<ProjectTaskTimeRecordDTO.TaskWorkTimeDTO> listByProductIds(List<String> productIds) {
        if (CollectionUtils.isEmpty(productIds)) {
            return Collections.emptyList();
        }
        SysCalendarDTO.ListDTO listDTO = new SysCalendarDTO.ListDTO();
        listDTO.setIsWorkDay(Boolean.FALSE);
        List<SysCalendarListVO> holidayList = sysUserFeign.listCalendar(listDTO);
        List<LocalDate> holidays = holidayList.stream().map(SysCalendarListVO::getCalendarDate).collect(Collectors.toList());
        List<ProjectTaskEntity> projectTaskList = projectTaskService.getByProductIds(productIds);
        List<ProjectTaskTimeRecordDTO.TaskWorkTimeDTO> resultList = new ArrayList<>(projectTaskList.size());
        for (ProjectTaskEntity item : projectTaskList) {
            ProjectTaskTimeRecordDTO.TaskWorkTimeDTO result = new ProjectTaskTimeRecordDTO.TaskWorkTimeDTO();
            result.setProductId(item.getProductId());
            result.setTaskId(item.getId());
            Integer planWorkTime = 0;
            LocalDate planStartTime = item.getPlanStartTime();
            LocalDate planEndTime = item.getPlanEndTime();
            if (null != planStartTime && null != planEndTime) {
                planWorkTime = LocalDateUtil.countDaysForLocalDate(planStartTime, planEndTime, holidays);
            }
            result.setTaskTime(planWorkTime);
            resultList.add(result);
        }

        return resultList;

    }

    @Override
    public PagingVO<ProjectTaskTimeRecordPageVO> exportTaskTimeRecord(PagingDTO<ProjectTaskTimeRecordDTO.PageRecordDto> dto) {
        IPage<ProjectTaskTimeRecordPageVO> page = baseMapper.pageTaskTimeRecord(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams(), dto.getPermissionSql());
        initPlanWorkTime(page.getRecords());
        return new PagingVO<>(page);
    }
}
