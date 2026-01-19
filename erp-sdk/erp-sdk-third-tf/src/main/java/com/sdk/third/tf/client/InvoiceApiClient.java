package com.sdk.third.tf.client;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.TypeReference;
import com.sdk.third.tf.constant.TfApiConstants;
import com.sdk.third.tf.dto.ApiResponseDTO;
import com.sdk.third.tf.dto.CancelInvoiceDTO;
import com.sdk.third.tf.dto.CancelInvoiceResponseDTO;
import com.sdk.third.tf.dto.CreateInvoiceDTO;
import com.sdk.third.tf.dto.CreateInvoiceResponseDTO;
import com.sdk.third.tf.dto.InvalidInvoiceDTO;
import com.sdk.third.tf.dto.InvalidInvoiceResponseDTO;
import com.sdk.third.tf.dto.InvoiceDetailResponseDTO;
import com.sdk.third.tf.dto.ReturnInvoiceDTO;
import com.sdk.third.tf.dto.ReturnInvoiceResponseDTO;
import com.sdk.third.tf.util.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 发票API客户端
 * 封装发票相关的API调用
 * 
 * @author system
 * @date 2025/01/XX
 */
@Slf4j
public class InvoiceApiClient {

    private static final String PATH_CREATE_INVOICE = "/api/invoice/create";
    private static final String PATH_GET_INVOICE_DETAIL = "/api/invoice/get_detail";
    private static final String PATH_CANCEL_INVOICE = "/api/invoice/cancel";
    private static final String PATH_RETURN_INVOICE = "/api/invoice/return";
    private static final String PATH_INVALID_INVOICE = "/api/invoice/invalid";

    @Autowired
    private TfApiClient tfApiClient;

