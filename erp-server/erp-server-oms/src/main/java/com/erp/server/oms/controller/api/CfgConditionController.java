package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.CfgConditionDTO;
import com.erp.server.oms.service.CfgConditionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 规则下拉
 *
 * @author Lambda
 * @since 2023-08-30
 */
@Slf4j
@RestController
@RequestMapping("/cfCondition")
@LogSystemModule("规则下拉")
public class CfgConditionController extends BaseController {

    @Resource
    private CfgConditionService cfConditionService;

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-08-30
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "规则新增")
    public ApiResult<String> add(@RequestBody @Validated CfgConditionDTO.AddDTO dto) {
        return success(cfConditionService.add(dto));
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author Lambda
     * @date: 2023-08-30
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "规则修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:cfCondition:update",
            serviceClass = CfgConditionService.class,
            keyIdName = "id")
    public ApiResult<Object> update(@RequestBody @Validated CfgConditionDTO.UpdateDTO dto) {
        cfConditionService.update(dto);
        return success();
    }

    /**
     * 根据条件code 获取到对应逻辑关系
     *
     * @param conditionCode
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<CfgConditionDTO.CommonDTO>> listByConditionCode(@RequestParam("conditionCode") String conditionCode) {
        List<CfgConditionDTO.CommonDTO> resultList = cfConditionService.listByConditionCode(conditionCode);
        return success(resultList);

    }


    /**
     * 所有的条件下拉
     *
     * @param
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.oms.dto.CfConditionDTO.CommonDTO>>
     * @author yl
     * @date 2023-10-08 14:38
     */
    @GetMapping("/listAll")
    public ApiResult<List<CfgConditionDTO.ListDTO>> listAllCondition() {
        List<CfgConditionDTO.ListDTO> result = cfConditionService.listAllCondition();
        return success(result);
    }

    /**
     * 申报规则的条件下拉
     *
     * @param
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.oms.dto.CfConditionDTO.CommonDTO>>
     * @author yl
     * @date 2023-10-08 14:38
     */
    @GetMapping("/listDeclareCondition")
    public ApiResult<List<CfgConditionDTO.ListDTO>> listDeclareCondition() {
        List<CfgConditionDTO.ListDTO> result = cfConditionService.listDeclareCondition();
        return success(result);
    }

    /**
     * 订单处理规则的条件下拉
     * @author Will
     * @date: 2024/5/9 14:33
     * @return ApiResult<List<ListDTO>>
     */
    @GetMapping("/listOrderHandleCondition")
    public ApiResult<List<CfgConditionDTO.ListDTO>> listOrderHandleCondition() {
        List<CfgConditionDTO.ListDTO> result = cfConditionService.listOrderHandleCondition();
        return success(result);
    }
    /**
     * 发票处理规则的条件下拉
     * @author zdy
     * @date: 2025/5/26 14:33
     * @return ApiResult<List<ListDTO>>
     */
    @GetMapping("/listInvoiceHandleCondition")
    public ApiResult<List<CfgConditionDTO.ListDTO>> listInvoiceHandleCondition() {
        List<CfgConditionDTO.ListDTO> result = cfConditionService.listInvoiceHandleCondition();
        return success(result);
    }

    /**
     * 条件 树结构
     * @author yl
     * @date 2023-10-08 15:08
     * @param
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.oms.dto.CfConditionDTO.TreeDTO>>
     */
    @GetMapping("/tree")
    public ApiResult<List<CfgConditionDTO.TreeDTO>> tree() {
        List<CfgConditionDTO.TreeDTO> result = cfConditionService.tree();
        return success(result);
    }




}
