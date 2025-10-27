package com.erp.server.plm.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.MoldMonitorDTO;
import com.erp.server.plm.query.MoldInfoQueryHandler;
import com.erp.server.plm.query.MoldMonitorQueryHandler;
import com.erp.server.plm.service.MoldInfoService;
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
import com.erp.server.plm.service.MoldMonitorService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.plm.dto.MoldMonitorDTO;

import java.util.List;

/**
 * 模具监控
 *
 * @author jack
 * @since 2025-10-22
 */
@Slf4j
@RestController
@LogSystemModule("模具监控")
@RequestMapping("/moldMonitor")
public class MoldMonitorController extends BaseController {

    @Resource
    private MoldMonitorService moldMonitorService;

    /**
     * 获取状态统计
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:moldMonitor:paging",
            tableAlias = "mm"
    )
    public ApiResult<List<MoldMonitorDTO.TabListDTO>> tabList(@RequestBody MoldMonitorDTO.TabDTO dto) {
        return success(moldMonitorService.tabList(dto));
    }

    /**
     * 列表查询
     * @author jack
     * @date: 2025-10-10
     * @param dto
     * @return ApiResult<PagingVO<MoldMonitorDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:moldMonitor:paging",
            tableAlias = "mm"
    )
    @WebAdvanceQuery(handler = MoldMonitorQueryHandler.class)
    public ApiResult<PagingVO<MoldMonitorDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<MoldMonitorDTO.PagingParamDTO> dto) {
        return success(moldMonitorService.paging(dto));
    }

    /**
     * 详情
     * @author jack
     * @date:  2025-10-10
     * @param id
     * @return ApiResult<MoldMonitorDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:moldMonitor:view",
            serviceClass = MoldMonitorService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<MoldMonitorDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(moldMonitorService.view(id));
    }


    /**
     * 根据模具id 查询关联订单
     * @author jack
     * @date: 2025-10-10
     * @param dto
     * @return ApiResult<List<MoldMonitorDTO.ListDTO>>
     */
    @PostMapping("/listRefOrderById")
    public ApiResult<List<MoldMonitorDTO.RefOrderDTO>> listRefOrderById(@RequestBody @Validated MoldMonitorDTO.RefOrderParamsDTO dto) {
        return success(moldMonitorService.listRefOrderById(dto));
    }


    /**
     * 返还确认
     * @author jack
     * @date: 2025-10-10
     * @param dto
     * @return ApiResult<BatchResultDTO>
     */
    @PostMapping("/updateReturnPriceById")
    public ApiResult<BatchResultDTO> updateReturnPriceById(@RequestBody @Validated MoldMonitorDTO.UpdateReturnParamsDTO dto) {
        return success(moldMonitorService.updateReturnPriceById(dto));
    }






}
