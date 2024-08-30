package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProductCertificateDTO;
import com.erp.model.plm.dto.ProductCertificateShowDTO;
import com.erp.model.plm.entity.ProductCertificateEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Description 产品证书信息服务类
 * @Author Luo_WG
 * @Date 2022/9/23 15:35
 **/
public interface ProductCertificateService extends IService<ProductCertificateEntity> {
    /**
     * @Description 产品证书信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductCertificateShowDTO>
     **/
    List<ProductCertificateShowDTO> list(String productId);

    /**
     * @Description 产品证书信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param skuId
     * @return java.util.List<com.erp.model.plm.dto.ProductCertificateShowDTO>
     **/
    List<ProductCertificateShowDTO> listBySkuId(String skuId);

    /**
     * @description: 分页查询
     * @author Will
     * @date: 2024/2/19 10:52
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<ProductCertificateDTO.ListDTO> paging(PagingDTO<ProductCertificateDTO.SearchParamDTO> dto);

    /**
     * @description: 导出
     * @author Will
     * @date: 2024/2/19 14:51
     * @param dto
     * @return Boolean 
     */
    Boolean exportExcel(ProductCertificateDTO.ExportParamDTO dto);

    /**
     * @description: 新增
     * @author Will
     * @date: 2024/2/19 14:56
     * @param dto
     */
    void add(ProductCertificateDTO.AddDTO dto);

    /**
     * @description: 修改
     * @author Will
     * @date: 2024/2/19 14:56
     * @param dto
     * @return Boolean
     */
    Boolean update(ProductCertificateDTO.UpdateDTO dto);
    /**
     * @description: 查询详情
     * @author Will
     * @date: 2024/2/19 14:56
     * @param id
     * @return ViewDTO
     */
    ProductCertificateDTO.ViewDTO view(String id);
    /**
     * @description: 删除
     * @author Will
     * @date: 2024/2/19 14:58
     * @param ids
     * @return Boolean
     */
    Boolean delete(List<String> ids);
    /**
     * @description: 导入
     * @author Will
     * @date: 2024/2/20 10:00
     * @param excelFile
     * @param response
     * @return Boolean
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);
    /**
     * @description: 产品信息新增或修改
     * @author Will
     * @date: 2024/2/21 10:49
     * @param productCertificateList
     */
    void productAddOrUpdate(List<ProductCertificateDTO.ProductAddOrUpdateDTO> productCertificateList);
    /**
     * @description: 根据skuid集合删除
     * @author Will
     * @date: 2024/2/21 11:25
     * @param skuIdList
     */
    void deleteBySkuIdList(List<String> skuIdList);

    /**
     * @description: 根据skuId集合查询
     * @author Will
     * @date: 2024/2/21 11:48
     * @param skuIdList
     * @return List<ProductCertificateEntity>
     */
    List<ProductCertificateEntity> listBySkuIdList(List<String> skuIdList);

    /**
     * 导出
     * @param dto 导出
     */
    PagingVO<ProductCertificateDTO.ListDTO> exportProductCertificate(PagingDTO<ProductCertificateDTO.ExportParamDTO> dto);
}
