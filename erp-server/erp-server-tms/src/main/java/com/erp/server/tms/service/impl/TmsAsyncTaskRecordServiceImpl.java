package com.erp.server.tms.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.AsyncTaskDetailRecordEntity;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.model.tms.enums.AsyncTaskRecordStatusEnum;
import com.erp.server.tms.mapper.AsyncTaskRecordMapper;
import com.erp.server.tms.service.AsyncTaskDetailRecordService;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 异步任务记录 服务实现类
 * </p>
 *
 * @author jack
 * @since 2026-01-28
 */
@Slf4j
@Service
public class TmsAsyncTaskRecordServiceImpl extends SuperServiceImpl<AsyncTaskRecordMapper, TmsAsyncTaskRecordEntity> implements TmsAsyncTaskRecordService {

    @Resource
    private AsyncTaskDetailRecordService asyncTaskDetailRecordService;

    @Override
    public String addTask(String businessType, String json){
        Integer count = lambdaQuery()
                .eq(TmsAsyncTaskRecordEntity::getBusinessType, businessType)
                .eq(TmsAsyncTaskRecordEntity::getDataJson, json)
                .eq(TmsAsyncTaskRecordEntity::getStatus, AsyncTaskRecordStatusEnum.ING.getCode())
                .count();
        if(count > 0){
            return null;
        }

        TmsAsyncTaskRecordEntity entity = new TmsAsyncTaskRecordEntity();
        entity.setBusinessType(businessType);
        entity.setDataJson(json);
        entity.setStartTime(LocalDateTime.now());
        entity.setStatus(AsyncTaskRecordStatusEnum.ING.getCode());
        return save(entity) ? entity.getId() : null;
    }

    @Override
    public void updateTask(String taskId,String status, String errorMsg) {
        lambdaUpdate()
                .set(TmsAsyncTaskRecordEntity::getStatus, status)
                .set(TmsAsyncTaskRecordEntity::getEndTime, LocalDateTime.now())
                .set(TmsAsyncTaskRecordEntity::getErrorData, errorMsg)
                .eq(TmsAsyncTaskRecordEntity::getId, taskId)

                .update();
    }

    @Override
    public void updateTaskFinally(String taskId) {
        TmsAsyncTaskRecordEntity mainEntity = getById(taskId);
        if(Objects.nonNull(mainEntity)){
            Integer count = asyncTaskDetailRecordService.lambdaQuery().eq(AsyncTaskDetailRecordEntity::getMainId, taskId).ne(AsyncTaskDetailRecordEntity::getStatus,AsyncTaskRecordStatusEnum.ING.getCode()).count();
            if(Objects.equals(mainEntity.getDetailCount(), count)){
                Integer failedCount = asyncTaskDetailRecordService.lambdaQuery().eq(AsyncTaskDetailRecordEntity::getMainId, taskId).eq(AsyncTaskDetailRecordEntity::getStatus, AsyncTaskRecordStatusEnum.FAILED.getCode()).count();

                if(failedCount == 0){
                    this.updateTask(taskId,AsyncTaskRecordStatusEnum.SUCCESS.getCode(),"");
                }else if(failedCount > 0 && failedCount == count){
                    this.updateTask(taskId,AsyncTaskRecordStatusEnum.FAILED.getCode(),"");
                }else if(failedCount > 0 && failedCount != count){
                    this.updateTask(taskId,AsyncTaskRecordStatusEnum.PART_SUCCESS.getCode(),"");
                }
            }
        }
    }

    @Override
    public List<TmsAsyncTaskRecordDTO.TabListDTO> tabList(PermissionsDTO dto) {
        return Collections.emptyList();
    }

    @Override
    public PagingVO<TmsAsyncTaskRecordDTO.ListDTO> paging(PagingDTO<TmsAsyncTaskRecordDTO.PagingParamDTO> dto) {
        return null;
    }

    @Override
    public void exportList(TmsAsyncTaskRecordDTO.PagingParamDTO dto, HttpServletResponse response) {

    }

    @Override
    public PagingVO<TmsAsyncTaskRecordDTO.DetailListDTO> pagingError(PagingDTO<TmsAsyncTaskRecordDTO.PagingDetailParamDTO> dto) {
        return null;
    }

    @Override
    public void exportError(TmsAsyncTaskRecordDTO.PagingDetailParamDTO dto, HttpServletResponse response) {

    }

    @Override
    public void updateStartTime(TmsAsyncTaskRecordDTO.UpdateDTO dto) {

    }

    @Override
    public BatchResultDTO retry(String id) {
        TmsAsyncTaskRecordEntity entity = getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("异步任务记录不存在");
        }

        Boolean isRetry = entity.getIsRetry();
        if(isRetry){
            throw new ServiceException("已有重试任务，无法再次重试");
        }

        Integer retryTimes = entity.getRetryTimes();
        if(null == retryTimes || retryTimes == 0){
            throw new ServiceException("已超过最大重试次数");
        }

        String status = entity.getStatus();
        if(!Objects.equals(status, AsyncTaskRecordStatusEnum.FAILED.getCode())){
            throw new ServiceException("仅支持失败任务重试");
        }

        Integer errorCount = entity.getErrorCount();
        if(null == errorCount || errorCount == 0){
            throw new ServiceException("不存在错误明细");
        }

        String dataJson = entity.getDataJson();
        if(StringUtils.isBlank(dataJson) || Objects.equals(dataJson,"{}")){
            throw new ServiceException("dataJson为空，无法重新创建任务重试");
        }
        TmsAsyncTaskRecordEntity newTask = new TmsAsyncTaskRecordEntity();
        BeanMapper.copy(entity,newTask);
        newTask.setId(null);
        newTask.setErrorCount(entity.getErrorCount() - 1 );
        newTask.setStartTime(LocalDateTime.now());

        return BatchResultDTO.success(newTask.getId(), newTask.getCode(), OperationTypeEnum.ADD);
    }

    @Override
    public BatchResultDTO errorRetry(String id) {
        return null;
    }


}