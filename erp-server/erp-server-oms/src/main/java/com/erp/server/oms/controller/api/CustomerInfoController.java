package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.CustomerAddressDTO;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.server.oms.service.CustomerAddressService;
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

    @Resource
    private CustomerAddressService customerAddressService;

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
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:customer:paging",
            tableAlias = "ci"
    )
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:customer:add",
            serviceClass = CustomerInfoService.class,
            keyIdName = "id"
    )
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:customer:submit",
            serviceClass = CustomerInfoService.class,
            keyIdName = "ids"
    )
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:customer:addAndSubmit",
            serviceClass = CustomerInfoService.class,
            keyIdName = "id"
    )
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
            menuCode = "oms:customer:view",
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
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:customer:update",
            serviceClass = CustomerInfoService.class,
            keyIdName = "id"
    )
    public ApiResult update(@RequestBody @Validated CustomerDTO.UpdateDTO dto) {
        String id = customerInfoService.updateCustomer(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改并提交
     *
     * @param
     * @return
     */
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:customer:update",
            serviceClass = CustomerInfoService.class,
            keyIdName = "id"
    )
    public ApiResult updateAndSubmit(@RequestBody @Validated CustomerDTO.UpdateDTO dto) {
        Boolean result = customerInfoService.updateAndSubmit(dto);
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
            tableField = "create_user_id",
            menuCode = "oms:customer:approve",
            serviceClass = CustomerInfoService.class,
            keyIdName = "ids"
    )
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        Boolean result = customerInfoService.approve(dto);
        return result ? success() : failure();
    }

    /**
     * 反审核
     */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:customer:disApprove",
            serviceClass = CustomerInfoService.class,
            keyIdName = "ids"
    )
    public ApiResult disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = customerInfoService.disApprove(dto.getIds());
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
            tableField = "create_user_id",
            menuCode = "oms:customer:cancelProcess",
            serviceClass = CustomerInfoService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = customerInfoService.cancelProcess(dto.getIds());
        return result ? success() : failure();
    }


    /**
     * 删除客户
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "oms:customer:delete",
            serviceClass = CustomerInfoService.class,
            keyIdName = "ids"
    )
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = customerInfoService.deleteByIds(dto.getIds());
        return result ? success() : failure();
    }

    /**
     * 导出数据
     */
    @PostMapping("/export")
    public ApiResult exportCustomer(@RequestBody @Valid CustomerDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean result = customerInfoService.exportExcel(dto, response);
        return result ? success() : failure();
    }

    /**
     * 客户列表
     */
    @GetMapping("/list")
    public ApiResult<List<CustomerDTO.InfoDTO>> list() {
        List<CustomerDTO.InfoDTO> list = customerInfoService.listCustomer();
        return success(list);
    }

    /**
     * 所有客户列表 没有任何限制
     */
    @GetMapping("/listAll")
    public ApiResult<List<CustomerDTO.InfoDTO>> listAll() {
        List<CustomerInfoEntity> list = customerInfoService.list();
        List<CustomerDTO.InfoDTO> resultList=  BeanMapper.copyList(list,CustomerDTO.InfoDTO.class);
        return success(resultList);
    }

    /**
     * 启用的客户列表
     */
    @GetMapping("/listEnable")
    public ApiResult<List<CustomerDTO.InfoDTO>> listEnable() {
        List<CustomerDTO.InfoDTO> list = customerInfoService.listEnable();
        return success(list);
    }

    /**
     * 启用或者停用客户
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateStatus")
    public ApiResult updateStatus(@RequestBody @Validated UpdateStateDTO.BatchUpdateDTO dto) {
        Boolean result = customerInfoService.updateStatus(dto);
        return result ? success() : failure();
    }

    /**
     * 获取客户的基础信息
     *
     * @param
     * @return
     */
    @GetMapping("/getBase")
    public ApiResult<CustomerDTO.BaseDTO> getBase(@RequestParam("customerId") String customerId) {
        CustomerDTO.BaseDTO result = customerInfoService.getBase(customerId);
        return success(result);
    }

    /**
     * 根据客户id 获取地址信息
     * @Author Luo_WG
     * @Date 2023/5/18 14:28
     * @param customerId customerId
     * @return java.util.List<com.erp.model.oms.dto.CustomerAddressDTO.ViewDTO>
     **/
    @GetMapping("/listCustomerAddress")
    public ApiResult<List<CustomerAddressDTO.ViewDTO>> listCustomerAddress(@RequestParam("customerId") String customerId) {
        List<CustomerAddressDTO.ViewDTO> viewDTOS = customerAddressService.listByMainId(customerId);
        return success(viewDTOS);
    }


}
