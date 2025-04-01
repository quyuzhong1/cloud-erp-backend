package com.erp.server.mrp.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.mrp.dto.CfgRuleSalesEstimateFileDTO;
import com.erp.model.mrp.entity.CfgRuleSalesEstimateFileEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 预估销量文件 服务类
 * </p>
 *
 * @author liao
 * @since 2025-02-19
 */
public interface CfgRuleSalesEstimateFileService extends SuperService<CfgRuleSalesEstimateFileEntity> {

    /**
     * 分页
     * @param params 参数
     */
    PagingVO<CfgRuleSalesEstimateFileDTO.PagingView> filePage(PagingDTO<CfgRuleSalesEstimateFileDTO.PagingParamDTO> params);

    /**
     * 下载导入模板
     * @param response 响应
     */
    void downloadRuleTemplate(HttpServletResponse response);

    /**
     * 导入文件
     *
     * @param excelFile 文件
     * @param platform  平台
     * @param response  响应
     */
    void importFile(MultipartFile excelFile, String platform, HttpServletResponse response);
}
