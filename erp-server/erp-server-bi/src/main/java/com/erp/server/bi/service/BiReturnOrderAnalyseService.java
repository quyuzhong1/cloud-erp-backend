package com.erp.server.bi.service;

import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.dto.ReturnOrderFilterDTO;
import com.erp.model.bi.vo.DateBarAndLineVO;
import com.erp.model.bi.vo.ReturnOrderAnalyseTableVO;

import java.util.List;

/**
 * 退货分析相关/一级模块
 * @Author Luo_WG
 * @Date 2022/12/16 11:07
 **/
public interface BiReturnOrderAnalyseService {

    /**
     * 退货分析-类别
     * @Author Luo_WG
     * @Date 2022/12/19 19:59
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>
     **/
    List<ReturnOrderAnalyseTableVO> returnOrderAnalByCategoryPaging(BiFilterDTO biFilterDTO);

    /**
     * 退货分析-店铺
     * @Author Luo_WG
     * @Date 2022/12/19 11:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>
     **/
    List<ReturnOrderAnalyseTableVO> returnOrderAnalByShopPaging(BiFilterDTO biFilterDTO);

    /**
     * 退货分析-平台
     * @Author Luo_WG
     * @Date 2022/12/19 11:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>
     **/
    List<ReturnOrderAnalyseTableVO> returnOrderAnalByPlatformPaging(BiFilterDTO biFilterDTO);

    /**
     * 退货分析-事业部
     * @Author Luo_WG
     * @Date 2022/12/19 11:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>
     **/
    List<ReturnOrderAnalyseTableVO> returnOrderAnalByDeptPaging(BiFilterDTO biFilterDTO);

    /**
     * 退货分析-日期
     * @Author Luo_WG
     * @Date 2022/12/19 11:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ReturnOrderAnalyseTableVo>
     **/
    DateBarAndLineVO returnOrderAnalByDate(ReturnOrderFilterDTO biFilterDTO);
}