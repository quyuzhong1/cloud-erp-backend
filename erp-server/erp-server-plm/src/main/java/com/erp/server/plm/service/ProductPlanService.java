package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.business.vo.SeriesVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.entity.ProductPlanEntity;
import com.erp.model.plm.vo.ProductPlanGroupVO;
import com.erp.model.plm.vo.ProductPlanStatisticsVO;
import com.erp.model.plm.vo.ProductPlanVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/20 20:05
 */
public interface ProductPlanService extends IService<ProductPlanEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/2/21 10:02
     * @param dto
     * @return PagingVO<List<ProductPlanVO>>
     */
    PagingVO<List<ProductPlanVO>> paging(PagingDTO<ProductPlanSearchDTO> dto);
    /**
     * @description: 查询详情
     * @author Will
     * @date: 2023/2/21 10:02
     * @param id
     * @return ProductPlanDetailsDTO
     */
    ProductPlanDetailsDTO productPlanDetails(String id);
    /**
     * @description: 删除
     * @author Will
     * @date: 2023/2/21 10:04
     * @param id
     * @return Boolean
     */
    Boolean deleteById(String id);
    /**
     * @description: 导入
     * @author Will
     * @date: 2023/2/21 10:11
     * @param excelFile
     * @param response
     * @return Boolean
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);
    /**
     * @description: 导出规划
     * @author Will
     * @date: 2023/2/21 10:30
     * @param productPlanSearchDTO
     * @param response
     * @return Boolean
     */
    Boolean exportProductPlan(ProductPlanSearchDTO productPlanSearchDTO, HttpServletResponse response);
    /**
     * @description: 新增备注
     * @author Will
     * @date: 2023/2/21 10:35
     * @param dto
     * @return Boolean
     */
    Boolean addRemark(ProductPlanRemarkDTO dto);
    /**
     * @description: 规划开发
     * @author Will
     * @date: 2023/2/21 10:55
     * @param dto
     * @return Boolean
     */
    Boolean planDevelopProduct(ProductPlanDevelopDTO dto);
    /**
     * @description: 指标数据
     * @author Will
     * @date: 2023/2/21 11:14
     * @param dto
     * @return List<ProductPlanStatisticsVO>
     */
    List<ProductPlanStatisticsVO> listProductPlanStatistics(ProductPlanGroupSerachDTO dto);
    /**
     * @description: 产品经理表格
     * @author Will
     * @date: 2023/2/21 16:59
     * @param dto 
     * @return List<ProductPlanGroupVO> 
     */
    List<ProductPlanGroupVO> listTableChargeName(ProductPlanGroupSerachDTO dto);
    /**
     * @description: 产品等级表格
     * @author Will
     * @date: 2023/2/21 16:59
     * @param dto 
     * @return List<ProductPlanGroupVO> 
     */
    List<ProductPlanGroupVO> listTableGrade(ProductPlanGroupSerachDTO dto);
    /**
     * @description: 产品分类表格
     * @author Will
     * @date: 2023/2/21 16:59
     * @param dto 
     * @return List<ProductPlanGroupVO> 
     */
    List<ProductPlanGroupVO> listTableCategory(ProductPlanGroupSerachDTO dto);
    /**
     * @description: 立项趋势
     * @author Will
     * @date: 2023/2/21 11:57
     * @param dto
     * @return List<SeriesVO>
     */
    List<SeriesVO> listApprovalTrend(ProductPlanGroupSerachDTO dto);
    /**
     * @description: 图片上传
     * @author Will
     * @date: 2023/2/21 12:08
     * @param multipartFile
     * @param id
     * @return Boolean
     */
    Boolean uploadImageUrl(MultipartFile multipartFile, String id);
    /**
     * @description: 根据年份和产品名称查询
     * @author Will
     * @date: 2023/2/22 14:17
     * @param year
     * @param name
     * @return ProductPlanEntity
     */
    ProductPlanEntity getByYearAndName(Integer year, String name);
    /**
     * @description:
     * @author Will
     * @date: 2023/2/24 11:07
     * @param productId 产品id
     * @param status 状态
     * @param type 1产品，2项目
     */
    void updateProductPlanStatus(String productId, Integer status, Integer type);
    /**
     * @description: 查询所有未关联规划
     * @author Will
     * @date: 2023/2/24 11:49
     * @return List<SelectShowDTO>
     */
    List<SelectShowDTO> listNotRelatedProductPlan();
    /**
     * @description:更新产品规划数据
     * @author Will
     * @date: 2023/2/24 12:10
     * @param productPlanEntity
     * @param productInfoEntity
     */
    void updateProductPlanByProduct(ProductPlanEntity productPlanEntity, ProductInfoEntity productInfoEntity);
    /**
     * @description: 关联产品规划
     * @author Will
     * @date: 2023/2/24 12:15
     * @param productPlanId
     * @param entity
     */
    void relatedProductPlanByProduct(String productPlanId, ProductInfoEntity entity);
    /**
     * @description: 根据productId更新首批入库时间和上市时间
     * @author Will
     * @date: 2023/2/24 15:17
     * @param productId
     */
    void updateRealDateByProductId(String productId);
    /**
     * @description: 产品id
     * @author Will
     * @date: 2023/2/24 16:21
     * @param productId
     */
    void removeProductId(String productId);
}
