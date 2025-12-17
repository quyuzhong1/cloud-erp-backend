package com.erp.server.dmp.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.oms.dto.RuleDeliveryWarehouseDTO;
import com.erp.model.tms.dto.LogisticsBillDTO;
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
import com.erp.server.dmp.service.RulePromptWordService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.RulePromptWordDTO;

import java.util.List;

/**
 * 汉化管理规则表
 *
 * @author lrp
 * @since 2025-01-17
 */
@Slf4j
@RestController
@LogSystemModule("汉化管理规则表")
@RequestMapping("/rulePromptWord")
public class RulePromptWordController extends BaseController {

    @Resource
    private RulePromptWordService rulePromptWordService;
    /**
     * tabList
     * @author lrp
     * @date:  2025-01-17
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/tabList")
    public ApiResult<List<RulePromptWordDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(rulePromptWordService.tabList(dto));
    }

    /**
     * 新增
     * @author lrp
     * @date:  2025-01-17
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<RulePromptWordDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<RulePromptWordDTO.PagingParamDTO> dto) {
        return success(rulePromptWordService.paging(dto));
    }

    /**
     * 详情
     * @author lrp
     * @date:  2025-01-17
     */
    @GetMapping("/view")
    public ApiResult<RulePromptWordDTO.ViewDTO> view(@RequestParam("id") String id) {
        RulePromptWordDTO.ViewDTO viewDTO = rulePromptWordService.view(id);
        return success(viewDTO);
    }

    /**
    * 新增
    * @author lrp
    * @date:  2025-01-17
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "汉化管理规则表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated RulePromptWordDTO.AddDTO dto) {
        return success(rulePromptWordService.add(dto));
    }

    /**
    * 修改
    * @author lrp
    * @date:  2025-01-17
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "汉化管理规则表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:rulePromptWord:update",
        serviceClass = RulePromptWordService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated RulePromptWordDTO.UpdateDTO dto) {
        rulePromptWordService.update(dto);
        return success();
    }

    /**
     * 修改状态
     * @author lrp
     * @date:  2025-01-17
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/batchUpdateStatus")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "批量修改汉化管理规则表状态,ids={ids},状态变更为={disabled} (true=启用 false=停用) ")
    public ApiResult<?> batchUpdateStatus(@RequestBody @Validated RulePromptWordDTO.UpdateStatusDTO dto) {
        rulePromptWordService.batchUpdateStatus(dto);
        return success();
    }



}
