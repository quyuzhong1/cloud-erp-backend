package com.sdk.oms.mercadolocal.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.JobTaskDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.RedisUtil;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.HttpCommonUtil;
import com.common.core.utils.OkHttpUtils;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.dto.ShopDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.sdk.oms.mercadolocal.constant.MercadoConstant;
import com.sdk.oms.mercadolocal.dto.MercadoInvoiceDTO;
import com.sdk.oms.mercadolocal.dto.MercadoShipOrderDTO;
import com.sdk.oms.mercadolocal.dto.MercadoShopInfoDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.PlatformMercadoRefreshTokenDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.PlatformMercadoTokenDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.cost.CostDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.listing.ListingDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.listing.ListingViewDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.order.OrderDataDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.order.OrderViewDTO;
import com.sdk.oms.mercadolocal.dto.mercadolocal.shipment.ShipmentViewDTO;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.thymeleaf.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 美客多平台SDK
 *
 * @Author Luo_WG
 * @Date 2024/2/27 18:04
 **/
@Slf4j
@Component
public class MercadoLocalSdkClientService {

//    public static void main(String[] args) {
//
//        String accessToken = "APP_USR-5344160433223219-041005-6c3718c50d01369fb13ca5666d1ef4d5-2119968271";
//        //组装授权url
//        String url = MercadoConstant.URL;
//        String path = "/orders/2000011159453786";
//        StringBuffer sb = new StringBuffer();
//        sb.append(url);
//        sb.append(path);
//        //入参
//        //设置请求头
//        Map<String, String> headerMap = new HashMap<>(1);
//        headerMap.put("Authorization", "Bearer " +accessToken);
////        headerMap.put("Content-Type", "application/xml");
//
//        //拉取数据
//        ApiResult apiResult = new ApiResult();
//        apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), "", null, headerMap, RequestMethod.GET);
//        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
//            System.out.println(apiResult.getData());
//        }
//
//    }
//    public static void main(String[] args) {
//
//        String accessToken = "APP_USR-5344160433223219-041005-6c3718c50d01369fb13ca5666d1ef4d5-2119968271";
//        //组装授权url
//        String url = MercadoConstant.URL;
//        String path = "/shipments/{id}/invoice_data/?siteId=MLB".replace("{id}","44665639687");
//        StringBuffer sb = new StringBuffer();
//        sb.append(url);
//        sb.append(path);
//        //入参
//        String param = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><nfeProc versao=\"4.00\" xmlns=\"http://www.portalfiscal.inf.br/nfe\"><NFe xmlns=\"http://www.portalfiscal.inf.br/nfe\"><infNFe Id=\"NFe35250352440061000173550070000001001151091188\" versao=\"4.00\"><ide><cUF>35</cUF><cNF>15109118</cNF><natOp>Venda de mercadorias</natOp><mod>55</mod><serie>7</serie><nNF>100</nNF><dhEmi>2025-03-28T23:28:46-03:00</dhEmi><dhSaiEnt>2025-03-28T23:28:46-03:00</dhSaiEnt><tpNF>1</tpNF><idDest>1</idDest><cMunFG>3550308</cMunFG><tpImp>1</tpImp><tpEmis>1</tpEmis><cDV>8</cDV><tpAmb>1</tpAmb><finNFe>1</finNFe><indFinal>1</indFinal><indPres>2</indPres><indIntermed>1</indIntermed><procEmi>0</procEmi><verProc>4.00 | UpSeller ERP</verProc></ide><emit><CNPJ>52440061000173</CNPJ><xNome>ROBO INTELIGENTE COMERCIAL LTDA</xNome><enderEmit><xLgr>ESTADO DE PERNAMBUCO</xLgr><nro>967</nro><xBairro>JARDIM EGLE</xBairro><cMun>3550308</cMun><xMun>São Paulo</xMun><UF>SP</UF><CEP>03936020</CEP><cPais>1058</cPais><xPais>Brasil</xPais></enderEmit><IE>125855854111</IE><CRT>1</CRT></emit><dest><CPF>43223224840</CPF><xNome>henrique Floret de Castro</xNome><enderDest><xLgr>Rua Doutor João de Góes Manso Sayão Netto 740,Proximo A P</xLgr><nro>S/N</nro><xBairro>Vila Industrial</xBairro><cMun>3506003</cMun><xMun>Bauru</xMun><UF>SP</UF><CEP>17055390</CEP><cPais>1058</cPais><xPais>Brasil</xPais></enderDest><indIEDest>9</indIEDest></dest><autXML><CNPJ>52223327000126</CNPJ></autXML><det nItem=\"1\"><prod><cProd>A014A</cProd><cEAN>SEM GTIN</cEAN><xProd>A100 Microfone</xProd><NCM>85181090</NCM><CFOP>5102</CFOP><uCom>UN</uCom><qCom>1</qCom><vUnCom>137.9</vUnCom><vProd>137.90</vProd><cEANTrib>SEM GTIN</cEANTrib><uTrib>UN</uTrib><qTrib>1</qTrib><vUnTrib>137.9</vUnTrib><indTot>1</indTot><xPed>UP30MX015756</xPed></prod><imposto><vTotTrib>46.78</vTotTrib><ICMS><ICMSSN102><orig>0</orig><CSOSN>102</CSOSN></ICMSSN102></ICMS><IPI><cEnq>999</cEnq><IPITrib><CST>99</CST><vBC>0.00</vBC><pIPI>0.00</pIPI><vIPI>0.00</vIPI></IPITrib></IPI><PIS><PISOutr><CST>99</CST><vBC>0.00</vBC><pPIS>0.00</pPIS><vPIS>0.00</vPIS></PISOutr></PIS><COFINS><COFINSOutr><CST>99</CST><vBC>0.00</vBC><pCOFINS>0.00</pCOFINS><vCOFINS>0.00</vCOFINS></COFINSOutr></COFINS></imposto><infAdProd>s_6803571842421218</infAdProd></det><total><ICMSTot><vBC>0.00</vBC><vICMS>0.00</vICMS><vICMSDeson>0.00</vICMSDeson><vFCPUFDest>0.00</vFCPUFDest><vICMSUFDest>0.00</vICMSUFDest><vICMSUFRemet>0</vICMSUFRemet><vFCP>0.00</vFCP><vBCST>0.00</vBCST><vST>0.00</vST><vFCPST>0.00</vFCPST><vFCPSTRet>0.00</vFCPSTRet><vProd>137.90</vProd><vFrete>0.00</vFrete><vSeg>0.00</vSeg><vDesc>0.00</vDesc><vII>0.00</vII><vIPI>0.00</vIPI><vIPIDevol>0.00</vIPIDevol><vPIS>0.00</vPIS><vCOFINS>0.00</vCOFINS><vOutro>0.00</vOutro><vNF>137.90</vNF><vTotTrib>46.78</vTotTrib></ICMSTot></total><transp><modFrete>2</modFrete><transporta/><vol><qVol>1</qVol></vol></transp><pag><detPag><indPag>0</indPag><tPag>99</tPag><xPag>pix</xPag><vPag>137.90</vPag></detPag></pag><infIntermed><CNPJ>03007331000141</CNPJ><idCadIntTran>CR20241127134854</idCadIntTran></infIntermed><infAdic><infCpl>Conf. Lei 12.741/2012 Tributação aprox. R$21.95 (15.92%) Federal e R$24.82 (18.0%) Estadual - Fonte IBPT /</infCpl></infAdic></infNFe><Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\"><SignedInfo><CanonicalizationMethod Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/><SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/><Reference URI=\"#NFe35250352440061000173550070000001001151091188\"><Transforms><Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/><Transform Algorithm=\"http://www.w3.org/TR/2001/REC-xml-c14n-20010315\"/></Transforms><DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/><DigestValue>1oEe+mjnqCwzhQjTUYqtuFhj/Yk=</DigestValue></Reference></SignedInfo><SignatureValue>EN7J/b+CW7uSWkv8AUDF73/XS2bxh0DQlV+cOCUU7DagPgo5kldrkJ1a5ngpnj7Q7ozSSt0jKO60etHNIBojWynS+evfq+W+nvdhH7mDZOfR3B09b4rM0/ptytDC19VjLDxQoLQEyPDz2y8kJKUn+18oSAuUJFEaDNpSmCTIqcwDvBLVcFncp6rona53pqPqXeIc/n3jCL7EWBQoFX8jILR2v8JzKgq925m1lMfe2b2d1ynSwEbmiwxVxUSKC1Nfpo7D2p2vxc+t4FmNPFULMkT/AMoODWM2rgZjfncJD2k6D9WsqgMuXvgV5RvFbDcfmr3yufV4jo8uWlynWUISGA==</SignatureValue><KeyInfo><X509Data><X509Certificate>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</X509Certificate></X509Data></KeyInfo></Signature></NFe><protNFe versao=\"4.00\"><infProt><tpAmb>1</tpAmb><verAplic>SP_NFE_PL009_V4</verAplic><chNFe>35250352440061000173550070000001001151091188</chNFe><dhRecbto>2025-03-28T23:30:48-03:00</dhRecbto><nProt>135250823129468</nProt><digVal>1oEe+mjnqCwzhQjTUYqtuFhj/Yk=</digVal><cStat>100</cStat><xMotivo>Autorizado o uso da NF-e</xMotivo></infProt></protNFe></nfeProc>";
//        //设置请求头
//        Map<String, String> headerMap = new HashMap<>(1);
//        headerMap.put("Authorization", "Bearer " +accessToken);
//        headerMap.put("Content-Type", "application/xml");
//
//        //拉取数据
//        ApiResult apiResult = new ApiResult();
//        apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), param, null, headerMap, RequestMethod.POST);
//        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
//            System.out.println(apiResult.getData());
//        }
//
//    }


