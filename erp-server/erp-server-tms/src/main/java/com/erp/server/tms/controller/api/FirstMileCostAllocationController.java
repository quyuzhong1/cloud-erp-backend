package com.erp.server.tms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.FirstMileCostAllocationDTO;
import com.erp.model.tms.entity.FirstMileCostAllocationEntity;
import com.erp.server.tms.query.FirstMileCostAllocationQueryHandler;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.FirstMileCostAllocationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 头程费用分摊
 *
 * @author zdy
 * @since 2024-08-20
 */
@Slf4j
@RestController
@LogSystemModule("头程费用分摊")
@RequestMapping("/firstMileCostAllocation")
public class FirstMileCostAllocationController extends BaseController {

    @Resource
    private FirstMileCostAllocationService firstMileCostAllocationService;

    /**
    * 新增
    * @author zdy
    * @date:  2024-08-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "头程费用分摊新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated FirstMileCostAllocationDTO.AddDTO dto) {
        return success(firstMileCostAllocationService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2024-08-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程费用分摊修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:firstMileCostAllocation:update",
        serviceClass = FirstMileCostAllocationService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated FirstMileCostAllocationDTO.UpdateDTO dto) {
        firstMileCostAllocationService.update(dto);
        return success();
    }

    /**
     * tab 列表
     *
     * @param dto
     * @author zdy
     * @date 2024-8-13 10:54
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:firstMileCostAllocation:paging",
            tableAlias = "lb"
    )
    public ApiResult<List<FirstMileCostAllocationDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<FirstMileCostAllocationDTO.TabListDTO> tabList = firstMileCostAllocationService.tabList(dto);
        return success(tabList);
    }


    /**
     * 分页
     *
     * @param dto
     * @author zdy
     * @date 2024-8-13 10:54
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:firstMileCostAllocation:paging",
            tableAlias = "a"
    )
    @WebAdvanceQuery(handler = FirstMileCostAllocationQueryHandler.class)
    public ApiResult<PagingVO<FirstMileCostAllocationDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<FirstMileCostAllocationDTO.PagingParamDTO> dto) {
        PagingVO<FirstMileCostAllocationDTO.PagingVO> pagingVO = firstMileCostAllocationService.paging(dto);
        return success(pagingVO);
    }
    /**
     * 删除记录
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:firstMileCostAllocation:submit",
            serviceClass = FirstMileCostAllocationService.class,
            keyIdName = "ids"
    )
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds().stream().distinct().collect(Collectors.toList());
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<FirstMileCostAllocationEntity> entityList = firstMileCostAllocationService.listByIds(ids);
        for (String id : ids) {
            FirstMileCostAllocationEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"SKU成本记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(firstMileCostAllocationService.delete(entity));
            }catch (Exception e){
                log.error("SKU成本记录删除失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getReconciliationCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
    /**
     * 导出Excel
     *
     * @param dto
     * @author zdy
     * @date 2024-8-15 10:54
     */
    @PostMapping("/exportExcel")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "create_user_id",
//            menuCode = "tms:firstMileCostAllocation:exportExcel",
//            tableAlias = "a"
//    )
    @WebAdvanceQuery(handler = FirstMileCostAllocationQueryHandler.class)
    public ApiResult<?> exportExcel(@RequestBody @Valid FirstMileCostAllocationDTO.PagingParamDTO dto, HttpServletResponse response) {
        firstMileCostAllocationService.exportExcel(dto, response);
        return success();
    }
}
