package com.erp.server.srm.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.server.srm.query.PoReconciliationDetailScmQueryHandler;
import com.erp.server.srm.service.PoReconciliationDetailScmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 采购对账单明细【scm】
 *
 * @author will
 * @since 2024-01-19
 */
@Slf4j
@RestController
@LogSystemModule("采购对账单明细")
@RequestMapping("/poReconciliationDetail/scm")
public class PoReconciliationDetailScmController extends BaseController {

    @Resource
    private PoReconciliationDetailScmService poReconciliationDetailScmService;


    /**
     * 分页查询
     * @author Will
     * @date: 2024/1/20 11:31
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliationDetail:scm:paging",
            tableAlias = "prd"
    )
    @WebAdvanceQuery(handler = PoReconciliationDetailScmQueryHandler.class)
    public ApiResult<PagingVO<PoReconciliationDetailDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<PoReconciliationDetailDTO.PagingParamDTO> dto) {
        return success(poReconciliationDetailScmService.paging(dto));
    }


    /**
     * 导出Excel
     * @author Will
     * @date: 2024/1/20 12:03
     * @param dto
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "采购对账单导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated PoReconciliationDetailDTO.PagingParamDTO dto) {
        poReconciliationDetailScmService.exportList(dto);
        return success(true);
    }


    /**
     * 生成对账单
     * @author Will
     * @date: 2024/1/23 18:00
     * @param dto
     */
    @PostMapping("/generatePoReconciliation")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliationDetail:scm:generatePoReconciliation",
            tableAlias = "prd"
    )
    public ApiResult<Object> generatePoReconciliation(@RequestBody @Validated PoReconciliationDetailDTO.GeneratePoReconciliationDTO dto) {
        Boolean flag = poReconciliationDetailScmService.generatePoReconciliation(dto);
        return flag == true ? success() : failure();
    }

}
