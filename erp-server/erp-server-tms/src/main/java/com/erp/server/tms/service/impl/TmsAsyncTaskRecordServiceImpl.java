package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.tms.dto.CfgSettingValueDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.TmsAsyncTaskDetailEntity;
import com.erp.model.tms.entity.CfgSettingEntity;
import com.erp.model.tms.entity.TmsAsyncTaskRecordEntity;
import com.erp.model.tms.enums.TmsAsyncTaskRecordExecTypeEnum;
import com.erp.model.tms.enums.TmsAsyncTaskRecordStatusEnum;
import com.erp.model.tms.enums.CfgSettingEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.mapper.TmsAsyncTaskRecordMapper;
import com.erp.server.tms.service.TmsAsyncTaskDetailService;
import com.erp.server.tms.service.CfgSettingService;
import com.erp.server.tms.service.TmsAsyncTaskRecordService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.*;

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
public class TmsAsyncTaskRecordServiceImpl extends SuperServiceImpl<TmsAsyncTaskRecordMapper, TmsAsyncTaskRecordEntity> implements TmsAsyncTaskRecordService {

    @Resource
    private TmsAsyncTaskDetailService tmsAsyncTaskDetailService;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private CfgSettingService cfgSettingService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;


    /**
     * 新增手动任务
     */
    @Override
    public String addManualTask(String businessType, String json){
        Integer count = lambdaQuery()
                .eq(TmsAsyncTaskRecordEntity::getBusinessType, businessType)
                .eq(TmsAsyncTaskRecordEntity::getDataJson, json)
                .in(TmsAsyncTaskRecordEntity::getStatus, Arrays.asList(TmsAsyncTaskRecordStatusEnum.ING.getCode(), TmsAsyncTaskRecordStatusEnum.PENDING.getCode()))
                .count();
        if(count > 0){
            return null;
        }

        TmsAsyncTaskRecordEntity entity = new TmsAsyncTaskRecordEntity();
        //重置任务ID
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_Z);
        entity.setCode(code);
        entity.setBusinessType(businessType);
        entity.setDataJson(json);
        entity.setStartTime(LocalDateTime.now());
        entity.setStatus(TmsAsyncTaskRecordStatusEnum.PENDING.getCode());
        entity.setExecType(TmsAsyncTaskRecordExecTypeEnum.MANUAL.getCode());
        return save(entity) ? entity.getId() : null;
    }

    /**
     * 新增自动任务
     */
    @Override
    public String addAutoTask(String businessType, String json){
        Integer count = lambdaQuery()
                .eq(TmsAsyncTaskRecordEntity::getBusinessType, businessType)
                .eq(TmsAsyncTaskRecordEntity::getDataJson, json)
                .in(TmsAsyncTaskRecordEntity::getStatus, Arrays.asList(TmsAsyncTaskRecordStatusEnum.ING.getCode(), TmsAsyncTaskRecordStatusEnum.PENDING.getCode()))
                .count();
        if(count > 0){
            return null;
        }
        //默认8小时
        Integer execTimeout = null;
        Integer errorCount = null;
        //获取分摊配置--任务超时时间
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.RECONCILIATION_CYCLE.getCode());
        if(Objects.nonNull(cfgSettingEntity) && ObjectUtil.isEmpty(cfgSettingEntity.getDataJson())){
            CfgSettingValueDTO.ReconciliationCycleDTO reconciliationCycleDTO = JSONUtil.toBean(cfgSettingEntity.getDataJson(),CfgSettingValueDTO.ReconciliationCycleDTO.class);
            //头程对账单
            if(Objects.equals(businessType,SourceTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode())){
                execTimeout = reconciliationCycleDTO.getFirstMileExecTimeout();
            }
            //报关对账
            if(Objects.equals(businessType,SourceTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode())){
                execTimeout = reconciliationCycleDTO.getDeclareExecTimeout();
            }
            //头程分摊
            if(Objects.equals(businessType,SourceTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode())){
                execTimeout = reconciliationCycleDTO.getFirstMileAllocationeExecTimeout();
            }
            //小包分摊
            if(Objects.equals(businessType,SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode())){
                execTimeout = reconciliationCycleDTO.getPackageBeginExecTimeout();
            }
            //中转分摊
            if(Objects.equals(businessType,SourceTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode())){
                execTimeout = reconciliationCycleDTO.getTransferBeginExecTimeout();
            }
        }

        TmsAsyncTaskRecordEntity entity = new TmsAsyncTaskRecordEntity();
        //重置任务ID
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_Z);
        entity.setCode(code);
        entity.setBusinessType(businessType);
        entity.setDataJson(json);
        entity.setStartTime(LocalDateTime.now());
        entity.setStatus(TmsAsyncTaskRecordStatusEnum.PENDING.getCode());
        entity.setExecType(TmsAsyncTaskRecordExecTypeEnum.AUTO.getCode());
        if(Objects.nonNull(execTimeout)){
            entity.setExecTimeout(execTimeout);
        }
        if(Objects.nonNull(errorCount)){
            entity.setErrorCount(errorCount);
        }
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

    /**
     * 任务超时中止
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void terminateTaskTimeout(String taskId, String errorMsg) {
        List<TmsAsyncTaskDetailEntity> detailEntityList = tmsAsyncTaskDetailService.lambdaQuery().eq(TmsAsyncTaskDetailEntity::getMainId, taskId).list();
        int detailCount = detailEntityList.size();
        long finishCount = detailEntityList.stream().filter(e -> e.getStatus().equals(TmsAsyncTaskRecordStatusEnum.FINISH.getCode())).count();
        tmsAsyncTaskDetailService.lambdaUpdate().set(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FAILED.getCode())
                .set(TmsAsyncTaskDetailEntity::getEndTime, LocalDateTime.now())
                .set(TmsAsyncTaskDetailEntity::getErrorData, errorMsg)
                .eq(TmsAsyncTaskDetailEntity::getMainId, taskId)
                .ne(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FINISH.getCode())
                .update();

        lambdaUpdate()
                .set(TmsAsyncTaskRecordEntity::getStatus,  TmsAsyncTaskRecordStatusEnum.FAILED.getCode())
                .set(TmsAsyncTaskRecordEntity::getEndTime, LocalDateTime.now())
                .set(TmsAsyncTaskRecordEntity::getErrorData, errorMsg)
                .set(TmsAsyncTaskRecordEntity::getDetailCount, detailCount)
                .set(TmsAsyncTaskRecordEntity::getErrorCount, detailCount - finishCount)
                .eq(TmsAsyncTaskRecordEntity::getId, taskId)
                .update();
    }

    @Override
    public void updateTaskFinally(String taskId) {
        TmsAsyncTaskRecordEntity mainEntity = getById(taskId);
        if(Objects.nonNull(mainEntity)){
            Integer count = tmsAsyncTaskDetailService.lambdaQuery().eq(TmsAsyncTaskDetailEntity::getMainId, taskId).ne(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.ING.getCode()).count();
            if(Objects.equals(mainEntity.getDetailCount(), count)){
                Integer failedCount = tmsAsyncTaskDetailService.lambdaQuery().eq(TmsAsyncTaskDetailEntity::getMainId, taskId).eq(TmsAsyncTaskDetailEntity::getStatus, TmsAsyncTaskRecordStatusEnum.FAILED.getCode()).count();
                if(failedCount > 0){
                    this.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FAILED.getCode(),"");
                }else {
                    this.updateTask(taskId, TmsAsyncTaskRecordStatusEnum.FINISH.getCode(),"");
                }
            }
        }
    }

    @Override
    public List<TmsAsyncTaskRecordDTO.TabListDTO> tabList(PermissionsDTO param) {
        TmsAsyncTaskRecordDTO.PagingParamDTO searchParam = new TmsAsyncTaskRecordDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<TmsAsyncTaskRecordDTO.TabListDTO> list = baseMapper.tabList(searchParam);

        // 获取状态列表
        List<String> statusList = TmsAsyncTaskRecordStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(TmsAsyncTaskRecordDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if (!existStatusList.contains(status)) {
                list.add(new TmsAsyncTaskRecordDTO.TabListDTO(status, "", 0));
            }
        });

        list.stream().forEach(e ->{
            e.setTabFlagName(TmsAsyncTaskRecordStatusEnum.getName(e.getTabFlag()));
        });

        // 修改为按照 TmsAsyncTaskRecordStatusEnum 枚举声明顺序排序
        list.sort(Comparator.comparingInt(tabDto -> {
            TmsAsyncTaskRecordStatusEnum statusEnum = TmsAsyncTaskRecordStatusEnum.getByCode(tabDto.getTabFlag());
            return statusEnum != null ? statusEnum.ordinal() : Integer.MAX_VALUE;
        }));

        // 在列表开头添加"全部"统计
        list.add(0, new TmsAsyncTaskRecordDTO.TabListDTO("all","全部", 0));
        return list;
    }

    @Override
    public PagingVO<TmsAsyncTaskRecordDTO.ListDTO> paging(PagingDTO<TmsAsyncTaskRecordDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TmsAsyncTaskRecordDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<TmsAsyncTaskRecordDTO.ListDTO> list) {
        if(CollUtil.isEmpty(list)) {
            return;
        }
        for (TmsAsyncTaskRecordDTO.ListDTO data : list) {
            data.setSysModuleName("TMS系统");
            data.setBusinessTypeName(SourceTypeEnum.getName(data.getBusinessType()));
            data.setStatusName(TmsAsyncTaskRecordStatusEnum.getName(data.getStatus()));
            data.setExecTypeName(TmsAsyncTaskRecordExecTypeEnum.getName(data.getExecType()));
        }
    }


    @Override
    public PagingVO<TmsAsyncTaskRecordDTO.DetailListDTO> pagingError(PagingDTO<TmsAsyncTaskRecordDTO.PagingDetailParamDTO> pagingParamDTO) {
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        String id = pagingParamDTO.getParams().getId();
        IPage<TmsAsyncTaskRecordDTO.DetailListDTO> pageData = this.baseMapper.pagingError(query, id);
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        return new PagingVO(pageData);
    }

    @Override
    public void exportList(TmsAsyncTaskRecordDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("异步任务导出", EXPORT_TMS_ASYNC_TASK_RECORD.getCode(), param);
    }


    @Override
    public void exportError(TmsAsyncTaskRecordDTO.PagingDetailParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("异步任务错误导出", EXPORT_TMS_ASYNC_TASK_DETAIL.getCode(), param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStartTime(TmsAsyncTaskRecordDTO.UpdateDTO dto) {
        TmsAsyncTaskRecordEntity entity = getById(dto.getId());
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("异步任务记录不存在");
        }
        //时间戳转LocalDateTime
        LocalDateTime startTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(dto.getStartTime()),
                ZoneId.systemDefault()
        );

        //判断dto的startTime是否比entity的startTime大
        if (entity.getStartTime().isAfter(startTime)) {
            throw new ServiceException("不能早于当前的任务执行时间");
        }

        lambdaUpdate()
                .set(TmsAsyncTaskRecordEntity::getStartTime, startTime)
                .eq(TmsAsyncTaskRecordEntity::getId, dto.getId())
                .update();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO retry(String id) {
        TmsAsyncTaskRecordEntity entity = getById(id);
        //数据校验
        checkData(entity);

        //默认8小时
        Integer execTimeout = null;
        //获取分摊配置--任务超时时间
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.RECONCILIATION_CYCLE.getCode());
        if(Objects.nonNull(cfgSettingEntity) && ObjectUtil.isEmpty(cfgSettingEntity.getDataJson())){
            CfgSettingValueDTO.ReconciliationCycleDTO reconciliationCycleDTO = JSONUtil.toBean(cfgSettingEntity.getDataJson(),CfgSettingValueDTO.ReconciliationCycleDTO.class);
            String businessType = entity.getBusinessType();
            //头程对账单
            if(Objects.equals(businessType,SourceTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode())){
                execTimeout = reconciliationCycleDTO.getFirstMileExecTimeout();
            }
            //报关对账
            if(Objects.equals(businessType,SourceTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode())){
                execTimeout = reconciliationCycleDTO.getDeclareExecTimeout();
            }
            //头程分摊
            if(Objects.equals(businessType,SourceTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode())){
                execTimeout = reconciliationCycleDTO.getFirstMileAllocationeExecTimeout();
            }
            //小包分摊
            if(Objects.equals(businessType,SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode())){
                execTimeout = reconciliationCycleDTO.getPackageBeginExecTimeout();
            }
            //中转分摊
            if(Objects.equals(businessType,SourceTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode())){
                execTimeout = reconciliationCycleDTO.getTransferBeginExecTimeout();
            }
        }

        TmsAsyncTaskRecordEntity newTask = new TmsAsyncTaskRecordEntity();
        //重置任务ID
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_Z);
        newTask.setCode(code);
        //减少可以重试的次数
        newTask.setErrorCount(entity.getErrorCount() - 1 );
        newTask.setStartTime(LocalDateTime.now());
        newTask.setDataJson(entity.getDataJson());
        newTask.setBusinessType(entity.getBusinessType());
        newTask.setStatus(TmsAsyncTaskRecordStatusEnum.PENDING.getCode());
        if(Objects.nonNull(execTimeout)){
            newTask.setExecTimeout(execTimeout);
        }
        newTask.setExecType(TmsAsyncTaskRecordExecTypeEnum.AUTO.getCode());
        boolean save = save(newTask);
        if(!save){
            throw new ServiceException("保存失败");
        }
        //表示任务已重试过
        entity.setIsRetry(Boolean.TRUE);
        updateById(entity);
        return BatchResultDTO.success(newTask.getId(), newTask.getCode(), OperationTypeEnum.ADD);
    }

    private static void checkData(TmsAsyncTaskRecordEntity entity) {
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
        if(Objects.equals(status, TmsAsyncTaskRecordStatusEnum.FAILED.getCode())){
            throw new ServiceException("仅支持失败任务重试");
        }

        String execType = entity.getExecType();
        if(StringUtils.isBlank(execType) && !Objects.equals(execType, TmsAsyncTaskRecordExecTypeEnum.AUTO.getCode())){
            throw new ServiceException("仅支持执行类型为自动的任务重试");
        }
        String dataJson = entity.getDataJson();
        if(StringUtils.isBlank(dataJson) || Objects.equals(dataJson,"{}")){
            throw new ServiceException("dataJson为空，无法重新创建任务重试");
        }
    }

    @Override
    public BatchResultDTO errorRetry(String id) {
        TmsAsyncTaskRecordEntity entity = getById(id);
        //数据校验
        checkData(entity);
        Integer errorCount = entity.getErrorCount();
        if(null == errorCount || errorCount == 0){
            throw new ServiceException("未找到错误明细");
        }
        //失败明细业务id集合
        List<String> businessIds = tmsAsyncTaskDetailService.listErrorDetail(id).stream().map(TmsAsyncTaskDetailEntity::getBusinessId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if(CollUtil.isEmpty(businessIds)){
            throw new ServiceException("未找到错误明细");
        }
        //默认8小时
        Integer execTimeout = null;
        //获取分摊配置--任务超时时间
        CfgSettingEntity cfgSettingEntity = cfgSettingService.getByKey(CfgSettingEnum.RECONCILIATION_CYCLE.getCode());
        if(Objects.nonNull(cfgSettingEntity) && ObjectUtil.isEmpty(cfgSettingEntity.getDataJson())){
            CfgSettingValueDTO.ReconciliationCycleDTO reconciliationCycleDTO = JSONUtil.toBean(cfgSettingEntity.getDataJson(),CfgSettingValueDTO.ReconciliationCycleDTO.class);
            String businessType = entity.getBusinessType();
            //头程对账单
            if(Objects.equals(businessType,SourceTypeEnum.TMS_FIRST_MILE_RECONCILIATION.getCode())){
                execTimeout = reconciliationCycleDTO.getFirstMileExecTimeout();
            }
            //报关对账
            if(Objects.equals(businessType,SourceTypeEnum.TMS_B2C_DECLARE_RECONCILIATION.getCode())){
                execTimeout = reconciliationCycleDTO.getDeclareExecTimeout();
            }
            //头程分摊
            if(Objects.equals(businessType,SourceTypeEnum.FIRST_MILE_COST_ALLOCATION.getCode())){
                execTimeout = reconciliationCycleDTO.getFirstMileAllocationeExecTimeout();
            }
            //小包分摊
            if(Objects.equals(businessType,SourceTypeEnum.SMALL_BAG_COST_ALLOCATION.getCode())){
                execTimeout = reconciliationCycleDTO.getPackageBeginExecTimeout();
            }
            //中转分摊
            if(Objects.equals(businessType,SourceTypeEnum.TRANSFER_DECLARE_COST_ALLOCATION.getCode())){
                execTimeout = reconciliationCycleDTO.getTransferBeginExecTimeout();
            }
        }

        //entity.getDataJson()转TmsAsyncTaskRecordDTO.PushDTO实体类
        TmsAsyncTaskRecordDTO.PushDTO pushDTO = JSONUtil.toBean(entity.getDataJson(),TmsAsyncTaskRecordDTO.PushDTO.class);
        pushDTO.setIds(businessIds);

        TmsAsyncTaskRecordEntity newTask = new TmsAsyncTaskRecordEntity();
        //重置任务ID
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_Z);
        newTask.setCode(code);
        //减少可以重试的次数
        newTask.setErrorCount(entity.getErrorCount() - 1 );
        newTask.setStartTime(LocalDateTime.now());
        newTask.setDataJson(JSONUtil.toJsonStr(pushDTO));
        newTask.setBusinessType(entity.getBusinessType());
        newTask.setStatus(TmsAsyncTaskRecordStatusEnum.PENDING.getCode());
        if(Objects.nonNull(execTimeout)){
            newTask.setExecTimeout(execTimeout);
        }
        newTask.setExecType(TmsAsyncTaskRecordExecTypeEnum.AUTO.getCode());
        boolean save = save(newTask);
        if(!save){
            throw new ServiceException("保存失败");
        }
        //表示任务已重试过
        entity.setIsRetry(Boolean.TRUE);
        updateById(entity);
        return BatchResultDTO.success(newTask.getId(), newTask.getCode(), OperationTypeEnum.ADD);
    }


}