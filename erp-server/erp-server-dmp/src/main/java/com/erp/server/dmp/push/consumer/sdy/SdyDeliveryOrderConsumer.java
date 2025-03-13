package com.erp.server.dmp.push.consumer.sdy;

import com.alibaba.fastjson.JSON;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.erp.server.dmp.push.service.sdy.SdyPushCommonService;

import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 速递云
 */

@Service
@Slf4j
public class SdyDeliveryOrderConsumer {
    @Resource
    private SdyPushCommonService sdyPushCommonService;


    public ApiResult handle(Object ext) {
        List<ShudiyunB2cOrderDTO> shudiyunB2cOrderDTOList = new ArrayList<>();
        if (ext.toString().startsWith("[") && ext.toString().endsWith("]")) {
            shudiyunB2cOrderDTOList = JSON.parseArray(ext.toString(), ShudiyunB2cOrderDTO.class);
        } else {
            ShudiyunB2cOrderDTO dto = JSON.parseObject(ext.toString(), ShudiyunB2cOrderDTO.class);
            shudiyunB2cOrderDTOList.add(dto);
        }
        ApiResult result = null;
        try {
			result = sdyPushCommonService.executeConsumer(shudiyunB2cOrderDTOList);
		} catch (Exception e) {
			return ApiResult.error("" , "调用数帝云接口异常" + ExceptionUtil.stacktraceToOneLineString(e));
		}
		return result;
    }
}
