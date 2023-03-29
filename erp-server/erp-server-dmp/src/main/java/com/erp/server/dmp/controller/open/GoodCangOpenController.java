package com.erp.server.dmp.controller.open;


import cn.hutool.json.JSONUtil;
import com.erp.model.dmp.dto.GoodCangDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 谷仓订阅
 *
 * @Author Cloud
 * @Date 2023/3/29 11:11
 **/
@Slf4j
@RequestMapping("/dmp/open/api/good/cang")
public class GoodCangOpenController {

    @PostMapping("/subscribe")
    public GoodCangDTO.ResultDTO subscribe(@RequestBody GoodCangDTO.SendReceivingDTO dto){
        log.warn("GoodCangOpenController>>>subscribe>>>dto ={}", JSONUtil.toJsonStr(dto));
        return new GoodCangDTO.ResultDTO();
    }

}
