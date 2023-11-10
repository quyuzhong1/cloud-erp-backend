package com.erp.server.tms.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.ShippingTemplateDTO;
import com.erp.server.tms.service.ShippingCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 运费计算控制层
 * @date 2023/11/10 12:16
 */
@Slf4j
@RestController
@LogSystemModule("运费计算")
@RequestMapping("/shippingCalculation")
public class ShippingCalculationController extends BaseController {

    @Resource
    private ShippingCalculationService shippingCalculationService;


    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:shippingCalculation:paging",
            tableAlias = "st"
    )
    public ApiResult<PagingVO<ShippingTemplateDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<ShippingTemplateDTO.PagingParamDTO> dto) {
        PagingVO<ShippingTemplateDTO.ListDTO> pagingVO = shippingCalculationService.paging(dto);
        return success(pagingVO);
    }
}
