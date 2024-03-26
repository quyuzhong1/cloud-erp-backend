package com.erp.server.tms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDetailDTO;
import com.erp.server.tms.query.TmsB2cDeclareReconciliationDetailQueryHandler;
import com.erp.server.tms.service.TmsB2cDeclareReconciliationDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * b2c报关对账单明细
 *
 * @author will
 * @since 2024-03-19
 */
@Slf4j
@RestController
@LogSystemModule("b2c报关对账单明细")
@RequestMapping("/tmsB2cDeclareReconciliationDetail")
public class TmsB2cDeclareReconciliationDetailController extends BaseController {

    @Resource
    private TmsB2cDeclareReconciliationDetailService tmsB2cDeclareReconciliationDetailService;


    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsB2cDeclareReconciliation:paging",
            tableAlias = "tbdr"
    )
    @WebAdvanceQuery(handler = TmsB2cDeclareReconciliationDetailQueryHandler.class)
    public ApiResult<PagingVO<TmsB2cDeclareReconciliationDetailDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<TmsB2cDeclareReconciliationDetailDTO.PagingParamDTO> dto) {
        return success(tmsB2cDeclareReconciliationDetailService.paging(dto));
    }

    /**
    * 更新对账状态
    * @author will
    * @date:  2024-03-19
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/updateStatus")
    @LogAction(value = LogActionEnum.INSERT, desc = "更新对账状态")
    public ApiResult<BaseResultDTO.AddDTO> updateStatus(@RequestBody @Validated TmsB2cDeclareReconciliationDetailDTO.UpdateStatusDTO dto) {
        return success(tmsB2cDeclareReconciliationDetailService.updateStatus(dto));
    }


}
