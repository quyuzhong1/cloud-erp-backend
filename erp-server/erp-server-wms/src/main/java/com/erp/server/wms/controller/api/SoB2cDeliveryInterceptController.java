package com.erp.server.wms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.entity.SoB2cDeliveryInterceptEntity;
import com.erp.server.wms.service.SoB2cDeliveryService;
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
import com.erp.server.wms.service.SoB2cDeliveryInterceptService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * b2c发货拦截单
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
@Slf4j
@RestController
@LogSystemModule("b2c发货拦截单")
@RequestMapping("/soB2cDeliveryIntercept")
public class SoB2cDeliveryInterceptController extends BaseController {

    @Resource
    private SoB2cDeliveryInterceptService soB2cDeliveryInterceptService;

    /**
    * 新增
    * @author Luo_WG
    * @date:  2023-12-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "b2c发货拦截单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SoB2cDeliveryInterceptDTO.AddDTO dto) {
        return success(soB2cDeliveryInterceptService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2023-12-13
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "b2c发货拦截单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:soB2cDeliveryIntercept:update",
        serviceClass = SoB2cDeliveryInterceptService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SoB2cDeliveryInterceptDTO.UpdateDTO dto) {
        soB2cDeliveryInterceptService.update(dto);
        return success();
    }

    /**
     * 获取状态统计
     * @Author Luo_WG
     * @Date 2023/12/14 10:01
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO.TabListDTO>>
     **/
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:soB2cDeliveryIntercept:paging",
            tableAlias = "sbdi"
    )
    public ApiResult<List<SoB2cDeliveryInterceptDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(soB2cDeliveryInterceptService.tabList(dto));
    }

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/12/14 11:02
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO.ListDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:soB2cDeliveryIntercept:paging",
            tableAlias = "sbdi"
    )
    public ApiResult<PagingVO<SoB2cDeliveryInterceptDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SoB2cDeliveryInterceptDTO.PagingParamDTO> dto) {
        return success(soB2cDeliveryInterceptService.paging(dto));
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
            menuCode = "wms:soB2cDeliveryIntercept:view",
            serviceClass = SoB2cDeliveryInterceptService.class,
            keyIdName = "id")
    public ApiResult<SoB2cDeliveryInterceptDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(soB2cDeliveryInterceptService.view(id));
    }

    /**
     * 物流拦截
     * @Author Luo_WG
     * @Date 2023/12/14 11:33
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>
     **/
    @PostMapping("/logisticsIntercept")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soB2cDeliveryIntercept:logisticsIntercept",
            serviceClass = SoB2cDeliveryInterceptService.class,
            keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> logisticsIntercept(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cDeliveryInterceptService.logisticsIntercept(id);
            }catch (Exception e){
                log.error("物流拦截单 物流拦截失败",e);
                SoB2cDeliveryInterceptEntity entity = soB2cDeliveryInterceptService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "物流拦截单不存在, 物流拦截失败");
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
     * 拦截结果确认
     * @Author Luo_WG
     * @Date 2023/12/14 11:45
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>
     **/
    @PostMapping("/interceptResultConfirm")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:soB2cDeliveryIntercept:interceptResultConfirm",
            serviceClass = SoB2cDeliveryInterceptService.class,
            keyIdName = "id")
    public ApiResult<List<BatchResultDTO>> interceptResultConfirm(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = soB2cDeliveryInterceptService.interceptResultConfirm(id);
            }catch (Exception e){
                log.error("物流拦截单 拦截结果确认失败",e);
                SoB2cDeliveryInterceptEntity entity = soB2cDeliveryInterceptService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "物流拦截单不存在, 拦截结果确认失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
