package com.erp.server.bi.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.enums.LogActionEnum;
import com.erp.model.bi.dto.AdvanceSearchDTO;
import com.erp.model.dmp.dto.*;
import com.erp.server.bi.service.BiShopChangeLogService;
import com.erp.server.bi.service.BiShopInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 数据源管理
 * @author Will
 * @version 1.0

 * @date 2022/12/14 14:42
 */
@RestController
@LogSystemModule("数据源管理")
@RequestMapping("dmpShopInfo")
public class DmpShopInfoController extends BaseController {

    @Resource
    private BiShopInfoService biShopInfoService;

    @Resource
    private BiShopChangeLogService biShopChangeLogService;

   /**
    * 店铺数据-分页查询
    * @author Will
    * @date: 2022/12/15 12:55
    * @param dto
    * @return ApiResult<PagingVO<DmpShopInfoShowDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:dmpShopInfo:paging", tableAlias = "dsi")
    public ApiResult<PagingVO<DmpShopInfoShowDTO>> queryByPage(@RequestBody @Validated PagingDTO<DmpShopInfoSearchDTO> dto) {
        PagingVO<DmpShopInfoShowDTO> pagingVO = biShopInfoService.paging(dto);
        return success(pagingVO);
    }

   /**
    * 店铺数据-新增
    * @author Will
    * @date: 2022/12/15 12:55
    * @param dto
    * @return ApiResult
    */
    @LogAction(value = LogActionEnum.INSERT, desc = "店铺数据新增")
    @PostMapping("/add")
    public ApiResult<Void> addDmpShopInfo(@RequestBody BiShopInfoDTO dto) {
        boolean flag = this.biShopInfoService.addDmpShopInfo(dto);
        return flag ? success() : failure();
    }

    /**
     * 店铺数据-编辑
     * @author Will
     * @date: 2022/12/15 12:58
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "店铺数据编辑")
    @PostMapping("/update")
    public ApiResult updateDmpShopInfo(@RequestBody BiShopInfoDTO dto) {
        boolean flag = this.biShopInfoService.updateDmpShopInfo(dto);
        return flag ? success() : failure();
    }

    /**
     * 店铺数据-查询单个店铺
     * @author Will
     * @date: 2022/12/15 16:36
     * @param id
     * @return ApiResult<DmpShopInfoDTO>
     */
    @LogViewService
    @RequestMapping("/getDmpShopInfoById")
    public ApiResult<BiShopInfoDTO>  getDmpShopInfoById(@RequestParam("id") String id) {
        BiShopInfoDTO dto = biShopInfoService.getDmpShopInfoById(id);
        return success(dto);
    }
    

    /**
     * 店铺数据-负责人变更
     * @author Will
     * @date: 2022/12/15 12:56
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "店铺数据负责人变更:店铺id={id},负责人id={chargeId}")
    @PostMapping("/changeChargeName")
    public ApiResult<Void> changeChargeName(@RequestBody DmpShopInfoChangeDTO dto) {
        boolean flag = this.biShopInfoService.changeChargeName(dto);
        return flag ? success() : failure();
    }

    /**
     * 店铺数据-部门变更
     * @author Will
     * @date: 2022/12/15 13:55
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "店铺数据部门变更:部门id={deptId},负责人id={chargeId}")
    @PostMapping("/changeDept")
    public ApiResult<Void> changeDept(@RequestBody DmpShopInfoDeptChangeDTO dto) {
        boolean flag = this.biShopInfoService.changeDept(dto);
        return flag ? success() : failure();
    }

    /**
     * 店铺数据-店铺变更记录
     * @author Will
     * @date: 2022/12/15 16:37
     * @param dto
     * @return ApiResult<DmpShopChangeLogDTO>
     */
    @PostMapping("/listDmpShopChangeLog")
    public ApiResult<PagingVO<DmpShopChangeLogDTO>> listByShopId(@RequestBody PagingDTO<AdvanceSearchDTO> dto) {
        PagingVO<DmpShopChangeLogDTO> pagingVO = biShopChangeLogService.paging(dto);
        return success(pagingVO);
    }

    /**
     *  店铺数据-导出
     * @author Will
     * @date: 2022/12/15 16:45
     * @param dto
     * @param response
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "店铺数据导出")
    @PostMapping(value = "/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:dmpShopInfo:paging", tableAlias = "dsi")
    public ApiResult<Void> exportExcel(@RequestBody DmpShopInfoSearchDTO dto, HttpServletResponse response) {
        biShopInfoService.exportExcel(dto, response);
        return  success();
    }
}
