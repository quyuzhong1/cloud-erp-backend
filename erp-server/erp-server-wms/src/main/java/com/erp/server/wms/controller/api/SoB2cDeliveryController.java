package com.erp.server.wms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.vo.PagingVO;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.RequisitionApplicationStatusEnum;
import com.erp.server.wms.service.RequisitionApplicationService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.SoB2cDeliveryService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;

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

    /**
    * 新增
    * @author Luo_WG
    * @date:  2023-12-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "b2c发货单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SoB2cDeliveryDTO.AddDTO dto) {
        Boolean addResult=soB2cDeliveryService.add(dto);
        return addResult?success():failure();
    }

    /**
     * 获取状态统计
     * @Author Luo_WG
     * @Date 2023/12/13 18:51
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SoB2cDeliveryDTO.TabListDTO>>
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
     * @Author Luo_WG
     * @Date 2023/12/13 19:13
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoB2cDeliveryDTO.ListDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:soB2cDelivery:paging",
            tableAlias = "sbd"
    )
    public ApiResult<PagingVO<SoB2cDeliveryDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SoB2cDeliveryDTO.PagingParamDTO> dto) {
        return success(soB2cDeliveryService.paging(dto));
    }

    /**
     * 详情
     * @Author Luo_WG
     * @Date 2023/11/17 9:03
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.RequisitionApplicationDTO.ViewDTO>
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
     * 手动发货
     * @Author Luo_WG
     * @Date 2023/12/13 19:25
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>
     **/
    @PostMapping("/manualDelivery")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soB2cDelivery:manualDelivery",
            serviceClass = SoB2cDeliveryService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> manualDelivery(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cDeliveryService.manualDelivery(id);
            }catch (Exception e){
                log.error("发货单 手动发货失败",e);
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
     * 虚假发货
     * @Author Luo_WG
     * @Date 2023/12/13 19:29
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>
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
            }catch (Exception e){
                log.error("发货单 虚假发货失败",e);
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
     * 打印拣货单预览
     * @Author Luo_WG
     * @Date 2023/12/13 19:37
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SoB2cDeliveryDTO.printPickingViewDTO>>
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
     * @Author Luo_WG
     * @Date 2023/12/19 16:12
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/printPicking")
    public ApiResult printPicking(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soB2cDeliveryService.printPicking(dto.getIds());
        return flag ? success() : failure();
    }

    /**
     * 取消打印拣货单
     * @Author Luo_WG
     * @Date 2023/12/19 16:12
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/printPickingCancel")
    public ApiResult printPickingCancel(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean flag = soB2cDeliveryService.printPickingCancel(dto.getIds());
        return flag ? success() : failure();
    }


    /**
     * 打印物流面单
     * @Author Luo_WG
     * @Date 2023/12/13 20:13
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SoB2cDeliveryDTO.PrintLogisticsWaybillDTO>>
     **/
    @PostMapping("/printLogisticsWaybill")
    public ApiResult<List<SoB2cDeliveryDTO.PrintLogisticsWaybillDTO>> printLogisticsWaybill(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(soB2cDeliveryService.printLogisticsWaybill(dto.getIds()));
    }

    /**
     * 打印配货单
     * @Author Luo_WG
     * @Date 2023/12/14 9:18
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SoB2cDeliveryDTO.PrintDistributionDTO>>
     **/
    @PostMapping("/printDistribution")
    public ApiResult<List<SoB2cDeliveryDTO.PrintDistributionDTO>> printDistribution(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        return success(soB2cDeliveryService.printDistribution(dto.getIds()));
    }
}
