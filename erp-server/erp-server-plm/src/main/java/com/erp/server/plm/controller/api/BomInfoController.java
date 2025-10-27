package com.erp.server.plm.controller.api;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
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
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.vo.BomPagingVO;
import com.erp.model.plm.vo.BomVersionVO;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.server.plm.query.BomInfoHandler;
import com.erp.server.plm.service.BomInfoService;
import com.erp.server.plm.service.BomSkuService;
import com.erp.server.plm.service.ProductBomHistoryService;
import lombok.extern.slf4j.Slf4j;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * BOM 管理
 *
 * @author yl
 * @since 2023-01-09 11:45:28
 */
@Slf4j
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
    @WebAdvanceQuery(handler = BomInfoHandler.class)
    public ApiResult<PagingVO<BomPagingVO>> queryByPage(@RequestBody @Validated PagingDTO<SearchPagingDTO> dto) {
        PagingVO<BomPagingVO> pagingVO = bomInfoService.paging(dto);
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
    public ApiResult<PagingVO<BomSkuPageDTO.ListDTO>> skuPaging(@RequestBody @Validated PagingDTO<BomSkuPageDTO.PagingParamDTO> dto) {
        PagingVO<BomSkuPageDTO.ListDTO> pagingVO = bomInfoService.skuPaging(dto);
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
    public ApiResult<Object> add(@RequestBody @Validated AddBomDTO dto) {
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
     * 审核
     * @author will
     * @date 2025/5/16 15:00
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/approve")
    @LogAction(value = LogActionEnum.APPROVE, desc = "bom审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = bomInfoService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("bom审核失败",e);
                BomInfoEntity entity = bomInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "bom不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getSerialNumber(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 提交审核
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "bom提交审核")
    @PostMapping("/submitAudit")
    public ApiResult<List<BatchResultDTO>> submitAudit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = bomInfoService.submitAudit(id,Boolean.TRUE);
            }catch (Exception e){
                log.error("bom单 提交审核失败",e);
                BomInfoEntity entity = bomInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "bom单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getSerialNumber(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 撤销
     * @author will
     * @date:  2025-05-16
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/cancelProcess")
    @LogAction(value = LogActionEnum.CANCEL, desc = "BOM撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = bomInfoService.cancelProcess(new ApproveDTO.CancelProcessDTO(id));
            }catch (Exception e){
                log.error("BOM撤回流程失败",e);
                BomInfoEntity entity = bomInfoService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "BOM不存在, 撤回流程失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getId(), entity.getSerialNumber(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 冻结bom
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "冻结bom:id={id}")
    @PostMapping("/freeze")
    public ApiResult<Object> freeze(@RequestBody @Validated BaseIdDTO dto) {
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
    public ApiResult<Object> defrost(@RequestBody @Validated BaseIdDTO dto) {
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
    public ApiResult<Object> scrap(@RequestBody @Validated BaseIdDTO dto) {
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
    public ApiResult<Object> recover(@RequestBody @Validated BaseIdDTO dto) {
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
    public ApiResult<Object> removeArchive(@RequestBody @Validated BaseIdDTO dto) {
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
    public ApiResult<Object> edit(@RequestBody @Validated UpdateBomDTO dto) {
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
    public ApiResult<Object> deleteById(@RequestBody @Validated BaseIdDTO dto) {
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
     * 导出bom 数据
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出bom")
    @PostMapping("/exportExcel")
    public ApiResult<Object> exportExcel(@RequestBody @Validated SearchPagingDTO dto) {
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
    public ApiResult<Object> importFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
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
    public ApiResult<Object> exportTemplate(HttpServletRequest request, HttpServletResponse response) {
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
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_95131);
        }
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
    public ApiResult<List<ProductBomInfoDTO.SkuBomVersion>> listBomVersionBySkuNos(@RequestBody ProductBomInfoDTO.SkuBomVersionParams dto) {
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
    public ApiResult<List<BomChildrenSkuDTO>> combinationSkuChildDetail(@RequestBody ProductBomInfoDTO.SkuIdParams dto) {
        return success(bomSkuService.listBomChildBySkuIds(dto.getSkuIds()));
    }
}

