package com.erp.server.oms.mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.SoB2cReturnDTO;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.dto.listAddDetailViewDTO;
import com.erp.model.oms.entity.SoB2cReturnDetailEntity;
import com.erp.model.oms.entity.SoB2cReturnEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.oms.dto.SoReturnDTO;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * b2c退货订单 Mapper 接口
 * </p>
 *
 * @author lrp
 * @since 2024-10-09
 */
@Mapper
public interface SoB2cReturnMapper extends BaseMapper<SoB2cReturnEntity> {

    IPage<SoB2cReturnDTO.PagingViewDTO> paging(Page query, SoB2cReturnDTO.PagingParamDTO params);

    List<SoB2cReturnDTO.GenerateSoReturnNoticeView> generateSoReturnNoticeView(@Param("ids") List<String> ids);

    List<SoB2cReturnDTO.BindReturnInstockViewDTO> bindReturnInstockView(@Param("ids")List<String> ids);

    List<SoB2cReturnDetailEntity> listDetailBySoIds(@Param("ids")List<String> soIds);

    List<SoB2cReturnDTO.ReturnLogisticsDTO> selectLogisticsCodePreview(@Param("ids") List<String> ids);

    List<SoB2cReturnDTO.ReturnInstockDTO> selectReturnInstockPreview(@Param("detailIds") List<String> detailIds);

    List<SoDetailDTO.AddDetailView> listAddDetailView(@Param("dto") listAddDetailViewDTO dto);

    /**
     * 预入库-关联售后单：分页查询 B2C 售后单（so_b2c_return），按 sku 过滤
     * @param query 分页
     * @param params 过滤参数
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.oms.dto.SoReturnDTO.LinkAfterSaleView>
     */
    IPage<SoReturnDTO.LinkAfterSaleView> pagingLinkAfterSaleB2C(Page query, @Param("params") SoReturnDTO.LinkAfterSalePagingParam params);

    /**
     * WEGO 退货入库：用参考单号一次查询，按 code/platform_return_no/platform_order_no/so_code OR 匹配，返回优先级最高的首条记录。
     */
    SoB2cReturnEntity findFirstByReferenceNo(@Param("referenceNo") String referenceNo);

    /**
     * 预入库-关联售后单：分页查询 B2C 售后单（so_b2c_return），按 sku 过滤
     * @param query 分页
     * @param params 过滤参数
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.erp.model.oms.dto.SoReturnDTO.LinkAfterSaleView>
     */
    IPage<SoReturnDTO.LinkAfterSaleView> pagingLinkAfterSaleB2C(Page query, @Param("params") SoReturnDTO.LinkAfterSalePagingParam params);
}
