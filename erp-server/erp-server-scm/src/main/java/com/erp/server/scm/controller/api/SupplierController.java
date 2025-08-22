package com.erp.server.scm.controller.api;


import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
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
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.PurchaseOrderSupplierDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.dto.SupplierTabCountDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.wms.dto.SupplierCountDTO;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.scm.mapper.PurchaseOrderDetailMapper;
import com.erp.server.scm.query.SupplierQueryHandler;
import com.erp.server.scm.service.PurchaseOrderSupplierService;
import com.erp.server.scm.service.SupplierContactService;
import com.erp.server.scm.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 供应商管理
 *
 * @author yl
 * @since 2023-03-15
 */
@Slf4j
@RestController
@LogSystemModule("供应商列表")
@RequestMapping("/supplier")
public class SupplierController extends BaseController {

    @Resource
    private WmsTaskFeign wmsTaskFeign;
    @Resource
    private SupplierService supplierService;

    @Resource
    private PurchaseOrderDetailMapper purchaseOrderDetailMapper;
    @Resource
    private PurchaseOrderSupplierService purchaseOrderSupplierService;

    @Resource
    private SupplierContactService supplierContactService;

    /**
     * 供应商分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:paging",
            tableAlias = "supplier"
    )
    @WebAdvanceQuery(handler = SupplierQueryHandler.class)
    public ApiResult<PagingVO<SupplierDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<SupplierDTO.PagingParamDTO> dto) {
        PagingVO<SupplierDTO.PagingViewDTO> pagingVO = supplierService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 获取供应商列表统计
     *
     * @return
     */
    @GetMapping("/getTabCount")
    public ApiResult<List<SupplierTabCountDTO>> getTabCount(){
        List<SupplierTabCountDTO> dtos = supplierService.getTabCount();
        return success(dtos);
    }

