package com.erp.server.oms.controller.api;


import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.wms.entity.WmsDeliveryPlanEntity;
import com.erp.server.oms.service.ShopInfoService;
import com.erp.server.oms.service.SoB2cService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.SoMultiChannelService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.SoMultiChannelDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.oms.entity.SoMultiChannelEntity;

/**
 * 多渠道订单主表
 *
 * @author zdy
 * @since 2025-08-20
 */
@Slf4j
@RestController
@LogSystemModule("多渠道订单主表")
@RequestMapping("/soMultiChannel")
public class SoMultiChannelController extends BaseController {

    @Resource
    private SoMultiChannelService soMultiChannelService;
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private ShopInfoService shopInfoService;
//    /**
//    * 新增
//    * @author zdy
//    * @date:  2025-08-20
//    * @param dto
//    * @return ApiResult<String>
//    */
//    @PostMapping("/add")
//    @LogAction(value = LogActionEnum.INSERT, desc = "多渠道订单主表新增")
//    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SoMultiChannelDTO.AddDTO dto) {
//        return success(soMultiChannelService.add(dto));
//    }

//    /**
//    * 修改
//    * @author zdy
//    * @date:  2025-08-20
//    * @param dto
//    * @return ApiResult
//    */
//    @PostMapping("/update")
//    @LogAction(value = LogActionEnum.UPDATE, desc = "多渠道订单主表修改")
//        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//        tableField = "create_user_id",
//        menuCode = "oms:soMultiChannel:update",
//        serviceClass = SoMultiChannelService.class,
//        keyIdName = "id")
//    public ApiResult<?> update(@RequestBody @Validated SoMultiChannelDTO.UpdateDTO dto) {
//        soMultiChannelService.update(dto);
//        return success();
//    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soMultiChannel:paging",
            tableAlias = "smc"
    )
    public ApiResult<List<SoMultiChannelDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(soMultiChannelService.tabList(dto));
    }

    /**
    * 列表查询
    * @author zdy
    * @date: 2025-08-20
    * @param dto
    * @return ApiResult<PagingVO<SoMultiChannelDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soMultiChannel:paging",
            tableAlias = "smc"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<SoMultiChannelDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SoMultiChannelDTO.PagingParamDTO> dto) {
        return success(soMultiChannelService.paging(dto));
    }

//    /**
//    * 新增并提交审核
//    * @author zdy
//    * @date:  2025-08-20
//    * @param dto
//    * @return ApiResult<Void>
//    */
//    @PostMapping("/addAndSubmit")
//    public ApiResult<BaseResultDTO.AddDTO> addAndSubmit(@RequestBody @Validated SoMultiChannelDTO.AddDTO dto) {
//        BaseResultDTO.AddDTO result = soMultiChannelService.addAndSubmit(dto);
//        return success(result);
//    }

