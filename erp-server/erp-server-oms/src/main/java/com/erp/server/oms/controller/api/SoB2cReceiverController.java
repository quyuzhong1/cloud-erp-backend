package com.erp.server.oms.controller.api;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.validator.ValidList;
import com.common.core.anno.LogAction;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.SoB2cReceiverDTO;
import com.erp.server.oms.service.SoB2cReceiverService;
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
 * B2C销售订单买家信息表
 *
 * @author Will
 * @since 2023-08-18
 */
@Slf4j
@RestController
@RequestMapping("/soB2cReceiver")
public class SoB2cReceiverController extends BaseController {

    @Resource
    private SoB2cReceiverService soB2cReceiverService;



    /**
     * 导入B2C销售订单的客户信息
     * @param excelFile  文件流
     * @param response   响应
     * @return com.common.core.vo.ApiResult
     * @Author jack
     * @Date 2025-03-21
     **/
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入B2C销售订单的客户信息")
    @PostMapping("/importB2cCustomerFile")
    public ApiResult importB2cCustomerFile(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        soB2cReceiverService.importB2cCustomerFile(excelFile, response);
        return success();
    }

    /**
     * 下载B2C客户更新模板
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "下载B2C客户更新模板")
    @GetMapping("/exportB2cCustomerUpdateTemplate")
    public void exportB2cCustomerUpdateTemplate(HttpServletRequest request, HttpServletResponse response) {
        String path = "classpath:excel/b2cCustomerUpdateTemplate.xlsx";
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
            e.printStackTrace();
        }
    }

    /**
     * 批量更新发票地址
     * @param addressDTOList
     * @return
     */
    @PostMapping("/updateInvoiceAddress")
    public ApiResult<List<BatchResultDTO>>  updateInvoiceAddress(@RequestBody @Validated ValidList<SoB2cReceiverDTO.AddressDTO> addressDTOList){
        List<BatchResultDTO> resultDTOS = new ArrayList<>(addressDTOList.size());
        for (SoB2cReceiverDTO.AddressDTO addressDTO : addressDTOList){
            try {
                soB2cReceiverService.updateInvoiceAddress(addressDTO.getSoId(), addressDTO.getInvoiceAddress());
                resultDTOS.add(BatchResultDTO.success(addressDTO.getSoId(), addressDTO.getSoCode(), "修改发票地址成功"));
            }catch (Exception e){
                log.error("修改发票地址异:"+ e);
                resultDTOS.add(BatchResultDTO.fail(addressDTO.getSoId(), addressDTO.getSoCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
