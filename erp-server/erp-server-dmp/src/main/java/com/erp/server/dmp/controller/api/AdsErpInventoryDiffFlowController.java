package com.erp.server.dmp.controller.api;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.AdsErpInventoryDiffFlowDTO;
import com.erp.server.dmp.query.AdsErpInventoryDiffFlowQueryHandler;
import com.erp.server.dmp.service.AdsErpInventoryDiffFlowService;

import lombok.extern.slf4j.Slf4j;

/**
 * 第三方仓流水差异表
 *
 * @author shukai
 * @since 2025-11-14
 */
@Slf4j
@RestController
@LogSystemModule("第三方仓流水差异表")
@RequestMapping("/adsErpInventoryDiffFlow")
public class AdsErpInventoryDiffFlowController extends BaseController {

    @Resource
    private AdsErpInventoryDiffFlowService adsErpInventoryDiffFlowService;

    /**
    * 列表查询
    * @author shukai
    * @date: 2025-11-14
    * @param dto
    * @return ApiResult<PagingVO<AdsErpInventoryDiffFlowDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = AdsErpInventoryDiffFlowQueryHandler.class)
    public ApiResult<PagingVO<AdsErpInventoryDiffFlowDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffFlowDTO.PagingParamDTO> dto) {
        return success(adsErpInventoryDiffFlowService.paging(dto));
    }
    
    /**
     *  统计
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/total")
    @WebAdvanceQuery(handler = AdsErpInventoryDiffFlowQueryHandler.class)
    public ApiResult<AdsErpInventoryDiffFlowDTO.TotalDTO> total(@RequestBody @Validated PagingDTO<AdsErpInventoryDiffFlowDTO.PagingParamDTO> dto) {
        return success(adsErpInventoryDiffFlowService.total(dto));
    }
    
    /**
     * 重新生成
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "第三方仓流水差异表重新生成")
    @PostMapping(value = "/reCreate")
    public ApiResult<Boolean> reCreate(@RequestBody @Validated AdsErpInventoryDiffFlowDTO.ReCreateDTO dto) {
        return success(adsErpInventoryDiffFlowService.reCreate(dto));
    }
    
    /**
     * 修改备注
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "第三方仓流水差异表修改备注")
    @PostMapping(value = "/updateRemark")
    public ApiResult<Boolean> updateRemark(@RequestBody @Validated AdsErpInventoryDiffFlowDTO.UpdateRemarkDTO dto) {
    	return success(adsErpInventoryDiffFlowService.updateRemark(dto));
    }

    /**
     *  导出Excel
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "第三方仓流水差异表导出")
    @PostMapping(value = "/exportExcel")
    public ApiResult<Boolean> exportExcel(@RequestBody @Validated AdsErpInventoryDiffFlowDTO.ExpotParamDTO dto) {
        return success(adsErpInventoryDiffFlowService.exportExcel(dto));
    }
    
    /**
     *  导入期初
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "第三方仓流水差异表导入")
    @PostMapping(value = "/importExcel")
    public ApiResult<Boolean> importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response)  throws Exception{
        return success(adsErpInventoryDiffFlowService.importExcel(excelFile , response));
    }


}
