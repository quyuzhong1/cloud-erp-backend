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
import com.erp.server.wms.service.AwdOutstockService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.AwdOutstockDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.wms.entity.AwdOutstockEntity;

/**
 * 
 *
 * @author wtr
 * @since 2025-12-22
 */
@Slf4j
@RestController
@LogSystemModule("awd出库货件")
@RequestMapping("/awdOutstock")
public class AwdOutstockController extends BaseController {

    @Resource
    private AwdOutstockService awdOutstockService;

    /**
    * 新增
    * @author wtr
    * @date:  2025-12-22
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AwdOutstockDTO.AddDTO dto) {
        return success(awdOutstockService.add(dto));
    }

    /**
    * 更新发货时间
    * @author wtr
    * @date:  2025-12-22
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/updateBillDate")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:awdOutstock:update",
        serviceClass = AwdOutstockService.class,
        keyIdName = "id")
    public ApiResult<?> updateBillDate(@RequestBody @Validated AwdOutstockDTO.UpdateDTO dto) {
        awdOutstockService.updateBillDate(dto);
        return success();
    }

    /**
     * 下推头程发货单弹窗
     * @author wtr
     * @date:  2025-12-22
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/generateDeliveryView")
    public ApiResult<?> generateTransferView(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        awdOutstockService.generateDeliveryView(dto);
        return success();
    }

    /**
     * 下推头程发货单
     * @author wtr
     * @date:  2025-12-22
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/generateDelivery")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:awdOutstock:update",
            serviceClass = AwdOutstockService.class,
            keyIdName = "id")
    public ApiResult<?> generateDelivery(@RequestBody @Validated AwdOutstockDTO.GenerateDeliveryDTO dto) {
        awdOutstockService.generateDelivery(dto);
        return success();
    }

    /**
    * 列表查询
    * @author wtr
    * @date: 2025-12-22
    * @param dto
    * @return ApiResult<PagingVO<AwdOutstockDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:awdOutstock:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<AwdOutstockDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AwdOutstockDTO.PagingParamDTO> dto) {
        return success(awdOutstockService.paging(dto));
    }

    /**
    * 导出Excel数据
    * @author wtr
    * @date:  2025-12-22
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:awdOutstock:export",
            tableAlias = ""
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    public void exportList(@RequestBody @Validated AwdOutstockDTO.ExportDTO dto, HttpServletResponse response) {
        awdOutstockService.exportList(dto, response);
    }


}
