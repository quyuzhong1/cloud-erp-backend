package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.ApproveDTO;
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
import com.common.core.entity.BaseEntity;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.CustomerB2bSellerChangeDTO;
import com.erp.model.oms.entity.CustomerB2bSellerChangeEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.server.oms.query.CustomerInfoQueryHandler;
import com.erp.server.oms.service.CustomerB2bSellerChangeService;
import com.erp.server.oms.service.CustomerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Resource
    private CustomerInfoService customerInfoService;
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
    public ApiResult<Object> update(@RequestBody @Validated CustomerB2bSellerChangeDTO.UpdateDTO dto) {
        customerB2bSellerChangeService.update(dto);
        return success();
    }

    /**
     * 修改并提交
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
    public ApiResult<Object> updateAndSubmit(@RequestBody @Validated CustomerB2bSellerChangeDTO.UpdateDTO dto) {
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
        List<BatchResultDTO> batchResultDTOList = customerB2bSellerChangeService.batchCancel(new ApproveDTO.BatchCancelProcessDTO(dto.getIds()));
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
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<CustomerB2bSellerChangeEntity> entityList = customerB2bSellerChangeService.listByIds(ids);
        List<String> mainIds = entityList.stream()
                .map(CustomerB2bSellerChangeEntity::getMainId)
                .collect(Collectors.toList());
        Map<String, CustomerInfoEntity> customerInfoEntityMap = customerInfoService.listByIds(mainIds)
                .stream()
                .collect(Collectors.toMap(BaseEntity::getId, Function.identity()));

        for (String id : ids) {
            CustomerB2bSellerChangeEntity entity = entityList.stream()
                    .filter(v -> v.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            BatchResultDTO result = null; // To store the result for each id

            if (Objects.isNull(entity)) {
                result = BatchResultDTO.fail(id, id, "客户变更销售员信息不存在");
            } else {
                CustomerInfoEntity customerInfoEntity = customerInfoEntityMap.get(entity.getMainId());
                if (Objects.isNull(customerInfoEntity)) {
                    result = BatchResultDTO.fail(id, id, "客户信息不存在");
                } else {
                    try {
                        result = customerB2bSellerChangeService.approve(dto, entity, customerInfoEntity);
                    } catch (Exception e) {
                        log.error("B2B客户变更销售员审核失败", e);
                        result = BatchResultDTO.fail(entity.getId(), customerInfoEntity.getCode(), e.getMessage());
                    }
                }
            }

            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 导出
     * @author lrp
     * @date:  2024-01-31
     * @return ApiResult
     */
    @PostMapping("/export")
    public ApiResult<Object> export(@RequestBody @Validated CustomerB2bSellerChangeDTO.ParamDTO dto) {
         customerB2bSellerChangeService.export(dto);
        return success();
    }
}
