package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsBillDetailDTO;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.LogisticsCarrierEntity;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.server.tms.mapper.LogisticsBillDetailMapper;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private LogisticsAuthService logisticsAuthService;

    @Autowired
    private LogisticsBillService logisticsBillService;

    @Autowired
    private LogisticsBillCostService logisticsBillCostService;

    @Autowired
    private LogisticsTrackService logisticsTrackService;
    @Autowired
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
            //新增物流费用单
            logisticsBillService.addLogisticsBillCost(billEntity, list);
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
        String dbTrackStatus=detailEntity.getTrackStatus();
        if (signCode.equals(dbTrackStatus) && detailEntity.getIsApiUpdate()) {
            throw new ServiceException(ApiError.ERROR_NOT_UPDATE_TRACK_STATUS);
        }
        detailEntity.setIsApiUpdate(Boolean.FALSE);
        //表示签收
        if (signCode.equals(trackStatus)) {
            detailEntity.setSignTime(trackTime);
        } else {
            detailEntity.setSignTime(null);
        }
        String oldTrackStatus = detailEntity.getTrackStatus();
        String oldTrackStatusName = LogisticTrackStatusEnum.getName(oldTrackStatus);
        String newTrackStatusName = LogisticTrackStatusEnum.getName(trackStatus);
        detailEntity.setTrackStatus(trackStatus);
        detailEntity.setTrackTime(trackTime);
        this.updateById(detailEntity);

        //添加物流轨迹
        addLogisticsTrack(detailEntity,trackTime,trackDesc);

        //操作日志
        String msg = StrUtil.format("用户【{}】从【{}】变更为【{}】 ", UserContext.getDefaultLoginUser().getUserName(), oldTrackStatusName, newTrackStatusName);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_BILL.getCode(), id, "状态变更");
        return BatchResultDTO.success(detailEntity.getId(), detailEntity.getTrackNo(), OperationTypeEnum.UPDATE_STATUS);
    }

    @Override
    public PagingVO<LogisticsBillDetailEntity> getPage(LogisticsBillDetailQueryDTO query) {
        Page<LogisticsBillDetailEntity> page = new Page<>();
        page.setSize(query.getSize());
        page.setCurrent(query.getCurrent());
        IPage<LogisticsBillDetailEntity> result = baseMapper.getTrackPage(page, query);
        return new PagingVO<>(result.getRecords(), (int) result.getTotal(), (int) result.getSize(), (int) result.getCurrent());
    }

    @Override
    public PagingVO<LogisticsTrackDTO.UpdateTrackDTO> getTrackDtoPage(LogisticsBillDetailQueryDTO query) {
        Page<LogisticsTrackDTO.UpdateTrackDTO> page = new Page<>();
        page.setSize(query.getSize());
        page.setCurrent(query.getCurrent());
        IPage<LogisticsTrackDTO.UpdateTrackDTO> result = baseMapper.getTrackDtoPage(page, query);
        buildTrackData(result.getRecords());
        return new PagingVO<>(result.getRecords(), (int) result.getTotal(), (int) result.getSize(), (int) result.getCurrent());
    }

    @Override
    public List<LogisticsTrackDTO.UpdateTrackDTO> listTrackDto(LogisticsBillDetailQueryDTO query) {
        return baseMapper.listTrackDto(query);
    }

    /**
     * 回填数据
     * @param records
     */
    private void buildTrackData(List<LogisticsTrackDTO.UpdateTrackDTO> records) {
        if (CollectionUtils.isEmpty(records)){
            return;
        }
        List<String> carrierIds = records.stream().filter(e -> StringUtils.isNotEmpty(e.getCarrierId())).map(LogisticsTrackDTO.UpdateTrackDTO::getCarrierId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(carrierIds)){
            return;
        }
        List<LogisticsCarrierEntity> carrierEntityList = logisticsCarrierService.listByIds(carrierIds);
        records.forEach(updateTrackDTO -> {
            LogisticsCarrierEntity carrier = carrierEntityList.stream().filter(e -> Objects.nonNull(e) && StringUtils.isNotEmpty(updateTrackDTO.getCarrierId())
                    && e.getId().equals(updateTrackDTO.getCarrierId())).findFirst().orElse(null);
            if (Objects.nonNull(carrier)){
                updateTrackDTO.setCarrierCode(carrier.getCarrierCode());
            }else {
                updateTrackDTO.setCarrierId(updateTrackDTO.getCarrierId());
            }
        });
    }

    @Override
    public List<LogisticsBillDetailEntity> getDetailByTrackNo(String trackNo) {
        return lambdaQuery().eq(LogisticsBillDetailEntity::getTrackNo, trackNo)
                .eq(LogisticsBillDetailEntity::getIsDeleted, false).list();
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
                LogisticsBillEntity logisticsBillEntity = billList.stream().filter(obj -> StrUtil.equals(entity.getMainId(), obj.getId())).findFirst().orElse(null);
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
        // TODO 验证数据 & 数据赋值
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
}
