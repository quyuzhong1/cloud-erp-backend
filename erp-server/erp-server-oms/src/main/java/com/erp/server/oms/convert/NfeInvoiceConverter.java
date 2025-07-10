package com.erp.server.oms.convert;

import com.erp.model.dmp.entity.DmpSoBillDetailEntity;
import com.erp.model.oms.entity.InvoiceInfoEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
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
            @Mapping(target = "bairro", source = "state"),
            @Mapping(target = "cep", source = "postalCode"),
            @Mapping(target = "cityId", source = "city"),
            @Mapping(target = "country", source = "country"),
            @Mapping(target = "cpfCnpj", source = "taxNo"),
            @Mapping(target = "email", source = "buyerEmail"),
            @Mapping(target = "mobile", source = "buyerPhoneNumber"),
            @Mapping(target = "name", source = "buyerName"),
            @Mapping(target = "numero", constant = "1"),
            @Mapping(target = "rua", source = "address1"),
            @Mapping(target = "state", source = "state"),
            @Mapping(target = "ieRg", source = "registrationNo"),
            @Mapping(target = "uf", ignore = true)
    })
    NfeInvoiceDTO.NfeClienteDTO soBillDetailEntityToNfeCliente(DmpSoBillDetailEntity dmpSoBillDetailEntity);

    @Mappings({
            @Mapping(target = "enviarEmailParaCliente", constant = "true"),
            @Mapping(target = "transactionId", source = "queryId"),
            @Mapping(target = "justificativa", source = "cancelReason"),
    })
    NfeInvoiceDTO.NfeCancelDTO invoiceInfoEntityToNfeCancel(InvoiceInfoEntity invoiceInfoEntity);
    @Mappings({
            @Mapping(target = "bairro", source = "firstAddress"),
            @Mapping(target = "cep", source = "postCode"),
            @Mapping(target = "cityId", source = "cityName"),
            @Mapping(target = "country", source = "country"),
            @Mapping(target = "cpfCnpj", source = "receiverTaxNo"),
            @Mapping(target = "email", source = "email"),
            @Mapping(target = "mobile", source = "telNumber"),
            @Mapping(target = "name", source = "name"),
            @Mapping(target = "numero", constant = "1"),
            @Mapping(target = "rua", source = "invoiceAddress"),
            @Mapping(target = "state", source = "provinceName"),
            @Mapping(target = "ieRg", constant = ""),
            @Mapping(target = "uf", ignore = true)
    })
    NfeInvoiceDTO.NfeClienteDTO soB2cReceiverEntityToNfeCliente(SoB2cReceiverEntity receiverEntity);
}
