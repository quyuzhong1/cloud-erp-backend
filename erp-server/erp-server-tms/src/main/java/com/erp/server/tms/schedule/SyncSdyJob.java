package com.erp.server.tms.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.SyncOperateEnum;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.tms.entity.LogisticsBillDetailEntity;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.server.tms.service.LogisticsBillDetailService;
import com.erp.server.tms.service.LogisticsBillService;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.LogisticsSupplierService;
import com.erp.server.tms.sync.SyncLogisticsBillService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SyncSdyJob {
    @Resource
    private LogisticsBillService logisticsBillService;
    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;
    @Resource
    private LogisticsChannelService logisticsChannelService;
    @Resource
    private LogisticsSupplierService logisticsSupplierService;
    @Resource
    private SyncLogisticsBillService syncLogisticsBillService;


    @XxlJob("syncSdySoB2c")
    public void syncSdySoB2c() {
        String jobParam = XxlJobHelper.getJobParam();
        LocalDateTime createStartTime = null;
        LocalDateTime createEndTime = null;
        Integer pageSize = 1000;// 每页记录数
        if (StrUtil.isNotBlank(jobParam)) {
            JSONObject jsonParam = JSONUtil.parseObj(jobParam);
            createStartTime = jsonParam.getLocalDateTime("createStartTime", LocalDateTime.now().minusMonths(1));
            createEndTime = jsonParam.getLocalDateTime("createEndTime", LocalDateTime.now());
            jsonParam.getInt("pageSize", 1000);
        }


        //总条数
        int currentPage = 0;

        List<LogisticsBillEntity> list = new ArrayList<>();
        while (true) {
            System.out.println("===========当前页数：" + currentPage + "开始时间：" + LocalDateTime.now());
            int offset = currentPage * pageSize;
            list = logisticsBillService.queryToSdy(createStartTime, createEndTime, pageSize, offset);
            if (CollUtil.isEmpty(list)) {
                return;
            }

            List<String> ids = list.stream().map(req -> req.getId()).collect(Collectors.toList());
            List<LogisticsBillDetailEntity> billDetailEntities = logisticsBillDetailService.listByMainIds(ids);

            List<String> channelIds = list.stream().map(req -> req.getChannelId()).distinct().collect(Collectors.toList());
            List<LogisticsChannelEntity> logisticsChannelEntities = logisticsChannelService.listByIds(channelIds);

            List<String> supplierIds = logisticsChannelEntities.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());
            List<LogisticsSupplierEntity> logisticsSupplierEntities = logisticsSupplierService.listByIds(supplierIds);


            for (LogisticsBillEntity entity : list) {
                List<LogisticsBillDetailEntity> detailEntityList = billDetailEntities.stream().filter(req -> req.getMainId().equals(entity.getId())).collect(Collectors.toList());
                syncLogisticsBillService.syncDataToSdy(entity, detailEntityList, SyncOperateEnum.OPERATE_APPROVE.getCode(), logisticsChannelEntities, logisticsSupplierEntities);
            }
        }
    }
}
