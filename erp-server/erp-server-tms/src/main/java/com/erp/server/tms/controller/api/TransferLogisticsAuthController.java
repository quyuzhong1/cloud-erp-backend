package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.erp.model.tms.dto.LogisticsAuthDTO;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.enums.LogisticsAuthStatusEnum;
import com.erp.server.tms.service.LogisticsAuthFieldService;
import com.erp.server.tms.service.LogisticsAuthService;
import com.erp.server.tms.service.TransferLogisticsAuthFieldService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import com.erp.server.tms.service.TransferLogisticsAuthService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.TransferLogisticsAuthDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 中转服务商授权表
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Slf4j
@RestController
@LogSystemModule("中转服务商授权表")
@RequestMapping("/transferLogisticsAuth")
public class TransferLogisticsAuthController extends BaseController {

    @Autowired
    private TransferLogisticsAuthService transferLogisticsAuthService;

    @Autowired
    private TransferLogisticsAuthFieldService transferLogisticsAuthFieldService;

    /**
     * 中转服务商授权新增
     * @Author Luo_WG
     * @Date 2024/1/19 11:29
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.dto.base.BaseResultDTO.AddDTO>
     **/
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "中转服务商授权表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsAuthDTO.AddDTO dto) {
        BaseResultDTO.AddDTO result = transferLogisticsAuthService.add(dto);
        String id = result.getId();
        if (StringUtils.isNotBlank(id)) {
            //先进行授权是否成功鉴权
            ApiResult apiResult = transferLogisticsAuthService.authLogistics(dto.getLogisticsPlatform(),dto.getFieldMap());
            if (apiResult.isSuccess()) {
                transferLogisticsAuthService.syncUpdateSaleChannel(dto.getLogisticsPlatform(),dto.getFieldMap());
            } else {
                transferLogisticsAuthService.removeById(id);
                transferLogisticsAuthFieldService.removeByAuthId(id);
                transferLogisticsAuthService.updateLogisticsAuthStatus(dto.getMainId(), LogisticsAuthStatusEnum.NOT.getCode());
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
        LogisticsAuthDTO.ViewDTO view = transferLogisticsAuthService.view(id);
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
        BaseResultDTO.UpdateDTO result=  transferLogisticsAuthService.update(dto);
        String id = result.getId();
        if (StringUtils.isNotBlank(id)) {
            //先进行授权是否成功鉴权
            ApiResult apiResult = transferLogisticsAuthService.authLogistics(dto.getLogisticsPlatform(),dto.getFieldMap());
            if (apiResult.isSuccess()) {
                transferLogisticsAuthService.syncUpdateSaleChannel(dto.getLogisticsPlatform(),dto.getFieldMap());
            } else {
                transferLogisticsAuthService.updateLogisticsAuthStatus(dto.getMainId(), LogisticsAuthStatusEnum.NOT.getCode());
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
