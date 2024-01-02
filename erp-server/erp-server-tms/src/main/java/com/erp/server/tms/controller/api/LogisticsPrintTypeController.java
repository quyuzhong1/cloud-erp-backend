package com.erp.server.tms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.LogisticsBillCostDTO;
import com.erp.model.tms.dto.LogisticsPrintTypeDTO;
import com.erp.model.tms.entity.LogisticsPrintTypeEntity;
import com.erp.server.tms.service.LogisticsPrintTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 面板打印设置表
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Slf4j
@RestController
@LogSystemModule("面板打印设置表")
@RequestMapping("/logisticsPrintType")
public class LogisticsPrintTypeController extends BaseController {

    @Resource
    private LogisticsPrintTypeService logisticsPrintTypeService;

    /**
     * 根据渠道id查询打印类型
     * @Author Luo_WG
     * @Date 2023/12/26 18:48
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.tms.dto.LogisticsPrintTypeDTO.ViewDTO>>
     **/
    @PostMapping("/listByChannelIds")
    public ApiResult<List<LogisticsPrintTypeDTO.ViewDTO>> listByChannelIds(@RequestBody BaseIdsDTO.IdsDTO dto) {
        List<LogisticsPrintTypeDTO.ViewDTO> logisticsPrintTypeEntities = logisticsPrintTypeService.listByChannelIds(dto.getIds());
        return success(logisticsPrintTypeEntities);
    }


}
