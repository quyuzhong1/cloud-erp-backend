package com.erp.server.sys.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.KingdeeDepartmentDTO;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.model.sys.entity.KingdeePostEntity;
import com.erp.server.sys.query.KingdeePostQueryHandler;
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
import com.erp.server.sys.service.KingdeePostService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.KingdeePostDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 金蝶架构管理-岗位分配
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@RestController
@LogSystemModule("金蝶岗位表")
@RequestMapping("/kingdeePost")
public class KingdeePostController extends BaseController {

    @Resource
    private KingdeePostService kingdeePostService;


    /**
     * 分页查询
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = KingdeePostQueryHandler.class)
    public ApiResult<PagingVO<KingdeePostDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<KingdeePostDTO.PagingParamDTO> dto) {
        PagingVO<KingdeePostDTO.PagingViewDTO> pagingVO = kingdeePostService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 根据部门获取下拉列表
     *
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2024-03-11
     */
    @GetMapping("/listByDeptId")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listByDeptId(@RequestParam("deptId") String deptId) {
        List<KingdeePostEntity> list = kingdeePostService.listByKingDeptId(deptId);
        List<BaseDropDownDTO.CommonDTO> result = list.stream().filter(d-> StringUtils.isNotBlank(d.getCode()))
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getId(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }


    /**
     * 根据组织获取下拉列表 如果传空默认为 深圳市唯迹科技有限公司
     *
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2024-03-11
     */
    @PostMapping("/listByOrgId")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listByOrgId(@RequestBody BaseIdDTO dto) {
        String id="";
         if(Objects.nonNull(dto)){
             id=dto.getId();
         }
        List<KingdeePostEntity> list = kingdeePostService.listByOrgId(id);
        List<BaseDropDownDTO.CommonDTO> result = list.stream().filter(d-> StringUtils.isNotBlank(d.getCode()))
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getId(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }


    /**
     * 初始化金蝶数据
     *
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2024-03-11
     */
    @GetMapping("/init")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "初始化")
    public ApiResult init() {
        Boolean result = kingdeePostService.init();
        return result ? success() : failure();
    }


    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2024-03-11
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "金蝶岗位表新增")
    public ApiResult add(@RequestBody @Validated KingdeePostDTO.AddDTO dto) {
        Boolean result = kingdeePostService.add(dto);
        return result ? success() : failure();
    }


    /**
     * 详情
     *
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2024-03-11
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<KingdeePostDTO.ViewDTO> view(@Param("id") String id) {
        return success(kingdeePostService.view(id));
    }

    /**
     * 修改
     *
     * @param dto
     * @return ApiResult
     * @author Lambda
     * @date: 2024-03-11
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "金蝶岗位修改")
    public ApiResult update(@RequestBody @Validated KingdeePostDTO.UpdateDTO dto) {
        Boolean result = kingdeePostService.update(dto);
        return result ? success() : failure();
    }

    /**
     * 删除
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "金蝶岗位删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = kingdeePostService.delete(id);
            } catch (Exception e) {
                log.error("金蝶岗位删除失败===>{}", e.getMessage());
                KingdeePostEntity entity = kingdeePostService.getById(id);
                if (Objects.isNull(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "金蝶岗位不存在, 删除失败");
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
