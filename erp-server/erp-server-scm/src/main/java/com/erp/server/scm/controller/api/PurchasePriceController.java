package com.erp.server.scm.controller.api;


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
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.entity.PilotApplicationEntity;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.entity.PurchasePriceChangeDetailEntity;
import com.erp.model.scm.entity.PurchasePriceChangeEntity;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.server.scm.query.PurchasePriceQueryHandler;
import com.erp.server.scm.service.PurchasePriceChangeDetailService;
import com.erp.server.scm.service.PurchasePriceDetailService;
import com.erp.server.scm.service.PurchasePriceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 采购价目管理
 *
 * @author Lambda
 * @since 2023-03-15
 */
@Slf4j
@RestController
@LogSystemModule("采购价目表")
@RequestMapping("/purchase/price")
public class PurchasePriceController extends BaseController {

    @Resource
    private PurchasePriceService purchasePriceService;
    @Resource
    private PurchasePriceDetailService purchasePriceDetailService;
    @Resource
    private PurchasePriceChangeDetailService purchasePriceChangeDetailService;

    /**
     * 采购价目分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:paging",
            tableAlias = "pp")
    @WebAdvanceQuery(handler = PurchasePriceQueryHandler.class)
    public ApiResult<PagingVO<PurchasePriceDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<PurchasePriceDTO.PagingParamDTO> dto) {
        PagingVO<PurchasePriceDTO.PagingViewDTO> pagingVO = purchasePriceService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 添加采购价目表
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "添加采购价目表")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:add",
            serviceClass = PurchasePriceService.class,
            keyIdName = "id")
    public ApiResult<?> add(@RequestBody @Validated PurchasePriceDTO.AddDTO dto) {
        PurchasePriceEntity entity = purchasePriceService.add(dto);
        return null != entity ? success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode())) : failure();
    }


    /**
     * tab列表
     * @author Will
     * @date: 2024/1/20 9:20
     * @param dto
     * @return ApiResult<List<TabListDTO>>
     */
    @PostMapping("/tab/list")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:paging",
            tableAlias = "pp")
    public ApiResult<List<PurchasePriceDTO.TabListDTO>> tabList(PermissionsDTO dto) {
        List<PurchasePriceDTO.TabListDTO> list = purchasePriceService.tabList(dto);
        return success(list);
    }

    /**
     * 提交并审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交采购价目")
    @PostMapping("/addAndSubmit")
    public ApiResult<?> addAndSubmit(@RequestBody @Validated PurchasePriceDTO.AddDTO dto) {
        //新增
        PurchasePriceEntity entity;
        try {
            entity  = purchasePriceService.add(dto);
            if (null == entity) {
                return  failure(ApiError.ERROR_1019.msg, new BaseResultDTO.AddAndSubmmitDTO("","",Boolean.FALSE));
            }
        } catch (ServiceException e) {
            log.error("新增失败，dto: {}", dto, e);
            return failure(e.getMessage(),new BaseResultDTO.AddAndSubmmitDTO("","",Boolean.FALSE));
        } catch (Exception e) {
            log.error("新增失败，dto: {}", dto, e);
            return failure(ApiError.ERROR_1019.msg,new BaseResultDTO.AddAndSubmmitDTO("","",Boolean.FALSE));
        }
        //提审
        try {
            entity = purchasePriceService.getById(entity.getId());
            if (ObjectUtil.isEmpty(entity)) {
                throw new ServiceException(ApiError.NOT_EXIST_BILL,"采购价目");
            }
            purchasePriceService.submitEntity(entity);
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
     * 采购价目详情
     *
     * @param dto
     * @return
     */
    @LogViewService
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:view",
            serviceClass = PurchasePriceService.class,
            keyIdName = "id")
    public ApiResult<PurchasePriceDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        PurchasePriceDTO.ViewDTO view = purchasePriceService.view(dto.getId());
        return success(view);
    }

    /**
     * 修改采购价目
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改采购价目")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:update",
            serviceClass = PurchasePriceService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated PurchasePriceDTO.UpdateDTO dto) {
        PurchasePriceEntity entity = purchasePriceService.updatePurchasePrice(dto);
        return null != entity ? success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode())) : failure();
    }

    /**
     * 修改并审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并审核采购价目")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:update",
            serviceClass = PurchasePriceService.class,
            keyIdName = "id")
    public ApiResult<BaseResultDTO.AddAndSubmmitDTO> updateAndSubmit(@RequestBody @Validated PurchasePriceDTO.UpdateDTO dto) {
        PurchasePriceEntity entity;
        try {
            entity = purchasePriceService.updatePurchasePrice(dto);
            if (null == entity) {
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
            purchasePriceService.submitEntity(entity);
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
     * 删除采购价目
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除采购价目")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:delete",
            serviceClass = PurchasePriceService.class,
            keyIdName = "ids")
    public ApiResult<?> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, PurchasePriceEntity> entityMap = purchasePriceService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PurchasePriceEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id, id,"采购价目不存在"));
                continue;
            }
            try {
                resultDTOS.add(purchasePriceService.deleteEntity(entity));
            }catch (Exception e){
                log.error("采购价目删除失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 采购价目提交审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交采购价目")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:submit",
            serviceClass = PurchasePriceService.class,
            keyIdName = "ids")
    public ApiResult<?> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, PurchasePriceEntity> entityMap = purchasePriceService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PurchasePriceEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id, id,"采购价目不存在"));
                continue;
            }
            try {
                resultDTOS.add(purchasePriceService.submitEntity(entity));
            }catch (Exception e){
                log.error("采购价目提交失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }



    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核采购价目")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:approve",
            serviceClass = PurchasePriceService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PurchasePriceEntity> entityList = purchasePriceService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PurchasePriceEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购价目不存在"));
                continue;
            }
            try {
                resultDTOS.add(purchasePriceService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("采购价目审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 取消流程
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-03-23 17:57
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销采购价目")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:cancelProcess",
            serviceClass = PurchasePriceService.class,
            keyIdName = "ids")
    public ApiResult<?> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, PurchasePriceEntity> entityMap = purchasePriceService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PurchasePriceEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购价目单不存在"));
                continue;
            }
            try {
                resultDTOS.add(purchasePriceService.cancelProcessEntity((entity)));
            }catch (Exception e){
                log.error("采购价目撤销失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 采购价目数据导出
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "采购价目数据导出")
    @PostMapping("/exportPurchasePrice")
    public ApiResult<?> exportPurchasePrice(@RequestBody @Valid PurchasePriceDTO.PagingParamDTO dto) {
        purchasePriceService.exportPurchasePrice(dto);
        return success();
    }

    /**
     * 批量导入
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "采购价目数据批量导入")
    @PostMapping("/import")
    public ApiResult<?> importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = purchasePriceService.importFile(excelFile, response);
        return result ? success() : failure();
    }

    /**
     * 下载模板
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板采购价目")
    @GetMapping("/downloadTemplate")
    public ApiResult<?> downloadTemplate(HttpServletResponse response) {
        purchasePriceService.downloadTemplate(response);
        return success();
    }

    /**
     * 反审核
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核采购价目")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:disApprove",
            serviceClass = PurchasePriceService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PurchasePriceEntity> entityList = purchasePriceService.listByIds(dto.getIds());
        List<PurchasePriceDetailEntity> detailList = purchasePriceDetailService.listDetailByMainIds(dto.getIds());
        List<String> priceDetailIds = detailList.stream().map(PurchasePriceDetailEntity::getId).distinct().collect(Collectors.toList());
        List<PurchasePriceChangeDetailEntity> changeDetailList = purchasePriceChangeDetailService.listByPurchasePriceDetailIds(priceDetailIds);
        for (String id : dto.getIds()) {
            PurchasePriceEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购价目不存在"));
                continue;
            }
            List<PurchasePriceDetailEntity> detailEntityList = detailList.stream().filter(e -> e.getPurchasePriceId().equals(id)).collect(Collectors.toList());
            List<String> priceDetailList = detailEntityList.stream().map(PurchasePriceDetailEntity::getId).distinct().collect(Collectors.toList());
            List<PurchasePriceChangeDetailEntity> changeDetailEntityList = changeDetailList.stream().filter(e -> priceDetailList.contains(e.getPurchasePriceDetailId())).collect(Collectors.toList());
            try {
                resultDTOS.add(purchasePriceService.disApprove(entity,detailEntityList,changeDetailEntityList));
            }catch (Exception e){
                log.error("采购价目反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 更新明细备注
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "更新明细备注采购价目:明细备注={remark}")
    @PostMapping("/updateDetailRemark")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:updateDetailRemark",
            serviceClass = PurchasePriceService.class,
            keyIdName = "ids"
    )
    public ApiResult<?> updateDetailRemark(@RequestBody @Valid BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PurchasePriceDetailEntity> detailList = purchasePriceDetailService.listByIds(dto.getIds());
        Map<String, PurchasePriceDetailEntity> detailMap = detailList.stream().collect(Collectors.toMap(BaseEntity::getId, e -> e));
        List<String> mainIds = detailList.stream().map(PurchasePriceDetailEntity::getPurchasePriceId).distinct().collect(Collectors.toList());
        Map<String, PurchasePriceEntity> entityMap = purchasePriceService.mapByIds(mainIds);
        for (String id : dto.getIds()) {
            PurchasePriceDetailEntity detailEntity = detailMap.get(id);
            if(Objects.isNull(detailEntity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购明细不存在"));
                continue;
            }
            PurchasePriceEntity entity = entityMap.get(detailEntity.getPurchasePriceId());
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购价目不存在"));
                continue;
            }
            try {
                Boolean disabled = purchasePriceService.updateDetailRemark(Collections.singletonList(id), dto.getRemark());
                if (disabled){
                    resultDTOS.add(BatchResultDTO.success(id, entity.getCode(), "更新采购价目明细备注失败"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, entity.getCode(), "更新采购价目明细备注成功"));
                }
            }catch (Exception e){
                log.error("更新采购采购价目明细备注失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 更新外部平台单号
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "采购价目:更新外部平台单号:ids={ids},外部平台单号={voucherNo}")
    @PostMapping("/updateOutPlatformCode")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id",
            menuCode = "scm:purchase:price:updateOutPlatformCode",
            serviceClass = PurchasePriceService.class,
            keyIdName = "ids"
    )
    public ApiResult<?> updateOutPlatformCode(@RequestBody @Valid PurchasePriceDTO.OutPlatformCodeDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, PurchasePriceEntity> entityMap = purchasePriceService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PurchasePriceEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购价目单不存在"));
                continue;
            }
            try {
                resultDTOS.add(purchasePriceService.updateOutPlatformCode(entity, dto.getVoucherNo()));
            }catch (Exception e){
                log.error("采购价目更新外部平台单号失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量获取列表采购单价
     * @param list
     * @return
     */
    @PostMapping("/batchGetPurchasePrice")
    public ApiResult<List<PurchasePriceDTO.PriceDTO>> batchGetPurchasePrice(@RequestBody List<PurchasePriceDTO.PriceDTO> list) {
        return success(purchasePriceService.batchGetPurchasePrice(list));
    }
}
