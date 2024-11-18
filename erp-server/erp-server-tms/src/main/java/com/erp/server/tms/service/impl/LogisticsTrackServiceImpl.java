package com.erp.server.tms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.server.tms.convert.TrackDataConverter;
import com.erp.server.tms.mapper.LogisticsTrackMapper;
import com.erp.server.tms.service.LogisticsBillDetailService;
import com.erp.server.tms.service.LogisticsTrackService;
import com.erp.server.tms.service.OperateLogService;
import com.google.common.collect.Lists;
import com.sdk.tms.track123.dto.PlatformTrackDTO;
import io.seata.common.util.StringUtils;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 物流轨迹表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2023-11-14
 */
@Slf4j
@Service
public class LogisticsTrackServiceImpl extends SuperServiceImpl<LogisticsTrackMapper, LogisticsTrackEntity> implements LogisticsTrackService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;
    private final static DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsTrackDTO.AddDTO addDTO) {
        LogisticsTrackEntity logisticsTrackEntity = new LogisticsTrackEntity();
        BeanMapperUtils.copy(addDTO, logisticsTrackEntity);
        if (CharSequenceUtil.isBlank(logisticsTrackEntity.getTrackNo())){
            return new BaseResultDTO.AddDTO();
        }

        // 数据处理
        handleData(logisticsTrackEntity);

        log.info("开始新增物流轨迹单");
        boolean save = super.save(logisticsTrackEntity);
        if (!save) {
            throw new ServiceException("物流轨迹单保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流轨迹单", logisticsTrackEntity.getId());
        
        operateLogService.addModuleOperateLog(msg, null, logisticsTrackEntity.getId(), "新增操作");
        

        return new BaseResultDTO.AddDTO(logisticsTrackEntity.getId(), logisticsTrackEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsTrackDTO.UpdateDTO updateDTO) {
        LogisticsTrackEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流轨迹单"));
        LogisticsTrackEntity logisticsTrackEntity = BeanMapperUtils.map(LogisticsTrackEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsTrackEntity);
        log.info("编辑 开始修改物流轨迹单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsTrackEntity);
        if (!save) {
            throw new ServiceException("物流轨迹单保存失败");
        }
        

        // 记录主单操作日志
        log.info("编辑 开始记录物流轨迹单日志数据，id：【{}】", logisticsTrackEntity.getId());
        String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsTrackEntity.getId(), "物流轨迹单");
        
        operateLogService.addModuleOperateLogByObj(old, logisticsTrackEntity, null, logisticsTrackEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
     * 根据运输单号查询数据
     *
     * @param trackNoList
     * @return
     * @author yl
     * @date 2023-11-16 15:50
     */
    @Override
    public List<LogisticsTrackEntity> listByTrackNoList(List<String> trackNoList) {
        if (CollectionUtils.isEmpty(trackNoList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(LogisticsTrackEntity::getTrackNo, trackNoList).orderByDesc(LogisticsTrackEntity::getTrackTime).list();
    }

    @Override
    public LogisticsTrackDTO.ViewDTO listByTrackNo(String trackNo) {
        if(StringUtils.isBlank(trackNo)){
            return new LogisticsTrackDTO.ViewDTO();
        }
        LogisticsTrackDTO.ViewDTO viewDTO = new LogisticsTrackDTO.ViewDTO();
        viewDTO.setTrackNo(trackNo);

        List<LogisticsTrackEntity> list = this.listByTrackNoList(Arrays.asList(trackNo));
        List<LogisticsTrackDTO.ListDTO> resultList = BeanMapperUtils.copyList(LogisticsTrackDTO.ListDTO.class, list);
        int size = resultList.size();
        for (int i = 0; i < size; i++) {
            LogisticsTrackDTO.ListDTO item = resultList.get(i);
            if (i == 0) {
                item.setIsLatest(Boolean.TRUE);
            } else {
                item.setIsLatest(Boolean.FALSE);

            }
            String status = item.getStatus();
            String statusName = LogisticTrackStatusEnum.getName(status);
            item.setStatusName(statusName);
        }
        viewDTO.setList(resultList);
        return viewDTO;
    }

    @Override
    @Async("tmsExecutor")
    public void processTrackData(PlatformTrackDTO dto){
        log.info(CharSequenceUtil.format("-------记录【{}】物流轨迹开始------", dto.getTrackNo()));
        if (CharSequenceUtil.isBlank(dto.getTrackNo()) || CollectionUtils.isEmpty(dto.getDetails())){
            return;
        }
        List<LogisticsTrackEntity> newList = TrackDataConverter.INSTANCE.platformToTrack(dto.getDetails());
        //增量数据库记录
        this.saveIncrementTrackData(dto.getTrackNo(), newList);
        //获取最新记录
        LogisticsTrackEntity maxTrack = newList.stream().max(Comparator.comparing(LogisticsTrackEntity::getTrackTime)).orElse(null);
        //根据跟踪号进行更新操作
        logisticsBillDetailService.updateLogisticsBillDetailByTrackNo(maxTrack);
        log.info(CharSequenceUtil.format("-------记录【{}】物流轨迹结束------", dto.getTrackNo()));

    }

    @Override
    public void updateBeforeThreeMonthTrackNo(LogisticsBillDetailQueryDTO query) {
        List<LogisticsTrackDTO.UpdateTrackDTO> dtoList = baseMapper.listBeforeThreeMonthTrack(query);
        if (CollectionUtils.isEmpty(dtoList)){
            return;
        }
        List<String> trackNoList = dtoList.stream().map(LogisticsTrackDTO.UpdateTrackDTO::getTrackNo).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(trackNoList)){
            return;
        }
        String code = LogisticTrackStatusEnum.SYSTEM_COMPLETE.getCode();
//        LocalDateTime trackTime = LocalDateTime.now();
        //集合分区
        List<List<String>> partition = Lists.partition(trackNoList, MathUtil.NUMBER_100);
        partition.forEach(e -> logisticsBillDetailService.batchUpdateTrackStatus(e,code,null, null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void webhookByTrack123(LogisticsTrackDTO.TrackWebHookDTO dto) {
        if (Objects.isNull(dto.getData()) || CharSequenceUtil.isBlank(dto.getData().getTrackNo()) || Objects.isNull(dto.getData().getLocalLogisticsInfo())
                || CollectionUtils.isEmpty(dto.getData().getLocalLogisticsInfo().getTrackingDetails())){
            log.info("webhook接收到数据格式无数据记录：{}", dto);
            return;
        }
        String trackNo = dto.getData().getTrackNo();
        //轨迹明细
        List<LogisticsTrackDTO.TrackingDetail> trackingDetails = dto.getData().getLocalLogisticsInfo().getTrackingDetails();
        //数据转换
        List<LogisticsTrackEntity> newList = TrackDataConverter.INSTANCE.convertWebHookToEntity(trackingDetails);
        //设置唯一值
        newList.forEach(e-> {e.setMd5(getDataMd5(e,trackNo));e.setTrackNo(trackNo);});
        //获取最新记录
        LogisticsTrackEntity maxTrack = newList.stream().max(Comparator.comparing(LogisticsTrackEntity::getTrackTime)).orElse(null);
        //增量数据库记录
        this.saveIncrementTrackData(trackNo, newList);
        //根据跟踪号进行更新操作
        logisticsBillDetailService.updateLogisticsBillDetailByTrackNo(maxTrack);
    }

    @Override
    public void saveIncrementTrackData(String trackNo, List<LogisticsTrackEntity> newList) {
        if (CharSequenceUtil.isBlank(trackNo) || CollectionUtils.isEmpty(newList)){
            return;
        }
        List<LogisticsTrackEntity> oldList = this.listByTrackNoList(Collections.singletonList(trackNo));
        if (CollectionUtils.isEmpty(oldList)){
            this.saveBatch(newList);
        }else {
            List<String> md5List = oldList.stream().map(LogisticsTrackEntity::getMd5).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
            List<LogisticsTrackEntity> noExistList = newList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getMd5()) && !md5List.contains(e.getMd5())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(noExistList)){
                this.saveBatch(noExistList);
            }
        }
    }

    /**
     * 获取唯一值
     * @param trackingDetail
     * @param trackNo
     * @return
     */
    private String getDataMd5(LogisticsTrackEntity trackingDetail, String trackNo) {
        String trackTime = trackingDetail.getTrackTime().format(TIME_FORMAT);
        return DigestUtil.md5Hex(trackNo + trackingDetail.getContent() + trackTime);
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsTrackEntity logisticsTrackEntity) {
        
    }
}
