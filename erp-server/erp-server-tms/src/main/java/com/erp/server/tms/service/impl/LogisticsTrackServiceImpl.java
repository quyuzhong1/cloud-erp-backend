package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.server.tms.convert.TrackDataConverter;
import com.erp.server.tms.mapper.LogisticsTrackMapper;
import com.erp.server.tms.service.*;
import com.google.common.collect.Lists;
import com.sdk.tms.track123.dto.PlatformTrackDTO;
import com.sdk.tms.track123.model.response.TrackDetail;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import io.seata.common.util.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
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
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;
    @Resource
    private LogisticsBillService logisticsBillService;
    @Resource
    private SoInfoFeign soInfoFeign;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsTrackDTO.AddDTO addDTO) {
        LogisticsTrackEntity logisticsTrackEntity = new LogisticsTrackEntity();
        BeanMapperUtils.copy(addDTO, logisticsTrackEntity);
        if (StrUtil.isBlank(logisticsTrackEntity.getTrackNo())){
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
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流轨迹单", logisticsTrackEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, logisticsTrackEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

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
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录物流轨迹单日志数据，id：【{}】", logisticsTrackEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsTrackEntity.getId(), "物流轨迹单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
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
    public void deleteByTrackNo(String trackNo) {
        baseMapper.deleteByTrackNo(trackNo);
    }

    /**
     * 运输状态 状态
     * notFind 查询不到
     * waitCollect等待揽收
     * trackIng运输途中
     * arriveWaitTake到达待取
     * deliveryIng派送途中
     * deliveryFail投递失败
     * sign 成功签收
     * maybeException可能异常
     * transportLong  运输过久
     *
     * @param logisticsTrackEntity
     */
    @Override
    public void checkTrackStatus(LogisticsTrackEntity logisticsTrackEntity) {
        if (Objects.isNull(logisticsTrackEntity)) return;
        List<LogisticsBillDetailEntity> detailList = logisticsBillDetailService.getDetailByTrackNo(logisticsTrackEntity.getTrackNo());
        if (CollectionUtils.isEmpty(detailList)) return;
        //状态更新同步
        detailList.forEach(detailByTrackNo -> {
            if (!detailByTrackNo.getTrackStatus().equalsIgnoreCase(logisticsTrackEntity.getStatus())) {
                detailByTrackNo.setTrackStatus(logisticsTrackEntity.getStatus());
                detailByTrackNo.setTrackTime(LocalDateTime.now());
                detailByTrackNo.setIsApiUpdate(Boolean.TRUE);
                if (LogisticTrackStatusEnum.SIGN.getCode().equalsIgnoreCase(logisticsTrackEntity.getStatus())) {
                    //TODO 同步订单状态
                    detailByTrackNo.setSignTime(logisticsTrackEntity.getTrackTime());
                } else {
                    detailByTrackNo.setSignTime(null);
                }
            }
        });
        if (CollectionUtils.isNotEmpty(detailList)){
            logisticsBillDetailService.updateBatchById(detailList);
        }
    }

    @Override
    public LogisticsTrackEntity getMaxByTrackTime(String trackNo) {
        if (StrUtil.isBlank(trackNo)){
            return null;
        }
        return baseMapper.getMaxByTrackTime(trackNo);
    }

    @Override
    @Async("tmsExecutor")
    public void processTrackData(PlatformTrackDTO dto){
        log.info(StrUtil.format("-------记录【{}】物流轨迹开始------", dto.getTrackNo()));
        List<LogisticsTrackEntity> logisticsTrackEntities = TrackDataConverter.INSTANCE.platformToTrack(dto.getDetails());
        if (StrUtil.isBlank(dto.getTrackNo())){
            return;
        }
        //获取跟踪号最新一条记录
        LogisticsTrackEntity trackEntity = this.getMaxByTrackTime(dto.getTrackNo());
        //未查询到物流轨迹 且最近一条物流轨迹是三个月前
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 计算三个月前的时间
        LocalDateTime threeMonthsAgo = now.minusMonths(3);
        //先物理删除  再新增
        if (CollectionUtils.isNotEmpty(logisticsTrackEntities)) {
            Boolean needUpdate = Boolean.FALSE;
            LogisticsTrackEntity max = Collections.max(logisticsTrackEntities, Comparator.comparing(LogisticsTrackEntity::getTrackTime));
            //查询不到就保存全部
            if (Objects.isNull(trackEntity)){
                needUpdate = Boolean.TRUE;
                this.saveBatch(logisticsTrackEntities);
            }else {
                List<LogisticsTrackEntity> lastList = logisticsTrackEntities.stream().filter(e -> Objects.nonNull(e)
                        && Objects.nonNull(e.getTrackTime())
                        && StrUtil.isNotBlank(e.getStatus())
                        && !LogisticTrackStatusEnum.NOT_FIND.getCode().equals(e.getStatus())
                        && e.getTrackTime().isAfter(trackEntity.getTrackTime())
                ).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(lastList)){
                    needUpdate = Boolean.TRUE;
                    this.saveBatch(lastList);
                }
            }
            if (needUpdate){
                logisticsBillDetailService.updateLogisticsBillDetailByTrackNo(max);
            }else {
                if (Objects.nonNull(trackEntity) && Objects.nonNull(trackEntity.getTrackTime()) && trackEntity.getTrackTime().isBefore(threeMonthsAgo)){
                    //系统完结
                    logisticsBillDetailService.updateTrackStatus(dto.getTrackNo(),LogisticTrackStatusEnum.SYSTEM_COMPLETE.getCode(),null,trackEntity.getTrackTime());
                }
            }
        }else {
            if (Objects.nonNull(trackEntity) && Objects.nonNull(trackEntity.getTrackTime()) && trackEntity.getTrackTime().isBefore(threeMonthsAgo)){
                //系统完结
                logisticsBillDetailService.updateTrackStatus(dto.getTrackNo(),LogisticTrackStatusEnum.SYSTEM_COMPLETE.getCode(),null,trackEntity.getTrackTime());
            }
        }
        log.info(StrUtil.format("-------记录【{}】物流轨迹结束------", dto.getTrackNo()));

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


    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsTrackEntity logisticsTrackEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
