package com.erp.server.dmp.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.server.dmp.pull.service.mabang.MabangDeliveryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @CreateTime: 2023-06-30  11:55
 * @Author: zhangchunlin
 */
@RestController
@RequestMapping(value = "/test")
public class TestController extends BaseController {

    @Autowired
    private MabangDeliveryServiceImpl deliveryService;

    @GetMapping("testFbaDelivery")
    public ApiResult<Void> testFbaDelivery() {
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        PlatformApiEnum apiEnum = PlatformApiEnum.MABANG_DELIVERY;
        jobTaskDTO.setApiCode(apiEnum.getTaskName());
        jobTaskDTO.setApiId(5);
        jobTaskDTO.setApiName("获取FBA发货单列表");
        jobTaskDTO.setId(30L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.parse("2023-07-03 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2023-07-03 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setPlatformId(4);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(apiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        try {
            deliveryService.pullDataSave(requestDTO);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return success();
    }

}