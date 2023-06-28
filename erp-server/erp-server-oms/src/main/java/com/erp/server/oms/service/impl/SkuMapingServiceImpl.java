package com.erp.server.oms.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SkuMapingEntity;
import com.erp.server.oms.mapper.SkuMapingMapper;
import com.erp.server.oms.service.SkuMapingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>
 * sku 对照表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-28
 */
@Service
@Slf4j
public class SkuMapingServiceImpl extends SuperServiceImpl<SkuMapingMapper, SkuMapingEntity> implements SkuMapingService {

    @Override
    public void downloadTemplate(HttpServletResponse response) {

        String path = "classpath:excel/skuMaping.xlsx";
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
            log.error("SkuMaping downloadTemplate  出错了 e>>>>>>>", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }
}
