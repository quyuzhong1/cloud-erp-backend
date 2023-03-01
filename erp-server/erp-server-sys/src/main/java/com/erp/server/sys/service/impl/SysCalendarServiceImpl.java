package com.erp.server.sys.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HolidayUtils;
import com.erp.model.sys.dto.SysCalendarDTO;
import com.erp.model.sys.entity.SysCalendarEntity;
import com.erp.model.sys.vo.SysCalendarListVO;
import com.erp.server.sys.mapper.SysCalendarMapper;
import com.erp.server.sys.service.SysCalendarService;
import com.common.core.serveice.SuperServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-02-24
 */
@Service
public class SysCalendarServiceImpl extends SuperServiceImpl<SysCalendarMapper, SysCalendarEntity> implements SysCalendarService {

    @Override
    public List<SysCalendarListVO> listByCondition(SysCalendarDTO.ListDTO dto) {
        if(null == dto.getDateType() && null != dto.getCalendarDate()){
            dto.setDateType(2);
        }
        dto.setCalendarDate(LocalDate.now());
        List<SysCalendarEntity> calendarEntityList = lambdaQuery()
                .eq(null != dto.getDateType() && 1 == dto.getDateType(), SysCalendarEntity::getCalendarDate, dto.getCalendarDate())
                .ge(null != dto.getDateType() && 2 == dto.getDateType(), SysCalendarEntity::getCalendarDate, dto.getCalendarDate().with(TemporalAdjusters.firstDayOfMonth()))
                .le(null != dto.getDateType() && 2 == dto.getDateType(), SysCalendarEntity::getCalendarDate, dto.getCalendarDate().with(TemporalAdjusters.lastDayOfMonth()))
                .ge(null != dto.getDateType() && 3 == dto.getDateType(), SysCalendarEntity::getCalendarDate, dto.getCalendarDate().with(TemporalAdjusters.firstDayOfYear()))
                .le(null != dto.getDateType() && 4 == dto.getDateType(), SysCalendarEntity::getCalendarDate, dto.getCalendarDate().with(TemporalAdjusters.lastDayOfYear()))
                .eq(null != dto.getIsManualSet(),SysCalendarEntity::getIsManualSet, dto.getIsManualSet())
                .eq(null != dto.getIsWorkDay(), SysCalendarEntity::getIsWorkDay, dto.getIsWorkDay())
                .eq(null != dto.getOrganization(), SysCalendarEntity::getOrganization, dto.getOrganization())
                .list();
        if(CollectionUtil.isEmpty(calendarEntityList)){
            return new ArrayList<>();
        }
        List<SysCalendarListVO> resultList = calendarEntityList.stream()
                .map(SysCalendarListVO::new)
                .collect(Collectors.toList());
        return resultList;
    }

    @Override
    public Boolean saveOrUpdateBatchDate(SysCalendarDTO.SaveOrUpdateDTO updateDTO) {
        // 查询日期是否存在
        List<SysCalendarEntity> calendarEntityList = lambdaQuery()
                .in(SysCalendarEntity::getCalendarDate, updateDTO.getCalendarDateList())
                .eq(StrUtil.isNotBlank(updateDTO.getOrganization()), SysCalendarEntity::getOrganization, updateDTO.getOrganization())
                .list();
        List<SysCalendarEntity> insertList =new ArrayList<>();
        List<SysCalendarEntity> updateList = new ArrayList<>();
        Map<LocalDate, SysCalendarEntity> entityMap = calendarEntityList.stream()
                .collect(Collectors.toMap(SysCalendarEntity::getCalendarDate, e -> e));
        updateDTO.getCalendarDateList().stream().forEach(entity -> {
            SysCalendarEntity sysCalendarEntity = entityMap.get(entity);
            if (null == sysCalendarEntity){
                SysCalendarEntity insertEntity = new SysCalendarEntity();
                insertEntity.setCalendarDate(entity);
                insertEntity.setIsWorkDay(updateDTO.getIsWorkDay());
                insertEntity.setIsManualSet(Boolean.TRUE);
                if(StrUtil.isNotBlank(updateDTO.getRemark())){
                    insertEntity.setRemark(updateDTO.getRemark());
                }
                insertList.add(insertEntity);
            }else {
                sysCalendarEntity.setIsWorkDay(updateDTO.getIsWorkDay());
                sysCalendarEntity.setIsManualSet(Boolean.TRUE);
                if(StrUtil.isNotBlank(updateDTO.getRemark())){
                    sysCalendarEntity.setRemark(updateDTO.getRemark());
                }
                updateList.add(sysCalendarEntity);
            }
        });
        if (CollectionUtil.isNotEmpty(insertList)){
            if (!saveBatch(insertList)) {
                throw new ServiceException(ApiError.ERROR_9037);
            }

        }
        if (CollectionUtil.isNotEmpty(updateList)){
            if(!updateBatchById(updateList)){
                throw new ServiceException(ApiError.ERROR_9037);
            }
        }
        return Boolean.TRUE;
    }
}