    /**
     * 添加供应商
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "添加供应商")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SupplierDTO.AddDTO dto) {
        SupplierEntity supplierEntity = supplierService.addSupplier(dto);
        return Objects.nonNull(supplierEntity) ? success() : failure();
    }


    /**
     * 提交并审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交供应商")
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated SupplierDTO.AddDTO dto) {
        SupplierEntity entity;
        try {
            entity = supplierService.addSupplier(dto);
            if (Objects.isNull(entity)) {
                return  failure(ApiError.ERROR_1019.msg, new BaseResultDTO.AddAndSubmmitDTO("","",Boolean.FALSE));
            }
        } catch (ServiceException e) {
            log.error("新增失败，dto: {}", dto, e);
            return failure(e.getMessage(),new BaseResultDTO.AddAndSubmmitDTO("","",Boolean.FALSE));
        } catch (Exception e) {
            log.error("新增失败，dto: {}", dto, e);
            return  failure(ApiError.ERROR_1019.msg, new BaseResultDTO.AddAndSubmmitDTO("","",Boolean.FALSE));
        }

        try {
            entity = supplierService.getById(entity.getId());
            if (ObjectUtil.isEmpty(entity)) {
                throw new ServiceException(ApiError.NOT_EXIST_BILL,"供应商");
            }
            supplierService.submit(entity);
        } catch (ServiceException e) {
            log.error("提交审批失败，ID: {}", entity.getId(), e);
            return failure(e.getMessage(),new BaseResultDTO.AddAndSubmmitDTO(entity.getId(),entity.getCode(),Boolean.TRUE));
        } catch (Exception e) {
            log.error("提交审批失败，ID: {}", entity.getId(), e);
            return failure(ApiError.RETRY_SUBMIT_ERROR.msg,new BaseResultDTO.AddAndSubmmitDTO(entity.getId(),entity.getCode(),Boolean.TRUE));
        }

        return success(new BaseResultDTO.AddAndSubmmitDTO(entity.getId(), entity.getCode(),Boolean.TRUE));
    }

    /**
     * 取消流程
     * @author Will
     * @date: 2023/12/01 13:59
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销供应商信息")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:cancelProcess",
            serviceClass = SupplierService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = supplierService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }


    /**
     * 修改供应商
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "提交并审核供应商")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:update",
            serviceClass = SupplierService.class,
            keyIdName = "id"
    )
    public ApiResult update(@RequestBody @Validated SupplierDTO.UpdateDTO dto) {
        //增加校验
        SupplierEntity supplier = supplierService.getById(dto.getId());
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        String msg = "请求成功！";
        if (Objects.nonNull(dto.getSrmDisabled()) && !supplier.getSrmDisabled().equals(dto.getSrmDisabled()) && !dto.getSrmDisabled()){
            //启用时 检查当前周期确认订单是否存在，存在则下月生效
            SupplierCountDTO countDTO = wmsTaskFeign.countOrderBySupplierId(dto.getId());
            if (Objects.nonNull(countDTO) && Objects.nonNull(countDTO.getLocalDate()) && countDTO.getCount() > 0){
                msg = "SRM协同开启后，下月生效";
            }
        }
        String supplierId = supplierService.updateSupplier(dto);
        return StringUtils.isNotBlank(supplierId) ? success(msg, supplierId) : failure();
    }

    /**
     * 修改并审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并审核供应商")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:update",
            serviceClass = SupplierService.class,
            keyIdName = "id"
    )
    public ApiResult<BaseResultDTO.AddAndSubmmitDTO> updateAndSubmit(@RequestBody @Validated SupplierDTO.UpdateDTO dto) {
        //增加校验
        SupplierEntity supplier = supplierService.getById(dto.getId());
        if (Objects.isNull(supplier)) {
            throw new ServiceException(ApiError.ERROR_SUPPLIER_ABSENCE);
        }
        String msg = "请求成功！";
        if (Objects.nonNull(dto.getSrmDisabled()) && !supplier.getSrmDisabled().equals(dto.getSrmDisabled()) && !dto.getSrmDisabled()){
            //启用时 检查当前周期确认订单是否存在，存在则下月生效
            SupplierCountDTO countDTO = wmsTaskFeign.countOrderBySupplierId(dto.getId());
            if (Objects.nonNull(countDTO) && Objects.nonNull(countDTO.getLocalDate()) && countDTO.getCount() > 0){
                msg = "SRM协同开启后，下月生效";
            }
        }

        String supplierId="";
        try {
            supplierId = supplierService.updateSupplier(dto);
            if (StringUtils.isBlank(supplierId)) {
                return failure(ApiError.ERROR_1020.msg,new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.FALSE));
            }
        } catch (ServiceException e) {
            log.error("更新失败，dto: {}", dto, e);
            return failure(e.getMessage(), new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.FALSE));
        } catch (Exception e) {
            log.error("更新失败，dto: {}", dto, e);
            return failure(ApiError.ERROR_1020.msg,new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.FALSE));
        }
        //提审
        try {
            SupplierEntity entity = supplierService.getById(supplierId);
            if (ObjUtil.isEmpty(entity)) {
                throw new ServiceException(ApiError.ERROR_98031);
            }
            supplierService.submit(entity);
        } catch (ServiceException e) {
            log.error("提交审批失败，ID: {}", dto.getId(), e);
            return failure( e.getMessage(),new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.TRUE));
        } catch (Exception e) {
            log.error("提交审批失败，ID: {}", dto.getId(), e);
            return failure(ApiError.RETRY_SUBMIT_ERROR.msg,new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.TRUE));
        }
        return success(new BaseResultDTO.AddAndSubmmitDTO(dto.getId(),"",Boolean.TRUE));
    }


    /**
     * 供应商详情
     *
     * @param dto
     * @return
     */
    @LogViewService
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:view",
            serviceClass = SupplierService.class,
            keyIdName = "id"
    )
    public ApiResult<SupplierDTO.SupplierViewDTO> view(@RequestBody @Validated SupplierDTO.ViewParamDTO dto) {
        SupplierDTO.SupplierViewDTO view = supplierService.view(dto.getId(),dto.getIsViewTel());
        return success(view);
    }


    /**
     * 删除供应商
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除供应商")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:delete",
            serviceClass = SupplierService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOList = supplierService.deleteByIds(dto.getIds());
        return resultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOList) : failure(resultDTOList);
    }


    /**
     * 供应商提交审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "供应商提交审核")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:submit",
            serviceClass = SupplierService.class,
            keyIdName = "ids"
    )
    public ApiResult submit(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, SupplierEntity> entityMap = supplierService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SupplierEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"供应商不存在"));
                continue;
            }
            try {
                resultDTOS.add(supplierService.submit(entity));
            }catch (Exception e){
                log.error("采购订单提交失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 启用供应商
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "启用供应商:id={id},状态值={state}(true=禁用,false=启用)")
    @PostMapping("/updateStatus")
    public ApiResult updateStatus(@RequestBody @Validated UpdateStateDTO dto) {
        Boolean result = supplierService.updateStatus(dto);
        return result == true ? success() : failure();
    }

    /**
     * 启用SRM协同
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "启用SRM协同:id={id},状态值={state}(true=否,false=是)")
    @PostMapping("/updateSrmStatus")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:update",
            serviceClass = SupplierService.class,
            keyIdName = "id"
    )
    public ApiResult updateSrmStatus(@RequestBody @Validated UpdateStateDTO dto) {
        return supplierService.updateSrmStatus(dto);
    }
    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核供应商")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:approve",
            serviceClass = SupplierService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SupplierEntity> entityList = supplierService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SupplierEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"供应商不存在"));
                continue;
            }
            try {
                resultDTOS.add(supplierService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("产品sku审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 反审核
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-03-22 11:56
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核供应商")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:disApprove",
            serviceClass = SupplierService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SupplierEntity> entityList = supplierService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SupplierEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"供应商不存在"));
                continue;
            }
            try {
                resultDTOS.add(supplierService.disApprove(entity));
            }catch (Exception e){
                log.error("产品sku审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     *
     * @author will
     * @date 2025/7/22 20:09
     * @param excelFile
     * @param type allUpdate,partUpdate
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入供应商")
    @PostMapping("/import")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile,@RequestParam(value = "type") String type, HttpServletResponse response) {
        Boolean result = supplierService.importFile(excelFile,type, response);
        return result == true ? success() : failure();
    }

    /**
     * 供应商导出
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出供应商")
    @PostMapping("/exportSupplier")
    public ApiResult exportSupplier(@RequestBody @Valid SupplierDTO.PagingParamDTO dto) {
        supplierService.exportSupplier(dto);
        return success();
    }

    /**
     * 下载模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板供应商")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        supplierService.downloadTemplate(response);
        return success();
    }


    /**
     * 供应商采购记录
     * 分页
     */
    @PostMapping("/purchasePaging")
    public ApiResult<PagingVO<PurchaseOrderSupplierDTO.SupplierPurchaseDTO>> purchasePaging(@RequestBody @Validated PagingDTO<BaseIdDTO> dto) {
        PagingVO<PurchaseOrderSupplierDTO.SupplierPurchaseDTO> pagingVO = purchaseOrderSupplierService.supplierPurchasePaging(dto);
        return success(pagingVO);
    }


    /**
     * 获取供应商的信息
     *
     * @return
     */
    @GetMapping("/getSupplierInfo")
    public ApiResult<SupplierDTO.ViewDTO> getSupplierContact(@RequestParam(value = "supplierId") String supplierId) {
        SupplierDTO.ViewDTO viewDTO = supplierService.getBySupplierId(supplierId);
        return success(viewDTO);
    }


    /**
     * 根据供应商类型 获取到 对应供应商
     * logistics 物流供应商
     * other 货代供应商
     *
     * @return
     */
    @GetMapping("/listSupplierByCategoryType")
    public ApiResult<List<BaseIdDTO>> listSupplierByCategoryType(@RequestParam("categoryType") String categoryType) {
        List<BaseIdDTO> list = supplierService.listSupplierByCategoryType(categoryType);
        return success(list);
    }

    /**
     * 根据供应商类型 获取到已审核的对应供应商
     * 未审核通过的会置为禁用
     *
     * @return
     */
    @GetMapping("/listApproveSupplierByCategoryType")
    public ApiResult<List<SupplierDTO.SupplierSimpleDTO>> listApproveSupplierByCategoryType(@RequestParam("categoryType") String categoryType) {
        return success(supplierService.listApproveSupplierByCategoryType(categoryType));
    }


    /**
     * 批量修改字段
     * @author will
     * @date 2025/8/12 10:50
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "批量修改字段")
    @PostMapping("/updateField")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id",
            menuCode = "scm:supplier:update",
            serviceClass = SupplierService.class,
            keyIdName = "id"
    )
    public ApiResult updateField(@RequestBody @Validated SupplierDTO.BatchUpdateFieldDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SupplierEntity> entityList = supplierService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SupplierEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"供应商不存在"));
                continue;
            }
            try {
                resultDTOS.add(supplierService.updateField(id,dto));
            }catch (Exception e){
                log.error("产品sku审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 更新外部平台单号
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "更新供应商外部平台编号:ids={ids},外部平台单号={outPlatformCode}")
    @PostMapping("/updateVoucherNo")
    public ApiResult updateVoucherNo(@RequestBody @Valid SupplierDTO.VoucherNoDTO dto) {
        Boolean result = supplierService.updateVoucherNo(dto.getIds(),dto.getVoucherNo());
        return result ? success() : failure();
    }

    /**
     * 获取电话号码
     * @author will
     * @date 2025/7/22 15:58
     * @param contactId
     * @return ApiResult<String>
     */
    @GetMapping("/getTelNumber")
    public ApiResult<String> getTelNumber(@RequestParam("contractId") String contactId) {
        return success(supplierContactService.getTelNumber(contactId));
    }

}
