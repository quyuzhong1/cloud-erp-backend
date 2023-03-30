package com.erp.server.dmp.controller.open;


import cn.hutool.json.JSONObject;
import com.erp.model.dmp.dto.GoodcangDTO;
import com.erp.server.dmp.pull.service.GoodcangStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 谷仓订阅
 *
 * @Author Cloud
 * @Date 2023/3/29 11:11
 **/
@Slf4j
@RestController
@RequestMapping("/dmp/open/api")
public class GoodcangOpenController {

    @Resource
    private GoodcangStockService goodcangStockService;

    @RequestMapping ("/goodcang/subscribe")
    public GoodcangDTO.ResultDTO subscribe(@RequestBody(required = false) JSONObject dto){
        log.warn("GoodCangOpenController>>>subscribe>>>dto ={}", dto);

        GoodcangDTO.MessageDTO messageDTO = new GoodcangDTO.MessageDTO();
        goodcangStockService.receiveGoDownEntry(messageDTO);
        return GoodcangDTO.ResultDTO.success();
    }

}
