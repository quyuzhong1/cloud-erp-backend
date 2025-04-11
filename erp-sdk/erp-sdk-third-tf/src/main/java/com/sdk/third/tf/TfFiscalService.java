package com.sdk.third.tf;

import cn.hutool.json.JSONUtil;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.erp.model.oms.entity.DictBasicEntity;
import com.sdk.third.tf.dto.NfeInvoiceDTO;
import com.sdk.third.tf.entity.CompanyDTO;
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

        Map<String, Object> paramMap = buildDefaultParam();
        paramMap.put("username","WJKJ");
        paramMap.put("password","WJKJ");
        paramMap.put("email","ulanzichat@ulanzi.cn");
        paramMap.put("name","WJKJ");
        paramMap.put("first_name","WJKJ");
        paramMap.put("city","São Paulo");
        paramMap.put("state","São Paulo");
        paramMap.put("zip_code","03936-020");
        paramMap.put("is_active",1);
        paramMap.put("razao_social","test123");
        paramMap.put("cnpj","40262014000106");
        paramMap.put("ie","133011581111");
        paramMap.put("senha_certificado","1234");
        paramMap.put("certificado","test4");
        paramMap.put("certificado_via_link",true);
        paramMap.put("rua","test");
        paramMap.put("numero","test");
        paramMap.put("bairro","test");
        paramMap.put("cep","test");
        paramMap.put("telefone","12345678");
        paramMap.put("ultimo_numero_nfe","test");
        paramMap.put("numero_serie_nfe","");
        Map<String, String> headerMap = new HashMap<>();
        //拉取数据
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(URL+path, JSONUtil.toJsonStr(paramMap), null, headerMap, RequestMethod.POST);
        log.error("请求结果,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("请求失败,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
            throw new RuntimeException("请求失败,code:" + apiResult.getCode() + ",msg:" + apiResult.getMsg()+ ",data:" + apiResult.getData());
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
     * @param createCompanyDTO
     * @return
     */
    public String createCompany(CompanyDTO createCompanyDTO){
        String path = "/cadastrar_empresa";
        String accessToken = getAccessToken();
        createCompanyDTO.setTokenPlataforma(accessToken);
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(URL+path, JSONUtil.toJsonStr(createCompanyDTO), null, new HashMap<>(), RequestMethod.POST);
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
    public String updateCompany(CompanyDTO updateCompanyDTO){
        String path = "/alterar_empresa";
        String accessToken = getAccessToken();
        updateCompanyDTO.setTokenPlataforma(accessToken);
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(URL+path, JSONUtil.toJsonStr(updateCompanyDTO), null, new HashMap<>(), RequestMethod.POST);
        log.error("请求结果,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("请求失败,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
            throw new RuntimeException("请求失败,code:" + apiResult.getCode() + ",msg:" + apiResult.getMsg()+ ",data:" + apiResult.getData());
        }
        return null;
    }

    /**
     * 删除公司信息
     * @return
     */
    public String deleteCompany(String cnpj){
        String path = "/alterar_empresa";
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
        return null;
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
        ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(URL+path, JSONUtil.toJsonStr(nfeCreateDTO), null, new HashMap<>(), RequestMethod.POST);
        log.error("请求结果,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("请求失败,code:{},msg:{},data:{}", apiResult.getCode(), apiResult.getMsg(),apiResult.getData());
            throw new RuntimeException("请求失败,code:" + apiResult.getCode() + ",msg:" + apiResult.getMsg()+ ",data:" + apiResult.getData());
        }
        return apiResult.getData();
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



    private String getAccessToken() {
        List<DictBasicEntity> dictBasicEntityList = FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType,"TF-ACCESS_TOKEN").list();
        if(CollectionUtils.isEmpty(dictBasicEntityList)){
            throw new ServiceException("没有找到TF-ACCESS_TOKEN的字典数据");
        }
        return dictBasicEntityList.get(0).getValue();
    }
}
