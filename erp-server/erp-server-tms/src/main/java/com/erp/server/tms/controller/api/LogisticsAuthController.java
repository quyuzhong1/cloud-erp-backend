package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.enums.LogisticsPlatformEnum;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.enums.LogisticsAuthStatusEnum;
import com.erp.server.tms.service.LogisticsAuthFieldService;
import com.erp.server.tms.service.LogisticsSupplierService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.LogisticsAuthService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.LogisticsAuthDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 物流商管理
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@RestController
@LogSystemModule("物流授权表")
@RequestMapping("/logisticsAuth")
public class LogisticsAuthController extends BaseController {

    @Autowired
    private LogisticsAuthService logisticsAuthService;

    @Autowired
    private LogisticsAuthFieldService logisticsAuthFieldService;

    @Autowired
    private LogisticsSupplierService logisticsSupplierService;

    /**
     * 物流授权新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-11-02
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流授权表新增")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsAuthDTO.AddDTO dto) {
        Map<String, String> authMap = dto.getFieldMap();
        String logisticsPlatform = dto.getLogisticsPlatform();
        if (LogisticsPlatformEnum.SHOPEE.getCode().equals(logisticsPlatform)
                ||LogisticsPlatformEnum.TIK_TOK.getCode().equals(logisticsPlatform)
                ||LogisticsPlatformEnum.TIK_TOK_FULLY.getCode().equals(logisticsPlatform)
                ||LogisticsPlatformEnum.MERCADOLIBRE_LOCAL.getCode().equals(logisticsPlatform)
                ||LogisticsPlatformEnum.MERCADOLIBRE.getCode().equals(logisticsPlatform)
                ||LogisticsPlatformEnum.AMZ_MULTI_CHANNEL.getCode().equals(logisticsPlatform)){
            authMap = logisticsAuthService.addShopAuth(authMap, logisticsPlatform);
        }
        BaseResultDTO.AddDTO result = logisticsAuthService.add(dto);
        String id = result.getId();
        if (StringUtils.isNotBlank(id)) {
            //先进行授权是否成功鉴权
            authMap.put("id",id);
            ApiResult apiResult = logisticsAuthService.authLogistics(logisticsPlatform,authMap);
            if (apiResult.isSuccess()) {
                logisticsAuthService.syncUpdateSaleChannel(logisticsPlatform,authMap);
            } else {
               logisticsAuthService.removeById(id);
               logisticsAuthFieldService.removeByAuthId(id);
               logisticsAuthService.updateLogisticsAuthStatus(dto.getMainId(),LogisticsAuthStatusEnum.NOT.getCode());
               return apiResult;
            }
        }
        return success(result);
    }


    /**
     * 物流授权详情
     *
     * @param id
     * @return ApiResult
     * @author Lambda
     * @date: 2023-11-02
     */
    @LogViewService
    @GetMapping("/view")
    public ApiResult<LogisticsAuthDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        LogisticsAuthDTO.ViewDTO view = logisticsAuthService.view(id);
        return success(view);
    }

    /**
     * 物流授权修改
     *
     * @param dto
     * @return ApiResult
     * @author Lambda
     * @date: 2023-11-02
     */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:logisticsAuth:update",
            serviceClass = LogisticsAuthService.class,
            keyIdName = "id")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流商授权更新")
    public ApiResult update(@RequestBody @Validated LogisticsAuthDTO.UpdateDTO dto) {
        BaseResultDTO.UpdateDTO result=  logisticsAuthService.update(dto);
        Map<String, String> authMap = dto.getFieldMap();
        String logisticsPlatform = dto.getLogisticsPlatform();
        String id = result.getId();
        if (StringUtils.isNotBlank(id)) {
            if (LogisticsPlatformEnum.SHOPEE.getCode().equals(logisticsPlatform)){
                authMap = logisticsAuthService.addShopAuth(authMap, logisticsPlatform);
            }
            //先进行授权是否成功鉴权
            authMap.put("id",id);
            ApiResult apiResult = logisticsAuthService.authLogistics(logisticsPlatform,authMap);
            if (apiResult.isSuccess()) {
                logisticsAuthService.syncUpdateSaleChannel(logisticsPlatform,authMap);
            } else {
                logisticsAuthService.removeById(id);
                logisticsAuthService.updateLogisticsAuthStatus(dto.getMainId(), LogisticsAuthStatusEnum.NOT.getCode());
                return apiResult;
            }
        }
        return success(result);
    }

    /**
     * 取消授权
     *
     * @return
     */
    @PostMapping("/cancel")
    @LogAction(value = LogActionEnum.CANCEL, desc = "物流商取消授权")
    public ApiResult<List<BatchResultDTO>> cancel(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = logisticsAuthService.cancel(id);
            } catch (Exception e) {
                log.error("物流商取消授权失败{}", e);
                LogisticsAuthEntity entity = logisticsAuthService.getByMainId("",id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "物流商授权不存在, 取消授权失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getId(), entity.getName(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }

        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);

    }


}
