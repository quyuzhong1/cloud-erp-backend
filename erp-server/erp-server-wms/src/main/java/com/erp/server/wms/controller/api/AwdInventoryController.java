package com.erp.server.wms.controller.api;


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
import com.erp.server.wms.service.AwdInventoryService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.AwdInventoryDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.wms.entity.AwdInventoryEntity;

/**
 * 
 *
 * @author wtr
 * @since 2025-12-26
 */
@Slf4j
@RestController
@LogSystemModule("")
@RequestMapping("/awdInventory")
public class AwdInventoryController extends BaseController {

    @Resource
    private AwdInventoryService awdInventoryService;

    /**
    * 列表查询
    * @author wtr
    * @date: 2025-12-26
    * @param dto
    * @return ApiResult<PagingVO<AwdInventoryDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:awdInventory:paging",
            tableAlias = "ai"
    )
    public ApiResult<PagingVO<AwdInventoryDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AwdInventoryDTO.PagingParamDTO> dto) {
        return success(awdInventoryService.paging(dto));
    }

    /**
    * 导出Excel数据
    * @author wtr
    * @date:  2025-12-26
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:awdInventory:export",
            tableAlias = "ai"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    public void exportList(@RequestBody @Validated AwdInventoryDTO.ExportDTO dto, HttpServletResponse response) {
        awdInventoryService.exportList(dto, response);
    }


}
