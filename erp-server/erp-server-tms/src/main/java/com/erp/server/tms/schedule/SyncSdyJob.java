package com.erp.server.tms.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.business.wrapper.QueryParam;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.scm.entity.SupplierEntity;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
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


    @XxlJob("syncSdyLogisticsBill")
    public void syncSdyLogisticsBill() {
        String jobParam = XxlJobHelper.getJobParam();
        LocalDateTime createStartTime = null;
        LocalDateTime createEndTime = null;
        Integer pageSize = 1000;// 每页记录数
        String queryParamsStr = "";
        if (StrUtil.isNotBlank(jobParam)) {
            JSONObject jsonParam = JSONUtil.parseObj(jobParam);
            createStartTime = jsonParam.getLocalDateTime("createStartTime", LocalDateTime.now().minusMonths(1));
            createEndTime = jsonParam.getLocalDateTime("createEndTime", LocalDateTime.now());
            jsonParam.getInt("pageSize", 1000);
            queryParamsStr = jsonParam.getStr("queryParams");
        }


        //总条数
        int currentPage = 0;

        List<LogisticsBillEntity> list = new ArrayList<>();
        while (true) {
            XxlJobHelper.log("===========当前页数：" + currentPage + "开始时间：" + LocalDateTime.now());
            int offset = currentPage * pageSize;
            if (StringUtils.isNotBlank(queryParamsStr)) {
                List<QueryParam> queryParams = JSONUtil.toList(queryParamsStr, QueryParam.class);
                QueryWrapper<LogisticsBillEntity> queryWrapper = (QueryWrapper<LogisticsBillEntity>) QueryParam.getQueryWrapper(queryParams);
                Page<LogisticsBillEntity> page = logisticsBillService.page(new Page<>(currentPage, pageSize), queryWrapper);
                list = page.getRecords();
            } else {
                list = logisticsBillService.queryToSdy(createStartTime, createEndTime, pageSize, offset);
            }
            if (CollUtil.isEmpty(list)) {
                return;
            }

            List<String> ids = list.stream().map(req -> req.getId()).collect(Collectors.toList());
            List<LogisticsBillDetailEntity> billDetailEntities = logisticsBillDetailService.listByMainIds(ids);

            Map<String, Pair<String, String>> logisticInfoMaps = syncLogisticsBillService.getLogisticInfo(list);
            for (LogisticsBillEntity entity : list) {
                List<LogisticsBillDetailEntity> detailEntityList = billDetailEntities.stream()
                        .filter(req -> req.getMainId().equals(entity.getId()) && CharSequenceUtil.isNotBlank(req.getTrackStatus()))
                        .collect(Collectors.toList());
                if (CollUtil.isNotEmpty(detailEntityList)) {
                    syncLogisticsBillService.syncDataToSdy(entity, detailEntityList, SyncOperateEnum.OPERATE_APPROVE.getCode(), logisticInfoMaps);
                }
            }
            currentPage++;
            XxlJobHelper.log("===========当前页数：" + currentPage + "结束时间：" + LocalDateTime.now());
        }
    }
}
