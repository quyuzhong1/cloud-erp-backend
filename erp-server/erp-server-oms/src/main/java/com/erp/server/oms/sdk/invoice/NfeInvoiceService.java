package com.erp.server.oms.sdk.invoice;

import com.erp.model.oms.dto.NfeInvoiceDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Nfe发票上传
 * @author will
 * @date 2025/4/11 09:54
 */
@Component
public class NfeInvoiceService {

    private static final Logger log = LoggerFactory.getLogger(NfeInvoiceService.class);

    @Resource
    private DmpAmazonFeign dmpAmazonFeign;

    @Async
    public void createInvoice(SoB2cEntity soB2cEntity) {
        NfeInvoiceDTO.NfeCreateDTO createDTO = new NfeInvoiceDTO.NfeCreateDTO();
        createDTO.setEmailDev("gray@ulanzi.cn");
        getNfeClienteDTO();
    }


    private void getNfeClienteDTO() {

    }

    private void getNfeItensDTO() {

    }

}
