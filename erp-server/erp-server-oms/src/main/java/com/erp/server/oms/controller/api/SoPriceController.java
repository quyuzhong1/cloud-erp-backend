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
import com.common.core.entity.BaseEntity;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.SoPriceDTO;
import com.erp.model.oms.entity.SoPriceChangeDetailEntity;
import com.erp.model.oms.entity.SoPriceDetailEntity;
import com.erp.model.oms.entity.SoPriceEntity;
import com.erp.server.oms.query.SoPriceQueryHandler;
import com.erp.server.oms.service.SoPriceChangeDetailService;
import com.erp.server.oms.service.SoPriceDetailService;
import com.erp.server.oms.service.SoPriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 销售价目表
 *
 * @author will
 * @since 2025-03-24
 */
@Slf4j
@RestController
@LogSystemModule("销售价目表")
@RequestMapping("/soPrice")
public class SoPriceController extends BaseController {

    @Resource
    private SoPriceService soPriceService;

    @Resource
    private SoPriceDetailService soPriceDetailService;


    @Resource
    private SoPriceChangeDetailService soPriceChangeDetailService;

    /**
     * 销售价目分页列表
     *
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "pricing_user_id,create_user_id",
            menuCode = "oms:soPrice:paging",
            tableAlias = "sp,sp")
    @WebAdvanceQuery(handler = SoPriceQueryHandler.class)
    public ApiResult<PagingVO<SoPriceDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<SoPriceDTO.PagingParamDTO> dto) {
        PagingVO<SoPriceDTO.PagingViewDTO> pagingVO = soPriceService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 添加销售价目表
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "添加销售价目表")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id,create_user_id",
            menuCode = "oms:soPrice:add",
            serviceClass = SoPriceService.class,
            keyIdName = "id")
    public ApiResult<?> add(@RequestBody @Validated SoPriceDTO.AddDTO dto) {
        SoPriceEntity entity = soPriceService.add(dto);
        return null != entity ? success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode())) : failure();
    }


    /**
     * tab列表
     * @author Will
     * @date: 2025/03/24 9:20
     * @param dto
     * @return ApiResult<List<TabListDTO>>
     */
    @PostMapping("/tab/list")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "pricing_user_id,create_user_id",
            menuCode = "oms:soPrice:paging",
            tableAlias = "sp,sp")
    public ApiResult<List<SoPriceDTO.TabListDTO>> tabList(PermissionsDTO dto) {
        List<SoPriceDTO.TabListDTO> list = soPriceService.tabList(dto);
        return success(list);
    }

    /**
     * 提交并审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交销售价目")
    @PostMapping("/addAndSubmit")
    public ApiResult<?> addAndSubmit(@RequestBody @Validated SoPriceDTO.AddDTO dto) {
        SoPriceEntity entity = soPriceService.addAndSubmit(dto);
        return null != entity ? success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode())) : failure();
    }

    /**
     * 销售价目详情
     *
     * @param dto
     * @return
     */
    @LogViewService
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id,create_user_id",
            menuCode = "oms:soPrice:view",
            serviceClass = SoPriceService.class,
            keyIdName = "id")
    public ApiResult<SoPriceDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        SoPriceDTO.ViewDTO view = soPriceService.view(dto.getId());
        return success(view);
    }

    /**
     * 修改销售价目
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改销售价目")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id,create_user_id",
            menuCode = "oms:soPrice:update",
            serviceClass = SoPriceService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SoPriceDTO.UpdateDTO dto) {
        SoPriceEntity entity = soPriceService.updateSoPrice(dto);
        return null != entity ? success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode())) : failure();
    }

    /**
     * 修改并审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并审核销售价目")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id,create_user_id",
            menuCode = "oms:soPrice:update",
            serviceClass = SoPriceService.class,
            keyIdName = "id")
    public ApiResult<?> updateAndSubmit(@RequestBody @Validated SoPriceDTO.UpdateDTO dto) {
        SoPriceEntity entity = soPriceService.updateAndSubmit(dto);
        return null != entity ? success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode())) : failure();
    }


    /**
     * 删除销售价目
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除销售价目")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id,create_user_id",
            menuCode = "oms:soPrice:delete",
            serviceClass = SoPriceService.class,
            keyIdName = "ids")
    public ApiResult<?> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, SoPriceEntity> entityMap = soPriceService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoPriceEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id, id,"销售价目不存在"));
                continue;
            }
            try {
                resultDTOS.add(soPriceService.delete(entity));
            }catch (Exception e){
                log.error("销售价目删除失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 销售价目提交审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交销售价目")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id,create_user_id",
            menuCode = "oms:soPrice:submit",
            serviceClass = SoPriceService.class,
            keyIdName = "ids")
    public ApiResult<?> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, SoPriceEntity> entityMap = soPriceService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoPriceEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id, id,"销售价目不存在"));
                continue;
            }
            try {
                resultDTOS.add(soPriceService.submit(entity));
            }catch (Exception e){
                log.error("销售价目提交失败",e);
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
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核销售价目")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id,create_user_id",
            menuCode = "oms:soPrice:approve",
            serviceClass = SoPriceService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoPriceEntity> entityList = soPriceService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoPriceEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售价目不存在"));
                continue;
            }
            try {
                resultDTOS.add(soPriceService.approve(entity,new ApproveOneDTO(id, dto.getType(),dto.getComment())));
            }catch (Exception e){
                log.error("销售价目审核失败",e);
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
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销销售价目")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id,create_user_id",
            menuCode = "oms:soPrice:cancelProcess",
            serviceClass = SoPriceService.class,
            keyIdName = "ids")
    public ApiResult<?> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, SoPriceEntity> entityMap = soPriceService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoPriceEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售价目单不存在"));
                continue;
            }
            try {
                resultDTOS.add(soPriceService.cancelProcess((entity)));
            }catch (Exception e){
                log.error("销售价目撤销失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 销售价目数据导出
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "销售价目数据导出")
    @PostMapping("/exportSoPrice")
    public ApiResult<?> exportSoPrice(@RequestBody @Valid SoPriceDTO.PagingParamDTO dto) {
        soPriceService.exportSoPrice(dto);
        return success();
    }

    /**
     * 批量导入
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "销售价目数据批量导入")
    @PostMapping("/import")
    public ApiResult<?> importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = soPriceService.importFile(excelFile, response);
        return result ? success() : failure();
    }

    /**
     * 下载模板
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板销售价目")
    @GetMapping("/downloadTemplate")
    public ApiResult<?> downloadTemplate(HttpServletResponse response) {
        soPriceService.downloadTemplate(response);
        return success();
    }

    /**
     * 反审核
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核销售价目")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id,create_user_id",
            menuCode = "oms:soPrice:disApprove",
            serviceClass = SoPriceService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoPriceEntity> entityList = soPriceService.listByIds(dto.getIds());
        List<SoPriceDetailEntity> detailList = soPriceDetailService.listDetailByMainIds(dto.getIds());
        List<String> priceDetailIds = detailList.stream().map(SoPriceDetailEntity::getId).distinct().collect(Collectors.toList());
        List<SoPriceChangeDetailEntity> changeDetailList = soPriceChangeDetailService.listBySoPriceDetailIds(priceDetailIds);
        for (String id : dto.getIds()) {
            SoPriceEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售价目不存在"));
                continue;
            }
            List<SoPriceDetailEntity> detailEntityList = detailList.stream().filter(e -> e.getMainId().equals(id)).collect(Collectors.toList());
            List<String> priceDetailList = detailEntityList.stream().map(SoPriceDetailEntity::getId).distinct().collect(Collectors.toList());
            List<SoPriceChangeDetailEntity> changeDetailEntityList = changeDetailList.stream().filter(e -> priceDetailList.contains(e.getSoPriceDetailId())).collect(Collectors.toList());
            try {
                resultDTOS.add(soPriceService.disApprove(entity,changeDetailEntityList));
            }catch (Exception e){
                log.error("销售价目反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 更新明细备注
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "更新明细备注销售价目:明细备注={remark}")
    @PostMapping("/updateDetailRemark")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "pricing_user_id,create_user_id",
            menuCode = "oms:soPrice:updateDetailRemark",
            serviceClass = SoPriceService.class,
            keyIdName = "ids"
    )
    public ApiResult<?> updateDetailRemark(@RequestBody @Valid BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoPriceDetailEntity> detailList = soPriceDetailService.listByIds(dto.getIds());
        Map<String, SoPriceDetailEntity> detailMap = detailList.stream().collect(Collectors.toMap(BaseEntity::getId, e -> e));
        List<String> mainIds = detailList.stream().map(SoPriceDetailEntity::getMainId).distinct().collect(Collectors.toList());
        Map<String, SoPriceEntity> entityMap = soPriceService.mapByIds(mainIds);
        for (String id : dto.getIds()) {
            SoPriceDetailEntity detailEntity = detailMap.get(id);
            if(Objects.isNull(detailEntity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售明细不存在"));
                continue;
            }
            SoPriceEntity entity = entityMap.get(detailEntity.getMainId());
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售价目不存在"));
                continue;
            }
            try {
                Boolean disabled = soPriceService.updateDetailRemark(Collections.singletonList(id), dto.getRemark());
                if (disabled){
                    resultDTOS.add(BatchResultDTO.success(id, entity.getCode(), "更新销售价目明细备注失败"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, entity.getCode(), "更新销售价目明细备注成功"));
                }
            }catch (Exception e){
                log.error("更新销售销售价目明细备注失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 批量获取销售报价
     * @author will
     * @date 2025/3/27 11:40
     * @param list
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.oms.dto.SoPriceDTO.PriceDTO>>
     */
    @PostMapping("/batchGetSoPrice")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "pricing_user_id,create_user_id",
            menuCode = "oms:soPrice:paging",
            tableAlias = "sp,sp")
    public ApiResult<List<SoPriceDTO.PriceDTO>> batchGetSoPrice(@RequestBody @Valid List<SoPriceDTO.PriceParamDTO> list) {
        return success(soPriceService.batchGetSoPrice(list));
    }

}
