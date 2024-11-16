package com.erp.server.plm.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.enums.ApprovalStatusEnum;
import com.erp.model.plm.enums.ProductProgressStatusEnum;
import com.erp.server.plm.service.ProductInfoService;
import com.erp.server.plm.service.ProjectInfoService;
import com.erp.server.plm.service.SysCodeService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;

/**
 * 产品开发管理
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@LogSystemModule("产品开发管理")
@RequestMapping("product")
public class ProductInfoController extends BaseController {

    @Autowired
    private ProductInfoService productInfoService;

    @Autowired
    private ProjectInfoService projectInfoService;

    @Autowired
    private SysCodeService sysCodeService;

    /**
     * 所有项目【PLM1.3】
     *
     * @param dto
     * @return com.common.core.vo.ApiResult<com.erp.common.vo.PagingVO < com.erp.model.plm.dto.ProductShowDTO>>
     * @author yl
     * @date 2022-10-09 10:17
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "plm:product:paging",
            tableAlias = "pt"
    )
    public ApiResult<PagingVO<ProductShowDTO>> paging(@RequestBody @Validated PagingDTO<ProductSearchDTO.PagingParamDTO> dto) {
        PagingVO<ProductShowDTO> pagingVO = productInfoService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 我的项目【PLM1.3】
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO < com.erp.model.plm.dto.ProductShowDTO>>
     * @author yl
     * @date 2023-06-12 14:54
     */
    @PostMapping("/myProject")
    public ApiResult<PagingVO<ProductShowDTO>> myProject(@RequestBody @Validated PagingDTO<ProductSearchDTO.PagingParamDTO> dto) {
        PagingVO<ProductShowDTO> pagingVO = productInfoService.myProject(dto);
        return success(pagingVO);
    }

    /**
     * 收藏的项目【PLM1.3】
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO < com.erp.model.plm.dto.ProductShowDTO>>
     * @author yl
     * @date 2023-06-12 14:54
     */
    @PostMapping("/collect")
    public ApiResult<PagingVO<ProductShowDTO>> collect(@RequestBody @Validated PagingDTO<ProductSearchDTO.PagingParamDTO> dto) {
        PagingVO<ProductShowDTO> pagingVO = productInfoService.collect(dto);
        return success(pagingVO);
    }

    /**
     * 所有项目的统计【PLM1.3】
     *
     * @param
     * @return
     */
    @GetMapping("/allCount")
    public ApiResult<ProductDTO.ProductCountDTO> allCount() {
        ProductDTO.ProductCountDTO productCount = productInfoService.allCount();
        return success(productCount);
    }

    /**
     * 我的项目的统计【PLM1.3】
     *
     * @param
     * @return
     */
    @GetMapping("/myProjectCount")
    public ApiResult<ProductDTO.ProductCountDTO> myProjectCount() {
        ProductDTO.ProductCountDTO productCount = productInfoService.myProjectCount();
        return success(productCount);
    }

    /**
     * 我收藏的统计【PLM1.3】
     *
     * @param
     * @return
     */
    @GetMapping("/collectCount")
    public ApiResult<ProductDTO.ProductCountDTO> collectCount() {
        ProductDTO.ProductCountDTO productCount = productInfoService.collectCount();
        return success(productCount);
    }


    /**
     * 产品开发管理所有导出【PLM1.3】
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "产品开发管理所有导出")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "plm:product:paging",
            tableAlias = "pt"
    )
    @PostMapping("/allExport")
    public ApiResult allExport(@RequestBody @Validated ProductSearchDTO.ExportDTO dto) {
        Boolean result= productInfoService.allExport(dto);
        return result ? success() : failure();
    }


    /**
     * 产品开发管理我的项目导出【PLM1.3】
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "产品开发管理我的项目导出")
    @PostMapping("/myProjectExport")
    public ApiResult<Object> myProjectExport(@RequestBody @Validated ProductSearchDTO.ExportDTO dto) {
        Boolean result= productInfoService.myProjectExport(dto);
        return result ? success() : failure();
    }


    /**
     * 产品开发管理 收藏项目导出【PLM1.3】
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "产品开发管理收藏项目导出")
    @PostMapping("/collectExport")
    public ApiResult<Object> collectExport(@RequestBody @Validated ProductSearchDTO.ExportDTO dto) {
        Boolean result= productInfoService.collectExport(dto);
        return result ? success() : failure();
    }




    /**
     * 项目下拉【PLM1.3】
     *
     * @param
     * @return
     */
    @GetMapping("/itemDropdown")
    public ApiResult<List<ProductDTO.DropdownDTO>> itemDropdown() {
        List<ProductDTO.DropdownDTO> list = productInfoService.getItemDropdown();
        return success(list);
    }


    /**
     * 产品列表-无分页
     *
     * @param dto
     * @return ApiResult<List < BasicDTO>>
     * @author Will
     * @date: 2023/2/10 14:47
     */
    @PostMapping("/list")
    @DataPermission(operationType = DataAttributeEnum.LIST, tableField = "charge_id", menuCode = "plm:product:paging", tableAlias = "pt")
    public ApiResult<List<BasicDTO>> listProductInfo(@RequestBody @Validated ProductSearchDTO.PagingParamDTO dto) {
        List<BasicDTO> list = productInfoService.listProductInfo(dto);
        return success(list);
    }


    /**
     * 产品列表-编辑时候详情
     */
    @LogViewService
    @PostMapping("/productInfo")
    public ApiResult<ProductDTO> info(@RequestBody @Validated BaseIdDTO dto) {
        ProductDTO product = productInfoService.info(dto.getId());
        return success(product);
    }

    /**
     * 产品列表-更改对应数据
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "产品列表-更改对应数据", keyIdName = "productId")
    @PostMapping("/updateProduct")
    public ApiResult<Object> update(@RequestBody @Validated UpdateProductDTO dto) {
        productInfoService.updateProduct(dto);
        return success();
    }


    /**
     * 产品列表-新建产品
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "产品列表-新建产品")
    @PostMapping("/saveOrUpdate")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "charge_id",
            menuCode = "plm:product:updateOrUpdate",
            serviceClass = ProductInfoService.class,
            keyIdName = "id"
    )
    public ApiResult<String> saveOrUpdate(@RequestBody @Validated ProductDTO dto) {
        String productId = productInfoService.saveOrUpdateProduct(dto);
        return success(productId);
    }

    /**
     * 产品列表-移动分类
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "产品列表-移动分类:产品ids={productIds},分类id={categoryId}")
    @PostMapping("/updateCategory")
    public ApiResult<Object> updateCategory(@RequestBody @Validated MoveCategoryDTO dto) {
        Boolean flag = productInfoService.updateCategory(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 产品列表-删除产品
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "产品列表-删除产品")
    @PostMapping("/remove")
    public ApiResult<Object> removeProduct(@RequestBody @Validated RemoveProductDTO dto) {
        Boolean flag = productInfoService.removeProduct(dto);
        return flag == true ? success() : failure();
    }

    @LogAction(value = LogActionEnum.EXPORT, desc = "产品列表-下载模板")
    @GetMapping("/exportTemplate")
    public void exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        productInfoService.exportTemplate(request, response);
    }

    /**
     * 旧概览
     * 先保留
     */
    @PostMapping("/info")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "charge_id",
            menuCode = "plm:task:paging",
            tableAlias = "project_task"
    )
    public ApiResult<ProjectInfoDTO> projectInfo(@RequestBody ProductTaskCountShowDTO productTaskCountShowDTO) {
        ProjectInfoDTO info = projectInfoService.projectInfo(productTaskCountShowDTO);
        return success(info);
    }


    /**
     * 新概览【PLM1.3】
     *
     */
    @PostMapping("/overview")
    public ApiResult<ProductOverviewDTO.InfoDTO> overview(@RequestBody BaseIdDTO dto) {
        ProductOverviewDTO.InfoDTO info = productInfoService.overview(dto.getId());
        return success(info);
    }


    /**
     * 保存模板
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "产品列表-保存模板")
    @PostMapping("/saveTemplate")
    public ApiResult<Object> projectInfo(@RequestBody @Validated SaveProductTemplateDTO dto) {
        Boolean flag = productInfoService.saveTemplate(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 新建产品-获取关联产品
     */
    @GetMapping("/list")
    public ApiResult<Object> list() {
        List<Map<String, Object>> list = productInfoService.getListObjs();
        return success(list);
    }


    /**
     * 数据导出
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "产品列表-数据导出")
    @PostMapping(value = "/exportProductData", produces = "application/octet-stream")
    public void exportProductData(@RequestBody @Validated ExportProductDataDTO dto) {
        productInfoService.exportProductData(dto);
    }

    /**
     * 产品列表-立项状态下拉框
     *
     * @return ApiResult
     * @author Will
     * @date: 2022/11/24 10:24
     */
    @GetMapping("/getApprovalStatusSelect")
    public ApiResult<List<SelectShowDTO>> getApprovalStatusSelect() {
        List<SelectShowDTO> list = new ArrayList<>();
        Arrays.stream(ApprovalStatusEnum.values()).forEach(obj -> {
            SelectShowDTO dto = new SelectShowDTO();
            dto.setValue(obj.getCode());
            dto.setLabel(obj.getName());
            list.add(dto);
        });
        return success(list);
    }

    /**
     * 产品列表-获取SPU编号
     *
     * @param categoryId
     * @return ApiResult
     * @author Will
     * @date: 2023/1/7 16:09
     */
    @GetMapping("/getSpuNo")
    public ApiResult<Object> getSpuNo(@Param("categoryId") String categoryId) {
        String spuNo = sysCodeService.getSpuNo(categoryId);
        return success(spuNo);
    }


    /**
     * 获取产品进展列表
     *
     * @return com.common.core.controller.vo.ApiResult
     * @author yl
     * @date 2023-02-23 18:01
     */
    @GetMapping("/getProgressStatus")
    public ApiResult<Object> getProgressStatus() {
        int length = ProductProgressStatusEnum.values().length;
        List<Map<String, Object>> list = new ArrayList<>(length);
        for (ProductProgressStatusEnum status : ProductProgressStatusEnum.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", status.getName());
            map.put("status", status.getStatus());
            list.add(map);
        }
        return success(list);
    }


    /**
     * 设置产品进度
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "设置产品进度:产品id={productId},进展状态={progressStatus}(normal=正常;postpone=延期;risk=风险;no=暂无)")
    @PostMapping("/setProgressStatus")
    public ApiResult<Object> setProgressStatus(@RequestBody @Validated SetProductProgressStatusDTO dto) {
        Boolean result = productInfoService.setProgressStatus(dto);
        return result == true ? success() : failure();
    }

    /**
     * 设置产品示意图
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "设置产品示意图:产品id={productId}")
    @PostMapping("/setSchematicImageUrl")
    public ApiResult<Object> setSchematicImageUrl(@RequestBody @Validated SetSchematicImageUrlDTO dto) {
        Boolean result = productInfoService.setSchematicImageUrl(dto);
        return result == true ? success() : failure();
    }



    /**
     * 产品开发管理-确认立项  ids为产品id集合【PLM1.3】
     *
     * @param
     * @return void
     * @author yl
     * @date 2022-10-09 14:38
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "确认立项:ids={ids}")
    @PostMapping("/batchEstablish")
    public ApiResult<Object> batchArchive(@RequestBody @Valid ProductInfoDTO.IdsDateDto dto) {
        boolean flag = productInfoService.batchEstablish(dto);
        return flag ? success() : failure();
    }

}

