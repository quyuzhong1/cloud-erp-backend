package com.erp.server.dmp.controller.api;

import cn.hutool.core.collection.CollectionUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.EnumCacheUtils;
import com.erp.model.dmp.dto.DmpOutputTaskDTO;
import com.erp.model.dmp.dto.DmpRestCloudDTO;
import com.erp.model.plm.vo.SchedulePagingVO;
import com.erp.server.dmp.query.DmpOutputTaskQueryHandler;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * RestCloud公共信息
 *
 * @author Jin
 * @since 2025-10-24
 */
@Slf4j
@RestController
@LogSystemModule("RestCloud公共信息")
@RequestMapping("/restCloud")
public class DmpRestCloudController extends BaseController {

    @Value("${restcloud.url:172.16.100.96}")
    private String restcloudUrl;

    @Value("${restcloud.port:8080}")
    private String restcloudPort;

    /**
     * RestCloud流程列表查询
     * @author Jim
     * @date: 2025-10-23
     * @param dto
     * @return ApiResult<PagingVO<DmpRestCloudDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = DmpOutputTaskQueryHandler.class)
    public ApiResult<PagingVO<DmpRestCloudDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<DmpRestCloudDTO.PagingParamDTO> dto) {
        PagingVO<DmpRestCloudDTO.ListDTO> restCloudPaging = new PagingVO<>();
        // TODO: 调用RestCloud接口获取流程列表数据
        return success(restCloudPaging);
    }

}
