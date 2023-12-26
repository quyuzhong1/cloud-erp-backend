package com.erp.server.dmp.controller.feign;

import com.alibaba.fastjson.JSONObject;
import com.common.core.utils.MapUtil;
import com.erp.model.dmp.dto.AmazonShopInfoDTO;
import com.erp.model.dmp.dto.DmpPullShipmentDTO;
import com.erp.model.dmp.dto.MongoDBUpdateDTO;
import com.erp.model.oms.dto.OmsMongoDTO;
import com.erp.server.dmp.enums.CleanDataTableEnum;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgAppClientService;
import com.erp.server.dmp.service.ReportHandleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Objects;

/**
 * 中台请求亚马逊Feign控制类
 *
 * @Author Cloud
 * @Date 2023/9/1 12:03
 **/
@Slf4j
@RestController
@RequestMapping("feign/mongodb")
public class DmpMongoDbFeignController {

    @Resource
    MongoService mongoService;
    /**
     * 拉取货件
     *
     * @Author Jim
     * @since 2023-10-10
     **/
    @PostMapping("/updateMongoDbData")
    public void updateMongoDbData(@RequestBody MongoDBUpdateDTO dto){
        if (Objects.isNull(dto) || StringUtils.isEmpty(dto.getTableName()) || StringUtils.isEmpty(dto.getUniqueId()) || Objects.isNull(dto.getIsClean())){
            return;
        }
        log.info("获取到配置：{}", JSONObject.toJSONString(dto));
        CleanDataTableEnum cleanDataTableEnum = CleanDataTableEnum.getByName(dto.getTableName());
        if (Objects.isNull(cleanDataTableEnum)) return;
        Class tClass = cleanDataTableEnum.getTClass();
        if (Objects.isNull(tClass)) return;
        OmsMongoDTO updateDto = new OmsMongoDTO();
        updateDto.setUniqueId(dto.getUniqueId());
        MapUtil mapUtil = new MapUtil();
        mapUtil.put("isClean", dto.getIsClean());
        mongoService.updateMongoData(updateDto, mapUtil, dto.getTableName(), tClass);
    }

}
