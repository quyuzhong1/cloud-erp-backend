package com.erp.server.dmp.controller.api;


import java.util.List;

import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.CfgDiffStrategyDTO;
import com.erp.server.dmp.query.CfgDiffStrategyQueryHandler;
import com.erp.server.dmp.service.CfgDiffStrategyService;

import lombok.extern.slf4j.Slf4j;

/**
 * 差异策略配置基础信息
 *
 * @author shukai
 * @since 2025-11-11
 */
@Slf4j
@RestController
@LogSystemModule("差异策略配置基础信息")
@RequestMapping("/cfgDiffStrategy")
public class CfgDiffStrategyController extends BaseController {

    @Resource
    private CfgDiffStrategyService cfgDiffStrategyService;

    /**
     * 获取 tab列表
     * @Author Luo_WG
     * @Date 2024/9/3 15:15
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.dmp.dto.DmpOutputTaskDTO.TabListDTO>>
     **/
    @PostMapping("/tabList")
    public ApiResult<List<CfgDiffStrategyDTO.TabListDTO>> tabList(@RequestBody @Validated PermissionsDTO dto) {
        return success(cfgDiffStrategyService.tabList(dto));
    }
    
    /**
     * 分页查询
     * 菜单code = dmp:cfgDiffStrategy:paging
     * tab=all为全部，able为启用，disable为停用
     * @author Will
     * @date: 2023/11/13 15:12
     * @param dto
     * @return ApiResult<PagingVO<CfgDiffStrategyDTO.ViewDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = CfgDiffStrategyQueryHandler.class)
    public ApiResult<PagingVO<CfgDiffStrategyDTO.ViewDTO>> paging(@RequestBody @Validated PagingDTO<CfgDiffStrategyDTO.PagingParamDTO> dto) {
        return success(cfgDiffStrategyService.paging(dto));
    }
    
    /**
     *  导出Excel
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "差异策略配置基础信息导出")
    @PostMapping(value = "/exportExcel")
    public ApiResult<Boolean> exportExcel(@RequestBody @Validated CfgDiffStrategyDTO.ExpotParamDTO dto) {
        return success(cfgDiffStrategyService.exportExcel(dto));
    }

    
    /**
    * 新增
    * @author shukai
    * @date:  2025-11-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "差异策略配置基础信息新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgDiffStrategyDTO.AddDTO dto) {
        return success(cfgDiffStrategyService.add(dto));
    }

    /**
    * 编辑
    * @author shukai
    * @date:  2025-11-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "差异策略配置基础信息编辑")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:cfgDiffStrategy:update",
        serviceClass = CfgDiffStrategyService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgDiffStrategyDTO.UpdateDTO dto) {
        cfgDiffStrategyService.update(dto);
        return success();
    }

    /**
     * 批量操作
     * @author shukai
     * @date:  2025-11-11
     * @param dto
     * @return ApiResult
     */
     @PostMapping("/batchOp")
     @LogAction(value = LogActionEnum.UPDATE, desc = "差异策略配置基础信息批量操作")
         @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
         tableField = "create_user_id",
         menuCode = "dmp:cfgDiffStrategy:batchOp",
         serviceClass = CfgDiffStrategyService.class,
         keyIdName = "id")
     public ApiResult<?> batchOp(@RequestBody @Validated CfgDiffStrategyDTO.BatchOpDTO dto) {
    	 cfgDiffStrategyService.batchOp(dto);
    	 return success();
     }

     /**
      * 展示
      * @author shukai
      * @date:  2025-11-11
      * @param dto
      * @return ApiResult
      */
      @PostMapping("/view")
      public ApiResult<CfgDiffStrategyDTO.UpdateDTO> view(@RequestBody @Validated BaseIdDTO dto) {
          return success(cfgDiffStrategyService.view(dto.getId()));
      }
}
