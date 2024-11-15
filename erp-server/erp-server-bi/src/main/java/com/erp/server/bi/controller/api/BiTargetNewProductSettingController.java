package com.erp.server.bi.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.core.utils.ExcelUtil;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.dto.excel.TargetNewProductSettingExportExcelDTO;
import com.erp.server.bi.convert.BiExportConverter;
import com.erp.server.bi.service.BiTargetNewProductSettingService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.bi.dto.BiTargetNewProductSettingDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 目标管理-新品
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@RestController
@LogSystemModule("目标管理")
@RequestMapping("/biTargetNewProductSetting")
public class BiTargetNewProductSettingController extends BaseController {

    @Resource
    private BiTargetNewProductSettingService biTargetNewProductSettingService;



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
    public ApiResult<PagingVO<BiTargetNewProductSettingDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<BiTargetYearDTO.PagingParamDTO> dto) {
        PagingVO<BiTargetNewProductSettingDTO.PagingViewDTO> pagingVO = biTargetNewProductSettingService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 按照新品导出目标
     * @param dto
     * @return
     */
    @PostMapping("/export")
    public Boolean export(@RequestBody @Validated PagingDTO<BiTargetYearDTO.PagingParamDTO> dto, HttpServletResponse response) {
        dto.setPageSize(1000);
        dto.setCurrPage(1);
        PagingVO<BiTargetNewProductSettingDTO.PagingViewDTO> pagingVO = biTargetNewProductSettingService.paging(dto);
        List<BiTargetNewProductSettingDTO.PagingViewDTO> list = (List<BiTargetNewProductSettingDTO.PagingViewDTO>) pagingVO.getList();
        List<TargetNewProductSettingExportExcelDTO> excels = BiExportConverter.INSTANCE.exportNewProductTargetStaff(list);
        //数据转换
        ExcelUtil.export("按新品导出目标报表", "新品", excels, TargetNewProductSettingExportExcelDTO.class, response);
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
        BiTargetYearDTO.PagingTotalDTO totalDTO = biTargetNewProductSettingService.pagingTotal(dto);
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
    @LogAction(value = LogActionEnum.INSERT, desc = "新增目标管理新品")
    public ApiResult<String> add(@RequestBody @Validated BiTargetNewProductSettingDTO.AddDTO dto) {
        return success(biTargetNewProductSettingService.add(dto));
    }

    /**
     * 详情
     *
     * @param id
     * @return
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<BiTargetNewProductSettingDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        BiTargetNewProductSettingDTO.ViewDTO view = biTargetNewProductSettingService.view(id);
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
//        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//        tableField = "create_user_id",
//        menuCode = "dmp:biTargetNewProductSetting:update",
//        serviceClass = BiTargetNewProductSettingService.class,
//        keyIdName = "id")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改目标管理新品")
    public ApiResult<Void> update(@RequestBody @Validated BiTargetNewProductSettingDTO.UpdateDTO dto) {
        biTargetNewProductSettingService.update(dto);
        return success();
    }


    /**
     * 下载模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出目标管理新品")
    @GetMapping("/downloadTemplate")
    public ApiResult<Void> downloadTemplate(HttpServletResponse response) {
        biTargetNewProductSettingService.downloadTemplate(response);
        return success();
    }

    /**
     * 导入新品目标设置
     *
     * @return
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导出目标管理新品")
    @PostMapping("/importFile")
    public ApiResult<BiTargetNewProductSettingDTO.ImportDTO> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        BiTargetNewProductSettingDTO.ImportDTO result = biTargetNewProductSettingService.importFile(excelFile, response);
        return success(result);
    }
    /**
     * 分页列表删除
     *
     * @return
     */
    @PostMapping("/remove")
    @LogAction(value = LogActionEnum.DELETE, desc = "新品目标设置删除")
    public ApiResult<Void> remove(@RequestBody @Validated BiTargetNewProductSettingDTO.RemoveDTO dto) {
        boolean result = biTargetNewProductSettingService.delete(dto);
        return result ? success() : failure();
    }
}
