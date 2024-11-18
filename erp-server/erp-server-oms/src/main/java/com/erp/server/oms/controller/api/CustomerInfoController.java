package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.CustomerAddressDTO;
import com.erp.model.oms.dto.CustomerB2bSellerChangeDTO;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.server.oms.query.CustomerInfoQueryHandler;
import com.erp.server.oms.service.CustomerAddressService;
import com.erp.server.oms.service.CustomerB2bSellerChangeService;
import com.erp.server.oms.service.CustomerInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 销售管理-客户管理
 *
 * @author lambda
 * @since 2023-05-10
 */
@RestController
@LogSystemModule("B2B客户列表")
@RequestMapping("/customer")
@Slf4j
public class CustomerInfoController extends BaseController {

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private CustomerAddressService customerAddressService;

    @Resource
    private CustomerB2bSellerChangeService customerB2bSellerChangeService;

    /**
     * 获取 tab列表
     *
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customer:paging",
            tableAlias = "ci"
    )
    public ApiResult<List<CustomerDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<CustomerDTO.TabListDTO> list = customerInfoService.tabList(dto);
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
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customer:paging",
            tableAlias = "ci"
    )
    @WebAdvanceQuery(handler = CustomerInfoQueryHandler.class)
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
    @LogAction(value = LogActionEnum.INSERT, desc = "新增客户信息")
    @PostMapping("/add")
    public ApiResult<Object> add(@RequestBody @Validated CustomerDTO.AddDTO dto) {
        String id = customerInfoService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 提交
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交客户信息")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customer:submit",
            serviceClass = CustomerInfoService.class,
            keyIdName = "ids"
    )
    public ApiResult<Object> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = customerInfoService.submit(dto.getIds());
        return Boolean.TRUE.equals(result) ? success() : failure();
    }

    /**
     * 新增并提交
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交客户信息")
    @PostMapping("/addAndSubmit")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated CustomerDTO.AddDTO dto) {
        String result = customerInfoService.addAndSubmit(dto);
        return StringUtils.isNotBlank(result) ? success() : failure();
    }


    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @LogViewService
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改客户信息")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customer:update",
            serviceClass = CustomerInfoService.class,
            keyIdName = "id"
    )
    public ApiResult<Object> update(@RequestBody @Validated CustomerDTO.UpdateDTO dto) {
        String id = customerInfoService.updateCustomer(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改并提交
     *
     * @param
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交客户信息")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customer:update",
            serviceClass = CustomerInfoService.class,
            keyIdName = "id"
    )
    public ApiResult<Object> updateAndSubmit(@RequestBody @Validated CustomerDTO.UpdateDTO dto) {
        Boolean result = customerInfoService.updateAndSubmit(dto);
        return Boolean.TRUE.equals(result) ? success() : failure();
    }

    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核交客户信息")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customer:approve",
            serviceClass = CustomerInfoService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<CustomerInfoEntity> entityList = customerInfoService.listByIds(ids);
        for (String id : ids) {
            CustomerInfoEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"客户信息不存在"));
                continue;
            }
            try {
                resultDTOS.add(customerInfoService.approve(dto, entity));
            }catch (Exception e){
                log.error("B2B客户审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);

    }

    /**
     * 反审核
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核交客户信息")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customer:disApprove",
            serviceClass = CustomerInfoService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<CustomerInfoEntity> entityList = customerInfoService.listByIds(ids);
        for (String id : ids) {
            CustomerInfoEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"客户信息不存在"));
                continue;
            }
            try {
                resultDTOS.add(customerInfoService.disApprove(entity));
            }catch (Exception e){
                log.error("B2B客户反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 撤销流程
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销客户信息")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customer:cancelProcess",
            serviceClass = CustomerInfoService.class,
            keyIdName = "ids")
    public ApiResult<Object> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = customerInfoService.cancelProcess(dto.getIds());
        return Boolean.TRUE.equals(result) ? success() : failure();
    }


    /**
     * 删除客户
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "删除客户信息")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customer:delete",
            serviceClass = CustomerInfoService.class,
            keyIdName = "ids"
    )
    public ApiResult<Object> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = customerInfoService.deleteByIds(dto.getIds());
        return Boolean.TRUE.equals(result) ? success() : failure();
    }

    /**
     * 导出数据
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出客户信息")
    @PostMapping("/export")
    public ApiResult<Object> exportCustomer(@RequestBody @Valid CustomerDTO.ExportDTO dto) {
        Boolean result = customerInfoService.exportExcel(dto);
        return Boolean.TRUE.equals(result) ? success() : failure();
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
        List<CustomerDTO.InfoDTO> resultList = BeanMapper.copyList(list, CustomerDTO.InfoDTO.class);
        return success(resultList);
    }

    /**
     * 远程搜索
     * @param dto
     */
    @PostMapping("/pageSelect")
    public ApiResult<PagingVO<CustomerDTO.PageSelectDTO>> pageSelect(@RequestBody @Validated PagingDTO<CustomerDTO.SelectDTO> dto) {
        dto.setPageSize(100);
        PagingVO<CustomerDTO.PageSelectDTO> pagingVO = customerInfoService.pagingSelect(dto);
        return success(pagingVO);
    }

