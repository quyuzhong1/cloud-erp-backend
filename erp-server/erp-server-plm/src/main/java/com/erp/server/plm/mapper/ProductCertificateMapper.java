package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.ProductCertificateDTO;
import com.erp.model.plm.dto.ProductCertificateShowDTO;
import com.erp.model.plm.entity.ProductCertificateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.erp.model.plm.entity.ProductCertificate
 */
@Mapper
public interface ProductCertificateMapper extends BaseMapper<ProductCertificateEntity> {
    /**
     * @Description 产品证书信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 15:43
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductCostShowDTO>
     **/
    List<ProductCertificateShowDTO> list(@Param("productId") String productId);

    /**
     * @Description 产品证书信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/23 15:43
     * @param skuId
     * @return java.util.List<com.erp.model.plm.dto.ProductCostShowDTO>
     **/
    List<ProductCertificateShowDTO> listBySkuId(@Param("skuId") String skuId);
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2024/2/19 10:55
     * @param query
     * @param params
     * @return IPage<ListDTO>
     */
    IPage<ProductCertificateDTO.ListDTO> paging(Page query,@Param("params") ProductCertificateDTO.SearchParamDTO params);

    /**
     * @description: 导出数据
     * @author Will
     * @date: 2024/2/19 16:50
     * @param params
     * @return List<ListDTO>
     */
    List<ProductCertificateDTO.ListDTO> exportExcel(@Param("params") ProductCertificateDTO.ExportParamDTO params);
    /**
     * @description: 导出数据
     * @author Will
     * @date: 2024/2/19 16:50
     * @param params
     * @return List<ListDTO>
     */
    Page<ProductCertificateDTO.ListDTO> exportExcel(@Param("page") Page<ProductCertificateDTO.ListDTO> page, @Param("params") ProductCertificateDTO.ExportParamDTO params);
}




