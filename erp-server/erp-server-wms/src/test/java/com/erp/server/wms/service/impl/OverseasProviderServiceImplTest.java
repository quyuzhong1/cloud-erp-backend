package com.erp.server.wms.service.impl;

import cn.hutool.json.JSONObject;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.server.wms.ErpServerWmsApplication;
import com.erp.server.wms.service.OverseasProviderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerWmsApplication.class})
public class OverseasProviderServiceImplTest {

    @Resource
    private OverseasProviderService overseasProviderService;

    @Test
    public void authorizeTest() {
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.set("appToken","7013991264f611e98ea200e01b680258");
//        jsonObject.set("appKey","6ff50abf64f611e98ea200e01b680258");
//        OverseasProviderDTO.AuthorizeParamDTO dto = new OverseasProviderDTO.AuthorizeParamDTO();
//        dto.setAuthJson(jsonObject);
//        dto.setId("1726456935660867586");
//        boolean result = overseasProviderService.authorize(dto);
//        System.out.println(result);
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("appToken","44ac3ae1211d416a080858e57833cc14");
        jsonObject.set("appKey","fa0c90d7dbb434fa2160209756db677c");
        OverseasProviderDTO.AuthorizeParamDTO dto = new OverseasProviderDTO.AuthorizeParamDTO();
        dto.setAuthJson(jsonObject);
        dto.setId("1726457716430561281");
        boolean result = overseasProviderService.authorize(dto);
        System.out.println(result);
    }

    @Test
    public void cancelAuthorizeTest() {
        boolean result = overseasProviderService.cancelAuthorize("1726457716430561281");
        System.out.println(result);
    }
}