package com.erp.server.dmp.inout.handler.etl.create;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Pair;
import com.erp.model.dmp.entity.DmpCfgEtlEntity;
import com.erp.model.dmp.entity.DmpEtlTaskEntity;
import com.erp.model.dmp.enums.DmpInputTaskStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpEtlCreateRequest;
import com.erp.server.dmp.inout.dto.request.DmpEtlHotfixCreateRequest;
import com.erp.server.dmp.inout.dto.response.DmpEtlCreateResponse;
import com.erp.server.dmp.service.DmpEtlTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * dmp清洗配置创建dmp_etl_task任务处理器
 *
 * @author Administrator
 *
 */
@Slf4j
@Service
public class DmpEtlHotfixCreateHandler extends DmpEtlBaseCreateHandler {

    @Resource
    private DmpEtlTaskService dmpEtlTaskService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<DmpEtlTaskEntity> createEtlTask(DmpEtlCreateRequest dmpRequest, DmpEtlCreateResponse dmpResponse) {
        DmpCfgEtlEntity dmpCfgEtlEntity = dmpResponse.getDmpCfgEtlEntity();

        List<DmpEtlTaskEntity> dmpEtlTaskEntityList = new ArrayList<>();
        DmpEtlHotfixCreateRequest dmpInputHotfixCreateRequest = (DmpEtlHotfixCreateRequest) dmpRequest;
        DmpEtlTaskEntity dmpEtlTaskEntity = null;
        LocalDateTime startTime = dmpInputHotfixCreateRequest.getStartTime();
        LocalDateTime endTime = dmpInputHotfixCreateRequest.getEndTime();
        boolean splitFlag = dmpInputHotfixCreateRequest.isSplitFlag() && (startTime != null && endTime != null && endTime.isAfter(startTime));
        List<Pair<LocalDateTime, LocalDateTime>> timeList = null;
        timeList = new ArrayList<>();
        timeList.add(new Pair<>(startTime, endTime));
        if (splitFlag) {
            Integer intervalTime = dmpCfgEtlEntity.getIntervalTime();
            if (intervalTime != null && intervalTime > 0) {
                long between = LocalDateTimeUtil.between(startTime, endTime, ChronoUnit.SECONDS);
                if (between > intervalTime) {
                    timeList = new ArrayList<>();
                    while (between > 0) {
                        LocalDateTime offset = LocalDateTimeUtil.offset(startTime, intervalTime, ChronoUnit.SECONDS);
                        if (offset.isAfter(endTime)) {
                            offset = endTime;
                        }
                        timeList.add(new Pair<>(startTime, offset));
                        startTime = offset;
                        between = between - intervalTime;
                    }
                }
            }
        }

        for (Pair<LocalDateTime, LocalDateTime> time : timeList) {
            dmpEtlTaskEntity = new DmpEtlTaskEntity();
            dmpEtlTaskEntity.setCfgEtlId(dmpCfgEtlEntity.getId());
            dmpEtlTaskEntity.setStartTime(time.getKey());
            dmpEtlTaskEntity.setEndTime(time.getValue());
            dmpEtlTaskEntity.setStatus(DmpInputTaskStatusEnum.INIT.getCode());
            dmpEtlTaskEntity.setExecTimeout(dmpInputHotfixCreateRequest.getExecTimeout());
            dmpEtlTaskEntity.setExtendJson(dmpInputHotfixCreateRequest.getExtendJson());
            dmpEtlTaskEntity.setNextExecTime(dmpInputHotfixCreateRequest.getNextExecTime());
            dmpEtlTaskEntityList.add(dmpEtlTaskEntity);
        }
        dmpEtlTaskService.saveBatch(dmpEtlTaskEntityList);
        return dmpEtlTaskEntityList;
    }


}
