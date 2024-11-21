package com.erp.server.dmp.push.service.sdy.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.HttpCommonUtil;
import com.erp.model.dmp.dto.SdySaveResultDTO;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.erp.server.dmp.push.service.sdy.SdyPushCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 数帝云推送通用接口
 */
@Slf4j
@Service
public class SdyPushCommonServiceImpl implements SdyPushCommonService {

    @Override
    public void executeConsumer(ShudiyunB2cOrderDTO shudiyunB2cOrderDTO) {
        String path = "http://172.16.100.50:30860/openapi/information/save";

        //入参
        HashMap<String, Object> orderParams = new HashMap<>(1);

        //设置请求头
        Map<String, String> orderHeaderMap = new HashMap<>(1);

        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(path, JSONUtil.toJsonStr(shudiyunB2cOrderDTO), null, orderHeaderMap, RequestMethod.POST);

        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, 数帝云接口请求失败，返回值 responseMap={}", path, orderParams.toString(), JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数帝云接口请求失败，返回值 responseMap={}",
                    path, orderParams.toString(), JSONUtil.toJsonStr(apiResult)));
        }

        SdySaveResultDTO orderDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), SdySaveResultDTO.class);
        if (orderDTO.getErrno() != 0) {
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数帝云接口请求失败，返回值 responseMap={}",
                    path, orderParams.toString(), JSONUtil.toJsonStr(apiResult)));
        }
    }
}
