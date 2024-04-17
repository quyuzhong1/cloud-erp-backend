package com.erp.server.wms.controller.api;


import cn.hutool.core.util.ObjectUtil;
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
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
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
import java.util.List;

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
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:soB2cDelivery:paging",
            tableAlias = "sbd"
    )
    @WebAdvanceQuery(handler = SoB2cDeliveryQueryHandler.class)
    public ApiResult<PagingVO<SoB2cDeliveryDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SoB2cDeliveryDTO.PagingParamDTO> dto) {
        return success(soB2cDeliveryService.paging(dto));
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
                if (isManual) {
                    //生成销售出库单
                    if(isSuccess){
                        soB2cDeliveryService.generateB2cSoOutstock(entity);
                    }
                }
                //清状态
                if(isSuccess){
                    SoB2cErrorDTO.DeleteDTO deleteDTO = new SoB2cErrorDTO.DeleteDTO();
                    deleteDTO.setType(type);
                    deleteDTO.setMainId(entity.getSourceId());
                    soB2cFeign.deleteError(deleteDTO);
                }
            } catch (Exception e) {
                SoB2cDeliveryEntity entity = soB2cDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "发货单不存在, 手动发货失败");
                    resultDTOS.add(result);
                    continue;
                }else{
                    SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
                    addError.setType(type);
                    addError.setParamJson(deliveryType);
                    addError.setReturnJson("");
                    addError.setMainId(entity.getSourceId());
                    addError.setMessage(e.getMessage());
                    soB2cFeign.addSoB2cError(addError);
                    log.error("发货单发货失败", e);
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 虚假发货
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
                log.error("发货单 虚假发货失败", e);
                SoB2cDeliveryEntity entity = soB2cDeliveryService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "发货单不存在, 虚假发货失败");
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
     * 订单标记发货失败后再次触发 ids 为销售订单id
     *
     * @param dto
     * @return
     */
    @PostMapping("/retryFalseDelivery")
    public ApiResult<List<BatchResultDTO>> retryDelivery(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        String type = SoB2cErrorTypeEnum.SIGN_DELIVERY.getCode();
        //id 为销售订单id
        for (String id : dto.getIds()) {
            try {
                List<BatchResultDTO> resultList = soB2cDeliveryService.retryFalseDelivery(id);
                resultDTOS.addAll(resultList);
            } catch (Exception e) {
                log.error("发货单 虚假发货失败", e);
            }

            SoB2cErrorDTO.DeleteDTO deleteDTO = new SoB2cErrorDTO.DeleteDTO();
            deleteDTO.setType(type);
            deleteDTO.setMainId(id);
            soB2cFeign.deleteError(deleteDTO);

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
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/12/19 16:12
     **/
    @PostMapping("/printPickingCancel")
    public ApiResult printPickingCancel(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soB2cDeliveryService.printPickingCancel(dto.getIds());
        return flag ? success() : failure();
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
     * 打印物流面单确认
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List < com.erp.model.wms.dto.SoB2cDeliveryDTO.PrintLogisticsWaybillDTO>>
     * @Author Luo_WG
     * @Date 2023/12/13 20:13
     **/

    @PostMapping("/printLogisticsBillConfirm")
    public void printLogisticsBillConfirm(@RequestBody @Validated SoB2cDeliveryDTO.PrintLogisticsBillConfirmDTO dto, HttpServletResponse response) {
        soB2cDeliveryService.printLogisticsBillConfirm(dto, response);

    }
}
