package com.erp.server.dmp.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.core.entity.BaseEntity;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.entity.ApiPlmSyncLogEntity;
import com.erp.model.dmp.entity.DmpOutInStockEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.server.dmp.mapper.DmpOutInStockMapper;
import com.erp.server.dmp.push.service.common.DmpSyncCommonService;
import com.erp.server.dmp.service.DmpOutInStockService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <p>
 * 手工出入库待同步数据表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-06-25
 */
@Slf4j
@Service
public class DmpOutInStockServiceImpl extends SuperServiceImpl<DmpOutInStockMapper, DmpOutInStockEntity> implements DmpOutInStockService {

    @Autowired
    private DmpOutInStockMapper dmpOutInStockMapper;

    @Autowired
    private MQProducerService mqProducerService;

    @Autowired
    private DmpSyncCommonService dmpSyncCommonService;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void updateSyncInfoSuccess(String id, String syncStatus, String targetOrderNo, String requestParam,
                               PlatformEntity platformEntity, Integer type, String approveType) {
        UpdateWrapper<DmpOutInStockEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(BaseEntity.ID,id).set(DmpOutInStockEntity.LAST_SYNC_MB_TIME, LocalDateTime.now())
                .set(DmpOutInStockEntity.SYNC_MB_STATUS, syncStatus).set(DmpOutInStockEntity.TARGET_ORDER_CODE, targetOrderNo);
        dmpOutInStockMapper.update(null, updateWrapper);

        dmpSyncCommonService.insertLogWriteBackSyncMabangStatus(platformEntity, id, requestParam, StrUtil.format("{}；ERP直接调拨单同步{}成功", SyncKingdeeOperateEnum.getDescByCode(approveType), PlatformEnum.MABANG.getDesc()), type, ApiSendStatusEnum.SUCCESS.getCode());

    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public void updateSyncInfoError(String id, String syncStatus, String sourceCode, String requestParam,
                               PlatformEntity platformEntity, Integer type, String errMsg, String approveType) {
        UpdateWrapper<DmpOutInStockEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq(BaseEntity.ID,id).set(DmpOutInStockEntity.LAST_SYNC_MB_TIME, LocalDateTime.now())
                .set(DmpOutInStockEntity.SYNC_MB_STATUS, syncStatus);
        dmpOutInStockMapper.update(null, updateWrapper);

        // 查询是否已经记录过错误日志
        ApiPlmSyncLogEntity apiPlmSyncLogEntity = dmpSyncCommonService.findLog(platformEntity, id, type);
        if(Objects.isNull(apiPlmSyncLogEntity)) {
            // 新增日志
            dmpSyncCommonService.insertLogWriteBackSyncMabangStatus(platformEntity, id, requestParam, StrUtil.format("{}；ERP直接调拨单同步{}失败，失败原因：{}", SyncKingdeeOperateEnum.getDescByCode(approveType), PlatformEnum.MABANG.getDesc(), errMsg), type, ApiSendStatusEnum.FAILURE.getCode());
        } else {
            // 更新日志
            dmpSyncCommonService.updateLog(apiPlmSyncLogEntity.getId(), requestParam, errMsg);
        }
        if(Objects.isNull(apiPlmSyncLogEntity)) {
            WarnMsgInfoDTO warnMsgInfoDTO = new WarnMsgInfoDTO();
            warnMsgInfoDTO.setTitle("ERP直接调拨单推送马帮手工出入库异常");
            warnMsgInfoDTO.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
            warnMsgInfoDTO.setBizName("ERP直接调拨单推送马帮手工出入库");
            warnMsgInfoDTO.setTableName("dmp_out_in_stock");
            warnMsgInfoDTO.setTableId(id);
            warnMsgInfoDTO.setKeyInfo(StrUtil.format("ERP直接调拨单单据编号: {}，失败原因：{}", sourceCode, errMsg));
            mqProducerService.sendWarnMsg(warnMsgInfoDTO);
        }
    }
}
