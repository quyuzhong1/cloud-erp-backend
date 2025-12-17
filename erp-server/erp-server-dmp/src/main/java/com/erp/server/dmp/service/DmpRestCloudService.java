package com.erp.server.dmp.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpRestCloudDTO;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * restCloud服务调用接口
 *
 * @author Jim
 * @date 2025/10/30 17:11
 * @Return
 */
public interface DmpRestCloudService {

    /**
     * 流程信息分页接口
     * @param dto 请求参数
     * @return 响应json
     */
    PagingVO<DmpRestCloudDTO.ListDTO> flowPaging(@Validated PagingDTO<DmpRestCloudDTO.PagingParamDTO> dto);
}
