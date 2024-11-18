package com.erp.server.wms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.OverseasWarehouseInboundAddressDTO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.server.wms.service.OverseasWarehouseInboundAddressService;
import com.erp.server.wms.service.OverseasWarehouseInboundService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 海外入库单常用揽收地址
 *
 * @author Jim
 * @since 2023-12-04
 */
@RestController
@RequestMapping("/overseasWarehouseInboundAddress")
public class OverseasWarehouseInboundAddressController extends BaseController {

    @Resource
    private OverseasWarehouseInboundAddressService overseasWarehouseInboundAddressService;


    /**
     * 新增
     *
     * @param dto
     * @return ApiResult
     * @author Jim
     * @date: 2023-12-04
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "海外入库单常用揽收地址-新增")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasWarehouseInboundAddress:update",
            serviceClass = OverseasWarehouseInboundAddressService.class,
            keyIdName = "owia")
    public ApiResult add(@RequestBody @Validated OverseasWarehouseInboundAddressDTO.AddDTO dto) {
        overseasWarehouseInboundAddressService.add(dto);
        return success();
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author Jim
     * @date: 2023-12-04
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "海外入库单常用揽收地址-修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasWarehouseInboundAddress:update",
            serviceClass = OverseasWarehouseInboundService.class,
            keyIdName = "owi")
    public ApiResult update(@RequestBody @Validated OverseasWarehouseInboundAddressDTO.UpdateDTO dto) {
        overseasWarehouseInboundAddressService.update(dto);
        return success();
    }

    /**
     * 列表
     *
     * @author Jim
     * @date: 2023-12-04
     */
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:overseasWarehouseInboundAddress:list",
            serviceClass = OverseasWarehouseInboundAddressService.class,
            keyIdName = "owia")
    @GetMapping("/list")
    public ApiResult addressList() {
        List<OverseasWarehouseInboundAddressDTO.ListDTO> resultList = overseasWarehouseInboundAddressService.addressList();
        return success(resultList);
    }


    /**
     * 删除
     *
     * @author Jim
     * @date: 2023-12-04
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "海外入库单常用揽收地址-删除")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasWarehouseInboundAddress:update",
            serviceClass = OverseasWarehouseInboundAddressService.class,
            keyIdName = "owia")
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag = overseasWarehouseInboundAddressService.deleteById(dto.getId());
        return flag ? success() : failure();
    }
}
