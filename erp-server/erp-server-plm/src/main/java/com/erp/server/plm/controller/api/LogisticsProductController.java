package com.erp.server.plm.controller.api;/**
 * @author Lambda
 * @Classname LogisticsController
 * @Description TODO
 * @Date 2023-11-06 12:24
 * @Created by yl
 */

import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.base.*;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.plm.entity.ProductLogisticsEntity;
import com.erp.server.plm.service.LogisticsProductService;
import com.erp.server.plm.service.ProductLogisticsService;
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
import javax.validation.Valid;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 物流产品
 *
 * @Author yl
 * @Date 2023-11-06 12:24
 */
@Slf4j
@RestController
@LogSystemModule("物流产品")
@RequestMapping("logistics/product")
public class LogisticsProductController extends BaseController {

    @Resource
    private LogisticsProductService logisticsProductService;

    @Resource
    private ProductLogisticsService productLogisticsService;
    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<LogisticsProductDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<LogisticsProductDTO.PagingParamDTO> dto) {
        PagingVO<LogisticsProductDTO.PagingVO> pagingVO = logisticsProductService.paging(dto);
        return success(pagingVO);

    }

    /**
     * tab页
     *
     * @param dto
     * @return
     */
    @PostMapping("/tabList")
    public ApiResult<List<LogisticsProductDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<LogisticsProductDTO.TabListDTO> tabList = logisticsProductService.tabList(dto);
        return success(tabList);

    }

    /**
     * 更新分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/update/paging")
    public ApiResult<PagingVO<LogisticsProductDTO.UpdatePagingDTO>> updatePaging(@RequestBody @Valid PagingDTO<LogisticsProductDTO.UpdatePagingParamDTO> dto) {
        PagingVO<LogisticsProductDTO.UpdatePagingDTO> pagingVO = logisticsProductService.updatePaging(dto);
        return success(pagingVO);

    }

    /**
     * 详情
     *
     * @param id
     * @return
     */
    @GetMapping("/view")
    @LogViewService
    public ApiResult<LogisticsProductDTO.ViewDTO> view(@RequestParam(value = "id") String id) {
        LogisticsProductDTO.ViewDTO viewDTO = logisticsProductService.view(id);
        return success(viewDTO);

    }

    /**
     * 修改
     *
     * @param dto
     * @return
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改物流产品")
    public ApiResult update(@RequestBody @Valid LogisticsProductDTO.UpdateDTO dto) {
        Boolean updateResult = logisticsProductService.update(dto);
        return updateResult?success():failure();

    }

    /**
     * 提交审核
     * @author will
     * @date:  2024-03-18
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/submit")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "物流产品提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = logisticsProductService.submit(id,Boolean.TRUE);
            }catch (Exception e){
                log.error("物流产品 提交审核失败",e);
                ProductLogisticsEntity entity = productLogisticsService.getEntityById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "物流产品不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 撤销
     * @author will
     * @date:  2024-03-18
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/cancelProcess")
    @LogAction(value = LogActionEnum.CANCEL, desc = "物流产品撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = logisticsProductService.cancelProcess(id);
            }catch (Exception e){
                log.error("物流产品撤回流程失败",e);
                ProductLogisticsEntity entity = productLogisticsService.getEntityById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "物流产品不存在, 撤回流程失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 审核
     * @author will
     * @date:  2024-01-08
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/approve")
    @LogAction(value = LogActionEnum.APPROVE, desc = "物流产品审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = logisticsProductService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("物流产品审核失败",e);
                ProductLogisticsEntity entity = productLogisticsService.getEntityById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "物流产品不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 反审核
     * @author will
     * @date:  2024-01-08
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/disApprove")
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "物流产品反审核")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO disApproveResult;
            try {
                disApproveResult = logisticsProductService.disApprove(id);
            }catch (Exception e){
                log.error("物流产品反审核失败",e);
                ProductLogisticsEntity entity = productLogisticsService.getEntityById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    disApproveResult = BatchResultDTO.fail(id, id, "物流产品不存在, 反审核失败");
                    resultDTOS.add(disApproveResult);
                    continue;
                }
                disApproveResult = BatchResultDTO.fail(entity.getId(), entity.getSkuNo(), e.getMessage());
            }
            resultDTOS.add(disApproveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 导出产品信息
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出物流产品信息")
    @PostMapping("/export")
    public ApiResult exportExcel(@RequestBody @Valid LogisticsProductDTO.ExportDTO dto) {
        Boolean result = logisticsProductService.exportExcel(dto);
        return result ? success() : failure();
    }

    /**
     * 导入产品信息
     * @return
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入物流产品信息")
    @PostMapping("/importExcel")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = logisticsProductService.importExcel(excelFile, response);
        return result ? success() : failure();
    }


    /**
     * 下载模板
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载模板物流产品")
    @GetMapping("/exportTemplate")
    public ApiResult exportTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/logisticsProductTemplate.xlsx";
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
     * 已审核备案SKU下拉
     * @author Will
     * @date: 2024/3/19 15:05
     * @return ApiResult<List<SelectDTO>>
     */
    @GetMapping("/selectSku")
    public ApiResult<List<LogisticsProductDTO.SelectDTO>> selectSku() {
        List<LogisticsProductDTO.SelectDTO> list = productLogisticsService.selectSku();
        return success(list);

    }

    /**
     * 推送备案
     * @author Will
     * @date: 2024/3/19 14:21
     * @param dto
     */
    @PostMapping("/pushRegistration")
    public  ApiResult<List<BatchResultDTO>> pushRegistration(@RequestBody LogisticsProductDTO.PushRegistrationDTO dto) {
        List<BatchResultDTO> resultDTOS = logisticsProductService.pushRegistration(dto);
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

}
