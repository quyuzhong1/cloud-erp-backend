package com.sdk.third.tf.client;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.TypeReference;
import com.sdk.third.tf.constant.TfApiConstants;
import com.sdk.third.tf.dto.ApiResponseDTO;
import com.sdk.third.tf.dto.CompanyDetailResponseDTO;
import com.sdk.third.tf.dto.CompanyListResponseDTO;
import com.sdk.third.tf.dto.CreateCompanyDTO;
import com.sdk.third.tf.dto.CreateCompanyResponseDTO;
import com.sdk.third.tf.dto.EditCompanyDTO;
import com.sdk.third.tf.util.SignUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 公司API客户端
 * 封装公司相关的API调用
 * 
 * @author system
 * @date 2025/01/XX
 */
@Slf4j
public class CompanyApiClient {

    private static final String PATH_CREATE_COMPANY = "/api/company/create";
    private static final String PATH_EDIT_COMPANY = "/api/company/edit";
    private static final String PATH_GET_COMPANY_DETAIL = "/api/company/get_detail";
    private static final String PATH_GET_COMPANY_LIST = "/api/company/get_list";
    
    // 请求字段名常量
    private static final String FIELD_INVOICE_TYPE = "invoice_type";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_CNPJ = "cnpj";
    private static final String FIELD_IE = "ie";
    private static final String FIELD_UNIT = "unit";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_CEP = "cep";
    private static final String FIELD_ADDRESS = "address";
    private static final String FIELD_HOUSE_NUMBER = "house_number";
    private static final String FIELD_TOWN = "town";
    private static final String FIELD_CITY = "city";
    private static final String FIELD_STATE = "state";
    private static final String FIELD_CERT_FILE = "cert_file";
    private static final String FIELD_CERT_PWD = "cert_pwd";
    private static final String FIELD_SERIE = "serie";
    private static final String FIELD_NUMBER = "number";
    private static final String FIELD_CATEGORY_ID = "category_id";
    private static final String FIELD_COMPANY_ID = "company_id";

    private TfApiClient tfApiClient;

    /**
     * 构造函数
     * 用于依赖注入（SDK 不使用 Spring 注解）
     */
    public CompanyApiClient() {
        // 默认构造函数，用于 Spring Bean 创建
    }

    /**
     * 构造函数（带依赖）
     * 用于依赖注入（SDK 不使用 Spring 注解）
     */
    public CompanyApiClient(TfApiClient tfApiClient) {
        this.tfApiClient = tfApiClient;
    }

    /**
     * 创建公司（新接口）
     * 注意：创建公司和获取公司列表接口使用经销商token（b2b_token），其他接口使用公司token
     * 
     * @param createCompanyDTO 创建公司DTO
     * @param b2bToken 经销商token（b2b_token）= TF-ACCESS_TOKEN，用于Header中的token
     * @param appKey AppKey（用于签名）
     * @return 创建公司响应数据DTO（data部分）
     */
    public CreateCompanyResponseDTO.CreateCompanyDataDTO createCompany(CreateCompanyDTO createCompanyDTO, String b2bToken, String appKey) {
        // 构建请求体（新接口不再在请求体中传递token_plataforma）
        Map<String, Object> requestBodyMap = buildRequestBody(createCompanyDTO);
        String requestBody = JSONUtil.toJsonStr(requestBodyMap);
        
        // 生成时间戳
        String timestamp = SignUtil.generateTimestamp();
        
        // 生成签名：MD5(AppKey + Path + bodyString + timestamp)
        String sign = SignUtil.generateSign(appKey, PATH_CREATE_COMPANY, requestBody, timestamp);
        
        // 构建Header：sign、timestamp、token（经销商token/b2b_token）
        Map<String, String> headers = buildHeaders(sign, timestamp, b2bToken);
        
        log.info("创建公司（新接口）, path: {}, timestamp: {}, sign: {}", 
            PATH_CREATE_COMPANY, timestamp, sign);
        
        // 响应格式：{"success": true, "message": "成功", "data": {"company_id": "2", "token": "123456789"}}
        TypeReference<ApiResponseDTO<CreateCompanyResponseDTO.CreateCompanyDataDTO>> typeRef = 
            new TypeReference<ApiResponseDTO<CreateCompanyResponseDTO.CreateCompanyDataDTO>>() {};
        
        ApiResponseDTO<CreateCompanyResponseDTO.CreateCompanyDataDTO> response = 
            tfApiClient.doPost(PATH_CREATE_COMPANY, requestBody, typeRef, headers);
        
        validateResponse(response);
        return response.getData();
    }

