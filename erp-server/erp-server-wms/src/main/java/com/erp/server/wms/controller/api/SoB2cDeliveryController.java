package com.erp.server.wms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.*;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.DeliverTypeEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.query.SoB2cDeliveryQueryHandler;
import com.erp.server.wms.service.SoB2cDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * b2c发货单
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
@Slf4j
@RestController
@LogSystemModule("b2c发货单")
@RequestMapping("/soB2cDelivery")
public class SoB2cDeliveryController extends BaseController {

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @Resource
    private SoB2cFeign soB2cFeign;

    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author Luo_WG
     * @date: 2023-12-13
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "b2c发货单新增")
    @DataIdempotent(keyIdName = "dto.soCode",businessType = RedisKeyConstant.SO_B2C_DELIVERY_KEY)
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SoB2cDeliveryDTO.AddDTO dto) {
        Boolean addResult = soB2cDeliveryService.add(dto);
        return addResult ? success() : failure();
    }

    /**
     * 获取状态统计
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.wms.dto.SoB2cDeliveryDTO.TabListDTO>>
     * @Author Luo_WG
     * @Date 2023/12/13 18:51
     **/
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:soB2cDelivery:paging",
            tableAlias = "sbd"
    )
    public ApiResult<List<SoB2cDeliveryDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(soB2cDeliveryService.tabList(dto));
    }

    /**
     * 列表查询
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO < com.erp.model.wms.dto.SoB2cDeliveryDTO.ListDTO>>
     * @Author Luo_WG
     * @Date 2023/12/13 19:13
     **/
    @PostMapping("/paging")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "create_user_id",
