package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.constant.SqlConstants;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.server.tms.mapper.LogisticsBillDetailMapper;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 物流单明细表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
@Slf4j
@Service
public class LogisticsBillDetailServiceImpl extends SuperServiceImpl<LogisticsBillDetailMapper, LogisticsBillDetailEntity> implements LogisticsBillDetailService {
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private LogisticsAuthService logisticsAuthService;

    @Resource
    private LogisticsBillService logisticsBillService;

    @Resource
    private LogisticsBillCostService logisticsBillCostService;

    @Resource
    private LogisticsTrackService logisticsTrackService;
    @Resource
    @Lazy
    private LogisticsCarrierService logisticsCarrierService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean add(LogisticsBillEntity billEntity, List<LogisticsBillDetailDTO.AddDTO> detailList ,boolean isGenerateCost) {
        if (CollectionUtils.isEmpty(detailList)) {
            return Boolean.FALSE;
        }
        String mainId = billEntity.getId();
        String channelId = billEntity.getChannelId();
        LogisticsAuthEntity authEntity = logisticsAuthService.getByChannelId(channelId);
        List<LogisticsBillDetailEntity> list = BeanMapper.copyList(detailList, LogisticsBillDetailEntity.class);
        list.forEach(l -> {
            l.setMainId(mainId);
            if(StringUtils.isNotBlank(authEntity.getId())){
                l.setLogisticsAuthId(authEntity.getId());
            }
            //重新排序对账次数

        });
        //批量新增
        this.saveBatch(list);
        if (isGenerateCost) {
            //自动生成功能打系统标识
            Boolean originalValue = UserContext.getIsUserSystem();
            UserContext.setIsUserSystem(Boolean.TRUE);
            try {
                //新增物流费用单
                logisticsBillService.addLogisticsBillCost(billEntity, list);
            }finally {
                //恢复系统标识
                UserContext.setIsUserSystem(originalValue);
            }
        }
        return Boolean.TRUE;
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsBillDTO.UpdateDTO updateDTO, String mainId) {
        List<LogisticsBillDetailDTO.UpdateDTO> detailList = updateDTO.getDetailList();
        //原明细数据
        List<LogisticsBillDetailEntity> oldList = this.listByMainIds(Arrays.asList(mainId));
        List<String> deleteIds = getDeleteIds(detailList, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            List<LogisticsBillDetailEntity> removeList = oldList.stream().filter(obj -> deleteIds.contains(obj.getId())).collect(Collectors.toList());
            //操作日志
            List<Pair<String, String>> pairList = removeList.stream().map(obj -> new Pair<>(obj.getMainId(), obj.getTrackNo())).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog("删除了一个运输单【%s】", ModuleTypeEnum.LOGISTICS_BILL.getCode(), pairList, "编辑操作");
            this.removeByIds(deleteIds);
            //删除费用
            logisticsBillCostService.deleteByLogisticsBillDetailIdList(deleteIds);
        }

        List<LogisticsBillDetailEntity> list = BeanMapper.copyList(detailList, LogisticsBillDetailEntity.class);

        // 数据处理
        handleData(list, mainId, Boolean.TRUE);

        //新增或修改明细
        return this.saveOrUpdateBatch(list);
    }

