package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.server.tms.mapper.LogisticsTrackMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsTrackDTO;

import java.time.LocalDateTime;
import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

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
    @Autowired
    private CommonService commonService;
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

        // 数据处理
        handleData(logisticsTrackEntity);

        log.info("开始新增物流轨迹单");
        boolean save = super.save(logisticsTrackEntity);
        if (!save) {
            throw new ServiceException("物流轨迹单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "物流轨迹单", logisticsTrackEntity.getId());
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
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsTrackEntity.getId(), "物流轨迹单");
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
        LogisticsTrackDTO.ViewDTO viewDTO = new LogisticsTrackDTO.ViewDTO();
        viewDTO.setTrackNo(trackNo);

        List<LogisticsTrackEntity> list = this.listByTrackNoList(Arrays.asList(trackNo));
        List<LogisticsTrackDTO.ListDTO> resultList = BeanMapperUtils.copyList(LogisticsTrackDTO.ListDTO.class, list);
        int size = resultList.size();
        for (int i = 0; i < size; i++) {
            LogisticsTrackDTO.ListDTO item = resultList.get(i);
            if (i == 0) {
                item.setIsLatest(Boolean.TRUE);
            }else{
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
        LogisticsBillDetailEntity detailByTrackNo = logisticsBillDetailService.getDetailByTrackNo(logisticsTrackEntity.getTrackNo());
        if (Objects.isNull(detailByTrackNo)) return;
        //状态更新同步
        if (!detailByTrackNo.getTrackStatus().equalsIgnoreCase(logisticsTrackEntity.getStatus())) {
            detailByTrackNo.setTrackStatus(logisticsTrackEntity.getStatus());
            detailByTrackNo.setTrackTime(LocalDateTime.now());
            logisticsBillDetailService.saveOrUpdate(detailByTrackNo);
            if (LogisticTrackStatusEnum.SIGN.getCode().equalsIgnoreCase(logisticsTrackEntity.getStatus())) {
                //TODO 同步订单状态

            }
        }
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsTrackEntity logisticsTrackEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