//            menuCode = "wms:soB2cDelivery:paging",
//            tableAlias = "sbd"
//    )
    @WebAdvanceQuery(handler = SoB2cDeliveryQueryHandler.class)
    public ApiResult<PagingVO<SoB2cDeliveryDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SoB2cDeliveryDTO.PagingParamDTO> dto) {
        return success(soB2cDeliveryService.paging(dto));
    }
    /**
     * 导出excel
     * @Author zdy
     * @Date 2024/4/18 16:51
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出b2c发货单")
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody @Validated SoB2cDeliveryDTO.PagingParamDTO dto) {
        Boolean flag = soB2cDeliveryService.exportExcel(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 详情
     *
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.RequisitionApplicationDTO.ViewDTO>
     * @Author Luo_WG
     * @Date 2023/11/17 9:03
     **/
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soB2cDelivery:view",
            serviceClass = SoB2cDeliveryService.class,
            keyIdName = "id")
    public ApiResult<SoB2cDeliveryDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(soB2cDeliveryService.view(id));
    }

    /**
     * 发货
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.common.business.dto.base.BatchResultDTO>>
     * @Author Luo_WG
     * @Date 2023/12/13 19:25
     **/
    @PostMapping("/delivery")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soB2cDelivery:delivery",
            serviceClass = SoB2cDeliveryService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> delivery(@RequestBody SoB2cDeliveryDTO.DeliverDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        String deliveryType = dto.getType();
        Boolean isManual = DeliverTypeEnum.MANUAL.getCode().equals(deliveryType);
        String type = SoB2cErrorTypeEnum.SIGN_DELIVERY.getCode();
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cDeliveryService.delivery(id, deliveryType);
                Boolean isSuccess = result.getSuccess();
                SoB2cDeliveryEntity entity = soB2cDeliveryService.getById(id);
                if (isManual && isSuccess) {
                    //生成销售出库单
                    Boolean isOutStock = soB2cDeliveryService.pushTransferInfoError(entity);
                    if (isOutStock) {
                        soB2cDeliveryService.generateB2cSoOutstock(entity);
                    }
                }
            } catch (Exception e) {
                SoB2cDeliveryEntity entity = soB2cDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "发货单不存在, 手动发货失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 手动标发
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.common.business.dto.base.BatchResultDTO>>
     * @Author Luo_WG
     * @Date 2023/12/13 19:29
     **/
    @PostMapping("/falseDelivery")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soB2cDelivery:falseDelivery",
            serviceClass = SoB2cDeliveryService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> falseDelivery(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cDeliveryService.falseDelivery(id);
            } catch (Exception e) {
                log.error("发货单 手动标发失败", e);
                SoB2cDeliveryEntity entity = soB2cDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "发货单不存在, 手动标发失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 打印拣货单预览
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.wms.dto.SoB2cDeliveryDTO.printPickingViewDTO>>
     * @Author Luo_WG
     * @Date 2023/12/13 19:37
     **/
    @PostMapping("/printPickingView")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:soB2cDelivery:printPickingView",
            tableAlias = "sbd"
    )
    public ApiResult<List<SoB2cDeliveryDTO.PrintPickingViewDTO>> printPickingView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(soB2cDeliveryService.printPickingView(dto.getIds()));
    }

    /**
     * 打印拣货单
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/12/19 16:12
     **/
    @PostMapping("/printPicking")
    public ApiResult printPicking(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soB2cDeliveryService.printPicking(dto.getIds());
        return flag ? success() : failure();
    }

    /**
     * 取消打印拣货单
     * 1.24。2版本调整为取消打印（拣货单，物流单）
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/12/19 16:12
     **/
    @PostMapping("/printPickingCancel")
    public ApiResult printPickingCancel(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = soB2cDeliveryService.printPickingCancel(id);
            }catch (Exception e){
                log.error("取消打印拣货单 取消打印失败",e);
                SoB2cDeliveryEntity entity = soB2cDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "发货单不存在, 取消打印拣货单失败");
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
     * 打印物流面单
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.wms.dto.SoB2cDeliveryDTO.PrintLogisticsWaybillDTO>>
     * @Author Luo_WG
     * @Date 2023/12/13 20:13
     **/
    @PostMapping("/printLogisticsWaybill")
    public ApiResult<List<SoB2cDeliveryDTO.PrintLogisticsWaybillDTO>> printLogisticsWaybill(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(soB2cDeliveryService.printLogisticsWaybillView(dto.getIds()));
    }

    /**
     * 打印物流面单预览
     * @param param
     * @Author Luo_WG
     * @Date 2024/4/26 11:04
     * @return void
     **/
    @PostMapping("/printLogisticsWaybillPreview")
    public ApiResult<List<SoB2cDeliveryDTO.PrintLogisticsWaybillDTO>> printLogisticsWaybillPreview(@RequestBody @Validated SoB2cDeliveryDTO.PrintLogisticsBillConfirmParam param) {
        return success(soB2cDeliveryService.printLogisticsWaybillPreview(param));
    }


    /**
     * 打印物流面单确认
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.wms.dto.SoB2cDeliveryDTO.PrintLogisticsWaybillDTO>>
     * @Author Luo_WG
     * @Date 2023/12/13 20:13
     **/

    @PostMapping("/printLogisticsBillConfirm")
    @Idempotent
    public void printLogisticsBillConfirm(@RequestBody @Validated SoB2cDeliveryDTO.PrintLogisticsBillConfirmDTO dto, HttpServletResponse response) {
        soB2cDeliveryService.printLogisticsBillConfirm(dto, response);
    }

    /**
     * 根据发货单大于物流面单
     * @author will
     * @date 2024/7/1 18:14
     * @param dto
     * @param response
     */
    @PostMapping("/printLogisticsBillConfirmById")
    public void printLogisticsBillConfirmById(@RequestBody @Validated BaseIdDTO dto, HttpServletResponse response) {
        soB2cDeliveryService.printLogisticsBillConfirmById(dto.getId(), response);
    }


    /**
     * 完成打印
     * @author Will
     * @date: 2024/4/17 10:44
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/finishPrint")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "完成打印")
    public ApiResult<List<BatchResultDTO>> finishPrint(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = soB2cDeliveryService.finishPrint(id);
            }catch (Exception e){
                log.error("委外发料单 提交审核失败",e);
                SoB2cDeliveryEntity entity = soB2cDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "发货单不存在, 完成打印失败");
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
     * 物流拦截
     */
    @PostMapping("/logisticsIntercept")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soB2cDelivery:logisticsIntercept",
            serviceClass = SoB2cDeliveryService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> logisticsIntercept(@RequestBody BaseIdsDTO.IdsDTO idsDTO) {
        List<BatchResultDTO> resultDTOS = soB2cDeliveryService.logisticsIntercept(idsDTO.getIds());
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 拦截结果确认
     * @Author Luo_WG
     * @Date 2023/12/14 11:45
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>
     **/
    @PostMapping("/interceptResultConfirm")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soB2cDelivery:interceptResultConfirm",
            serviceClass = SoB2cDeliveryService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> interceptResultConfirm(@RequestBody SoB2cDeliveryInterceptDTO.InterceptResultConfirmDTO dto) {
        List<BatchResultDTO> resultDTOS = soB2cDeliveryService.interceptResultConfirm(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 生成波次
     * @param dto 参数
     * @see BaseResultDTO.AddDTO
     */
    @PostMapping("/generationWaves")
    public ApiResult<List<BaseResultDTO.AddDTO>> generationWaves(@RequestBody @Validated SoB2cDeliveryDTO.GenerationWavesDTO dto){
        List<BaseResultDTO.AddDTO> result = soB2cDeliveryService.generationWaves(dto);
        return ApiResult.success(result);
    }

    /**
     * 清除异常
     * @param dto 参数
     * @see BaseResultDTO.AddDTO
     */
    @PostMapping("/batchClearException")
    public ApiResult<List<BatchResultDTO>> batchClearException(@RequestBody @Validated BaseIdsDTO.IdsDTO dto){
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        for (String id : new HashSet<>(dto.getIds())) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = soB2cDeliveryService.clearException(id);
            }catch (Exception e){
                log.error("清除异常失败",e);
                SoB2cDeliveryEntity entity = soB2cDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "发货单不存在, 清除异常失败");
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
     * 取消发货
     * @param dto 参数
     * @see BaseResultDTO.AddDTO
     */
    @PostMapping("/batchCancelShipment")
    public ApiResult<List<BatchResultDTO>> batchCancelShipment(@RequestBody @Validated SoB2cDeliveryDTO.CancelShipmentView dto){
        Map<String, List<SoB2cDeliveryDTO.CancelShipmentDTO>> collect = dto.getCancelShipments().stream().collect(Collectors.groupingBy(SoB2cDeliveryDTO.CancelShipmentDTO::getId));
        List<BatchResultDTO> resultDTOS = new ArrayList<>(collect.keySet().size());
        for (Map.Entry<String, List<SoB2cDeliveryDTO.CancelShipmentDTO>> entry : collect.entrySet()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = soB2cDeliveryService.cancelShipment(entry.getKey(), entry.getValue());
            }catch (Exception e){
                log.error("取消发货失败",e);
                SoB2cDeliveryEntity entity = soB2cDeliveryService.getById(entry.getKey());
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), "发货单不存在, 取消发货失败");
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
     * 取消发货弹窗
     * @param dto 参数
     * @see BaseResultDTO.AddDTO
     */
    @PostMapping("/cancelShipmentView")
    public ApiResult<SoB2cDeliveryDTO.CancelShipmentView> cancelShipmentView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto){
        SoB2cDeliveryDTO.CancelShipmentView view = soB2cDeliveryService.cancelShipmentView(dto.getIds());
        return success(view);
    }


    /**
     * 重新出库
     * @author will
     * @date 2024/7/12 15:47
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/retryOutstock")
    public ApiResult<List<BatchResultDTO>> retryOutstock(@RequestBody @Validated BaseIdsDTO.IdsDTO dto){
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = soB2cDeliveryService.retryOutstock(id);
                if (resultDTO.getSuccess()) {
                    SoB2cDeliveryEntity entity = soB2cDeliveryService.getById(id);
                    entity.setBatchNo(resultDTO.getId());
                    soB2cDeliveryService.generateB2cSoOutstock(entity);
                }
            }catch (Exception e){
                log.error("重新出库失败",e);
                SoB2cDeliveryEntity entity = soB2cDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "发货单不存在, 重新出库失败");
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
                receiverResult = soB2cDeliveryService.handleErrorData(id);
            } catch (Exception e) {
                log.error("处理数据", e);
                SoB2cDeliveryEntity entity = soB2cDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    receiverResult = BatchResultDTO.fail(id, entity.getCode(), "发货单不存在, 处理数据失败");
                    resultDTOS.add(receiverResult);
                    continue;
                }
                receiverResult = BatchResultDTO.fail(id, entity.getCode(), e.getMessage());
            }
            resultDTOS.add(receiverResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
