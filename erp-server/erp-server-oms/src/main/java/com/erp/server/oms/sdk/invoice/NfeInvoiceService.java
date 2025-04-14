package com.erp.server.oms.sdk.invoice;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.entity.DmpSoBillDetailEntity;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.InvoiceInfoEntity;
import com.erp.model.oms.entity.InvoiceTaxEntity;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.InvoiceInfoStatusEnum;
import com.erp.model.oms.enums.InvoiceInfoUploadStatusEnum;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.erp.model.oms.enums.SoB2cNfeStatusEnum;
import com.erp.server.oms.convert.NfeInvoiceConverter;
import com.erp.server.oms.service.InvoiceInfoService;
import com.erp.server.oms.service.InvoiceTaxService;
import com.erp.server.oms.service.SkuMappingService;
import com.erp.server.oms.service.SoB2cDetailService;
import com.sdk.oms.mercadolocal.dto.MercadoInvoiceDTO;
import com.sdk.oms.mercadolocal.service.MercadoLocalSdkClientService;
import com.sdk.third.tf.TfFiscalService;
import com.sdk.third.tf.dto.NfeInvoiceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Nfe发票上传
 * @author will
 * @date 2025/4/11 09:54
 */
@Component
public class NfeInvoiceService {

    private static final Logger log = LoggerFactory.getLogger(NfeInvoiceService.class);

    @Resource
    private SoB2cDetailService soB2cDetailService;

    @Resource
    private SkuMappingService skuMappingService;

    @Resource
    private InvoiceTaxService invoiceTaxService;

    @Resource
    private InvoiceInfoService invoiceInfoService;

    @Resource
    private TfFiscalService tfFiscalService;

    @Resource
    private MercadoLocalSdkClientService mercadoLocalSdkClientService;


    @Async
    @Transactional(rollbackFor = Exception.class)
    public void createInvoice(SoB2cEntity soB2cEntity) {
        String invoiceStatus = InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode();
        String remark = "";
        String uploadStatus = InvoiceInfoUploadStatusEnum.WAIT_UPLOAD.getCode();
        try {
            NfeInvoiceDTO.NfeCreateDTO createDTO = new NfeInvoiceDTO.NfeCreateDTO();
            createDTO.setEmailDev("gray@ulanzi.cn");
            //地址信息
            NfeInvoiceDTO.NfeClienteDTO nfeClienteDTO = getNfeClienteDTO(soB2cEntity);
            createDTO.setCliente(nfeClienteDTO);
            //税务信息
            List<NfeInvoiceDTO.NfeItensDTO> nfeItensList = getNfeItensDTO(soB2cEntity);
            createDTO.setItens(nfeItensList);
            Object invoice = tfFiscalService.createInvoice(createDTO);
        }catch (Exception e){
            invoiceStatus = InvoiceInfoStatusEnum.INVOICE_FAILED.getCode();
            remark = e.getMessage();
        }
        //解析invoice数据



        //美客多自动上传发票、速卖通无需上传
        if (CharSequenceUtil.equals(soB2cEntity.getDictPlatform(), PlatformDictEnum.ALI_EXPRESS.getCode())) {
            uploadStatus = SoB2cNfeStatusEnum.NOT_NEED_UPLOAD.getCode();
        }else {
            try {
                //上传到平台
                MercadoInvoiceDTO mercadoInvoiceDTO = new MercadoInvoiceDTO();
                mercadoLocalSdkClientService.uploadInvoice(mercadoInvoiceDTO);
            } catch (Exception e) {
                uploadStatus = InvoiceInfoUploadStatusEnum.UPLOAD_FAILED.getCode();
                remark = e.getMessage();
            }
        }
        //更新开票状态
        InvoiceInfoEntity invoiceInfoEntity = invoiceInfoService.getInvoicingBySoId(soB2cEntity.getId());
        invoiceInfoEntity.setStatus(invoiceStatus);
        invoiceInfoEntity.setUploadStatus(uploadStatus);
        invoiceInfoEntity.setRemark(remark);
        invoiceInfoService.updateNfeStatusById(invoiceInfoEntity);
    }