    private static RedisUtil redisUtil;


    @Resource
    private ShopInfoFeign shopInfoFeign;


    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    public void setRedisUtil(RedisUtil redisUtil) {
        MercadoLocalSdkClientService.redisUtil = redisUtil;
    }

    /**
     * 发送请求到美客多获取token
     *
     * @param paramMap
     * @return com.sdk.oms.mercado.dto.mercado.MercadoTokenDTO
     * @Author Luo_WG
     * @Date 2024/2/27 18:05
     **/
    public PlatformMercadoTokenDTO sendMercadoPostToken(Map<String, String> paramMap) {

        //组装授权url
        String clientId = paramMap.get("clientId");
        String clientSecret = paramMap.get("clientSecret");
        String redirectUri = paramMap.get("redirectUri");
        String url = paramMap.get("baseUrl");
        String code = paramMap.get("code");

        //https://api.mercadolibre.com/oauth/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s
        String path = "/oauth/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s";
        String baseUrl = String.format(url + path, clientId, clientSecret, code, redirectUri);

        //入参（无）
        Map<String, Object> param = new HashMap<>();
        param.put("code", code);

        //请求头
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("content-type", "application/x-www-form-urlencoded");
        headerMap.put("accept", "application/json");

        //发起POST请求
        String bodyStr = OkHttpUtils.doPost(baseUrl, param, headerMap);

        //解析数据
        PlatformMercadoTokenDTO tokenDTO = null;
        try {
            tokenDTO = JSONUtil.toBean(bodyStr, PlatformMercadoTokenDTO.class);
//            log.info(String.format("::::: 美客多授权 ::::: 请求地址 => %s, 平台返回值 => %s ", baseUrl, tokenDTO));
        } catch (Exception e) {
            log.error("调用url={},入参params={}, 美客多授权失败，返回值 responseMap={}, 错误信息={}", bodyStr, param.toString(), JSONUtil.toJsonStr(bodyStr), e.getMessage());
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多授权失败，返回值 responseMap={}, 错误信息={}",
                    bodyStr, param.toString(), JSONUtil.toJsonStr(bodyStr), e.getMessage()));
        }
        if (StringUtil.isBlank(tokenDTO.getAccessToken())) {
            throw new ServiceException(ApiError.ERROR_SHOP_AUTHORIZE_FAIL, PlatformDictEnum.MERCADOLIBRE_LOCAL.getName(), bodyStr);
        }

        //返回token实体
        return tokenDTO;
    }

    /**
     * 美客多刷新token方法
     *
     * @param dto
     * @return
     */
    public PlatformMercadoRefreshTokenDTO refreshToken(ShopDTO.RefreshTokenDTO dto) {

        //TG-65e12c65326e580001c3a0ba-1509269799
        //组装刷新token请求的url
        //https://api.mercadolibre.com
        String path = "/oauth/token?grant_type=refresh_token&client_id=%s&client_secret=%s&refresh_token=%s";
        String baseUrl = String.format(dto.getBaseUrl() + path, dto.getClientId(), dto.getClientSecret(), dto.getRefreshToken());

        //入参（无）
        Map<String, Object> param = new HashMap<>();

        //请求头（无）
        Map<String, String> headerMap = new HashMap<>();

        String token = "";
        long sleepTime = 1000;
        int count = 0;
        //解析数据
        PlatformMercadoRefreshTokenDTO refreshTokenDTO = null;
        while(StringUtil.isBlank(token)) {
            String bodyStr = OkHttpUtils.doPost(baseUrl, param, headerMap);
            try {
                refreshTokenDTO = JSONUtil.toBean(bodyStr, PlatformMercadoRefreshTokenDTO.class);
//            log.info(String.format("::::: 美客多刷新token ::::: 请求地址 => %s, 平台返回值 => %s ", baseUrl, refreshTokenDTO));
            } catch (Exception e) {
                log.error("调用url={},入参params={}, 美客多刷新token失败，返回值 responseMap={}, 错误信息={}", bodyStr, param.toString(), JSONUtil.toJsonStr(bodyStr));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多刷新token失败，返回值 responseMap={}",
                        bodyStr, param.toString(), JSONUtil.toJsonStr(bodyStr), ExceptionUtil.stacktraceToString(e)));
            }

            if(StringUtil.isBlank(refreshTokenDTO.getAccessToken())) {
                if(count == 10) {
                    throw new ServiceException(ApiError.ERROR_SHOP_AUTHORIZE_FAIL, PlatformDictEnum.MERCADOLIBRE_LOCAL.getName(), bodyStr);

                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                sleepTime = sleepTime + 1000;
                count = count + 1;
            } else {
                token = refreshTokenDTO.getAccessToken();
            }

        }
        //返回token实体
        return refreshTokenDTO;
    }

    //https://global-selling.mercadolibre.com/authorization?client_id=3457166802805723&redirect_uri=https://erptest.ulanzi.cn:8020/store-permission-result&response_type=code


    /**
     * 发送请求获取指定店铺的sku信息
     *
     * @param shopInfoDTO
     * @return
     */
    public List<ListingViewDTO> sendMercadoGetListing(MercadoShopInfoDTO shopInfoDTO) {

        List<ListingViewDTO> resultsBeanList = new ArrayList<>();

        //每次最多获取200条
        Integer pageSize = 50;
        //当前页数
        Integer pageNo = 0;
        //总页数
        Integer pageCount = 1;

        Boolean nexflag = true;
        String baseUrl = "https://api.mercadolibre.com/users/" + shopInfoDTO.getUserId() + "/items/search";
        while (nexflag) {
            int offset = pageSize * pageNo;

            //入参
            HashMap<String, Object> params = new HashMap<>(2);
            params.put("limit", pageSize);
            params.put("offset", offset);

            //设置请求头
            Map<String, String> headerMap = new HashMap<>(1);
            headerMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(baseUrl, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                nexflag = false;
                log.error("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}", baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多items/search数据失败，返回值 responseMap={}",
                        baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            //解析数据
            ObjectMapper objectMapper = new ObjectMapper();
            ListingDTO listingDTO = null;
            try {
                listingDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), ListingDTO.class);
            } catch (JsonProcessingException e) {
                nexflag = false;
                log.error("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}", baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }

            if (CollectionUtils.isEmpty(listingDTO.getResults())) {
                nexflag = false;
                break;
            }
            pageNo++;

            //获取到所有客户的产品id
            List<String> results = listingDTO.getResults();

            //根据产品id查询产品详情信息
            List<ListingViewDTO> listingViewDTOS = this.listItemView(results, shopInfoDTO.getAccessToken());
            resultsBeanList.addAll(listingViewDTOS);

        }

        if (CollectionUtils.isEmpty(resultsBeanList)) {
            return Collections.emptyList();
        }

        return resultsBeanList;
    }

    /**
     * 根据产品id查询产品详情信息
     *
     * @param results
     * @param accessToken
     * @return
     */
    private List<ListingViewDTO> listItemView(List<String> results, String accessToken) {
        List<List<String>> partition = Lists.partition(results, 20);

        List<ListingViewDTO> resultList = new ArrayList<>();
        for (List<String> list : partition) {
            String baseUrl = "https://api.mercadolibre.com/items";

            //入参
            HashMap<String, Object> params = new HashMap<>(1);
            params.put("ids", StringUtils.join(list, ","));

            //设置请求头
            Map<String, String> headerMap = new HashMap<>(1);
            headerMap.put("Authorization", "Bearer " + accessToken);

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(baseUrl, JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
            if (!Objects.equals(apiResult.getCode(), 200)) {
                log.error("调用url={},入参params={}, 美客多Listing数据失败，返回值 responseMap={}", baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多Listing数据失败，返回值 responseMap={}",
                        baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }


            ObjectMapper objectMapper = new ObjectMapper();
            List<ListingViewDTO> dataList = null;
            try {
                dataList = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), new TypeReference<List<ListingViewDTO>>() {
                });
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                log.error("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}", baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        baseUrl, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            if (CollectionUtil.isEmpty(dataList)) {
                break;
            }
            resultList.addAll(dataList);
        }

        return resultList;
    }

//    public static void main(String[] args) {
//        MercadoLocalSdkClientService sdkClientService = new MercadoLocalSdkClientService();
//        MercadoShopInfoDTO shopInfoDTO = new MercadoShopInfoDTO();
//        JobTaskDTO task = new JobTaskDTO();
//        shopInfoDTO.setUserId(2119968271L);
//        shopInfoDTO.setAccessToken("APP_USR-5344160433223219-031022-1a5190634d7aba85c9b279a5ffb4af4d-2119968271");
//        task.setLastTime(LocalDateTime.parse("2025-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//        task.setNextTime(LocalDateTime.parse("2025-03-11 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//        sdkClientService.sendMercadoGetOrder(shopInfoDTO, task);
//    }


    private String dateToStr(LocalDateTime dateTime) {

        // 转换为UTC时区的OffsetDateTime
        OffsetDateTime utcTime = dateTime
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime()
                .withOffsetSameInstant(ZoneOffset.UTC);

        // 自定义格式化
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String formatted = utcTime.format(formatter);

        // 字符串替换
        String target = formatted.replace("+00:00", "-00");
        return target;
    }

    /**
     * 发送请求获取指定店铺的订单
     *
     * @param shopInfoDTO
     * @return
     */
    public List<OrderViewDTO> sendMercadoGetOrder(MercadoShopInfoDTO shopInfoDTO, JobTaskDTO task) {
        String url = MercadoConstant.URL;
        String path = "/orders/search";
        //每页最大50条
        Integer pageSize = 50;
        //当前页数
        Integer pageNo = 0;
        //总页数
        Integer pageCount = 1;

        List<OrderViewDTO> resultList = new ArrayList<>();
        Boolean nexflag = true;
        while (nexflag) {
            int offset = pageSize * pageNo;

            StringBuffer sb = new StringBuffer();
            sb.append(url);
            sb.append(path);
            sb.append("?seller=");
            sb.append(shopInfoDTO.getUserId());
            sb.append("&limit=");
            sb.append(pageSize);
            sb.append("&offset=");
            sb.append(offset);
            sb.append("&order.date_last_updated.from=");
            sb.append(this.dateToStr(task.getLastTime()));
            sb.append("&order.date_last_updated.to=");
            sb.append(this.dateToStr(task.getNextTime()));
            sb.append("&order.status=");
            sb.append("cancelled,paid,invalid");

            //入参
            HashMap<String, Object> params = new HashMap<>(2);
            //设置请求头
            Map<String, String> headerMap = new HashMap<>(1);
            headerMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());

            //拉取数据
            ApiResult apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), JSONUtil.toJsonStr(params), null, headerMap, RequestMethod.GET);
            if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
                nexflag = false;
                log.error("调用url={},入参params={}, 美客多marketplace/orders/search数据失败，返回值 responseMap={}", url + path, params.toString(), JSONUtil.toJsonStr(apiResult));
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            ObjectMapper objectMapper = new ObjectMapper();
            OrderDataDTO orderDataDTO = null;
            try {
                orderDataDTO = objectMapper.readValue(JSONUtil.toJsonStr(apiResult.getData()), OrderDataDTO.class);
            } catch (JsonProcessingException e) {
                nexflag = false;
                log.error("美客多orders/search接口数据解析错误，数据={}", apiResult.getData());
                throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                        url + path, params.toString(), JSONUtil.toJsonStr(apiResult)));
            }
            //解析数据
