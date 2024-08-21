package com.erp.rpc.dmp.feign;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpPullTaskDTO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "erp-dmp", contextId = "exportDmpFeign")
public interface ExportDmpFeign {

    @PostMapping("/feign/export/pullTaskHistory")
    PagingVO<DmpPullTaskDTO.ListDTO> exportPullTaskHistory(PagingDTO<DmpPullTaskDTO.ParamDTO> dto);
    @PostMapping("/feign/export/pullTask")
    PagingVO<DmpPullTaskDTO.ListDTO> exportPullTask(PagingDTO<DmpPullTaskDTO.ParamDTO> dto);
    @PostMapping("/feign/export/pushTask")
    PagingVO<DmpPushTaskDTO.ListDTO> exportPushTask(PagingDTO<DmpPushTaskDTO.ParamDTO> dto);
    @PostMapping("/feign/export/pushTaskHistory")
    PagingVO<DmpPushTaskDTO.ListDTO> exportPushTaskHistory(PagingDTO<DmpPushTaskDTO.ParamDTO> dto);
}
