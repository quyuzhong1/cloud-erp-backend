package com.erp.server.sys.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.server.sys.query.DictGlobalAreaQueryHandler;
import com.erp.server.sys.query.DictParentBaseQueryHandler;
import com.erp.server.sys.service.DictCountryService;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 地址管理-国家管理
 *
 * @author Lambda
 * @since 2023-03-21
 */
@RestController
@RequestMapping("dict/country")
public class DictCountryController extends BaseController {


    @Resource
    private DictCountryService dictCountryService;


    /**
     * 金蝶初始化数据
     * @return
     */
    @GetMapping("/init")
    public ApiResult init() {
        Boolean result = dictCountryService.init();
        return result ? success() : failure();
    }


    /**
     * 分页查询
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = DictParentBaseQueryHandler.class)
    public ApiResult<PagingVO<DictCountryDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<DictCountryDTO.PagingParamDTO> dto) {
        PagingVO<DictCountryDTO.PagingViewDTO> pagingVO = dictCountryService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 添加国家
     *
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated DictCountryDTO.AddDTO dto) {
        Boolean result = dictCountryService.add(dto);
        return result ? success() : failure();
    }

    /**
     * 修改国家
     *
     * @param dto
     * @return
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated DictCountryDTO.UpdateDTO dto) {
        Boolean result = dictCountryService.update(dto);
        return result ? success() : failure();
    }

    /**
     * 详情
     * @author Lambda
     * @date:  2024-03-11
     * @return ApiResult<String>
     */
    @GetMapping("/view")
    public ApiResult<DictCountryDTO.ViewDTO> view(@Param("id") String id) {
        return success(dictCountryService.view(id));
    }

    /**
     * 获取国家列表
     *
     * @param
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<DictCountryDTO.ListDTO>> list() {
        List<DictCountryDTO.ListDTO> list = dictCountryService.listCountry();
        List<DictCountryDTO.ListDTO> resultList = list.stream()
                .filter(e -> !e.getDisabled())
                .collect(Collectors.toList());
        return success(resultList);
    }

    @GetMapping("/country")
    public void addCountry(@RequestParam("country") String country) {
        dictCountryService.initRegionList(country);
    }

    /**
     * 根据类型获取到区域国家列表列表
     *
     * @param type
     */
    @GetMapping("/areaCountryList")
    public ApiResult<List<DictCountryDTO.CascadeDTO>> areaCountryListByType(@RequestParam("type") String type) {
        List<DictCountryDTO.CascadeDTO> list = dictCountryService.areaCountryListByType(type);
        return success(list);
    }


    /**
     * 查询国家区域数据
     * @author Will
     * @date: 2023/11/9 9:36
     * @param dto
     * @return ApiResult<List<ListRegionDTO>>
     */
    @PostMapping("/listAreaCountry")
    public ApiResult<List<DictCountryDTO.ListRegionDTO>> listAreaCountry(@RequestBody @Validated DictCountryDTO.ListParamDTO dto) {
        List<DictCountryDTO.ListRegionDTO> list = dictCountryService.listAreaCountry(dto);
        return success(list);
    }

    /**
     * 根据参数查询国家数据
     * @author Will
     * @date: 2023/11/9 15:03
     * @param dto
     * @return ApiResult<List<ListDTO>>
     */
    @PostMapping("/listCountryByParam")
    public ApiResult<List<DictCountryDTO.ListDTO>> listCountryByParam(@RequestBody @Validated DictCountryDTO.ListParamDTO dto) {
        List<DictCountryDTO.ListDTO> list = dictCountryService.listCountryByParam(dto);
        return success(list);
    }

}
