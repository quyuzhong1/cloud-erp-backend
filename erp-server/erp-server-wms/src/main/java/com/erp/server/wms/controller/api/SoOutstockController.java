package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.server.wms.service.SoOutstockService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 销售出库-销售出库单
 *
 * @author lambda
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/so/outstock")
public class SoOutstockController extends BaseController {

    @Resource
    private SoOutstockService soOutstockService;

    /**
     * 获取 tab列表
     *
     * @return
     */
    @GetMapping("/tabList")
    public ApiResult<List<SoOutstockDTO.TabListDTO>> tabList() {
        List<SoOutstockDTO.TabListDTO> tabList = soOutstockService.tabList();
        return success(tabList);
    }

    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SoOutstockDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<SoOutstockDTO.PagingParamDTO> dto) {
        return success(null);
    }

    /**
     * 创建
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:so:outstock:add",
            serviceClass = SoOutstockService.class,
            keyIdName = "id"
    )
    public ApiResult add(@RequestBody @Validated SoOutstockDTO.AddDTO dto) {
        String id = soOutstockService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 批量提交审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:so:outstock:submit",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids"
    )
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = soOutstockService.submit(dto.getIds());
        return result ? success() : failure();
    }


    /**
     * 提交审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:so:outstock:addAndSubmit",
            serviceClass = SoOutstockService.class,
            keyIdName = "id"
    )
    public ApiResult addAndSubmit(@RequestBody @Validated SoOutstockDTO.AddDTO dto) {
        Boolean result = soOutstockService.addAndSubmit(dto);
        return result ? success() : failure();
    }

    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:so:outstock:view",
            serviceClass = SoOutstockService.class,
            keyIdName = "id"
    )
    public ApiResult<SoOutstockDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        SoOutstockDTO.ViewDTO view = soOutstockService.view(dto.getId());
        return success(view);
    }

    /**
     * 修改
     *
     * @param dto
     * @return
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated SoOutstockDTO.UpdateDTO dto) {
        return success(null);
    }

    /**
     * 修改并提交
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateAndSubmit")
    public ApiResult updateAndSubmit(@RequestBody @Validated SoOutstockDTO.UpdateDTO dto) {
        return success(null);
    }


    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:so:outstock:approve",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids"
    )
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        Boolean result = soOutstockService.approve(dto);
        return result?success():failure();
    }

    /**
     * 反审核
     */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:so:outstock:disApprove",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids"
    )
    public ApiResult disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = soOutstockService.disApprove(dto);
        return result?success():failure();
    }


    /**
     * 撤销流程
     *
     * @param dto
     * @return
     */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:so:outstock:cancelProcess",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = soOutstockService.cancelProcess(dto.getIds());
        return result ? success() : failure();
    }

    /**
     * 删除销售出库单
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:so:outstock:delete",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = soOutstockService.delete(dto.getIds());
        return result ? success() : failure();
    }

    /**
     * 作废
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023/5/10 20:11
     */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:so:outstock:invalid",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean result = soOutstockService.invalid(dto.getIds(),dto.getRemark());
        return result?success():failure();
    }

    /**
     * 导出
     * 数据
     */
    @PostMapping("/export")
    public ApiResult exportWarehouse(@RequestBody @Valid SoOutstockDTO.ExportDTO dto, HttpServletResponse response) {

        return success();
    }

}
