package com.sdk.third.tf.client;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.TypeReference;
import com.sdk.third.tf.dto.TaxCategoryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Autowired
    private TfApiClient tfApiClient;

    /**
     * 创建税种
     * 
     * @param createDTO 创建税种DTO
     * @return 创建响应DTO
     */
    public TaxCategoryDTO.CreateCategoryResponseDTO createCategory(TaxCategoryDTO.CreateCategoryDTO createDTO) {
        String path = "/api/category/create";
        String requestBody = JSONUtil.toJsonStr(createDTO);
        
        TypeReference<TaxCategoryDTO.ApiResponseDTO<TaxCategoryDTO.CreateCategoryResponseDTO>> typeRef = 
            new TypeReference<TaxCategoryDTO.ApiResponseDTO<TaxCategoryDTO.CreateCategoryResponseDTO>>() {};
        
        TaxCategoryDTO.ApiResponseDTO<TaxCategoryDTO.CreateCategoryResponseDTO> response = 
            tfApiClient.doPost(path, requestBody, typeRef);
        
        validateResponse(response);
        return response.getData();
    }

    /**
     * 查询税种列表
     * 
     * @param page 页码
     * @param pageSize 每页大小
     * @return 税种列表响应DTO
     */
    public TaxCategoryDTO.CategoryListResponseDTO getCategoryList(Integer page, Integer pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("page", page != null ? page : 1);
        params.put("page_size", pageSize != null ? pageSize : 10);
        
        String path = "/api/category/get_list" + TfApiClient.buildQueryString(params);
        
        TypeReference<TaxCategoryDTO.ApiResponseDTO<TaxCategoryDTO.CategoryListResponseDTO>> typeRef = 
            new TypeReference<TaxCategoryDTO.ApiResponseDTO<TaxCategoryDTO.CategoryListResponseDTO>>() {};
        
        TaxCategoryDTO.ApiResponseDTO<TaxCategoryDTO.CategoryListResponseDTO> response = 
            tfApiClient.doGet(path, typeRef);
        
        validateResponse(response);
        return response.getData();
    }

    /**
     * 查询税种详情
     * 
     * @param categoryId 税种ID
     * @return 税种详情DTO
     */
    public TaxCategoryDTO.CategoryDetailDTO getCategoryDetail(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("税种ID不能为空");
        }
        
        String path = "/api/category/get_detail?category_id=" + categoryId;
        
        TypeReference<TaxCategoryDTO.ApiResponseDTO<TaxCategoryDTO.CategoryDetailDTO>> typeRef = 
            new TypeReference<TaxCategoryDTO.ApiResponseDTO<TaxCategoryDTO.CategoryDetailDTO>>() {};
        
        TaxCategoryDTO.ApiResponseDTO<TaxCategoryDTO.CategoryDetailDTO> response = 
            tfApiClient.doGet(path, typeRef);
        
        validateResponse(response);
        return response.getData();
    }

    /**
     * 编辑税种
     * 
     * @param editDTO 编辑税种DTO
     * @return 编辑响应DTO
     */
    public TaxCategoryDTO.EditCategoryResponseDTO editCategory(TaxCategoryDTO.EditCategoryDTO editDTO) {
        String path = "/api/category/edit";
        String requestBody = JSONUtil.toJsonStr(editDTO);
        
        TypeReference<TaxCategoryDTO.ApiResponseDTO<TaxCategoryDTO.EditCategoryResponseDTO>> typeRef = 
            new TypeReference<TaxCategoryDTO.ApiResponseDTO<TaxCategoryDTO.EditCategoryResponseDTO>>() {};
        
        TaxCategoryDTO.ApiResponseDTO<TaxCategoryDTO.EditCategoryResponseDTO> response = 
            tfApiClient.doPost(path, requestBody, typeRef);
        
        validateResponse(response);
        return response.getData();
    }

    /**
     * 删除税种
     * 
     * @param categoryId 税种ID
     */
    public void deleteCategory(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("税种ID不能为空");
        }
        
        String path = "/api/category/delete";
        TaxCategoryDTO.DeleteCategoryDTO deleteDTO = new TaxCategoryDTO.DeleteCategoryDTO(categoryId);
        String requestBody = JSONUtil.toJsonStr(deleteDTO);
        
        TypeReference<TaxCategoryDTO.ApiResponseDTO<Object>> typeRef = 
            new TypeReference<TaxCategoryDTO.ApiResponseDTO<Object>>() {};
        
        TaxCategoryDTO.ApiResponseDTO<Object> response = 
            tfApiClient.doPost(path, requestBody, typeRef);
        
        validateResponse(response);
    }

    /**
     * 验证API响应
     */
    private void validateResponse(TaxCategoryDTO.ApiResponseDTO<?> response) {
        if (response == null) {
            throw new RuntimeException("API响应为空");
        }
        
        if (!response.isSuccess()) {
            String message = response.getMessage() != null ? response.getMessage() : "未知错误";
            throw new RuntimeException("API请求失败: " + message);
        }
    }
}
