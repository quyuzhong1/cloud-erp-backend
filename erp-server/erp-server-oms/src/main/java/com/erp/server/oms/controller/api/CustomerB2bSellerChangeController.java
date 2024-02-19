package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.CustomerB2bSellerChangeDTO;
import com.erp.server.oms.query.CustomerInfoQueryHandler;
import com.erp.server.oms.service.CustomerB2bSellerChangeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * b2b客户销售员变更单
 *
 * @author lrp
 * @since 2024-01-31
 */
@Slf4j
@RestController
@LogSystemModule("b2b客户销售员变更单")
@RequestMapping("/customerB2bSellerChange")
public class CustomerB2bSellerChangeController extends BaseController {

    @Resource
    private CustomerB2bSellerChangeService customerB2bSellerChangeService;

    /**
     * 获取tabFlag
     */
    @GetMapping("/tabFlag")
    public ApiResult<List<CustomerB2bSellerChangeDTO.TabFlagDTO>> tabFlag() {
        return success(customerB2bSellerChangeService.tabFlag());
    }
    /**
     * 分页
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = CustomerInfoQueryHandler.class)
    public ApiResult<PagingVO<CustomerB2bSellerChangeDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CustomerB2bSellerChangeDTO.ParamDTO> dto) {
        return success(customerB2bSellerChangeService.paging(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2024-01-31
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "b2b客户销售员变更单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:customerB2bSellerChange:update",
        serviceClass = CustomerB2bSellerChangeService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CustomerB2bSellerChangeDTO.UpdateDTO dto) {
        customerB2bSellerChangeService.update(dto);
        return success();
    }

    /**
     * 修改
     * @author lrp
     * @date:  2024-01-31
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/updateAndSubmit")
    @LogAction(value = LogActionEnum.UPDATE, desc = "b2b客户销售员变更单修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:customerB2bSellerChange:update",
            serviceClass = CustomerB2bSellerChangeService.class,
            keyIdName = "id")
    public ApiResult<?> updateAndSubmit(@RequestBody @Validated CustomerB2bSellerChangeDTO.UpdateDTO dto) {
        customerB2bSellerChangeService.updateAndSubmit(dto);
        return success();
    }

    /**
     * 批量提交审核
     * @author lrp
     * @date:  2024-01-31
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/batchSubmit")
    @LogAction(value = LogActionEnum.UPDATE, desc = "b2b客户销售员变更单修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:customerB2bSellerChange:update",
            serviceClass = CustomerB2bSellerChangeService.class,
            keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> batchResultDTOList = customerB2bSellerChangeService.batchSubmit(dto.getIds());
        return batchResultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(batchResultDTOList) : failure(batchResultDTOList);
    }

    /**
     * 批量删除
     * @author lrp
     * @date:  2024-01-31
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/batchDelete")
    @LogAction(value = LogActionEnum.DELETE, desc = "b2b客户销售员变更单删除")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:customerB2bSellerChange:update",
            serviceClass = CustomerB2bSellerChangeService.class,
            keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> batchResultDTOList = customerB2bSellerChangeService.batchDelete(dto.getIds());
        return batchResultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(batchResultDTOList) : failure(batchResultDTOList);
    }


    /**
     * 批量撤销
     * @author lrp
     * @date:  2024-01-31
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/batchCancel")
    @LogAction(value = LogActionEnum.CANCEL, desc = "b2b客户销售员变更单撤销")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:customerB2bSellerChange:update",
            serviceClass = CustomerB2bSellerChangeService.class,
            keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> batchCancel(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> batchResultDTOList = customerB2bSellerChangeService.batchCancel(dto.getIds());
        return batchResultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(batchResultDTOList) : failure(batchResultDTOList);
    }


    /**
     * 批量审核
     * @author lrp
     * @date:  2024-01-31
     * @return ApiResult
     */
    @PostMapping("/batchApprove")
    @LogAction(value = LogActionEnum.APPROVE, desc = "b2b客户销售员变更单审核")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:customerB2bSellerChange:update",
            serviceClass = CustomerB2bSellerChangeService.class,
            keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        List<BatchResultDTO> batchResultDTOList = customerB2bSellerChangeService.batchApprove(baseApproveParamDTO);
        return batchResultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(batchResultDTOList) : failure(batchResultDTOList);
    }

    /**
     * 导出
     * @author lrp
     * @date:  2024-01-31
     * @return ApiResult
     */
    @PostMapping("/export")
    @WebAdvanceQuery(handler = CustomerInfoQueryHandler.class)
    public ApiResult export(@RequestBody @Validated CustomerB2bSellerChangeDTO.ParamDTO dto, HttpServletResponse response) {
         customerB2bSellerChangeService.export(dto,response);
        return success();
    }
}
