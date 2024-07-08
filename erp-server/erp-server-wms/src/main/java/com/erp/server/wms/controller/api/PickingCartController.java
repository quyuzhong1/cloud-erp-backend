package com.erp.server.wms.controller.api;


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
import com.erp.model.wms.dto.PickingCartDTO;
import com.erp.model.wms.entity.PickingCartEntity;
import com.erp.server.wms.service.PickingCartService;
import com.erp.server.wms.service.SubcontractIssueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 拣货车管理
 *
 * @author will
 * @since 2024-06-20
 */
@Slf4j
@RestController
@LogSystemModule("拣货车管理")
@RequestMapping("/pickingCart")
public class PickingCartController extends BaseController {

    @Resource
    private PickingCartService pickingCartService;

    /**
     * 新增
     * @author will
     * @date:  2024-06-20
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:pickingCart:paging",
            tableAlias = "pc")
    @WebAdvanceQuery
    public ApiResult<PagingVO<PickingCartDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<PickingCartDTO.PagingParamDTO> dto) {
        PagingVO<PickingCartDTO.ListDTO> pagingVO = pickingCartService.paging(dto);
        return success(pagingVO);
    }

    /**
    * 新增
    * @author will
    * @date:  2024-06-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "拣货车管理新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated PickingCartDTO.AddDTO dto) {
        return success(pickingCartService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-06-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "拣货车管理修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:pickingCart:update",
        serviceClass = PickingCartService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated PickingCartDTO.UpdateDTO dto) {
        pickingCartService.update(dto);
        return success();
    }

    /**
     * 更新拣货车状态
     * @author will
     * @date 2024/6/24 10:53
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/updateStatus")
    @LogAction(value = LogActionEnum.UPDATE, desc = "拣货车管理状态更新")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pickingCart:updateStatus",
            serviceClass = PickingCartService.class,
            keyIdName = "id")
    public ApiResult<?> updateStatus(@RequestBody @Validated PickingCartDTO.UpdateStatusDTO dto) {
        pickingCartService.updateStatus(dto);
        return success();
    }

    /**
     * 查看详情
     * @author will
     * @date 2024/6/24 9:21
     * @param id
     * @return ApiResult<ViewDTO>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pickingCart:view",
            serviceClass = SubcontractIssueService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<PickingCartDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(pickingCartService.view(id));
    }

    /**
     * 删除
     * @author will
     * @date:  2024-06-24
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pickingCart:delete",
            serviceClass = SubcontractIssueService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "拣货车删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = pickingCartService.delete(id);
            }catch (Exception e){
                log.error("拣货车删除失败",e);
                PickingCartEntity entity = pickingCartService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "拣货车不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 远程搜索
     * 通过拣货车编号模糊查询
     */
    @GetMapping("/searchByKeyword")
    public ApiResult<List<PickingCartDTO.ViewDTO>> searchByKeyword(@RequestParam String code){
        List<PickingCartDTO.ViewDTO> list = pickingCartService.searchByKeyword(code);
        return success(list);
    }

}
