package com.erp.server.srm.schedule;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.scm.dto.CfgSupplierSalesDTO;
import com.erp.model.wms.dto.CfgSettingValueDTO;
import com.erp.model.wms.entity.CfgSettingEntity;
import com.erp.model.wms.enums.CfgSettingEnum;
import com.erp.model.wms.enums.ReconciliationTypeEnum;
import com.erp.rpc.dmp.feign.DmpSkuSaleReportFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.wms.feign.CfgSettingFeign;
import com.erp.server.srm.service.PoReconciliationDetailScmService;
import com.erp.server.srm.service.SalesSharingService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * @description: 根据销量配置生成销量共享
 * @author jack
 * @date: 2025-06-23
 */
@Component
@Slf4j
@EnableScheduling
public class CalSalesSharingJob {


    @Resource
    private SalesSharingService salesSharingService;

    /**
     *
     *
     * @return
     */
    @XxlJob("CalSalesSharingJob")
    public ReturnT calSalesSharingJob() {
        XxlJobHelper.log("====CalSalesSharingJob 开始=====");
        long start = System.currentTimeMillis();

        salesSharingService.calSalesSharing();

        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("====CalSalesSharingJob 结束=====");
        return ReturnT.SUCCESS;
    }
}
