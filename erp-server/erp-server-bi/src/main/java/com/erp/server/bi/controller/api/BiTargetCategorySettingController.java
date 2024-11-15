package com.erp.server.bi.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.core.utils.ExcelUtil;
import com.erp.model.bi.dto.BiTargetYearDTO;
import com.erp.model.bi.dto.excel.TargetCategorySettingExportExcelDTO;
import com.erp.server.bi.convert.BiExportConverter;
import com.erp.server.bi.service.BiTargetCategorySettingService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.bi.dto.BiTargetCategorySettingDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 目标管理-品类
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@RestController
@LogSystemModule("目标管理")
@RequestMapping("/biTargetCategorySetting")
public class BiTargetCategorySettingController extends BaseController {

    @Resource
    private BiTargetCategorySettingService biTargetCategorySettingService;



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
    public ApiResult<PagingVO<BiTargetCategorySettingDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<BiTargetYearDTO.PagingParamDTO> dto) {
        PagingVO<BiTargetCategorySettingDTO.PagingViewDTO> pagingVO = biTargetCategorySettingService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 按照品类导出目标
     * @param dto
     * @return
     */
    @PostMapping("/export")
    public Boolean export(@RequestBody @Validated PagingDTO<BiTargetYearDTO.PagingParamDTO> dto, HttpServletResponse response) {
        dto.setPageSize(1000);
        dto.setCurrPage(1);
        PagingVO<BiTargetCategorySettingDTO.PagingViewDTO> pagingVO = biTargetCategorySettingService.paging(dto);
        List<BiTargetCategorySettingDTO.PagingViewDTO> list = (List<BiTargetCategorySettingDTO.PagingViewDTO>) pagingVO.getList();
        List<TargetCategorySettingExportExcelDTO> excels = BiExportConverter.INSTANCE.exportCategoryTargetStaff(list);
        //数据转换
        ExcelUtil.export("按品类导出目标报表", "品类", excels, TargetCategorySettingExportExcelDTO.class, response);
        return Boolean.TRUE;
    }
    /**
     * 分页
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
        BiTargetYearDTO.PagingTotalDTO totalDTO = biTargetCategorySettingService.pagingTotal(dto);
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
    @LogAction(value = LogActionEnum.INSERT, desc = "分类目标设置添加")
    public ApiResult<String> add(@RequestBody @Validated BiTargetCategorySettingDTO.AddDTO dto) {
        return success(biTargetCategorySettingService.add(dto));
    }


    /**
     * 详情
     *
     * @param id
     * @return
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<BiTargetCategorySettingDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        BiTargetCategorySettingDTO.ViewDTO view = biTargetCategorySettingService.view(id);
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "分类目标设置修改")
//        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//        tableField = "create_user_id",
//        menuCode = "dmp:biTargetCategorySetting:update",
//        serviceClass = BiTargetCategorySettingService.class,
//        keyIdName = "id")
    public ApiResult<Void> update(@RequestBody @Validated BiTargetCategorySettingDTO.UpdateDTO dto) {
        biTargetCategorySettingService.update(dto);
        return success();
    }

    /**
     * 下载模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Void> downloadTemplate(HttpServletResponse response) {
        biTargetCategorySettingService.downloadTemplate(response);
        return success();
    }

    /**
     * 导入单品目标设置
     *
     * @return
     */
    @PostMapping("/importFile")
    @LogAction(value = LogActionEnum.IMPORT, desc = "分类目标设置导入")
    public ApiResult<BiTargetCategorySettingDTO.ImportDTO> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        BiTargetCategorySettingDTO.ImportDTO result = biTargetCategorySettingService.importFile(excelFile, response);
        return success(result);
    }

    /**
     * 分页列表删除
     *
     * @return
     */
    @PostMapping("/remove")
    @LogAction(value = LogActionEnum.DELETE, desc = "品类目标设置删除")
    public ApiResult<Void> remove(@RequestBody @Validated BiTargetCategorySettingDTO.RemoveDTO dto) {
        boolean result = biTargetCategorySettingService.delete(dto);
        return result ? success() : failure();
    }


}
