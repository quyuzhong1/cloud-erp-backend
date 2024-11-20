package com.erp.server.oms.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.server.oms.service.SoB2cErrorService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * B2C订单标记发货重试
 */
@Component
@Slf4j
public class SoB2cErrorFeiShuJob {

    private static String namespace = SpringUtil.getProperty("spring.cloud.nacos.discovery.namespace");

    @Resource
    private SoB2cErrorService soB2cErrorService;
    /**
     * B2C订单异常统计并飞书通知
     * @Author jack
     **/
    @XxlJob("SoB2cErrorFeiShuJob")
    public ReturnT<String> SoB2cErrorFeiShuJob() {
        XxlJobHelper.log("SoB2cErrorFeiShuJob 执行开始");
        List<SoB2cErrorDTO.TypeCountDTO> list = soB2cErrorService.getTypeCountDTO();
        if (CollectionUtil.isEmpty(list)) {
                return ReturnT.SUCCESS;
        }

        StringBuffer sb = new StringBuffer();
        sb.append("B2C销售订单异常订单汇总提醒");
        sb.append("\n");
        sb.append("业务名称：B2C销售订单-异常订单");
        sb.append("\n");
        sb.append("关键信息：");
        sb.append("\n");
        for (SoB2cErrorDTO.TypeCountDTO typeCountDTO : list) {
            SoB2cErrorTypeEnum soB2cErrorTypeEnum = SoB2cErrorTypeEnum.getEnum(typeCountDTO.getType());
            if(soB2cErrorTypeEnum.getName().contains("异常")){
                sb.append(soB2cErrorTypeEnum.getName()+"，数量："+typeCountDTO.getTypeCount());
            }else{
                sb.append(soB2cErrorTypeEnum.getName()+"异常，数量："+typeCountDTO.getTypeCount());
            }
            sb.append("\n");
        }
        Map<String, Object> bodyMap = new HashMap<String, Object>();
        bodyMap.put("msg_type", "text");
        Map<String, String> contentMap = new HashMap<String, String>();
        contentMap.put("text", sb.toString());
        bodyMap.put("content", contentMap);
        String url = "https://open.feishu.cn/open-apis/bot/v2/hook/b18f5837-19c7-4383-808b-3b9d34b591f3";
//        if("prod".equals(namespace)) {
            XxlJobHelper.log("SoB2cErrorFeiShuJob 发送至机器人");
            url = "https://open.feishu.cn/open-apis/bot/v2/hook/b18f5837-19c7-4383-808b-3b9d34b591f3";
            HttpUtil.post(url, JSON.toJSONString(bodyMap));
//        }
        XxlJobHelper.log("SoB2cErrorFeiShuJob 执行任务列表结束");
        return ReturnT.SUCCESS;
    }
}
