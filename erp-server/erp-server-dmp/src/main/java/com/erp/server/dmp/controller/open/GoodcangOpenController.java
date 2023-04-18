package com.erp.server.dmp.controller.open;


import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.MD5;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.utils.Md5Util;
import com.erp.model.dmp.dto.GoodcangDTO;
import com.erp.server.dmp.pull.service.GoodcangStockService;
import com.erp.server.dmp.utils.GoodCangApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.LinkedHashMap;

/**
 * 谷仓订阅
 *
 * @Author Cloud
 * @Date 2023/3/29 11:11
 **/
@Slf4j
@RestController
@RequestMapping("open/api")
public class GoodcangOpenController {

    private static final String SEND_RECEIVING = "SendReceiving";

    @Resource
    private GoodcangStockService goodcangStockService;

    @RequestMapping ("/goodcang/subscribe")
    public GoodcangDTO.ResultDTO subscribe(@RequestParam(required = false) LinkedHashMap dto){
        log.warn("GoodCangOpenController>>>subscribe>>>dto ={}", dto);
        // 类型校验
        String messageType = dto.getOrDefault("MessageType","").toString();
        if(!SEND_RECEIVING.equals(messageType)){
            log.error("MessageType 不是上架单 dto ={} ", JSONUtil.toJsonStr(dto));
            return GoodcangDTO.ResultDTO.fail(StrUtil.format("MessageType 不是上架单 messageType ={} ", messageType));
        }
        String appToken = dto.getOrDefault("AppToken","").toString();
        if (!appToken.equals(GoodCangApiUtils.APP_TOKEN)){
            log.error("AppToken 错误 appToken ={} ", appToken);
            return GoodcangDTO.ResultDTO.fail(StrUtil.format("AppToken 错误 appToken ={} ", appToken));
        }
        // 签名校验
        String message = dto.getOrDefault("Message", "").toString();
        String sign = dto.get("Sign").toString();
        String sendTime = dto.getOrDefault("SendTime", "").toString();
        String signStr = message + appToken + sendTime;
//        使用hutool工具进行md5解密
        String signStrMd5 = Md5Util.getMd5(signStr);
        if (!signStrMd5.equals(sign)) {
            log.error("签名校验失败 signStrMd5 ={} sign ={} ", signStrMd5, sign);
//            return GoodcangDTO.ResultDTO.fail(StrUtil.format("签名校验失败 signStrMd5 ={} sign ={} ", signStrMd5, sign));
        }

        GoodcangDTO.MessageDTO messageDTO = JSONUtil.toBean(message, GoodcangDTO.MessageDTO.class);
        if(StrUtil.isBlank(messageDTO.getReceivingCode())){
            return GoodcangDTO.ResultDTO.fail("receiving_code 为空");
        }
        goodcangStockService.receiveGoDownEntry(messageDTO);
        return GoodcangDTO.ResultDTO.success();
    }

}
