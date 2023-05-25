package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoChangeDTO;
import com.erp.server.oms.service.SoChangeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 销售管理-销售变更管理
 *
 * @author lambda
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/soChange")
public class SoChangeController extends BaseController {

    @Resource
    private SoChangeService soChangeService;

    /**
     * 获取 tab列表
     *
     * @return
     */
    @GetMapping("/tabList")
    public ApiResult<List<SoChangeDTO.TabListDTO>> tabList() {
        List<SoChangeDTO.TabListDTO> tabList = soChangeService.tabList();
        return success(tabList);
    }


    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "user_id",
            menuCode = "oms:soChange:paging",
            tableAlias = "sc"
    )
    public ApiResult<PagingVO<SoChangeDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<SoChangeDTO.PagingParamDTO> dto) {
        PagingVO<SoChangeDTO.PagingViewDTO> pagingVO = soChangeService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 根据销售订单id
     * 获取到对应的详情
     *
     * @param soId
     * @return
     */
    @GetMapping("/getViewBySoId")
    public ApiResult<SoChangeDTO.ViewDTO> view(@RequestParam("soId") String soId) {
        SoChangeDTO.ViewDTO view = soChangeService.getViewBySoId(soId);
        return success(view);
    }


    /**
     * 创建
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "user_id",
            menuCode = "oms:soChange:add",
            serviceClass = SoChangeService.class,
            keyIdName = "id"
    )
    public ApiResult add(@RequestBody @Validated SoChangeDTO.AddDTO dto) {
        String id = soChangeService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 提交
     *
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "user_id",
            menuCode = "oms:soChange:submit",
            serviceClass = SoChangeService.class,
            keyIdName = "id"
    )
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = soChangeService.submit(dto.getIds());
        return result ? success() : failure();
    }

    /**
     * 新增并提交
     *
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "user_id",
            menuCode = "oms:soChange:submit",
            serviceClass = SoChangeService.class,
            keyIdName = "id"
    )
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated SoChangeDTO.AddDTO dto) {
        Boolean result = soChangeService.addAndSubmit(dto);
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
            tableField = "user_id",
            menuCode = "oms:soChange:submit",
            serviceClass = SoChangeService.class,
            keyIdName = "id"
    )
    public ApiResult<SoChangeDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        SoChangeDTO.ViewDTO view = soChangeService.view(dto.getId());
        return success(view);
    }


    /**
     * 修改
     *
     * @param dto
     * @return
     */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "user_id",
            menuCode = "oms:soChange:update",
            serviceClass = SoChangeService.class,
            keyIdName = "id"
    )
    public ApiResult update(@RequestBody @Validated SoChangeDTO.UpdateDTO dto) {
        String id = soChangeService.updateSoChange(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();

    }

    /**
     * 修改并提交
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "user_id",
            menuCode = "oms:soChange:updateAndSubmit",
            serviceClass = SoChangeService.class,
            keyIdName = "id"
    )
    public ApiResult updateAndSubmit(@RequestBody @Validated SoChangeDTO.UpdateDTO dto) {
        Boolean result = soChangeService.updateAndSubmit(dto);
        return result ? success() : failure();
    }

    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "user_id",
            menuCode = "oms:soChange:approve",
            serviceClass = SoChangeService.class,
            keyIdName = "id"
    )
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        Boolean result = soChangeService.approve(dto);
        return result ? success() : failure();
    }


    /**
     * 撤销流程
     *
     * @param dto
     * @return
     */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "user_id",
            menuCode = "oms:soChange:cancelProcess",
            serviceClass = SoChangeService.class,
            keyIdName = "id"
    )
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = soChangeService.cancelProcess(dto.getIds());
        return result ? success() : failure();
    }


    /**
     * 删除销售变更单
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "user_id",
            menuCode = "oms:soChange:delete",
            serviceClass = SoChangeService.class,
            keyIdName = "id"
    )
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = soChangeService.deleteByIds(dto.getIds());
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
            tableField = "user_id",
            menuCode = "oms:soChange:invalid",
            serviceClass = SoChangeService.class,
            keyIdName = "id"
    )
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean result = soChangeService.invalid(dto.getIds(), dto.getRemark());
        return result ? success() : failure();
    }

    /**
     * 导出
     * 数据
     */
    @PostMapping("/export")
    public ApiResult exportWarehouse(@RequestBody @Valid SoChangeDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean result = soChangeService.exportExcel(dto, response);
        return result ? success() : failure();

    }


}
