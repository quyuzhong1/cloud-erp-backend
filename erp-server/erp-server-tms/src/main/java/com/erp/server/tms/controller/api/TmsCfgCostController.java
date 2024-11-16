package com.erp.server.tms.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.TmsCfgCostDTO;
import com.erp.model.tms.entity.TmsCfgCostEntity;
import com.erp.server.tms.query.TmsCfgCostQueryHandler;
import com.erp.server.tms.service.TmsCfgCostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 费用管理配置表
 *
 * @author will
 * @since 2024-03-15
 */
@Slf4j
@RestController
@LogSystemModule("费用管理配置表")
@RequestMapping("/tmsCfgCost")
public class TmsCfgCostController extends BaseController {

    @Resource
    private TmsCfgCostService tmsCfgCostService;


    /**
     * 分页查询
     * @author Will
     * @date: 2024/3/18 11:58
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsCfgCost:paging",
            tableAlias = "tcc"
    )
    @WebAdvanceQuery(handler = TmsCfgCostQueryHandler.class)
    public ApiResult<PagingVO<TmsCfgCostDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<TmsCfgCostDTO.PagingParamDTO> dto) {
        return success(tmsCfgCostService.paging(dto));
    }

    /**
     * 费用名称下拉
     * @date: 2024/3/22 15:52
     * @param dto
     * @return ApiResult<ViewDTO>
     */
    @PostMapping("/listDropDown")
    public ApiResult<List<TmsCfgCostDTO.DropDownDTO>> listDropDown(@RequestBody @Validated TmsCfgCostDTO.DropDownParamDTO dto) {
        return success(tmsCfgCostService.listDropDown(dto));
    }


    /**
    * 新增
    * @author will
    * @date:  2024-03-15
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "费用管理配置表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TmsCfgCostDTO.AddDTO dto) {
        return success(tmsCfgCostService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-03-15
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "费用管理配置表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:tmsCfgCost:update",
        serviceClass = TmsCfgCostService.class,
        keyIdName = "id")
    public ApiResult<Object> update(@RequestBody @Validated TmsCfgCostDTO.UpdateDTO dto) {
        tmsCfgCostService.update(dto);
        return success();
    }

    /**
     * 查看详情
     * @author Will
     * @date: 2024/3/18 9:10
     * @param id
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<TmsCfgCostDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(tmsCfgCostService.view(id));
    }

    /**
     * 删除
     * @author Will
     * @date: 2024/3/18 9:13
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "tms:tmsCfgCost:delete",
            serviceClass = TmsCfgCostService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "费用管理删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = tmsCfgCostService.delete(id);
            }catch (Exception e){
                log.error("费用管理删除失败",e);
                TmsCfgCostEntity entity = tmsCfgCostService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "费用管理数据不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getId(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

}