    /**
     * 地址信息
     * @author will
     * @date 2025/4/11 15:22
     * @param soB2cEntity
     * @return NfeClienteDTO
     */
    private NfeInvoiceDTO.NfeClienteDTO getNfeClienteDTO(SoB2cEntity soB2cEntity) {
        //查询亚马逊财务配送报告
        List<DmpSoBillDetailEntity> allDmpSoBillDetailEntityList = FeignQuery.create(DmpSoBillDetailEntity.class)
                .eq(DmpSoBillDetailEntity::getPlatformCode,soB2cEntity.getPlatformCode())
                .eq(DmpSoBillDetailEntity::getShopId,soB2cEntity.getShopId())
                .eq(DmpSoBillDetailEntity::getSourcePlatform, soB2cEntity.getDictPlatform())
                .list();
        if (CollUtil.isEmpty(allDmpSoBillDetailEntityList)) {
            return null;
        }
        DmpSoBillDetailEntity dmpSoBillDetailEntity = allDmpSoBillDetailEntityList.get(0);
        NfeInvoiceDTO.NfeClienteDTO nfeClienteDTO = NfeInvoiceConverter.INSTANCE.soB2cEntityToWalmartShipDTO(dmpSoBillDetailEntity);
        return nfeClienteDTO;
    }

    /**
     * 税务信息
     * @author will
     * @date 2025/4/11 16:21
     * @param soB2cEntity
     * @return List<NfeItensDTO>
     */
    private List<NfeInvoiceDTO.NfeItensDTO> getNfeItensDTO(SoB2cEntity soB2cEntity) {
        List<NfeInvoiceDTO.NfeItensDTO> itens = new ArrayList<>();

        List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(soB2cEntity.getId());
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        //平台sku
        List<String> platformSkuNoList = detailList.stream().map(SoB2cDetailEntity::getPlatformSkuNo).distinct().collect(Collectors.toList());

        // 查询该店铺所有平台sku
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setShopIdList(Collections.singletonList(soB2cEntity.getShopId()));
        paramDTO.setPlatformList(Collections.singletonList(soB2cEntity.getDictPlatform()));
        paramDTO.setType(RuleTypeEnum.PLATFORM.getCode());
        paramDTO.setPlatformSkuNoList(platformSkuNoList);
        // 所有包含历史映射关系
        List<ListingInfoWithSkuMappingDTO> listingInfoEntityList = skuMappingService.findListDto(paramDTO);
        Map<String, List<ListingInfoWithSkuMappingDTO>> listingMap = listingInfoEntityList.stream().distinct().collect(Collectors.groupingBy(obj -> CharSequenceUtil.format("{}-{}-{}",obj.getPlatform(),obj.getPlatformSkuNo(),obj.getShopId())));

        //listingId集合
        List<String> listingIdList = listingInfoEntityList.stream().map(ListingInfoWithSkuMappingDTO::getListingId).distinct().collect(Collectors.toList());
        //查询发票税务信息
        List<InvoiceTaxEntity> invoiceTaxList = invoiceTaxService.listByListingIdList(listingIdList);
        Map<String, InvoiceTaxEntity> taxMap = invoiceTaxList.stream().collect(Collectors.toMap(InvoiceTaxEntity::getListingId, Function.identity()));

        for (SoB2cDetailEntity detailEntity :detailList) {
            NfeInvoiceDTO.NfeItensDTO nfeItensDTO = new NfeInvoiceDTO.NfeItensDTO();
            //listing信息
            List<ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingList = listingMap.get(CharSequenceUtil.format("{}-{}-{}", soB2cEntity.getDictPlatform(), detailEntity.getPlatformSkuNo(),soB2cEntity.getShopId()));
            //税务信息
            InvoiceTaxEntity invoiceTaxEntity = CollUtil.isEmpty(listingInfoWithSkuMappingList) ? null : listingInfoWithSkuMappingList.stream().filter(obj -> ObjUtil.isNotEmpty(taxMap.get(obj.getListingId()))).map(obj -> taxMap.get(obj.getListingId())).findFirst().orElse(null);
            if (ObjUtil.isEmpty(invoiceTaxEntity)) {
                throw new ServiceException(ApiError.ERROR_SKU_INVOICE_TAX_NOT_EXIST);
            }
            nfeItensDTO.setName(invoiceTaxEntity.getInvoiceProductName());
            nfeItensDTO.setSku(detailEntity.getPlatformSkuNo());
            nfeItensDTO.setNcm(invoiceTaxEntity.getInvoiceHsCode());
            nfeItensDTO.setCfopExterno(invoiceTaxEntity.getDiffStateTaxCode());
            nfeItensDTO.setCfopInterno(invoiceTaxEntity.getSameStateTaxCode());
            nfeItensDTO.setQuantity(detailEntity.getQty());
            nfeItensDTO.setUnitPrice(detailEntity.getPrice());
            itens.add(nfeItensDTO);
        }
        return itens;
    }

}
