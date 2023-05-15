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
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.server.oms.service.CustomerInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 销售管理-客户管理
 *
 * @author lambda
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/customer")
public class CustomerInfoController extends BaseController {

    @Resource
    private CustomerInfoService customerInfoService;


    /**
     * 获取 tab列表
     *
     * @return
     */
    @GetMapping("/tabList")
    public ApiResult<List<CustomerDTO.TabListDTO>> tabList() {
        List<CustomerDTO.TabListDTO> list = customerInfoService.tabList();
        return success(list);
    }

    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<CustomerDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<CustomerDTO.PagingParamDTO> dto) {
        PagingVO<CustomerDTO.PagingViewDTO> pagingVO = customerInfoService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 新增
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated CustomerDTO.AddDTO dto) {
        String id = customerInfoService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 提交
     *
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = customerInfoService.submit(dto.getIds());
        return result ? success() : failure();
    }

    /**
     * 新增并提交
     *
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated CustomerDTO.AddDTO dto) {
        Boolean result = customerInfoService.addAndSubmit(dto);
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
            menuCode = "scm:supplier:view",
            serviceClass = CustomerInfoService.class,
            keyIdName = "id"
    )
    public ApiResult<CustomerDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        CustomerDTO.ViewDTO view = customerInfoService.view(dto.getId());
        return success(view);
    }

    /**
     * 修改
     *
     * @param
     * @return
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated CustomerDTO.ViewDTO dto) {
        return success();
    }

    /**
     * 修改并提交
     *
     * @param
     * @return
     */
    @PostMapping("/updateAndSubmit")
    public ApiResult updateAndSubmit(@RequestBody @Validated CustomerDTO.ViewDTO dto) {
        return success();
    }

    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        return success();
    }

    /**
     * 反审核
     */
    @PostMapping("/disApprove")
    public ApiResult disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        return success();
    }

    /**
     * 删除仓库
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        return success();
    }

    /**
     * 导出数据
     */
    @PostMapping("/export")
    public ApiResult exportWarehouse(@RequestBody @Valid CustomerDTO.ExportDTO dto, HttpServletResponse response) {

        return success();
    }


}
