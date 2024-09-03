package com.erp.server.mrp.service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 补货建议导入导出 服务类
 * </p>
 *
 * @author will
 * @since 2024-08-30
 */
public interface ReplenishmentSuggestionImportService {

    /**
     * 补货规则导入模板
     * @author will
     * @date 2024/8/30 16:40
     * @param response
     */
    void downloadRuleTemplate(HttpServletResponse response);
    /**
     * 导入补货规则
     * @author will
     * @date 2024/8/30 16:49
     * @param excelFile
     * @param response
     * @return Boolean
     */
    void importRule(String id,MultipartFile excelFile, HttpServletResponse response);
    /**
     * 运营预估月销导入模板
     * @author will
     * @date 2024/8/30 16:42
     * @param response
     */
    void downloadSalesEstimateTemplate(HttpServletResponse response);
    /**
     * 导入运营预估月销
     * @author will
     * @date 2024/8/30 16:49
     * @param excelFile
     * @param response
     * @return Boolean
     */
    void importSalesEstimate(MultipartFile excelFile, HttpServletResponse response);

}