    /**
     * 远程搜索
     * @param dto
     */
    @PostMapping("/pagingSelect")
    public ApiResult<PagingVO<CustomerDTO.PageSelectDTO>> pagingSelect(@RequestBody @Validated PagingDTO<CustomerDTO.SelectDTO> dto) {
        PagingVO<CustomerDTO.PageSelectDTO> pagingVO = customerInfoService.pagingSelect(dto);
        return success(pagingVO);
    }

    /**
     * 根据名称搜索客户收货信息
     */
    @GetMapping("/listByName")
    public ApiResult<List<CustomerDTO.ReceiveInfoDTO>> listByName(@RequestParam(value = "name",required = false) String name) {
        List<CustomerDTO.ReceiveInfoDTO> list = customerInfoService.listReceiveByName(name);
        return success(list);
    }

    /**
     * 启用的客户列表
     */
    @PostMapping("/listEnable")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customer:paging",
            tableAlias = "customer_info"
    )
    public ApiResult<List<CustomerDTO.InfoDTO>> listEnable(PermissionsDTO dto) {
        List<CustomerDTO.InfoDTO> list = customerInfoService.listEnable(dto.getPermissionSql());
        return success(list);
    }

    /**
     * 启用或者停用客户
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "启用或者停用客户：ids={ids},禁用状态={disabled}(true=禁用;false=启用)")
    @PostMapping("/updateStatus")
    public ApiResult<Object> updateStatus(@RequestBody @Validated UpdateStateDTO.BatchUpdateDTO dto) {
        Boolean result = customerInfoService.updateStatus(dto);
        return Boolean.TRUE.equals(result) ? success() : failure();
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
     *
     * @param customerId customerId
     * @return java.util.List<com.erp.model.oms.dto.CustomerAddressDTO.ViewDTO>
     * @Author Luo_WG
     * @Date 2023/5/18 14:28
     **/
    @GetMapping("/listCustomerAddress")
    public ApiResult<List<CustomerAddressDTO.ViewDTO>> listCustomerAddress(@RequestParam("customerId") String customerId) {
        List<CustomerAddressDTO.ViewDTO> viewDTOS = customerAddressService.listByMainId(customerId);
        return success(viewDTOS);
    }

    /**
     * 根据id查询客户地址
     * @author Will
     * @date: 2023/7/13 12:08
     * @param customerAddressId
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/getCustomerAddressById")
    public ApiResult<CustomerAddressDTO.ViewDTO> getCustomerAddressById(@RequestParam("customerAddressId") String customerAddressId) {
        CustomerAddressDTO.ViewDTO viewDTO = customerAddressService.getCustomerAddressById(customerAddressId);
        return success(viewDTO);
    }

    /**
     * 处理平台的历史数据
     *
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_WITHOUT_PARAMS, desc = "处理平台的历史数据")
    @GetMapping("/processData")
    public ApiResult<Object> processData() {
        Boolean result = customerInfoService.processData();
        return Boolean.TRUE.equals(result) ? success() : failure();
    }

    /**
     * 导入客户
     * @param file
     * @return
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入客户信息")
    @PostMapping(value = "importCustomer")
    public ApiResult<Void> importCustomer(@RequestParam(value = "file") MultipartFile file) throws IOException {
        customerInfoService.importCustomer(file);
        return success();
    }

    /**
     * 导入客户关联金蝶信息
     * @param file
     * @return
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入客户关联金蝶信息")
    @PostMapping(value = "importCustomerKingdee")
    public ApiResult<Void> importCustomerKingdee(@RequestParam(value = "file") MultipartFile file) throws IOException {
        customerInfoService.importCustomerKingdee(file);
        return success();
    }

    /**
     * 保存销售员变更信息
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "保存销售员变更信息")
    @PostMapping(value = "addSellerChange")
    public ApiResult<List<BatchResultDTO>> saveSellerChange(@RequestBody List<CustomerB2bSellerChangeDTO.AddDTO> addDTOList) {
        List<BatchResultDTO> batchResultDTOList = customerB2bSellerChangeService.batchAdd(addDTOList);
        return batchResultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(batchResultDTOList) : failure(batchResultDTOList);
    }


    /**
     * 保存并提交销售员变更信息
     * @return
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "保存并提交销售员变更信息")
    @PostMapping(value = "addAndSubmitSellerChange")
    public ApiResult<List<BatchResultDTO>> addAndSubmitSellerChange(@RequestBody List<CustomerB2bSellerChangeDTO.AddDTO> addDTOList) {
        List<BatchResultDTO> batchResultDTOList = customerB2bSellerChangeService.batchAddAndSubmit(addDTOList);
        return batchResultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(batchResultDTOList) : failure(batchResultDTOList);
    }

    /**
     * 启用的客户列表(简称)
     *
     * @param dto
     * @return java.util.List<com.erp.model.oms.dto.CustomerDTO.InfoDTO>
     * @Author jack
     * @Date 2024-11-06
     **/
    @PostMapping("/listSimpleName")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customer:paging",
            tableAlias = "customer_info"
    )
    public ApiResult<List<CustomerDTO.InfoDTO>> listSimpleName(@RequestBody CustomerDTO.PageSelectDTO dto) {
        List<CustomerDTO.InfoDTO> list = customerInfoService.listSimpleName(dto);
        return success(list);
    }

}
