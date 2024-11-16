package com.erp.server.bi.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.core.utils.ExcelUtil;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.dto.excel.TargetShopSettingExportExcelDTO;
import com.erp.server.bi.convert.BiExportConverter;
import com.erp.server.bi.service.BiTargetShopSettingService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.bi.dto.BiTargetShopSettingDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 目标管理-店铺
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@RestController
@LogSystemModule("目标管理")
@RequestMapping("/biTargetShopSetting")
public class BiTargetShopSettingController extends BaseController {

    @Resource
    private BiTargetShopSettingService biTargetShopSettingService;


    /**
     * 分页
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-09-13
     */
    @PostMapping("/paging")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "create_user_id",
//            menuCode = "bi:biTargetStaffSetting:paging",
//            tableAlias = ""
//    )
    public ApiResult<PagingVO<BiTargetShopSettingDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<BiTargetYearDTO.PagingParamDTO> dto) {
        PagingVO<BiTargetShopSettingDTO.PagingViewDTO> pagingVO = biTargetShopSettingService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 按照店铺导出目标
     * @param dto
     * @return
     */
    @PostMapping("/export")
    public Boolean export(@RequestBody @Validated PagingDTO<BiTargetYearDTO.PagingParamDTO> dto, HttpServletResponse response) {
        dto.setPageSize(1000);
        dto.setCurrPage(1);
        PagingVO<BiTargetShopSettingDTO.PagingViewDTO> pagingVO = biTargetShopSettingService.paging(dto);
        List<BiTargetShopSettingDTO.PagingViewDTO> list = (List<BiTargetShopSettingDTO.PagingViewDTO>) pagingVO.getList();
        List<TargetShopSettingExportExcelDTO> excels = BiExportConverter.INSTANCE.exportShopTargetStaff(list);
        //数据转换
        ExcelUtil.export("按店铺导出目标报表", "店铺", excels, TargetShopSettingExportExcelDTO.class, response);
        return Boolean.TRUE;
    }
    /**
     * 分页统计
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-09-13
     */
    @PostMapping("/pagingTotal")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "create_user_id",
//            menuCode = "bi:biTargetStaffSetting:paging",
//            tableAlias = ""
//    )
    public ApiResult<BiTargetYearDTO.PagingTotalDTO> pagingTotal(@RequestBody @Validated BiTargetYearDTO.PagingParamDTO dto) {
        BiTargetYearDTO.PagingTotalDTO totalDTO = biTargetShopSettingService.pagingTotal(dto);
        return success(totalDTO);
    }

    /**
    * 新增
    * @author Lambda
    * @date:  2023-09-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "店铺目标设置添加")
    public ApiResult<String> add(@RequestBody @Validated BiTargetShopSettingDTO.AddDTO dto) {
        return success(biTargetShopSettingService.add(dto));
    }


    /**
     * 详情
     *
     * @param id
     * @return
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<BiTargetShopSettingDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        BiTargetShopSettingDTO.ViewDTO view = biTargetShopSettingService.view(id);
        return success(view);
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2023-09-13
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "店铺目标设置修改")
//        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//        tableField = "create_user_id",
//        menuCode = "dmp:biTargetShopSetting:update",
//        serviceClass = BiTargetShopSettingService.class,
//        keyIdName = "id")
    public ApiResult<Void> update(@RequestBody @Validated BiTargetShopSettingDTO.UpdateDTO dto) {
        biTargetShopSettingService.update(dto);
        return success();
    }


    /**
     * 下载模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载目标管理店铺模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Void> downloadTemplate(HttpServletResponse response) {
        biTargetShopSettingService.downloadTemplate(response);
        return success();
    }

    /**
     * 导入店铺目标设置
     *
     * @return
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入目标管理店铺模板")
    @PostMapping("/importFile")
    public ApiResult<BiTargetShopSettingDTO.ImportDTO> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        BiTargetShopSettingDTO.ImportDTO result = biTargetShopSettingService.importFile(excelFile, response);
        return success(result);
    }


    /**
     * 分页列表删除
     *
     * @return
     */
    @PostMapping("/remove")
    @LogAction(value = LogActionEnum.DELETE, desc = "店铺目标设置删除")
    public ApiResult<Object> remove(@RequestBody @Validated  BiTargetShopSettingDTO.RemoveDTO dto) {
        boolean result = biTargetShopSettingService.delete(dto);
        return result ? success() : failure();
    }

}