    /**
     * 编辑公司（新接口）
     * 注意：编辑公司接口使用公司token（cfg_invoice_setting.token），不是经销商token
     * 
     * @param editCompanyDTO 编辑公司DTO
     * @param companyToken 公司token（cfg_invoice_setting.token），用于Header中的token
     * @param appKey AppKey（用于签名）
     */
    public void editCompany(EditCompanyDTO editCompanyDTO, String companyToken, String appKey) {
        // 构建请求体
        Map<String, Object> requestBodyMap = buildEditRequestBody(editCompanyDTO);
        String requestBody = JSONUtil.toJsonStr(requestBodyMap);
        
        // 生成时间戳
        String timestamp = SignUtil.generateTimestamp();
        
        // 生成签名：MD5(AppKey + Path + bodyString + timestamp)
        String sign = SignUtil.generateSign(appKey, PATH_EDIT_COMPANY, requestBody, timestamp);
        
        // 构建Header：sign、timestamp、token（公司token）
        Map<String, String> headers = buildHeaders(sign, timestamp, companyToken);
        
        log.info("编辑公司（新接口）, path: {}, timestamp: {}, sign: {}", 
            PATH_EDIT_COMPANY, timestamp, sign);
        
        // 响应格式：{"success": true, "message": "成功"}
        TypeReference<ApiResponseDTO<Object>> typeRef = 
            new TypeReference<ApiResponseDTO<Object>>() {};
        
        ApiResponseDTO<Object> response = 
            tfApiClient.doPost(PATH_EDIT_COMPANY, requestBody, typeRef, headers);
        
        validateEditResponse(response);
    }

    /**
     * 查询公司详情（新接口）
     * 注意：查询公司详情接口使用公司token（cfg_invoice_setting.token），不是经销商token
     * 
     * @param cnpj CNPJ（用于查询参数）
     * @param companyToken 公司token（cfg_invoice_setting.token），用于Header中的token
     * @param appKey AppKey（用于签名）
     * @return 公司详情数据DTO（data部分）
     */
    public CompanyDetailResponseDTO.CompanyDetailDataDTO getCompanyDetail(String cnpj, String companyToken, String appKey) throws UnsupportedEncodingException {
        // 构建查询参数（URL编码CNPJ）
        String encodedCnpj = URLEncoder.encode(cnpj, StandardCharsets.UTF_8.name());
        String path = PATH_GET_COMPANY_DETAIL + "?cnpj=" + encodedCnpj;
        
        // 生成时间戳
        String timestamp = SignUtil.generateTimestamp();
        
        // 生成签名：MD5(AppKey + Path + bodyString + timestamp)
        // GET请求bodyString为空字符串
        String sign = SignUtil.generateSign(appKey, path, "", timestamp);
        
        // 构建Header：sign、timestamp、token（公司token）
        Map<String, String> headers = buildHeaders(sign, timestamp, companyToken);
        
        log.info("查询公司详情（新接口）, path: {}, timestamp: {}, sign: {}", 
            path, timestamp, sign);
        
        // 响应格式：{"success": true, "message": "成功", "data": {...}}
        TypeReference<ApiResponseDTO<CompanyDetailResponseDTO.CompanyDetailDataDTO>> typeRef = 
            new TypeReference<ApiResponseDTO<CompanyDetailResponseDTO.CompanyDetailDataDTO>>() {};
        
        ApiResponseDTO<CompanyDetailResponseDTO.CompanyDetailDataDTO> response = 
            tfApiClient.doGet(path, typeRef, headers);
        
        validateGetDetailResponse(response);
        return response.getData();
    }

    /**
     * 获取公司列表（新接口）
     * 注意：获取公司列表接口使用经销商token（b2b_token），不是公司token
     * 
     * @param page 页码（必填）
     * @param pageSize 页大小（必填，最大20）
     * @param b2bToken 经销商token（b2b_token）= TF-ACCESS_TOKEN，用于Header中的token
     * @param appKey AppKey（用于签名）
     * @return 公司列表数据DTO（data部分）
     */
    public CompanyListResponseDTO.CompanyListDataDTO getCompanyList(
            Integer page, Integer pageSize, String b2bToken, String appKey) {
        // 构建查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("page", page != null ? page : 1);
        params.put("page_size", pageSize != null ? Math.min(pageSize, 20) : 20); // 最大20
        
        String path = PATH_GET_COMPANY_LIST + TfApiClient.buildQueryString(params);
        
        // 生成时间戳
        String timestamp = SignUtil.generateTimestamp();
        
        // 生成签名：MD5(AppKey + Path + bodyString + timestamp)
        // GET请求bodyString为空字符串
        String sign = SignUtil.generateSign(appKey, path, "", timestamp);
        
        // 构建Header：sign、timestamp、token（经销商token/b2b_token）
        Map<String, String> headers = buildHeaders(sign, timestamp, b2bToken);
        
        log.info("获取公司列表（新接口）, path: {}, timestamp: {}, sign: {}", 
            path, timestamp, sign);
        
        // 响应格式：{"success": true, "message": "成功", "data": {...}}
        TypeReference<ApiResponseDTO<CompanyListResponseDTO.CompanyListDataDTO>> typeRef = 
            new TypeReference<ApiResponseDTO<CompanyListResponseDTO.CompanyListDataDTO>>() {};
        
        ApiResponseDTO<CompanyListResponseDTO.CompanyListDataDTO> response = 
            tfApiClient.doGet(path, typeRef, headers);
        
        validateGetListResponse(response);
        return response.getData();
    }
    
