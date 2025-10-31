package com.erp.server.sys.controller.api;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.DictBasicAllDTO;
import com.erp.model.sys.dto.DictBasicAllDTO.ViewDTO;
import com.erp.server.sys.query.DictBasicAllQueryHandler;
import com.erp.server.sys.service.DictBasicAllService;

import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 字典表
 *
 * @author shukai
 * @since 2025-10-24
 */
@Slf4j
@RestController
@LogSystemModule("字典数据")
@RequestMapping("/dictBasicAll")
public class DictBasicAllController extends BaseController {

    @Resource
    private DictBasicAllService dictBasicAllService;

    /**
     * 获取 tab列表
     * @Author Luo_WG
     * @Date 2024/9/3 15:15
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.dmp.dto.DmpOutputTaskDTO.TabListDTO>>
     **/
    @PostMapping("/tabList")
    public ApiResult<List<DictBasicAllDTO.TabListDTO>> tabList(@RequestBody @Validated PagingDTO<DictBasicAllDTO.PagingParamDTO> dto) {
    	List<DictBasicAllDTO.TabListDTO> tabList = dictBasicAllService.tabList(dto);
        return success(tabList);
    }
    
    /**
     * 分页查询
     * 菜单code = sys:dictBasicAll:paging
     * tab=all为全部，able为启用，disable为停用
     * @author Will
     * @date: 2023/11/13 15:12
     * @param dto
     * @return ApiResult<PagingVO<DictBasicAllDTO.ViewDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = DictBasicAllQueryHandler.class)
    public ApiResult<PagingVO<DictBasicAllDTO.ViewDTO>> paging(@RequestBody @Validated PagingDTO<DictBasicAllDTO.PagingParamDTO> dto) {
        return success(dictBasicAllService.paging(dto));
    }
    
    /**
     *  导出Excel
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "字典数据导出")
    @PostMapping(value = "/exportExcel")
    public ApiResult<Boolean> exportExcel(@RequestBody @Validated PagingDTO<DictBasicAllDTO.ExpotParamDTO> dto) {
        return success(dictBasicAllService.exportExcel(dto));
    }
    
    /**
    * 字典数据新增
    * @author shukai
    * @date:  2025-10-24
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "字典数据新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DictBasicAllDTO.AddDTO dto) {
        return success(dictBasicAllService.add(dto));
    }

    /**
     * 字典数据批量编辑
     * @author shukai
     * @date:  2025-10-24
     * @param dto
     * @return ApiResult
     */
     @PostMapping("/update")
     @LogAction(value = LogActionEnum.UPDATE, desc = "字典数据编辑")
     public ApiResult<?> update(@RequestBody @Validated DictBasicAllDTO.UpdateDTO dto) {
    	 dictBasicAllService.update(dto);
         return success();
     }
    
    /**
    * 字典数据批量操作
    * @author shukai
    * @date:  2025-10-24
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/batchOp")
    @LogAction(value = LogActionEnum.UPDATE, desc = "字典数据批量操作")
    public ApiResult<?> batchOp(@RequestBody @Validated DictBasicAllDTO.BatchOpDTO dto) {
    	dictBasicAllService.batchOp(dto);
        return success();
    }

    /**
     * 获取类型
     * @author shukai
     * @date:  2025-10-24
     * @param dto
     * @return ApiResult
     */
     @PostMapping("/getType")
     public ApiResult<List<DictBasicAllDTO.TypeResponseDTO>> getType(@RequestBody @Validated DictBasicAllDTO.TypeRequestDTO dto) {
    	 String systemCode = dto.getSystemCode();
    	 PagingDTO<DictBasicAllDTO.PagingParamDTO> pageDto = new PagingDTO<>();
    	 pageDto.setPageSize(-1);
    	 DictBasicAllDTO.PagingParamDTO pDto = new DictBasicAllDTO.PagingParamDTO();
    	 Map<String,String> sqlMap = new HashMap<>();
    	 sqlMap.put("default", " 1 = 1 ");
    	 pDto.setSqlMap(sqlMap);
    	 AdvanceQueryDTO advanceQueryDTO = new AdvanceQueryDTO();
    	 advanceQueryDTO.setField("systemCode");
    	 advanceQueryDTO.setValue(systemCode);
    	 pDto.setAdvanceQueryDTOList(Arrays.asList(advanceQueryDTO));
    	 pageDto.setParams(pDto);
    	 PagingVO<ViewDTO> paging = dictBasicAllService.paging(pageDto);
    	 List<DictBasicAllDTO.TypeResponseDTO> result = new ArrayList<>();
    	 List<ViewDTO> list = paging.getList();
    	 if(CollUtil.isNotEmpty(list)) {
    		 list.forEach(l -> {
    			 if(l.getTypeName() == null) {
    				 l.setTypeName("");
    			 }
    		 });
    		 Map<String, String> typeMaps = list.stream().collect(Collectors.toMap(ViewDTO::getType, ViewDTO::getTypeName , (v1 , v2) -> v1));
    		 for(Map.Entry<String, String> typeMap : typeMaps.entrySet()) {
    			 DictBasicAllDTO.TypeResponseDTO r = new DictBasicAllDTO.TypeResponseDTO();
    			 r.setType(typeMap.getKey());
    			 r.setTypeName(typeMap.getValue());
    			 result.add(r);
    		 }
    	 }
         return success(result);
     }
}
