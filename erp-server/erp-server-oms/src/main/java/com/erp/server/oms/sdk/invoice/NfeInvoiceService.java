package com.erp.server.oms.sdk.invoice;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.enums.CurrencyEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FileUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
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
import com.common.business.constant.BusinessCommonConstants;
import com.common.core.utils.Md5Util;
import com.erp.model.oms.entity.CfgSettingEntity;
import com.sdk.oms.mercadolocal.dto.MercadoInvoiceDTO;
import com.sdk.oms.mercadolocal.service.MercadoLocalSdkClientService;
import com.sdk.oms.shopee.dto.base.ShopeeResponse;
import com.sdk.oms.shopee.dto.order.request.OrderRequest;
import com.sdk.oms.shopee.dto.order.response.OrderDetail;
import com.sdk.oms.shopee.dto.order.response.OrderItemDetail;
import com.sdk.oms.shopee.dto.order.response.RecipientAddress;
import com.sdk.oms.shopee.service.ShopeeOrderService;
import com.sdk.oms.shopee.utils.ShopeeApiUtils;
import com.sdk.third.tf.TfFiscalService;
import com.sdk.third.tf.dto.CancelInvoiceDTO;
import com.sdk.third.tf.dto.CancelInvoiceResponseDTO;
import com.sdk.third.tf.dto.CreateInvoiceDTO;
import com.sdk.third.tf.dto.CreateInvoiceResponseDTO;
import com.sdk.third.tf.dto.GetDanfeDTO;
import com.sdk.third.tf.dto.GetDanfeResponseDTO;
import com.sdk.third.tf.dto.InvalidInvoiceDTO;
import com.sdk.third.tf.dto.InvalidInvoiceResponseDTO;
import com.sdk.third.tf.dto.InvoiceDetailResponseDTO;
import com.sdk.third.tf.dto.NfeInvoiceDTO;
import com.sdk.third.tf.dto.ReturnInvoiceDTO;
import com.sdk.third.tf.dto.ReturnInvoiceResponseDTO;
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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    private static final String SHOPEE_BR_DEDUCT_ERROR_MSG = "虾皮巴西店铺不支持按佣金开票，请选择产品全额或自定义比例";
    private static final String SHOPEE_BR_FREIGHT_ERROR_MSG = "虾皮巴西店铺不支持含买家运费开票";

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
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private CfgRuleInvoiceAmountService cfgRuleInvoiceAmountService;
    @Resource
    private ShopAuthService shopAuthService;
    @Resource
    private ShopeeOrderService shopeeOrderService;

    @Transactional(rollbackFor = Exception.class)
    @Deprecated
    public Boolean createInvoice(SoB2cEntity soB2cEntity) {
        String invoiceStatus = InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode();
        Object obj = null;
        String uploadStatus = InvoiceInfoUploadStatusEnum.WAIT_UPLOAD.getCode();
        //配置信息
        CfgInvoiceSettingDetailEntity invoiceSettingDetail = null;
        CfgInvoiceSettingEntity invoiceSetting = null;
        //发票规则
        String dictInvoiceRule;
        BigDecimal ratio;
        //匹配产品总价计算规则
        InvoiceInfoDTO.ProductAmountRuleResultDTO ruleResultDTO = null;
        try {
            ruleResultDTO = invoiceInfoService.productAmountRule(soB2cEntity);
        }catch (Exception e){
            log.error("产品总价值规则匹配失败！销售订单：{}", soB2cEntity.getCode(),e);
        }
        if (Objects.nonNull(ruleResultDTO) && ruleResultDTO.getIsMatch()){
            invoiceSettingDetail = ruleResultDTO.getInvoiceSettingDetail();
            invoiceSetting = ruleResultDTO.getInvoiceSetting();
            dictInvoiceRule = ruleResultDTO.getDictInvoiceRule();
            ratio = ruleResultDTO.getRatio();
        }else {
            String msg = Objects.nonNull(ruleResultDTO) && CharSequenceUtil.isNotBlank(ruleResultDTO.getMsg()) ? "产品总价值" + ruleResultDTO.getMsg() : "产品总价值规则匹配失败";
            //开票失败更新开票状态
            InvoiceInfoEntity invoiceInfoEntity = invoiceInfoService.getInvoicingBySoId(soB2cEntity.getId());
            invoiceInfoEntity.setStatus(InvoiceInfoStatusEnum.INVOICE_FAILED.getCode());
            invoiceInfoEntity.setRemark(msg);
            invoiceInfoService.updateNfeStatusById(invoiceInfoEntity);
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.INVOICE_INFO.getCode(), soB2cEntity.getId(),"开票失败");
            return Boolean.FALSE;
        }
        NfeInvoiceDTO.NfeCreateDTO createDTO = new NfeInvoiceDTO.NfeCreateDTO();
        String invoiceAddress = "";
        String sellerTaxNo = "";
        String companyName = "";
        try {
            companyName = invoiceSetting.getCompanyName();
            sellerTaxNo = invoiceSetting.getLeiCode();
            createDTO.setEmailDev("gray@ulanzi.cn");
            //地址信息
            NfeInvoiceDTO.NfeClienteDTO nfeClienteDTO = getNfeClienteDTO(soB2cEntity,invoiceSettingDetail);
            nfeClienteDTO.setEmail(CharSequenceUtil.EMPTY);
            invoiceAddress = nfeClienteDTO.getRua();
            createDTO.setCliente(nfeClienteDTO);
            log.warn("地址信息已查询完成！销售订单：{}nfeClienteDTO:{}", soB2cEntity.getCode(),JSONUtil.toJsonStr(nfeClienteDTO));
            //税务信息
            getNfeItensDTO(soB2cEntity,invoiceSettingDetail,createDTO,dictInvoiceRule, ratio);
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
            invoiceInfoEntity.setCfgId(invoiceSettingDetail.getId());
            invoiceInfoEntity.setStatus(InvoiceInfoStatusEnum.INVOICE_FAILED.getCode());
            invoiceInfoEntity.setRemark(e.getMessage());
            invoiceInfoEntity.setInvoiceAddress(invoiceAddress);
            invoiceInfoEntity.setSellerTaxNo(sellerTaxNo);
            invoiceInfoEntity.setCompanyName(companyName);
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
            invoiceInfoEntity.setCfgId(invoiceSettingDetail.getId());
            invoiceInfoEntity.setStatus(InvoiceInfoStatusEnum.INVOICE_FAILED.getCode());
            invoiceInfoEntity.setRemark(e.getMessage());
            invoiceInfoEntity.setInvoiceAddress(invoiceAddress);
            invoiceInfoEntity.setSellerTaxNo(sellerTaxNo);
            invoiceInfoEntity.setCompanyName(companyName);
            invoiceInfoService.updateNfeStatusById(invoiceInfoEntity);
            operateLogService.addModuleOperateLog(e.getMessage(), ModuleTypeEnum.INVOICE_INFO.getCode(), soB2cEntity.getId(),"开票失败");
            return Boolean.FALSE;
        }
        if (Objects.isNull(resultDTO) || Objects.isNull(resultDTO.getSuccesso())||!resultDTO.getSuccesso() || 200 !=  resultDTO.getStatus()) {
            log.error("创建发票失败,返回错误信息,返回信息:{}", JSONUtil.toJsonStr(resultDTO));
//            throw new ServiceException(ApiError.ERROR_INVOICE_NFE_CREATE_INVOICE,"未知");
            //开票失败更新开票状态
            InvoiceInfoEntity invoiceInfoEntity = invoiceInfoService.getInvoicingBySoId(soB2cEntity.getId());
            invoiceInfoEntity.setCfgId(invoiceSettingDetail.getId());
            invoiceInfoEntity.setStatus(InvoiceInfoStatusEnum.INVOICE_FAILED.getCode());
            invoiceInfoEntity.setRemark(JSONUtil.toJsonStr(resultDTO));
            invoiceInfoEntity.setInvoiceAddress(invoiceAddress);
            invoiceInfoEntity.setSellerTaxNo(sellerTaxNo);
            invoiceInfoEntity.setCompanyName(companyName);
            invoiceInfoService.updateNfeStatusById(invoiceInfoEntity);
            operateLogService.addModuleOperateLog(CharSequenceUtil.format("返回信息：{}",JSONUtil.toJsonStr(resultDTO)), ModuleTypeEnum.INVOICE_INFO.getCode(), soB2cEntity.getId(),"开票失败");
            return Boolean.FALSE;
        }
        //回写序列号和起始编号
        cfgInvoiceSettingService.updateSerialNoById(invoiceSettingDetail.getMainId(),resultDTO.getSerie(),resultDTO.getNumeroNfe());
        //更新开票状态
        InvoiceInfoEntity invoiceInfoEntity = invoiceInfoService.getInvoicingBySoId(soB2cEntity.getId());
        invoiceInfoEntity.setCfgId(invoiceSettingDetail.getId());
        invoiceInfoEntity.setStatus(invoiceStatus);
        invoiceInfoEntity.setUploadStatus(PlatformDictEnum.ALI_EXPRESS.getCode().equals(soB2cEntity.getDictPlatform()) ? InvoiceInfoUploadStatusEnum.NOT_NEED_UPLOAD.getCode() : uploadStatus);
        invoiceInfoEntity.setQueryId(resultDTO.getId());
        invoiceInfoEntity.setQueryKey(getQueryKey(resultDTO.getLink_xml()));
        invoiceInfoEntity.setPlatformInvoiceNo(resultDTO.getRecibo());
        invoiceInfoEntity.setNo(resultDTO.getSerie());
        invoiceInfoEntity.setStartCode(String.valueOf(resultDTO.getNumeroNfe()));
        invoiceInfoEntity.setInvoiceAddress(invoiceAddress);
        invoiceInfoEntity.setSellerTaxNo(sellerTaxNo);
        invoiceInfoEntity.setCompanyName(companyName);
        invoiceInfoService.updateNfeStatusById(invoiceInfoEntity);

        //上传xml、pdf
        uploadFile(invoiceInfoEntity.getId(),resultDTO.getLink_xml(),resultDTO.getLink_nota());

        //是否自动上传发票
        if (shouldAutoUploadInvoice(soB2cEntity, invoiceSettingDetail)) {
            invoiceInfoService.uploadNfeInvoice(soB2cEntity,invoiceInfoEntity.getId());
        }
        return Boolean.TRUE;
    }

    /**
     * 开具发票（新接口V2）
     * 使用新接口路径：/api/invoice/create
     * 
     * @param soB2cEntity 销售订单
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean createInvoiceV2(SoB2cEntity soB2cEntity) {
        String invoiceStatus = InvoiceInfoStatusEnum.INVOICE_SUCCESS.getCode();
        String uploadStatus = InvoiceInfoUploadStatusEnum.WAIT_UPLOAD.getCode();
        //配置信息
        CfgInvoiceSettingDetailEntity invoiceSettingDetail = null;
        CfgInvoiceSettingEntity invoiceSetting = null;
        String dictInvoiceRule;
        BigDecimal ratio;
        InvoiceInfoDTO.ProductAmountRuleResultDTO ruleResultDTO = null;
        try {
            ruleResultDTO = invoiceInfoService.productAmountRule(soB2cEntity);
        }catch (Exception e){
            log.error("产品总价值规则匹配失败！销售订单：{}", soB2cEntity.getCode(),e);
        }
        if (Objects.nonNull(ruleResultDTO) && ruleResultDTO.getIsMatch()){
            invoiceSettingDetail = ruleResultDTO.getInvoiceSettingDetail();
            invoiceSetting = ruleResultDTO.getInvoiceSetting();
            dictInvoiceRule = ruleResultDTO.getDictInvoiceRule();
            ratio = ruleResultDTO.getRatio();
        }else {
            String msg = Objects.nonNull(ruleResultDTO) && CharSequenceUtil.isNotBlank(ruleResultDTO.getMsg()) ? "产品总价值" + ruleResultDTO.getMsg() : "产品总价值规则匹配失败";
            InvoiceInfoEntity invoiceInfoEntity = invoiceInfoService.getInvoicingBySoId(soB2cEntity.getId());
            invoiceInfoEntity.setStatus(InvoiceInfoStatusEnum.INVOICE_FAILED.getCode());
            invoiceInfoEntity.setRemark(msg);
            invoiceInfoService.updateNfeStatusById(invoiceInfoEntity);
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.INVOICE_INFO.getCode(), soB2cEntity.getId(),"开票失败");
            return Boolean.FALSE;
        }
        
        String invoiceAddress = "";
        String sellerTaxNo = "";
        String companyName = "";
        CreateInvoiceResponseDTO.CreateInvoiceDataDTO responseData = null;
        
        try {
            companyName = invoiceSetting.getCompanyName();
            sellerTaxNo = invoiceSetting.getLeiCode();
            CreateInvoiceDTO createInvoiceDTO = buildCreateInvoiceDTO(soB2cEntity, invoiceSettingDetail, invoiceSetting, dictInvoiceRule, ratio);
            NfeInvoiceDTO.NfeClienteDTO nfeClienteDTO = getNfeClienteDTO(soB2cEntity, invoiceSettingDetail);
            invoiceAddress = nfeClienteDTO.getRua();
            log.warn("开具发票（新接口V2）请求参数, 销售订单：{}", soB2cEntity.getCode());
            String companyToken = invoiceSettingDetail.getToken();
            if (CharSequenceUtil.isBlank(companyToken)) {
                throw new ServiceException("公司token不能为空");
            }
            responseData = tfFiscalService.createInvoiceV2(createInvoiceDTO, companyToken);
            log.warn("开具发票（新接口V2）响应, 销售订单：{}, status:{}", soB2cEntity.getCode(), responseData.getStatus());
        }catch (Exception e){
            log.error("创建发票失败,返回信息:{}", e.getMessage(), e);
            InvoiceInfoEntity invoiceInfoEntity = invoiceInfoService.getInvoicingBySoId(soB2cEntity.getId());
            invoiceInfoEntity.setCfgId(invoiceSettingDetail.getId());
            invoiceInfoEntity.setStatus(InvoiceInfoStatusEnum.INVOICE_FAILED.getCode());
            invoiceInfoEntity.setRemark(e.getMessage());
            invoiceInfoEntity.setInvoiceAddress(invoiceAddress);
            invoiceInfoEntity.setSellerTaxNo(sellerTaxNo);
            invoiceInfoEntity.setCompanyName(companyName);
            invoiceInfoService.updateNfeStatusById(invoiceInfoEntity);
            operateLogService.addModuleOperateLog(e.getMessage(), ModuleTypeEnum.INVOICE_INFO.getCode(), soB2cEntity.getId(),"开票失败");
            return Boolean.FALSE;
        }
        
        if (responseData == null) {
            log.error("创建发票失败,响应数据为空");
            InvoiceInfoEntity invoiceInfoEntity = invoiceInfoService.getInvoicingBySoId(soB2cEntity.getId());
            invoiceInfoEntity.setCfgId(invoiceSettingDetail.getId());
            invoiceInfoEntity.setStatus(InvoiceInfoStatusEnum.INVOICE_FAILED.getCode());
            invoiceInfoEntity.setRemark("响应数据为空");
            invoiceInfoEntity.setInvoiceAddress(invoiceAddress);
            invoiceInfoEntity.setSellerTaxNo(sellerTaxNo);
            invoiceInfoEntity.setCompanyName(companyName);
            invoiceInfoService.updateNfeStatusById(invoiceInfoEntity);
            return Boolean.FALSE;
        }
        
        String status = responseData.getStatus();
        InvoiceInfoStatusEnum statusEnum = InvoiceStatusMapper.mapToEnum(status);
        
        if (InvoiceStatusMapper.isSuccess(status)) {
            // 开票成功后，查询发票详情以获取完整的发票信息（特别是XML链接）
            InvoiceDetailResponseDTO.InvoiceDetailDataDTO detailData = null;
            try {
                String companyToken = invoiceSettingDetail.getToken();
                detailData = tfFiscalService.getInvoiceDetailV2(responseData.getUuid(), companyToken);
                log.warn("查询发票详情成功, uuid: {}, xml: {}", responseData.getUuid(), detailData.getXml());
            } catch (Exception e) {
                log.error("查询发票详情失败, uuid: {}, 错误: {}", responseData.getUuid(), e.getMessage(), e);
                // 即使查询详情失败，也继续使用创建接口返回的数据
            }
            
            // 使用详情接口返回的数据（如果查询成功），否则使用创建接口返回的数据
            Integer serie = detailData != null && detailData.getSerie() != null ? detailData.getSerie() : responseData.getSerie();
            Integer number = detailData != null && detailData.getNumber() != null ? detailData.getNumber() : responseData.getNfe();
            String xmlUrl = detailData != null && CharSequenceUtil.isNotBlank(detailData.getXml()) ? detailData.getXml() : responseData.getXml();
            String chave = detailData != null && CharSequenceUtil.isNotBlank(detailData.getChave()) ? detailData.getChave() : responseData.getChave();
            
            if (serie != null && number != null) {
                cfgInvoiceSettingService.updateSerialNoById(invoiceSettingDetail.getMainId(), serie, number);
            }
            
            InvoiceInfoEntity invoiceInfoEntity = invoiceInfoService.getInvoicingBySoId(soB2cEntity.getId());
            invoiceInfoEntity.setCfgId(invoiceSettingDetail.getId());
            invoiceInfoEntity.setStatus(invoiceStatus);
            invoiceInfoEntity.setUploadStatus(PlatformDictEnum.ALI_EXPRESS.getCode().equals(soB2cEntity.getDictPlatform()) ? InvoiceInfoUploadStatusEnum.NOT_NEED_UPLOAD.getCode() : uploadStatus);
            invoiceInfoEntity.setQueryId(responseData.getUuid());
            // queryKey直接使用chave，不需要解析XML
            invoiceInfoEntity.setQueryKey(chave);
            invoiceInfoEntity.setPlatformInvoiceNo(chave);
            invoiceInfoEntity.setNo(serie);
            invoiceInfoEntity.setStartCode(String.valueOf(number));
            invoiceInfoEntity.setInvoiceAddress(invoiceAddress);
            invoiceInfoEntity.setSellerTaxNo(sellerTaxNo);
            invoiceInfoEntity.setCompanyName(companyName);
            invoiceInfoService.updateNfeStatusById(invoiceInfoEntity);
            
            // 上传xml、pdf（使用详情接口返回的XML链接）
            if (CharSequenceUtil.isNotBlank(xmlUrl)) {
                uploadFile(invoiceInfoEntity.getId(), xmlUrl, "");
                // 获取Danfe PDF并上传
                generateAndUploadPdfFromDanfe(invoiceInfoEntity.getId(), responseData.getUuid(), invoiceSettingDetail.getToken());
            }
            
            if (shouldAutoUploadInvoice(soB2cEntity, invoiceSettingDetail)) {
                invoiceInfoService.uploadNfeInvoice(soB2cEntity,invoiceInfoEntity.getId());
            }
            return Boolean.TRUE;
        } else if (InvoiceStatusMapper.isProcessing(status)) {
            InvoiceInfoEntity invoiceInfoEntity = invoiceInfoService.getInvoicingBySoId(soB2cEntity.getId());
            invoiceInfoEntity.setCfgId(invoiceSettingDetail.getId());
            invoiceInfoEntity.setStatus(statusEnum.getCode());
            invoiceInfoEntity.setQueryId(responseData.getUuid());
            invoiceInfoEntity.setRemark(CharSequenceUtil.isNotBlank(responseData.getMotivo()) ? responseData.getMotivo() : "处理中");
            invoiceInfoEntity.setInvoiceAddress(invoiceAddress);
            invoiceInfoEntity.setSellerTaxNo(sellerTaxNo);
            invoiceInfoEntity.setCompanyName(companyName);
            invoiceInfoService.updateNfeStatusById(invoiceInfoEntity);
            return Boolean.TRUE;
        } else {
            // 失败状态（包括 InvoicingFailed, Failed, Canceled, Voided 等）
            String motivo = CharSequenceUtil.isNotBlank(responseData.getMotivo()) ? responseData.getMotivo() : statusEnum.getName();
            boolean isErrorStatus = InvoiceStatusMapper.isFailed(status);
            
            if (isErrorStatus) {
                if (InvoiceStatusMapper.isCanceled(status)) {
                    log.warn("发票已取消, uuid: {}, motivo: {}", responseData.getUuid(), motivo);
                } else if (InvoiceStatusMapper.isVoided(status)) {
                    log.warn("发票已作废, uuid: {}, motivo: {}", responseData.getUuid(), motivo);
                } else {
                    log.error("创建发票失败, status:{}, motivo:{}", status, motivo);
                }
            } else {
                log.error("创建发票返回未知状态, status:{}, motivo:{}", status, motivo);
            }
            
            InvoiceInfoEntity invoiceInfoEntity = invoiceInfoService.getInvoicingBySoId(soB2cEntity.getId());
            invoiceInfoEntity.setCfgId(invoiceSettingDetail.getId());
            invoiceInfoEntity.setStatus(statusEnum.getCode());
            invoiceInfoEntity.setQueryId(responseData.getUuid());
            invoiceInfoEntity.setRemark(CharSequenceUtil.format("status:{}, motivo:{}", status, motivo));
            invoiceInfoEntity.setInvoiceAddress(invoiceAddress);
            invoiceInfoEntity.setSellerTaxNo(sellerTaxNo);
            invoiceInfoEntity.setCompanyName(companyName);
            invoiceInfoService.updateNfeStatusById(invoiceInfoEntity);
            
            if (isErrorStatus) {
                operateLogService.addModuleOperateLog(
                    CharSequenceUtil.format("开票失败, status:{}, motivo:{}", status, motivo),
                    ModuleTypeEnum.INVOICE_INFO.getCode(),
                    soB2cEntity.getId(),
                    "开票失败"
                );
            }
            return Boolean.FALSE;
        }
    }

    /**
     * 构建开具发票DTO（新接口）
     */
    private CreateInvoiceDTO buildCreateInvoiceDTO(SoB2cEntity soB2cEntity, CfgInvoiceSettingDetailEntity invoiceSettingDetail, CfgInvoiceSettingEntity invoiceSetting, String dictInvoiceRule, BigDecimal ratio) {
        CreateInvoiceDTO dto = new CreateInvoiceDTO();
        // 使用销售单号code生成15位唯一值，避免ID截取导致的重复问题
        String invoiceId = generateUniqueInvoiceId(soB2cEntity.getCode());
        dto.setId(invoiceId);
        List<InvoiceInfoEntity> existList = invoiceInfoService.listBySoIds(Collections.singletonList(soB2cEntity.getId()));
        boolean isReopen = existList.stream().anyMatch(e -> InvoiceInfoStatusEnum.INVOICE_FAILED.getCode().equals(e.getStatus()) && InvoiceInfoInvoiceTypeEnum.NFE.getCode().equals(e.getInvoiceType()));
        dto.setIsReopen(isReopen);
        dto.setNatureOfOperation("Operação de comercialização");
        dto.setTransactionType("1");
        dto.setModel("55");
        dto.setIssuanceType("1");
        dto.setAmbiente(getAmbiente());
        // total_discount_amount：根据API规范不传，故不设置
        dto.setCliente(buildClienteDTO(soB2cEntity, invoiceSettingDetail));
        dto.setProducts(buildProductDTOList(soB2cEntity, invoiceSettingDetail, invoiceSetting, dictInvoiceRule, ratio));
        dto.setTransportation(buildTransportationDTO(soB2cEntity, invoiceSettingDetail));
        return dto;
    }
    
    private CreateInvoiceDTO.ClienteDTO buildClienteDTO(SoB2cEntity soB2cEntity, CfgInvoiceSettingDetailEntity invoiceSettingDetail) {
        NfeInvoiceDTO.NfeClienteDTO nfeClienteDTO = getNfeClienteDTO(soB2cEntity, invoiceSettingDetail);
        CreateInvoiceDTO.ClienteDTO clienteDTO = new CreateInvoiceDTO.ClienteDTO();
        clienteDTO.setName(nfeClienteDTO.getName());
        String cpfCnpj = nfeClienteDTO.getCpfCnpj();
        if (CharSequenceUtil.isNotBlank(cpfCnpj)) {
            if (cpfCnpj.length() <= 11) clienteDTO.setCpf(cpfCnpj);
            else clienteDTO.setCnpj(cpfCnpj);
        }
        clienteDTO.setIe(nfeClienteDTO.getIeRg());
        clienteDTO.setEndereco(nfeClienteDTO.getRua());
        clienteDTO.setNumero(CharSequenceUtil.isNotBlank(nfeClienteDTO.getNumero()) ? nfeClienteDTO.getNumero() : "S/N");
        clienteDTO.setBairro(nfeClienteDTO.getBairro());
        
        // 获取城市信息：优先从账单地址接口获取，否则从订单接口获取
        clienteDTO.setCity(nfeClienteDTO.getCityId());
        
        clienteDTO.setUf(nfeClienteDTO.getUf());
        clienteDTO.setCep(nfeClienteDTO.getCep());
        clienteDTO.setTelefone(nfeClienteDTO.getMobile());
        clienteDTO.setEmail(CharSequenceUtil.EMPTY);
        return clienteDTO;
    }
    
    /**
     * 从数据源获取城市信息
     * 优先从账单地址接口（DmpSoBillDetailEntity）获取，否则从订单接口（SoB2cReceiverEntity）获取
     */
    private String getCityFromSource(SoB2cEntity soB2cEntity) {
        // 查询账单地址接口数据
        List<DmpSoBillDetailEntity> allDmpSoBillDetailEntityList = FeignQuery.create(DmpSoBillDetailEntity.class)
                .eq(DmpSoBillDetailEntity::getPlatformCode, soB2cEntity.getPlatformCode())
                .eq(DmpSoBillDetailEntity::getShopId, soB2cEntity.getShopId())
                .eq(DmpSoBillDetailEntity::getSourcePlatform, soB2cEntity.getDictPlatform())
                .list();
        
        // 优先使用账单地址接口的城市信息（美客多：city_name）
        if (CollUtil.isNotEmpty(allDmpSoBillDetailEntityList)) {
            String city = allDmpSoBillDetailEntityList.get(0).getCity();
            if (CharSequenceUtil.isNotBlank(city)) {
                return city;
            }
        }
        
        // 如果没有账单地址数据，从订单接口获取（速卖通：city）
        SoB2cReceiverEntity receiverEntity = soB2cReceiverService.getByMainId(soB2cEntity.getId());
        if (ObjUtil.isNotEmpty(receiverEntity) && CharSequenceUtil.isNotBlank(receiverEntity.getCityName())) {
            return receiverEntity.getCityName();
        }
        
        return null;
    }
    
    private List<CreateInvoiceDTO.ProductDTO> buildProductDTOList(SoB2cEntity soB2cEntity, CfgInvoiceSettingDetailEntity invoiceSettingDetail, CfgInvoiceSettingEntity invoiceSetting, String dictInvoiceRule, BigDecimal ratio) {
        List<CreateInvoiceDTO.ProductDTO> products = new ArrayList<>();
        List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(soB2cEntity.getId());
        if (CollUtil.isEmpty(detailList)) throw new ServiceException(ApiError.SO_B2C_DETAIL_NOT_FOUND);
        Map<String, OrderItemDetail> shopeeOrderItemMap = getShopeeBrazilOrderItemMap(soB2cEntity);
        List<String> platformSkuNoList = detailList.stream().map(SoB2cDetailEntity::getPlatformSkuNo).distinct().collect(Collectors.toList());
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setShopIdList(Collections.singletonList(soB2cEntity.getShopId()));
        paramDTO.setPlatformList(Collections.singletonList(soB2cEntity.getDictPlatform()));
        paramDTO.setType(RuleTypeEnum.B2C_PLATFORM.getCode());
        paramDTO.setPlatformSkuNoList(platformSkuNoList);
        List<ListingInfoWithSkuMappingDTO> listingInfoEntityList = skuMappingService.findListDto(paramDTO);
        Map<String, List<ListingInfoWithSkuMappingDTO>> listingMap = listingInfoEntityList.stream().distinct().collect(Collectors.groupingBy(obj -> CharSequenceUtil.format("{}-{}-{}", obj.getPlatform(), obj.getPlatformSkuNo(), obj.getShopId())));
        List<String> listingIdList = listingInfoEntityList.stream().map(ListingInfoWithSkuMappingDTO::getListingId).distinct().collect(Collectors.toList());
        List<InvoiceTaxEntity> invoiceTaxList = invoiceTaxService.listByListingIdList(listingIdList);
        Map<String, InvoiceTaxEntity> taxMap = invoiceTaxList.stream().collect(Collectors.toMap(InvoiceTaxEntity::getListingId, Function.identity()));
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(soB2cEntity.getShopId());
        if (ObjUtil.isEmpty(shopInfoEntity)) throw new ServiceException(ApiError.SHOP_NOT_FOUND);
        String taxCategoryId = invoiceSetting.getTaxCategoryId();
        for (SoB2cDetailEntity detailEntity : detailList) {
            CreateInvoiceDTO.ProductDTO productDTO = new CreateInvoiceDTO.ProductDTO();
            List<ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingList = listingMap.get(CharSequenceUtil.format("{}-{}-{}", soB2cEntity.getDictPlatform(), detailEntity.getPlatformSkuNo(), soB2cEntity.getShopId()));
            InvoiceTaxEntity invoiceTaxEntity = CollUtil.isEmpty(listingInfoWithSkuMappingList) ? null : listingInfoWithSkuMappingList.stream().filter(obj -> ObjUtil.isNotEmpty(taxMap.get(obj.getListingId()))).map(obj -> taxMap.get(obj.getListingId())).findFirst().orElse(null);
            if (Objects.isNull(invoiceTaxEntity)) throw new ServiceException(ApiError.FIN_SKU_INVOICE_TAX_INFO_NOT_FOUND, detailEntity.getPlatformSkuNo(), shopInfoEntity.getName());
            productDTO.setName(invoiceTaxEntity.getInvoiceProductName());
            productDTO.setCodigo(detailEntity.getPlatformSkuNo());
            productDTO.setNcm(invoiceTaxEntity.getInvoiceHsCode());
            productDTO.setCount(String.valueOf(detailEntity.getQty()));
            productDTO.setUnit("UN");
            BigDecimal unitPrice = getUnitPrice(soB2cEntity, detailEntity, dictInvoiceRule, ratio, shopeeOrderItemMap);
            productDTO.setUnitPrice(unitPrice);
            productDTO.setTotalPrice(MathUtil.multiplyWithTwo(unitPrice, detailEntity.getQty()));
            // discount_price字段：根据API规范，当值为0或null时不传，所以不设置该字段
            // productDTO.setDiscountPrice(BigDecimal.ZERO); // 已移除：不传discount_price字段
            if (CharSequenceUtil.isNotBlank(taxCategoryId)) productDTO.setCategoryId(taxCategoryId);
            productDTO.setOrigem("0");
            // indicador_total: 若SKU为赠品传"0"，若SKU不为赠品传"1"，默认是"1"
            Boolean isGift = detailEntity.getIsGift();
            productDTO.setIndicadorTotal(Boolean.TRUE.equals(isGift) ? "0" : "1");
            products.add(productDTO);
        }
        return products;
    }
    
    private CreateInvoiceDTO.TransportationDTO buildTransportationDTO(SoB2cEntity soB2cEntity, CfgInvoiceSettingDetailEntity invoiceSettingDetail) {
        CreateInvoiceDTO.TransportationDTO transportationDTO = new CreateInvoiceDTO.TransportationDTO();
        transportationDTO.setTransportMode("0");
        transportationDTO.setFreightAmount(getShipCost(soB2cEntity, invoiceSettingDetail));
        return transportationDTO;
    }
    
    private String getQueryKeyFromXml(String xmlContent) {
        if (CharSequenceUtil.isEmpty(xmlContent)) return "";
        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new java.io.StringReader(xmlContent));
            Element root = document.getRootElement();
            Element nfeElement = root.element("NFe");
            if (nfeElement != null) {
                Element infNFeElement = nfeElement.element("infNFe");
                if (infNFeElement != null) {
                    String queryKey = infNFeElement.attributeValue("Id");
                    if (CharSequenceUtil.isNotBlank(queryKey)) return queryKey.replace("NFe", "");
                }
            }
            return "";
        } catch (Exception e) {
            log.error("获取queryKey失败,xml内容异常：{}", e.getMessage());
            return "";
        }
    }
    
    private void uploadXmlContent(String invoiceId, String xmlContent) {
        if (CharSequenceUtil.isBlank(xmlContent)) return;
        try {
            byte[] xmlBytes = xmlContent.getBytes("UTF-8");
            MultipartFile xmlFile = new org.springframework.mock.web.MockMultipartFile("file", "invoice.xml", "application/xml", new java.io.ByteArrayInputStream(xmlBytes));
            String xmlUrl = filefeign.uploadFile(xmlFile);
            List<OmsAttachmentDTO.UpdateDTO> addOrUpdateList = new ArrayList<>();
            addOrUpdateList.add(new OmsAttachmentDTO.UpdateDTO(AttachmentTypeEnum.INVOICE_INFO_XML.getCode(), xmlUrl, "invoice.xml", invoiceId));
            omsAttachmentService.batchAddOrUpdate(addOrUpdateList);
        } catch (Exception e) {
            log.error("发票XML上传失败", e);
        }
    }
    
    /**
     * 根据Danfe接口获取PDF并上传
     * 注意：不再通过XML填充PDF模板生成PDF，而是直接通过getDanfe接口获取PDF URL
     * 
     * @param invoiceId 发票ID
     * @param uuid 发票UUID
     * @param companyToken 公司token（用于调用getDanfe接口）
     */
    private void generateAndUploadPdfFromDanfe(String invoiceId, String uuid, String companyToken) {
        if (CharSequenceUtil.isBlank(uuid) || CharSequenceUtil.isBlank(companyToken)) {
            log.warn("获取Danfe PDF参数不完整, invoiceId:{}, uuid:{}, companyToken:{}", invoiceId, uuid, companyToken);
            return;
        }
        try {
            // 调用getDanfe接口获取PDF URL
            GetDanfeDTO getDanfeDTO = new GetDanfeDTO();
            getDanfeDTO.setUuid(uuid);
            
            GetDanfeResponseDTO.GetDanfeDataDTO danfeData = tfFiscalService.getDanfeV2(getDanfeDTO, companyToken);
            
            // 优先使用danfe，如果没有则使用danfe_simples
            String pdfUrl = CharSequenceUtil.isNotBlank(danfeData.getDanfe()) 
                ? danfeData.getDanfe() 
                : danfeData.getDanfeSimples();
            
            if (CharSequenceUtil.isBlank(pdfUrl)) {
                log.warn("获取Danfe PDF URL为空, invoiceId:{}, uuid:{}", invoiceId, uuid);
                return;
            }
            
            // 上传PDF文件
            uploadFile(invoiceId, "", pdfUrl);
            log.info("获取Danfe PDF并上传成功, invoiceId:{}, uuid:{}, pdfUrl:{}", invoiceId, uuid, pdfUrl);
        } catch (Exception e) {
            log.error("获取Danfe PDF并上传失败, invoiceId:{}, uuid:{}", invoiceId, uuid, e);
            // 失败时更新备注，但不抛出异常，避免影响主流程
            try {
                InvoiceInfoEntity invoiceInfoEntity = invoiceInfoService.getById(invoiceId);
                if (invoiceInfoEntity != null) {
                    String remark = CharSequenceUtil.isNotBlank(invoiceInfoEntity.getRemark()) 
                        ? invoiceInfoEntity.getRemark() + "；获取Danfe PDF失败: " + e.getMessage()
                        : "获取Danfe PDF失败: " + e.getMessage();
                    invoiceInfoEntity.setRemark(remark);
                    invoiceInfoService.updateNfeStatusById(invoiceInfoEntity);
                }
            } catch (Exception ex) {
                log.error("更新发票备注失败, invoiceId:{}", invoiceId, ex);
            }
        }
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
            return queryKey.replace("NFe", "");
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
        if (isShopeeBrazilOrder(soB2cEntity)) {
            uploadShopeeBrazilInvoice(soB2cEntity);
            return;
        }
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
        if (isShopeeBrazilOrder(soB2cEntity)) {
            if (ObjUtil.isNotEmpty(invoiceSettingDetail) && Boolean.TRUE.equals(invoiceSettingDetail.getIsContainShipFee())) {
                throw new ServiceException(SHOPEE_BR_FREIGHT_ERROR_MSG);
            }
            return BigDecimal.ZERO;
        }
        if (!CharSequenceUtil.equals(PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode(),soB2cEntity.getDictPlatform())) {
            return BigDecimal.ZERO;
        }
        if (ObjUtil.isEmpty(invoiceSettingDetail) || !Boolean.TRUE.equals(invoiceSettingDetail.getIsContainShipFee())){
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
     *
     * @param soB2cEntity
     * @param invoiceSettingDetail
     * @return NfeClienteDTO
     * @author will
     * @date 2025/4/11 15:22
     */
    private NfeInvoiceDTO.NfeClienteDTO getNfeClienteDTO(SoB2cEntity soB2cEntity, CfgInvoiceSettingDetailEntity invoiceSettingDetail) {
        String dictVerifyType = invoiceSettingDetail.getDictVerifyType();
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
        NfeInvoiceDTO.NfeClienteDTO nfeClienteDTO;
        if (CollUtil.isEmpty(allDmpSoBillDetailEntityList)) {
            if (PlatformDictEnum.ALI_EXPRESS.getCode().equals(soB2cEntity.getDictPlatform())
                    || (PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equals(soB2cEntity.getDictPlatform()))){
                throw new ServiceException("开票地址信息不能为空");
            }
           //按照销售信息赋值
            nfeClienteDTO = getNfeClienteDTOBySoB2c(soB2cEntity, receiverEntity,dictVerifyType);
        } else {
            DmpSoBillDetailEntity dmpSoBillDetailEntity = allDmpSoBillDetailEntityList.get(0);
            if (CharSequenceUtil.isNotBlank(receiverEntity.getIeNo())){
                dmpSoBillDetailEntity.setRegistrationNo(receiverEntity.getIeNo());
            }
            if (InvoiceVerifyTypeEnum.IE.getCode().equals(dictVerifyType) && CharSequenceUtil.isBlank(dmpSoBillDetailEntity.getRegistrationNo())){
                throw new ServiceException("公司买家IE号不允许为空");
            }
            nfeClienteDTO = NfeInvoiceConverter.INSTANCE.soBillDetailEntityToNfeCliente(dmpSoBillDetailEntity);
            String newCep = CharSequenceUtil.isBlank(nfeClienteDTO.getCep()) ? "" : removeSignAndSpace(nfeClienteDTO.getCep());
            nfeClienteDTO.setCep(newCep);
            // 巴西开票要求买家税号(CPF/CNPJ)只保留数字，去除".-/"等特殊符号；订单数据不变
            nfeClienteDTO.setCpfCnpj(sanitizeCpfCnpj(nfeClienteDTO.getCpfCnpj()));
            nfeClienteDTO.setBairro(getBairroStr(soB2cEntity.getDictPlatform(), nfeClienteDTO.getRua(),nfeClienteDTO.getBairro()));
            if (CharSequenceUtil.isNotBlank(receiverEntity.getInvoiceAddress())){
                nfeClienteDTO.setRua(receiverEntity.getInvoiceAddress());
            }else {
                nfeClienteDTO.setRua(getRuaStr(soB2cEntity.getDictPlatform(), nfeClienteDTO.getRua()));
            }
        }
        fillReceiverFallbackClientInfo(nfeClienteDTO, receiverEntity);
        nfeClienteDTO = enrichShopeeBrazilClientDTO(soB2cEntity, nfeClienteDTO);
        fillProvinceInfo(nfeClienteDTO);
        nfeClienteDTO.setEmail(CharSequenceUtil.EMPTY);
        return nfeClienteDTO;
    }

    /**
     * 清洗买家税号(CPF/CNPJ)：去除所有非数字字符，仅保留纯数字
     * 巴西开票接口要求 cpf/cnpj 为纯数字，CPF=11位、CNPJ=14位；
     * 实现方式与 CfgInvoiceSettingServiceImpl#formatCnpjToDatabase 保持一致
     */
    private String sanitizeCpfCnpj(String cpfCnpj) {
        if (CharSequenceUtil.isBlank(cpfCnpj)) {
            return cpfCnpj;
        }
        return cpfCnpj.replaceAll("[^0-9]", "");
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

    private NfeInvoiceDTO.NfeClienteDTO getNfeClienteDTOBySoB2c(SoB2cEntity soB2cEntity, SoB2cReceiverEntity receiverEntity, String dictVerifyType) {
        if (InvoiceVerifyTypeEnum.IE.getCode().equals(dictVerifyType) && CharSequenceUtil.isBlank(receiverEntity.getIeNo())){
            throw new ServiceException("公司买家IE号不允许为空");
        }
        NfeInvoiceDTO.NfeClienteDTO nfeClienteDTO = NfeInvoiceConverter.INSTANCE.soB2cReceiverEntityToNfeCliente(receiverEntity);
        // 巴西开票要求买家税号(CPF/CNPJ)只保留数字，去除".-/"等特殊符号；订单数据不变
        nfeClienteDTO.setCpfCnpj(sanitizeCpfCnpj(nfeClienteDTO.getCpfCnpj()));
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getBairro())){
            nfeClienteDTO.setBairro(receiverEntity.getFirstAddress() );
        }
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getMobile())){
            nfeClienteDTO.setMobile(receiverEntity.getReceiverTelNumber());
        }
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getRua())){
            nfeClienteDTO.setRua(receiverEntity.getSecondAddress() + receiverEntity.getFullAddress());
        }
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getRua()) && CharSequenceUtil.isNotBlank(receiverEntity.getInvoiceAddress())) {
            nfeClienteDTO.setRua(receiverEntity.getInvoiceAddress());
        }
        if (isShopeeBrazilOrder(soB2cEntity) && CharSequenceUtil.isBlank(nfeClienteDTO.getBairro())) {
            nfeClienteDTO.setBairro(receiverEntity.getDistrictName());
        }
        nfeClienteDTO.setEmail(CharSequenceUtil.EMPTY);
        return nfeClienteDTO;
    }


    /**
     * 税务信息
     *
     * @param soB2cEntity
     * @param dictInvoiceRule
     * @param ratio
     * @return List<NfeItensDTO>
     * @author will
     * @date 2025/4/11 16:21
     */
    private void getNfeItensDTO(SoB2cEntity soB2cEntity, CfgInvoiceSettingDetailEntity invoiceSettingDetail, NfeInvoiceDTO.NfeCreateDTO createDTO, String dictInvoiceRule, BigDecimal ratio) {
        List<NfeInvoiceDTO.NfeItensDTO> itens = new ArrayList<>();
        Map<String, OrderItemDetail> shopeeOrderItemMap = getShopeeBrazilOrderItemMap(soB2cEntity);

        //财务信息
        SoB2cFinanceEntity financeEntity = soB2cFinanceService.getByMainId(soB2cEntity.getId());
        if (ObjUtil.isEmpty(financeEntity)) {
            throw new ServiceException(ApiError.SO_B2C_FINANCE_NOT_FOUND);
        }

        List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(soB2cEntity.getId());
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.SO_B2C_DETAIL_NOT_FOUND);
        }
        //平台sku
        List<String> platformSkuNoList = detailList.stream().map(SoB2cDetailEntity::getPlatformSkuNo).distinct().collect(Collectors.toList());

        // 查询该店铺所有平台sku
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setShopIdList(Collections.singletonList(soB2cEntity.getShopId()));
        paramDTO.setPlatformList(Collections.singletonList(soB2cEntity.getDictPlatform()));
        paramDTO.setType(RuleTypeEnum.B2C_PLATFORM.getCode());
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
            throw new ServiceException(ApiError.SHOP_NOT_FOUND);
        }

        BigDecimal valorTotal = BigDecimal.ZERO;
        for (SoB2cDetailEntity detailEntity :detailList) {
            NfeInvoiceDTO.NfeItensDTO nfeItensDTO = new NfeInvoiceDTO.NfeItensDTO();
            //listing信息
            List<ListingInfoWithSkuMappingDTO> listingInfoWithSkuMappingList = listingMap.get(CharSequenceUtil.format("{}-{}-{}", soB2cEntity.getDictPlatform(), detailEntity.getPlatformSkuNo(),soB2cEntity.getShopId()));
            //税务信息
            InvoiceTaxEntity invoiceTaxEntity = CollUtil.isEmpty(listingInfoWithSkuMappingList) ? null : listingInfoWithSkuMappingList.stream().filter(obj -> ObjUtil.isNotEmpty(taxMap.get(obj.getListingId()))).map(obj -> taxMap.get(obj.getListingId())).findFirst().orElse(null);
            if (Objects.isNull(invoiceTaxEntity)) {
                throw new ServiceException(ApiError.FIN_SKU_INVOICE_TAX_INFO_NOT_FOUND,detailEntity.getPlatformSkuNo(),shopInfoEntity.getName());
            }
            nfeItensDTO.setName(invoiceTaxEntity.getInvoiceProductName());
            nfeItensDTO.setSku(detailEntity.getPlatformSkuNo());
            nfeItensDTO.setNcm(invoiceTaxEntity.getInvoiceHsCode());
            nfeItensDTO.setCfopExterno(invoiceTaxEntity.getDiffStateTaxCode());
            nfeItensDTO.setCfopInterno(invoiceTaxEntity.getSameStateTaxCode());
            nfeItensDTO.setQuantity(detailEntity.getQty());
            nfeItensDTO.setCoPedClienteApi(soB2cEntity.getCode());

            //产品金额
            nfeItensDTO.setUnitPrice(getUnitPrice(soB2cEntity,detailEntity,dictInvoiceRule,ratio, shopeeOrderItemMap));
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
     * @param dictInvoiceRule
     * @param ratio
     * @return BigDecimal
     * @author will
     * @date 2025/4/14 14:42
     */
    private BigDecimal getUnitPrice (SoB2cEntity soB2cEntity, SoB2cDetailEntity detailEntity, String dictInvoiceRule, BigDecimal ratio) {
        return getUnitPrice(soB2cEntity, detailEntity, dictInvoiceRule, ratio, Collections.emptyMap());
    }

    private BigDecimal getUnitPrice (SoB2cEntity soB2cEntity, SoB2cDetailEntity detailEntity, String dictInvoiceRule, BigDecimal ratio, Map<String, OrderItemDetail> shopeeOrderItemMap) {
        if (isShopeeBrazilOrder(soB2cEntity)) {
            return getShopeeBrazilUnitPrice(soB2cEntity, detailEntity, dictInvoiceRule, ratio, shopeeOrderItemMap);
        }
        BigDecimal price = detailEntity.getPrice();
        if (CharSequenceUtil.isEmpty(dictInvoiceRule)) {
            price = detailEntity.getPrice();
        }
        if (CharSequenceUtil.equals(dictInvoiceRule, InvoiceRuleEnum.AMOUNT.getCode())) {
            price = detailEntity.getPrice();
        }
        if (CharSequenceUtil.equals(dictInvoiceRule, InvoiceRuleEnum.CUSTOM.getCode())) {
            price = MathUtil.divide(MathUtil.multiplyWithTwo(detailEntity.getPrice(),ratio), MathUtil.BigDecimal_100);
        }
        if (CharSequenceUtil.equals(dictInvoiceRule, InvoiceRuleEnum.DEDUCT.getCode())) {
            price = MathUtil.subtract(detailEntity.getPrice(),detailEntity.getSaleFee()) ;
        }
        return convertToBrl(soB2cEntity, price, detailEntity.getCurrency(), detailEntity.getExchangeRate());
    }

    private boolean shouldAutoUploadInvoice(SoB2cEntity soB2cEntity, CfgInvoiceSettingDetailEntity invoiceSettingDetail) {
        return ObjUtil.isNotEmpty(invoiceSettingDetail)
                && Boolean.TRUE.equals(invoiceSettingDetail.getIsAutoUpload())
                && (CharSequenceUtil.equals(PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode(), soB2cEntity.getDictPlatform())
                || isShopeeBrazilOrder(soB2cEntity));
    }

    private boolean isShopeeBrazilOrder(SoB2cEntity soB2cEntity) {
        if (ObjUtil.isEmpty(soB2cEntity) || CharSequenceUtil.isBlank(soB2cEntity.getShopId())) {
            return false;
        }
        ShopInfoEntity shopInfoEntity = shopInfoService.getById(soB2cEntity.getShopId());
        return isShopeeBrazilShop(shopInfoEntity);
    }

    private boolean isShopeeBrazilShop(ShopInfoEntity shopInfoEntity) {
        return ObjUtil.isNotEmpty(shopInfoEntity)
                && CharSequenceUtil.equals(shopInfoEntity.getDictPlatform(), PlatformDictEnum.SHOPEE.getCode())
                && CharSequenceUtil.equalsIgnoreCase(shopInfoEntity.getDictCountryCode(), "BR");
    }

    private void fillReceiverFallbackClientInfo(NfeInvoiceDTO.NfeClienteDTO nfeClienteDTO, SoB2cReceiverEntity receiverEntity) {
        if (ObjUtil.isEmpty(nfeClienteDTO) || ObjUtil.isEmpty(receiverEntity)) {
            return;
        }
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getName())) {
            nfeClienteDTO.setName(CharSequenceUtil.isNotBlank(receiverEntity.getReceiverName()) ? receiverEntity.getReceiverName() : receiverEntity.getName());
        }
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getCpfCnpj()) && CharSequenceUtil.isNotBlank(receiverEntity.getReceiverTaxNo())) {
            nfeClienteDTO.setCpfCnpj(removeSignAndSpace(receiverEntity.getReceiverTaxNo()));
        }
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getIeRg()) && CharSequenceUtil.isNotBlank(receiverEntity.getIeNo())) {
            nfeClienteDTO.setIeRg(receiverEntity.getIeNo());
        }
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getMobile())) {
            nfeClienteDTO.setMobile(CharSequenceUtil.isNotBlank(receiverEntity.getReceiverTelNumber()) ? receiverEntity.getReceiverTelNumber() : receiverEntity.getTelNumber());
        }
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getCityId())) {
            nfeClienteDTO.setCityId(receiverEntity.getCityName());
        }
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getState())) {
            nfeClienteDTO.setState(receiverEntity.getProvinceName());
        }
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getBairro())) {
            nfeClienteDTO.setBairro(CharSequenceUtil.isNotBlank(receiverEntity.getDistrictName()) ? receiverEntity.getDistrictName() : receiverEntity.getFirstAddress());
        }
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getRua())) {
            if (CharSequenceUtil.isNotBlank(receiverEntity.getInvoiceAddress())) {
                nfeClienteDTO.setRua(receiverEntity.getInvoiceAddress());
            } else {
                nfeClienteDTO.setRua(CharSequenceUtil.nullToEmpty(receiverEntity.getSecondAddress()) + CharSequenceUtil.nullToEmpty(receiverEntity.getFullAddress()));
            }
        }
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getNumero()) || CharSequenceUtil.equalsIgnoreCase(nfeClienteDTO.getNumero(), "S/N")) {
            nfeClienteDTO.setNumero(CharSequenceUtil.isNotBlank(receiverEntity.getHouseNumber()) ? receiverEntity.getHouseNumber() : "S/N");
        }
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getCep())) {
            nfeClienteDTO.setCep(sanitizeCep(receiverEntity.getPostCode()));
        } else {
            nfeClienteDTO.setCep(sanitizeCep(nfeClienteDTO.getCep()));
        }
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getCountry())) {
            nfeClienteDTO.setCountry(receiverEntity.getCountry());
        }
    }

    private NfeInvoiceDTO.NfeClienteDTO enrichShopeeBrazilClientDTO(SoB2cEntity soB2cEntity, NfeInvoiceDTO.NfeClienteDTO nfeClienteDTO) {
        if (!isShopeeBrazilOrder(soB2cEntity)) {
            return nfeClienteDTO;
        }
        OrderDetail orderDetail = getShopeeBrazilOrderDetail(soB2cEntity);
        RecipientAddress recipientAddress = orderDetail.getRecipientAddress();
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getName())) {
            nfeClienteDTO.setName(CharSequenceUtil.isNotBlank(orderDetail.getBuyerUsername()) ? orderDetail.getBuyerUsername() : ObjUtil.isEmpty(recipientAddress) ? CharSequenceUtil.EMPTY : recipientAddress.getName());
        }
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getCpfCnpj()) && CharSequenceUtil.isNotBlank(orderDetail.getBuyerCpfId())) {
            nfeClienteDTO.setCpfCnpj(removeSignAndSpace(orderDetail.getBuyerCpfId()));
        }
        if (ObjUtil.isNotEmpty(recipientAddress)) {
            if (CharSequenceUtil.isBlank(nfeClienteDTO.getRua())) {
                nfeClienteDTO.setRua(recipientAddress.getFullAddress());
            }
            if (CharSequenceUtil.isBlank(nfeClienteDTO.getBairro())) {
                nfeClienteDTO.setBairro(recipientAddress.getDistrict());
            }
            if (CharSequenceUtil.isBlank(nfeClienteDTO.getCityId())) {
                nfeClienteDTO.setCityId(recipientAddress.getCity());
            }
            if (CharSequenceUtil.isBlank(nfeClienteDTO.getState())) {
                nfeClienteDTO.setState(recipientAddress.getState());
            }
            if (CharSequenceUtil.isBlank(nfeClienteDTO.getMobile())) {
                nfeClienteDTO.setMobile(recipientAddress.getPhone());
            }
            if (CharSequenceUtil.isBlank(nfeClienteDTO.getCep())) {
                nfeClienteDTO.setCep(sanitizeCep(recipientAddress.getZipcode()));
            }
            if (CharSequenceUtil.isBlank(nfeClienteDTO.getCountry())) {
                nfeClienteDTO.setCountry(recipientAddress.getRegion());
            }
        }
        if (CharSequenceUtil.isBlank(nfeClienteDTO.getNumero())) {
            nfeClienteDTO.setNumero("S/N");
        }
        return nfeClienteDTO;
    }

    private void fillProvinceInfo(NfeInvoiceDTO.NfeClienteDTO nfeClienteDTO) {
        if (ObjUtil.isEmpty(nfeClienteDTO) || CharSequenceUtil.isBlank(nfeClienteDTO.getState())) {
            throw new ServiceException("开票省份/州二字码未找到");
        }
        String state = CharSequenceUtil.trim(nfeClienteDTO.getState());
        if (CharSequenceUtil.isBlank(state)) {
            throw new ServiceException("开票省份/州二字码未找到");
        }
        if (state.length() <= 2) {
            nfeClienteDTO.setUf(state.toUpperCase(Locale.ROOT));
            nfeClienteDTO.setState(state.toUpperCase(Locale.ROOT));
            return;
        }
        List<DictCityEntity> dictCityList = FeignQuery.create(DictCityEntity.class)
                .eq(DictCityEntity::getCountryCode, nfeClienteDTO.getCountry())
                .eq(DictCityEntity::getType,"province")
                .list();
        Optional<DictCityEntity> cityOptional = dictCityList.stream()
                .filter(city -> equalsIgnoreCaseAny(state, city.getCode(), city.getCodeEn(), city.getCodePt(), city.getName()))
                .findFirst();
        if (!cityOptional.isPresent()) {
            throw new ServiceException("开票省份/州二字码未找到");
        }
        DictCityEntity dictCityEntity = cityOptional.get();
        nfeClienteDTO.setUf(CharSequenceUtil.blankToDefault(dictCityEntity.getCode(), state).toUpperCase(Locale.ROOT));
        nfeClienteDTO.setState(CharSequenceUtil.isNotBlank(dictCityEntity.getCodePt()) ? dictCityEntity.getCodePt() : nfeClienteDTO.getUf());
    }

    private boolean equalsIgnoreCaseAny(String source, String... targetArr) {
        if (CharSequenceUtil.isBlank(source) || targetArr == null) {
            return false;
        }
        for (String target : targetArr) {
            if (CharSequenceUtil.isNotBlank(target) && CharSequenceUtil.equalsIgnoreCase(source, target)) {
                return true;
            }
        }
        return false;
    }

    private Map<String, OrderItemDetail> getShopeeBrazilOrderItemMap(SoB2cEntity soB2cEntity) {
        if (!isShopeeBrazilOrder(soB2cEntity)) {
            return Collections.emptyMap();
        }
        OrderDetail orderDetail = getShopeeBrazilOrderDetail(soB2cEntity);
        if (ObjUtil.isEmpty(orderDetail) || CollUtil.isEmpty(orderDetail.getItemList())) {
            throw new ServiceException("Shopee订单明细不存在");
        }
        Map<String, OrderItemDetail> itemMap = new HashMap<>();
        for (OrderItemDetail orderItemDetail : orderDetail.getItemList()) {
            if (CharSequenceUtil.isNotBlank(orderItemDetail.getModelSku())) {
                itemMap.put(orderItemDetail.getModelSku(), orderItemDetail);
            }
            if (CharSequenceUtil.isNotBlank(orderItemDetail.getItemSku())) {
                itemMap.put(orderItemDetail.getItemSku(), orderItemDetail);
            }
            if (ObjUtil.isNotEmpty(orderItemDetail.getModelId())) {
                itemMap.put(String.valueOf(orderItemDetail.getModelId()), orderItemDetail);
            }
            if (ObjUtil.isNotEmpty(orderItemDetail.getItemId())) {
                itemMap.put(String.valueOf(orderItemDetail.getItemId()), orderItemDetail);
            }
            if (ObjUtil.isNotEmpty(orderItemDetail.getOrderItemId())) {
                itemMap.put(String.valueOf(orderItemDetail.getOrderItemId()), orderItemDetail);
            }
        }
        return itemMap;
    }

    private OrderDetail getShopeeBrazilOrderDetail(SoB2cEntity soB2cEntity) {
        if (!isShopeeBrazilOrder(soB2cEntity)) {
            return null;
        }
        ShopAuthEntity shopAuthEntity = getShopeeShopAuth(soB2cEntity);
        CfgAppClientEntity cfgAppClientEntity = getShopeeCfgAppClient();
        OrderRequest orderRequest = OrderRequest.builder()
                .host(cfgAppClientEntity.getUrl())
                .token(getShopeeAccessToken(shopAuthEntity))
                .shopId(Long.parseLong(shopAuthEntity.getShopeeId()))
                .partnerId(Long.parseLong(cfgAppClientEntity.getClientId()))
                .tmpPartnerKey(cfgAppClientEntity.getClientSecret())
                .orderSns(soB2cEntity.getPlatformCode())
                .build();
        ShopeeResponse shopeeResponse = shopeeOrderService.getOrderDetail(orderRequest);
        if (ObjUtil.isEmpty(shopeeResponse)) {
            throw new ServiceException("Shopee订单详情接口返回为空");
        }
        if (CharSequenceUtil.isNotBlank(shopeeResponse.getError())) {
            throw new ServiceException(CharSequenceUtil.blankToDefault(shopeeResponse.getMessage(), shopeeResponse.getError()));
        }
        if (ObjUtil.isEmpty(shopeeResponse.getResponse())) {
            throw new ServiceException("Shopee订单详情数据为空");
        }
        JSONArray orderList = shopeeResponse.getResponse().getJSONArray("order_list");
        if (ObjUtil.isEmpty(orderList) || orderList.isEmpty()) {
            throw new ServiceException("Shopee订单详情不存在");
        }
        return orderList.getJSONObject(0).toBean(OrderDetail.class);
    }

    private ShopAuthEntity getShopeeShopAuth(SoB2cEntity soB2cEntity) {
        ShopAuthEntity shopAuthEntity = shopAuthService.getByShopId(soB2cEntity.getShopId());
        if (ObjUtil.isEmpty(shopAuthEntity) || CharSequenceUtil.isBlank(shopAuthEntity.getShopeeId())) {
            throw new ServiceException("Shopee店铺授权信息不存在");
        }
        return shopAuthEntity;
    }

    private String getShopeeAccessToken(ShopAuthEntity shopAuthEntity) {
        String accessToken = CharSequenceUtil.isNotBlank(shopAuthEntity.getAccessToken()) ? shopAuthEntity.getAccessToken() : shopAuthEntity.getToken();
        if (CharSequenceUtil.isBlank(accessToken)) {
            throw new ServiceException("Shopee access_token不存在");
        }
        return accessToken;
    }

    private CfgAppClientEntity getShopeeCfgAppClient() {
        CfgAppClientEntity cfgAppClientEntity = dmpTaskFeign.getCfgAppClient(CfgAppClientDTO.FindDTO.init(AppClientEnum.SHOPEE_ACCESS_TOKEN));
        if (ObjUtil.isEmpty(cfgAppClientEntity)) {
            throw new ServiceException("Shopee基础配置未找到");
        }
        if (CharSequenceUtil.isBlank(cfgAppClientEntity.getUrl())
                || CharSequenceUtil.isBlank(cfgAppClientEntity.getClientId())
                || CharSequenceUtil.isBlank(cfgAppClientEntity.getClientSecret())) {
            throw new ServiceException("Shopee基础配置不完整");
        }
        return cfgAppClientEntity;
    }

    private BigDecimal getShopeeBrazilUnitPrice(SoB2cEntity soB2cEntity, SoB2cDetailEntity detailEntity, String dictInvoiceRule, BigDecimal ratio, Map<String, OrderItemDetail> shopeeOrderItemMap) {
        if (CharSequenceUtil.equals(dictInvoiceRule, InvoiceRuleEnum.DEDUCT.getCode())) {
            throw new ServiceException(SHOPEE_BR_DEDUCT_ERROR_MSG);
        }
        OrderItemDetail orderItemDetail = matchShopeeBrazilOrderItem(detailEntity, shopeeOrderItemMap);
        BigDecimal price = BigDecimal.valueOf(orderItemDetail.getModelOriginalPrice());
        if (CharSequenceUtil.equals(dictInvoiceRule, InvoiceRuleEnum.CUSTOM.getCode())) {
            price = MathUtil.divide(MathUtil.multiplyWithTwo(price, ratio), MathUtil.BigDecimal_100);
        }
        return convertToBrl(soB2cEntity, price, detailEntity.getCurrency(), detailEntity.getExchangeRate());
    }

    private OrderItemDetail matchShopeeBrazilOrderItem(SoB2cDetailEntity detailEntity, Map<String, OrderItemDetail> shopeeOrderItemMap) {
        if (CollUtil.isEmpty(shopeeOrderItemMap)) {
            throw new ServiceException("Shopee订单明细不存在");
        }
        List<String> matchKeyList = Arrays.asList(
                detailEntity.getPlatformSkuNo(),
                detailEntity.getPlatformSkuId(),
                detailEntity.getThirdDetailId(),
                detailEntity.getSourceDetailId()
        );
        for (String matchKey : matchKeyList) {
            if (CharSequenceUtil.isNotBlank(matchKey) && ObjUtil.isNotEmpty(shopeeOrderItemMap.get(matchKey))) {
                return shopeeOrderItemMap.get(matchKey);
            }
        }
        if (shopeeOrderItemMap.size() == 1) {
            return shopeeOrderItemMap.values().iterator().next();
        }
        throw new ServiceException(CharSequenceUtil.format("Shopee订单明细未匹配到SKU，平台SKU：{}", detailEntity.getPlatformSkuNo()));
    }

    private void uploadShopeeBrazilInvoice(SoB2cEntity soB2cEntity) {
        InvoiceInfoDTO.AttachDTO attachDTO = invoiceInfoService.getNewInvoicedAttachBySoId(soB2cEntity.getId(), InvoiceInfoInvoiceTypeEnum.NFE.getCode(), AttachmentTypeEnum.INVOICE_INFO_PDF.getCode());
        if (ObjUtil.isEmpty(attachDTO)) {
            throw new ServiceException("NF-e发票未找到pdf文件");
        }
        byte[] pdfBytes = FastDFSClientUtil.getFileByte(attachDTO.getAttachUrl());
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new ServiceException("获取pdf文件失败");
        }
        if (pdfBytes.length > 1024 * 1024) {
            throw new ServiceException("Shopee上传发票PDF大小不能超过1MB");
        }
        ShopAuthEntity shopAuthEntity = getShopeeShopAuth(soB2cEntity);
        CfgAppClientEntity cfgAppClientEntity = getShopeeCfgAppClient();
        long timestamp = System.currentTimeMillis() / 1000L;
        String accessToken = getShopeeAccessToken(shopAuthEntity);
        String path = "/api/v2/order/upload_invoice_doc";
        String uploadUrl = buildShopeeUploadInvoiceUrl(cfgAppClientEntity, shopAuthEntity, accessToken, path, timestamp);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("order_sn", soB2cEntity.getPlatformCode())
                .addFormDataPart("file_type", "1")
                .addFormDataPart("file", CharSequenceUtil.blankToDefault(attachDTO.getAttachName(), soB2cEntity.getCode() + ".pdf"),
                        RequestBody.create(MediaType.parse("application/pdf"), pdfBytes))
                .build();
        Request request = new Request.Builder().url(uploadUrl).post(requestBody).build();
        try (Response response = new OkHttpClient().newCall(request).execute()) {
            String responseBody = response.body() == null ? CharSequenceUtil.EMPTY : response.body().string();
            if (!response.isSuccessful()) {
                throw new ServiceException(CharSequenceUtil.format("Shopee上传发票失败,httpStatus:{}, body:{}", response.code(), responseBody));
            }
            ShopeeResponse shopeeResponse = JSONUtil.toBean(responseBody, ShopeeResponse.class);
            if (ObjUtil.isEmpty(shopeeResponse)) {
                throw new ServiceException("Shopee上传发票返回为空");
            }
            if (CharSequenceUtil.isNotBlank(shopeeResponse.getError())) {
                throw new ServiceException(CharSequenceUtil.blankToDefault(shopeeResponse.getMessage(), shopeeResponse.getError()));
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(CharSequenceUtil.format("Shopee上传发票失败: {}", e.getMessage()));
        }
    }

    private String buildShopeeUploadInvoiceUrl(CfgAppClientEntity cfgAppClientEntity, ShopAuthEntity shopAuthEntity, String accessToken, String path, long timestamp) {
        Map<String, Object> urlParamMap = new LinkedHashMap<>();
        urlParamMap.put("partner_id", cfgAppClientEntity.getClientId());
        urlParamMap.put("timestamp", timestamp);
        urlParamMap.put("access_token", accessToken);
        urlParamMap.put("shop_id", shopAuthEntity.getShopeeId());
        urlParamMap.put("sign", ShopeeApiUtils.getOrderSign(path, accessToken, Long.parseLong(cfgAppClientEntity.getClientId()), cfgAppClientEntity.getClientSecret(), Long.parseLong(shopAuthEntity.getShopeeId()), timestamp));
        return ShopeeApiUtils.buildUrl(cfgAppClientEntity.getUrl() + path, urlParamMap);
    }

    private BigDecimal convertToBrl(SoB2cEntity soB2cEntity, BigDecimal price, String currency, BigDecimal exchangeRate) {
        if (Objects.isNull(price)) {
            return BigDecimal.ZERO;
        }
        if (CharSequenceUtil.isBlank(currency) || CurrencyEnum.BRL.getCurrencyCode().equals(currency)) {
            return price;
        }
        if (Objects.isNull(exchangeRate)) {
            throw new ServiceException("订单明细汇率为空，请维护汇率后再提交");
        }
        try {
            BigDecimal rate = dmpTaskFeign.getRate(soB2cEntity.getBillDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), CurrencyEnum.BRL.getCurrencyCode());
            return MathUtil.divide(MathUtil.multiplyWithFour(exchangeRate,price), rate);
        }catch (Exception e){
            throw new ServiceException( CurrencyEnum.BRL.getCurrencyName() + "汇率为空，请维护汇率后再提交");
        }
    }

    private String sanitizeCep(String cep) {
        return CharSequenceUtil.isBlank(cep) ? CharSequenceUtil.EMPTY : removeSignAndSpace(cep);
    }

    /**
     * 取消发票（旧接口）
     * @deprecated 请使用 cancelInvoiceV2 方法
     * @author will
     * @date 2025/4/14 14:58
     * @param invoiceInfoEntity
     * @param nfeCancelDTO
     * @return void
     */
    @Deprecated
    public void cancelInvoice(InvoiceInfoEntity invoiceInfoEntity,NfeInvoiceDTO.NfeCancelDTO nfeCancelDTO) {
        //b2c订单信息
        SoB2cEntity soB2cEntity = soB2cService.getById(invoiceInfoEntity.getSoId());
        if (Objects.isNull(soB2cEntity)){
            throw new ServiceException(ApiError.SO_B2C_NOT_FOUND);
        }
        if (CharSequenceUtil.isBlank(soB2cEntity.getDictPlatform()) || CharSequenceUtil.isBlank(soB2cEntity.getShopId())) {
            throw new ServiceException(ApiError.SO_B2C_PLATFORM_SHOP_REQUIRED,soB2cEntity.getCode());
        }
        //税务信息
        CfgInvoiceSettingDetailEntity invoiceSettingDetail = cfgInvoiceSettingDetailService.getInvoiceSettingDetail(soB2cEntity.getDictPlatform(), soB2cEntity.getShopId());
        nfeCancelDTO.setTokenEmpresa(invoiceSettingDetail.getToken());
        Object obj;
        try {
             obj = tfFiscalService.cancelInvoice(nfeCancelDTO);
        } catch (Exception e) {
            throw new ServiceException(ApiError.FIN_INVOICE_NFE_CANCEL_FAILED,e.getMessage());
        }
    }

    /**
     * 取消发票（新接口V2）
     * 使用新接口路径：/api/invoice/cancel
     * 
     * @param invoiceInfoEntity 发票信息实体
     * @param cancelReason 取消原因（必填）
     * @return 取消发票响应数据DTO（data部分）
     */
    @Transactional(rollbackFor = Exception.class)
    public CancelInvoiceResponseDTO.CancelInvoiceDataDTO cancelInvoiceV2(InvoiceInfoEntity invoiceInfoEntity, String cancelReason) {
        if (CharSequenceUtil.isBlank(cancelReason)) {
            throw new ServiceException("取消原因不能为空");
        }
        
        //b2c订单信息
        SoB2cEntity soB2cEntity = soB2cService.getById(invoiceInfoEntity.getSoId());
        if (Objects.isNull(soB2cEntity)){
            throw new ServiceException(ApiError.SO_B2C_NOT_FOUND);
        }
        if (CharSequenceUtil.isBlank(soB2cEntity.getDictPlatform()) || CharSequenceUtil.isBlank(soB2cEntity.getShopId())) {
            throw new ServiceException(ApiError.SO_B2C_PLATFORM_SHOP_REQUIRED,soB2cEntity.getCode());
        }
        
        // 获取发票UUID（queryId字段存储的是uuid）
        String uuid = invoiceInfoEntity.getQueryId();
        if (CharSequenceUtil.isBlank(uuid)) {
            throw new ServiceException("发票UUID不存在，无法进行取消操作");
        }
        
        //税务信息
        CfgInvoiceSettingDetailEntity invoiceSettingDetail = cfgInvoiceSettingDetailService.getInvoiceSettingDetail(soB2cEntity.getDictPlatform(), soB2cEntity.getShopId());
        String companyToken = invoiceSettingDetail.getToken();
        if (CharSequenceUtil.isBlank(companyToken)) {
            throw new ServiceException("公司token不能为空");
        }
        
        // 构建取消发票DTO
        CancelInvoiceDTO cancelInvoiceDTO = new CancelInvoiceDTO();
        cancelInvoiceDTO.setUuid(uuid);
        
        // 调用新接口
        CancelInvoiceResponseDTO.CancelInvoiceDataDTO responseData;
        try {
            responseData = tfFiscalService.cancelInvoiceV2(cancelInvoiceDTO, companyToken);
            log.warn("取消发票（新接口V2）响应, uuid: {}, status: {}", uuid, responseData.getStatus());
        } catch (Exception e) {
            log.error("取消发票失败, uuid: {}, 错误: {}", uuid, e.getMessage(), e);
            throw new ServiceException(ApiError.FIN_INVOICE_NFE_CANCEL_FAILED, e.getMessage());
        }
        
        // 根据返回结果确认是否取消成功
        String status = responseData.getStatus();
        if (InvoiceStatusMapper.isCanceled(status)) {
            // 取消成功
            // 修改原单据的性质从普通改为取消
            invoiceInfoEntity.setInvoiceNature(InvoiceNatureEnum.CANCEL.getCode());
            invoiceInfoEntity.setCancelReason(cancelReason);
            
            // 如果返回了新的XML，替换XML并重新上传
            if (CharSequenceUtil.isNotBlank(responseData.getXml())) {
                // 删除旧的XML附件
                // TODO: 删除旧XML附件的逻辑

                // 上传新的XML
                uploadFile(invoiceInfoEntity.getId(), responseData.getXml(), "");

                // 更新queryKey（如果chave有变化）
                if (CharSequenceUtil.isNotBlank(responseData.getChave())) {
                    invoiceInfoEntity.setQueryKey(responseData.getChave());
                }
            }
            
            // 取消发票成功后，重新获取并上传PDF（PDF内容会更新为取消状态）
            try {
                generateAndUploadPdfFromDanfe(invoiceInfoEntity.getId(), uuid, companyToken);
            } catch (Exception e) {
                // PDF上传失败时，记录失败原因到备注，但不影响主流程
                String pdfErrorMsg = CharSequenceUtil.format("取消发票后重新上传PDF失败: {}", e.getMessage());
                invoiceInfoEntity.setRemark(pdfErrorMsg);
                log.error("取消发票后重新上传PDF失败, invoiceId: {}, uuid: {}", invoiceInfoEntity.getId(), uuid, e);
            }

            // 重新上传至平台（如果配置了自动上传）
            if (shouldAutoUploadInvoice(soB2cEntity, invoiceSettingDetail)) {
                try {
                    invoiceInfoService.uploadNfeInvoice(soB2cEntity, invoiceInfoEntity.getId());
                } catch (Exception e) {
                    // 上传失败时，记录失败原因到备注
                    String uploadErrorMsg = CharSequenceUtil.format("取消发票后重新上传平台失败: {}", e.getMessage());
                    invoiceInfoEntity.setRemark(uploadErrorMsg);
                    log.error("取消发票后重新上传平台失败, invoiceId: {}", invoiceInfoEntity.getId(), e);
                }
            }
            
            // 更新发票信息
            invoiceInfoService.updateById(invoiceInfoEntity);
            
            log.warn("取消发票成功, invoiceId: {}, uuid: {}", invoiceInfoEntity.getId(), uuid);
        } else if ("Failed".equals(status)) {
            // 取消失败
            String motivo = CharSequenceUtil.isNotBlank(responseData.getMotivo()) ? responseData.getMotivo() : "取消发票失败";
            log.error("取消发票失败, uuid: {}, motivo: {}", uuid, motivo);
            throw new ServiceException(ApiError.FIN_INVOICE_NFE_CANCEL_FAILED, motivo);
        } else {

            // 其他状态
            String motivo = CharSequenceUtil.isNotBlank(responseData.getMotivo()) ? responseData.getMotivo() : "未知状态";
            log.error("取消发票返回未知状态, uuid: {}, status: {}, motivo: {}", uuid, status, motivo);
            throw new ServiceException(ApiError.FIN_INVOICE_NFE_CANCEL_FAILED, CharSequenceUtil.format("取消发票返回未知状态: {}, {}", status, motivo));
        }
        
        return responseData;
    }

    /**
     * 退票（旧接口）
     * @deprecated 请使用 returnInvoiceV2 方法
     * @param invoiceInfoEntity
     * @param nfeReturnDTO
     */
    @Deprecated
    public void returnInvoice(InvoiceInfoEntity invoiceInfoEntity,NfeInvoiceDTO.NfeReturnDTO nfeReturnDTO) {
        //b2c订单信息
        SoB2cEntity soB2cEntity = soB2cService.getById(invoiceInfoEntity.getSoId());
        if (Objects.isNull(soB2cEntity)){
            throw new ServiceException(ApiError.SO_B2C_NOT_FOUND);
        }
        if (CharSequenceUtil.isBlank(soB2cEntity.getDictPlatform()) || CharSequenceUtil.isBlank(soB2cEntity.getShopId())) {
            throw new ServiceException(ApiError.SO_B2C_PLATFORM_SHOP_REQUIRED,soB2cEntity.getCode());
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
            throw new ServiceException(ApiError.FIN_INVOICE_NFE_RETURN_FAILED,e.getMessage());
        }
        //解析是否成功，失败抛出异常原因
        NfeInvoiceDTO.NfeReturnResultDTO resultDTO = JSONUtil.toBean(obj.toString(), NfeInvoiceDTO.NfeReturnResultDTO.class);
        if (Objects.isNull(resultDTO) || resultDTO.getErro()){
            throw new ServiceException("退票接口异常：{}",resultDTO.getMsg());
        }
    }

    /**
     * 退货发票（新接口V2）
     * 使用新接口路径：/api/invoice/return
     * 取消失败的发票可使用退货发票
     * 
     * @param invoiceInfoEntity 发票信息实体（原发票）
     * @param returnReason 退货原因（natureza_operacao）
     * @param returnTaxCode 退货CFOP（4位数字）
     * @return 退货发票响应数据DTO（data部分）
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnInvoiceResponseDTO.ReturnInvoiceDataDTO returnInvoiceV2(
            InvoiceInfoEntity invoiceInfoEntity, String returnReason, String returnTaxCode) {
        if (CharSequenceUtil.isBlank(returnReason)) {
            throw new ServiceException("退货原因不能为空");
        }
        if (CharSequenceUtil.isBlank(returnTaxCode)) {
            throw new ServiceException("退货CFOP不能为空");
        }
        
        //b2c订单信息
        SoB2cEntity soB2cEntity = soB2cService.getById(invoiceInfoEntity.getSoId());
        if (Objects.isNull(soB2cEntity)){
            throw new ServiceException(ApiError.SO_B2C_NOT_FOUND);
        }
        if (CharSequenceUtil.isBlank(soB2cEntity.getDictPlatform()) || CharSequenceUtil.isBlank(soB2cEntity.getShopId())) {
            throw new ServiceException(ApiError.SO_B2C_PLATFORM_SHOP_REQUIRED,soB2cEntity.getCode());
        }
        
        // 获取发票UUID或chave（优先使用chave，如果没有则使用uuid）
        String chave = invoiceInfoEntity.getQueryKey();
        String uuid = invoiceInfoEntity.getQueryId();
        if (CharSequenceUtil.isBlank(chave) && CharSequenceUtil.isBlank(uuid)) {
            throw new ServiceException("发票UUID/chave不存在，无法进行退货操作");
        }
        // 用于日志和查询的标识
        String uuidOrChave = CharSequenceUtil.isNotBlank(chave) ? chave : uuid;
        
        //税务信息
        CfgInvoiceSettingDetailEntity invoiceSettingDetail = cfgInvoiceSettingDetailService.getInvoiceSettingDetail(soB2cEntity.getDictPlatform(), soB2cEntity.getShopId());
        CfgInvoiceSettingEntity invoiceSetting = cfgInvoiceSettingService.getById(invoiceSettingDetail.getMainId());
        if (ObjUtil.isEmpty(invoiceSetting)) {
            throw new ServiceException("发票设置不存在");
        }
        String companyToken = invoiceSettingDetail.getToken();
        if (CharSequenceUtil.isBlank(companyToken)) {
            throw new ServiceException("公司token不能为空");
        }
        // 构建退货发票DTO
        ReturnInvoiceDTO returnInvoiceDTO = new ReturnInvoiceDTO();
        // 根据实际情况设置uuid或chave（二选一）
        if (CharSequenceUtil.isNotBlank(chave)) {
            returnInvoiceDTO.setChave(chave);
        } else if (CharSequenceUtil.isNotBlank(uuid)) {
            returnInvoiceDTO.setUuid(uuid);
        }
        returnInvoiceDTO.setCfop(returnTaxCode);
        returnInvoiceDTO.setNatureOfOperation(returnReason);
        
        // 构建return_detail（第三方生成的发票需填写）
        ReturnInvoiceDTO.ReturnDetailDTO returnDetailDTO = buildReturnDetailDTO(soB2cEntity, invoiceSettingDetail, invoiceSetting);
//        returnInvoiceDTO.setReturnDetail(returnDetailDTO);
        
        // 调用新接口
        ReturnInvoiceResponseDTO.ReturnInvoiceDataDTO responseData;
        try {
            responseData = tfFiscalService.returnInvoiceV2(returnInvoiceDTO, companyToken);
            log.warn("退货发票（新接口V2）响应, uuid/chave: {}, status: {}", uuidOrChave, responseData.getStatus());
        } catch (Exception e) {
            log.error("退货发票失败, uuid/chave: {}, 错误: {}", uuidOrChave, e.getMessage(), e);
            throw new ServiceException(ApiError.FIN_INVOICE_NFE_RETURN_FAILED, e.getMessage());
        }
        
        // 根据返回结果确认是否退货成功
        String status = responseData.getStatus();
        if (InvoiceStatusMapper.isSuccess(status)) {
            // 退货成功
            // 如果返回了XML，上传XML文件
            if (CharSequenceUtil.isNotBlank(responseData.getXml())) {
                uploadFile(invoiceInfoEntity.getId(), responseData.getXml(), "");
            }
            // 获取Danfe PDF并上传（退货发票的UUID）
            if (CharSequenceUtil.isNotBlank(responseData.getUuid())) {
                generateAndUploadPdfFromDanfe(invoiceInfoEntity.getId(), responseData.getUuid(), companyToken);
            }
            log.warn("退货发票成功, invoiceId: {}, uuid/chave: {}", invoiceInfoEntity.getId(), uuidOrChave);
        } else if (InvoiceStatusMapper.isFailed(status)) {
            // 退货失败
            String motivo = CharSequenceUtil.isNotBlank(responseData.getMotivo()) ? responseData.getMotivo() : "退货发票失败";
            log.error("退货发票失败, uuid/chave: {}, motivo: {}", uuidOrChave, motivo);
            throw new ServiceException(ApiError.FIN_INVOICE_NFE_RETURN_FAILED, motivo);
        } else {
            // 其他状态
            String motivo = CharSequenceUtil.isNotBlank(responseData.getMotivo()) ? responseData.getMotivo() : "未知状态";
            log.error("退货发票返回未知状态, uuid/chave: {}, status: {}, motivo: {}", uuidOrChave, status, motivo);
            throw new ServiceException(ApiError.FIN_INVOICE_NFE_RETURN_FAILED, CharSequenceUtil.format("退货发票返回未知状态: {}, {}", status, motivo));
        }
        
        return responseData;
    }

    /**
     * 构建退货详情DTO（return_detail）
     * 复用开具发票的逻辑，但使用退货相关的配置
     */
    private ReturnInvoiceDTO.ReturnDetailDTO buildReturnDetailDTO(
            SoB2cEntity soB2cEntity, 
            CfgInvoiceSettingDetailEntity invoiceSettingDetail,
            CfgInvoiceSettingEntity invoiceSetting) {
        ReturnInvoiceDTO.ReturnDetailDTO returnDetailDTO = new ReturnInvoiceDTO.ReturnDetailDTO();
        
        // 基础字段
        // 使用销售单号code生成15位唯一值，避免ID截取导致的重复问题
        String invoiceId = generateUniqueInvoiceId(soB2cEntity.getCode());
        returnDetailDTO.setId(invoiceId);
        returnDetailDTO.setTransactionType("1"); // 出项发票
        returnDetailDTO.setModel("55"); // NFe
        returnDetailDTO.setIssuanceType("1"); // 正常
        returnDetailDTO.setAmbiente(getAmbiente());
        returnDetailDTO.setTotalDiscountAmount(BigDecimal.ZERO);
        
        // 客户信息（和开具发票保持一致）
        CreateInvoiceDTO.ClienteDTO clienteDTO = buildClienteDTO(soB2cEntity, invoiceSettingDetail);
        returnDetailDTO.setCliente(clienteDTO);
        
        // 商品列表（需要获取订单明细，但可能需要调整数量和价格）
        // 这里先使用开具发票的逻辑，后续可根据业务需求调整
        List<SoB2cDetailEntity> detailList = soB2cDetailService.listByMainId(soB2cEntity.getId());
        if (CollUtil.isEmpty(detailList)) {
            throw new ServiceException(ApiError.SO_B2C_DETAIL_NOT_FOUND);
        }
        
        // 获取开票规则和比例（可能需要从原发票获取）
        String dictInvoiceRule = invoiceSettingDetail.getDictInvoiceRule();
        BigDecimal ratio = invoiceSettingDetail.getRatio();
        
        List<CreateInvoiceDTO.ProductDTO> products = buildProductDTOList(soB2cEntity, invoiceSettingDetail, invoiceSetting, dictInvoiceRule, ratio);
        returnDetailDTO.setProducts(products);
        
        // 运输信息
        CreateInvoiceDTO.TransportationDTO transportationDTO = buildTransportationDTO(soB2cEntity, invoiceSettingDetail);
        returnDetailDTO.setTransportation(transportationDTO);
        
        return returnDetailDTO;
    }
    /**
     * 作废发票号（旧接口）
     * @deprecated 请使用 invalidInvoiceV2 方法
     * @param nfeVoidedDTO
     */
    @Deprecated
    public void voidedInvoice(NfeInvoiceDTO.NfeVoidedDTO nfeVoidedDTO) {
        Object obj;
        try {
            log.error("作废接口请求：{}", JSONUtil.toJsonStr(nfeVoidedDTO));
            obj = tfFiscalService.voidedInvoice(nfeVoidedDTO);
            log.error("作废接口返回：{}", JSONUtil.toJsonStr(obj));
        } catch (Exception e) {
            throw new ServiceException(ApiError.FIN_INVOICE_NFE_VOID_FAILED,e.getMessage());
        }
        //{"retorno":{"attributes":{"versao":"4.00"},"infInut":{"tpAmb":"1","verAplic":"SP_NFE_PL009_V4","cStat":"102","xMotivo":"Inutilização de número homologado","cUF":"35","ano":"25","CNPJ":"59399522000150","mod":"55","serie":"1","nNFIni":"1","nNFFin":"2","dhRecbto":"2025-07-10T00:53:25-03:00","nProt":"135251896535761"}}}

    }

    /**
     * 作废发票号（新接口V2）
     * 使用新接口路径：/api/invoice/invalid
     * 作废发票一般使用场景为发票号跳号时使用
     * 
     * @param cfgInvoiceSettingId 发票设置ID
     * @param serie 发票序列号
     * @param startNumber 作废起始号
     * @param endNumber 作废结束号
     * @param motivo 作废原因
     * @return 作废发票响应数据DTO（data部分）
     */
    @Transactional(rollbackFor = Exception.class)
    public InvalidInvoiceResponseDTO.InvalidInvoiceDataDTO invalidInvoiceV2(
            String cfgInvoiceSettingId, String serie, String startNumber, String endNumber, String motivo) {
        if (CharSequenceUtil.isBlank(cfgInvoiceSettingId)) {
            throw new ServiceException("发票设置ID不能为空");
        }
        if (CharSequenceUtil.isBlank(serie)) {
            throw new ServiceException("发票序列号不能为空");
        }
        if (CharSequenceUtil.isBlank(startNumber)) {
            throw new ServiceException("作废起始号不能为空");
        }
        if (CharSequenceUtil.isBlank(endNumber)) {
            throw new ServiceException("作废结束号不能为空");
        }
        if (CharSequenceUtil.isBlank(motivo)) {
            throw new ServiceException("作废原因不能为空");
        }
        
        // 获取发票设置信息
        CfgInvoiceSettingEntity settingEntity = cfgInvoiceSettingService.getById(cfgInvoiceSettingId);
        if (settingEntity == null || CharSequenceUtil.isBlank(settingEntity.getToken())) {
            throw new ServiceException("发票授权信息不存在或token为空");
        }
        String companyToken = settingEntity.getToken();
        
        // 构建作废发票DTO
        InvalidInvoiceDTO invalidInvoiceDTO = new InvalidInvoiceDTO();
        invalidInvoiceDTO.setModelo("55"); // 默认NFe模式
        invalidInvoiceDTO.setAmbiente(getAmbiente()); // 从配置获取ambiente
        invalidInvoiceDTO.setSerie(serie);
        invalidInvoiceDTO.setStartNumber(startNumber);
        invalidInvoiceDTO.setEndNumber(endNumber);
        invalidInvoiceDTO.setMotivo(motivo);
        
        log.info("作废发票号（新接口V2）, invoiceSettingId: {}, serie: {}, startNumber: {}, endNumber: {}", 
            cfgInvoiceSettingId, serie, startNumber, endNumber);
        
        // 调用新接口
        InvalidInvoiceResponseDTO.InvalidInvoiceDataDTO response;
        try {
            response = tfFiscalService.invalidInvoiceV2(invalidInvoiceDTO, companyToken);
            log.info("作废发票号成功, uuid: {}, xml: {}", response.getUuid(), response.getXml());
        } catch (Exception e) {
            log.error("作废发票号失败", e);
            throw new ServiceException("作废发票号失败: " + e.getMessage());
        }
        
        return response;
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
            throw new ServiceException(ApiError.FIN_INVOICE_NFE_UPDATE_CCE_FAILED,e.getMessage());
        }
        NfeInvoiceDTO.NfeCceResultDTO resultDTO = new NfeInvoiceDTO.NfeCceResultDTO();
        try {
            //解析obj
            resultDTO = JSONUtil.toBean(obj.toString(), NfeInvoiceDTO.NfeCceResultDTO.class);
        } catch (Exception e) {
            log.error("解析信息失败,返回信息:{}", JSONUtil.toJsonStr(resultDTO));
            throw new ServiceException(ApiError.FIN_INVOICE_NFE_JSON_PARSE_FAILED);
        }
        if (resultDTO.getErro() || 200 !=  resultDTO.getStatus()) {
            log.error("创建发票失败,返回错误信息,返回信息:{}", JSONUtil.toJsonStr(resultDTO));
            throw new ServiceException(ApiError.FIN_INVOICE_NFE_UPDATE_CCE_FAILED,"未知");
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
            throw new ServiceException(ApiError.FIN_COMPANY_TOKEN_NOT_FOUND);
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
            throw new ServiceException(ApiError.FIN_INVOICE_UPLOAD_FILE_NOT_FOUND);
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
     * 生成15位唯一的发票ID
     * 基于销售单号code生成，确保唯一性且可追溯
     * 
     * 方案：
     * 1. 如果code长度<=15，直接使用（不补零）
     * 2. 如果code长度>15，使用MD5哈希，取前15位
     * 
     * 追溯方式：
     * 1. 如果code<=15位：
     *    - 生成的ID就是原始code，可以直接使用
     *    - 例如：code="SO12345" -> ID="SO12345"
     * 
     * 2. 如果code>15位（使用MD5）：
     *    - MD5是单向哈希函数，无法从哈希值反向推导原始值
     *    - 追溯方法：通过InvoiceInfoEntity.soCode字段查询
     *      - 通过第三方返回的uuid查询：SELECT so_code FROM invoice_info WHERE query_id = 'uuid'
     *      - 通过销售订单ID查询：SELECT so_code FROM invoice_info WHERE so_id = '销售订单ID'
     * 
     * 注意：InvoiceInfoEntity在创建时会设置soCode字段（见buildNfeInvoiceEntity方法），
     *       所以可以通过soCode字段直接追溯原始销售单号，无需反向MD5
     * 
     * @param salesOrderCode 销售单号code
     * @return 15位唯一的发票ID
     */
    private String generateUniqueInvoiceId(String salesOrderCode) {
        if (CharSequenceUtil.isBlank(salesOrderCode)) {
            throw new ServiceException("销售单号不能为空");
        }
        
        // 如果code长度<=15，直接使用（不补零）
        if (salesOrderCode.length() <= 15) {
            log.debug("销售单号code: {}, 长度: {}, 生成的发票ID: {} (可直接追溯)", 
                salesOrderCode, salesOrderCode.length(), salesOrderCode);
            return salesOrderCode;
        }
        
        // 如果code长度>15，使用MD5哈希，取前15位
        // MD5哈希可以确保不同code生成不同的15位值（碰撞概率极低）
        // 注意：MD5无法反向，需要通过InvoiceInfoEntity.soCode字段追溯原始code
        String md5Hash = Md5Util.md5(salesOrderCode);
        // MD5返回32位十六进制字符串，取前15位
        String invoiceId = md5Hash.substring(0, 15);
        
        log.debug("销售单号code: {}, 长度: {}, MD5哈希: {}, 生成的发票ID: {} (需通过soCode字段追溯)", 
            salesOrderCode, salesOrderCode.length(), md5Hash, invoiceId);
        return invoiceId;
    }

    /**
     * 从15位发票ID追溯原始销售单号code
     * 
     * 追溯逻辑：
     * 1. 如果invoiceId长度<=15，可能就是原始code（但需要通过uuid验证确认）
     *    - 例如：invoiceId="SO12345" 可能就是原始code="SO12345"
     * 
     * 2. 如果invoiceId长度=15且是MD5哈希格式（code>15位的情况）：
     *    - MD5是单向哈希函数，无法从哈希值反向推导原始值
     *    - 必须通过数据库查询InvoiceInfoEntity.soCode字段
     *    - 推荐方式：通过第三方返回的uuid查询
     *      SQL: SELECT so_code FROM invoice_info WHERE query_id = 'uuid'
     *    - 或者通过销售订单ID查询：
     *      SQL: SELECT so_code FROM invoice_info WHERE so_id = '销售订单ID'
     * 
     * 注意：InvoiceInfoEntity在创建时会设置soCode字段（见InvoiceInfoServiceImpl.buildNfeInvoiceEntity方法），
     *       所以可以通过soCode字段直接追溯原始销售单号，无需反向MD5
     * 
     * @param invoiceId 15位发票ID（第三方接口返回的id字段）
     * @param uuid 第三方返回的uuid（推荐使用，用于精确查询）
     * @return 原始销售单号code，如果无法追溯则返回null
     */
    public String traceBackToSalesOrderCode(String invoiceId, String uuid) {
        if (CharSequenceUtil.isBlank(invoiceId)) {
            return null;
        }
        
        // 方式1：如果invoiceId长度<=15，可能就是原始code（直接返回）
        if (invoiceId.length() <= 15) {
            log.debug("invoiceId长度<=15，可能是原始code, invoiceId: {}", invoiceId);
            // 注意：这里不能直接返回，因为可能是MD5的前15位也是<=15的情况
            // 但根据生成逻辑，如果code<=15，生成的ID就是code本身，所以这里可以尝试返回
            // 如果需要更精确，可以通过uuid查询验证
        }
        
        // 方式2：通过uuid查询InvoiceInfoEntity获取soCode（推荐方式）
        if (CharSequenceUtil.isNotBlank(uuid)) {
            try {
                // 通过queryId（uuid）查询InvoiceInfoEntity
                List<InvoiceInfoEntity> invoiceList = invoiceInfoService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InvoiceInfoEntity>()
                        .eq(InvoiceInfoEntity::getQueryId, uuid)
                        .last("limit 1")
                );
                if (CollUtil.isNotEmpty(invoiceList)) {
                    InvoiceInfoEntity invoiceInfo = invoiceList.get(0);
                    if (CharSequenceUtil.isNotBlank(invoiceInfo.getSoCode())) {
                        log.debug("通过uuid追溯销售单号成功, uuid: {}, invoiceId: {} -> code: {}", 
                            uuid, invoiceId, invoiceInfo.getSoCode());
                        return invoiceInfo.getSoCode();
                    }
                }
            } catch (Exception e) {
                log.warn("通过uuid追溯销售单号失败, uuid: {}, invoiceId: {}", uuid, invoiceId, e);
            }
        }
        
        // 方式3：如果invoiceId是MD5格式，无法直接反向
        // 需要通过其他方式查询（如通过销售订单ID、时间范围等）
        log.warn("无法直接追溯销售单号, invoiceId: {}, uuid: {} (MD5无法反向，需通过数据库查询soCode字段)", 
            invoiceId, uuid);
        return null;
    }

    /**
     * 获取ambiente配置值
     * ambiente用于区分线上和测试的开票环境
     * 
     * ambiente值说明：
     * - "1" = Produção(正式环境/生产环境)
     * - "2" = Homologação(测试环境)
     * 
     * 优先级：
     * 1. 从cfg_setting表读取（key="tfAmbiente"），value可以是字符串"1"或"2"，也可以是JSON格式{"ambiente":"1"}或{"ambiente":"2"}
     * 2. 如果没有配置，则根据Spring Profile判断：
     *    - prod环境 → "1"（正式环境/生产环境）
     *    - 其他环境（test/uat/dev） → "2"（测试环境）
     * 
     * @return ambiente值，"1"（正式环境）或"2"（测试环境）
     */
    private String getAmbiente() {
        try {
            // 从cfg_setting表读取（key="tfAmbiente"）
            CfgSettingEntity tfAmbienteSetting = cfgSettingService.getSettingByKey("tfAmbiente");
            if (tfAmbienteSetting != null && CharSequenceUtil.isNotBlank(tfAmbienteSetting.getValue())) {
                String ambiente = tfAmbienteSetting.getValue().trim();
                // 如果是JSON格式，尝试解析
                if (ambiente.startsWith("{")) {
                    try {
                        JSONObject jsonObject = JSONUtil.parseObj(ambiente);
                        ambiente = jsonObject.getStr("ambiente");
                    } catch (Exception e) {
                        log.warn("解析cfg_setting(tfAmbiente)的JSON失败，将使用默认策略", e);
                    }
                }
                // 验证值是否有效（只能是"1"或"2"）
                if (CharSequenceUtil.isNotBlank(ambiente) && ("1".equals(ambiente) || "2".equals(ambiente))) {
                    log.debug("从cfg_setting表(tfAmbiente)读取ambiente配置: {}", ambiente);
                    return ambiente;
                } else {
                    log.warn("cfg_setting(tfAmbiente)的值无效: {}, 将使用默认策略", ambiente);
                }
            }
            
            // 根据Spring Profile判断
            // ambiente值："1"=正式环境，"2"=测试环境
            boolean isProd = BusinessCommonConstants.hasProfile("prod");
            // 正式环境传"1"，测试环境传"2"
            String ambiente = isProd ? "1" : "2";
            log.info("根据Spring Profile判断ambiente: profile包含prod={}, ambiente={} (prod传1=正式环境, 其他传2=测试环境)", isProd, ambiente);
            return ambiente;
            
        } catch (Exception e) {
            log.error("获取ambiente配置失败，使用默认值", e);
            // 默认使用"2"（测试环境），避免误操作生产数据
            // 如果系统是prod环境，建议在cfg_setting表中配置key="tfAmbiente", value="1"
            return "2";
        }
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

//    @Transactional(rollbackFor = Exception.class)
    public void createInvoiceProcess(SoB2cEntity soB2cEntity, InvoiceInfoEntity invoiceInfoEntity) {
         log.warn("记录异步开票日志开始：{}",invoiceInfoEntity.getCode());
        try {
            Boolean result = this.createInvoiceV2(soB2cEntity);
            if (result){
                //添加日志
                operateLogService.addModuleOperateLog(CharSequenceUtil.format("用户【{}】销售订单【{}】生成NF-e发票【{}】", UserContext.getDefaultLoginUser().getUserName(),soB2cEntity.getCode(),invoiceInfoEntity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "生成NF-e发票操作");
            }else {
                //添加日志
                operateLogService.addModuleOperateLog(CharSequenceUtil.format("用户【{}】销售订单【{}】生成NF-e发票【{}】",UserContext.getDefaultLoginUser().getUserName(),soB2cEntity.getCode(),invoiceInfoEntity.getCode()), ModuleTypeEnum.SO_B2C.getCode(), soB2cEntity.getId(), "开票失败");
            }
        }catch (Exception e){
            log.error("创建发票失败,返回错误信息,返回信息:{}", e.getMessage());
            InvoiceInfoEntity entity = invoiceInfoService.getInvoicingBySoId(soB2cEntity.getId());
            entity.setStatus(InvoiceInfoStatusEnum.INVOICE_FAILED.getCode());
            entity.setRemark(e.getMessage());
            invoiceInfoService.updateNfeStatusById(entity);
            operateLogService.addModuleOperateLog(e.getMessage(), ModuleTypeEnum.INVOICE_INFO.getCode(), soB2cEntity.getId(),"开票失败");
        }
        log.warn("记录异步开票日志结束：{}",invoiceInfoEntity.getCode());
    }
}

