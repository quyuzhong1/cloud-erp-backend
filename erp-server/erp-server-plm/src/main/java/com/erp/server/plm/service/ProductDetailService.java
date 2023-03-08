package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.TaskRefSkuConfigEntity;
import com.erp.model.plm.vo.SkuVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface ProductDetailService extends IService<ProductDetailEntity> {

    /**
     * @Description 产品信息查询列表
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param pagingDTO:查询参数
     * @return PagingVO
     **/
    PagingVO<ProductDetailShowDTO> paging(PagingDTO<ProductSkuDTO> pagingDTO);

    /**
     * @Description 条件查询产品信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:28
     * @param name:产品名称
     * @return ProductDetailShowDTO
     **/
    ProductDetailShowDTO getProductBy(String name, String skuNo);

    /**
     * @Description 无规格产品信息明细
     * @Author Luo_WG
     * @Date 2022/9/22 12:05
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductNoDetailDTO>
     **/
    ProductNoSpecDetailAllDTO getNoSpecDetailById(String productId);

    /**
     * @Description 多规格产品信息明细
     * @Author Luo_WG
     * @Date 2022/9/22 12:05
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.dto.ProductVariantShowDTO>
     **/
    ProductManyDetailDTO getManySpecDetailById(String productId);

    /**
     * @Description 保存/修改产品sku信息表数据
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productSkuBaseInfoDTO 新增产品无规格sku信息请求参数
     * @return java.lang.String
     **/
    String saveOrUpdate(ProductSkuBaseInfoDTO productSkuBaseInfoDTO);

    /**
     * @Description 保存/修改产品sku信息表数据-批量
     * @Author Luo_WG
     * @Date 2022/9/23 10:13
     * @param productDetailList 新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdateBatch(List<ProductDetailDTO> productDetailList);

    /**
     * @Description 新增无规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/21 16:31
     * @param productNoSpecDTO:新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdateNoSpec(ProductNoSpecDTO productNoSpecDTO);

    /**
     * @Description 新增多规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:52
     * @param productManySpecDTO:新增产品多规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdateManySpec(ProductManySpecDTO productManySpecDTO);

    /**
     * @Description 多规格自动生成
     * @Author Luo_WG
     * @Date 2022/9/26 14:54
     * @param variantAutoAddDTO:自动生成请求参数
     * @return java.lang.Boolean
     **/
    List<ProductDetailEntity> insertManySpecAuto(VariantAutoAddDTO variantAutoAddDTO);

    /**
     * @Description 删除多规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 11:32
     * @param skuId:产品sku表主键id
     * @return java.lang.Boolean
     **/
    Boolean delete(String skuId);

    /**
     * @Description 根据产品id删除产品信息
     * @Author Luo_WG
     * @Date 2022/9/22 11:32
     * @param id:产品sku表主键id
     * @return java.lang.Boolean
     **/
    Boolean deleteByProductId(String id);

    /**
     * @Description 删除多规格sku信息-批量
     * @Author Luo_WG
     * @Date 2022/9/22 11:32
     * @param skuIds:产品sku表主键id
     * @return java.lang.Boolean
     **/
    Boolean deleteBatch(List<String> skuIds);

    /**
     * @Description 根据产品主键id查询sku明细
     * @Author Luo_WG
     * @Date 2022/9/26 18:25
     * @param productId:产品信息表id
     * @return java.util.List<com.erp.model.plm.entity.ProductDetailEntity>
     **/
    List<ProductDetailEntity> queryByProductId(String productId);

    /**
     * @Description 检查sku是否重复-集合
     * @Author Luo_WG
     * @Date 2022/9/27 9:17
     * @param skuList:sku集合
     **/
    Boolean checkSkuNos(List<String> skuList);

    /**
     * @Description 检查sku是否重复
     * @Author Luo_WG
     * @Date 2022/9/27 9:17
     * @param sku sku
     **/
    Boolean checkSkuNo(String sku, String id);

    /**
     * @Description 检查spu编号是否重复
     * @Author Luo_WG
     * @Date 2022/9/27 9:28
     * @param spuNo spu编号
     * @param id 主键id
     * @return void
     **/
    Boolean checkSpuNo(String spuNo, String id);

    /**
     * @Description 检查spu编号是否重复
     * @Author Luo_WG
     * @Date 2022/9/27 9:28
     * @param name spu编号
     * @param id 主键id
     * @return void
     **/
    Boolean checkName(String name, String id);


    /**
     * @Description 根据sku查询sku表信息
     * @Author Luo_WG
     * @Date 2022/9/28 17:04
     * @param sku：sku
     * @return com.erp.model.plm.entity.ProductDetailEntity
     **/
    ProductDetailEntity getProductIdBySku(String sku);

    /**
     * @Description 根据sku查询sku表信息(数据清洗)
     * @Author Luo_WG
     * @Date 2022/9/28 17:04
     * @param sku：sku
     * @return com.erp.model.plm.entity.ProductDetailEntity
     **/
    CleanSkuDto getProductIdBySkuClean(String sku);
    
    /**
     * 方法说明
     * @author yl
     * @date 2022-11-21 17:13
     * @param productId
     * @return 
     */
    List<ProductDetailEntity> getSkuListByProductId(String productId);
    /**
     * 获取配置字段
     */
    List<String> getByFileldFlag(String flag, List<TaskRefSkuConfigEntity> refSkuFiledConfigList);
    /**
     * @Description 新增无规格sku信息
     * @Author Luo_WG
     * @Date 2022/9/22 10:55
     * @param productNoSpecDTO:新增产品无规格sku信息请求参数
     * @return java.lang.Boolean
     **/
    Boolean inportExcel(ProductNoSpecDTO productNoSpecDTO);

    /**
     * 导出excel的sku数据
     * @Author Luo_WG
     * @Date 2022/10/9 11:49
     * @param productSkuExcelDTO productSkuExcelDTO
     * @param response response
     * @return void
     **/
    void exportProduct(ProductSkuExcelDTO productSkuExcelDTO, HttpServletResponse response);

    /**
     * 根据sku id集合
     * @author yl
     * @date 2022-11-24 9:20
     * @param skuIdList
     * @return java.util.List<com.erp.model.plm.entity.ProductDetailEntity>
     */
    List<ProductDetailEntity> getByIdList(List<String> skuIdList);

    List<BaseIdDTO> getNotFinish(List<String> skuIdList);
    /**
     * @description: 审核通过
     * @author Will
     * @date: 2022/11/28 14:51
     * @param dto
     * @return Boolean
     */
    Boolean approvalPass(ProductDetailOperateDTO dto);
    /**
     * @description: 审核不通过
     * @author Will
     * @date: 2022/11/28 14:51
     * @param dto
     * @return Boolean
     */
    Boolean approvalReject(ProductDetailOperateDTO dto);
    /**
     * @description: 设置审批人
     * @author Will
     * @date: 2022/11/28 16:43
     * @param dto
     * @return Boolean
     */
    Boolean updateApprover(ProductDetailApproveParamDTO dto);
    /**
     * @description: 审核完成
     * @author Will
     * @date: 2022/11/30 17:22
     * @param processId

     */
    Boolean productDetailProcessPass(String processId);
    /**
     * @description: 申请变更
     * @author Will
     * @date: 2022/12/1 15:41
     * @param id
     * @return Boolean
     */
    Boolean applyChange(String id);
    /**
     * @description: 反审核
     * @author Will
     * @date: 2022/12/1 16:56
     * @param id
     * @return Boolean
     */
    Boolean deApprove(String id);
    /**
     * @description: 重启审核流程
     * @author Will
     * @date: 2022/12/7 16:30
     * @param dto
     * @return Boolean
     */
    Boolean restartProcessPass(ProductDetailOperateDTO dto);
    /**
     * @description: 根据sku参数查询
     * @author Will
     * @date: 2022/12/26 11:56
     * @param params (id,skuNo)
     * @return ProductDetailDTO
     */
    ProductDetailDTO getSkuByParam(Map<String, String> params);


    /**
     * 根据sku 编号 获取sku 信息以及对应的spu 信息
     * @param skuNoList
     * @return
     */
    List<SkuVO> getSkuBySkuNos(List<String> skuNoList);


    /**
     * 根据sku 编号 获取sku 信息以及对应的spu 信息
     * @param skuIdList
     * @return
     */
    List<SkuVO> getSkuBySkuIds(List<String> skuIdList);

    /**
     * 搜索sku 信息
     * @param searchKeyword
     * @return
     */
    List<SkuVO> searchSku(String searchKeyword);

    /**
     *  获取 审核通过 的sku 信息
     * @param searchKeyword
     * @return
     */
    List<ChangeInfoDTO> getSku(String searchKeyword);

    /**
     * 根据skuId 获取到sku 单位最小的信息 在bom 和变更那边会用到
     * @author yl
     * @date 2023-01-29 14:11
     * @param skuId
     * @return com.erp.model.plm.dto.ProductSmallestUnitDTO
     */
    ProductSmallestUnitDTO getSkuBySkuId(String skuId);

    
    /**
     * 变更管理 审核通过后
     * 变更sku
     * @author yl
     * @date 2023-01-30 17:10
     * @param sku
     * @return void
     */
    void changeSku(ProductSmallestUnitDTO sku);

    
    /**
     * 根据sku ids 获取到产品经理
     * @author yl
     * @date 2023-02-01 17:24
     * @param skuIdList
     * @return java.util.List<java.lang.String>
     */
    List<String> getManagerBySkuIds(List<String> skuIdList);


    /**
     * 根据部门名称获取到对应的领导
     * @author yl
     * @date 2023-02-01 17:49
     * @param secondDeptName
     * @return java.util.List<java.lang.String>
     */
    List<String> getApproveLead(String secondDeptName);
    /**
     * @description: 根据产品id更新产品开发状态
     * @author Will
     * @date: 2023/2/2 14:53
     * @param productId
     * @param state
     */
    void updateProductStateByProductId(String productId, Integer state);
    /**
     * @description: 提交
     * @author Will
     * @date: 2023/2/9 13:34
     * @param id
     * @return Boolean
     */
    Boolean commit(String id);
    /**
     * @description: 取消流程
     * @author Will
     * @date: 2023/2/9 14:37
     * @param id
     * @return Boolean
     */
    Boolean unCommit(String id);
    /**
     * @description: 发送金蝶数据
     * @author Will
     * @date: 2023/2/13 13:27
     * @param id
     * @return Boolean
     */
    Boolean sendKingDeeData(String id);
    /**
     * @description: 处理负责人id
     * @author Will
     * @date: 2023/3/2 17:34
     */
    void handleChargeId();
    /**
     * @description: 查询审核通过的负责人
     * @author Will
     * @date: 2023/3/7 14:12
     * @return List<ProductDetailEntity>
     */
    List<ProductDetailEntity> listByAuditPass();
    /**
     * @description: 搜索父级sku
     * @author Will
     * @date: 2023/3/7 20:07
     * @param searchKeyword
     * @return List<SkuVO>
     */
    List<SkuVO> searchParentSku(String searchKeyword,String bomId);
}