    /**
     * 验证查询公司详情API响应
     */
    private void validateGetDetailResponse(ApiResponseDTO<CompanyDetailResponseDTO.CompanyDetailDataDTO> response) {
        if (response == null) {
            throw new RuntimeException("API响应为空");
        }
        
        if (!response.isSuccess()) {
            String message = response.getMessage() != null ? response.getMessage() : "未知错误";
            throw new RuntimeException("查询公司详情失败: " + message);
        }
        
        if (response.getData() == null) {
            throw new RuntimeException("查询公司详情失败: 响应数据为空");
        }
    }
    
    /**
     * 构建请求Header
     * 
     * @param sign 签名
     * @param timestamp 时间戳
     * @param token 经销商token（b2b_token）
     * @return Header Map
     */
    private Map<String, String> buildHeaders(String sign, String timestamp, String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put(TfApiConstants.Header.SIGN, sign);
        headers.put(TfApiConstants.Header.TIMESTAMP, timestamp);
        headers.put(TfApiConstants.Header.TOKEN, token);
        return headers;
    }

    /**
     * 构建请求体
     * 注意：新接口不再在请求体中传递token_plataforma，token通过Header传递
     * 
     * @param createCompanyDTO 创建公司DTO
     * @return 请求体Map
     */
    private Map<String, Object> buildRequestBody(CreateCompanyDTO createCompanyDTO) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(FIELD_INVOICE_TYPE, createCompanyDTO.getInvoiceType());
        requestBody.put(FIELD_NAME, createCompanyDTO.getName());
        requestBody.put(FIELD_CNPJ, createCompanyDTO.getCnpj());
        requestBody.put(FIELD_IE, createCompanyDTO.getIe());
        requestBody.put(FIELD_UNIT, createCompanyDTO.getUnit());
        requestBody.put(FIELD_EMAIL, createCompanyDTO.getEmail());
        requestBody.put(FIELD_CEP, createCompanyDTO.getCep());
        requestBody.put(FIELD_ADDRESS, createCompanyDTO.getAddress());
        requestBody.put(FIELD_HOUSE_NUMBER, createCompanyDTO.getHouseNumber());
        requestBody.put(FIELD_TOWN, createCompanyDTO.getTown());
        requestBody.put(FIELD_CITY, createCompanyDTO.getCity());
        requestBody.put(FIELD_STATE, createCompanyDTO.getState());
        requestBody.put(FIELD_CERT_FILE, createCompanyDTO.getCertFile());
        requestBody.put(FIELD_CERT_PWD, createCompanyDTO.getCertPwd());
        requestBody.put(FIELD_SERIE, createCompanyDTO.getSerie());
        requestBody.put(FIELD_NUMBER, createCompanyDTO.getNumber());
        requestBody.put(FIELD_CATEGORY_ID, createCompanyDTO.getCategoryId());
        // 注意：新接口不再在请求体中传递token_plataforma，token通过Header传递
        return requestBody;
    }

    /**
     * 构建编辑公司请求体
     * 
     * @param editCompanyDTO 编辑公司DTO
     * @return 请求体Map
     */
    private Map<String, Object> buildEditRequestBody(EditCompanyDTO editCompanyDTO) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put(FIELD_INVOICE_TYPE, editCompanyDTO.getInvoiceType());
        requestBody.put(FIELD_CNPJ, editCompanyDTO.getCnpj());
        requestBody.put(FIELD_NAME, editCompanyDTO.getName());
        requestBody.put(FIELD_IE, editCompanyDTO.getIe());
        requestBody.put(FIELD_UNIT, editCompanyDTO.getUnit());
        requestBody.put(FIELD_EMAIL, editCompanyDTO.getEmail());
        requestBody.put(FIELD_CEP, editCompanyDTO.getCep());
        requestBody.put(FIELD_ADDRESS, editCompanyDTO.getAddress());
        requestBody.put(FIELD_HOUSE_NUMBER, editCompanyDTO.getHouseNumber());
        requestBody.put(FIELD_TOWN, editCompanyDTO.getTown());
        requestBody.put(FIELD_CITY, editCompanyDTO.getCity());
        requestBody.put(FIELD_STATE, editCompanyDTO.getState());
        requestBody.put(FIELD_CERT_FILE, editCompanyDTO.getCertFile());
        requestBody.put(FIELD_CERT_PWD, editCompanyDTO.getCertPwd());
        requestBody.put(FIELD_SERIE, editCompanyDTO.getSerie());
        requestBody.put(FIELD_NUMBER, editCompanyDTO.getNumber());
        requestBody.put(FIELD_COMPANY_ID, editCompanyDTO.getCompanyId());
        // 注意：编辑公司接口不包含category_id字段
        return requestBody;
    }

    /**
     * 验证创建公司API响应
     */
    private void validateResponse(ApiResponseDTO<CreateCompanyResponseDTO.CreateCompanyDataDTO> response) {
        if (response == null) {
            throw new RuntimeException("API响应为空");
        }
        
        if (!response.isSuccess()) {
            String message = response.getMessage() != null ? response.getMessage() : "未知错误";
            throw new RuntimeException("创建公司失败: " + message);
        }
        
        if (response.getData() == null) {
            throw new RuntimeException("创建公司失败: 响应数据为空");
        }
        
        if (response.getData().getToken() == null || response.getData().getToken().trim().isEmpty()) {
            throw new RuntimeException("创建公司失败: 未返回公司token");
        }
    }

    /**
     * 验证编辑公司API响应
     */
    private void validateEditResponse(ApiResponseDTO<Object> response) {
        if (response == null) {
            throw new RuntimeException("API响应为空");
        }
        
        if (!response.isSuccess()) {
            String message = response.getMessage() != null ? response.getMessage() : "未知错误";
            throw new RuntimeException("编辑公司失败: " + message);
        }
    }

    /**
     * 验证获取公司列表API响应
     */
    private void validateGetListResponse(ApiResponseDTO<CompanyListResponseDTO.CompanyListDataDTO> response) {
        if (response == null) {
            throw new RuntimeException("API响应为空");
        }
        
        if (!response.isSuccess()) {
            String message = response.getMessage() != null ? response.getMessage() : "未知错误";
            throw new RuntimeException("获取公司列表失败: " + message);
        }
        
        if (response.getData() == null) {
            throw new RuntimeException("获取公司列表失败: 响应数据为空");
        }
    }

    /**
     * 测试main方法
     * 用于测试获取公司列表接口
     */
    public static void main(String[] args) {
        try {
            String token = "2469149568994570b542f7ae950dcc1b";
            
            // 创建TfApiClient实例（由于CompanyApiClient依赖TfApiClient，需要先创建）
            TfApiClient tfApiClient = new TfApiClient();
            
            // 创建CompanyApiClient实例并注入TfApiClient
            CompanyApiClient companyApiClient = new CompanyApiClient();
            // 使用反射设置私有字段（或者将TfApiClient改为public setter）
            try {
                java.lang.reflect.Field field = CompanyApiClient.class.getDeclaredField("tfApiClient");
                field.setAccessible(true);
                field.set(companyApiClient, tfApiClient);
            } catch (Exception e) {
                log.error("设置TfApiClient失败", e);
                return;
            }
            
            // 调用获取公司列表接口
            // 签名的appKey就是header的token，所以appKey = token
            CompanyListResponseDTO.CompanyListDataDTO result = companyApiClient.getCompanyList(1, 20, token, token);
            
            // 打印结果
            log.info("获取公司列表成功:");
            log.info("总数: {}", result.getTotal());
            log.info("总页数: {}", result.getTotalPages());
            log.info("当前页: {}", result.getPage());
            log.info("公司列表: {}", JSONUtil.toJsonStr(result.getCompanys()));
            
            System.out.println("=================== 获取公司列表结果 ===================");
            System.out.println("总数: " + result.getTotal());
            System.out.println("总页数: " + result.getTotalPages());
            System.out.println("当前页: " + result.getPage());
            System.out.println("公司列表: " + JSONUtil.toJsonPrettyStr(result.getCompanys()));
            System.out.println("=======================================================");
            
        } catch (Exception e) {
            log.error("获取公司列表失败", e);
            e.printStackTrace();
        }
    }
}
