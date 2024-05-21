package com.erp.server.dmp.controller.api;


import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.ThirdShopDTO;
import com.erp.server.dmp.service.ThirdShopService;
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
import com.erp.server.dmp.service.ThirdWarehouseService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.ThirdWarehouseDTO;

/**
 * 第三方系统仓库表
 *
 * @author hyj
 * @since 2024-05-17
 */
@Slf4j
@RestController
@LogSystemModule("第三方系统仓库表")
@RequestMapping("/thirdWarehouse")
public class ThirdWarehouseController extends BaseController {

    @Resource
    private ThirdWarehouseService thirdWarehouseService;

    /**
     * 新增
     * @author hyj
     * @date:  2024-05-17
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "第三方系统仓库表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ThirdWarehouseDTO.AddDTO dto) {
        return success(thirdWarehouseService.add(dto));
    }

    /**
     * 修改
     * @author hyj
     * @date:  2024-05-17
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "第三方系统仓库表修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "dmp:thirdWarehouse:update",
            serviceClass = ThirdWarehouseService.class,
            keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated ThirdWarehouseDTO.UpdateDTO dto) {
        thirdWarehouseService.update(dto);
        return success();
    }

    /**
     * 列表查询
     * @author Luo_WG
     * @date: 2023-08-24
     * @param dto
     * @return ApiResult<PagingVO<WarehouseLocationMoveDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:thirdShop:paging",
            serviceClass = ThirdShopService.class,
            keyIdName = "id"
    )
    public ApiResult<PagingVO<ThirdWarehouseDTO.PageDTO>> paging(@RequestBody @Validated PagingDTO<ThirdWarehouseDTO.PagingParamDTO> dto) {
        return success(thirdWarehouseService.paging(dto));
    }

    /**
     * 远程搜索
     * @author Luo_WG
     * @date: 2023-08-24
     * @param dto
     * @return ApiResult<PagingVO<WarehouseLocationMoveDTO.ListDTO>>
     */
    @PostMapping("/pagingSelect")
    public ApiResult<PagingVO<ThirdWarehouseDTO.PageSelectDTO>> pagingSelect(@RequestBody @Validated PagingDTO<ThirdWarehouseDTO.SelectDTO> dto) {
        return success(thirdWarehouseService.pagingSelect(dto));
    }

}
