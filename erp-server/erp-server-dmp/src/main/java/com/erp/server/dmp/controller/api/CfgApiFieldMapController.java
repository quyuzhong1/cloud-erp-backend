package com.erp.server.dmp.controller.api;

import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.model.dmp.dto.CfgApiFieldMapValueDTO;
import com.erp.model.dmp.vo.CfgApiFieldMapVO;
import com.erp.server.dmp.service.CfgApiFieldMapService;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * API字段映射
 * @author Will
 * @version 1.0
 * @date 2023/1/11 11:47
 */
@RestController
@RequestMapping("cfgApiFieldMap")
@LogSystemModule("API授权信息")
public class CfgApiFieldMapController extends BaseController {

    @Resource
    private CfgApiFieldMapService cfgApiFieldMapService;

    /**
     * 分页查询
     * @author Will
     * @date: 2023/1/11 12:13
     * @param dto
     * @return ApiResult<PagingVO<CfgApiFieldMapVO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<CfgApiFieldMapVO>> queryByPage(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto) {
        PagingVO<CfgApiFieldMapVO> pagingVO = cfgApiFieldMapService.paging(dto);
        return success(pagingVO);
    }


   /**
    * 新增
    * @author Will
    * @date: 2023/1/11 12:13
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult add(@RequestBody @Validated CfgApiFieldMapDTO dto) {
        Boolean flag = this.cfgApiFieldMapService.insert(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 批量新增
     * @author Will
     * @date: 2023/1/11 12:13
     * @param list
     * @return ApiResult
     */
    @PostMapping("/batchAdd")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_INSERT, desc = "批量新增")
    public ApiResult batchAdd(@RequestBody @Validated List<CfgApiFieldMapDTO> list) {
        Boolean flag = this.cfgApiFieldMapService.batchAdd(list);
        return flag == true ? success() : failure();
    }


    /**
     * 编辑
     * @author Will
     * @date: 2023/1/11 12:13
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "编辑")
    public ApiResult update(@RequestBody @Validated CfgApiFieldMapDTO dto) {
        this.cfgApiFieldMapService.update(dto);
        return success();
    }

    /**
     * 删除
     * @author Will
     * @date: 2023/1/11 12:19
     * @param ids
     * @return ApiResult
     */
    @PostMapping("/batchDelete")
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除")
    public ApiResult batchDelete(@RequestBody @Validated List<String> ids){
        this.cfgApiFieldMapService.batchDelete(ids);
        return success();
    }

    /**
     * 查询单条数据
     * @author Will
     * @date: 2023/1/11 12:23
     * @param id
     * @return ApiResult
     */
    @GetMapping("/getById")
    public ApiResult getById(@Param("id") String id){
        CfgApiFieldMapDTO dto = this.cfgApiFieldMapService.getCfgApiFieldMapById(id);
        return success(dto);
    }

    /**
     * 查询明细数据
     * @author Will
     * @date: 2023/1/11 12:23
     * @param fieldMapId
     * @return ApiResult
     */
    @GetMapping("/listDetails")
    public ApiResult listDetails(@Param("fieldMapId") String fieldMapId){
       List<CfgApiFieldMapValueDTO> list = this.cfgApiFieldMapService.listDetails(fieldMapId);
        return success(list);
    }

}
