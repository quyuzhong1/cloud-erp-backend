package com.erp.server.oms.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.entity.BaseEntity;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cErrorEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.server.oms.service.SoB2cAbnormalService;
import com.erp.server.oms.service.SoB2cErrorService;
import com.erp.server.oms.service.SoB2cService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rtfparserkit.rtf.Command.list;

/**
 * B2C订单标记发货重试
 */
@Component
@Slf4j
public class SoB2cErrorFeiShuJob {

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
        sb.append("\r\n");
        sb.append("业务名称：B2C销售订单-异常订单");
        sb.append("\r\n");
        sb.append("关键信息：");
        sb.append("\r\n");
        for (SoB2cErrorDTO.TypeCountDTO typeCountDTO : list) {
            SoB2cErrorTypeEnum soB2cErrorTypeEnum = SoB2cErrorTypeEnum.getEnum(typeCountDTO.getType());
            sb.append(soB2cErrorTypeEnum.getName()+"，数量："+typeCountDTO.getTypeCount());
            sb.append("\r\n");
        }
        Map<String, Object> bodyMap = new HashMap<String, Object>();
        bodyMap.put("msg_type", "text");
        Map<String, String> contentMap = new HashMap<String, String>();
        contentMap.put("text", sb.toString());
        bodyMap.put("content", contentMap);
        String url = "";
        HttpUtil.post(url, JSON.toJSONString(bodyMap));

        XxlJobHelper.log("SoB2cErrorFeiShuJob 执行任务列表结束");
        return ReturnT.SUCCESS;
    }
}