//            OrderDTO orderDTO = JSONUtil.toBean(JSONUtil.toJsonStr(apiResult.getData()), OrderDTO.class);
            if (CollectionUtils.isEmpty(orderDataDTO.getResults())) {
                nexflag = false;
                break;
            }

            for (OrderViewDTO orderViewDTO : orderDataDTO.getResults()) {
                //根据发货id查询发货详情
                ShipmentViewDTO shippingRecords = getShippingRecords(shopInfoDTO, orderViewDTO.getShipping().getId());
                if (ObjectUtil.isNotEmpty(shippingRecords)) {
                    orderViewDTO.setShipmentViewDTO(shippingRecords);
                }

                //根据发货id查询费用信息
                CostDTO shippingCost = getShippingCost(shopInfoDTO, orderViewDTO.getShipping().getId());
                if (ObjectUtil.isNotEmpty(shippingCost)) {
                    orderViewDTO.setCostDTO(shippingCost);
                }
                resultList.add(orderViewDTO);
            }
            pageNo++;
        }
        return resultList;
    }

    /**
     * 根据发货id查询发货详情
     *
     * @param shopInfoDTO
     * @param shippingId
     * @return
     */
    public ShipmentViewDTO getShippingRecords(MercadoShopInfoDTO shopInfoDTO, Long shippingId) {
        String orderUrl = "https://api.mercadolibre.com/shipments/" + shippingId + "";

        //入参
        HashMap<String, Object> orderParams = new HashMap<>(1);

        //设置请求头
        Map<String, String> orderHeaderMap = new HashMap<>(1);
        orderHeaderMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());
        orderHeaderMap.put("x-format-new", "true");

        //拉取数据
        ApiResult shipmentResult = new ApiResult();
        Object data = null;
        long sleepTime = 1000;
        int count = 0;
        while (ObjectUtil.isEmpty(data)) {
            shipmentResult = HttpCommonUtil.sendOkHttpApiResult(orderUrl, JSONUtil.toJsonStr(orderParams), null, orderHeaderMap, RequestMethod.GET);
            if (shipmentResult.getMsg().equalsIgnoreCase("Read timed out")) {
                if (count == 10) {
                    throw new ServiceException("调用美客多" + orderUrl + "接口重试" + count + "失败");
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                sleepTime = sleepTime + 1000;
                count = count + 1;
            }
            data = shipmentResult.getData();
        }

        if (!Objects.equals(shipmentResult.getCode(), 200) && !Objects.equals(shipmentResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, 美客多marketplace/shipments数据失败，返回值 responseMap={}", orderUrl, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                    orderUrl, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult)));
        }

        //解析数据
        ObjectMapper objectMapper = new ObjectMapper();
        ShipmentViewDTO orderViewDTO = null;
        try {
            orderViewDTO = objectMapper.readValue(JSONUtil.toJsonStr(shipmentResult.getData()), ShipmentViewDTO.class);
        } catch (JsonProcessingException e) {
            log.error("美客多shipments/'shippingId'/接口数据解析错误，数据={}", shipmentResult.getData());
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 数据解析失败，返回值 responseMap={}",
                    orderUrl, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult)));
        }
        return orderViewDTO;

    }

    /**
     * 根据发货id查询费用信息
     *
     * @param shopInfoDTO
     * @param shippingId
     * @return
     */
    private CostDTO getShippingCost(MercadoShopInfoDTO shopInfoDTO, Long shippingId) {
        String orderUrl = "https://api.mercadolibre.com/shipments/" + shippingId + "/costs";

        //入参
        HashMap<String, Object> orderParams = new HashMap<>(1);

        //设置请求头
        Map<String, String> orderHeaderMap = new HashMap<>(1);
        orderHeaderMap.put("Authorization", "Bearer " + shopInfoDTO.getAccessToken());
        orderHeaderMap.put("x-format-new", "true");

        //拉取数据
        ApiResult shipmentResult = new ApiResult();
        Object data = null;
        long sleepTime = 1000;
        int count = 0;
        while (ObjectUtil.isEmpty(data)) {
            shipmentResult = HttpCommonUtil.sendOkHttpApiResult(orderUrl, JSONUtil.toJsonStr(orderParams), null, orderHeaderMap, RequestMethod.GET);
            if (shipmentResult.getMsg().equalsIgnoreCase("Read timed out")) {
                if (count == 10) {
                    throw new ServiceException("调用美客多" + orderUrl + "接口重试" + count + "失败");
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                sleepTime = sleepTime + 1000;
                count = count + 1;
            }
            data = shipmentResult.getData();
        }

        if (!Objects.equals(shipmentResult.getCode(), 200) && !Objects.equals(shipmentResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, 美客多费用明细数据失败，返回值 responseMap={}", orderUrl, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 费用明细请求失败，返回值 responseMap={}",
                    orderUrl, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult)));
        }

        //解析数据
        ObjectMapper objectMapper = new ObjectMapper();
        CostDTO costDTO = null;
        try {
            costDTO = objectMapper.readValue(JSONUtil.toJsonStr(shipmentResult.getData()), CostDTO.class);
        } catch (JsonProcessingException e) {
            log.error("美客多费用明细接口数据解析错误，数据={}", shipmentResult.getData());
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 费用明细数据解析失败，返回值 responseMap={}",
                    orderUrl, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult)));
        }
        return costDTO;

    }

    /**
     * 查询店铺信息
     *
     * @param shopId
     * @return
     */
    public MercadoShopInfoDTO getShopInfoByShopId(String shopId) {
        String tokenKey = StrUtil.format(RedisCacheConstants.REDIS_PLATFORM_TOKEN, PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode(), shopId);
        // 缓存获取
        Object tokenObj = redisUtil.get(tokenKey);
        if (null != tokenObj) {
            if (tokenObj instanceof MercadoShopInfoDTO) {
                return (MercadoShopInfoDTO) tokenObj;
            }
        } else {
            ShopAuthEntity shopAuthEntity = shopInfoFeign.getShopAuthByShopId(shopId);
            ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(shopId);
            CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
            AppClientEnum appClientEnum = AppClientEnum.MERCADO_ACCESS_TOKEN;
            findDTO.setBusinessType(appClientEnum.getBusinessType());
            findDTO.setDictPlatform(appClientEnum.getPlatform());
            findDTO.setPlatformType(appClientEnum.getPlatformType());
            CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
            if (Objects.isNull(cfgAppClient)) {
                return null;
            }
            MercadoShopInfoDTO result = new MercadoShopInfoDTO();
            result.setBaseUrl(cfgAppClient.getUrl());
            result.setClientId(cfgAppClient.getClientId());
            result.setClientSecret(cfgAppClient.getClientSecret());

            Map<String, Object> extendData = shopInfoEntity.getExtendData();
            result.setUserId(Long.valueOf(extendData.get("userId") + ""));
            result.setSiteId(shopInfoEntity.getBusinessModel());
            if (Objects.nonNull(shopAuthEntity)) {
                result.setAccessToken(shopAuthEntity.getAccessToken());
                redisUtil.set(tokenKey, result, shopAuthEntity.getExpiresIn());
            }

            return result;
        }
        return null;

    }

    /**
     * 根据发货id查询发货详情
     *
     * @param authMap
     * @param shippingId
     * @return
     */
    public String printShippingLabel(Map<String, String> authMap, Long shippingId) {
//        String orderUrl = "https://api.mercadolibre.com/marketplace/shipments/"+shippingId+"/labels";
        String orderUrl = "https://api.mercadolibre.com/shipment_labels";
        String token = authMap.get("token");
        //入参
        HashMap<String, Object> orderParams = new HashMap<>(1);
        orderParams.put("shipment_ids",shippingId);
        orderParams.put("response_type","pdf");
        //设置请求头
        Map<String, String> orderHeaderMap = new HashMap<>(1);
        orderHeaderMap.put("Authorization", "Bearer " + token);
        orderHeaderMap.put("x-format-new", "true");
        //拉取数据
        return OkHttpUtils.doGetJsonBase64(orderUrl, orderParams, orderHeaderMap);
    }

    /**
     * 上传发票
     */
    public void uploadInvoice(MercadoInvoiceDTO mercadoInvoiceDTO) {

        //  根据店铺ID获取授权
        MercadoShopInfoDTO shopInfoByShopId = this.getShopInfoByShopId(mercadoInvoiceDTO.getShopId());

        String accessToken = shopInfoByShopId.getAccessToken();
        //组装授权url
        String url = MercadoConstant.URL;
        String path = "/shipments/{id}/invoice_data/?siteId=" + shopInfoByShopId.getSiteId();
        path = path.replace("{id}",mercadoInvoiceDTO.getShipmentId());
        StringBuffer sb = new StringBuffer();
        sb.append(url);
        sb.append(path);
        //入参
        String param = mercadoInvoiceDTO.getXmlContent();
        //设置请求头
        Map<String, String> headerMap = new HashMap<>(1);
        headerMap.put("Authorization", "Bearer " +accessToken);
        headerMap.put("Content-Type", "application/xml");

        //拉取数据
        ApiResult apiResult = new ApiResult();
        apiResult = HttpCommonUtil.sendOkHttpApiResult(sb.toString(), param, null, headerMap, RequestMethod.POST);
        if (!Objects.equals(apiResult.getCode(), 200) && !Objects.equals(apiResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, 美客多上传发票数据失败，返回值 responseMap={}", path, param, JSONUtil.toJsonStr(apiResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多上传发票数据失败，返回值 responseMap={}",
                    path, param, JSONUtil.toJsonStr(apiResult)));
        }
    }
    /**
     * 标记发货
     */
    public void shipOrder(MercadoShipOrderDTO shipOrderDTO) {
        //  根据店铺ID获取授权
        MercadoShopInfoDTO shopInfoByShopId = this.getShopInfoByShopId(shipOrderDTO.getShopId());

        String orderUrl = "https://api.mercadolibre.com/shipments/" + shipOrderDTO.getShipmentId() + "/tracking ";
        String token = shopInfoByShopId.getAccessToken();

        //入参
        HashMap<String, Object> orderParams = new HashMap<>(3);
        orderParams.put("tracking_id", shipOrderDTO.getTrackingId());
        orderParams.put("tracking_url", shipOrderDTO.getTrackingUrl());
        orderParams.put("carrier", shipOrderDTO.getCarrier());
        //设置请求头
        Map<String, String> orderHeaderMap = new HashMap<>(1);
        orderHeaderMap.put("Authorization", "Bearer " + token);

        //拉取数据
        ApiResult shipmentResult = HttpCommonUtil.sendOkHttpApiResult(orderUrl, JSONUtil.toJsonStr(orderParams), null, orderHeaderMap, RequestMethod.POST);
        if (!Objects.equals(shipmentResult.getCode(), 200) && !Objects.equals(shipmentResult.getCode(), 201)) {
            log.error("调用url={},入参params={}, 美客多标记发货shipments/tracking数据失败，返回值 responseMap={}", orderUrl, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult));
            throw new RuntimeException(StrUtil.format("调用url={},入参params={}, 美客多shipments/tracking数据失败，返回值 responseMap={}",
                    orderUrl, orderParams.toString(), JSONUtil.toJsonStr(shipmentResult)));
        }

    }
}
