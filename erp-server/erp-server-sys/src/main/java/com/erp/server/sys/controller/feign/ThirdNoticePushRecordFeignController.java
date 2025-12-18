package com.erp.server.sys.controller.feign;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.ThirdNoticePushRecordEntity;
import com.erp.server.sys.service.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
}
