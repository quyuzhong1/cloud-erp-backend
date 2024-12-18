package com.erp.server.mrp.service;
import com.erp.model.mrp.dto.CalcSalesInfoFavoriteDTO;
import com.erp.model.mrp.entity.CfgRuleCalcEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.mrp.dto.CfgRuleCalcDTO;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 试算配置 服务类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
public interface CfgRuleCalcService extends SuperService<CfgRuleCalcEntity> {

    /**
     * 新增
     *
     * @param dto 参数
     * @author liaohui
     * @date: 2024-11-11
     */
    BatchResultDTO add(CfgRuleCalcDTO.AddDTO dto);

    /**
     * 下载历史销量
     * @param dto 参数
     */
    void downloadHistorySales(CfgRuleCalcDTO.DownloadDTO dto);

    /**
     * 下载模板
     */
    void downloadRuleTemplate(HttpServletResponse response);

    /**
     * 查看详情
     * @param id id
     */
    CfgRuleCalcDTO.ViewDTO view(String id);

    /**
     * 添加关注
     *
     * @param dto 参数
     */
    void addFavorite(CalcSalesInfoFavoriteDTO.AddDTO dto);

    /**
     *
     * 取消关注
     * @param dto 参数
     */
    void cancelFavorite(CalcSalesInfoFavoriteDTO.CancelDTO dto);
}
