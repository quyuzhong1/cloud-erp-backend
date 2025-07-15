package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.validator.AddGroup;
import com.common.business.validator.UpdateGroup;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.entity.QcInfoEntity;
import com.erp.server.wms.query.QcInfoQueryHandler;
import com.erp.server.wms.service.QcInfoService;
import com.erp.server.wms.service.QcResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;

/**
 * 质检单
 *
 * @author lambda
 * @since 2023-04-14
 */
@Slf4j
@RestController
@LogSystemModule("质检单")
@RequestMapping("/qcBill")
public class QcInfoController extends BaseController {


    @Resource
    private QcInfoService qcInfoService;
    @Resource
    private QcResultService qcResultService;


    /**
     * 分页
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "qc_user_id",
            warehouseTableField = "qb.warehouse_id",
            menuCode = "wms:qcBill:paging",
            tableAlias = "qb")
    @WebAdvanceQuery(handler = QcInfoQueryHandler.class)
    public ApiResult<PagingVO<QcInfoDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<QcInfoDTO.PagingParamDTO> dto) {
        PagingVO<QcInfoDTO.PagingViewDTO> pagingVO = qcInfoService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 获取 质检tab 列表(待质检以及数量的列表)
     *
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "qc_user_id",
            warehouseTableField = "qb.warehouse_id",
            menuCode = "wms:qcBill:paging",
            tableAlias = "qb")
    public ApiResult<List<QcInfoDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<QcInfoDTO.TabListDTO> list = qcInfoService.tabList(dto);
        return success(list);
    }


    /**
     * 暂存
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "暂存质检单")
    @PostMapping("/draft")
    public ApiResult<?> draft(@RequestBody QcInfoDTO.SaveOrUpdateDTO dto) {
        QcInfoEntity entity = qcInfoService.draft(dto);
        return null != entity ? success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode())) : failure();
    }

    /**
     * 保存
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增质检单")
    @PostMapping("/add")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "qc_user_id",
            menuCode = "wms:qcBill:add",
            serviceClass = QcInfoService.class,
            keyIdName = "id")
    public ApiResult<?> add(@RequestBody @Validated({AddGroup.class,UpdateGroup.class}) QcInfoDTO.SaveOrUpdateDTO dto) {
        QcInfoEntity entity = qcInfoService.add(dto);
        return null != entity ? success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode())) : failure();
    }


    /**
     * 详情
     *
     * @param dto
     * @return
     */
    @LogViewService
    @PostMapping("/view")
    public ApiResult<QcInfoDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        QcInfoDTO.ViewDTO view = qcInfoService.view(dto.getId());
        return success(view);
    }

    /**
     * 完成质检
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "完成质检:id={id}")
    @PostMapping("/finish")
    public ApiResult<?> finish(@RequestBody @Validated({AddGroup.class}) QcInfoDTO.SaveOrUpdateDTO dto) {
        QcInfoEntity entity = qcInfoService.finish(dto);
        return null != entity ? success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode())) : failure();
    }

    /**
     * 免检
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "免检:id={id}")
    @PostMapping("/exemption")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "qc_user_id",
            menuCode = "wms:qcBill:batchExemption",
            serviceClass = QcInfoService.class,
            keyIdName = "id")
    public ApiResult<?> exemption(@RequestBody @Validated({UpdateGroup.class}) QcInfoDTO.SaveOrUpdateDTO dto) {
        QcInfoEntity entity = qcInfoService.exemption(dto);
        return null != entity ? success(new BaseResultDTO.AddDTO(entity.getId(), entity.getCode())) : failure();
    }


    /**
     * 批量完成质检
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "完成质检", keyIdName = "ids")
    @PostMapping("/batchFinish")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "qc_user_id",
            menuCode = "wms:qcBill:batchFinish",
            serviceClass = QcInfoService.class,
            keyIdName = "ids")
    public ApiResult<?> actionFinish(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, QcInfoEntity> entityMap = qcInfoService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            QcInfoEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"质检单不存在"));
                continue;
            }
            try {
                resultDTOS.add(qcInfoService.finish(entity));
            }catch (Exception e){
                log.error("质检单完成质检失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }



    /**
     * 批量免检
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量免检", keyIdName = "ids")
    @PostMapping("/batchExemption")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "qc_user_id",
            menuCode = "wms:qcBill:batchExemption",
            serviceClass = QcInfoService.class,
            keyIdName = "ids")
    public ApiResult<?> batchExemption(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, QcInfoEntity> entityMap = qcInfoService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            QcInfoEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"质检单不存在"));
                continue;
            }
            try {
                resultDTOS.add(qcInfoService.batchExemption(entity));
            }catch (Exception e){
                log.error("质检单完成质检失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 批量取消质检
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "取消质检", keyIdName = "ids")
    @PostMapping("/batchCancel")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "qc_user_id",
            menuCode = "wms:qcBill:batchCancel",
            serviceClass = QcInfoService.class,
            keyIdName = "ids")
    public ApiResult<?> batchCancel(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, QcInfoEntity> entityMap = qcInfoService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            QcInfoEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"质检单不存在"));
                continue;
            }
            if(entity.getSourceType().equals(SourceTypeEnum.QC_NOTICE.getCode())){
                resultDTOS.add(BatchResultDTO.fail(id, id, "数据来源质检通知单不可在此操作"));
                continue;
            }
            try {
                resultDTOS.add(qcInfoService.batchCancel(entity));
            }catch (Exception e){
                log.error("质检单完成质检失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 删除
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除质检单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "qc_user_id",
            menuCode = "wms:qcBill:delete",
            serviceClass = QcInfoService.class,
            keyIdName = "ids")
    public ApiResult<?> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, QcInfoEntity> entityMap = qcInfoService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            QcInfoEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"质检单不存在"));
                continue;
            }
            try {
                resultDTOS.add(qcInfoService.delete(entity));
            }catch (Exception e){
                log.error("质检单删除失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 撤销
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销质检单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "qc_user_id",
            menuCode = "wms:qcBill:cancelProcess",
            serviceClass = QcInfoService.class,
            keyIdName = "ids")
    public ApiResult<?> cancelProcess(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, QcInfoEntity> entityMap = qcInfoService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            QcInfoEntity entity = entityMap.get(id);
            if (Objects.isNull(entity)) {
                resultDTOS.add(BatchResultDTO.fail(id, id, "质检单不存在"));
                continue;
            }
            if(entity.getSourceType().equals(SourceTypeEnum.QC_NOTICE.getCode())){
                resultDTOS.add(BatchResultDTO.fail(id, id, "数据来源质检通知单不可在此操作"));
                continue;
            }
            try {
                resultDTOS.add(qcInfoService.cancelProcess(entity));
            } catch (Exception e) {
                log.error("质检单删除失败", e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 导出质检单
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出质检单")
    @PostMapping("/exportQcBill")
    public ApiResult<?> exportWarehouse(@RequestBody @Valid QcInfoDTO.ExportDTO dto) {
        qcInfoService.exportQcBill(dto);
        return success();
    }

    /**
     * 分配质检员
     *
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "分配质检员:ids={ids},质检员={qcUserId}")
    @PostMapping("/assign")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "qc_user_id",
            menuCode = "wms:qcBill:assign",
            serviceClass = QcInfoService.class,
            keyIdName = "ids")
    public ApiResult<?> assign(@RequestBody @Valid QcInfoDTO.AssignDTO dto) {
        Boolean result = qcInfoService.assign(dto);
        return result ? success() : failure();
    }

    /**
     * 批量更新处理措施
     *
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量更新处理措施:处理措施={handleModeDict}", keyIdName = "ids")
    @PostMapping("/updateHandleMode")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "qc_user_id",
            menuCode = "wms:qcBill:updateHandleMode",
            serviceClass = QcInfoService.class,
            keyIdName = "ids")
    public ApiResult<?> updateHandleMode(@RequestBody @Valid QcResultDTO.UpdateHandleModeDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, QcInfoEntity> entityMap = qcInfoService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            QcInfoEntity entity = entityMap.get(id);
            if (Objects.isNull(entity)) {
                resultDTOS.add(BatchResultDTO.fail(id, id, "质检单不存在"));
                continue;
            }
            try {
                Boolean flag = qcInfoService.updateHandleMode(new QcResultDTO.UpdateHandleModeDTO(dto.getHandleModeDict(), Collections.singletonList(id)));
                if (flag) {
                    resultDTOS.add(BatchResultDTO.success(id, entity.getCode(), "更新处理措施成功"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, entity.getCode(), "更新处理措施失败"));
                }
            } catch (Exception e) {
                log.error("质检单处理措施删除失败", e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 下推退货单数据显示
     *
     * @param dto
     * @return ApiResult<ViewGeneratePurchaseReturnOrderDTO>
     * @author yl
     * @date: 2023/4/11 20:30
     */
    @PostMapping("/viewGeneratePurchaseReturnOrder")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "qc_user_id",
            menuCode = "wms:qcBill:generatePurchaseReturnOrder",
            serviceClass = QcInfoService.class,
            keyIdName = "ids")
    public ApiResult<List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO>> viewGeneratePurchaseReturnOrder(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> list = qcInfoService.viewGeneratePurchaseReturnOrder(dto.getIds());
        return success(list);
    }


    /**
     * 下推退货单数据保存
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.wms.dto.PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO>>
     * @author yl
     * @date 2023-04-24 9:25
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "下推退货单数据保存")
    @PostMapping("/generatePurchaseReturnOrder")
    public ApiResult<List<BatchResultDTO>> generatePurchaseReturnOrder(@RequestBody @Validated PoInstockDTO.ListGeneratePurchaseReturnOrderDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getList().size());
        try {
            resultDTOS = qcInfoService.generatePurchaseReturnOrder(dto.getList());
        }catch (Exception e){
            log.error("下推退货单数据保存失败",e);
            BatchResultDTO resultDTO = BatchResultDTO.fail(dto.getList().get(0).getSourceId(), "", e.getMessage());
            resultDTOS.add(resultDTO);
        }

        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    @PostMapping("/test")
    public ApiResult<?> test(@RequestBody List<String> ids) {
        qcResultService.sendQcResultMsg(ids);
        return success();
    }

    /**
     * 退货签收单下推质检单
     * @Author Luo_WG
     * @Date 2023/5/23 14:05
     * @param ids ids
     * @return java.lang.Boolean
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "退货签收单下推质检单")
    @PostMapping("/returnReceiveGenerateQCSave")
    public ApiResult<?> returnReceiveGenerateQCSave(@RequestBody List<String> ids) {
        Boolean flag = qcInfoService.returnReceiveGenerateQCSave(ids);
        return Objects.equals(flag, Boolean.TRUE) ? success() : failure();
    }

    /**
     * 下推退货入库单-列表查询
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping(value = "/generateSoReturnInstockView")
    public ApiResult<List<SoReturnInstockDTO.GenerateSoReturnInstockView>> generateSoReturnInstockView(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<SoReturnInstockDTO.GenerateSoReturnInstockView> generateSoDeliveryViews = qcInfoService.generateSoReturnInstockView(dto.getIds());
        return success(generateSoDeliveryViews);
    }

    /**
     * 导出质检单日报
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出质检单日报")
    @PostMapping("/exportDailyQcBill")
    public ApiResult<?> exportDailyExcel(@RequestBody @Valid QcInfoDTO.ExportDTO dto) {
        qcInfoService.exportDailyExcel(dto);
        return success();
    }

    /**
     * 获取质检用户
     * @Author Luo_WG
     * @Date 2023/7/20 16:34
     * @return com.common.core.controller.vo.ApiResult
     **/
    @GetMapping("/listQcUser")
    public ApiResult<List<SysUserInfoEntity>> listQcUser() {
        List<SysUserInfoEntity> sysUserInfoEntities = qcInfoService.listQcUser();
        return success(sysUserInfoEntities);
    }

    /**
     * 复检抽检
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "复检抽检:复检抽检结果={qcSampleResult},ids={ids}")
    @PostMapping("/reQcSample")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "qc_user_id",
            menuCode = "wms:qcBill:reQcSample",
            serviceClass = QcInfoService.class,
            keyIdName = "id")
    public ApiResult<Void> reQcSample(@RequestBody @Validated() QcInfoDTO.ReQcDTO dto) {
        qcInfoService.reQcSample(dto);
        return success();
    }

    /**
     * TODO 临时接口，修复质检的来源单号
     * @Author Luo_WG
     * @Date 2023/10/23 10:45
     * @return com.common.core.controller.vo.ApiResult<java.lang.Void>
     **/
    @PostMapping("/repairQcInfoSourceCode")
    public ApiResult<Void> repairQcInfoSourceCode() {
        qcInfoService.repairQcInfoSourceCode();
        return success();
    }

    /**
     * 根据质检单id 获取（产品经理 + 项目经理）推送消息人员
     * @Author jack
     * @Date 2025-07-03
     **/
    @PostMapping("/listQcItemRolePeople")
    public ApiResult<Map<String, String>> listQcItemRolePeople(@RequestBody QcResultDTO.QcItemRolePeopleDTO dto ) {
        return success(qcResultService.listQcItemRolePeople(dto));
    }

}
