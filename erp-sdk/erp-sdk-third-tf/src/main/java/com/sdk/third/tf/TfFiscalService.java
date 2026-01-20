package com.sdk.third.tf;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.erp.model.oms.entity.CfgSettingEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.sdk.third.tf.client.TaxCategoryApiClient;
import com.sdk.third.tf.client.CompanyApiClient;
import com.sdk.third.tf.client.InvoiceApiClient;
import com.sdk.third.tf.constant.TfApiConstants;
import com.sdk.third.tf.dto.CompanyDetailResponseDTO;
import com.sdk.third.tf.dto.CompanyListResponseDTO;
import com.sdk.third.tf.dto.CreateCompanyDTO;
import com.sdk.third.tf.dto.CreateCompanyResponseDTO;
import com.sdk.third.tf.dto.CancelInvoiceDTO;
import com.sdk.third.tf.dto.CancelInvoiceResponseDTO;
import com.sdk.third.tf.dto.CreateInvoiceDTO;
import com.sdk.third.tf.dto.CreateInvoiceResponseDTO;
import com.sdk.third.tf.dto.EditCompanyDTO;
import com.sdk.third.tf.dto.GetDanfeDTO;
import com.sdk.third.tf.dto.GetDanfeResponseDTO;
import com.sdk.third.tf.dto.InvalidInvoiceDTO;
import com.sdk.third.tf.dto.InvalidInvoiceResponseDTO;
import com.sdk.third.tf.dto.InvoiceDetailResponseDTO;
import com.sdk.third.tf.dto.ReturnInvoiceDTO;
import com.sdk.third.tf.dto.ReturnInvoiceResponseDTO;
import com.sdk.third.tf.dto.NfeInvoiceDTO;
import com.sdk.third.tf.dto.TaxCategoryDTO;
import com.sdk.third.tf.entity.AddCompanyDTO;
import com.sdk.third.tf.entity.CompanyDTO;
import com.sdk.third.tf.entity.CompanyInfoEntity;
import com.sdk.third.tf.entity.UpdateCompanyDTO;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class TfFiscalService {

    @Autowired
    private TaxCategoryApiClient taxCategoryApiClient;

    @Autowired
    private CompanyApiClient companyApiClient;

    @Autowired
    private InvoiceApiClient invoiceApiClient;

    public final static String ACCESS_TOKEN = "19-04-2023_10-47-No37tBi0Yw39Fida4MdUYwmXdksxdY1sIjkmt4-Ymwx04dy1iu94m0b";

    @Deprecated
    public final static String URL = TfApiConstants.BASE_URL;

    public static void main(String[] args) {
        String path = "/cadastrar_empresa";
//        Map<String, String> bodyMap = new HashMap<>();
//        bodyMap.put("tipo_pesquisa","cnpj");
//        bodyMap.put("search","9661144118088");
//        bodyMap.put("token_plataforma","16-04-2025_09-19-No28tBi0Yw28FixdY1sIjkmt4-dksxdY1sIjkB12n413207N4-sb7XiNa");
        CompanyDTO updateCompanyDTO = new CompanyDTO();
        updateCompanyDTO.setTokenPlataforma("16-04-2025_09-19-No28tBi0Yw28FixdY1sIjkmt4-dksxdY1sIjkB12n413207N4-sb7XiNa");
        updateCompanyDTO.setCnpj("966114411480881");
        updateCompanyDTO.setIe("000000000");
        updateCompanyDTO.setRazaoSocial("WJKJ");
        updateCompanyDTO.setUltimoNumeroNfe("test");
        updateCompanyDTO.setTelefone("12345678");
        updateCompanyDTO.setIsActive(1);
        updateCompanyDTO.setNumero("test");
        updateCompanyDTO.setCity("São Paulo");
        updateCompanyDTO.setBairro("test");
        updateCompanyDTO.setCertificadoViaLink(true);
        updateCompanyDTO.setSenhaCertificado("1241231");
        updateCompanyDTO.setZipCode("03936-020");
        updateCompanyDTO.setCertificado("https://erptest.ulanzi.cn:9002/group1/M00/AE/F3/rBBkDGf_i-qAZzqVAAAPxveKY34854.pfx");
        updateCompanyDTO.setCep("test");
        updateCompanyDTO.setPassword("WJKJ");
        updateCompanyDTO.setName("WJKJ");
        updateCompanyDTO.setRazaoSocial("test12345");
        updateCompanyDTO.setState("São Paulo");
        updateCompanyDTO.setApiCompleta(true);
        updateCompanyDTO.setFirstName("WJKJTEST123456");
        updateCompanyDTO.setEmail("ulanzichat@ulanzi.cn");
        updateCompanyDTO.setUsername("WJKJTEST123456");
        updateCompanyDTO.setRua("test");
        updateCompanyDTO.setNaturezaId("10");
        updateCompanyDTO.setSurname("1412321");
        updateCompanyDTO.setLastName("1231");
        updateCompanyDTO.setLandmark("1231");
        updateCompanyDTO.setAmbiente("1231");
        Map<String, String> headerMap = new HashMap<>();

        //拉取数据
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(URL+path, JSONUtil.toJsonStr(updateCompanyDTO), null, headerMap, RequestMethod.POST);
        log.error("请求结果,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("请求失败,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
            throw new RuntimeException("请求失败,code:" + apiResult.getCode() + ",msg:" + apiResult.getMsg()+ ",data:" + apiResult.getData());
        }
        CompanyInfoEntity companyInfoEntity = JSON.parseObject(apiResult.getData(),new TypeReference<CompanyInfoEntity>() {}.getType());
        System.out.println(companyInfoEntity);
    }
    /**
     * 创建公司（旧接口）
     * @deprecated 请使用 {@link #createCompanyV2(CreateCompanyDTO)} 替代
     * @param addCompanyDTO
     * @return 成功返回公司token 失败抛出异常
     */
    @Deprecated
    public String createCompany(AddCompanyDTO addCompanyDTO){
        String path = "/cadastrar_empresa";
        String accessToken = getAccessToken();
        addCompanyDTO.setTokenPlataforma(accessToken);
        buildDefaultCompany(addCompanyDTO);
        log.error("创建公司,{}", JSONUtil.toJsonStr(addCompanyDTO));
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(URL+path, JSONUtil.toJsonStr(addCompanyDTO), null, new HashMap<>(), RequestMethod.POST);
        log.error("请求结果,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("请求失败,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
            throw new RuntimeException("请求失败,code:" + apiResult.getCode() + ",msg:" + apiResult.getMsg()+ ",data:" + apiResult.getData());
        }
        CompanyInfoEntity result = JSON.parseObject(apiResult.getData().toString(),new TypeReference<CompanyInfoEntity>() {}.getType());
        if(Objects.isNull(result.getId())){
            log.error("创建公司失败,{}", apiResult.getData());
            throw new ServiceException("创建公司失败:"+ apiResult.getData());
        }
        CompanyInfoEntity companyInfoEntity = this.queryCompany(addCompanyDTO.getCnpj());
        return companyInfoEntity.getTokenEmpresa();
    }

    /**
     * 创建公司（新接口）
     * 使用新接口路径：/company/create
     * 注意：新接口需要在Header中传递sign、timestamp、token（经销商token/b2b_token）
     * 创建公司和获取公司列表接口使用经销商token（TF-ACCESS_TOKEN），其他接口使用公司token（cfg_invoice_setting.token）
     * 
     * @param createCompanyDTO 创建公司DTO
     * @return 创建公司响应数据DTO（包含companyId和token）
     */
    public CreateCompanyResponseDTO.CreateCompanyDataDTO createCompanyV2(CreateCompanyDTO createCompanyDTO) {
        // 获取经销商token（b2b_token）= TF-ACCESS_TOKEN（平台token）
        // 注意：创建公司和获取公司列表接口使用经销商token
        // 签名的appKey就是header的token，所以这里appKey = b2bToken
        String b2bToken = getAccessToken();
        
        log.info("创建公司（新接口）, cnpj: {}, 使用经销商token", createCompanyDTO.getCnpj());
        
        CreateCompanyResponseDTO.CreateCompanyDataDTO response = companyApiClient.createCompany(createCompanyDTO, b2bToken, b2bToken);
        
        log.info("创建公司成功, companyId: {}, token: {}", response.getCompanyId(), response.getToken());
        return response;
    }

    /**
     * 编辑公司（新接口）
     * 使用新接口路径：/api/company/edit
     * 注意：编辑公司接口使用公司token（cfg_invoice_setting.token），不是经销商token
     * 
     * @param editCompanyDTO 编辑公司DTO
     * @param companyToken 公司token（cfg_invoice_setting.token）
     */
    public void editCompanyV2(EditCompanyDTO editCompanyDTO, String companyToken) {
        // 签名的appKey就是header的token，所以这里appKey = companyToken
        
        log.info("编辑公司（新接口）, cnpj: {}, 使用公司token", editCompanyDTO.getCnpj());
        
        companyApiClient.editCompany(editCompanyDTO, companyToken, companyToken);
        
        log.info("编辑公司成功, cnpj: {}", editCompanyDTO.getCnpj());
    }

    /**
     * 查询公司详情（新接口）
     * 使用新接口路径：/company/get_detail
     * 注意：查询公司详情接口使用公司token（cfg_invoice_setting.token），不是经销商token
     * 
     * @param cnpj CNPJ
     * @param companyToken 公司token（cfg_invoice_setting.token）
     * @return 公司详情数据DTO（data部分）
     */
    public CompanyDetailResponseDTO.CompanyDetailDataDTO getCompanyDetailV2(String cnpj, String companyToken) throws UnsupportedEncodingException {
        // 签名的appKey就是header的token，所以这里appKey = companyToken
        
        log.info("查询公司详情（新接口）, cnpj: {}, 使用公司token", cnpj);
        
        CompanyDetailResponseDTO.CompanyDetailDataDTO response = 
            companyApiClient.getCompanyDetail(cnpj, companyToken, companyToken);
        
        log.info("查询公司详情成功, cnpj: {}, companyId: {}", cnpj, response.getCompanyId());
        return response;
    }

    /**
     * 获取公司列表（新接口）
     * 使用新接口路径：/api/company/get_list
     * 注意：获取公司列表接口使用经销商token（TF-ACCESS_TOKEN），不是公司token
     * 
     * @param page 页码（必填）
     * @param pageSize 页大小（必填，最大20）
     * @return 公司列表数据DTO（data部分）
     */
    public CompanyListResponseDTO.CompanyListDataDTO getCompanyListV2(Integer page, Integer pageSize) {
        // 获取经销商token（b2b_token）= TF-ACCESS_TOKEN（平台token）
        // 签名的appKey就是header的token，所以这里appKey = b2bToken
        String b2bToken = getAccessToken();
        
        log.info("获取公司列表（新接口）, page: {}, pageSize: {}, 使用经销商token", page, pageSize);
        
        CompanyListResponseDTO.CompanyListDataDTO response = 
            companyApiClient.getCompanyList(page, pageSize, b2bToken, b2bToken);
        
        log.info("获取公司列表成功, total: {}, totalPages: {}", response.getTotal(), response.getTotalPages());
        return response;
    }
    
    /**
     * 获取AppKey（用于签名）
     * 
     * @return AppKey
     */
    private String getAppKey() {
        List<DictBasicEntity> dictBasicEntityList = FeignQuery.create(DictBasicEntity.class)
            .eq(DictBasicEntity::getType, TfApiConstants.DictType.TF_APP_KEY).list();
        if (CollectionUtils.isEmpty(dictBasicEntityList)) {
            throw new ServiceException("没有找到TF-APP-KEY的字典数据");
        }
        return dictBasicEntityList.get(0).getValue();
    }

    /**
     * 查询公司信息
     * @param cnpj
     */
    public CompanyInfoEntity queryCompany(String cnpj){
        String path = "/consultar_empresa";
        String accessToken = getAccessToken();
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("tipo_pesquisa","cnpj");
        bodyMap.put("search",cnpj);
        bodyMap.put("token_plataforma",accessToken);
        Map<String, String> headerMap = new HashMap<>();

        log.error("查询公司信息,{}", JSONUtil.toJsonStr(bodyMap));
        //拉取数据
        ApiResult<String> apiResult = HttpCommonUtil.sendOkHttpApiResult(URL+path, JSONUtil.toJsonStr(bodyMap), null, headerMap, RequestMethod.POST);
        log.error("请求结果,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("请求失败,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
            throw new RuntimeException("请求失败,code:" + apiResult.getCode() + ",msg:" + apiResult.getMsg()+ ",data:" + apiResult.getData());
        }
        CompanyInfoEntity companyInfoEntity = JSON.parseObject(apiResult.getData(),new TypeReference<CompanyInfoEntity>() {}.getType());
        if(Objects.isNull(companyInfoEntity.getTokenEmpresa())){
            log.error("查询公司失败,{}", apiResult.getData());
            throw new ServiceException("查询公司失败:"+ apiResult.getData());
        }
        return companyInfoEntity;
    }

    /**
     * 修改公司信息
     * @param updateCompanyDTO
     * @return
     */
    public void updateCompany(UpdateCompanyDTO updateCompanyDTO){
        String path = "/alterar_empresa";
//        String accessToken = getAccessToken();
//        updateCompanyDTO.setTokenPlataforma(accessToken);
        buildDefaultCompany(updateCompanyDTO);
        updateCompanyDTO.setApiCompleta(null);
        log.error("更新公司信息,{}", JSONUtil.toJsonStr(updateCompanyDTO));
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(URL+path, JSONUtil.toJsonStr(updateCompanyDTO), null, new HashMap<>(), RequestMethod.POST);
        log.error("请求结果,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("请求失败,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
            throw new RuntimeException("请求失败,code:" + apiResult.getCode() + ",msg:" + apiResult.getMsg()+ ",data:" + apiResult.getData());
        }
        if(!apiResult.getData().toString().contains("\"success\":1")){
            log.error("更新公司失败,{}", apiResult.getData().toString());
            throw new ServiceException("更新公司失败:"+apiResult.getData().toString());
        }
    }

    /**
     * 删除公司信息
     * @return
     */
    public void deleteCompany(String cnpj){
        if(StringUtils.isBlank(cnpj)){
            throw new ServiceException("删除公司失败,cnpj不能为空");
        }
        String path = "/desativar_empresa";
        String accessToken = getAccessToken();
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("cnpj",cnpj);
        paramMap.put("token_plataforma",accessToken);
        log.error("删除公司,{}", JSONUtil.toJsonStr(paramMap));
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(URL+path, JSONUtil.toJsonStr(paramMap), null, new HashMap<>(), RequestMethod.POST);
        log.error("请求结果,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("请求失败,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
            throw new RuntimeException("请求失败,code:" + apiResult.getCode() + ",msg:" + apiResult.getMsg()+ ",data:" + apiResult.getData());
        }
        if(!apiResult.getData().toString().contains("desativado")){
            log.error("删除公司失败,{}", apiResult.getData().toString());
            throw new ServiceException("删除公司失败:"+apiResult.getData().toString());
        }
    }


    /**
     * 生成发票（旧接口，已废弃）
     * @deprecated 请使用 createInvoiceV2 方法
     * @author will
     * @date 2025/4/11 12:12
     * @param nfeCreateDTO
     * @return Object
     */
    @Deprecated
    public Object createInvoice(NfeInvoiceDTO.NfeCreateDTO nfeCreateDTO){
        String path = "/emitir_transparente";
        String body = JSONUtil.toJsonStr(nfeCreateDTO);
        return doPostUrl(URL + path, body);
    }

    /**
     * 开具发票（新接口）
     * 使用新接口路径：/api/invoice/create
     * 注意：开具发票接口使用公司token（cfg_invoice_setting.token），不是经销商token
     * 
     * @param createInvoiceDTO 开具发票DTO
     * @param companyToken 公司token（cfg_invoice_setting.token）
     * @return 开具发票响应数据DTO（data部分）
     */
    public CreateInvoiceResponseDTO.CreateInvoiceDataDTO createInvoiceV2(
            CreateInvoiceDTO createInvoiceDTO, String companyToken) {
        // 签名的appKey就是header的token，所以这里appKey = companyToken
        
        log.info("开具发票（新接口）, id: {}, 使用公司token", createInvoiceDTO.getId());
        
        CreateInvoiceResponseDTO.CreateInvoiceDataDTO response = 
            invoiceApiClient.createInvoice(createInvoiceDTO, companyToken, companyToken);
        
        log.info("开具发票成功, uuid: {}, status: {}", response.getUuid(), response.getStatus());
        return response;
    }

    /**
     * 查询发票详情
     * 使用新接口路径：/api/invoice/get_detail
     * 
     * @param uuid 发票UUID
     * @param companyToken 公司token（cfg_invoice_setting.token）
     * @return 发票详情响应数据DTO（data部分）
     */
    public InvoiceDetailResponseDTO.InvoiceDetailDataDTO getInvoiceDetailV2(String uuid, String companyToken) {
        // 签名的appKey就是header的token，所以这里appKey = companyToken
        
        log.info("查询发票详情, uuid: {}, 使用公司token", uuid);
        
        InvoiceDetailResponseDTO.InvoiceDetailDataDTO response = 
            invoiceApiClient.getInvoiceDetail(uuid, companyToken, companyToken);
        
        log.info("查询发票详情成功, uuid: {}, status: {}", uuid, response.getStatus());
        return response;
    }

    /**
     * 取消发票（新接口）
     * 使用新接口路径：/api/invoice/cancel
     * 
     * @param cancelInvoiceDTO 取消发票DTO
     * @param companyToken 公司token（cfg_invoice_setting.token）
     * @return 取消发票响应数据DTO（data部分）
     */
    public CancelInvoiceResponseDTO.CancelInvoiceDataDTO cancelInvoiceV2(
            CancelInvoiceDTO cancelInvoiceDTO, String companyToken) {
        // 签名的appKey就是header的token，所以这里appKey = companyToken
        
        log.info("取消发票（新接口）, uuid: {}, 使用公司token", cancelInvoiceDTO.getUuid());
        
        CancelInvoiceResponseDTO.CancelInvoiceDataDTO response = 
            invoiceApiClient.cancelInvoice(cancelInvoiceDTO, companyToken, companyToken);
        
        log.info("取消发票成功, uuid: {}, status: {}", cancelInvoiceDTO.getUuid(), response.getStatus());
        return response;
    }

    /**
     * 退货发票（新接口）
     * 使用新接口路径：/api/invoice/return
     * 
     * @param returnInvoiceDTO 退货发票DTO
     * @param companyToken 公司token（cfg_invoice_setting.token）
     * @return 退货发票响应数据DTO（data部分）
     */
    public ReturnInvoiceResponseDTO.ReturnInvoiceDataDTO returnInvoiceV2(
            ReturnInvoiceDTO returnInvoiceDTO, String companyToken) {
        // 签名的appKey就是header的token，所以这里appKey = companyToken
        
        log.info("退货发票（新接口）, uuid/chave: {}, 使用公司token", returnInvoiceDTO.getUuidOrChave());
        
        ReturnInvoiceResponseDTO.ReturnInvoiceDataDTO response = 
            invoiceApiClient.returnInvoice(returnInvoiceDTO, companyToken, companyToken);
        
        log.info("退货发票成功, uuid: {}, status: {}", returnInvoiceDTO.getUuidOrChave(), response.getStatus());
        return response;
    }

    /**
     * 作废发票（新接口）
     * 使用新接口路径：/api/invoice/invalid
     * 作废发票一般使用场景为发票号跳号时使用
     * 
     * @param invalidInvoiceDTO 作废发票DTO
     * @param companyToken 公司token（cfg_invoice_setting.token）
     * @return 作废发票响应数据DTO（data部分）
     */
    public InvalidInvoiceResponseDTO.InvalidInvoiceDataDTO invalidInvoiceV2(
            InvalidInvoiceDTO invalidInvoiceDTO, String companyToken) {
        // 签名的appKey就是header的token，所以这里appKey = companyToken
        
        log.info("作废发票（新接口）, serie: {}, startNumber: {}, endNumber: {}, 使用公司token", 
            invalidInvoiceDTO.getSerie(), invalidInvoiceDTO.getStartNumber(), invalidInvoiceDTO.getEndNumber());
        
        InvalidInvoiceResponseDTO.InvalidInvoiceDataDTO response = 
            invoiceApiClient.invalidInvoice(invalidInvoiceDTO, companyToken, companyToken);
        
        log.info("作废发票成功, uuid: {}", response.getUuid());
        return response;
    }

    /**
     * 获取发票Danfe（新接口）
     * 使用新接口路径：/api/invoice/get_danfe
     * Danfe URL链接就是PDF文件
     * 
     * @param getDanfeDTO 获取Danfe DTO
     * @param companyToken 公司token（cfg_invoice_setting.token）
     * @return 获取Danfe响应数据DTO（data部分）
     */
    public GetDanfeResponseDTO.GetDanfeDataDTO getDanfeV2(
            GetDanfeDTO getDanfeDTO, String companyToken) {
        // 签名的appKey就是header的token，所以这里appKey = companyToken
        
        log.info("获取发票Danfe（新接口）, uuid: {}, 使用公司token", getDanfeDTO.getUuid());
        
        GetDanfeResponseDTO.GetDanfeDataDTO response = 
            invoiceApiClient.getDanfe(getDanfeDTO, companyToken, companyToken);
        
        log.info("获取发票Danfe成功, uuid: {}, danfe: {}", getDanfeDTO.getUuid(), response.getDanfe());
        return response;
    }


    /**
     * 取消发票
     * @author will
     * @date 2025/4/11 12:12
     * @param nfeCancelDTO
     * @return Object
     */
    public Object cancelInvoice(NfeInvoiceDTO.NfeCancelDTO nfeCancelDTO){
        String path = "/cancelar_nota";
        String body = JSONUtil.toJsonStr(nfeCancelDTO);
        return doPostUrl(URL + path, body);
    }
    /**
     * 退票接口
     * @author zdy
     * @date 2025/7/9 12:12
     * @param nfeReturnDTO
     * @return Object
     */
    public Object returnInvoice(NfeInvoiceDTO.NfeReturnDTO nfeReturnDTO){
        String path = "/registrar_insucesso_entrega";
        String body = JSONUtil.toJsonStr(nfeReturnDTO);
        return doPostUrl(URL + path, body);
    }

    /**
     * 作废接口
     * @author zdy
     * @date 2025/7/9 12:12
     * @param nfeVoidedDTO
     * @return Object
     */
    public Object voidedInvoice(NfeInvoiceDTO.NfeVoidedDTO nfeVoidedDTO){
        String path = "/inutilizar_nfe";
        String body = JSONUtil.toJsonStr(nfeVoidedDTO);
        return doPostUrl(URL + path, body);
    }



    /**
     * 更新cce信息
     * @author will
     * @date 2025/4/15 11:41
     * @param nfeCceDTO
     * @return Object
     */
    public Object updateCceInvoice(NfeInvoiceDTO.NfeCceDTO nfeCceDTO){
        String path = "/corrigirCce_api";
        String body = JSONUtil.toJsonStr(nfeCceDTO);
        return doPostUrl(URL + path, body);
    }

    // ==================== 税种相关接口 ====================

    /**
     * 创建税种
     * 注意：税种接口使用公司token（cfg_invoice_setting.token），不是经销商token
     * 
     * @param createDTO 创建税种DTO
     * @param companyToken 公司token（cfg_invoice_setting.token）
     * @return 税种ID
     */
    public String createTaxCategory(TaxCategoryDTO.CreateCategoryDTO createDTO, String companyToken) {
        // 签名的appKey就是header的token，所以这里appKey = companyToken
        
        log.info("创建税种, descricao: {}, 使用公司token", createDTO.getDescricao());
        TaxCategoryDTO.CreateCategoryResponseDTO response = 
            taxCategoryApiClient.createCategory(createDTO, companyToken, companyToken);
        if (response == null || StringUtils.isBlank(response.getCategoryId())) {
            throw new ServiceException("创建税种失败，未返回税种ID");
        }
        log.info("创建税种成功, categoryId: {}", response.getCategoryId());
        return response.getCategoryId();
    }

    /**
     * 查询税种列表
     * 注意：税种接口使用公司token（cfg_invoice_setting.token），不是经销商token
     * 
     * @param page 页码
     * @param pageSize 每页大小
     * @param companyToken 公司token（cfg_invoice_setting.token）
     * @return 税种列表响应
     */
    public TaxCategoryDTO.CategoryListResponseDTO getTaxCategoryList(
            Integer page, Integer pageSize, String companyToken) {
        // 签名的appKey就是header的token，所以这里appKey = companyToken
        
        log.info("查询税种列表, page: {}, pageSize: {}, 使用公司token", page, pageSize);
        return taxCategoryApiClient.getCategoryList(page, pageSize, companyToken, companyToken);
    }

    /**
     * 查询税种详情
     * 注意：税种接口使用公司token（cfg_invoice_setting.token），不是经销商token
     * 
     * @param categoryId 税种ID
     * @param companyToken 公司token（cfg_invoice_setting.token）
     * @return 税种详情
     */
    public TaxCategoryDTO.CategoryDetailDTO getTaxCategoryDetail(String categoryId, String companyToken) throws UnsupportedEncodingException {
        if (StringUtils.isBlank(categoryId)) {
            throw new ServiceException("税种ID不能为空");
        }
        // 签名的appKey就是header的token，所以这里appKey = companyToken
        
        log.info("查询税种详情, categoryId: {}, 使用公司token", categoryId);
        return taxCategoryApiClient.getCategoryDetail(categoryId, companyToken, companyToken);
    }

    /**
     * 编辑税种
     * 注意：税种接口使用公司token（cfg_invoice_setting.token），不是经销商token
     * 
     * @param editDTO 编辑税种DTO
     * @param companyToken 公司token（cfg_invoice_setting.token）
     * @return 税种ID
     */
    public String editTaxCategory(TaxCategoryDTO.EditCategoryDTO editDTO, String companyToken) {
        if (StringUtils.isBlank(editDTO.getCategoryId())) {
            throw new ServiceException("税种ID不能为空");
        }
        // 签名的appKey就是header的token，所以这里appKey = companyToken
        
        log.info("编辑税种, categoryId: {}, 使用公司token", editDTO.getCategoryId());
        TaxCategoryDTO.EditCategoryResponseDTO response = 
            taxCategoryApiClient.editCategory(editDTO, companyToken, companyToken);
        if (response == null || StringUtils.isBlank(response.getCategoryId())) {
            throw new ServiceException("编辑税种失败，未返回税种ID");
        }
        log.info("编辑税种成功, categoryId: {}", response.getCategoryId());
        return response.getCategoryId();
    }

    /**
     * 删除税种
     * 注意：税种接口使用公司token（cfg_invoice_setting.token），不是经销商token
     * 
     * @param categoryId 税种ID
     * @param companyToken 公司token（cfg_invoice_setting.token）
     */
    public void deleteTaxCategory(String categoryId, String companyToken) {
        if (StringUtils.isBlank(categoryId)) {
            throw new ServiceException("税种ID不能为空");
        }
        // 签名的appKey就是header的token，所以这里appKey = companyToken
        
        log.info("删除税种, categoryId: {}, 使用公司token", categoryId);
        taxCategoryApiClient.deleteCategory(categoryId, companyToken, companyToken);
        log.info("删除税种成功, categoryId: {}", categoryId);
    }

    private String getAccessToken() {
        List<DictBasicEntity> dictBasicEntityList = FeignQuery.create(DictBasicEntity.class)
            .eq(DictBasicEntity::getType, TfApiConstants.DictType.TF_ACCESS_TOKEN).list();
        if(CollectionUtils.isEmpty(dictBasicEntityList)){
            throw new ServiceException("没有找到TF-ACCESS_TOKEN的字典数据");
        }
        return dictBasicEntityList.get(0).getValue();
    }

    private void buildDefaultCompany(CompanyDTO companyDTO) {
        List<CfgSettingEntity> cfgSettingEntityList = FeignQuery.create(CfgSettingEntity.class).eq(CfgSettingEntity::getKey,"tfCompany").list();
        if(CollectionUtils.isEmpty(cfgSettingEntityList)){
            throw new ServiceException("没有找到配置数据");
        }
        CfgSettingEntity cfgSettingEntity = cfgSettingEntityList.get(0);
        String jsonStr = cfgSettingEntity.getValue();
        JSONObject jsonObject = JSONUtil.parseObj(jsonStr);
        companyDTO.setCity(jsonObject.getStr("city"));
        companyDTO.setCertificadoViaLink(jsonObject.get("certificado_via_link",Boolean.class));
        companyDTO.setCertificado(jsonObject.getStr("certificado"));
        companyDTO.setZipCode(jsonObject.getStr("zip_code"));
        companyDTO.setPassword(jsonObject.getStr("password"));
        companyDTO.setName(jsonObject.getStr("name"));
        companyDTO.setState(jsonObject.getStr("state"));
        companyDTO.setApiCompleta(jsonObject.get("api_completa",Boolean.class));
        companyDTO.setFirstName(jsonObject.getStr("first_name"));
        companyDTO.setEmail(jsonObject.getStr("email"));
        companyDTO.setNaturezaId(jsonObject.getStr("natureza_id"));
        companyDTO.setSurname(companyDTO.getName());
        companyDTO.setLastName(companyDTO.getName());
        companyDTO.setLandmark(jsonObject.getStr("landmark"));
        companyDTO.setAmbiente(jsonObject.getStr("ambiente"));
    }

    /**
     * jdk原生数据请求
     * @author will
     * @date 2025/4/22 19:14
     * @param postUrl
     * @param jsonStr
     * @return String
     */
    private static String doPostUrl (String postUrl,String jsonStr) {
        // 读取响应体（成功或错误）
        String responseBody = null;
        try {
            java.net.URL url = new URL(postUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonStr.getBytes("UTF-8"));
            }
            // 获取响应码
            int status = conn.getResponseCode();
            System.out.println("Response Code: " + status);

            // 读取响应体（成功或错误）
            if (status >= 200 && status < 300) {
                responseBody = readStream(conn.getInputStream());
            } else {
                String error = readStream(conn.getErrorStream());
                throw new ServiceException(error);
            }
            System.out.println("Response Body: " + responseBody);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
        return responseBody;
    }

    /**
     * 将 InputStream 转换为字符串
     * @author will
     * @date 2025/4/22 19:14
     * @param inputStream
     * @return String
     */
    private static String readStream(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (Exception e) {
          throw new ServiceException(CharSequenceUtil.format("流转字符串失败，原因：{}",e.getMessage()));
        }
    }
}
