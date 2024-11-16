package com.erp.server.scm.service.impl;

import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidList;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.workflow.dto.ProcessManagementDTO;
import com.erp.rpc.workflow.WorkflowFeign;
import com.erp.server.scm.service.CommonService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yl
 * @Classname CommonServiceImpl

 * @Date 2023-03-15 11:50
 * @Created by yl
 */
@Service
public class CommonServiceImpl implements CommonService {

    @Resource
    private WorkflowFeign workflowFeign;

    /**
     * 公共的下载模板
     *
     * @param request
     * @param response
     * @return void
     * @author yl
     * @date 2023-03-21 9:21
     */
    @Override
    public void downloadTemplate(HttpServletRequest request, HttpServletResponse response, String type) {
        String pathName = type + ".xlsx";
        String path = "classpath:excel/" +pathName;
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
            throw new ServiceException(ApiError.DEFAULT);
        }
    }

    @Override
    public List<String> listProcessCurBusinessIds (String businessKey) {
        //获取当前人需要审核的业务ids
        ValidList<ProcessManagementDTO.ApproveActivityDTO> dtoList = new ValidList<>();
        ProcessManagementDTO.ApproveActivityDTO approveActivityDTO = new ProcessManagementDTO.ApproveActivityDTO();
        approveActivityDTO.setCurApproveId(UserContext.getDefaultLoginUser().getUid());
        approveActivityDTO.setBusinessKey(businessKey);
        dtoList.add(approveActivityDTO);
        ApiResult<List<ProcessManagementDTO.CurApproveInfoDTO>> listApiResult = workflowFeign.batchCurApproverByApprove(dtoList);
        if (200 != listApiResult.getCode()) {
            throw new ServiceException(ApiError.ERROR_94006);
        }
        List<String> businessIds = listApiResult.getData().stream().filter(obj -> StringUtils.isNotBlank(obj.getBusinessId())).map(ProcessManagementDTO.CurApproveInfoDTO::getBusinessId).collect(Collectors.toList());
        return  businessIds;
    }

}
