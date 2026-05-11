package com.erp.rpc.sys.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.ThirdNoticePushRecordEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-sys", contextId = "thirdNoticePushRecordFeign", configuration = ExportFeignConfig.class)
public interface ThirdNoticePushRecordFeign {

    @PostMapping("/feign/thirdNoticePushRecord/sendThirdNoticeByMqAsync")
    void sendThirdNoticeByMqAsync(@RequestBody ThirdNoticePushRecordDTO.SendThirdNoticeParamsDTO dto);

    @PostMapping("/feign/thirdNoticePushRecord/updateStatusById")
    Boolean updateStatusById(@RequestBody ThirdNoticePushRecordEntity entity);

    @PostMapping("/feign/thirdNoticePushRecord/batchSendMqRecordConsumer")
    Boolean batchSendMqRecordConsumer(@RequestBody List<String> jsonStrList);

}
