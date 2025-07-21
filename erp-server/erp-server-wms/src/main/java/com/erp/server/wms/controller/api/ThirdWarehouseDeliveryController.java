package com.erp.server.wms.controller.api;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.wms.dto.ThirdWarehouseDeliveryDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseDTO.WarehouseUpdateStateDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.sys.feign.AuthDataFeign;
import com.erp.server.wms.query.ThirdWarehouseDeliveryQueryHandler;
import com.erp.server.wms.query.WarehouseQueryHandler;
import com.erp.server.wms.service.ThirdWarehouseDeliveryService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 三方仓发货单
 *
 */
@Slf4j
@RestController
@LogSystemModule("三方仓发货单")
@RequestMapping("/thirdWarehouseDelivery")
public class ThirdWarehouseDeliveryController extends BaseController {

    @Resource
    private ThirdWarehouseDeliveryService thirdWarehouseDeliveryService;

    /**
     * 三方仓发货单
     *
     * @param
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = ThirdWarehouseDeliveryQueryHandler.class)
    public ApiResult<PagingVO<ThirdWarehouseDeliveryDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<ThirdWarehouseDeliveryDTO.PagingParamDTO> dto) {
        PagingVO<ThirdWarehouseDeliveryDTO.PagingViewDTO> pagingVO = thirdWarehouseDeliveryService.paging(dto);
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
    public ApiResult export(@RequestBody @Validated ThirdWarehouseDeliveryDTO.PagingParamDTO dto) {
        thirdWarehouseDeliveryService.export(dto);
        return success(true);
    }

    /**
     * 详情
     *
     * @param
     * @return
     */
    @PostMapping("/view")
    public ApiResult<ThirdWarehouseDeliveryDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        return success(thirdWarehouseDeliveryService.view(dto.getId()));
    }
}
