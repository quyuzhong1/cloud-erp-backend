package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.CfgRuleInvoiceDTO;
import com.erp.model.oms.entity.CfgRuleInvoiceEntity;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.CfgRuleInvoiceService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * 开票规则
 *
 * @author zdy
 * @since 2025-05-23
 */
@Slf4j
@RestController
@LogSystemModule("开票规则")
@RequestMapping("/cfgRuleInvoice")
public class CfgRuleInvoiceController extends BaseController {

    @Resource
    private CfgRuleInvoiceService cfgRuleInvoiceService;

    /**
    * 开票规则新增
    * @author zdy
    * @date:  2025-05-23
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "开票规则新增")
    public ApiResult<String> add(@RequestBody @Validated CfgRuleInvoiceDTO.AddDTO dto) {
        return success(cfgRuleInvoiceService.add(dto));
    }

    /**
    * 开票规则修改
    * @author zdy
    * @date:  2025-05-23
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "开票规则修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:cfgRuleInvoice:update",
        serviceClass = CfgRuleInvoiceService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgRuleInvoiceDTO.UpdateDTO dto) {
        cfgRuleInvoiceService.update(dto);
        return success();
    }

    /**
     * 开票规则分页查询
     *
     * @param dto
     * @return ApiResult<String>
     * @author zdy
     * @date: 2025-05-23
     */
    @PostMapping("/paging")
    @WebAdvanceQuery
    public ApiResult<PagingVO<CfgRuleInvoiceDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<CfgRuleInvoiceDTO.PagingParamDTO> dto) {
        PagingVO<CfgRuleInvoiceDTO.PagingViewDTO> pagingVO = cfgRuleInvoiceService.paging(dto);
        return success(pagingVO);
    }
    /**
     * 开票规则详情
     *
     * @param id
     * @return ApiResult<String>
     * @author zdy
     * @date: 2025-05-23
     */
    @GetMapping("/view")
    public ApiResult<CfgRuleInvoiceDTO.ViewDTO> view(@RequestParam("id") String id) {
        CfgRuleInvoiceDTO.ViewDTO viewDTO = cfgRuleInvoiceService.view(id);
        return success(viewDTO);
    }

    /**
     * 开票规则更改启用禁用状态
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author zdy
     * @date 2025-05-23
     */
    @PostMapping("/updateStatus")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "启用停用:ids={ids},状态值={disabled}(true=禁用,false=启用)")
    public ApiResult<Object> updateStatus(@RequestBody @Validated UpdateStateDTO.BatchUpdateDTO dto) {
        List<BatchResultDTO> list = new ArrayList<>(dto.getIds().size());
        List<CfgRuleInvoiceEntity> entityList = cfgRuleInvoiceService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            try {
                CfgRuleInvoiceEntity entity = entityList.stream().filter(e -> e.getId().equals(id)).findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "开票规则"));
                BatchResultDTO batchResultDTO = cfgRuleInvoiceService.updateStatus(entity, dto.getDisabled());
                list.add(batchResultDTO);
            }catch (Exception e){
                log.error("开票规则更改启用禁用状态失败", e);
                list.add(new BatchResultDTO(id, id, e.getMessage(),false));
            }
        }
        return list.stream().allMatch(BatchResultDTO::getSuccess) ? success(list) : failure(list);
    }
    /**
     * 批量删除
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "开票规则批量删除")
    public ApiResult<Object> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> list = new ArrayList<>(dto.getIds().size());
        List<CfgRuleInvoiceEntity> entityList = cfgRuleInvoiceService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            try {
                CfgRuleInvoiceEntity entity = entityList.stream().filter(e -> e.getId().equals(id)).findFirst().orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "开票规则"));
                BatchResultDTO batchResultDTO = cfgRuleInvoiceService.delete(entity);
                list.add(batchResultDTO);
            }catch (Exception e){
                log.error("开票规则批量删除失败", e);
                list.add(new BatchResultDTO(id, id, e.getMessage(),false));
            }
        }
        return list.stream().allMatch(BatchResultDTO::getSuccess)? success(list) : failure(list);
    }
}
