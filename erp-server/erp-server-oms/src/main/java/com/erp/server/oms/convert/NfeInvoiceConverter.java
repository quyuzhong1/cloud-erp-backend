package com.erp.server.oms.convert;

import com.erp.model.dmp.entity.DmpSoBillDetailEntity;
import com.erp.model.oms.entity.InvoiceInfoEntity;
import com.sdk.third.tf.dto.NfeInvoiceDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface NfeInvoiceConverter {

    NfeInvoiceConverter INSTANCE = Mappers.getMapper(NfeInvoiceConverter.class);

    @Mappings({
            @Mapping(target = "bairro",  source = "state"),
            @Mapping(target = "cep", source = "postalCode"),
            @Mapping(target = "cityId", source = "city"),
            @Mapping(target = "country", source = "country"),
            @Mapping(target = "cpfCnpj", source = "taxNo"),
            @Mapping(target = "email", source = "buyerEmail"),
            @Mapping(target = "mobile", source = "buyerPhoneNumber"),
            @Mapping(target = "name", source = "buyerName"),
            @Mapping(target = "numero", constant = ""),
            @Mapping(target = "rua", source = "address1"),
            @Mapping(target = "state", source = "state"),
            @Mapping(target = "ieRg", source = "registrationNo"),
    })
    NfeInvoiceDTO.NfeClienteDTO soBillDetailEntityToNfeCliente(DmpSoBillDetailEntity dmpSoBillDetailEntity);

    @Mappings({
            @Mapping(target = "enviarEmailParaCliente", constant = "true"),
            @Mapping(target = "transactionId", source = "queryId"),
            @Mapping(target = "justificativa", source = "cancelReason"),
    })
    NfeInvoiceDTO.NfeCancelDTO invoiceInfoEntityToNfeCancel(InvoiceInfoEntity invoiceInfoEntity);

}
