package com.erp.rpc.sys.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-sys", contextId = "thirdNoticePushRecordFeign", configuration = ExportFeignConfig.class)
public interface ThirdNoticePushRecordFeign {

    @PostMapping("/feign/thirdNoticePushRecord/sendThirdNoticeByMqAsync")
    void sendThirdNoticeByMqAsync(@RequestBody ThirdNoticePushRecordDTO.SendThirdNoticeParamsDTO dto);

}
