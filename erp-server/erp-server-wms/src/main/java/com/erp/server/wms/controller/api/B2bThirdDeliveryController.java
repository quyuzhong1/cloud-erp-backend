package com.erp.server.wms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.ThirdWarehouseDeliveryDTO;
import com.erp.server.wms.query.ThirdWarehouseDeliveryQueryHandler;
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
import com.erp.server.wms.service.B2bThirdDeliveryService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.B2bThirdDeliveryDTO;

import java.util.List;

/**
 * B2B三方发货单
 *
 * @author zdy
 * @since 2025-11-26
 */
@Slf4j
@RestController
@LogSystemModule("B2B三方发货单")
@RequestMapping("/b2bThirdDelivery")
public class B2bThirdDeliveryController extends BaseController {

    @Resource
    private B2bThirdDeliveryService b2bThirdDeliveryService;
    /**
     * tab页
     *
     * @param
     * @return
     */
    @PostMapping("/tabList")
    public ApiResult<List<B2bThirdDeliveryDTO.TabListDTO>> tabList() {
        return success(b2bThirdDeliveryService.tabList());
    }
    /**
     * 分页查询
     *
     * @param
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = ThirdWarehouseDeliveryQueryHandler.class)
    public ApiResult<PagingVO<B2bThirdDeliveryDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<B2bThirdDeliveryDTO.PagingParamDTO> dto) {
        PagingVO<B2bThirdDeliveryDTO.PagingViewDTO> pagingVO = b2bThirdDeliveryService.paging(dto);
        return success(pagingVO);
    }
    /**
     * 导出
     *
     * @param
     * @return
     */
    @PostMapping("/export")
    @WebAdvanceQuery(handler = ThirdWarehouseDeliveryQueryHandler.class)
    public ApiResult export(@RequestBody @Validated B2bThirdDeliveryDTO.PagingParamDTO dto) {
        b2bThirdDeliveryService.export(dto);
        return success(true);
    }
    /**
     * 详情
     *
     * @param
     * @return
     */
    @PostMapping("/view")
    public ApiResult<B2bThirdDeliveryDTO.ViewDTO> view(@RequestBody @Validated B2bThirdDeliveryDTO.ViewQueryDTO dto) {
        return success(b2bThirdDeliveryService.view(dto.getId(),dto.getSoId()));
    }

    /**
    * 新增
    * @author zdy
    * @date:  2025-11-26
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "B2B三方发货单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated B2bThirdDeliveryDTO.AddDTO dto) {
        return success(b2bThirdDeliveryService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-11-26
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "B2B三方发货单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:b2bThirdDelivery:update",
        serviceClass = B2bThirdDeliveryService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated B2bThirdDeliveryDTO.UpdateDTO dto) {
        b2bThirdDeliveryService.update(dto);
        return success();
    }



}
