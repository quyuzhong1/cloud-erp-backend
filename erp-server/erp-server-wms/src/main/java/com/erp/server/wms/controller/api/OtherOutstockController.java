package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.OtherOutstockDTO;
import com.erp.server.wms.service.OtherOutstockService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 *  其他出库单
 *
 * @author will
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/otherOutstock")
public class OtherOutstockController extends BaseController {


    @Resource
    private OtherOutstockService otherOutstockService;
    
    /**
     * 列表查询
     * @author Will
     * @date: 2023/5/10 19:56
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:paging",
            tableAlias = "oo"
    )
    public ApiResult<PagingVO<OtherOutstockDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<OtherOutstockDTO.SearchParamDTO> dto) {
        PagingVO<OtherOutstockDTO.ListDTO> pagingVO = otherOutstockService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表数量
     * @author Will
     * @date: 2023/5/10 20:08
     * @param dto
     * @return ApiResult<List<ListStatusCountDTO>>
     */
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:paging",
            tableAlias = "oo"
    )
    public ApiResult<List<OtherOutstockDTO.ListStatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<OtherOutstockDTO.ListStatusCountDTO> list = otherOutstockService.listCount(dto);
        return success(list);
    }

    /**
     * 新增
     * @author Will
     * @date: 2023/5/10 19:58
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:add",
            serviceClass = OtherOutstockService.class,
            keyIdName = "id")
    public ApiResult add(@RequestBody @Validated OtherOutstockDTO.AddDTO dto) {
        String id = otherOutstockService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 新增并提交
     * @author Will
     * @date: 2023/5/10 19:59
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:add",
            serviceClass = OtherOutstockService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated OtherOutstockDTO.AddDTO dto) {
        String id = otherOutstockService.addAndSubmit(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改
     * @author Will
     * @date: 2023/5/10 20:02
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:update",
            serviceClass = OtherOutstockService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated OtherOutstockDTO.UpdateDTO dto) {
        Boolean flag = otherOutstockService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改并提交
     * @author Will
     * @date: 2023/5/10 20:02
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:update",
            serviceClass = OtherOutstockService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated OtherOutstockDTO.UpdateDTO dto) {
        Boolean flag = otherOutstockService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 提交
     * @author Will
     * @date: 2023/5/10 20:00
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:submit",
            serviceClass = OtherOutstockService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = otherOutstockService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 查看详情
     * @author Will
     * @date: 2023/5/10 20:10
     * @param id
     * @return ApiResult
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:view",
            serviceClass = OtherOutstockService.class,
            keyIdName = "id")
    public ApiResult<OtherOutstockDTO.ViewDTO> view(@RequestParam("id") String id) {
        OtherOutstockDTO.ViewDTO dto = otherOutstockService.view(id);
        return success(dto);
    }


    /**
     * 删除
     * @author Will
     * @date: 2023/5/10 20:09
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:delete",
            serviceClass = OtherOutstockService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = otherOutstockService.delete(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 作废
     * @author Will
     * @date: 2023/5/10 20:11
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:invalid",
            serviceClass = OtherOutstockService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = otherOutstockService.invalid(dto.getIds(),dto.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @author Will
     * @date: 2023/5/10 20:11
     * @param baseApproveParamDTO
     * @return ApiResult
     */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:approve",
            serviceClass = OtherOutstockService.class,
            keyIdName = "ids")
    public ApiResult approve(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        otherOutstockService.approve(baseApproveParamDTO);
        return success();
    }

    /**
     * 批量反审核
     * @author Will
     * @date: 2023/5/10 20:12
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:disApprove",
            serviceClass = OtherOutstockService.class,
            keyIdName = "ids")
    public ApiResult disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = otherOutstockService.disApprove(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 取消流程
     * @author Will
     * @date: 2023/5/10 20:24
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:cancelProcess",
            serviceClass = OtherOutstockService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = otherOutstockService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }

    /**
     * 导出
     * @author Will
     * @date: 2023/5/10 20:25
     * @param dto
     * @param response
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "warehouse_keeper_id",
            menuCode = "wms:otherOutstock:paging",
            tableAlias = "oo"
    )
    public ApiResult exportExcel(@RequestBody OtherOutstockDTO.SearchParamDTO dto, HttpServletResponse response) {
        Boolean flag = otherOutstockService.exportExcel(dto, response);
        return flag == true ? success() : failure();
    }
}
