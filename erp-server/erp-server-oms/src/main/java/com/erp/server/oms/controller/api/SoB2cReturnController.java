package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.RefundOrderDTO;
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
import com.erp.server.oms.service.SoB2cReturnService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.SoB2cReturnDTO;

/**
 * b2c退货订单
 *
 * @author lrp
 * @since 2024-10-09
 */
@Slf4j
@RestController
@LogSystemModule("b2c退货订单")
@RequestMapping("/soB2cReturn")
public class SoB2cReturnController extends BaseController {

    @Resource
    private SoB2cReturnService soB2cReturnService;

    /**
     * 退款订单分页
     *
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<SoB2cReturnDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<SoB2cReturnDTO.PagingParamDTO> dto) {
        PagingVO<SoB2cReturnDTO.PagingViewDTO> pagingVO = soB2cReturnService.paging(dto);
        return success(pagingVO);
    }

    /**
    * 新增
    * @author lrp
    * @date:  2024-10-09
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "b2c退货订单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SoB2cReturnDTO.AddDTO dto) {
        return success(soB2cReturnService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2024-10-09
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "b2c退货订单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:soB2cReturn:update",
        serviceClass = SoB2cReturnService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SoB2cReturnDTO.UpdateDTO dto) {
        soB2cReturnService.update(dto);
        return success();
    }



}
