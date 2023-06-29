package com.erp.server.oms.service.impl;

import com.alibaba.excel.EasyExcel;
import com.common.business.service.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.erp.model.oms.dto.excel.SkuMapingImportExcelDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SkuMapingEntity;
import com.erp.model.oms.enums.DictBasicEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.listener.SkuMapingExcelListener;
import com.erp.server.oms.mapper.SkuMapingMapper;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.ShopInfoService;
import com.erp.server.oms.service.SkuMapingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;

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


    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private DictBasicService dictBasicService;

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


    /**
     * 导入sku对照信息
     *
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-29 11:01
     */
    @Override
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) {
        List<SkuVO> skuList = plmTaskFeign.listApproveSku();
        List<ShopInfoEntity> shopInfoList = shopInfoService.list();
        List<SkuMapingEntity> skuMapingList = this.listEffectiveList();
        String key = DictBasicEnum.PLATFORM.getType();
        List<DictBasicDTO.ViewDTO> dictBasicList = dictBasicService.getByKey(key);

        SkuMapingExcelListener excelListenerUtil = new SkuMapingExcelListener(this, skuList, shopInfoList, skuMapingList,dictBasicList);
        try {
            EasyExcel.read(excelFile.getInputStream(), SkuMapingImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("sku 对照表导入错误！", e);
            return Boolean.FALSE;
        }
        List<SkuMapingImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        if (errorList.size() > 0) {
            String fileName = "sku对照错误信息";
            ExcelUtil.export(fileName, "error", errorList, SkuMapingImportExcelDTO.class, response);
            return Boolean.FALSE;
        }

        return Boolean.TRUE;

    }


    /**
     * 获取到有效的sku 对照表
     *
     * @param
     * @return java.util.List<com.erp.model.oms.entity.SkuMapingEntity>
     * @author yl
     * @date 2023-06-29 11:29
     */
    private List<SkuMapingEntity> listEffectiveList() {
        LocalDateTime now = LocalDateTime.now();
        List<SkuMapingEntity> resultList = this.lambdaQuery().
                lt(SkuMapingEntity::getEffectiveTime, now).
                //失效日期要大于现在日期
                        ge(SkuMapingEntity::getExpireTime, now).list();

        return resultList;
    }
}
