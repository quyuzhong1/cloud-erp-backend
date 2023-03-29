package com.erp.server.dmp.controller.open;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.erp.model.dmp.dto.GoodCangDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 谷仓订阅
 *
 * @Author Cloud
 * @Date 2023/3/29 11:11
 **/
@Slf4j
@RestController
@RequestMapping("/dmp/open/api/good/cang")
public class GoodCangOpenController {

    @RequestMapping ("/subscribe")
    public GoodCangDTO.ResultDTO subscribe(@RequestBody JSONObject dto){
        log.warn("GoodCangOpenController>>>subscribe>>>dto ={}", dto);
        return GoodCangDTO.ResultDTO.success();
    }

}
