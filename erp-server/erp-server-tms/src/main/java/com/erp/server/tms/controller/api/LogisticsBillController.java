package com.erp.server.tms.controller.api;


import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.LogisticsBillService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;

import java.util.List;

/**
 * 物流单
 *
 * @author lambda
 * @since 2023-11-09
 */
@Slf4j
@RestController
@LogSystemModule("物流单")
@RequestMapping("/logisticsBill")
public class LogisticsBillController extends BaseController {

    @Resource
    private LogisticsBillService logisticsBillService;


    /**
     * tab 列表
     *
     * @param dto
     * @author yl
     * @date 2023-11-09 10:54
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:logisticsBill:paging",
            tableAlias = "logistics_bill_detail"
    )
    public ApiResult<List<LogisticsBillDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<LogisticsBillDTO.TabListDTO> tabList = logisticsBillService.tabList(dto);
        return success(tabList);
    }


    /**
     * tab 列表
     *
     * @param dto
     * @author yl
     * @date 2023-11-09 10:54
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:logisticsBill:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<LogisticsBillDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<LogisticsBillDTO.PagingParamDTO> dto) {
        PagingVO<LogisticsBillDTO.PagingVO> pagingVO = logisticsBillService.paging(dto);
        return success(pagingVO);
    }


}
