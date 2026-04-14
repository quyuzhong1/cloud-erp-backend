package com.erp.server.dmp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.BiSettlementExchangeRateDTO;
import com.erp.model.dmp.entity.BiSettlementExchangeRateEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @date 2022/12/19 10:16
 */
public interface BiSettlementExchangeRateService extends IService<BiSettlementExchangeRateEntity> {

    /**
     * 根据日期和原币种查询汇率
     *
     * @param date
     * @param sourceCurrencyCode
     * @return
     */
    BigDecimal findByCurrencyAndDate(String date, String sourceCurrencyCode);

    BigDecimal findByCurrencyAndMonth(String date, String sourceCurrencyCode);

    /**
     * 根据日期、目标币别、来源币别查询汇率信息
     *
     * @param date
     * @param targetCurrencyCode
     * @param sourceCurrencyCode
     * @return BigDecimal
     * @author will
     * @date 2024/8/7 17:12
     */
    BigDecimal listRedisByCurrencyCode(String date, String targetCurrencyCode, String sourceCurrencyCode);

    /**
     * @param pagingDTO
     * @return PagingVO<ListDTO>
     * @description: 分页查询
     * @author Will
     * @date: 2023/8/15 10:25
     */
    PagingVO<BiSettlementExchangeRateDTO.ListDTO> paging(PagingDTO<BiSettlementExchangeRateDTO.SearchParamDTO> pagingDTO);

    /**
     * @return List<Map < String>>
     * @description: 界面回显
     * @author Will
     * @date: 2022/12/20 18:49
     */
    List<Map<String, Object>> listSettlementExchangeRate();

    /**
     * @param kingdeeId
     * @return BiSettlementExchangeRateEntity
     * @description: 根据金蝶id查询
     * @author Will
     * @date: 2023/8/14 18:11
     */
    BiSettlementExchangeRateEntity getByKingdeeId(String kingdeeId);

    /**
     * @param addDTO
     * @return String
     * @description: 金蝶拉取代码新增
     * @author Will
     * @date: 2023/8/14 18:36
     */
    String add(BiSettlementExchangeRateDTO.AddDTO addDTO);

    /**
     * @param updateDTO
     * @return Boolean
     * @description: 金蝶拉取代码修改
     * @author Will
     * @date: 2023/8/14 18:36
     */
    Boolean update(BiSettlementExchangeRateDTO.UpdateDTO updateDTO);

    /**
     * @param ids
     * @return Boolean
     * @description: 提交
     * @author Will
     * @date: 2023/8/14 18:40
     */
    Boolean submit(List<String> ids);

    /**
     * @param baseApproveParamDTO
     * @return Boolean
     * @description: 审核
     * @author Will
     * @date: 2023/8/14 18:40
     */
    Boolean approve(BaseApproveParamDTO baseApproveParamDTO);

    /**
     * @param ids
     * @return Boolean
     * @description: 反审核
     * @author Will
     * @date: 2023/8/14 18:48
     */
    Boolean disApprove(List<String> ids);

    void syncLastestRateToDht();
}
