package com.erp.server.tms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.LogisticsSaleChannelDTO;
import com.erp.model.tms.dto.SaleChannelDTO;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.service.LogisticsSaleChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 销售平台物流渠道表
 *
 * @author zdy
 * @since 2023-11-03
 */
@Slf4j
@RestController
@LogSystemModule("销售平台物流渠道表")
@RequestMapping("/logisticsSaleChannel")
public class LogisticsSaleChannelController extends BaseController {

    @Autowired
    private LogisticsSaleChannelService logisticsSaleChannelService;

    /**
    * 新增
    * @author zdy
    * @date:  2023-11-03
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "销售平台物流渠道表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsSaleChannelDTO.AddDTO dto) {
        return success(logisticsSaleChannelService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2023-11-03
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "销售平台物流渠道表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsSaleChannel:update",
        serviceClass = LogisticsSaleChannelService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated LogisticsSaleChannelDTO.UpdateDTO dto) {
        logisticsSaleChannelService.update(dto);
        return success();
    }

    /**
     * 根据平台类型获取渠道列表
     * @return
     */
    @PostMapping("/listByType")
    public ApiResult<List<SaleChannelDTO>> listByType(@RequestParam String platformType) {
        return success(logisticsSaleChannelService.listByType(platformType));
    }

}
