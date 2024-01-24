package com.erp.server.wms.schedule;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONArray;
import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.srm.dto.ReturnConfirmDTO;
import com.erp.model.srm.entity.CfgSettingEntity;
import com.erp.model.srm.enums.ConfigKeyEnum;
import com.erp.model.wms.dto.CfgSettingValueDTO;
import com.erp.model.wms.dto.extension.TStkCloseProfileDTO;
import com.erp.model.wms.entity.InventoryClosedRecordEntity;
import com.erp.model.wms.entity.PoReturnEntity;
import com.erp.rpc.srm.feign.SrmCfgSettingFeign;
import com.erp.server.wms.service.PoReturnService;
import com.erp.server.wms.utils.KingdeeExtensionUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 采购退货单定时器
 * @Author Luo_WG
 * @Date 2024/1/12 14:44
 **/
@Component
@Slf4j
@EnableScheduling
public class PoReturnJob {
    @Resource
    private PoReturnService poReturnService;
    @Resource
    private SrmCfgSettingFeign srmCfgSettingFeign;

    /**
     * 退货单超时确认定时器
     * @Author Luo_WG
     * @Date 2024/1/12 14:46
     * @return com.xxl.job.core.biz.model.ReturnT<java.lang.String>
     **/
    @XxlJob("poReturnAutoConfirmJob")
    public ReturnT<String> poReturnAutoConfirmJob() {
        XxlJobHelper.log("=====采购退货单自动确认 开始任务=====");
        long start = System.currentTimeMillis();
        poReturnService.poReturnAutoConfirm();
        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("=====采购退货单自动确认 结束任务=====");
        return ReturnT.SUCCESS;
    }
}