    @Override
    public List<LogisticsBillDetailEntity> listByMainIds(List<String> mainIds) {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(LogisticsBillDetailEntity::getMainId, mainIds).list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeByMainIds(List<String> mainIds,boolean isDeleteCost) {
        List<LogisticsBillDetailEntity> list = listByMainIds(mainIds);
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        List<String> detailIdList = list.stream().map(LogisticsBillDetailEntity::getId).collect(Collectors.toList());
        //删除物流费用明细
        if(isDeleteCost){
            logisticsBillCostService.deleteByLogisticsBillDetailIdList(detailIdList);
        }
        //删除物流明细
        return  this.removeByIds(detailIdList);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateStatus(String id, String trackStatus,LocalDateTime trackTime,String trackDesc) {
        LogisticsBillDetailEntity detailEntity = this.getById(id);
        if (Objects.isNull(detailEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "自发货物流单详情");
        }
        String signCode = LogisticTrackStatusEnum.SIGN.getCode();
        String manualCompleteCode = LogisticTrackStatusEnum.MANUAL_COMPLETE.getCode();
        String dbTrackStatus=detailEntity.getTrackStatus();
        if (signCode.equals(dbTrackStatus) && detailEntity.getIsApiUpdate()) {
            throw new ServiceException(ApiError.ERROR_NOT_UPDATE_TRACK_STATUS);
        }
        detailEntity.setIsApiUpdate(Boolean.FALSE);
        //表示签收
        if (signCode.equals(trackStatus) || Objects.equals(manualCompleteCode, trackStatus)) {
            detailEntity.setSignTime(trackTime);
        } else {
            detailEntity.setSignTime(null);
        }
        String oldTrackStatus = detailEntity.getTrackStatus();
        String oldTrackStatusName = LogisticTrackStatusEnum.getName(oldTrackStatus);
        String newTrackStatusName = LogisticTrackStatusEnum.getName(trackStatus);
        detailEntity.setTrackStatus(trackStatus);
        detailEntity.setTrackTime(trackTime);
        detailEntity.setTrackContent(trackDesc);
        this.updateById(detailEntity);

        //添加物流轨迹
        addLogisticsTrack(detailEntity,trackTime,trackDesc);

        //推送数帝云
        if (detailEntity.getSignTime() != null) {
            LogisticsBillEntity billEntity = logisticsBillService.getById(detailEntity.getMainId());
            logisticsBillService.pushSdyFieldHandler(billEntity, LogisticTrackStatusEnum.getName(trackStatus));
        }

        //操作日志
        String msg = CharSequenceUtil.format("用户【{}】从【{}】变更为【{}】 ", UserContext.getDefaultLoginUser().getUserName(), oldTrackStatusName, newTrackStatusName);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_BILL.getCode(), id, "状态变更");
        return BatchResultDTO.success(detailEntity.getId(), detailEntity.getTrackNo(), OperationTypeEnum.UPDATE_STATUS);
    }

    @Override
    public List<LogisticsTrackDTO.UpdateTrackDTO> listTrackDto(LogisticsBillDetailQueryDTO query) {
        return baseMapper.listTrackDto(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTrackNo(LogisticsBillDTO.UpdateTrackNoDTO billDTO) {
        List<String> outstockIdList = billDTO.getOutstockIdList();
        String trackNo = billDTO.getTrackNo();
        if (CollectionUtils.isEmpty(outstockIdList) || StringUtils.isBlank(trackNo)) {
            return Boolean.FALSE;
        }
        List<LogisticsBillEntity> billList = logisticsBillService.listByOutstockIdList(outstockIdList);
        if (CollectionUtils.isNotEmpty(billList)) {
            List<String> billIdList = billList.stream().map(LogisticsBillEntity::getId).collect(Collectors.toList());
            this.removeByMainIds(billIdList,true);
            List<LogisticsBillDetailEntity> billDetailList = new ArrayList<>(billIdList.size());
            for (String mainId : billIdList) {
                LogisticsBillDetailEntity detailEntity = new LogisticsBillDetailEntity();
                detailEntity.setMainId(mainId);
                detailEntity.setTrackNo(trackNo);
                billDetailList.add(detailEntity);
            }
            this.saveBatch(billDetailList);
            for (LogisticsBillDetailEntity entity : billDetailList) {
                LogisticsBillEntity logisticsBillEntity = billList.stream().filter(obj -> CharSequenceUtil.equals(entity.getMainId(), obj.getId())).findFirst().orElse(null);
                //新增物流费用单
                logisticsBillService.addLogisticsBillCost(logisticsBillEntity,Arrays.asList(entity));
            }
        }
        return Boolean.FALSE;
    }

    @Override
    public List<LogisticsBillDetailEntity> listByTrackNo(List<String> trackNoList) {
        if (CollectionUtils.isEmpty(trackNoList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(LogisticsBillDetailEntity::getTrackNo, trackNoList).list();
    }

    @Override
    public List<LogisticsBillDetailEntity> listByPlatformCodeAndTrackNo(List<String> platformCodeList, List<String> trackNoList) {
        if (CollectionUtils.isEmpty(platformCodeList) || CollectionUtils.isEmpty(trackNoList)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listByPlatformCodeAndTrackNo(platformCodeList,trackNoList);
    }

    /**
     * @description: 添加物流轨迹
     * @author Will
     * @date: 2024/5/9 8:55
     * @param detailEntity
     * @param trackTime
     * @param trackDesc
     */
    private void addLogisticsTrack (LogisticsBillDetailEntity detailEntity,LocalDateTime trackTime,String trackDesc) {
        if (CharSequenceUtil.isBlank(detailEntity.getTrackNo())){
            return;//不记录空跟踪号轨迹
        }
        LogisticsTrackDTO.AddDTO addDTO = new LogisticsTrackDTO.AddDTO();
        addDTO.setTrackNo(detailEntity.getTrackNo());
        addDTO.setContent(trackDesc);
        addDTO.setTrackTime(trackTime);
        addDTO.setStatus(detailEntity.getTrackStatus());
        logisticsTrackService.add(addDTO);
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(List<LogisticsBillDetailEntity> logisticsBillDetailEntityList, String mainId, Boolean isUpdate) {
        
    }

    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<LogisticsBillDetailDTO.UpdateDTO> newList, List<LogisticsBillDetailEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(LogisticsBillDetailDTO.UpdateDTO::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(LogisticsBillDetailEntity
                ::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    @Override
    public void updateLogisticsBillDetailByTrackNo(LogisticsTrackEntity logisticsTrackEntity) {
        if (Objects.isNull(logisticsTrackEntity) || CharSequenceUtil.isBlank(logisticsTrackEntity.getTrackNo()) || CharSequenceUtil.isBlank(logisticsTrackEntity.getOrderStatus())){
            return;
        }
        LocalDateTime signTime = null;
        String trackStatus = logisticsTrackEntity.getOrderStatus();
        if (LogisticTrackStatusEnum.SIGN.getCode().equalsIgnoreCase(trackStatus)) {
            signTime = logisticsTrackEntity.getTrackTime();
        }
        LocalDateTime trackTime = Objects.nonNull(logisticsTrackEntity.getTrackTime()) ? logisticsTrackEntity.getTrackTime() : LocalDateTime.now();
        //根据跟踪号查询更新
        this.lambdaUpdate().eq(LogisticsBillDetailEntity::getTrackNo, logisticsTrackEntity.getTrackNo())
                .set(LogisticsBillDetailEntity::getIsApiUpdate, Boolean.TRUE)
                .set(LogisticsBillDetailEntity::getTrackStatus, trackStatus)
                .set(LogisticsBillDetailEntity::getTrackTime, trackTime)
                .set(LogisticsBillDetailEntity::getTrackContent,logisticsTrackEntity.getContent())
                .set(LogisticsBillDetailEntity::getSignTime, signTime)
                .set(LogisticsBillDetailEntity::getUpdateTime, LocalDateTime.now())
                .update();
        //根据运单号查询更新
        if (CharSequenceUtil.isNotBlank(logisticsTrackEntity.getTrackNo())){
            baseMapper.updateTransportNo(Collections.singletonList(logisticsTrackEntity.getTrackNo()),Boolean.TRUE,trackStatus,signTime, trackTime,logisticsTrackEntity.getContent());
        }

        LogisticsBillDetailEntity detailEntity = lambdaQuery().eq(LogisticsBillDetailEntity::getTrackNo, logisticsTrackEntity.getTrackNo()).last(SqlConstants.LIMIT_1).one();
        if (ObjectUtil.isNotEmpty(detailEntity)) {
            LogisticsBillEntity billEntity = logisticsBillService.getById(detailEntity.getMainId());
            if (detailEntity.getSignTime() != null) {
                //同步速递云运单
                logisticsBillService.pushSdyFieldHandler(billEntity, LogisticTrackStatusEnum.getName(trackStatus));
            }
        }
    }

    @Override
    public void batchUpdateTrackStatus(List<String> trackNoList, String code, LocalDateTime signTime, LocalDateTime trackTime) {
        if (CollectionUtils.isEmpty(trackNoList) || CharSequenceUtil.isBlank(code)){
            return;
        }
        this.lambdaUpdate().in(LogisticsBillDetailEntity::getTrackNo, trackNoList)
                .set(LogisticsBillDetailEntity::getIsApiUpdate, Boolean.TRUE)
                .set(LogisticsBillDetailEntity::getTrackStatus, code)
                .set(Objects.nonNull(trackTime), LogisticsBillDetailEntity::getTrackTime, trackTime)
                .set(LogisticsBillDetailEntity::getUpdateTime, LocalDateTime.now())
                .set(Objects.nonNull(signTime), LogisticsBillDetailEntity::getSignTime, signTime)
                .update();
        //根据运单号查询更新
        if (CollectionUtils.isNotEmpty(trackNoList)){
            baseMapper.updateTransportNo(trackNoList,Boolean.TRUE,code,signTime, LocalDateTime.now(), "");
        }
    }

    @Override
    public void updateRegisterStatus(List<LogisticsBillDetailDTO.BillDetailErrorDTO> errorList, int status) {
        if (CollectionUtils.isEmpty(errorList)){
            return;
        }
        errorList.forEach(e ->{
            this.lambdaUpdate().set(LogisticsBillDetailEntity::getRegisterStatus, status)
                    .set(LogisticsBillDetailEntity::getUpdateTime, LocalDateTime.now())
                    .set(LogisticsBillDetailEntity::getTrackTime, LocalDateTime.now())
                    .set(LogisticsBillDetailEntity::getRegisterResult, e.getErrorMsg())
                    .eq(LogisticsBillDetailEntity::getId, e.getId()).update();
        });
    }

    @Override
    public void updateRegisterStatusByParams(List<LogisticsBillDetailDTO.BillDetailDTO> sucessList, int status) {
        if (CollectionUtils.isEmpty(sucessList)){
            return;
        }
        //根据跟踪号进行的更新
        sucessList.forEach(e ->{
            this.lambdaUpdate().set(LogisticsBillDetailEntity::getRegisterStatus, status)
                    .set(LogisticsBillDetailEntity::getUpdateTime, LocalDateTime.now())
                    .set(LogisticsBillDetailEntity::getTrackTime, LocalDateTime.now())
                    .set(CharSequenceUtil.isNotBlank(e.getPlatformOrderNo()), LogisticsBillDetailEntity::getPlatformOrderNo, e.getPlatformOrderNo())
                    .eq(LogisticsBillDetailEntity::getTrackNo, e.getTrackNo()).ne(LogisticsBillDetailEntity::getRegisterStatus, status).update();
        });
        //根据运单号关联的更新
        sucessList.forEach(e ->{
            baseMapper.updateRegisticsStatus(e.getTrackNo(), e.getPlatformOrderNo(), status);
        });
    }

    @Override
    public void updateTrackEnableByIds(List<String> detailIds) {
        if (CollUtil.isEmpty(detailIds)){
            return;
        }
        this.lambdaUpdate().set(LogisticsBillDetailEntity::getTrackEnable, Boolean.FALSE)
               .in(LogisticsBillDetailEntity::getId, detailIds).update();
    }

    @Override
    public void updateRegisterParams(List<LogisticsTrackDTO.UpdateTrackDTO> refList) {
        if (CollUtil.isEmpty(refList)){
            return;
        }
        refList.forEach(e -> this.lambdaUpdate()
                .set(LogisticsBillDetailEntity::getRegisterMobile, CharSequenceUtil.isNotBlank(e.getTelNumber()) ? e.getTelNumber() : "")
                .set(LogisticsBillDetailEntity::getThirdRefId, CharSequenceUtil.isNotBlank(e.getThirdRefId()) ? e.getThirdRefId() : "")
                .set(LogisticsBillDetailEntity::getUpdateTime, LocalDateTime.now())
                .eq(LogisticsBillDetailEntity::getId, e.getId()).update());
    }

    @Override
    public Integer countByThirdRefId(String id) {
        return this.lambdaQuery().eq(LogisticsBillDetailEntity::getThirdRefId, id).count();
    }
}
