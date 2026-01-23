package com.sdk.third.tf.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sdk.third.tf.util.JsonUtil;
import com.sdk.third.tf.constant.TfApiConstants;
import com.sdk.third.tf.dto.ApiResponseDTO;
import com.sdk.third.tf.dto.TaxCategoryDTO;
import com.sdk.third.tf.util.SignUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 税种API客户端
 * 封装税种相关的API调用
 * 
 * @author system
 * @date 2025/01/XX
 */
@Slf4j
public class TaxCategoryApiClient {

    private static final String PATH_CREATE_CATEGORY = "/api/category/create";
    private static final String PATH_GET_CATEGORY_LIST = "/api/category/get_list";
    private static final String PATH_GET_CATEGORY_DETAIL = "/api/category/get_detail";
    private static final String PATH_EDIT_CATEGORY = "/api/category/edit";
    private static final String PATH_DELETE_CATEGORY = "/api/category/delete";

    private TfApiClient tfApiClient;

    /**
     * 构造函数
     * 用于依赖注入（SDK 不使用 Spring 注解）
     */
    public TaxCategoryApiClient() {
        // 默认构造函数，用于 Spring Bean 创建
    }

    /**
     * 构造函数（带依赖）
     * 用于依赖注入（SDK 不使用 Spring 注解）
     */
    public TaxCategoryApiClient(TfApiClient tfApiClient) {
        this.tfApiClient = tfApiClient;
    }

    /**
     * 创建税种
     * 注意：税种接口使用公司token（cfg_invoice_setting.token），不是经销商token
     * 
     * @param createDTO 创建税种DTO
     * @param companyToken 公司token（cfg_invoice_setting.token），用于Header中的token
     * @param appKey AppKey（用于签名）
     * @return 创建响应DTO
     */
    public TaxCategoryDTO.CreateCategoryResponseDTO createCategory(
            TaxCategoryDTO.CreateCategoryDTO createDTO, String companyToken, String appKey) {
        String requestBody = JsonUtil.toJsonString(createDTO);
        
        // 生成时间戳
        String timestamp = SignUtil.generateTimestamp();
        
        // 生成签名：MD5(AppKey + Path + bodyString + timestamp)
        String sign = SignUtil.generateSign(appKey, PATH_CREATE_CATEGORY, requestBody, timestamp);
        
        // 构建Header：sign、timestamp、token（公司token）
        Map<String, String> headers = buildHeaders(sign, timestamp, companyToken);
        
        log.info("创建税种, path: {}, timestamp: {}, sign: {}", PATH_CREATE_CATEGORY, timestamp, sign);
        
        TypeReference<ApiResponseDTO<TaxCategoryDTO.CreateCategoryResponseDTO>> typeRef = 
            new TypeReference<ApiResponseDTO<TaxCategoryDTO.CreateCategoryResponseDTO>>() {};
        
        ApiResponseDTO<TaxCategoryDTO.CreateCategoryResponseDTO> response = 
            tfApiClient.doPost(PATH_CREATE_CATEGORY, requestBody, typeRef, headers);
        
        validateResponse(response);
        return response.getData();
    }

