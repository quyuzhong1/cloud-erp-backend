package com.erp.server.wms.schedule;

import cn.hutool.core.util.StrUtil;
import com.erp.server.wms.service.CfgRuleWaveService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 波次规则定时任务
 * @author will
 * @date 2024/6/25 16:01
 */
@Component
@Slf4j
public class CfgRuleWaveJob {

    @Resource
    private CfgRuleWaveService cfgRuleWaveService;


    /**
     * 自动执行波次规则
     * @author will
     * @date 2024/6/25 16:04
     * @return ReturnT<String>
     */
    @XxlJob("autoExecuteRule")
    public ReturnT<String> autoExecuteRule() {
        //可输入时间
        String time = XxlJobHelper.getJobParam();

        XxlJobHelper.log("====开始执行波次规则=====");
        cfgRuleWaveService.autoExecuteRule(time);
        XxlJobHelper.log("====结束执行波次规则=====");
        return ReturnT.SUCCESS;
    }

}