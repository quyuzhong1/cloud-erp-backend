package com.erp.server.oms.controller.api;


import com.common.core.controller.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 销售管理-客户管理
 *
 * @author lambda
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/customerB2c")
public class CustomerB2cInfoController extends BaseController {

/*
    @Resource
    private CustomerB2cInfoService customerB2cInfoService;

    @Resource
    private CustomerB2cAddressService customerB2cAddressService;

    */
/**
     * 获取 tab列表
     *
     * @return
     *//*

    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:paging",
            tableAlias = "ci"
    )
    public ApiResult<List<CustomerB2cDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<CustomerB2cDTO.TabListDTO> list = customerB2cInfoService.tabList(dto);
        return success(list);
    }

    */
/**
     * 分页列表
     *
     * @param dto
     * @return
     *//*

    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:paging",
            tableAlias = "ci"
    )
    public ApiResult<PagingVO<CustomerB2cDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<CustomerB2cDTO.PagingParamDTO> dto) {
        PagingVO<CustomerB2cDTO.PagingViewDTO> pagingVO = customerB2cInfoService.paging(dto);
        return success(pagingVO);
    }


    */
/**
     * 新增
     *
     * @param dto
     * @return
     *//*

    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated CustomerB2cDTO.AddDTO dto) {
        String id = customerB2cInfoService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    */
/**
     * 提交
     *
     * @param dto
     * @return
     *//*

    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:submit",
            serviceClass = CustomerB2cInfoService.class,
            keyIdName = "ids"
    )
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = customerB2cInfoService.submit(dto.getIds());
        return result ? success() : failure();
    }

    */
/**
     * 新增并提交
     *
     * @param dto
     * @return
     *//*

    @PostMapping("/addAndSubmit")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated CustomerB2cDTO.AddDTO dto) {
        Boolean result = customerB2cInfoService.addAndSubmit(dto);
        return result ? success() : failure();
    }


    */
/**
     * 详情
     *
     * @param dto
     * @return
     *//*

    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:view",
            serviceClass = CustomerB2cInfoService.class,
            keyIdName = "id"
    )
    public ApiResult<CustomerB2cDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        CustomerB2cDTO.ViewDTO view = customerB2cInfoService.view(dto.getId());
        return success(view);
    }

    */
/**
     * 修改
     *
     * @param
     * @return
     *//*

    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:update",
            serviceClass = CustomerB2cInfoService.class,
            keyIdName = "id"
    )
    public ApiResult update(@RequestBody @Validated CustomerB2cDTO.UpdateDTO dto) {
        String id = customerB2cInfoService.updateCustomerB2c(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    */
/**
     * 修改并提交
     *
     * @param
     * @return
     *//*

    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:update",
            serviceClass = CustomerB2cInfoService.class,
            keyIdName = "id"
    )
    public ApiResult updateAndSubmit(@RequestBody @Validated CustomerB2cDTO.UpdateDTO dto) {
        Boolean result = customerB2cInfoService.updateAndSubmit(dto);
        return result ? success() : failure();
    }

    */
/**
     * 审核
     *
     * @param dto
     * @return
     *//*

    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:approve",
            serviceClass = CustomerB2cInfoService.class,
            keyIdName = "ids"
    )
    public ApiResult audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        Boolean result = customerB2cInfoService.approve(dto);
        return result ? success() : failure();
    }

    */
/**
     * 反审核
     *//*

    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:disApprove",
            serviceClass = CustomerB2cInfoService.class,
            keyIdName = "ids"
    )
    public ApiResult disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = customerB2cInfoService.disApprove(dto.getIds());
        return result ? success() : failure();
    }


    */
/**
     * 撤销流程
     *
     * @param dto
     * @return
     *//*

    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:cancelProcess",
            serviceClass = CustomerB2cInfoService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = customerB2cInfoService.cancelProcess(dto.getIds());
        return result ? success() : failure();
    }


    */
/**
     * 删除客户
     *
     * @param dto
     * @return
     *//*

    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:delete",
            serviceClass = CustomerB2cInfoService.class,
            keyIdName = "ids"
    )
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = customerB2cInfoService.deleteByIds(dto.getIds());
        return result ? success() : failure();
    }

    */
/**
     * 导出数据
     *//*

    @PostMapping("/export")
    public ApiResult exportCustomerB2c(@RequestBody @Valid CustomerB2cDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean result = customerB2cInfoService.exportExcel(dto, response);
        return result ? success() : failure();
    }

    */
/**
     * 客户列表
     *//*

    @GetMapping("/list")
    public ApiResult<List<CustomerB2cDTO.InfoDTO>> list() {
        List<CustomerB2cDTO.InfoDTO> list = customerB2cInfoService.listCustomerB2c();
        return success(list);
    }

    */
/**
     * 所有客户列表 没有任何限制
     *//*

    @GetMapping("/listAll")
    public ApiResult<List<CustomerB2cDTO.InfoDTO>> listAll() {
        List<CustomerB2cInfoEntity> list = customerB2cInfoService.list();
        List<CustomerB2cDTO.InfoDTO> resultList = BeanMapper.copyList(list, CustomerB2cDTO.InfoDTO.class);
        return success(resultList);
    }

    */
/**
     * 启用的客户列表
     *//*

    @PostMapping("/listEnable")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:paging",
            tableAlias = "customerB2c_info"
    )
    public ApiResult<List<CustomerB2cDTO.InfoDTO>> listEnable(PermissionsDTO dto) {
        List<CustomerB2cDTO.InfoDTO> list = customerB2cInfoService.listEnable(dto.getPermissionSql());
        return success(list);
    }

    */
/**
     * 启用或者停用客户
     *
     * @param dto
     * @return
     *//*

    @PostMapping("/updateStatus")
    public ApiResult updateStatus(@RequestBody @Validated UpdateStateDTO.BatchUpdateDTO dto) {
        Boolean result = customerB2cInfoService.updateStatus(dto);
        return result ? success() : failure();
    }

    */
/**
     * 获取客户的基础信息
     *
     * @param
     * @return
     *//*

    @GetMapping("/getBase")
    public ApiResult<CustomerB2cDTO.BaseDTO> getBase(@RequestParam("customerB2cId") String customerB2cId) {
        CustomerB2cDTO.BaseDTO result = customerB2cInfoService.getBase(customerB2cId);
        return success(result);
    }

    */
/**
     * 根据客户id 获取地址信息
     *
     * @param customerB2cId customerB2cId
     * @return java.util.List<com.erp.model.oms.dto.CustomerB2cAddressDTO.ViewDTO>
     * @Author Luo_WG
     * @Date 2023/5/18 14:28
     **//*

    @GetMapping("/listCustomerB2cAddress")
    public ApiResult<List<CustomerB2cAddressDTO.ViewDTO>> listCustomerB2cAddress(@RequestParam("customerB2cId") String customerB2cId) {
        List<CustomerB2cAddressDTO.ViewDTO> viewDTOS = customerB2cAddressService.listByMainId(customerB2cId);
        return success(viewDTOS);
    }

    */
/**
     * 根据id查询客户地址
     * @author Will
     * @date: 2023/7/13 12:08
     * @param customerB2cAddressId
     * @return ApiResult<ViewDTO>
     *//*

    @GetMapping("/getCustomerB2cAddressById")
    public ApiResult<CustomerB2cAddressDTO.ViewDTO> getCustomerB2cAddressById(@RequestParam("customerB2cAddressId") String customerB2cAddressId) {
        CustomerB2cAddressDTO.ViewDTO viewDTO = customerB2cAddressService.getCustomerB2cAddressById(customerB2cAddressId);
        return success(viewDTO);
    }

    */
/**
     * 处理平台的历史数据
     *
     * @return
     *//*

    @GetMapping("/processData")
    public ApiResult processData() {
        Boolean result = customerB2cInfoService.processData();
        return result ? success() : failure();
    }

    */
/**
     * 导入客户
     * @param file
     * @return
     *//*

    @PostMapping(value = "importCustomerB2c")
    public ApiResult<Void> importCustomerB2c(@RequestParam(value = "file") MultipartFile file) throws IOException {
        customerB2cInfoService.importCustomerB2c(file);
        return success();
    }

    */
/**
     * 导入客户关联金蝶信息
     * @param file
     * @return
     *//*

    @PostMapping(value = "importCustomerB2cKingdee")
    public ApiResult<Void> importCustomerB2cKingdee(@RequestParam(value = "file") MultipartFile file) throws IOException {
        customerB2cInfoService.importCustomerB2cKingdee(file);
        return success();
    }
*/


}
