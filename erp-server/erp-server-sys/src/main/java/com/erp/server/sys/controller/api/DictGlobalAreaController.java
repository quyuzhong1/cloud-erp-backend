package com.erp.server.sys.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.DictGlobalAreaDTO;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.server.sys.query.DictGlobalAreaQueryHandler;
import com.erp.server.sys.query.KingdeeDepartmentQueryHandler;
import com.erp.server.sys.service.DictGlobalAreaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 地址管理-区域管理
 *
 * @author Lambda
 * @since 2023-03-21
 */
@Slf4j
@RestController
@RequestMapping("/dict/global/area")
public class DictGlobalAreaController extends BaseController {

    @Resource
    private DictGlobalAreaService dictGlobalAreaService;



    /**
     * 金蝶初始化数据
     * @return
     */
    @GetMapping("/init")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "初始化")
    public ApiResult init() {
        Boolean result = dictGlobalAreaService.init();
        return result ? success() : failure();
    }

    /**
     * 分页查询
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = DictGlobalAreaQueryHandler.class)
    public ApiResult<PagingVO<DictGlobalAreaDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<DictGlobalAreaDTO.PagingParamDTO> dto) {
        PagingVO<DictGlobalAreaDTO.PagingViewDTO> pagingVO = dictGlobalAreaService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 详情
     * @author Lambda
     * @date:  2024-03-11
     * @return ApiResult<String>
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<DictGlobalAreaDTO.ViewDTO> view(@Param("id") String id) {
        return success(dictGlobalAreaService.view(id));
    }

    /**
     * 添加地区
     *
     * @param list
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated List<DictGlobalAreaDTO.AddOrUpdateDTO> list) {
        Boolean result = dictGlobalAreaService.addOrUpdate(list);
        return result ? success() : failure();
    }

    /**
     * 修改地区
     *
     * @param dto
     * @return
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated DictGlobalAreaDTO.UpdateDTO dto) {
        Boolean result = dictGlobalAreaService.update(dto);
        return result ? success() : failure();
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
                deleteResult = dictGlobalAreaService.delete(id);
            }catch (Exception e){
                log.error("金蝶区域删除失败===>{}", e.getMessage());
                DictGlobalAreaEntity entity = dictGlobalAreaService.getById(id);
                if (Objects.isNull(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "金蝶区域不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getKingdeeCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
