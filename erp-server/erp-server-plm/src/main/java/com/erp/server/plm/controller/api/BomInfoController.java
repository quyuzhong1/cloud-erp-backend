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
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.vo.BomPagingVO;
import com.erp.model.plm.vo.BomVersionVO;
import com.erp.model.workflow.dto.ProcessPassDTO;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.server.plm.service.BomInfoService;
import com.erp.server.plm.service.BomSkuService;
import com.erp.server.plm.service.ProductBomHistoryService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * BOM 管理
 *
 * @author yl
 * @since 2023-01-09 11:45:28
 */
@RestController
@LogSystemModule("BOM管理")
@RequestMapping("bom")
public class BomInfoController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private BomInfoService bomInfoService;


    @Resource
    private ProductBomHistoryService productBomHistoryService;


    @Resource
    private BomSkuService bomSkuService;

    /**
     * 分页查询
     *
     * @param
     * @return 查询结果
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:bom:paging",
            tableAlias = "b"
    )
    public ApiResult<PagingVO<List<BomPagingVO>>> queryByPage(@RequestBody @Validated PagingDTO<SearchPagingDTO> dto) {
        PagingVO<List<BomPagingVO>> pagingVO = bomInfoService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 分页查询显示组合SKU列表
     *
     * @param
     * @return 查询结果
     */
    @PostMapping("/skuPaging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:bom:paging",
            tableAlias = "b"
    )
    public ApiResult<PagingVO<List<BomSkuPageDTO.ListDTO>>> skuPaging(@RequestBody @Validated PagingDTO<BomSkuPageDTO.PagingParamDTO> dto) {
        PagingVO<List<BomSkuPageDTO.ListDTO>> pagingVO = bomInfoService.skuPaging(dto);
        return success(pagingVO);
    }


    /**
     * 新增BOM
     *
     * @param
     * @return 新增结果
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增BOM")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated AddBomDTO dto) {
        this.bomInfoService.insert(dto);
        return success();
    }

    /**
     * bom 详情
     *
     * @param
     * @return 新增结果
     */
    @LogViewService
    @PostMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:bom:view",
            serviceClass = BomInfoService.class,
            keyIdName="id"
    )
    public ApiResult<BomDTO> details(@RequestBody @Validated BaseIdDTO dto) {
        BomDTO bom = bomInfoService.getBomDetails(dto.getId());
        return success(bom);
    }

    /**
     * bom  审核 通过
     *
     * @param
     * @return 新增结果
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "bom审核通过")
    @PostMapping("/approvalPass")
    public ApiResult approvalPass(@RequestBody @Validated AuditParamDTO dto) {
        bomInfoService.approvalPass(dto);
        return success();
    }


    /**
     * bom  审核 不通过
     *
     * @param
     * @return 新增结果
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "bom审核不通过")
    @PostMapping("/approvalNoPass")
    public ApiResult approvalNoPass(@RequestBody @Validated AuditParamDTO dto) {
        bomInfoService.approvalNoPass(dto);
        return success();
    }


    /**
     * 提交审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "bom提交审核")
    @PostMapping("/submitAudit")
    public ApiResult submitAudit(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomInfoService.submitAudit(dto.getId());
        return result == true ? success() : failure();
    }



    /**
     * 重启流程
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "bom反审核")
    @PostMapping("/restartAudit")
    public ApiResult restartAudit(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomInfoService.restartAudit(dto.getId());
        return result == true ? success() : failure();
    }

    /**
     * 冻结bom
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "冻结bom:id={id}")
    @PostMapping("/freeze")
    public ApiResult freeze(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomInfoService.freeze(dto.getId());
        return result == true ? success() : failure();
    }

    /**
     * 解冻bom
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "解冻bom:id={id}")
    @PostMapping("/defrost")
    public ApiResult defrost(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomInfoService.defrost(dto.getId());
        return result == true ? success() : failure();
    }

    /**
     * 报废bom
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "报废bom:id={id}")
    @PostMapping("/scrap")
    public ApiResult scrap(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomInfoService.scrap(dto.getId());
        return result == true ? success() : failure();
    }

    /**
     * 恢复bom
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "恢复bom:id={id}")
    @PostMapping("/recover")
    public ApiResult recover(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomInfoService.recover(dto.getId());
        return result == true ? success() : failure();
    }

    /**
     * 解除归档
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "解除归档:id={id}")
    @PostMapping("/removeArchive")
    public ApiResult removeArchive(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomInfoService.removeArchive(dto.getId());
        return result == true ? success() : failure();
    }


    /**
     * 编辑bom
     *
     * @param
     * @return 编辑结果
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "更新bom")
    @PostMapping("/update")
    public ApiResult edit(@RequestBody @Validated UpdateBomDTO dto) {
        Boolean flag = this.bomInfoService.edit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 删除bom
     *
     * @param dto 主键
     * @return 删除是否成功
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除bom")
    @PostMapping("/delete")
    public ApiResult deleteById(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag = bomInfoService.deleteById(dto.getId());
        return flag == true ? success() : failure();
    }

    /**
     * 历史版本信息
     */
    @PostMapping("/version/list")
    public ApiResult<List<BomVersionVO>> versionList(@RequestBody @Validated BaseIdDTO dto) {
        List<BomVersionVO> list = productBomHistoryService.getVersionList(dto.getId());
        return success(list);
    }


    /**
     * 发起变更
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "发起变更:id={id}")
    @PostMapping("/startChange")
    public ApiResult startChange(@RequestBody @Validated UpdateBomDTO dto) {
        Boolean result = bomInfoService.startChange(dto);
        return result == true ? success() : failure();
    }


    /**
     * 导出bom 数据
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出bom")
    @PostMapping("/exportExcel")
    public ApiResult exportExcel(@RequestBody @Validated SearchPagingDTO dto) {
        bomInfoService.exportExcel(dto);
        return success();
    }


    /**
     * 导入bom
     * @author Will
     * @date: 2023/3/7 9:39
     * @param excelFile
     * @param response
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入bom")
    @PostMapping("/importFile")
    public ApiResult importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean flag = bomInfoService.importFile(excelFile,response);
        return flag == true ? success() : failure();
    }

   /**
    * 下载模板
    * @author Will
    * @date: 2023/3/7 9:39
    * @param request
    * @param response
    * @return ApiResult
    */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板bom")
    @GetMapping("/exportTemplate")
    public ApiResult exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/bomInfoTemplate.xlsx";
        String excelName = "template.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_95131);
        }
        return success();
    }



    /**
     * bom审核通过后改变 bom 状态
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "bom审核通过后改变 bom 状态:流程id={processId},具体业务表id={businessTableId}")
    @PostMapping("/workflow/pass")
    public ApiResult processPass(@RequestBody ProcessPassDTO dto) {
        bomInfoService.bomProcessPass(dto);
        return success();
    }


    /**
     * bom 审核情况
     *
     * @return
     */
    @PostMapping("/auditInfo")
    public ApiResult<List<ApproveNodeRecordVO>> auditInfo(@RequestBody @Validated BaseIdDTO dto) {
        List<ApproveNodeRecordVO> list=bomInfoService.auditInfo(dto.getId());
        return success(list);
    }



    /**
     * 查询sku版本信息
     * @Author Luo_WG
     * @Date 2023/11/2 8:57
     * @param dto
     * @return java.util.List<com.erp.model.plm.dto.ProductBomInfoDTO.skuBomVersion>
     **/
    @PostMapping("/listBomVersionBySkuNos")
    public ApiResult<List<ProductBomInfoDTO.skuBomVersion>> listBomVersionBySkuNos(@RequestBody ProductBomInfoDTO.skuBomVersionParams dto) {
        return success(bomSkuService.listBomVersionBySkuNos(dto.getSkuNos()));
    }

    /**
     * 根据id查询子件信息
     * @Author Luo_WG
     * @Date 2023/11/16 17:21
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<OverseasDeliveryPlanDTO.DeliverRecordDTO>>
     **/
    @PostMapping("/combinationSkuChildDetail")
    public ApiResult<List<BomChildrenSkuDTO>> combinationSkuChildDetail(@RequestBody ProductBomInfoDTO.skuIdParams dto) {
        List<BomChildrenSkuDTO> result =  bomSkuService.listBomChildBySkuIds(dto.getSkuIds());
        return success(result);
    }
}

