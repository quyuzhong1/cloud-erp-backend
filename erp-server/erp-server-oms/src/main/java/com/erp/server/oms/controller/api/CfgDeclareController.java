package com.erp.server.oms.controller.api;


import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.RuleLogisticsDTO;
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
import com.erp.server.oms.service.CfgDeclareService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.CfgDeclareDTO;

/**
 * 申报规则表
 *
 * @author zdy
 * @since 2024-05-08
 */
@Slf4j
@RestController
@LogSystemModule("申报规则表")
@RequestMapping("/cfgDeclare")
public class CfgDeclareController extends BaseController {

    @Resource
    private CfgDeclareService cfgDeclareService;

    /**
    * 新增
    * @author zdy
    * @date:  2024-05-08
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "申报规则表新增")
    public ApiResult<String> add(@RequestBody @Validated CfgDeclareDTO.AddDTO dto) {
        return success(cfgDeclareService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2024-05-08
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "申报规则表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:cfgDeclare:update",
        serviceClass = CfgDeclareService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgDeclareDTO.UpdateDTO dto) {
        cfgDeclareService.update(dto);
        return success();
    }

    /**
     * 申报规则分页查询
     *
     * @param dto
     * @return ApiResult<String>
     * @author zdy
     * @date: 2024-05-08
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<CfgDeclareDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<CfgDeclareDTO.PagingParamDTO> dto) {
        PagingVO<CfgDeclareDTO.PagingViewDTO> pagingVO = cfgDeclareService.paging(dto);
        return success(pagingVO);
    }
    /**
     * 申报规则详情
     *
     * @param id
     * @return ApiResult
     * @author Lambda
     * @date: 2023-08-28
     */
    @GetMapping("/view")
    public ApiResult<CfgDeclareDTO.ViewDTO> view(@RequestParam("id") String id) {
        CfgDeclareDTO.ViewDTO viewDTO = cfgDeclareService.view(id);
        return success(viewDTO);
    }

    /**
     * 申报规则更改启用禁用状态
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-08-30 14:13
     */
    @PostMapping("/updateStatus")
    public ApiResult updateStatus(@RequestBody @Validated UpdateStateDTO dto) {
        Boolean result = cfgDeclareService.updateStatus(dto);
        return result ? success() : failure();
    }
}
