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
import com.common.business.annotation.WebAdvanceQuery;
import com.erp.server.tms.service.DeliveryDeclareDetailMidService;
import com.erp.server.tms.query.DeliveryDeclareDetailMidQueryHandler;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import com.common.business.dto.ApproveDTO;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.DeliveryDeclareDetailMidDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.tms.entity.DeliveryDeclareDetailMidEntity;

/**
 * 报关明细中间表
 *
 * @author jack
 * @since 2026-04-27
 */
@Slf4j
@RestController
@LogSystemModule("报关明细中间表")
@RequestMapping("/deliveryDeclareDetailMid")
public class DeliveryDeclareDetailMidController extends BaseController {

    @Resource
    private DeliveryDeclareDetailMidService deliveryDeclareDetailMidService;

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:deliveryDeclareDetailMid:paging",
            tableAlias = "dddm"
    )
    public ApiResult<List<DeliveryDeclareDetailMidDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(deliveryDeclareDetailMidService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2026-04-27
    * @param dto
    * @return ApiResult<PagingVO<DeliveryDeclareDetailMidDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:deliveryDeclareDetailMid:paging",
            tableAlias = "dddm"
    )
    @WebAdvanceQuery(handler = DeliveryDeclareDetailMidQueryHandler.class)
    public ApiResult<PagingVO<DeliveryDeclareDetailMidDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<DeliveryDeclareDetailMidDTO.PagingParamDTO> dto) {
        return success(deliveryDeclareDetailMidService.paging(dto));
    }


    /**
    * 详情
    * @author jack
    * @date:  2026-04-27
    * @param id
    * @return ApiResult<DeliveryDeclareDetailMidDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:deliveryDeclareDetailMid:view",
            serviceClass = DeliveryDeclareDetailMidService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<DeliveryDeclareDetailMidDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(deliveryDeclareDetailMidService.view(id));
    }

    /**
     * 合并前预览
     *
     * @param dto 报关明细中间表id集合
     * @return ApiResult<List<DeliveryDeclareDetailMidDTO.MergePreviewDTO>>
     * @author jack
     * @date 2026-04-29
     */
    @PostMapping("/mergePreview")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:deliveryDeclareDetailMid:mergePreview",
            serviceClass = DeliveryDeclareDetailMidService.class,
            keyIdName = "id")
    public ApiResult<List<DeliveryDeclareDetailMidDTO.MergePreviewDTO>> mergePreview(@RequestBody BaseIdsDTO.IdsDTO dto) {
        return success(deliveryDeclareDetailMidService.mergePreview(dto.getIds()));
    }


}
