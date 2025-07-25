package com.erp.server.sys.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.DictCityDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.server.sys.query.DictParentBaseQueryHandler;
import com.erp.server.sys.service.DictCityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *地址管理-省份城市管理
 *
 * @author Lambda
 * @since 2023-03-21
 */
@Slf4j
@RestController
@LogSystemModule("地址管理-省份城市管理")
@RequestMapping("/dict/city")
public class DictCityController extends BaseController {

    @Resource
    private DictCityService dictCityService;




    /**
     * 省份分页
     * @param dto
     * @return
     */
    @PostMapping("/provincePaging")
    @WebAdvanceQuery(handler = DictParentBaseQueryHandler.class)
    public ApiResult<PagingVO<DictCityDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<DictCityDTO.ProvincePagingParamDTO> dto) {
        PagingVO<DictCityDTO.PagingViewDTO> pagingVO = dictCityService.provincePaging(dto);
        return success(pagingVO);
    }

    /**
     * 省份下拉
     *
     * @return
     */
    @GetMapping("/provinceList")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> provinceList() {
        List<DictCityEntity> provinceList = dictCityService.listProvince();
        List<BaseDropDownDTO.CommonDTO> result = provinceList.stream().filter(d -> StringUtils.isNotBlank(d.getKingdeeCode()))
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getId(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 省份导出
     * @param
     * @return
     */
    @PostMapping("/provinceExport")
    @LogAction(value = LogActionEnum.EXPORT, desc = "省份导出")
    @WebAdvanceQuery(handler = DictParentBaseQueryHandler.class)
    public ApiResult<Boolean> provinceExport(@RequestBody @Validated DictCityDTO.ProvincePagingParamDTO dto) {
         dictCityService.provinceExport(dto);
         return success(true);
    }

    /**
     * 添加省
     * @param
     * @return
     */
    @PostMapping("/addProvince")
    @LogAction(value = LogActionEnum.INSERT, desc = "添加省")
    public ApiResult addProvince(@RequestBody @Validated DictCityDTO.AddProvinceDTO dto) {
        Boolean result = dictCityService.addProvince(dto);
        return result ? success() : failure();
    }

    /**
     * 省详情
     * @param id
     * @return
     */
    @GetMapping("/provinceView")
    public ApiResult<DictCityDTO.ViewDTO> provinceView(@RequestParam("id") String id) {
        DictCityDTO.ViewDTO result = dictCityService.provinceView(id);
        return success(result);
    }

    /**
     * 修改省
     * @param
     * @return
     */
    @PostMapping("/updateProvince")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改省")
    public ApiResult updateProvince(@RequestBody @Validated DictCityDTO.UpdateProvinceDTO dto) {
        Boolean result = dictCityService.updateProvince(dto);
        return result ? success() : failure();
    }


    /**
     * 城市分页
     * @param dto
     * @return
     */
    @PostMapping("/cityPaging")
    @WebAdvanceQuery(handler = DictParentBaseQueryHandler.class)
    public ApiResult<PagingVO<DictCityDTO.PagingViewDTO>> cityPaging(@RequestBody @Validated PagingDTO<DictCityDTO.CityPagingParamDTO> dto) {
        PagingVO<DictCityDTO.PagingViewDTO> pagingVO = dictCityService.cityPaging(dto);
        return success(pagingVO);
    }

    /**
     * 城市导出
     * @param
     * @return
     */
    @PostMapping("/cityExport")
    @LogAction(value = LogActionEnum.EXPORT, desc = "城市导出")
    @WebAdvanceQuery(handler = DictParentBaseQueryHandler.class)
    public ApiResult<Boolean> cityExport(@RequestBody @Validated DictCityDTO.ProvincePagingParamDTO dto) {
        dictCityService.cityExport(dto);
        return success(true);
    }
    /**
     * 添加城市
     * @param
     * @return
     */
    @PostMapping("/addCity")
    @LogAction(value = LogActionEnum.INSERT, desc = "添加城市")
    public ApiResult addCity(@RequestBody @Validated DictCityDTO.AddCityDTO dto) {
        Boolean result = dictCityService.addCity(dto);
        return result ? success() : failure();
    }

    /**
     * 城市详情
     * @param
     * @return
     */
    @GetMapping("/cityView")
    public ApiResult<DictCityDTO.ViewDTO> cityView(@RequestParam("id") String id) {
        DictCityDTO.ViewDTO result = dictCityService.cityView(id);
        return success(result);
    }

    /**
     * 修改城市
     * @param
     * @return
     */
    @PostMapping("/updateCity")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改城市")
    public ApiResult updateCity(@RequestBody @Validated DictCityDTO.UpdateCityDTO dto) {
        Boolean result = dictCityService.updateCity(dto);
        return result ? success() : failure();
    }



    /**
     * 获取省份城市列表
     *
     * @param countryCode
     * @return
     */
    @GetMapping("/treeList")
    public ApiResult<List<DictCityDTO.ListDTO>> list(@RequestParam("countryCode") String countryCode) {
        List<DictCityDTO.ListDTO> list = dictCityService.listCity(countryCode);
        return success(list);
    }

    /**
     * 全部国家级联
     * @author will
     * @date 2025/7/16 14:52
     * @return ApiResult<List<ListDTO>>
     */
    @GetMapping("/countryTreeList")
    public ApiResult<List<DictCityDTO.ListDTO>> countryTreeList() {
        List<DictCityDTO.ListDTO> list = dictCityService.countryTreeList();
        return success(list);
    }

    /**
     * 删除
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除")
    public ApiResult<List<BatchResultDTO>>  delete(@RequestBody  @Valid BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = dictCityService.delete(id);
            }catch (Exception e){
                log.error("金蝶省市删除失败===>{}", e.getMessage());
                DictCityEntity entity = dictCityService.getById(id);
                if (Objects.isNull(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "省市不存在, 删除失败");
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