//    /**
//    * 修改并提交审核
//    * @author zdy
//    * @date:  2025-08-20
//    * @param dto
//    * @return ApiResult<Void>
//    */
//    @PostMapping("/updateAndSubmit")
//    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//            tableField = "create_user_id",
//            menuCode = "oms:soMultiChannel:updateAndSubmit",
//            serviceClass = SoMultiChannelService.class,
//            keyIdName = "id")
//    public ApiResult<Void> updateAndSubmit(@RequestBody @Validated SoMultiChannelDTO.UpdateDTO dto) {
//        soMultiChannelService.updateAndSubmit(dto);
//        return success();
//    }

    /**
    * 提交审核
    * @author zdy
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soMultiChannel:submit",
            serviceClass = SoMultiChannelService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "多渠道订单主表提交审核")
    public ApiResult<List<BatchResultDTO>> batchSubmit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SoMultiChannelEntity> list = soMultiChannelService.lambdaQuery().in(SoMultiChannelEntity::getId, ids).list();
		Map<String, SoMultiChannelEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoMultiChannelEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = soMultiChannelService.submit(id);
            }catch (Exception e){
                log.error("多渠道订单主单 提交审核失败",e);
                SoMultiChannelEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "多渠道订单主单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 审核
    * @author zdy
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soMultiChannel:approve",
            serviceClass = SoMultiChannelService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.APPROVE, desc = "多渠道订单主表审核")
    public ApiResult<List<BatchResultDTO>> batchApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<SoMultiChannelEntity> list = soMultiChannelService.lambdaQuery().in(SoMultiChannelEntity::getId, ids).list();
		Map<String, SoMultiChannelEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoMultiChannelEntity::getId, w -> w));
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = soMultiChannelService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("多渠道订单主单审核失败",e);
                SoMultiChannelEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "多渠道订单主单不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 反审核
    * @author zdy
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soMultiChannel:disApprove",
            serviceClass = SoMultiChannelService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "多渠道订单主表反审核")
    public ApiResult<List<BatchResultDTO>> batchDisApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SoMultiChannelEntity> list = soMultiChannelService.lambdaQuery().in(SoMultiChannelEntity::getId, ids).list();
		Map<String, SoMultiChannelEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoMultiChannelEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = soMultiChannelService.disApprove(id);
            }catch (Exception e){
                log.error("多渠道订单主单反审核失败",e);
                SoMultiChannelEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "多渠道订单主单不存在, 反审核失败");
                    resultDTOS.add(disApproveResult);
                    continue;
                }
                disApproveResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(disApproveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
    * 删除
    * @author zdy
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soMultiChannel:delete",
            serviceClass = SoMultiChannelService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "多渠道订单主表删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		// TODO 数据查询放入外层，处理结果统一更新或单条更新
		List<SoMultiChannelEntity> list = soMultiChannelService.lambdaQuery().in(SoMultiChannelEntity::getId, ids).list();
		Map<String, SoMultiChannelEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoMultiChannelEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = soMultiChannelService.delete(id);
            }catch (Exception e){
                log.error("多渠道订单主单删除失败",e);
                SoMultiChannelEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "多渠道订单主单不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 重新创建
     * @author zdy
     * @date:  2025-08-20
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/reCreate")
    @LogAction(value = LogActionEnum.UPDATE, desc = "多渠道订单重新创建")
    public ApiResult<List<BatchResultDTO>> batchCreate(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SoMultiChannelEntity> list = soMultiChannelService.lambdaQuery().in(SoMultiChannelEntity::getId, ids).list();
        Map<String, SoMultiChannelEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoMultiChannelEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            SoMultiChannelEntity entity = idEntityMap.get(id);
            if (ObjectUtil.isEmpty(entity)) {
                deleteResult = BatchResultDTO.fail(id, id, "多渠道订单主单不存在, 重新创建失败");
                resultDTOS.add(deleteResult);
                continue;
            }
            try {
                deleteResult = soMultiChannelService.reCreate(entity);
            }catch (Exception e){
                log.error("多渠道订单主单重新创建失败",e);
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 撤销
    * @author zdy
    * @date:  2025-08-20
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soMultiChannel:cancelProcess",
            serviceClass = SoMultiChannelService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "多渠道订单主表撤销")
    public ApiResult<List<BatchResultDTO>> batchCancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        // TODO 数据查询放入外层，处理结果统一更新或单条更新
        List<SoMultiChannelEntity> list = soMultiChannelService.lambdaQuery().in(SoMultiChannelEntity::getId, ids).list();
        Map<String, SoMultiChannelEntity> idEntityMap = list.stream().collect(Collectors.toMap(SoMultiChannelEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = soMultiChannelService.cancelProcess(id);
            }catch (Exception e){
                log.error("多渠道订单主单撤回流程失败",e);
                SoMultiChannelEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "多渠道订单主单不存在, 撤回流程失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 详情
    * @author zdy
    * @date:  2025-08-20
    * @param id
    * @return ApiResult<SoMultiChannelDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:soMultiChannel:view",
            serviceClass = SoMultiChannelService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<SoMultiChannelDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(soMultiChannelService.view(id));
    }

    /**
    * 导出Excel数据
    * @author zdy
    * @date:  2025-08-20
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:soMultiChannel:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "多渠道订单主表导出Excel数据")
    public ApiResult<Boolean>  exportList(@RequestBody @Validated SoMultiChannelDTO.PagingParamDTO dto, HttpServletResponse response) {
        soMultiChannelService.exportList(dto, response);
        return success(true);
    }

    /**
     * 创建多渠道订单弹窗
     * @return
     */
    @PostMapping("/listSoMultiChannel")
    public ApiResult<List<SoMultiChannelDTO.SoViewDTO>> listSoMultiChannel(@RequestBody @Validated SoMultiChannelDTO.IdsDTO idDTO) {
        List<String> ids = idDTO.getSoIds().stream().filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        return success(soMultiChannelService.listSoMultiChannel(ids, idDTO.getDeliveryWarehouseId(),idDTO.getShopId()));
    }

    /**
     * 多渠道订单保存
     * @param dto
     * @return
     */
    @PostMapping("/batchSave")
    public ApiResult<List<BatchResultDTO>> batchSave(@RequestBody @Validated SoMultiChannelDTO.SaveDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        List<String> soIds = dto.getDetailList().stream().map(SoMultiChannelDTO.SoViewDTO::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cService.listByIds(soIds);
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(dto.getShopId());
        LogisticsChannelEntity channelEntity = FeignQuery.getById(LogisticsChannelEntity.class, dto.getLogisticsChannelId());
        for (String id : soIds) {
            BatchResultDTO submit;
            SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(e -> e.getId().equals(id)).findFirst().orElse(null);
            if (Objects.isNull(soB2cEntity)) {
                submit = BatchResultDTO.fail(id, id, "订单不存在, 提交失败");
                resultDTOS.add(submit);
                continue;
            }
            ShopInfoEntity shopInfoEntity1 = shopInfoService.getById(soB2cEntity.getShopId());
            if (Objects.nonNull(shopInfoEntity1)){
                soB2cEntity.setShopName(shopInfoEntity1.getName());
            }
            try {
                SoMultiChannelDTO.AddDTO addDTO = soMultiChannelService.buildAddDTO(dto, id, shopInfoEntity,soB2cEntity,channelEntity);
                BaseResultDTO.AddDTO add = soMultiChannelService.add(addDTO);
                submit = BatchResultDTO.success(add.getId(), add.getCode());
            }catch (Exception e){
                log.error("多渠道订单保存失败",e);
                submit = BatchResultDTO.fail(soB2cEntity.getId(), soB2cEntity.getCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
