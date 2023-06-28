package com.erp.server.dmp.task;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SourceTypeEnum;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.dto.mabang.MabangInOutStockDTO;
import com.erp.model.dmp.entity.DmpOutInStockDetailEntity;
import com.erp.model.dmp.entity.DmpOutInStockEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.server.dmp.push.service.mabang.MabangCommonService;
import com.erp.server.dmp.push.service.mabang.MabangInOutStockService;
import com.erp.server.dmp.service.DmpOutInStockDetailService;
import com.erp.server.dmp.service.DmpOutInStockService;
import com.erp.server.dmp.utils.MabangUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * DMP推送数据至马帮补偿处理
 * @CreateTime: 2023-06-28  18:44
 * @Author: zhangchunlin
 */
@Slf4j
@Component
public class PushTransferToMbJob {

    @Autowired
    private DmpOutInStockService dmpOutInStockService;

    @Autowired
    private MabangInOutStockService mabangInOutStockService;

    @Autowired
    private MabangCommonService mabangCommonService;

    @Autowired
    private DmpOutInStockDetailService dmpOutInStockDetailService;

    /**
     * 推送直接调拨单到马帮出入库补偿处理
     * 同步失败的定时再次推送
     * @return
     */
    @XxlJob("PushTransferToMb")
    public ReturnT<String> PushTransferToMb() {
        XxlJobHelper.log("PushTransferToMb start");
        // 查询同步失败的出入库
        List<DmpOutInStockEntity> recordEntityList = dmpOutInStockService.lambdaQuery()
                .eq(DmpOutInStockEntity::getTargetPlatformSign, PlatformEnum.MABANG.getDesc())
                .in(DmpOutInStockEntity::getSyncMbStatus, Arrays.asList("-1"))
                .in(DmpOutInStockEntity::getSourceType, Arrays.asList(SourceTypeEnum.TRANSFER_INFO.getCode()))
                .le(DmpOutInStockEntity::getUpdateTime, LocalDateTime.now().minusHours(1))
                .list();

        // 按修改时间升序
        if(CollUtil.isNotEmpty(recordEntityList)) {
            recordEntityList.sort(Comparator.comparing(DmpOutInStockEntity::getUpdateTime));

            for (DmpOutInStockEntity recordEntity : recordEntityList) {
                //模块类型
                Integer type = ApiModuleTypeEnum.TRANSFER_INFO.getCode();
                PlatformEntity platformEntity = mabangCommonService.getPlatformEntity(recordEntity.getId(), type);
                if (ObjectUtils.isEmpty(platformEntity)) {
                    return ReturnT.SUCCESS;
                }
                List<DmpOutInStockDetailEntity> detailList = dmpOutInStockDetailService.findByMainId(recordEntity.getId());
                MabangInOutStockDTO mabangInOutStockDTO = MabangUtil.fillMabangInOutStockByDmp(recordEntity, detailList);
                try {
                    // 推送马帮
                    if(Objects.equals(recordEntity.getType(), "in")) {
                        mabangInOutStockService.sendToMabangInStock(recordEntity, mabangInOutStockDTO, platformEntity, type, "" );
                    } else if(Objects.equals(recordEntity.getType(), "out")) {
                        mabangInOutStockService.sendToMabangOutStock(recordEntity, mabangInOutStockDTO, platformEntity, type, "" );
                    }
                }catch (Exception e){
                    log.error("推推送直接调拨单到马帮出入库异常", e);
                    XxlJobHelper.log("推送直接调拨单到马帮出入库异常", e);
                }
            }
        }
        XxlJobHelper.log("PushTransferToMb end");
        return ReturnT.SUCCESS;
    }

}