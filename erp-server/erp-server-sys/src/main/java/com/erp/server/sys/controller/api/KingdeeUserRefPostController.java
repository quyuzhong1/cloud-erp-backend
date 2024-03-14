package com.erp.server.sys.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.server.sys.query.KingdeeUserQueryHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
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
import com.erp.server.sys.service.KingdeeUserRefPostService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.KingdeeUserRefPostDTO;

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
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated KingdeeUserRefPostDTO.AddDTO dto) {
        return success(kingdeeUserRefPostService.add(dto));
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
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "sys:kingdeeUserRefPost:update",
        serviceClass = KingdeeUserRefPostService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated KingdeeUserRefPostDTO.UpdateDTO dto) {
        kingdeeUserRefPostService.update(dto);
        return success();
    }



}
