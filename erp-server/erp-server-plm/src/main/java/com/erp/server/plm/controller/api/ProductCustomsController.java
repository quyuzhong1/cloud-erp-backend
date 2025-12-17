package com.erp.server.plm.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.ProductCustomsDTO;
import com.erp.model.plm.entity.ProductCustomsEntity;
import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.model.scm.entity.SupplierCredentialEntity;
import com.erp.server.plm.service.ProductCustomsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 目的国清关信息
 *
 * @author admin
 * @since 2023-03-15
 */
@RestController
@LogSystemModule("目的国清关信息")
@RequestMapping("/productCustoms")
@Slf4j
public class ProductCustomsController extends BaseController {


    @Resource
    private ProductCustomsService productCustomsService;


    /**
     * 分页查询
     *
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:productCustoms:paging",
            tableAlias = "pc"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<ProductCustomsDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<ProductCustomsDTO.PagingParamDTO> dto) {
        PagingVO<ProductCustomsDTO.ListDTO> pagingVO = productCustomsService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 新增
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增目的国清关信息")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated ProductCustomsDTO.AddListDTO dto) {
       Boolean  result= productCustomsService.add(dto);
       return result==true?success():failure();
    }


    /**
     * 编辑
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "编辑目的国清关信息")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:productCustoms:update",
            serviceClass = ProductCustomsService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated ProductCustomsDTO.AddListDTO dto) {
        Boolean  result= productCustomsService.update(dto);
        return result==true?success():failure();
    }

    /**
     * 删除
     * @author jack
     * @date:  2025-06-21
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:productCustoms:delete",
            serviceClass = ProductCustomsService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "目的国清关信息删除")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<ProductCustomsEntity> list = productCustomsService.listByIds(ids);
        Map<String, ProductCustomsEntity> idEntityMap = list.stream().collect(Collectors.toMap(ProductCustomsEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = productCustomsService.delete(id);
            }catch (Exception e){
                log.error("目的国清关信息删除失败",e);
                ProductCustomsEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "目的国清关信息不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }else{
                    deleteResult = BatchResultDTO.fail(entity.getId(), entity.getSkuNo()+":"+entity.getCountryName(), e.getMessage());
                }
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 详情
     * @author jack
     * @date:  2025-06-21
     * @return ApiResult<ProductCustomsDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:productCustoms:view",
            serviceClass = ProductCustomsService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<ProductCustomsDTO.ViewDTO> view(@RequestParam(value = "skuId") String skuId) {
        return success(productCustomsService.view(skuId));
    }

    /**
     * 导出Excel数据
     * @author jack
     * @date:  2025-06-21
     * @param dto
     * @param response
     * @return
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "plm:productCustoms:export",
            tableAlias = "pc"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    @WebAdvanceQuery
    public ApiResult<Object> exportList(@RequestBody @Validated ProductCustomsDTO.PagingParamDTO dto, HttpServletResponse response) {
        productCustomsService.exportList(dto, response);
        return success();
    }

    /**
     * 导入
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入目的国清关信息")
    @PostMapping("/importFile")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = productCustomsService.importFile(excelFile, response);
        return result == true ? success() : failure();
    }

    /**
     * 下载模板
     *
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        productCustomsService.downloadTemplate(response);
        return success();
    }

}
