package com.erp.server.tms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.BasicQueryLogisticsProviderService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.business.dto.ApproveDTO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.BasicQueryLogisticsProviderDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.tms.entity.BasicQueryLogisticsProviderEntity;

/**
 * 查询物流商信息表
 *
 * @author jack
 * @since 2026-03-31
 */
@Slf4j
@RestController
@LogSystemModule("查询物流商信息表")
@RequestMapping("/basicQueryLogisticsProvider")
public class BasicQueryLogisticsProviderController extends BaseController {

    @Resource
    private BasicQueryLogisticsProviderService basicQueryLogisticsProviderService;


    /**
    * 下拉查询物流商列表
    * @author jack
    * @date: 2026-03-31
    * @param dto
    * @return ApiResult<List<BasicQueryLogisticsProviderDTO.ListAllVO>>
    */
    @PostMapping("/listAll")
    public ApiResult<List<BasicQueryLogisticsProviderDTO.ListAllVO>> listAll(@RequestBody BasicQueryLogisticsProviderDTO.ListAllParamDTO dto) {
        return success(basicQueryLogisticsProviderService.listAll(dto));
    }

}
