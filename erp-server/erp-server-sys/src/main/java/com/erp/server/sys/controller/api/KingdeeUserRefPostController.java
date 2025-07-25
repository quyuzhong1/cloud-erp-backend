package com.erp.server.sys.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.sys.entity.KingdeePostEntity;
import com.erp.model.sys.entity.KingdeeUserRefPostEntity;
import com.erp.server.sys.query.KingdeeOperatorQueryHandler;
import com.erp.server.sys.query.KingdeeUserPostDetailQueryHandler;
import com.erp.server.sys.query.KingdeeUserQueryHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import com.erp.server.sys.service.KingdeeUserRefPostService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.KingdeeUserRefPostDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 金蝶架构管理-员工任岗
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@RestController
@LogSystemModule("金蝶员工任岗表")
@RequestMapping("/kingdeeUserPost")
public class KingdeeUserRefPostController extends BaseController {

    @Resource
    private KingdeeUserRefPostService kingdeeUserRefPostService;




    /**
     * 根据人员id查询部门
     * @author Will
     * @date: 2023/3/27 12:10
     * @param userId
     * @return ApiResult
     */
    @GetMapping("/getDeptByUserId")
    public ApiResult<KingdeeUserRefPostEntity> getDeptByUserId(@RequestParam("userId") String userId) {
        KingdeeUserRefPostEntity entity = kingdeeUserRefPostService.getDeptByUserId(userId);
        return success(entity);
    }

    /**
     * 分页查询
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = KingdeeUserQueryHandler.class)
    public ApiResult<PagingVO<KingdeeUserRefPostDTO.PagingUserViewDTO>> paging(@RequestBody @Validated PagingDTO<KingdeeUserRefPostDTO.PagingParamDTO> dto) {
        PagingVO<KingdeeUserRefPostDTO.PagingUserViewDTO> pagingVO = kingdeeUserRefPostService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 任岗明细分页查询
     * @param dto
     * @return
     */
    @PostMapping("/detailPaging")
    @WebAdvanceQuery(handler = KingdeeUserPostDetailQueryHandler.class)
    public ApiResult<PagingVO<KingdeeUserRefPostDTO.DetailPagingViewDTO>> detailPaging(@RequestBody @Validated PagingDTO<KingdeeUserRefPostDTO.DetailPagingParamDTO> dto) {
        PagingVO<KingdeeUserRefPostDTO.DetailPagingViewDTO> pagingVO = kingdeeUserRefPostService.detailPaging(dto);
        return success(pagingVO);
    }

    /**
     * 初始化金蝶数据
     * @author Lambda
     * @date:  2024-03-11
     * @return ApiResult<String>
     */
    @GetMapping("/init")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "初始化")
    public ApiResult init() {
        Boolean result = kingdeeUserRefPostService.init();
        return result ? success() : failure();
    }

    /**
     * 员工详情
     * @author Lambda
     * @date:  2024-03-11
     * @return ApiResult<String>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<KingdeeUserRefPostDTO.UserPostViewDTO> view(@Param("id") String id) {
        return success(kingdeeUserRefPostService.view(id));
    }


    /**
    * 新增
    * @author Lambda
    * @date:  2024-03-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "金蝶员工任岗表新增")
    public ApiResult add(@RequestBody @Validated KingdeeUserRefPostDTO.AddDTO dto) {
        Boolean result = kingdeeUserRefPostService.add(dto);
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "金蝶员工任岗表修改")
    public ApiResult update(@RequestBody @Validated KingdeeUserRefPostDTO.UpdateDTO dto) {
        Boolean result = kingdeeUserRefPostService.update(dto);
        return result ? success() : failure();
    }

    /**
     * 删除
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "金蝶员工任岗表删除 ids={ids}")
    public ApiResult<List<BatchResultDTO>>  delete(@RequestBody  @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = kingdeeUserRefPostService.delete(id);
            }catch (Exception e){
                log.error("金蝶员工任岗位 删除失败===>{}", e.getMessage());
                KingdeeUserRefPostEntity entity = kingdeeUserRefPostService.getById(id);
                if (Objects.isNull(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "金蝶员工岗位不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }



}