    /**
     * 查询税种列表
     * 注意：税种接口使用公司token（cfg_invoice_setting.token），不是经销商token
     * 
     * @param page 页码
     * @param pageSize 每页大小
     * @param companyToken 公司token（cfg_invoice_setting.token），用于Header中的token
     * @param appKey AppKey（用于签名）
     * @return 税种列表响应DTO
     */
    public TaxCategoryDTO.CategoryListResponseDTO getCategoryList(
            Integer page, Integer pageSize, String companyToken, String appKey) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page != null ? page : 1);
        params.put("page_size", pageSize != null ? pageSize : 10);
        
        String path = PATH_GET_CATEGORY_LIST + TfApiClient.buildQueryString(params);
        
        // 生成时间戳
        String timestamp = SignUtil.generateTimestamp();
        
        // 生成签名：MD5(AppKey + Path + bodyString + timestamp)
        // GET请求bodyString为空字符串
        String sign = SignUtil.generateSign(appKey, path, "", timestamp);
        
        // 构建Header：sign、timestamp、token（公司token）
        Map<String, String> headers = buildHeaders(sign, timestamp, companyToken);
        
        log.info("查询税种列表, path: {}, timestamp: {}, sign: {}", path, timestamp, sign);
        
        TypeReference<ApiResponseDTO<TaxCategoryDTO.CategoryListResponseDTO>> typeRef = 
            new TypeReference<ApiResponseDTO<TaxCategoryDTO.CategoryListResponseDTO>>() {};
        
        ApiResponseDTO<TaxCategoryDTO.CategoryListResponseDTO> response = 
            tfApiClient.doGet(path, typeRef, headers);
        
        validateResponse(response);
        return response.getData();
    }

    /**
     * 查询税种详情
     * 注意：税种接口使用公司token（cfg_invoice_setting.token），不是经销商token
     * 
     * @param categoryId 税种ID
     * @param companyToken 公司token（cfg_invoice_setting.token），用于Header中的token
     * @param appKey AppKey（用于签名）
     * @return 税种详情DTO
     */
    public TaxCategoryDTO.CategoryDetailDTO getCategoryDetail(
            String categoryId, String companyToken, String appKey) throws UnsupportedEncodingException {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("税种ID不能为空");
        }
        
        // URL编码categoryId
        String encodedCategoryId = URLEncoder.encode(categoryId, StandardCharsets.UTF_8.name());
        String path = PATH_GET_CATEGORY_DETAIL + "?category_id=" + encodedCategoryId;
        
        // 生成时间戳
        String timestamp = SignUtil.generateTimestamp();
        
        // 生成签名：MD5(AppKey + Path + bodyString + timestamp)
        // GET请求bodyString为空字符串
        String sign = SignUtil.generateSign(appKey, path, "", timestamp);
        
        // 构建Header：sign、timestamp、token（公司token）
        Map<String, String> headers = buildHeaders(sign, timestamp, companyToken);
        
        log.info("查询税种详情, path: {}, timestamp: {}, sign: {}", path, timestamp, sign);
        
        TypeReference<ApiResponseDTO<TaxCategoryDTO.CategoryDetailDTO>> typeRef = 
            new TypeReference<ApiResponseDTO<TaxCategoryDTO.CategoryDetailDTO>>() {};
        
        ApiResponseDTO<TaxCategoryDTO.CategoryDetailDTO> response = 
            tfApiClient.doGet(path, typeRef, headers);
        
        validateResponse(response);
        return response.getData();
    }

    /**
     * 编辑税种
     * 注意：税种接口使用公司token（cfg_invoice_setting.token），不是经销商token
     * 
     * @param editDTO 编辑税种DTO
     * @param companyToken 公司token（cfg_invoice_setting.token），用于Header中的token
     * @param appKey AppKey（用于签名）
     * @return 编辑响应DTO
     */
    public TaxCategoryDTO.EditCategoryResponseDTO editCategory(
            TaxCategoryDTO.EditCategoryDTO editDTO, String companyToken, String appKey) {
        String requestBody = JsonUtil.toJsonString(editDTO);
        
        // 生成时间戳
        String timestamp = SignUtil.generateTimestamp();
        
        // 生成签名：MD5(AppKey + Path + bodyString + timestamp)
        String sign = SignUtil.generateSign(appKey, PATH_EDIT_CATEGORY, requestBody, timestamp);
        
        // 构建Header：sign、timestamp、token（公司token）
        Map<String, String> headers = buildHeaders(sign, timestamp, companyToken);
        
        log.info("编辑税种, path: {}, timestamp: {}, sign: {}", PATH_EDIT_CATEGORY, timestamp, sign);
        
        TypeReference<ApiResponseDTO<TaxCategoryDTO.EditCategoryResponseDTO>> typeRef = 
            new TypeReference<ApiResponseDTO<TaxCategoryDTO.EditCategoryResponseDTO>>() {};
        
        ApiResponseDTO<TaxCategoryDTO.EditCategoryResponseDTO> response = 
            tfApiClient.doPost(PATH_EDIT_CATEGORY, requestBody, typeRef, headers);
        
        validateResponse(response);
        return response.getData();
    }

    /**
     * 删除税种
     * 注意：税种接口使用公司token（cfg_invoice_setting.token），不是经销商token
     * 
     * @param categoryId 税种ID
     * @param companyToken 公司token（cfg_invoice_setting.token），用于Header中的token
     * @param appKey AppKey（用于签名）
     */
    public void deleteCategory(String categoryId, String companyToken, String appKey) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("税种ID不能为空");
        }
        
        TaxCategoryDTO.DeleteCategoryDTO deleteDTO = new TaxCategoryDTO.DeleteCategoryDTO(categoryId);
        String requestBody = JsonUtil.toJsonString(deleteDTO);
        
        // 生成时间戳
        String timestamp = SignUtil.generateTimestamp();
        
        // 生成签名：MD5(AppKey + Path + bodyString + timestamp)
        String sign = SignUtil.generateSign(appKey, PATH_DELETE_CATEGORY, requestBody, timestamp);
        
        // 构建Header：sign、timestamp、token（公司token）
        Map<String, String> headers = buildHeaders(sign, timestamp, companyToken);
        
        log.info("删除税种, path: {}, timestamp: {}, sign: {}", PATH_DELETE_CATEGORY, timestamp, sign);
        
        TypeReference<ApiResponseDTO<Object>> typeRef = 
            new TypeReference<ApiResponseDTO<Object>>() {};
        
        ApiResponseDTO<Object> response = 
            tfApiClient.doPost(PATH_DELETE_CATEGORY, requestBody, typeRef, headers);
        
        validateResponse(response);
    }

    /**
     * 构建请求Header
     * 
     * @param sign 签名
     * @param timestamp 时间戳
     * @param token 公司token
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
     * 验证API响应
     */
    private void validateResponse(ApiResponseDTO<?> response) {
        if (response == null) {
            throw new RuntimeException("API响应为空");
        }
        
        if (!response.isSuccess()) {
            String message = response.getMessage() != null ? response.getMessage() : "未知错误";
            throw new RuntimeException("API请求失败: " + message);
        }
    }
}
