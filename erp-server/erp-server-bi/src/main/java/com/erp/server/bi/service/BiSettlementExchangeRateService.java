package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.BiSettlementExchangeRateDTO;
import com.erp.model.bi.entity.BiSettlementExchangeRateEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/19 10:16
 */
public interface BiSettlementExchangeRateService  extends IService<BiSettlementExchangeRateEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/8/15 10:25
     * @param pagingDTO
     * @return PagingVO<ListDTO>
     */
    PagingVO<BiSettlementExchangeRateDTO.ListDTO> paging(PagingDTO<BiSettlementExchangeRateDTO.SearchParamDTO> pagingDTO);
    /**
     * @description: 界面回显
     * @author Will
     * @date: 2022/12/20 18:49
     * @return List<Map<String>>
     */
    List<Map<String, Object>> listSettlementExchangeRate();
    /**
     * @description: 根据金蝶id查询
     * @author Will
     * @date: 2023/8/14 18:11
     * @param kingdeeId
     * @return BiSettlementExchangeRateEntity
     */
    BiSettlementExchangeRateEntity getByKingdeeId(String kingdeeId);
    /**
     * @description: 金蝶拉取代码新增
     * @author Will
     * @date: 2023/8/14 18:36
     * @param addDTO
     * @return String
     */
    String add(BiSettlementExchangeRateDTO.AddDTO addDTO);
    /**
     * @description: 金蝶拉取代码修改
     * @author Will
     * @date: 2023/8/14 18:36
     * @param updateDTO
     * @return Boolean
     */
    Boolean update(BiSettlementExchangeRateDTO.UpdateDTO updateDTO);
    /**
     * @description: 提交
     * @author Will
     * @date: 2023/8/14 18:40
     * @param ids
     * @return Boolean
     */
    Boolean submit(List<String> ids);

    /**
     * @description: 审核
     * @author Will
     * @date: 2023/8/14 18:40
     * @param baseApproveParamDTO
     * @return Boolean
     */
    Boolean approve(BaseApproveParamDTO baseApproveParamDTO);

    /**
     * @description: 反审核
     * @author Will
     * @date: 2023/8/14 18:48
     * @param ids
     * @return Boolean
     */
    Boolean disApprove(List<String> ids);
    /**
     * @description: 根据币制和日期查询汇率信息
     * @author Will
     * @date: 2023/8/24 17:45
     * @param date
     * @param sourceCurrencyCode
     * @return BigDecimal
     */
    BigDecimal findByCurrencyAndDate(String date, String sourceCurrencyCode);

    /**
     * 根据日期、目标币别、来源币别查询汇率信息
     * @author will
     * @date 2024/8/7 17:27
     * @param date
     * @param targetCurrencyCode
     * @param sourceCurrencyCode
     * @return BigDecimal
     */
    BigDecimal listRedisByCurrencyCode (String date, String targetCurrencyCode, String sourceCurrencyCode);

    void syncLastestRateToDht();
}
