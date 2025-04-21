package com.sdk.third.tf;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.erp.model.oms.entity.CfgSettingEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.sdk.third.tf.dto.NfeInvoiceDTO;
import com.sdk.third.tf.entity.AddCompanyDTO;
import com.sdk.third.tf.entity.CompanyDTO;
import com.sdk.third.tf.entity.UpdateCompanyDTO;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class TfFiscalService {


    public final static String ACCESS_TOKEN = "19-04-2023_10-47-No37tBi0Yw39Fida4MdUYwmXdksxdY1sIjkmt4-Ymwx04dy1iu94m0b";

    public final static String URL = "https://tffiscal.com.br/api";

    public static void main(String[] args) {
        String path = "/cadastrar_empresa";
        CompanyDTO updateCompanyDTO = new CompanyDTO();
        updateCompanyDTO.setTokenPlataforma("16-04-2025_09-19-No28tBi0Yw28FixdY1sIjkmt4-dksxdY1sIjkB12n413207N4-sb7XiNa");
        updateCompanyDTO.setCnpj("96611441148088");
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
        updateCompanyDTO.setFirstName("WJKJTEST22131");
        updateCompanyDTO.setEmail("ulanzichat@ulanzi.cn");
        updateCompanyDTO.setUsername("WJKJTEST22131");
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
        if(!apiResult.getData().contains("\"success\":1")){
            throw new ServiceException("adsa");
        }
    }

    private static Map<String, Object> buildDefaultParam() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("token_plataforma",ACCESS_TOKEN);
        paramMap.put("api_completa",true);
        return paramMap;
    }

    /**
     * 创建公司
     * @param addCompanyDTO
     * @return 成功返回公司token 失败抛出异常
     */
    public String createCompany(AddCompanyDTO addCompanyDTO){
        String path = "/cadastrar_empresa";
        String accessToken = getAccessToken();
        addCompanyDTO.setTokenPlataforma(accessToken);
        buildDefaultCompany(addCompanyDTO);
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(URL+path, JSONUtil.toJsonStr(addCompanyDTO), null, new HashMap<>(), RequestMethod.POST);
        log.error("请求结果,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("请求失败,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
            throw new RuntimeException("请求失败,code:" + apiResult.getCode() + ",msg:" + apiResult.getMsg()+ ",data:" + apiResult.getData());
        }
        return null;
    }

    /**
     * 修改公司信息
     * @param updateCompanyDTO
     * @return
     */
    public void updateCompany(UpdateCompanyDTO updateCompanyDTO){
        String path = "/alterar_empresa";
        String accessToken = getAccessToken();
        updateCompanyDTO.setTokenPlataforma(accessToken);
        buildDefaultCompany(updateCompanyDTO);
        updateCompanyDTO.setApiCompleta(null);
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
     * 生成发票
     * @author will
     * @date 2025/4/11 12:12
     * @param nfeCreateDTO
     * @return Object
     */
    public Object createInvoice(NfeInvoiceDTO.NfeCreateDTO nfeCreateDTO){
        String path = "/emitir_transparente";
        String accessToken = getAccessToken();
        nfeCreateDTO.setTokenEmpresa(accessToken);

        HttpRequest createPost = HttpUtil.createPost(URL+path);
        String body = JSONUtil.toJsonStr(nfeCreateDTO);
        createPost.body(body);
        // 创建明确包含 Content-Type 的请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type","application/json");
        headers.put("Content-Length", String.valueOf(body.length()));
        headers.put("Host", "tffiscal.com.br");
        createPost.addHeaders(headers);
        HttpResponse response = createPost.execute();
        log.warn("请求参数-body:{},响应结果-response:{}", body, response.body());
        if (200 != response.getStatus() ) {
            throw new ServiceException(ApiError.ERROR_INVOICE_NFE_CREATE_INVOICE,response.body());
        }
        return response.body();
    }


    /**
     * 取消发票
     * @author will
     * @date 2025/4/11 12:12
     * @param nfeCreateDTO
     * @return Object
     */
    public Object cancelInvoice(NfeInvoiceDTO.NfeCancelDTO nfeCreateDTO){
        String path = "/cancelar_nota";
        String accessToken = getAccessToken();
        nfeCreateDTO.setTokenEmpresa(accessToken);
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(URL+path, JSONUtil.toJsonStr(nfeCreateDTO), null, new HashMap<>(), RequestMethod.POST);
        log.error("请求结果,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("请求失败,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
            throw new RuntimeException("请求失败,code:" + apiResult.getCode() + ",msg:" + apiResult.getMsg()+ ",data:" + apiResult.getData());
        }
        return apiResult.getData();
    }

    /**
     * 查询发票
     * @author will
     * @date 2025/4/14 16:15
     * @param nfeListParamDTO
     * @return Object
     */
    public Object getNfeInvoiceResult(NfeInvoiceDTO.NfeListParamDTO nfeListParamDTO){
        String path = "/consultar_nota";
        String accessToken = getAccessToken();
        nfeListParamDTO.setTokenEmpresa(accessToken);
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(URL+path, JSONUtil.toJsonStr(nfeListParamDTO), null, new HashMap<>(), RequestMethod.POST);
        log.error("请求结果,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("请求失败,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
            throw new RuntimeException("请求失败,code:" + apiResult.getCode() + ",msg:" + apiResult.getMsg()+ ",data:" + apiResult.getData());
        }
        return apiResult.getData();
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
        String accessToken = getAccessToken();
        nfeCceDTO.setTokenEmpresa(accessToken);
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(URL+path, JSONUtil.toJsonStr(nfeCceDTO), null, new HashMap<>(), RequestMethod.POST);
        log.error("请求结果,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("请求失败,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
            throw new RuntimeException("请求失败,code:" + apiResult.getCode() + ",msg:" + apiResult.getMsg()+ ",data:" + apiResult.getData());
        }
        return apiResult.getData();
    }

    private String getAccessToken() {
        List<DictBasicEntity> dictBasicEntityList = FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType,"TF-ACCESS_TOKEN").list();
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
}
