package com.erp.server.bi.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.validator.AddGroup;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.common.core.utils.ExcelUtil;
import com.erp.model.bi.dto.*;
import com.erp.model.bi.dto.excel.TargetSkuSettingExportExcelDTO;
import com.erp.server.bi.convert.BiExportConverter;
import com.erp.server.bi.service.BiTargetSkuSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 目标管理-单品
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@RestController
@LogSystemModule("目标管理")
@RequestMapping("/biTargetSkuSetting")
public class BiTargetSkuSettingController extends BaseController {

    @Resource
    private BiTargetSkuSettingService biTargetSkuSettingService;



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
    public ApiResult<PagingVO<BiTargetSkuSettingDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<BiTargetYearDTO.PagingParamDTO> dto) {
        PagingVO<BiTargetSkuSettingDTO.PagingViewDTO> pagingVO = biTargetSkuSettingService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 按照SKU导出目标
     * @param dto
     * @return
     */
    @PostMapping("/export")
    public Boolean export(@RequestBody @Validated PagingDTO<BiTargetYearDTO.PagingParamDTO> dto, HttpServletResponse response) {
        dto.setPageSize(1000);
        dto.setCurrPage(1);
        PagingVO<BiTargetSkuSettingDTO.PagingViewDTO> pagingVO = biTargetSkuSettingService.paging(dto);
        List<BiTargetSkuSettingDTO.PagingViewDTO> list = (List<BiTargetSkuSettingDTO.PagingViewDTO>) pagingVO.getList();
        List<TargetSkuSettingExportExcelDTO> excels = BiExportConverter.INSTANCE.exportSkuTargetStaff(list);
        //数据转换
        ExcelUtil.export("按SKU导出目标报表", "SKU", excels, TargetSkuSettingExportExcelDTO.class, response);
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
        BiTargetYearDTO.PagingTotalDTO totalDTO = biTargetSkuSettingService.pagingTotal(dto);
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
    @LogAction(value = LogActionEnum.INSERT, desc = "SKU目标设置添加")
    public ApiResult<String> add(@RequestBody @Validated(value = {AddGroup.class})   BiTargetSkuSettingDTO.AddDTO dto) {
        return success(biTargetSkuSettingService.add(dto));
    }

    /**
     * 详情
     *
     * @param id
     * @return
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<BiTargetSkuSettingDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        BiTargetSkuSettingDTO.ViewDTO view = biTargetSkuSettingService.view(id);
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
    @LogAction(value = LogActionEnum.UPDATE, desc = "单品目标设置修改")
//        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
//        tableField = "create_user_id",
//        menuCode = "dmp:biTargetSkuSetting:update",
//        serviceClass = BiTargetSkuSettingService.class,
//        keyIdName = "id")
    public ApiResult<Void> update(@RequestBody @Validated BiTargetSkuSettingDTO.UpdateDTO dto) {
        biTargetSkuSettingService.update(dto);
        return success();
    }


    /**
     * 下载模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载目标单品模板")
    @GetMapping("/downloadTemplate")
    public ApiResult<Void> downloadTemplate(HttpServletResponse response) {
        biTargetSkuSettingService.downloadTemplate(response);
        return success();
    }


    /**
     * 导入单品目标设置
     *
     * @return
     */
    @PostMapping("/importFile")
    @LogAction(value = LogActionEnum.IMPORT, desc = "单品目标设置导入")
    public ApiResult<BiTargetSkuSettingDTO.ImportDTO> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        BiTargetSkuSettingDTO.ImportDTO result = biTargetSkuSettingService.importFile(excelFile, response);
        return success(result);
    }

    /**
     * 分页列表删除
     *
     * @return
     */
    @PostMapping("/remove")
    @LogAction(value = LogActionEnum.DELETE, desc = "单品目标设置删除")
    public ApiResult<Void> remove(@RequestBody @Validated BiTargetSkuSettingDTO.RemoveDTO dto) {
        boolean result = biTargetSkuSettingService.delete(dto);
        return result ? success() : failure();
    }

}
