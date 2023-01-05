package com.erp.server.bi.controller;

import com.erp.common.business.annotation.DataPermission;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.DataAttributeEnum;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.AdvanceSearchDTO;
import com.erp.model.dmp.dto.*;
import com.erp.server.bi.service.DmpShopChangeLogService;
import com.erp.server.bi.service.DmpShopInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * 数据源管理
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 14:42
 */
@RestController
@RequestMapping("bi/dmpShopInfo")
public class DmpShopInfoController extends BaseController {

    @Resource
    private DmpShopInfoService dmpShopInfoService;

    @Resource
    private DmpShopChangeLogService dmpShopChangeLogService;

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
        PagingVO<DmpShopInfoShowDTO> pagingVO = dmpShopInfoService.paging(dto);
        return success(pagingVO);
    }

   /**
    * 店铺数据-新增
    * @author Will
    * @date: 2022/12/15 12:55
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/add")
    public ApiResult addDmpShopInfo(@RequestBody DmpShopInfoDTO dto) {
        Boolean flag = this.dmpShopInfoService.addDmpShopInfo(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 店铺数据-编辑
     * @author Will
     * @date: 2022/12/15 12:58
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    public ApiResult updateDmpShopInfo(@RequestBody DmpShopInfoDTO dto) {
        Boolean flag = this.dmpShopInfoService.updateDmpShopInfo(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 店铺数据-查询单个店铺
     * @author Will
     * @date: 2022/12/15 16:36
     * @param id
     * @return ApiResult<DmpShopInfoDTO>
     */
    @RequestMapping("/getDmpShopInfoById")
    public ApiResult<DmpShopInfoDTO>  getDmpShopInfoById(@RequestParam("id") String id) {
        DmpShopInfoDTO dto = dmpShopInfoService.getDmpShopInfoById(id);
        return success(dto);
    }
    

    /**
     * 店铺数据-负责人变更
     * @author Will
     * @date: 2022/12/15 12:56
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/changeChargeName")
    public ApiResult changeChargeName(@RequestBody DmpShopInfoChangeDTO dto) {
        Boolean flag = this.dmpShopInfoService.changeChargeName(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 店铺数据-部门变更
     * @author Will
     * @date: 2022/12/15 13:55
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/changeDept")
    public ApiResult changeDept(@RequestBody DmpShopInfoDeptChangeDTO dto) {
        Boolean flag = this.dmpShopInfoService.changeDept(dto);
        return flag == true ? success() : failure();
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
        PagingVO<DmpShopChangeLogDTO> pagingVO = dmpShopChangeLogService.paging(dto);
        return success(pagingVO);
    }

    /**
     *  店铺数据-导出
     * @author Will
     * @date: 2022/12/15 16:45
     * @param dto
     * @param response
     */
    @PostMapping(value = "/exportExcel")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "bi:dmpShopInfo:paging", tableAlias = "dsi")
    public ApiResult exportExcel(@RequestBody DmpShopInfoSearchDTO dto, HttpServletResponse response) {
        dmpShopInfoService.exportExcel(dto, response);
        return  success();
    }
}
