package com.erp.server.tms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.ShippingCalculationDTO;
import com.erp.model.tms.dto.ShippingTemplateDTO;
import com.erp.server.tms.service.ShippingCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 运费计算控制层
 * @author Will
 * @version 1.0
 * @date 2023/11/10 12:16
 */
@Slf4j
@RestController
@LogSystemModule("运费计算")
@RequestMapping("/shippingCalculation")
public class ShippingCalculationController extends BaseController {

    @Resource
    private ShippingCalculationService shippingCalculationService;


    /**
     * 运费计算列表
     * @author Will
     * @date: 2023/11/10 17:35
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:shippingCalculation:paging",
            tableAlias = "st"
    )
    public ApiResult<PagingVO<ShippingCalculationDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<ShippingCalculationDTO.PagingParamDTO> dto) {
        PagingVO<ShippingCalculationDTO.ListDTO> pagingVO = shippingCalculationService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 运费计算导出
     * @author Will
     * @date: 2023/11/10 17:36
     * @param dto
     * @param response
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:shippingCalculation:paging",
            tableAlias = "st"
    )
    public ApiResult exportExcel(@RequestBody ShippingCalculationDTO.PagingParamDTO dto, HttpServletResponse response) {
        Boolean flag = shippingCalculationService.exportExcel(dto, response);
        return flag == true ? success() : failure();
    }
}
