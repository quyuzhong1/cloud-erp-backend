package com.erp.server.plm.controller.api;


import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.BaseIdsDTO.IdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.utils.ApplicationContextUtils;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.file.dto.FileDTO;
import com.erp.model.file.dto.FileDTO.FileSizeInfo;
import com.erp.model.plm.dto.SkuStdRetailPriceDTO;
import com.erp.model.plm.dto.SkuStdRetailPriceDTO.AddDTO;
import com.erp.model.plm.entity.SkuStdRetailPriceEntity;
import com.erp.server.plm.query.SkuStdRetailPriceQueryHandler;
import com.erp.server.plm.service.SkuStdRetailPriceService;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * sku标准零售价表
 *
 * @author shukai
 * @since 2026-03-16
 */
@Slf4j
@RestController
@LogSystemModule("sku标准零售价表")
@RequestMapping("/skuStdRetailPrice")
public class SkuStdRetailPriceController extends BaseController {

    @Resource
    private SkuStdRetailPriceService skuStdRetailPriceService;

    /**
     * 获取Tab
     * @return
     */
     @PostMapping("/tabList")
     @DataPermission(operationType = DataAttributeEnum.LIST,
             tableField = "create_user_id",
             menuCode = "plm:skuStdRetailPrice:paging",
             tableAlias = ""
     )
     public ApiResult<List<SkuStdRetailPriceDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(skuStdRetailPriceService.tabList(dto));
     }

     /**
     * 列表查询，menuCode为plm:skuStdRetailPrice:paging
     * @author shukai
     * @date: 2026-03-16
     * @param dto
     * @return ApiResult<PagingVO<SkuStdRetailPriceDTO.ListDTO>>
     */
     @PostMapping("/paging")
     @DataPermission(operationType = DataAttributeEnum.LIST,
             tableField = "create_user_id",
             menuCode = "plm:skuStdRetailPrice:paging",
             tableAlias = ""
     )
     @WebAdvanceQuery(handler = SkuStdRetailPriceQueryHandler.class)
     public ApiResult<PagingVO<SkuStdRetailPriceDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SkuStdRetailPriceDTO.PagingParamDTO> dto) {
         return success(skuStdRetailPriceService.paging(dto));
     }
     
    /**
    * 新增币别
    * @author shukai
    * @date:  2026-03-16
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/batchAdd")
    @LogAction(value = LogActionEnum.INSERT, desc = "sku标准零售价表新增")
    public ApiResult<List<BaseResultDTO.AddDTO>> batchAdd(@RequestBody @Validated List<SkuStdRetailPriceDTO.AddDTO> dtoList) {
        return success(skuStdRetailPriceService.batchAdd(dtoList , true));
    }
    
    /**
     * 下载excel模板
     */
    @GetMapping("/exportTemplate")
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载excel模板")
    public ApiResult<Object> exportTrackTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "excel/skuStdRetailPriceTemplate.xlsx";
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
            throw new ServiceException(ApiError.FILE_IMPORT_TEMPLATE_DOWNLOAD_FAILED);
        }
        return success();
    }
    
    /**
     *  导入Excel
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入Excel")
    @PostMapping(value = "/importExcel")
    public ApiResult<Boolean> importExcel(@RequestBody @Validated FileSizeInfo info, HttpServletResponse response)  throws Exception{
        return success(skuStdRetailPriceService.importExcel(info, response));
    }
    
    /**
     *  编辑回传数据
     * @author shukai
     * @date:  2026-03-16
     * @param id
     * @return ApiResult<SkuStdRetailPriceDTO.ViewDTO>>
     */
    @PostMapping("/batchView")
     @LogViewService
     public ApiResult<List<SkuStdRetailPriceDTO.ListDTO>> batchView(@RequestBody @Validated IdsDTO ids) {
    	PagingDTO<SkuStdRetailPriceDTO.PagingParamDTO> dto = new PagingDTO<>();
    	dto.setPageSize(-1);
    	SkuStdRetailPriceDTO.PagingParamDTO params = new SkuStdRetailPriceDTO.PagingParamDTO();
    	AdvanceQueryDTO advanceQueryDTO = new AdvanceQueryDTO();
    	advanceQueryDTO.setCompare("inList");
    	advanceQueryDTO.setDataType("string");
   	 	advanceQueryDTO.setField("g.id");
   	 	advanceQueryDTO.setGroupName("default");
   	 	advanceQueryDTO.setIsExtend(false);
   	 	advanceQueryDTO.setValue(ids.getIds());
    	params.setAdvanceQueryDTOList(Arrays.asList(advanceQueryDTO));
    	dto.setParams(params);
        return success(ApplicationContextUtils.getBean(SkuStdRetailPriceController.class).paging(dto).getData().getList());
     }

    /**
    * 批量编辑
    * @author shukai
    * @date:  2026-03-16
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/batchUpdate")
    @LogAction(value = LogActionEnum.UPDATE, desc = "sku标准零售价表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "plm:skuStdRetailPrice:update",
        serviceClass = SkuStdRetailPriceService.class,
        keyIdName = "id")
    public ApiResult<Boolean> batchUpdate(@RequestBody @Validated List<SkuStdRetailPriceDTO.UpdateDTO> dtoList) {
    	skuStdRetailPriceService.batchAdd(BeanUtil.copyToList(dtoList, SkuStdRetailPriceDTO.AddDTO.class) , true);
    	return success(true);
    }

    /**
    *  批量删除
    * @author shukai
    * @date:  2026-03-16
    * @param id
    * @return ApiResult<SkuStdRetailPriceDTO.ViewDTO>>
    */
    @PostMapping("/batchDelete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:skuStdRetailPrice:delete",
            serviceClass = SkuStdRetailPriceService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<Boolean> batchDelete(@RequestBody @Validated IdsDTO ids) {
    	List<SkuStdRetailPriceEntity> listByIds = skuStdRetailPriceService.listByIds(ids.getIds());
    	if(CollUtil.isNotEmpty(listByIds)) {
    		String cnyInfo = listByIds.stream().filter(l -> l.getCurrency().equals("CNY")).map(SkuStdRetailPriceEntity::getSkuNo).collect(Collectors.joining("、"));
    		if(StringUtils.isNotBlank(cnyInfo)) {
    			throw new ServiceException("不允许删除如下SKU的CNY币种标准零售价：" + cnyInfo);
    		}
    		List<AddDTO> addList = BeanUtil.copyToList(listByIds, SkuStdRetailPriceDTO.AddDTO.class);
    		addList.forEach(a -> a.setIsDeleted(true));
			skuStdRetailPriceService.batchAdd(addList , true);
    	}
        return success(true);
    }
    
    /**
     *  导出Excel
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出")
    @PostMapping(value = "/exportExcel")
    public ApiResult<Boolean> exportExcel(@RequestBody @Validated SkuStdRetailPriceDTO.ExportDTO dto) {
        return success(skuStdRetailPriceService.exportExcel(dto));
    }

    /**
     *  setting
     * @author Will
     * @date: 2023/11/13 16:19
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "分摊设置")
    @PostMapping(value = "/setting")
    public ApiResult<Boolean> setting(@RequestBody @Validated SkuStdRetailPriceDTO.SettingDTO dto) {
        return success(skuStdRetailPriceService.setting(dto));
    }
}
