package com.erp.server.wms.controller.api;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.dto.SoDeliveryNoticeDTO;
import com.erp.model.wms.dto.WarehouseLocationMoveDTO;
import com.erp.model.wms.entity.PackingTaskEntity;
import com.erp.model.wms.entity.SoDeliveryNoticeEntity;
import com.erp.server.wms.query.SoDeliveryNoticeQueryHandler;
import com.erp.server.wms.service.PackingTaskService;
import com.erp.server.wms.service.SoDeliveryNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 销售发货通知单
 * @author Luo_WG
 * @since 2023-05-10
 */
@Slf4j
@RestController
@LogSystemModule("销售出库发货通知单")
@RequestMapping("/soDeliveryNotice")
public class SoDeliveryNoticeController extends BaseController {
    @Resource
    private SoDeliveryNoticeService soDeliveryNoticeService;

    @Resource
    private PackingTaskService packingTaskService;

    /**
     * 列表查询
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO < com.erp.model.wms.dto.soDeliveryNoticeDTO.PagingViewDTO>>
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "sdn.warehouse_id",
            menuCode = "wms:soDeliveryNotice:paging",
            tableAlias = "sdn"
    )
    @WebAdvanceQuery(handler = SoDeliveryNoticeQueryHandler.class)
    public ApiResult<PagingVO<SoDeliveryNoticeDTO.PagingView>> paging(@RequestBody @Validated PagingDTO<SoDeliveryNoticeDTO.PagingParam> dto) {
        PagingVO<SoDeliveryNoticeDTO.PagingView> pagingVO = soDeliveryNoticeService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 列表状态数量统计
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.wms.dto.soDeliveryNoticeDTO.soDeliveryNoticeCountDTO>>
     * @Author Luo_WG
     * @Date 2023/4/17 13:14
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            warehouseTableField = "sdn.warehouse_id",
            menuCode = "wms:soDeliveryNotice:paging",
            tableAlias = "sdn"
    )
    public ApiResult<List<SoDeliveryNoticeDTO.StatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<SoDeliveryNoticeDTO.StatusCountDTO> soDeliveryNoticeCountDTOS = soDeliveryNoticeService.listCount(dto);
        return success(soDeliveryNoticeCountDTOS);
    }

    /**
     * 新增
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "新增发货通知单")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SoDeliveryNoticeDTO.Add dto) {
        String id = soDeliveryNoticeService.add(dto);
        return CharSequenceUtil.isNotBlank(id) == true ? success() : failure();
    }

    /**
     * 修改
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     **/
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改发货通知单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soDeliveryNotice:update",
            serviceClass = SoDeliveryNoticeService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated SoDeliveryNoticeDTO.Update dto) {
        Boolean flag = soDeliveryNoticeService.update(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     *
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.soDeliveryNoticeDTO.ViewDTO>
     * @Author Luo_WG
     * @Date 2023/4/6 18:57
     **/
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soDeliveryNotice:view",
            serviceClass = SoDeliveryNoticeService.class,
            keyIdName = "id")
    public ApiResult<SoDeliveryNoticeDTO.View> view(@RequestParam("id") String id) {
        SoDeliveryNoticeDTO.View dto = soDeliveryNoticeService.view(id);
        return success(dto);
    }

    /**
     * 提交
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     **/
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交发货通知单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soDeliveryNotice:submit",
            serviceClass = SoDeliveryNoticeService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soDeliveryNoticeService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 新增提交
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     **/
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交发货通知单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soDeliveryNotice:add",
            serviceClass = SoDeliveryNoticeService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated SoDeliveryNoticeDTO.Add dto) {
        Boolean flag = soDeliveryNoticeService.addAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改提交
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     **/
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交发货通知单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soDeliveryNotice:update",
            serviceClass = SoDeliveryNoticeService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated SoDeliveryNoticeDTO.Update dto) {
        Boolean flag = soDeliveryNoticeService.updateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     **/
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核发货通知单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soDeliveryNotice:approve",
            serviceClass = SoDeliveryNoticeService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoDeliveryNoticeEntity> entityList = soDeliveryNoticeService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoDeliveryNoticeEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"发货通知单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(soDeliveryNoticeService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("发货通知单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量反审核
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     **/
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核发货通知单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soDeliveryNotice:disApprove",
            serviceClass = SoDeliveryNoticeService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoDeliveryNoticeEntity> entityList = soDeliveryNoticeService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoDeliveryNoticeEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"发货通知单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(soDeliveryNoticeService.disApprove(entity));
            }catch (Exception e){
                log.error("发货通知单反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消流程
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/13 18:58
     **/
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销发货通知单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soDeliveryNotice:cancelProcess",
            serviceClass = SoDeliveryNoticeService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soDeliveryNoticeService.cancelProcess(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 批量作废
     *
     * @param remarkDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     **/
    @LogAction(value = LogActionEnum.INVALID, desc = "作废发货通知单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soDeliveryNotice:invalid",
            serviceClass = SoDeliveryNoticeService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO remarkDTO) {
        Boolean flag = soDeliveryNoticeService.invalid(remarkDTO.getIds(), remarkDTO.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量删除
     *
     * @param idsDTO idsDTO
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除记录")
    @PostMapping("/delete")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        try {
            List<BatchResultDTO> resultDTOS = soDeliveryNoticeService.deleteByIds(idsDTO.getIds(), true);
            return success(resultDTOS);
        } catch (Exception e) {
            log.error("批量删除发货通知单失败", e);
            return failure(e.getMessage());
        }
    }

    /**
     * 导出
     *
     * @param dto      dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出发货通知单")
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody SoDeliveryNoticeDTO.PagingParam dto) {
        Boolean flag = soDeliveryNoticeService.exportExcel(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 下推销售出库单-保存
     *
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/13 18:5
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "下推销售出库单")
    @PostMapping(value = "/generateSoDeliverySave")
    public ApiResult<List<BatchResultDTO>> generateSoDeliverySave(@RequestBody BaseIdsDTO.DeliveryDTO idsDTO) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(idsDTO.getIds().size());
        for (String id : idsDTO.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = soDeliveryNoticeService.generateSoDeliverySave(id,idsDTO.getDeliveryDate());
            }catch (Exception e){
                log.error("发货通知单不存在, 下推销售出库单失败",e);
                SoDeliveryNoticeEntity entity = soDeliveryNoticeService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "发货通知单不存在, 下推销售出库单失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 下推发货通知单-保存
     * @Author Luo_WG
     * @Date 2023/5/25 12:30
     * @param validList validList
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "下推发货通知单")
    @PostMapping("/generateDeliverySave")
    public ApiResult generateDeliverySave(@RequestBody @Validated ValidList<SoInfoDTO.GenerateDeliveryView> validList) {
        Boolean flag = soDeliveryNoticeService.generateDeliverySave(validList.getList());
        return flag ? success() : failure();
    }

    /**
     * 销售单详情-单据关联-发货通知单
     * @Author Luo_WG
     * @Date 2023/5/25 16:36
     * @param soId
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SoDeliveryNoticeDTO.PagingView>>
     **/
    @GetMapping("/listSoDeliveryNoticeBySourceId")
    public ApiResult<List<SoDeliveryNoticeDTO.PagingView>> listSoDeliveryNoticeBySourceId(@RequestParam("soId") String soId) {
        List<SoDeliveryNoticeDTO.PagingView> list = soDeliveryNoticeService.listSoReturnDetailBySourceId(soId);
        return success(list);
    }

    /**
     * 生成拣货单
     * @param picking 参数
     */
    @PostMapping("/generatePickingList")
    public ApiResult<List<WarehouseLocationMoveDTO.GenPickToSkuMove>> generatePickingList(@RequestBody @Validated SoDeliveryNoticeDTO.GeneratePickingDTO picking) {
        List<WarehouseLocationMoveDTO.GenPickToSkuMove> moves = soDeliveryNoticeService.generatePickingList(picking);
        return success(moves);
    }

    /**
     * 生成拣货单的弹窗
     * @param page 要货单id
     */
    @PostMapping("/generatePickingView")
    public ApiResult<PagingVO<SoDeliveryNoticeDTO.PickingViewDTO>> generatePickingView(@RequestBody @Validated PagingDTO<String> page) {
        PagingVO<SoDeliveryNoticeDTO.PickingViewDTO> result = soDeliveryNoticeService.generatePickingView(page);
        return success(result);
    }

    /**
     * 下推装箱任务
     **/
    @PostMapping("/generatePackingTask")
    public ApiResult<List<BatchResultDTO>> generatePackingTask(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<SoDeliveryNoticeEntity> entityList = soDeliveryNoticeService.listByIds(dto.getIds());
        List<String> sourceCodes = entityList.stream().map(SoDeliveryNoticeEntity::getCode).distinct().collect(Collectors.toList());
        List<PackingTaskEntity> packingTaskEntityList = packingTaskService.listBySourceCodes(sourceCodes);
        List<BatchResultDTO> result = new ArrayList<>();
        for (String id : dto.getIds()) {
            SoDeliveryNoticeEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                result.add(BatchResultDTO.fail(id,id,"发货通知单为空"));
                continue;
            }
            try {
                PackingTaskEntity packingTaskEntity = packingTaskEntityList.stream().filter(v->v.getSourceCode().equals(entity.getCode())).findFirst().orElse(null);
                if(Objects.nonNull(packingTaskEntity)){
                    result.add(BatchResultDTO.fail(id,entity.getCode(),"已生成装箱任务不可重复生成"));
                    continue;
                }
                result.add(soDeliveryNoticeService.generatePackingTask(entity));
            }catch (Exception e){
                log.error("发货通知单下推装箱任务失败>>>>>", e);
                result.add(BatchResultDTO.fail(entity.getId(),entity.getCode(),e.getMessage()));
            }
        }
        return result.stream().allMatch(BatchResultDTO::getSuccess) ? success(result) : failure(result);
    }


    /**
     * 库存数据修复
     * @author will
     * @date 2024/7/31 19:35
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/handleErrorData")
    public ApiResult<List<BatchResultDTO>> handleErrorData(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO receiverResult;
            try {
                receiverResult = soDeliveryNoticeService.handleErrorData(id);
            } catch (Exception e) {
                log.error("处理数据", e);
                SoDeliveryNoticeEntity entity = soDeliveryNoticeService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    receiverResult = BatchResultDTO.fail(id, entity.getCode(), "发货通知单不存在, 处理数据失败");
                    resultDTOS.add(receiverResult);
                    continue;
                }
                receiverResult = BatchResultDTO.fail(id, entity.getCode(), e.getMessage());
            }
            resultDTOS.add(receiverResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量修改中转仓库
     * @author zdy
     * @date:  2024-10-24
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/updateTransferWarehouse")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "批量修改中转仓库")
    public ApiResult<List<BatchResultDTO>> updateTransferWarehouse(@RequestBody @Validated BaseIdsDTO.ChangeDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<SoDeliveryNoticeEntity> entityList = soDeliveryNoticeService.listByIds(ids);
        for (String id : ids) {
            BatchResultDTO resultDTO;
            SoDeliveryNoticeEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"发货通知单记录不存在"));
                continue;
            }
            try {
                resultDTO = soDeliveryNoticeService.updateTransferWarehouse(entity, dto.getChangeIds());
            }catch (Exception e){
                log.error("发货通知单修改中转仓库失败",e);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "发货通知单不存在, 修改中转仓库失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 下推加工单保存
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "下推加工单保存")
    @PostMapping(value = "/generateMachineInfo")
    public ApiResult generateMachineInfo(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result =  soDeliveryNoticeService.generateMachineInfo(dto.getIds());
        return result ? success():failure();
    }

    /**
     * 打印客户SKU标签预览
     *
     */
    @PostMapping(value = "/printSkuLabelView")
    public ApiResult<List<SoDeliveryNoticeDTO.PrintSkuLabelDTO>> printSkuLabelView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<SoDeliveryNoticeDTO.PrintSkuLabelDTO> printSkuLabelDTOList = soDeliveryNoticeService.printSkuLabelView(dto.getIds());
        return success(printSkuLabelDTOList);
    }
    /**
     * 打印客户SKU标签确认
     *
     */
    @PostMapping(value = "/printSkuLabelConfirm")
    public void printSkuLabelConfirm(@RequestBody @Validated SoDeliveryNoticeDTO.PrintSkuLabelConfirmDTO dto , HttpServletResponse response) {
        soDeliveryNoticeService.printSkuLabelConfirm(dto, response);
    }
}

