package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.utils.BeanMapper;
import com.erp.model.oms.dto.CustomerAddressDTO;
import com.erp.model.oms.dto.CustomerB2CDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.CustomerB2cEntity;
import com.erp.server.oms.query.CustomerB2cQueryHandler;
import com.erp.server.oms.service.CustomerB2cAddressService;
import com.erp.server.oms.service.CustomerB2cService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * B2C销售管理-B2C客户管理
 *
 * @author lambda
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/customerB2c")
@LogSystemModule("B2C销售管理-B2C客户管理")
@Slf4j
public class CustomerB2cController extends BaseController {

    @Resource
    private CustomerB2cService customerB2cService;

    @Resource
    private CustomerB2cAddressService customerB2cAddressService;

    /**
     * 获取 tab列表
     *
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:paging",
            tableAlias = "ci"
    )
    public ApiResult<List<CustomerB2CDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<CustomerB2CDTO.TabListDTO> list = customerB2cService.tabList(dto);
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
            menuCode = "oms:customerB2c:paging",
            tableAlias = "ci"
    )
    @WebAdvanceQuery(handler = CustomerB2cQueryHandler.class)
    public ApiResult<PagingVO<CustomerB2CDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<CustomerB2CDTO.PagingParamDTO> dto) {
        PagingVO<CustomerB2CDTO.PagingViewDTO> pagingVO = customerB2cService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 新增
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "添加客户信息")
    public ApiResult<Object> add(@RequestBody @Validated CustomerB2CDTO.AddDTO dto) {
        String id = customerB2cService.add(dto);
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
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:submit",
            serviceClass = CustomerB2cService.class,
            keyIdName = "ids"
    )
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交审核")
    public ApiResult<Object> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = customerB2cService.submit(dto.getIds());
        return Boolean.TRUE.equals(result) ? success() : failure();
    }

    /**
     * 新增并提交
     *
     * @param dto
     * @return
     */
    @PostMapping("/addAndSubmit")
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交审核")
    public ApiResult<Void> addAndSubmit(@RequestBody @Validated CustomerB2CDTO.AddDTO dto) {
        String id = customerB2cService.addAndSubmit(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }


    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:view",
            serviceClass = CustomerB2cService.class,
            keyIdName = "id"
    )
    public ApiResult<CustomerB2CDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        CustomerB2CDTO.ViewDTO view = customerB2cService.view(dto.getId());
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
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:update",
            serviceClass = CustomerB2cService.class,
            keyIdName = "id"
    )
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改客户信息")
    public ApiResult<Object> update(@RequestBody @Validated CustomerB2CDTO.UpdateDTO dto) {
        String id = customerB2cService.updateCustomer(dto);
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
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:update",
            serviceClass = CustomerB2cService.class,
            keyIdName = "id"
    )
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交")
    public ApiResult<Object> updateAndSubmit(@RequestBody @Validated CustomerB2CDTO.UpdateDTO dto) {
        Boolean result = customerB2cService.updateAndSubmit(dto);
        return Boolean.TRUE.equals(result) ? success() : failure();
    }

    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:approve",
            serviceClass = CustomerB2cService.class,
            keyIdName = "ids"
    )
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<CustomerB2cEntity> entityList = customerB2cService.listByIds(ids);
        for (String id : ids) {
            CustomerB2cEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"B2C客户不存在"));
                continue;
            }
            try {
                resultDTOS.add(customerB2cService.approve(dto, entity));
            }catch (Exception e){
                log.error("B2C客户审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 反审核
     */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:disApprove",
            serviceClass = CustomerB2cService.class,
            keyIdName = "ids"
    )
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核")
    public ApiResult<Object> disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<CustomerB2cEntity> entityList = customerB2cService.listByIds(ids);
        for (String id : ids) {
            CustomerB2cEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"B2C客户不存在"));
                continue;
            }
            try {
                resultDTOS.add(customerB2cService.disApprove(entity));
            }catch (Exception e){
                log.error("B2C客户反审核失败",e);
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
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:cancelProcess",
            serviceClass = CustomerB2cService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销流程")
    public ApiResult<Object> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = customerB2cService.cancelProcess(dto.getIds());
        return Boolean.TRUE.equals(result) ? success() : failure();
    }


    /**
     * 删除客户
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:delete",
            serviceClass = CustomerB2cService.class,
            keyIdName = "ids"
    )
    @LogAction(value = LogActionEnum.DELETE, desc = "删除客户")
    public ApiResult<Object> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean result = customerB2cService.deleteByIds(dto.getIds());
        return Boolean.TRUE.equals(result) ? success() : failure();
    }

    /**
     * 导出数据
     */
    @PostMapping("/export")
    @WebAdvanceQuery(handler = CustomerB2cQueryHandler.class)
    @LogAction(value = LogActionEnum.EXPORT, desc = "客户列表导出")
    public ApiResult<Object> exportCustomerB2c(@RequestBody @Valid CustomerB2CDTO.ExportDTO dto, HttpServletResponse response) {
        Boolean result = customerB2cService.exportExcel(dto, response);
        return Boolean.TRUE.equals(result) ? success() : failure();
    }

    /**
     * 客户列表
     */
    @GetMapping("/list")
    public ApiResult<List<CustomerB2CDTO.InfoDTO>> list() {
        List<CustomerB2CDTO.InfoDTO> list = customerB2cService.listCustomer();
        return success(list);
    }

    /**
     * 客户列表远程搜索
     */
    @PostMapping("/pagingSelect")
    public ApiResult<PagingVO<CustomerB2CDTO.InfoDTO>> pagingSelect(@RequestBody @Valid PagingDTO<CustomerB2CDTO.SelectDTO> searchDTO) {
        PagingVO<CustomerB2CDTO.InfoDTO> list = customerB2cService.pagingSelect(searchDTO);
        return success(list);
    }
    /**
     * 所有客户列表 没有任何限制
     */
    @GetMapping("/listAll")
    public ApiResult<List<CustomerB2CDTO.InfoDTO>> listAll() {
        List<CustomerB2cEntity> list = customerB2cService.list();
        List<CustomerB2CDTO.InfoDTO> resultList = BeanMapper.copyList(list, CustomerB2CDTO.InfoDTO.class);
        return success(resultList);
    }

    /**
     * 启用的客户列表
     */
    @PostMapping("/listEnable")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,seller_id",
            menuCode = "oms:customerB2c:paging",
            tableAlias = "customer_b2c"
    )
    public ApiResult<List<CustomerB2CDTO.InfoDTO>> listEnable(PermissionsDTO dto) {
        List<CustomerB2CDTO.InfoDTO> list = customerB2cService.listEnable(dto.getPermissionSql());
        return success(list);
    }

    /**
     * 启用或者停用客户
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateStatus")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "启用或者停用客户 ids={ids},状态值={disabled}(true=禁用,false=启用)")
    public ApiResult<Object> updateStatus(@RequestBody @Validated UpdateStateDTO.BatchUpdateDTO dto) {
        Boolean result = customerB2cService.updateStatus(dto);
        return Boolean.TRUE.equals(result) ? success() : failure();
    }

    /**
     * 获取客户的基础信息
     *
     * @param
     * @return
     */
    @GetMapping("/getBase")
    public ApiResult<CustomerB2CDTO.BaseDTO> getBase(@RequestParam("customerId") String customerId) {
        CustomerB2CDTO.BaseDTO result = customerB2cService.getBase(customerId);
        return success(result);
    }

    /**
     * 根据客户id 获取地址信息
     *
     * @param customerId customerId
     * @return java.util.List<com.erp.model.oms.dto.CustomerB2cAddressDTO.ViewDTO>
     * @Author Luo_WG
     * @Date 2023/5/18 14:28
     **/
    @GetMapping("/listCustomerAddress")
    public ApiResult<List<CustomerAddressDTO.ViewDTO>> listCustomerAddress(@RequestParam("customerId") String customerId) {
        List<CustomerAddressDTO.ViewDTO> viewDTOS = customerB2cAddressService.listByMainId(customerId);
        return success(viewDTOS);
    }

    /**
     * 根据id查询客户地址
     *
     * @param customerAddressId
     * @return ApiResult<ViewDTO>
     * @author Will
     * @date: 2023/7/13 12:08
     */
    @GetMapping("/getCustomerAddressById")
    public ApiResult<CustomerAddressDTO.ViewDTO> getCustomerAddressById(@RequestParam("customerAddressId") String customerAddressId) {
        CustomerAddressDTO.ViewDTO viewDTO = customerB2cAddressService.getCustomerAddressById(customerAddressId);
        return success(viewDTO);
    }

    /**
     * 处理平台的历史数据
     *
     * @return
     */
    @GetMapping("/processData")
    public ApiResult<Object> processData() {
        Boolean result = customerB2cService.processData();
        return Boolean.TRUE.equals(result) ? success() : failure();
    }

    /**
     * 导入客户
     *
     * @param file
     * @return
     */
    @PostMapping(value = "importCustomer")
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入客户")
    public ApiResult<Void> importCustomer(@RequestParam(value = "file") MultipartFile file) throws IOException {
        customerB2cService.importCustomer(file);
        return success();
    }


    /**
     * 根据客户id查询买家信息
     * @author Will
     * @date: 2023/9/5 19:12
     * @param dto
     * @return ApiResult<ViewReceiveDataDTO>
     */
    @PostMapping("/viewReceiveData")
    public ApiResult<SoB2cDTO.ViewReceiveDataDTO> viewReceiveData(@RequestBody @Validated BaseIdDTO dto) {
        return success(customerB2cService.viewReceiveData(dto.getId()));
    }

    /**
     * b2c客户下拉 支持搜索
     * @param pagingDTO
     * @return
     */
    @PostMapping("/drop/down")
    public ApiResult<CustomerB2CDTO.DropPagingDTO<CustomerB2CDTO.DropListDTO>> customerDropDown(@RequestBody PagingDTO<CustomerB2CDTO.DropSearchDTO> pagingDTO) {
        return success(customerB2cService.customerDropDown(pagingDTO));
    }

}
