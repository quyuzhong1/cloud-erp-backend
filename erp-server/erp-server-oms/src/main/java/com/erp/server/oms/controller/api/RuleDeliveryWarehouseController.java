package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.RuleDeliveryWarehouseDTO;
import com.erp.server.oms.service.RuleDeliveryWarehouseService;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 订单规则
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Slf4j
@RestController
@RequestMapping("/ruleDeliveryWarehouse")
@LogSystemModule("发货仓库规则表")
public class RuleDeliveryWarehouseController extends BaseController {

    @Resource
    private RuleDeliveryWarehouseService ruleDeliveryWarehouseService;




    /**
     * 发货仓库规则分页查询
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-08-28
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<RuleDeliveryWarehouseDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<RuleDeliveryWarehouseDTO.PagingParamDTO> dto) {
        PagingVO<RuleDeliveryWarehouseDTO.PagingViewDTO> pagingVO = ruleDeliveryWarehouseService.paging(dto);
        return success(pagingVO);
    }

    /**
    * 发货仓库规则新增
    * @author Lambda
    * @date:  2023-08-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "发货仓库规则新增")
    public ApiResult<String> add(@RequestBody @Validated RuleDeliveryWarehouseDTO.AddDTO dto) {
        return success(ruleDeliveryWarehouseService.add(dto));
    }

    /**
    * 发货仓库规则修改
    * @author Lambda
    * @date:  2023-08-28
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "发货仓库规则修改")
    public ApiResult update(@RequestBody @Validated RuleDeliveryWarehouseDTO.UpdateDTO dto) {
        Boolean result = ruleDeliveryWarehouseService.update(dto);
        return result?success():failure();
    }


    /**
     * 发货仓库规则表详情
     *
     * @param id
     * @return ApiResult
     * @author Lambda
     * @date: 2023-08-28
     */
    @GetMapping("/view")
    public ApiResult<RuleDeliveryWarehouseDTO.ViewDTO> view(@RequestParam("id") String id) {
        RuleDeliveryWarehouseDTO.ViewDTO viewDTO = ruleDeliveryWarehouseService.view(id);
        return success(viewDTO);
    }
    /**
     * 发货仓库规则表更改启用禁用状态
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-08-30 14:13
     */
    @PostMapping("/updateStatus")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "发货仓库规则表更改启用禁用状态 ids={id},状态值={state}(true=禁用,false=启用)")
    public ApiResult updateStatus(@RequestBody @Validated UpdateStateDTO dto) {
        Boolean result = ruleDeliveryWarehouseService.updateStatus(dto);
        return result ? success() : failure();
    }
}