    /**
     * 开具发票（新接口）
     * 注意：开具发票接口使用公司token（cfg_invoice_setting.token），不是经销商token
     * 
     * @param createInvoiceDTO 开具发票DTO
     * @param companyToken 公司token（cfg_invoice_setting.token），用于Header中的token
     * @param appKey AppKey（用于签名）
     * @return 开具发票响应数据DTO（data部分）
     */
    public CreateInvoiceResponseDTO.CreateInvoiceDataDTO createInvoice(
            CreateInvoiceDTO createInvoiceDTO, String companyToken, String appKey) {
        // 构建请求体
        String requestBody = JSONUtil.toJsonStr(createInvoiceDTO);
        
        // 生成时间戳
        String timestamp = SignUtil.generateTimestamp();
        
        // 生成签名：MD5(AppKey + Path + bodyString + timestamp)
        String sign = SignUtil.generateSign(appKey, PATH_CREATE_INVOICE, requestBody, timestamp);
        
        // 构建Header：sign、timestamp、token（公司token）
        Map<String, String> headers = buildHeaders(sign, timestamp, companyToken);
        
        log.info("开具发票（新接口）, path: {}, timestamp: {}, sign: {}", 
            PATH_CREATE_INVOICE, timestamp, sign);
        
        // 响应格式：{"success": true/false, "message": "...", "data": {...}}
        TypeReference<ApiResponseDTO<CreateInvoiceResponseDTO.CreateInvoiceDataDTO>> typeRef = 
            new TypeReference<ApiResponseDTO<CreateInvoiceResponseDTO.CreateInvoiceDataDTO>>() {};
        
        ApiResponseDTO<CreateInvoiceResponseDTO.CreateInvoiceDataDTO> response = 
            tfApiClient.doPost(PATH_CREATE_INVOICE, requestBody, typeRef, headers);
        
        validateResponse(response);
        return response.getData();
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
     * 查询发票详情
     * 
     * @param uuid 发票UUID
     * @param companyToken 公司token（cfg_invoice_setting.token）
     * @param appKey AppKey（用于签名）
     * @return 发票详情响应数据DTO（data部分）
     */
    public InvoiceDetailResponseDTO.InvoiceDetailDataDTO getInvoiceDetail(String uuid, String companyToken, String appKey) {
        try {
            // 构建查询参数（需要URL编码）
            String encodedUuid = java.net.URLEncoder.encode(uuid, java.nio.charset.StandardCharsets.UTF_8.name());
            String path = PATH_GET_INVOICE_DETAIL + "?uuid=" + encodedUuid;
            
            // 生成时间戳
            String timestamp = SignUtil.generateTimestamp();
            
            // 生成签名：MD5(AppKey + Path + bodyString + timestamp)
            // GET请求bodyString为空
            String sign = SignUtil.generateSign(appKey, path, "", timestamp);
            
            // 构建Header：sign、timestamp、token（公司token）
            Map<String, String> headers = buildHeaders(sign, timestamp, companyToken);
            
            log.info("查询发票详情, path: {}, timestamp: {}, sign: {}", path, timestamp, sign);
            
            // 响应格式：{"success": true, "message": "...", "data": {...}}
            TypeReference<ApiResponseDTO<InvoiceDetailResponseDTO.InvoiceDetailDataDTO>> typeRef = 
                new TypeReference<ApiResponseDTO<InvoiceDetailResponseDTO.InvoiceDetailDataDTO>>() {};
            
            ApiResponseDTO<InvoiceDetailResponseDTO.InvoiceDetailDataDTO> response = 
                tfApiClient.doGet(path, typeRef, headers);
            
            validateDetailResponse(response);
            return response.getData();
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("URL编码失败", e);
        }
    }

    /**
     * 验证API响应
     */
    private void validateResponse(ApiResponseDTO<CreateInvoiceResponseDTO.CreateInvoiceDataDTO> response) {
        if (response == null) {
            throw new RuntimeException("API响应为空");
        }
        
        // 注意：即使success为false，也可能返回data，需要根据实际业务处理
        if (response.getData() == null) {
            String message = response.getMessage() != null ? response.getMessage() : "未知错误";
            throw new RuntimeException("开具发票失败: " + message);
        }
    }

    /**
     * 取消发票（新接口）
     * 注意：取消发票接口使用公司token（cfg_invoice_setting.token），不是经销商token
     * 
     * @param cancelInvoiceDTO 取消发票DTO
     * @param companyToken 公司token（cfg_invoice_setting.token），用于Header中的token
     * @param appKey AppKey（用于签名）
     * @return 取消发票响应数据DTO（data部分）
     */
    public CancelInvoiceResponseDTO.CancelInvoiceDataDTO cancelInvoice(
            CancelInvoiceDTO cancelInvoiceDTO, String companyToken, String appKey) {
        // 构建请求体
        String requestBody = JSONUtil.toJsonStr(cancelInvoiceDTO);
        
        // 生成时间戳
        String timestamp = SignUtil.generateTimestamp();
        
        // 生成签名：MD5(AppKey + Path + bodyString + timestamp)
        String sign = SignUtil.generateSign(appKey, PATH_CANCEL_INVOICE, requestBody, timestamp);
        
        // 构建Header：sign、timestamp、token（公司token）
        Map<String, String> headers = buildHeaders(sign, timestamp, companyToken);
        
        log.info("取消发票（新接口）, path: {}, timestamp: {}, sign: {}", 
            PATH_CANCEL_INVOICE, timestamp, sign);
        
        // 响应格式：{"success": true/false, "message": "...", "data": {...}}
        TypeReference<ApiResponseDTO<CancelInvoiceResponseDTO.CancelInvoiceDataDTO>> typeRef = 
            new TypeReference<ApiResponseDTO<CancelInvoiceResponseDTO.CancelInvoiceDataDTO>>() {};
        
        ApiResponseDTO<CancelInvoiceResponseDTO.CancelInvoiceDataDTO> response = 
            tfApiClient.doPost(PATH_CANCEL_INVOICE, requestBody, typeRef, headers);
        
        validateCancelResponse(response);
        return response.getData();
    }

    /**
     * 验证查询发票详情API响应
     */
    private void validateDetailResponse(ApiResponseDTO<InvoiceDetailResponseDTO.InvoiceDetailDataDTO> response) {
        if (response == null) {
            throw new RuntimeException("API响应为空");
        }
        
        if (response.getData() == null) {
            String message = response.getMessage() != null ? response.getMessage() : "未知错误";
            throw new RuntimeException("查询发票详情失败: " + message);
        }
    }

    /**
     * 退货发票（新接口）
     * 注意：退货发票接口使用公司token（cfg_invoice_setting.token），不是经销商token
     * 
     * @param returnInvoiceDTO 退货发票DTO
     * @param companyToken 公司token（cfg_invoice_setting.token），用于Header中的token
     * @param appKey AppKey（用于签名）
     * @return 退货发票响应数据DTO（data部分）
     */
    public ReturnInvoiceResponseDTO.ReturnInvoiceDataDTO returnInvoice(
            ReturnInvoiceDTO returnInvoiceDTO, String companyToken, String appKey) {
        // 构建请求体
        String requestBody = JSONUtil.toJsonStr(returnInvoiceDTO);
        
        // 生成时间戳
        String timestamp = SignUtil.generateTimestamp();
        
        // 生成签名：MD5(AppKey + Path + bodyString + timestamp)
        String sign = SignUtil.generateSign(appKey, PATH_RETURN_INVOICE, requestBody, timestamp);
        
        // 构建Header：sign、timestamp、token（公司token）
        Map<String, String> headers = buildHeaders(sign, timestamp, companyToken);
        
        log.info("退货发票（新接口）, path: {}, timestamp: {}, sign: {}", 
            PATH_RETURN_INVOICE, timestamp, sign);
        
        // 响应格式：{"success": true/false, "message": "...", "data": {...}}
        TypeReference<ApiResponseDTO<ReturnInvoiceResponseDTO.ReturnInvoiceDataDTO>> typeRef = 
            new TypeReference<ApiResponseDTO<ReturnInvoiceResponseDTO.ReturnInvoiceDataDTO>>() {};
        
        ApiResponseDTO<ReturnInvoiceResponseDTO.ReturnInvoiceDataDTO> response = 
            tfApiClient.doPost(PATH_RETURN_INVOICE, requestBody, typeRef, headers);
        
        validateReturnResponse(response);
        return response.getData();
    }

    /**
     * 验证取消发票API响应
     */
    private void validateCancelResponse(ApiResponseDTO<CancelInvoiceResponseDTO.CancelInvoiceDataDTO> response) {
        if (response == null) {
            throw new RuntimeException("API响应为空");
        }
        
        if (response.getData() == null) {
            String message = response.getMessage() != null ? response.getMessage() : "未知错误";
            throw new RuntimeException("取消发票失败: " + message);
        }
    }

    /**
     * 作废发票（新接口）
     * 注意：作废发票接口使用公司token（cfg_invoice_setting.token），不是经销商token
     * 作废发票一般使用场景为发票号跳号时使用
     * 
     * @param invalidInvoiceDTO 作废发票DTO
     * @param companyToken 公司token（cfg_invoice_setting.token），用于Header中的token
     * @param appKey AppKey（用于签名）
     * @return 作废发票响应数据DTO（data部分）
     */
    public InvalidInvoiceResponseDTO.InvalidInvoiceDataDTO invalidInvoice(
            InvalidInvoiceDTO invalidInvoiceDTO, String companyToken, String appKey) {
        // 构建请求体
        String requestBody = JSONUtil.toJsonStr(invalidInvoiceDTO);
        
        // 生成时间戳
        String timestamp = SignUtil.generateTimestamp();
        
        // 生成签名：MD5(AppKey + Path + bodyString + timestamp)
        String sign = SignUtil.generateSign(appKey, PATH_INVALID_INVOICE, requestBody, timestamp);
        
        // 构建Header：sign、timestamp、token（公司token）
        Map<String, String> headers = buildHeaders(sign, timestamp, companyToken);
        
        log.info("作废发票（新接口）, path: {}, timestamp: {}, sign: {}", 
            PATH_INVALID_INVOICE, timestamp, sign);
        
        // 响应格式：{"success": true/false, "message": "...", "data": {...}}
        TypeReference<ApiResponseDTO<InvalidInvoiceResponseDTO.InvalidInvoiceDataDTO>> typeRef = 
            new TypeReference<ApiResponseDTO<InvalidInvoiceResponseDTO.InvalidInvoiceDataDTO>>() {};
        
        ApiResponseDTO<InvalidInvoiceResponseDTO.InvalidInvoiceDataDTO> response = 
            tfApiClient.doPost(PATH_INVALID_INVOICE, requestBody, typeRef, headers);
        
        validateInvalidResponse(response);
        return response.getData();
    }

    /**
     * 验证退货发票API响应
     */
    private void validateReturnResponse(ApiResponseDTO<ReturnInvoiceResponseDTO.ReturnInvoiceDataDTO> response) {
        if (response == null) {
            throw new RuntimeException("API响应为空");
        }
        
        if (response.getData() == null) {
            String message = response.getMessage() != null ? response.getMessage() : "未知错误";
            throw new RuntimeException("退货发票失败: " + message);
        }
    }

    /**
     * 验证作废发票API响应
     */
    private void validateInvalidResponse(ApiResponseDTO<InvalidInvoiceResponseDTO.InvalidInvoiceDataDTO> response) {
        if (response == null) {
            throw new RuntimeException("API响应为空");
        }
        
        if (response.getData() == null) {
            String message = response.getMessage() != null ? response.getMessage() : "未知错误";
            throw new RuntimeException("作废发票失败: " + message);
        }
    }
}
