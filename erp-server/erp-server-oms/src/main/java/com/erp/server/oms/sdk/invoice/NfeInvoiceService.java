package com.erp.server.oms.sdk.invoice;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FileUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpSoBillDetailEntity;
import com.erp.model.oms.dto.InvoiceInfoDTO;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.OmsAttachmentDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.*;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.oms.convert.NfeInvoiceConverter;
import com.erp.server.oms.service.*;
import com.sdk.oms.mercadolocal.dto.MercadoInvoiceDTO;
import com.sdk.oms.mercadolocal.service.MercadoLocalSdkClientService;
import com.sdk.third.tf.TfFiscalService;
import com.sdk.third.tf.dto.NfeInvoiceDTO;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.core.utils.MathUtil.removeSignAndSpace;

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
    @Lazy
    private InvoiceInfoService invoiceInfoService;

    @Resource
    private TfFiscalService tfFiscalService;

    @Resource
    private MercadoLocalSdkClientService mercadoLocalSdkClientService;

    @Resource
    private CfgInvoiceSettingDetailService cfgInvoiceSettingDetailService;
    @Resource
    private CfgInvoiceSettingService cfgInvoiceSettingService;

    @Resource
    private OmsAttachmentService omsAttachmentService;

    @Resource
    private SoB2cFinanceService soB2cFinanceService;

    @Resource
    private SoB2cService soB2cService;

    @Resource
    private ShopInfoService shopInfoService;
    @Resource
    private SoB2cReceiverService soB2cReceiverService;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private FileFeign filefeign;
    @Resource
    private OperateLogService operateLogService;

    @Transactional(rollbackFor = Exception.class)
    public Boolean createInvoice(SoB2cEntity soB2cEntity) {
        String invoiceStatus = InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode();
        Object obj = null;
        String uploadStatus = InvoiceInfoUploadStatusEnum.WAIT_UPLOAD.getCode();
        //配置信息
        CfgInvoiceSettingDetailEntity invoiceSettingDetail = cfgInvoiceSettingDetailService.getInvoiceSettingDetail(soB2cEntity.getDictPlatform(), soB2cEntity.getShopId());

        NfeInvoiceDTO.NfeCreateDTO createDTO = new NfeInvoiceDTO.NfeCreateDTO();
        try {
            createDTO.setEmailDev("gray@ulanzi.cn");
            //地址信息
            NfeInvoiceDTO.NfeClienteDTO nfeClienteDTO = getNfeClienteDTO(soB2cEntity);
            createDTO.setCliente(nfeClienteDTO);
            log.warn("地址信息已查询完成！销售订单：{}nfeClienteDTO:{}", soB2cEntity.getCode(),JSONUtil.toJsonStr(nfeClienteDTO));
            //税务信息
            getNfeItensDTO(soB2cEntity,invoiceSettingDetail,createDTO);
            log.warn("税务信息已查询完成！");
            //token
            createDTO.setTokenEmpresa(invoiceSettingDetail.getToken());
            //付款信息
            getPayMentDTO(createDTO);
            log.warn("付款信息已查询完成！");
             obj = tfFiscalService.createInvoice(createDTO);
            log.warn("创建发票接口调用成功！请求参数-body:{},返回值：{}",JSONUtil.toJsonStr(createDTO), JSONUtil.toJsonStr(obj));
        }catch (Exception e){
            log.error("创建发票失败,返回信息:{}", e.getMessage());
            log.error("请求参数-body:{}", JSONUtil.toJsonStr(createDTO));
            //开票失败更新开票状态
            InvoiceInfoEntity invoiceInfoEntity = invoiceInfoService.getInvoicingBySoId(soB2cEntity.getId());
            invoiceInfoEntity.setStatus(InvoiceInfoStatusEnum.INVOICE_FAILED.getCode());
            invoiceInfoEntity.setRemark(e.getMessage());
            invoiceInfoService.updateNfeStatusById(invoiceInfoEntity);
            operateLogService.addModuleOperateLog(e.getMessage(), ModuleTypeEnum.INVOICE_INFO.getCode(), soB2cEntity.getId(),"开票失败");
            return Boolean.FALSE;
        }
        //开票成功
        NfeInvoiceDTO.NfeSuccessResultDTO resultDTO = null;
        try {
            // 先尝试解析为JSONArray
            if (obj instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) obj;
                if (jsonArray.isEmpty()) {
                    throw new ServiceException("空数组无法解析");
                }
                resultDTO = jsonArray.get(0, NfeInvoiceDTO.NfeSuccessResultDTO.class);
            }
            // 再尝试解析为JSONObject
            else if (obj instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) obj;
                resultDTO = jsonObject.toBean(NfeInvoiceDTO.NfeSuccessResultDTO.class);
            }
            // 处理字符串类型的原始JSON
            else if (obj instanceof String) {
                // 尝试解析为JSONArray
                if (obj.toString().trim().startsWith("[")) {
                    JSONArray jsonArray = JSONUtil.parseArray((String) obj);
                    resultDTO = jsonArray.get(0, NfeInvoiceDTO.NfeSuccessResultDTO.class);
                }
                // 尝试解析为JSONObject
                else {
                    JSONObject jsonObject = JSONUtil.parseObj((String) obj);
                    resultDTO = jsonObject.toBean(NfeInvoiceDTO.NfeSuccessResultDTO.class);
                }
            }
            // 处理其他未知类型
            else {
                throw new IllegalArgumentException("不支持的数据类型");
            }
        } catch (Exception e) {
            log.error("解析信息失败, 原始数据: {}, 错误: {}",
                    JSONUtil.toJsonStr(obj),
                    e.getMessage());
//            throw new ServiceException(ApiError.ERROR_INVOICE_NFE_CREATE_JSON_HANDLE);
            //开票失败更新开票状态
            InvoiceInfoEntity invoiceInfoEntity = invoiceInfoService.getInvoicingBySoId(soB2cEntity.getId());
            invoiceInfoEntity.setStatus(InvoiceInfoStatusEnum.INVOICE_FAILED.getCode());
            invoiceInfoEntity.setRemark(e.getMessage());
            invoiceInfoService.updateNfeStatusById(invoiceInfoEntity);
            operateLogService.addModuleOperateLog(e.getMessage(), ModuleTypeEnum.INVOICE_INFO.getCode(), soB2cEntity.getId(),"开票失败");
            return Boolean.FALSE;
        }
        if (Objects.isNull(resultDTO) || Objects.isNull(resultDTO.getSuccesso())||!resultDTO.getSuccesso() || 200 !=  resultDTO.getStatus()) {
            log.error("创建发票失败,返回错误信息,返回信息:{}", JSONUtil.toJsonStr(resultDTO));
//            throw new ServiceException(ApiError.ERROR_INVOICE_NFE_CREATE_INVOICE,"未知");
            //开票失败更新开票状态
            InvoiceInfoEntity invoiceInfoEntity = invoiceInfoService.getInvoicingBySoId(soB2cEntity.getId());
            invoiceInfoEntity.setStatus(InvoiceInfoStatusEnum.INVOICE_FAILED.getCode());
            invoiceInfoEntity.setRemark(JSONUtil.toJsonStr(resultDTO));
            invoiceInfoService.updateNfeStatusById(invoiceInfoEntity);
            operateLogService.addModuleOperateLog(CharSequenceUtil.format("返回信息：{}",JSONUtil.toJsonStr(resultDTO)), ModuleTypeEnum.INVOICE_INFO.getCode(), soB2cEntity.getId(),"开票失败");
            return Boolean.FALSE;
        }
        //回写序列号和起始编号
        cfgInvoiceSettingService.updateSerialNoById(invoiceSettingDetail.getMainId(),resultDTO.getSerie(),resultDTO.getNumeroNfe());
        //更新开票状态
        InvoiceInfoEntity invoiceInfoEntity = invoiceInfoService.getInvoicingBySoId(soB2cEntity.getId());
        invoiceInfoEntity.setStatus(invoiceStatus);
        invoiceInfoEntity.setUploadStatus(PlatformDictEnum.ALI_EXPRESS.getCode().equals(soB2cEntity.getDictPlatform()) ? InvoiceInfoUploadStatusEnum.NOT_NEED_UPLOAD.getCode() : uploadStatus);
        invoiceInfoEntity.setQueryId(resultDTO.getId());
        invoiceInfoEntity.setQueryKey(getQueryKey(resultDTO.getLink_xml()));
        invoiceInfoEntity.setPlatformInvoiceNo(resultDTO.getRecibo());
        invoiceInfoEntity.setNo(resultDTO.getSerie());
        invoiceInfoEntity.setStartCode(String.valueOf(resultDTO.getNumeroNfe()));
        invoiceInfoService.updateNfeStatusById(invoiceInfoEntity);

        //上传xml、pdf
        uploadFile(invoiceInfoEntity.getId(),resultDTO.getLink_xml(),resultDTO.getLink_nota());

        //是否自动上传发票
        if (invoiceSettingDetail.getIsAutoUpload() && CharSequenceUtil.equals(PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode(),soB2cEntity.getDictPlatform())) {
            invoiceInfoService.uploadNfeInvoice(soB2cEntity,invoiceInfoEntity.getId());
        }
        return Boolean.TRUE;
    }

    /**
     * 根据xml文件获取queryKey
     * @author zdy
     * @date 2025/7/18 15:37
     * @param linkXml
     * @return
     */
    public String getQueryKey(String linkXml) {
        if (CharSequenceUtil.isEmpty(linkXml)) {
            return "";
        }
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new URL(linkXml));
            Element root = document.getRootElement();
            String queryKey = root.element("NFe").element("infNFe").attributeValue("Id");
            return queryKey;
        } catch (Exception e) {
            log.error("获取queryKey失败,xml文件:{}异常：{}",linkXml, e.getMessage());
            return "";
        }
    }

    /**
     * 付款信息
     * @author will
     * @date 2025/4/18 15:37
     * @param createDTO
     * @return void
     */
    private void getPayMentDTO(NfeInvoiceDTO.NfeCreateDTO createDTO) {
        NfeInvoiceDTO.NfePayMentDTO nfePayMentDTO = new NfeInvoiceDTO.NfePayMentDTO();
        nfePayMentDTO.setAmount(createDTO.getFinalTotal());
        //默认现金
        nfePayMentDTO.setMethod("cash");
        nfePayMentDTO.setCardType(nfePayMentDTO.getMethod());
        nfePayMentDTO.setVencimento(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        nfePayMentDTO.setNote("");
        createDTO.setPayment(Collections.singletonList(nfePayMentDTO));
    }

    /**
     * 上传Nfe发票
     * @author will
     * @date 2025/4/15 16:32
     * @param soB2cEntity
     * @return void
     */
    public void uploadNfeInvoice (SoB2cEntity soB2cEntity) {
        //上传到平台
        MercadoInvoiceDTO mercadoInvoiceDTO = new MercadoInvoiceDTO();
        mercadoInvoiceDTO.setShopId(soB2cEntity.getShopId());
        String extendData = soB2cEntity.getExtendData();
        JSONObject entries = JSONUtil.parseObj(extendData);
        Object shipmentId = entries.get("shipmentId");
        mercadoInvoiceDTO.setShipmentId(ObjUtil.isEmpty(shipmentId) ? "" : shipmentId.toString());

        InvoiceInfoDTO.AttachDTO attachDTO = invoiceInfoService.getNewInvoicedAttachBySoId(soB2cEntity.getId(), InvoiceInfoInvoiceTypeEnum.NFE.getCode(), AttachmentTypeEnum.INVOICE_INFO_XML.getCode());
        if (ObjUtil.isEmpty(attachDTO)) {
            throw new ServiceException("NF-e发票未找到xml文件");
        }
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(FastDFSClientUtil.publicUrl + attachDTO.getAttachUrl());
            try (CloseableHttpResponse response = client.execute(request)) {
                String xmlContent = EntityUtils.toString(response.getEntity(), "UTF-8");
                mercadoInvoiceDTO.setXmlContent(xmlContent);
            }
        } catch (Exception e) {
            throw  new ServiceException("获取xml文件失败");
        }
        //NF-e发票
        mercadoLocalSdkClientService.uploadInvoice(mercadoInvoiceDTO);
    }


    /**
     * 运费
     * @author will
     * @date 2025/4/14 14:17
     * @param soB2cEntity
     * @param invoiceSettingDetail
     * @return BigDecimal
     */
    private BigDecimal getShipCost(SoB2cEntity soB2cEntity,CfgInvoiceSettingDetailEntity invoiceSettingDetail) {
        if (!CharSequenceUtil.equals(PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode(),soB2cEntity.getDictPlatform())) {
            return BigDecimal.ZERO;
        }
        if (ObjUtil.isEmpty(invoiceSettingDetail) || !invoiceSettingDetail.getIsContainShipFee()){
            return BigDecimal.ZERO;
        }
        SoB2cFinanceEntity financeEntity = soB2cFinanceService.getByMainId(soB2cEntity.getId());
        if (Objects.isNull(financeEntity)){
            throw new ServiceException("销售订单财务信息不存在");
        }
        if (BigDecimal.ZERO.compareTo(financeEntity.getShippingCost()) == 0){
            return BigDecimal.ZERO;
        }
        if (CharSequenceUtil.isBlank(financeEntity.getCurrency())){
            throw new ServiceException("销售订单财务信息币种不存在");
        }
        if (CurrencyEnum.BRL.getCurrencyCode().equals(soB2cEntity.getCurrency())){
            return financeEntity.getShippingCost();
        }
        //转换成人民币
        BigDecimal rate;
        try {
            rate = dmpTaskFeign.getRate(soB2cEntity.getBillDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), soB2cEntity.getCurrency());
        }catch (Exception e){
            throw new ServiceException(soB2cEntity.getCurrency() + "获取汇率失败");
        }
        BigDecimal rate2;
        try {
            rate2 = dmpTaskFeign.getRate(soB2cEntity.getBillDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), CurrencyEnum.BRL.getCurrencyCode());
            return MathUtil.divide(MathUtil.multiplyWithFour(rate,financeEntity.getShippingCost()), rate2);
        }catch (Exception e){
            throw new ServiceException(CurrencyEnum.BRL.getCurrencyName() + "获取汇率失败");
        }
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
        SoB2cReceiverEntity receiverEntity = soB2cReceiverService.getByMainId(soB2cEntity.getId());
        if (ObjUtil.isEmpty(receiverEntity)) {
            throw new ServiceException("B2C买家信息记录不存在");
        }
        if (CollUtil.isEmpty(allDmpSoBillDetailEntityList)) {
            if (PlatformDictEnum.ALI_EXPRESS.getCode().equals(soB2cEntity.getDictPlatform())
                    || (PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equals(soB2cEntity.getDictPlatform()))){
                throw new ServiceException("开票地址信息不能为空");
            }
           //按照销售信息赋值
            return getNfeClienteDTOBySoB2c(receiverEntity);
        }
        DmpSoBillDetailEntity dmpSoBillDetailEntity = allDmpSoBillDetailEntityList.get(0);
        NfeInvoiceDTO.NfeClienteDTO nfeClienteDTO = NfeInvoiceConverter.INSTANCE.soBillDetailEntityToNfeCliente(dmpSoBillDetailEntity);
        String newCep = CharSequenceUtil.isBlank(nfeClienteDTO.getCep()) ? "" : removeSignAndSpace(nfeClienteDTO.getCep());
        nfeClienteDTO.setCep(newCep);
        nfeClienteDTO.setBairro(getBairroStr(soB2cEntity.getDictPlatform(), nfeClienteDTO.getRua(),nfeClienteDTO.getBairro()));
        if (CharSequenceUtil.isNotBlank(receiverEntity.getInvoiceAddress())){
            nfeClienteDTO.setRua(receiverEntity.getInvoiceAddress());
        }else {
            nfeClienteDTO.setRua(getRuaStr(soB2cEntity.getDictPlatform(), nfeClienteDTO.getRua()));
        }
        //州（省份）二字码缩写
        List<DictCityEntity> dictCityList = FeignQuery.create(DictCityEntity.class)
                .eq(DictCityEntity::getCountryCode, nfeClienteDTO.getCountry())
                .eq(DictCityEntity::getType,"province")
                .last("and (code_en = '" + nfeClienteDTO.getState() + "' or code_pt = '" + nfeClienteDTO.getState() + "')")
                .list();
        if (CollUtil.isEmpty(dictCityList)) {
            throw new ServiceException("开票省份/州二字码未找到");
        }
        nfeClienteDTO.setUf(dictCityList.get(0).getCode());
        nfeClienteDTO.setState(dictCityList.get(0).getCodePt());
        return nfeClienteDTO;
    }

    private String getRuaStr(String dictPlatform, String rua) {
        if (CharSequenceUtil.isBlank(rua)){
            return rua;
        }
        if (PlatformDictEnum.ALI_EXPRESS.getCode().equals(dictPlatform)){
            //按照英文分号分割，取分号后面的内容
            if (rua.contains(";")){
                String[] split = rua.split(";");
                StringBuilder sb = new StringBuilder();
                for (int i = 1 ; i < split.length; i++){
                    if (i != 1){
                        sb.append(";").append(split[i]);
                    }else {
                        sb.append(split[i]);
                    }
                }
                return sb.toString();
            }
            return rua;
        }else if (PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equals(dictPlatform)){
            //英文冒号-:，符号前信息拼接到bairro，符号后信息保留到rua
            if (rua.contains(":")){
                String[] split = rua.split(":");
                StringBuilder sb = new StringBuilder();
                for (int i = 1 ; i < split.length; i++){
                    if (i != 1){
                        sb.append(":").append(split[i]);
                    }else {
                        sb.append(split[i]);
                    }
                }
                return sb.toString();
            }
            return rua;
        }else {
            return rua;
        }
    }

    private String getBairroStr(String dictPlatform, String rua, String bairro) {
        if (CharSequenceUtil.isBlank(rua)){
            return bairro;
        }
        if (PlatformDictEnum.ALI_EXPRESS.getCode().equals(dictPlatform)){
            //英文分号-;，符号前信息拼接到bairro，符号后信息保留到rua
            if (rua.contains(";")){
                String[] split = rua.split(";");
                return split[0];
            }
        }else if (PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equals(dictPlatform)){
            //英文冒号-:，符号前信息拼接到bairro，符号后信息保留到rua
            if (rua.contains(":")){
                String[] split = rua.split(":");
                return split[0];
            }
        }
        return bairro;
    }

    private NfeInvoiceDTO.NfeClienteDTO getNfeClienteDTOBySoB2c(SoB2cReceiverEntity receiverEntity) {
        NfeInvoiceDTO.NfeClienteDTO nfeClienteDTO = NfeInvoiceConverter.INSTANCE.soB2cReceiverEntityToNfeCliente(receiverEntity);
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getBairro())){
            nfeClienteDTO.setBairro(receiverEntity.getFirstAddress() );
        }
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getMobile())){
            nfeClienteDTO.setMobile(receiverEntity.getReceiverTelNumber());
        }
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getRua())){
            nfeClienteDTO.setRua(receiverEntity.getSecondAddress() + receiverEntity.getFullAddress());
        }
        //州（省份）二字码缩写
        List<DictCityEntity> dictCityList = FeignQuery.create(DictCityEntity.class)
                .eq(DictCityEntity::getCountryCode, nfeClienteDTO.getCountry())
                .eq(DictCityEntity::getType,"province")
                .last("and (code_en = '" + nfeClienteDTO.getState() + "' or code_pt = '" + nfeClienteDTO.getState() + "' or code = '"+ nfeClienteDTO.getState() + "')")
                .list();
        if (CollUtil.isEmpty(dictCityList)) {
            throw new ServiceException("开票省份/州二字码未找到");
        }
        nfeClienteDTO.setUf(dictCityList.get(0).getCode());
        nfeClienteDTO.setState(dictCityList.get(0).getCodePt());
        return nfeClienteDTO;
    }


    /**
     * 税务信息
     * @author will
     * @date 2025/4/11 16:21
     * @param soB2cEntity
     * @return List<NfeItensDTO>
     */
    private void getNfeItensDTO(SoB2cEntity soB2cEntity,CfgInvoiceSettingDetailEntity invoiceSettingDetail,NfeInvoiceDTO.NfeCreateDTO createDTO) {
        List<NfeInvoiceDTO.NfeItensDTO> itens = new ArrayList<>();

        //财务信息
        SoB2cFinanceEntity financeEntity = soB2cFinanceService.getByMainId(soB2cEntity.getId());
        if (ObjUtil.isEmpty(financeEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_FINANCE_NOT_EXIST);
        }

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

        //店铺名称
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(soB2cEntity.getShopId());
        if (ObjUtil.isEmpty(shopInfoEntity)) {
            throw new ServiceException(ApiError.ERROR_92058);
        }

        BigDecimal valorTotal = BigDecimal.ZERO;
        for (SoB2cDetailEntity detailEntity :detailList) {
            NfeInvoiceDTO.NfeItensDTO nfeItensDTO = new NfeInvoiceDTO.NfeItensDTO();
            //listing信息
            List<ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingList = listingMap.get(CharSequenceUtil.format("{}-{}-{}", soB2cEntity.getDictPlatform(), detailEntity.getPlatformSkuNo(),soB2cEntity.getShopId()));
            //税务信息
            InvoiceTaxEntity invoiceTaxEntity = CollUtil.isEmpty(listingInfoWithSkuMappingList) ? null : listingInfoWithSkuMappingList.stream().filter(obj -> ObjUtil.isNotEmpty(taxMap.get(obj.getListingId()))).map(obj -> taxMap.get(obj.getListingId())).findFirst().orElse(null);
            if (Objects.isNull(invoiceTaxEntity)) {
                throw new ServiceException(ApiError.ERROR_SKU_INVOICE_TAX_NOT_EXIST,detailEntity.getPlatformSkuNo(),shopInfoEntity.getName());
            }
            nfeItensDTO.setName(invoiceTaxEntity.getInvoiceProductName());
            nfeItensDTO.setSku(detailEntity.getPlatformSkuNo());
            nfeItensDTO.setNcm(invoiceTaxEntity.getInvoiceHsCode());
            nfeItensDTO.setCfopExterno(invoiceTaxEntity.getDiffStateTaxCode());
            nfeItensDTO.setCfopInterno(invoiceTaxEntity.getSameStateTaxCode());
            nfeItensDTO.setQuantity(detailEntity.getQty());
            nfeItensDTO.setCoPedClienteApi(soB2cEntity.getCode());

            //产品金额
            nfeItensDTO.setUnitPrice(getUnitPrice(soB2cEntity,detailEntity,invoiceSettingDetail));
            itens.add(nfeItensDTO);
            valorTotal = MathUtil.add(valorTotal,MathUtil.multiplyWithTwo(nfeItensDTO.getUnitPrice(),nfeItensDTO.getQuantity()));
        }
        createDTO.setItens(itens);
        //查询运费
        BigDecimal shipCost = getShipCost(soB2cEntity, invoiceSettingDetail);
        BigDecimal total = MathUtil.add(valorTotal, shipCost);
        createDTO.setValorTotal(total);
        createDTO.setFinalTotal(total);
    }

    /**
     * 真实售价
     *
     * @param soB2cEntity
     * @param detailEntity
     * @param invoiceSettingDetail
     * @return BigDecimal
     * @author will
     * @date 2025/4/14 14:42
     */
    private BigDecimal getUnitPrice (SoB2cEntity soB2cEntity, SoB2cDetailEntity detailEntity, CfgInvoiceSettingDetailEntity invoiceSettingDetail) {
        BigDecimal price = detailEntity.getPrice();
        if (ObjUtil.isEmpty(invoiceSettingDetail)) {
            price = detailEntity.getPrice();
        }
        if (CharSequenceUtil.equals(invoiceSettingDetail.getDictInvoiceRule(), InvoiceRuleEnum.AMOUNT.getCode())) {
            price = detailEntity.getPrice();
        }
        if (CharSequenceUtil.equals(invoiceSettingDetail.getDictInvoiceRule(), InvoiceRuleEnum.CUSTOM.getCode())) {
            price = MathUtil.multiplyWithTwo(detailEntity.getPrice(),invoiceSettingDetail.getRatio()) ;
        }
        if (CharSequenceUtil.equals(invoiceSettingDetail.getDictInvoiceRule(), InvoiceRuleEnum.DEDUCT.getCode())) {
            price = MathUtil.subtract(detailEntity.getPrice(),detailEntity.getSaleFee()) ;
        }
        String currency = detailEntity.getCurrency();
        if(CurrencyEnum.BRL.getCurrencyCode().equals(currency)){
            return price;
        }
        BigDecimal exchangeRate = detailEntity.getExchangeRate();
        try {
            BigDecimal rate = dmpTaskFeign.getRate(soB2cEntity.getBillDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), CurrencyEnum.BRL.getCurrencyCode());
            return MathUtil.divide(MathUtil.multiplyWithFour(exchangeRate,price), rate);
        }catch (Exception e){
            throw new ServiceException( CurrencyEnum.BRL.getCurrencyName() + "汇率为空，请维护汇率后再提交");
        }
    }

    /**
     * 取消发票
     * @author will
     * @date 2025/4/14 14:58
     * @param nfeCancelDTO
     * @return void
     */
    public void cancelInvoice(InvoiceInfoEntity invoiceInfoEntity,NfeInvoiceDTO.NfeCancelDTO nfeCancelDTO) {
        //b2c订单信息
        SoB2cEntity soB2cEntity = soB2cService.getById(invoiceInfoEntity.getSoId());
        if (Objects.isNull(soB2cEntity)){
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        if (CharSequenceUtil.isBlank(soB2cEntity.getDictPlatform()) || CharSequenceUtil.isBlank(soB2cEntity.getShopId())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST_PLATFORM_SHOP,soB2cEntity.getCode());
        }
        //税务信息
        CfgInvoiceSettingDetailEntity invoiceSettingDetail = cfgInvoiceSettingDetailService.getInvoiceSettingDetail(soB2cEntity.getDictPlatform(), soB2cEntity.getShopId());
        nfeCancelDTO.setTokenEmpresa(invoiceSettingDetail.getToken());
        Object obj;
        try {
             obj = tfFiscalService.cancelInvoice(nfeCancelDTO);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_INVOICE_NFE_CANCEL,e.getMessage());
        }
    }

    /**
     * 退票
     * @param invoiceInfoEntity
     * @param nfeReturnDTO
     */
    public void returnInvoice(InvoiceInfoEntity invoiceInfoEntity,NfeInvoiceDTO.NfeReturnDTO nfeReturnDTO) {
        //b2c订单信息
        SoB2cEntity soB2cEntity = soB2cService.getById(invoiceInfoEntity.getSoId());
        if (Objects.isNull(soB2cEntity)){
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        if (CharSequenceUtil.isBlank(soB2cEntity.getDictPlatform()) || CharSequenceUtil.isBlank(soB2cEntity.getShopId())) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST_PLATFORM_SHOP,soB2cEntity.getCode());
        }
        //税务信息
        CfgInvoiceSettingDetailEntity invoiceSettingDetail = cfgInvoiceSettingDetailService.getInvoiceSettingDetail(soB2cEntity.getDictPlatform(), soB2cEntity.getShopId());
        nfeReturnDTO.setTokenEmpresa(invoiceSettingDetail.getToken());
        Object obj;
        try {
            log.error("退票接口请求：{}", JSONUtil.toJsonStr(nfeReturnDTO));
            obj = tfFiscalService.returnInvoice(nfeReturnDTO);
            log.error("退票接口返回：{}", JSONUtil.toJsonStr(obj));
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_INVOICE_NFE_RETURN,e.getMessage());
        }
        //解析是否成功，失败抛出异常原因
        NfeInvoiceDTO.NfeReturnResultDTO resultDTO = JSONUtil.toBean(obj.toString(), NfeInvoiceDTO.NfeReturnResultDTO.class);
        if (Objects.isNull(resultDTO) || resultDTO.getErro()){
            throw new ServiceException("退票接口异常：{}",resultDTO.getMsg());
        }
    }
    /**
     * 作废
     * @param nfeVoidedDTO
     */
    public void voidedInvoice(NfeInvoiceDTO.NfeVoidedDTO nfeVoidedDTO) {
        Object obj;
        try {
            log.error("作废接口请求：{}", JSONUtil.toJsonStr(nfeVoidedDTO));
            obj = tfFiscalService.voidedInvoice(nfeVoidedDTO);
            log.error("作废接口返回：{}", JSONUtil.toJsonStr(obj));
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_INVOICE_NFE_VOIDED,e.getMessage());
        }
        //{"retorno":{"attributes":{"versao":"4.00"},"infInut":{"tpAmb":"1","verAplic":"SP_NFE_PL009_V4","cStat":"102","xMotivo":"Inutilização de número homologado","cUF":"35","ano":"25","CNPJ":"59399522000150","mod":"55","serie":"1","nNFIni":"1","nNFFin":"2","dhRecbto":"2025-07-10T00:53:25-03:00","nProt":"135251896535761"}}}

    }
    /**
     * 更新Cce数据
     * @author will
     * @date 2025/4/15 11:53
     * @param invoiceInfoEntity
     * @param nfeCceDTO
     * @return void
     */
    public void updateCceInvoice(InvoiceInfoEntity invoiceInfoEntity,NfeInvoiceDTO.NfeCceDTO nfeCceDTO) {
        //b2c订单信息
        SoB2cEntity soB2cEntity = soB2cService.getById(invoiceInfoEntity.getSoId());
        //税务信息
        CfgInvoiceSettingDetailEntity invoiceSettingDetail = cfgInvoiceSettingDetailService.getInvoiceSettingDetail(soB2cEntity.getDictPlatform(), soB2cEntity.getShopId());
        nfeCceDTO.setTokenEmpresa(invoiceSettingDetail.getToken());
        Object obj;
        try {
            obj = tfFiscalService.updateCceInvoice(nfeCceDTO);
        } catch (Exception e) {
            throw new ServiceException(ApiError.ERROR_INVOICE_NFE_UPDATE_CCE,e.getMessage());
        }
        NfeInvoiceDTO.NfeCceResultDTO resultDTO = new NfeInvoiceDTO.NfeCceResultDTO();
        try {
            //解析obj
            resultDTO = JSONUtil.toBean(obj.toString(), NfeInvoiceDTO.NfeCceResultDTO.class);
        } catch (Exception e) {
            log.error("解析信息失败,返回信息:{}", JSONUtil.toJsonStr(resultDTO));
            throw new ServiceException(ApiError.ERROR_INVOICE_NFE_CREATE_JSON_HANDLE);
        }
        if (resultDTO.getErro() || 200 !=  resultDTO.getStatus()) {
            log.error("创建发票失败,返回错误信息,返回信息:{}", JSONUtil.toJsonStr(resultDTO));
            throw new ServiceException(ApiError.ERROR_INVOICE_NFE_UPDATE_CCE,"未知");
        }
        //上传发票
        uploadFile(invoiceInfoEntity.getId(),resultDTO.getUrlXmlUpload(),"");
        //是否自动上传发票
        if (CharSequenceUtil.equals(PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode(),soB2cEntity.getDictPlatform())) {
            invoiceInfoService.uploadNfeInvoice(soB2cEntity,invoiceInfoEntity.getId());
        }
    }

    /**
     * 公司token不存在
     * @author will
     * @date 2025/4/14 16:56
     * @param platform
     * @param shopId
     * @return String
     */
    private String getCompanyToken(String platform,String shopId) {
        CfgInvoiceSettingDetailEntity invoiceSettingDetail = cfgInvoiceSettingDetailService.getInvoiceSettingDetail(platform, shopId);
        if (ObjUtil.isEmpty(invoiceSettingDetail) || CharSequenceUtil.isBlank(invoiceSettingDetail.getToken())) {
            throw new ServiceException(ApiError.ERROR_INVOICE_COMPANY_TOKEN_NOT_EXIST);
        }
        return invoiceSettingDetail.getToken();
    }

    /**
     * 上传
     * @author will
     * @date 2025/4/14 15:14
     * @param invoiceXmlUrl
     * @param invoicePdfUrl
     * @return void
     */
    private void uploadFile (String invoiceId,String invoiceXmlUrl,String invoicePdfUrl) {
        if (CharSequenceUtil.isBlank(invoiceXmlUrl) && CharSequenceUtil.isBlank(invoicePdfUrl)) {
            throw new ServiceException(ApiError.ERROR_INVOICE_NFE_UPLOAD_NOT_EXIST);
        }
        List<OmsAttachmentDTO.UpdateDTO> addOrUpdateList = new ArrayList<>();
        //上传xml
        if (CharSequenceUtil.isNotBlank(invoiceXmlUrl)) {
            try {

                MultipartFile xmlFile = readFileUrl(invoiceXmlUrl,AttachmentTypeEnum.INVOICE_INFO_XML.getCode());
                String xmlUrl = filefeign.uploadFile(xmlFile);
                addOrUpdateList.add(new OmsAttachmentDTO.UpdateDTO(AttachmentTypeEnum.INVOICE_INFO_XML.getCode(),xmlUrl,xmlFile.getOriginalFilename(),invoiceId));
            } catch (Exception e) {
                log.error("发票XML上传失败");
            }
        }
        //上传pdf
       if (CharSequenceUtil.isNotBlank(invoicePdfUrl)) {
           try {
               MultipartFile pdfFile = readFileUrl(invoicePdfUrl,AttachmentTypeEnum.INVOICE_INFO_PDF.getCode());
               String pdfUrl = filefeign.uploadFile(pdfFile);
               addOrUpdateList.add(new OmsAttachmentDTO.UpdateDTO( AttachmentTypeEnum.INVOICE_INFO_PDF.getCode(),pdfUrl,pdfFile.getOriginalFilename(),invoiceId));
           } catch (Exception e) {
               log.error("发票PDF上传失败");
           }
       }
      omsAttachmentService.batchAddOrUpdate(addOrUpdateList);
    }
    /**
     * 读取文件
     * @author will
     * @date 2025/4/25 15:01
     * @param invoiceUrl
     * @return MultipartFile
     */
    private MultipartFile readFileUrl (String invoiceUrl,String type) {
        // 原始文件路径（可能无后缀）
        File sourceFile = new File(invoiceUrl);
        String defaultSuffix = AttachmentTypeEnum.INVOICE_INFO_XML.getCode().equals(type) ? "xml" : "pdf";
        return FileUtil.toMultipartFile(invoiceUrl,sourceFile.getName(),defaultSuffix);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createInvoiceProcess(SoB2cEntity soB2cEntity, InvoiceInfoEntity invoiceInfoEntity) {
         log.warn("记录异步开票日志开始：{}",invoiceInfoEntity.getCode());
        try {
            Boolean result = this.createInvoice(soB2cEntity);
            if (result){
                //添加日志
                operateLogService.addModuleOperateLog(CharSequenceUtil.format("销售订单【{}】生成NF-e发票",soB2cEntity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), invoiceInfoEntity.getId(), "生成NF-e发票操作");
            }else {
                //添加日志
                operateLogService.addModuleOperateLog(CharSequenceUtil.format("销售订单【{}】生成NF-e发票",soB2cEntity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), invoiceInfoEntity.getId(), "开票失败");
            }
        }catch (Exception e){
            InvoiceInfoEntity entity = invoiceInfoService.getInvoicingBySoId(soB2cEntity.getId());
            entity.setStatus(InvoiceInfoStatusEnum.INVOICE_FAILED.getCode());
            entity.setRemark(e.getMessage());
            invoiceInfoService.updateNfeStatusById(entity);
            operateLogService.addModuleOperateLog(e.getMessage(), ModuleTypeEnum.INVOICE_INFO.getCode(), soB2cEntity.getId(),"开票失败");
        }
        log.warn("记录异步开票日志结束：{}",invoiceInfoEntity.getCode());
    }
}

