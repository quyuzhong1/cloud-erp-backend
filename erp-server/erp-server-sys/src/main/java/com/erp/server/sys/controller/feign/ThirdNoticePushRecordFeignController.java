package com.erp.server.sys.controller.feign;

import cn.hutool.core.collection.CollUtil;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.ThirdNoticePushRecordEntity;
import com.erp.server.sys.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/feign/thirdNoticePushRecord")
public class ThirdNoticePushRecordFeignController {

    @Resource
    private ThirdNoticePushRecordService thirdNoticePushRecordService;

    @PostMapping("/sendThirdNoticeByMqAsync")
    public void exportCfgThirdNoticePushRecord(@RequestBody ThirdNoticePushRecordDTO.SendThirdNoticeParamsDTO dto){
        thirdNoticePushRecordService.sendThirdNoticeByMqAsync(dto.getJsonMap(),dto.getDiffFields());
    }

    @PostMapping("/updateStatusById")
    public Boolean updateStatusById(@RequestBody ThirdNoticePushRecordEntity entity){
        boolean update = thirdNoticePushRecordService.lambdaUpdate()
                .set(ThirdNoticePushRecordEntity::getStatus,entity.getStatus())
                .set(ThirdNoticePushRecordEntity::getSendTime,entity.getSendTime())
                .set(ThirdNoticePushRecordEntity::getErrorReason,entity.getErrorReason())
                .eq(ThirdNoticePushRecordEntity::getId,entity.getId())
                .update();
        return update;
    }

    /**
     *
     * @return
     */
    @PostMapping("/batchSendMqRecordConsumer")
    public Boolean batchSendMqRecordConsumer(@RequestBody ThirdNoticePushRecordDTO.BatchSendMqRecordConsumerDTO dto) {
        boolean success = true;
        if (dto != null && CollUtil.isNotEmpty(dto.getJsonStrList())) {
            for (String jsonStr : dto.getJsonStrList()) {
                try {
                    thirdNoticePushRecordService.sendMqRecordConsumer(jsonStr);
                } catch (Exception e) {
                    success = false;
                    log.error("batchSendMqRecordConsumer failed, jsonStr={}", jsonStr, e);
                }
            }
        }
        return success;
    }
}
