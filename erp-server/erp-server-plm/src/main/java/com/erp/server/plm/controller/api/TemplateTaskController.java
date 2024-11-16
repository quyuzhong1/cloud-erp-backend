package com.erp.server.plm.controller.api;

import com.alibaba.excel.EasyExcel;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.dto.excel.ProjectTaskExcelDTO;
import com.erp.model.plm.dto.excel.TemplateTaskExcelDTO;
import com.erp.model.plm.vo.TemplateTaskVO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.listener.TemplateTaskExcelListener;
import com.erp.server.plm.service.TemplatePhaseService;
import com.erp.server.plm.service.TemplateTaskDocsNameService;
import com.erp.server.plm.service.TemplateTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 模板管理
 *
 * @author Will
 * @version 1.0
 * @description:
 * @date 2022/11/14 9:15
 */
@RestController
@LogSystemModule("系统通用设置")
@RequestMapping("template/task")
public class TemplateTaskController extends BaseController {

    @Autowired
    private TemplateTaskService templateTaskService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private TemplatePhaseService templatePhaseService;

    @Resource
    private TemplateTaskDocsNameService templateTaskDocsNameService;


    /**
     * 模板详情-模板任务-列表分页查询
     *
     * @author Will
     * @date: 2022/11/14 9:25
     * @param dto
     * @return ApiResult<PagingVO<TemplateTaskShowDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<TemplateTaskShowDTO>> paging(@RequestBody PagingDTO<TemplateSearchDTO> dto) {
        PagingVO<TemplateTaskShowDTO> pagingVO = templateTaskService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 模板详情-模板任务-新增或修改-PLM-1.3
     *
     * @author Will
     * @date: 2022/11/14 9:25
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "模板详情-模板任务-新增或修改")
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated TemplateTaskDTO dto) {
        Boolean flag = templateTaskService.saveOrUpdate(dto);
        return flag ? success() : failure();
    }

    /**
     * 模板详情-模板任务-删除
     *
     * @author Will
     * @date: 2022/11/14 14:58
     * @param dto
     * @return ApiResult
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "模板详情-模板任务-删除")
    @DeleteMapping("/remove")
    public ApiResult remove(@RequestBody @Validated TemplateTaskParamDTO dto) {
        Boolean flag = templateTaskService.removeTask(dto.getId(),dto.getTemplateId());
        return flag == true ? success() : failure();
    }

    /**
     * 模板详情-模板任务-获取前置任务列表
     *
     * @author Will
     * @date: 2022/11/18 14:58
     * @param templateId
     * @return ApiResult
     */
    @GetMapping("/list")
    public ApiResult list(@RequestParam("templateId") String templateId) {
        List<Map<String, Object>> list = templateTaskService.getTaskListByTemplateId(templateId);
        return success(list);
    }

    /**
     * 模板详情-模板任务-任务详情数据
     *
     * @author Will
     * @date: 2022/11/18 14:32
     * @param dto
     * @return ApiResult<TemplateTaskDTO>
     */
    @PostMapping("/taskDetails")
    public ApiResult<TemplateTaskVO> taskDetails(@RequestBody @Validated TemplateTaskParamDTO dto) {
        TemplateTaskVO taskVO = templateTaskService.taskDetails(dto);
        return success(taskVO);
    }

    /**
     * excel导入模板任务-PLM-1.3
     * @param excelFile 文件流
     * @param response  响应
     * @return com.common.core.vo.ApiResult
     * @Author Luo_WG
     * @Date 2022/9/28 11:46
     **/
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入模板任务")
    @PostMapping("/importTemplateTaskFile")
    public ApiResult importTemplateTaskFile(@RequestParam(value = "excelFile") MultipartFile excelFile, @RequestParam(value = "templateId") String templateId, HttpServletResponse response) {
        TemplateTaskExcelListener excelListenerUtil = new TemplateTaskExcelListener(templateId, sysUserFeign, templatePhaseService, templateTaskService, templateTaskDocsNameService);
        try {
            EasyExcel.read(excelFile.getInputStream(), TemplateTaskExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_95124);
        }
        List<TemplateTaskExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123);
        }
        List<TemplateTaskExcelDTO> list = excelListenerUtil.getDateList();
        if (list.size() > 0) {
            StringBuffer sb = new StringBuffer();
            String excelPath = "excel/templateTaskError.xlsx";
            String name = "templateTaskError";
            String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
            sb.append(date);
            sb.append(name);
            try {
                new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
            } catch (IOException e) {
                throw new ServiceException(ApiError.ERROR_95125);
            }

            return failure();
        }
        return success();
    }


    /**
     * 下载导入任务模板-PLM-1.3
     *
     * @param request  request
     * @param response response
     * @Author Luo_WG
     * @Date 2022/9/28 11:46
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载导入任务模板")
    @GetMapping("/importTemplate")
    public void importTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/templateTaskExportTemplate.xlsx";
        String excelName = "templateTaskExportTemplate";

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
            e.printStackTrace();
        }
    }

    /**
     * 模板详情-模板任务-批量删除-PLM-1.3
     * @Author Luo_WG
     * @Date 2023/6/20 16:45
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "模板详情-模板任务-批量删除")
    @PostMapping("/removeTaskBatch")
    public ApiResult removeTaskBatch(@RequestBody @Validated TemplateTaskParamsDTO dto) {
        Boolean flag = templateTaskService.removeTaskBatch(dto.getIds(),dto.getTemplateId());
        return flag == true ? success() : failure();
    }

}
