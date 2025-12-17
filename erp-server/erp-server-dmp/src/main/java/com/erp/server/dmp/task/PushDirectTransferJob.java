package com.erp.server.dmp.task;

import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.SyncKingdeeOmsStatusEnum;
import com.erp.server.dmp.entity.DmpWarehouseInboundRecordEntity;
import com.erp.server.dmp.service.DmpWarehouseInboundRecordService;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeePushModuleEnum;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 推送直接调拨单
 *
 * @Author Cloud
 * @Date 2023/4/6 14:31
 **/
@Component
@Slf4j
public class PushDirectTransferJob {

    @Resource
    private DmpWarehouseInboundRecordService dmpWarehouseInboundRecordService;

    /**
     * 推送直接调拨单到金蝶
     * @return
     */
    @XxlJob("pushDirectTransferToKingdee")
    public ReturnT<String> pushDirectTransferToKingdee() {
        XxlJobHelper.log("pushDirectTransferToKingdee start");
        // 查询未推送完成订单
        List<DmpWarehouseInboundRecordEntity> recordEntityList = dmpWarehouseInboundRecordService.lambdaQuery()
                .in(DmpWarehouseInboundRecordEntity::getPlatformSign, Arrays.asList(OmsPlatformEnum.OMS_GOOD_CANG.getName(), OmsPlatformEnum.OMS_IML.getName()))
                .in(DmpWarehouseInboundRecordEntity::getSyncKingdeeStatus, Arrays.asList(SyncKingdeeOmsStatusEnum.BE_SUBMIT, SyncKingdeeOmsStatusEnum.BE_AUDIT))
                .le(DmpWarehouseInboundRecordEntity::getUpdateTime, LocalDateTime.now().minusHours(1))
                .list();
        // 执行金蝶后续操作
        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.STK_TRANSFER_DIRECT.getCode());
        for (DmpWarehouseInboundRecordEntity recordEntity : recordEntityList) {
            try {
                // 推送金蝶
                dmpWarehouseInboundRecordService.pushDirectTransferToKingdee(recordEntity, apiUtils);
            }catch (Exception e){
                log.error("推送直接调拨单到金蝶异常", e);
                XxlJobHelper.log("推送直接调拨单到金蝶异常", e);
            }
        }
        XxlJobHelper.log("pushDirectTransferToKingdee end");
        return ReturnT.SUCCESS;
    }
}
