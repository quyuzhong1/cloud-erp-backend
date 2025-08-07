package com.erp.server.wms.controller.api;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.server.wms.query.SoOutstockQueryHandler;
import com.erp.server.wms.service.SoOutstockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;

/**
 * 销售出库-销售出库单
 *
 * @author lambda
 * @since 2023-05-10
 */
@Slf4j
@RestController
@LogSystemModule("销售出库单")
@RequestMapping("/so/outstock")
public class SoOutstockController extends BaseController {

    @Resource
    private SoOutstockService soOutstockService;
    

    /**
     * 获取 tab列表
     *
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,seller_id",
            warehouseTableField = "so.warehouse_id",
            shopTableField = "so.shop_id",
            menuCode = "wms:so:outstock:paging",
            tableAlias = "so"
    )
    public ApiResult<List<SoOutstockDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
    	List<SoOutstockDTO.TabListDTO> tabList = soOutstockService.tabList(dto);
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
            tableField = "create_user_id,seller_id",
            warehouseTableField = "so.warehouse_id",
            shopTableField = "so.shop_id",
            menuCode = "wms:so:outstock:paging",
            tableAlias = "so"
    )
    @WebAdvanceQuery(handler = SoOutstockQueryHandler.class)
    public ApiResult<PagingVO<SoOutstockDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<SoOutstockDTO.PagingParamDTO> dto) {
    	PagingVO<SoOutstockDTO.PagingViewDTO> pagingVO = soOutstockService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 查询总数量
     *
     * @param dto
     * @return ApiResult<PagingTotalDTO>
     * @author Will
     * @date: 2023/11/1 14:16
     */
    @PostMapping("/getTotalByQuery")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id,seller_id",
            menuCode = "wms:so:outstock:paging",
            tableAlias = "so"
    )
    @WebAdvanceQuery(handler = SoOutstockQueryHandler.class)
    public ApiResult<SoOutstockDTO.PagingTotalDTO> getTotalByQuery(@RequestBody @Validated SoOutstockDTO.PagingParamDTO dto) {
    	SoOutstockDTO.PagingTotalDTO pagingTotalDTO = soOutstockService.getTotalByQuery(dto);
        return success(pagingTotalDTO);
    }


    /**
     * 创建
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增销售出库单")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SoOutstockDTO.AddDTO dto) {
        String id = soOutstockService.add(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();
    }

    /**
     * 批量提交审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交销售出库单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "wms:so:outstock:submit",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids"
    )
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = soOutstockService.submit(dto.getIds());
        return result ? success() : failure();
    }


    /**
     * 提交审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交销售出库单")
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated SoOutstockDTO.AddDTO dto) {
        Boolean result = soOutstockService.addAndSubmit(dto);
        return result ? success() : failure();
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
            menuCode = "wms:so:outstock:view",
            serviceClass = SoOutstockService.class,
            keyIdName = "id"
    )
    public ApiResult<SoOutstockDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        SoOutstockDTO.ViewDTO view = soOutstockService.view(dto.getId());
        return success(view);
    }

    /**
     * 修改
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改销售出库单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "wms:so:outstock:update",
            serviceClass = SoOutstockService.class,
            keyIdName = "id"
    )
    public ApiResult update(@RequestBody @Validated SoOutstockDTO.UpdateDTO dto) {
        String id = soOutstockService.updateSoOutstock(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改并提交
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交销售出库单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "wms:so:outstock:update",
            serviceClass = SoOutstockService.class,
            keyIdName = "id"
    )
    public ApiResult updateAndSubmit(@RequestBody @Validated SoOutstockDTO.UpdateDTO dto) {
        Boolean result = soOutstockService.updateAndSubmit(dto);
        return result ? success() : failure();
    }

    /**
     * 更新跟踪号
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023/7/12 16:54
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "销售出库单列表修改:运输单号={trackNo},ids={idList}")
    @PostMapping("/pagingUpdate")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "wms:so:outstock:update",
            serviceClass = SoOutstockService.class,
            keyIdName = "id"
    )
    public ApiResult<List<BatchResultDTO>> pagingUpdate(@RequestBody @Validated List<SoOutstockDTO.PagingUpdateDTO> dto) {
        List<BatchResultDTO> batchResultDTOList = soOutstockService.pagingUpdate(dto);
        return batchResultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(batchResultDTOList) : failure(batchResultDTOList);
    }

    /**
     * 审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核销售出库单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "wms:so:outstock:approve",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> audit(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<String> ids = dto.getIds();
        for (String id : ids) {
            BatchResultDTO result;
            try {
                result = soOutstockService.approve(new ApproveOneDTO(id, dto.getType(), dto.getComment()));
            } catch (Exception e) {
                log.error("销售出库 审核失败>>>>{}", e);
                SoOutstockEntity entity = soOutstockService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "销售出库单不存在, 审核失败");
                    resultDTOS.add(result);
                    continue;
                }
                String message = e.getMessage();
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), message);

            }
            resultDTOS.add(result);
        }

        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);


    }

    /**
     * 重新生成销售出库单
     *
     * @param
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-28 19:43
     */
    @PostMapping("afreshGenerateB2cOutstock")
    public ApiResult<String> afreshGenerateB2cOutstock(@RequestBody BaseIdsDTO.IdsDTO dto) {
        Boolean result =  soOutstockService.afreshGenerateB2cOutstock(dto.getIds());
        return result?success():failure("重新生成销售出库单失败");
    }

    /**
     * 反审核
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核销售出库单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "wms:so:outstock:disApprove",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoOutstockEntity> entityList = soOutstockService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoOutstockEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售出库单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(soOutstockService.disApprove(entity, Boolean.TRUE));
            }catch (Exception e){
                log.error("销售出库单反审核失败",e);
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
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销销售出库单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "wms:so:outstock:cancelProcess",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = soOutstockService.cancelProcess(dto.getIds());
        return result ? success() : failure();
    }

    /**
     * 删除销售出库单
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除记录")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "wms:so:outstock:delete",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        try {
            List<BatchResultDTO> resultDTOS = soOutstockService.deleteByIds(dto.getIds(), true);
            return success(resultDTOS);
        } catch (Exception e) {
            log.error("批量删除销售出库单失败", e);
            return failure(e.getMessage());
        }
    }

    /**
     * 作废
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023/5/10 20:11
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "作废销售出库单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id,seller_id",
            menuCode = "wms:so:outstock:invalid",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean result = soOutstockService.invalid(dto.getIds(), dto.getRemark());
        return result ? success() : failure();
    }

    /**
     * 导出
     * 数据
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出销售出库单")
    @PostMapping("/export")
    public ApiResult exportWarehouse(@RequestBody @Valid SoOutstockDTO.ExportDTO dto) {
        Boolean result = soOutstockService.exportExcel(dto);
        return result ? success() : failure();
    }


    /**
     * 销售订单关联销售出库单
     *
     * @param soId
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-05-23 18:01
     */
    @GetMapping("/listSoRefSoOutstock")
    public ApiResult<List<SoOutstockDTO.SoRefDTO>> soRefSoOutstock(@RequestParam("soId") String soId) {
        List<SoOutstockDTO.SoRefDTO> list = soOutstockService.listSoRefSoOutstockBySoId(soId);
        return success(list);
    }

    /**
     * 打印
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.lang.Void>
     * @Author Luo_WG
     * @Date 2023/7/13 10:44
     **/
    @PostMapping("/print")
    public ApiResult<List<SoOutstockDTO.PrintDTO>> print(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<SoOutstockDTO.PrintDTO> printDTOList = soOutstockService.print(dto.getIds());
        return success(printDTOList);
    }

    /**
     * 修复销售出库单历史数据
     *
     * @return
     */
    @PostMapping("/tempRepairHistoryDb")
    public ApiResult tempRepairHistoryDb() {
        soOutstockService.tempRepairHistoryDb();
        return success();
    }

    /**
     * 修复销售出库单历史数据
     *
     * @return
     */
    @GetMapping("/test")
    public ApiResult test(@RequestParam("code") String code) {
        String soB2cCode = "XSDD24010300005";
        try {
            soOutstockService.generateB2cSoOutstockByCode(code);
        } catch (Exception e) {
            log.error("销售订单{} 生成销售出库单失败>>>>>>{}", soB2cCode, e.getMessage());
        }


        return success();
    }
    
    /**
     * 下推物流单
     */
    @LogAction(value = LogActionEnum.UPDATE_STATUS, desc = "下推物流单")
    @PostMapping("/saveLogisticsBill")
    public ApiResult<List<BatchResultDTO>> saveLogisticsBill(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoOutstockEntity> entityList = soOutstockService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoOutstockEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"销售出库单记录不存在"));
                continue;
            }
            if(ApproveStatusEnum.APPROVE != entity.getApproveStatus()) {
            	resultDTOS.add(BatchResultDTO.fail(id,entity.getCode(),"销售出库单不是已审核，不允许下推物流单"));
                continue;
            }
            try {
            	soOutstockService.saveLogisticsBill(entity);
                resultDTOS.add(BatchResultDTO.success(id, entity.getCode()));
            }catch (Exception e){
                log.error("销售出库单下推物流单失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 修复数据
     * @author will
     * @date 2024/12/31 18:45
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/handleWdtData")
    public ApiResult<List<BatchResultDTO>> handleWdtData(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = soOutstockService.handleWdtData(id);
            }catch (Exception e){
                log.error("销售出库单 修复数据失败",e);
                SoOutstockEntity entity = soOutstockService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "销售出库单不存在, 修复数据失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

}
