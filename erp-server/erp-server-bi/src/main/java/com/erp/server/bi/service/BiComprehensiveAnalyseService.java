package com.erp.server.bi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.dto.SkuDateFilterDTO;
import com.erp.model.bi.dto.SkuFilterDTO;
import com.erp.model.bi.vo.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BiComprehensiveAnalyseService {

    /**
     * SKU矩阵
     * @Author Luo_WG
     * @Date 2022/12/26 10:42
     * @param biFilterDTO biFilterDTO
     * @return com.erp.common.vo.PagingVO<com.erp.model.bi.vo.SkuMatrixVO>
     **/
    PagingVO<MatrixVO> skuMatrix(PagingDTO<BiFilterDTO> biFilterDTO);

    /**
     * 店铺矩阵
     * @Author Luo_WG
     * @Date 2022/12/26 10:42
     * @param biFilterDTO biFilterDTO
     * @return com.erp.common.vo.PagingVO<com.erp.model.bi.vo.SkuMatrixVO>
     **/
    PagingVO<MatrixVO> shopMatrix(PagingDTO<BiFilterDTO> biFilterDTO);

    /**
     * 平台店铺对比趋势
     * @Author Luo_WG
     * @Date 2022/12/26 10:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ShopContrastTrendVO>
     **/
    List<ShopContrastTrendVO> shopContrastTrend(BiFilterDTO biFilterDTO);

    /**
     * 品类矩阵
     * @Author Luo_WG
     * @Date 2022/12/26 10:35
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.ShopContrastTrendVO>
     **/
    PagingVO<MatrixVO> categoryMatrix(PagingDTO<BiFilterDTO> biFilterDTO);

    /**
     * 销售明细表-SKU
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SaleDetailVO> saleDetailSku(BiFilterDTO biFilterDTO);

    /**
     * 销售明细表-店铺
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SaleDetailVO> saleDetailShop(BiFilterDTO biFilterDTO);

    /**
     * 销售明细表-用户
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SaleDetailVO> saleDetailUser(BiFilterDTO biFilterDTO);

    /**
     * 销售明细表-日期
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SaleDetailVO> saleDetailDate(SkuFilterDTO biFilterDTO);

    /**
     * SKU日期销售额趋势图
     * @Author Luo_WG
     * @Date 2022/12/27 10:41
     * @param biFilterDTO biFilterDTO
     * @return java.util.List<com.erp.model.bi.vo.SaleDetailVO>
     **/
    List<SkuDateSaleTrendVO> skuDateSaleTrend(SkuDateFilterDTO biFilterDTO);
}
