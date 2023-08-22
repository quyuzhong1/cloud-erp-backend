package com.erp.server.wms.controller.pda;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.server.wms.service.SoOutstockService;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * PDA:销售出库单
 * @author LUO_WG
 * @since 2023-04-07
 */
@RestController
@RequestMapping("/pdaSoOutstock")
public class PdaSoOutstockController extends BaseController {

    @Resource
    private SoOutstockService soOutstockService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/8/22 11:32
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoOutstockDTO.PdaPagingViewDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoOutstock:paging",
            tableAlias = "so"
    )
    public ApiResult<PagingVO<SoOutstockDTO.PdaPagingViewDTO>> paging(@RequestBody @Validated PagingDTO<SoOutstockDTO.PdaPagingParamDTO> dto) {
        PagingVO<SoOutstockDTO.PdaPagingViewDTO> pagingVO = soOutstockService.pdaPaging(dto);
        return success(pagingVO);
    }

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:14
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.WarehouseReceiveCountDTO>>
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoOutstock:paging",
            tableAlias = "so"
    )
    public ApiResult<List<SoOutstockDTO.PdaCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<SoOutstockDTO.PdaCountDTO> warehouseReceiveCountDTOS = soOutstockService.pdaListCount(dto);
        return success(warehouseReceiveCountDTOS);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/8/22 15:16
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated SoOutstockDTO.AddDTO dto) {
        String id = soOutstockService.add(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/8/22 15:19
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:so:pdaSoOutstock:update",
            serviceClass = SoOutstockService.class,
            keyIdName = "id"
    )
    public ApiResult update(@RequestBody @Validated SoOutstockDTO.UpdateDTO dto) {
        String id = soOutstockService.updateSoOutstock(dto);
        return StringUtils.isNotBlank(id) ? success() : failure();
    }

    /**
     * 详情
     * @Author Luo_WG
     * @Date 2023/8/22 15:19
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.SoOutstockDTO.ViewDTO>
     **/
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoOutstock:view",
            serviceClass = SoOutstockService.class,
            keyIdName = "id"
    )
    public ApiResult<SoOutstockDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        SoOutstockDTO.ViewDTO view = soOutstockService.view(dto.getId());
        return success(view);
    }

    /**
     * 批量提交
     * @Author Luo_WG
     * @Date 2023/8/22 15:19
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoOutstock:submit",
            serviceClass = SoOutstockService.class,
            keyIdName = "ids"
    )
    public ApiResult submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = soOutstockService.submit(dto.getIds());
        return result ? success() : failure();
    }

    /**
     * 新增并提交
     * @Author Luo_WG
     * @Date 2023/8/22 15:20
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/addAndSubmit")
    public ApiResult addAndSubmit(@RequestBody @Validated SoOutstockDTO.AddDTO dto) {
        Boolean result = soOutstockService.addAndSubmit(dto);
        return result ? success() : failure();
    }

}
