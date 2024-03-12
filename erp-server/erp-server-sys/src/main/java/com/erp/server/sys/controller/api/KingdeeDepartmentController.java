package com.erp.server.sys.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.server.sys.query.KingdeeDepartmentQueryHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.sys.service.KingdeeDepartmentService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.KingdeeDepartmentDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@RestController
@LogSystemModule("金蝶部门")
@RequestMapping("/kingdeeDepartment")
public class KingdeeDepartmentController extends BaseController {

    @Resource
    private KingdeeDepartmentService kingdeeDepartmentService;


    /**
     * 初始化金蝶数据
     * @author Lambda
     * @date:  2024-03-11
     * @return ApiResult<String>
     */
    @GetMapping("/init")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "初始化")
    public ApiResult init() {
        Boolean result = kingdeeDepartmentService.init();
        return result ? success() : failure();
    }

    /**
     * 分页查询
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = KingdeeDepartmentQueryHandler.class)
    public ApiResult<PagingVO<KingdeeDepartmentDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<KingdeeDepartmentDTO.PagingParamDTO> dto) {
        PagingVO<KingdeeDepartmentDTO.PagingViewDTO> pagingVO = kingdeeDepartmentService.paging(dto);
        return success(pagingVO);
    }


    /**
    * 新增
    * @author Lambda
    * @date:  2024-03-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult add(@RequestBody @Validated KingdeeDepartmentDTO.AddDTO dto) {
        Boolean result = kingdeeDepartmentService.add(dto);
        return result ? success() : failure();
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2024-03-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
    public ApiResult update(@RequestBody @Validated KingdeeDepartmentDTO.UpdateDTO dto) {
        Boolean result = kingdeeDepartmentService.update(dto);
        return result?success():failure();
    }


    /**
     * 详情
     * @author Lambda
     * @date:  2024-03-11
     * @return ApiResult<String>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<KingdeeDepartmentDTO.ViewDTO> view(@Param("id") String id) {
        return success(kingdeeDepartmentService.view(id));
    }


    /**
     * 删除
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public ApiResult<List<BatchResultDTO>>  delete(@RequestBody  @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = kingdeeDepartmentService.delete(id);
            }catch (Exception e){
                log.error("金蝶部门删除失败===>{}", e.getMessage());
                KingdeeDepartmentEntity entity = kingdeeDepartmentService.getById(id);
                if (Objects.isNull(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "金蝶部门不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getKingdeeDeptCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


}
