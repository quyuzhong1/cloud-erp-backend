package com.erp.server.tms.service.logistics;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.entity.LogisticsAuthEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSaleChannelEntity;
import com.erp.model.tms.vo.request.*;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import com.erp.server.tms.ErpServerTmsApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * @author zdy
 * @ClassName DsfLogisticsHandlerImplTest
 * @description: TODO
 * @date 2023年11月09日
 * @version: 1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ErpServerTmsApplication.class})
public class DsfLogisticsHandlerImplTest {
    @Resource
    DsfLogisticsHandlerImpl dsfLogisticsHandler;

    @Test
    public void getChannel(){
        LogisticsAuthEntity authEntity = new LogisticsAuthEntity();
        authEntity.setAccount("5dca6db7-6a21-4d31-a5f8-33a24a4f5b9d");
        authEntity.setPassword("b8bd24a5-35b0-4e8a-bbc0-c7458e21c7ad");
        ChanelQueryVO chanelQueryVO = new ChanelQueryVO();
        chanelQueryVO.setTransportMode("1");
        chanelQueryVO.setLogisticsAuthEntity(authEntity);
        ApiResult<List<LogisticsSaleChannelEntity>> channel = dsfLogisticsHandler.getChannel(chanelQueryVO);
        System.out.println(channel);
    }
    @Test
    public void createOrder(){
        LogisticsChannelEntity logisticsChannelEntity = new LogisticsChannelEntity();
        logisticsChannelEntity.setTaxModel("DDP");
        logisticsChannelEntity.setCode("");
        LogisticsAuthEntity authEntity = new LogisticsAuthEntity();
        authEntity.setAccount("");
        authEntity.setPassword("");
        SenderInfo senderInfo = new SenderInfo();
        senderInfo.setAddressFirst("address");
        senderInfo.setContact("contact");
        senderInfo.setCityName("newyork");
        senderInfo.setCompanyName("componeny");
        senderInfo.setName("name");
        senderInfo.setProvinceName("shenzhen");
        senderInfo.setTelNumber("12345678");
        senderInfo.setEmail("321546");
        senderInfo.setCountry("CN");
        senderInfo.setZipCode("515800");
        ReceiverInfoVO receiverInfoVO = ReceiverInfoVO.builder()
                .addressFirst("address")
                .email("123@q.con")
                .city("shenz")
                .name("mark")
                .companyName("componey")
                .contact("mark")
                .country("MX")
                .zipCode("11510")
                .province("state")
                .telNumber("123456789")
                .build();
        LogisticsProductVO logisticsProductVO = new LogisticsProductVO();
        logisticsProductVO.setEnglishUsage("materi");
        logisticsProductVO.setDeclareChineseName("物流");
        logisticsProductVO.setDeclareEnglishName("mta");
        logisticsProductVO.setPrice(new BigDecimal("12"));
        logisticsProductVO.setWeight(1999);
        logisticsProductVO.setQuantity(10);
        LogisticsOrderVO logisticsOrderVO = LogisticsOrderVO.builder()
                .deliveryNo("")
                .logisticsChannelEntity(logisticsChannelEntity)
                .logisticsAuthEntity(authEntity)
                .logisticsProductVOList(Collections.singletonList(logisticsProductVO))
                .senderInfo(senderInfo)
                .receiverInfoVO(receiverInfoVO)
                .build();
        ApiResult<LogisticsOrderResponseVO> order = dsfLogisticsHandler.createOrder(logisticsOrderVO);
        System.out.println(order);
    }

}
