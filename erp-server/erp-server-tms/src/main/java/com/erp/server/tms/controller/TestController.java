package com.erp.server.tms.controller;

import cn.hutool.json.JSONUtil;
import com.common.business.utils.PdfUtil;
import com.erp.model.file.dto.FileDTO;
import com.sdk.oms.tiktok.dto.TikTokShopInfoDTO;
import com.sdk.oms.tiktok.dto.tiktok.packages.PackageDocumentDTO;
import com.sdk.oms.tiktok.service.TikTokSdkClientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "/test")
public class TestController {

    @Resource
    private TikTokSdkClientService tikTokSdkClientService;

    @GetMapping("/test")
    public void select() throws Exception {
        String a = "{\"@type\":\"com.sdk.oms.tiktok.dto.TikTokShopInfoDTO\",\"accessToken\":\"ROW_JtbZeQAAAACj-JAAAriAWjVtF2MrUIFdPkU8v2PwmslxLKqRdTTD6LC69bn3X7wtL1fvhLtlULbb9xoOJGkUBexq78KD43AkOaiNHMBhmGQF1fEt3fHZNAjtCeoww0tiGrmCHo1ofh5nBIRZ7H3P0p1D49dYpGXvVp8uTLqDZUEw1E0270J7agiQ8Oar55MZwtzjWiXwjKw\",\"baseUrl\":\"https://auth.tiktok-shops.com\",\"clientId\":\"6buinkjt3hmld\",\"clientSecret\":\"8ff628de24faf70c24855de4d967fb6a17a47e3f\",\"id\":\"1820403424249143297\",\"sellerType\":\"CROSS_BORDER\",\"shopCipher\":\"TTP_pEhpJwAAAADvOkDJ2jIoaS9Uak191t0d\",\"site\":\"US\"}";
        TikTokShopInfoDTO tikTokShopInfoDTO = com.alibaba.fastjson.JSON.parseObject(a, TikTokShopInfoDTO.class);
        PackageDocumentDTO packageDocumentDTO = tikTokSdkClientService.getPackageDocument(tikTokShopInfoDTO,"1155463856738374055","SHIPPING_LABEL");
        System.out.println(JSONUtil.toJsonStr(packageDocumentDTO));
        String base64 = PdfUtil.convertPdfUrlToBase64(packageDocumentDTO.getData().getDocUrl(),false);
//        FileDTO.UploadBase64 uploadBase64 = FileDTO.UploadBase64.builder()
//                .base64(base64)
//                .fileName(logisticsGetLabelVO.getDeliveryNo() + ".pdf")
//                .build();
//        url = fileFeign.uploadFileByBase64(uploadBase64);
        System.out.println(base64);
    }
}
