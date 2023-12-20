package com.common.business.utils;

import com.common.business.dto.PrintWayBillPdfDTO;
import com.common.business.dto.PrintWayBillPdfDetailDTO;
import com.common.business.pfd.kit.component.PDFHeaderFooter;
import com.common.business.pfd.kit.component.PDFKit;
import com.common.business.pfd.kit.component.chart.model.XYLine;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fgm on 2017/4/17.
 * 360报告
 *
 */
@Slf4j
public class ReportKit360 {


    public  String createPDF(String templatePath,Object data, String fileName){
        //pdf保存路径
        try {
            //设置自定义PDF页眉页脚工具类
            PDFHeaderFooter headerFooter=new PDFHeaderFooter();
            PDFKit kit=new PDFKit();
            kit.setHeaderFooterBuilder(headerFooter);
            //设置输出路径
            kit.setSaveFilePath("/Users/fgm/Desktop/pdf/hello.pdf");

            String saveFilePath=kit.exportToFile(fileName,data);
            return  saveFilePath;
        } catch (Exception e) {
            log.error("PDF生成失败{}", ExceptionUtils.getFullStackTrace(e));
            return null;
        }

    }

    public static void main(String[] args) {

        ReportKit360 kit = new ReportKit360();
        PrintWayBillPdfDTO billPdfDTO=new PrintWayBillPdfDTO();
        billPdfDTO.setPrintTime(LocalDateTime.now());
        billPdfDTO.setShopName("店铺名称");
        billPdfDTO.setCustomerId("testete");
        billPdfDTO.setAmount(BigDecimal.ZERO);
        billPdfDTO.setWeight(BigDecimal.TEN);
        billPdfDTO.setChannelName("渠道渠道名称");
        billPdfDTO.setTransportNo("DT123333");
        billPdfDTO.setRemark("备注");
        List<PrintWayBillPdfDetailDTO> detailList = new ArrayList<>();
        PrintWayBillPdfDetailDTO detailDTO = new PrintWayBillPdfDetailDTO();
        detailDTO.setSkuImagesUrl("图片");
        detailDTO.setSkuNo("skuNo123");
        detailDTO.setProductName("产品名称1");
        detailDTO.setWarehouseLocation("仓位");
        detailDTO.setQty(10);
        detailDTO.setVariantProperty("颜色：红色");
        detailList.add(detailDTO);
        PrintWayBillPdfDetailDTO detailDTOt = new PrintWayBillPdfDetailDTO();
        detailDTOt.setSkuImagesUrl("图片2");
        detailDTOt.setSkuNo("skuNo222");
        detailDTOt.setProductName("产品名称2");
        detailDTOt.setWarehouseLocation("仓位2");
        detailDTOt.setQty(12);
        detailDTOt.setVariantProperty("颜色2：红色2");
        detailList.add(detailDTOt);
        billPdfDTO.setDetailList(detailList);

        String templatePath="/Users/fgm/workspaces/fix/pdf-kit/src/test/resources/templates";
        String path= kit.createPDF(templatePath, billPdfDTO,"hello1.pdf");
        System.out.println(path);



    }






}
