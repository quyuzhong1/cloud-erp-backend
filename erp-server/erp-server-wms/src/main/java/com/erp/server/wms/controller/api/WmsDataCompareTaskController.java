package com.erp.server.wms.controller.api;


import javax.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO;
import com.erp.model.wms.dto.WmsDataCompareTaskDTO.SetNextViewDTO;
import com.erp.server.wms.service.WmsDataCompareTaskService;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据对比任务
 *
 * @author shukai
 * @since 2024-03-20
 */
@Slf4j
@RestController
@LogSystemModule("数据对比任务")
@RequestMapping("/wmsDataCompareTask")
public class WmsDataCompareTaskController extends BaseController {

    @Resource
    private WmsDataCompareTaskService wmsDataCompareTaskService;

    /**
    *数据对比-导入数据-下一步
    * @author shukai
    * @date:  2024-03-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "数据对比-导入数据-下一步")
    public ApiResult<WmsDataCompareTaskDTO.AddViewDTO> add(@RequestBody @Validated WmsDataCompareTaskDTO.AddDTO dto) {
        return success(wmsDataCompareTaskService.add(dto));
    }

    /**
     * 数据对比-下载系统数据
     * @author shukai
     * @date:  2024-03-20
     * @param dto
     * @return ApiResult<String>
     */
     @PostMapping("/downloadSystemData")
     @LogAction(value = LogActionEnum.EXPORT, desc = "数据对比-下载系统数据")
     public ApiResult<String> downloadSystemData(@RequestBody @Validated BaseIdDTO dto) {
         return success(wmsDataCompareTaskService.downloadSystemData(dto));
     }
     
     /**
      * 数据对比-对比设置-下一步
      * @author shukai
      * @date:  2024-03-20
      * @param dto
      * @return ApiResult<String>
      */
     @PostMapping("/setNext")
     @LogAction(value = LogActionEnum.SUBMIT, desc = "数据对比-对比设置-下一步")
     public ApiResult<SetNextViewDTO> setNext(@RequestBody @Validated WmsDataCompareTaskDTO.SetNextDTO dto) {
    	 return success(wmsDataCompareTaskService.setNext(dto));
     }
    
//    /**
//    * 修改
//    * @author shukai
//    * @date:  2024-03-20
//    * @param dto
//    * @return ApiResult
//    */
//    @PostMapping("/update")
//    @LogAction(value = LogActionEnum.UPDATE, desc = "数据对比任务修改")
//        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//        tableField = "create_user_id",
//        menuCode = "wms:wmsDataCompareTask:update",
//        serviceClass = WmsDataCompareTaskService.class,
//        keyIdName = "id")
//    public ApiResult<?> update(@RequestBody @Validated WmsDataCompareTaskDTO.UpdateDTO dto) {
//        wmsDataCompareTaskService.update(dto);
//        return success();
//    }

    
    /**
     * 对比报告-高级查询
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery()
    public ApiResult<PagingVO<WmsDataCompareTaskDTO.ViewDTO>> paging(@RequestBody @Validated PagingDTO<WmsDataCompareTaskDTO.PagingParamDTO> dto) {
        PagingVO<WmsDataCompareTaskDTO.ViewDTO> pagingVO = null;
        return success(pagingVO);
    }

}
