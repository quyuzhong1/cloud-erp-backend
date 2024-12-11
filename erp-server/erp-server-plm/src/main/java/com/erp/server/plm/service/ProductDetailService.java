package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.TaskRefSkuConfigEntity;
import com.erp.model.plm.vo.SkuInfoSimpleVO;
import com.erp.model.plm.vo.SkuSimpleVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.sys.openapi.UploadSkuDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
     * 比较尺寸
     *
     * @param larger   大尺寸
     * @param smaller  小尺寸
     * @param apiError 报错信息
     * @author hyj
     * @date 2024/5/10 9:05
     */
    void compareDimensions(BigDecimal larger, BigDecimal smaller, ApiError apiError);
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
     *
     * @param productSkuExcelDTO productSkuExcelDTO
     * @return void
     * @Author Luo_WG
     * @Date 2022/10/9 11:49
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
    Boolean approvalPass(ProductDetailOperateDTO dto,Boolean isCheck);
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
    /**
     * 更新同步状态
     * @author Will
     * @date: 2023/3/9 9:26
     * @param id
     * @param syncKingdeeId
     */
    Boolean updateSyncKingdeeId(String id, String syncKingdeeId);



    /**
     * 根据skuid 集合获取到sku 信息
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author yl
     * @date 2023-03-21 12:06
     */
    List<SkuVO> getSkuInfoBySkuIds(List<String> skuIds);

    /**
     * 搜索sku 信息
     * @author yl
     * @date 2023-04-03 17:51
     * @param dto
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     */
    List<SkuVO> searchSkuInfo(ProductDetailDTO.SearchDTO dto);

    /**
     * 更改产品状态
     * @author yl
     * @date 2023-06-14 11:12
     * @param productIds
     * @param code
     * @return void
     */
    void updateProductStateByProductIdList(List<String> productIds, Integer code);

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/6/14 15:36
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean submit(List<String> ids,Boolean isCheck);

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/6/14 16:38
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @return java.lang.Boolean
     **/
    BatchResultDTO approve(ProductDetailEntity entity, String type, String comment, Boolean isNeedProcess);

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/6/14 17:18
     * @param entity
     * @return java.lang.Boolean
     **/
    BatchResultDTO disApprove(ProductDetailEntity entity);

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/6/14 17:28
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean cancelProcess(List<String> ids);

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/6/14 17:42
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean deleteBatch(List<String> ids);


    /**
     * 根据产品id 集合获取数据
     * @author yl
     * @date 2023-06-14 18:33
     * @param productIdList
     * @return java.util.List<com.erp.model.plm.entity.ProductDetailEntity>
     */
    List<ProductDetailEntity> listSkuByProductIds(List<String> productIdList);

    /**
     * 批量更新字段
     * @Author Luo_WG
     * @Date 2023/6/15 11:32
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean updateBatchFiled(ProductDetailBatchUpdateDTO dto);

    /**
     * 更新不可删除标识
     * @Author Luo_WG
     * @Date 2023/6/15 11:32
     * @param skuIds skuIds
     * @return java.lang.Boolean
     **/
    Boolean updateOccupyStatus(List<String> skuIds);

    /**
     * 根据skuId查询产品信息-无规格-产品详情-PLM-1.3
     * @Author Luo_WG
     * @Date 2023/6/29 14:21
     * @param skuId
     * @return com.erp.model.plm.dto.ProductNoSpecDetailAllDTO
     **/
    ProductNoSpecDetailAllDTO getNoSpecDetailBySkuId(String skuId);


    /**
     * 获取不参与库存操作的sku
     * @return
     */
    List<SkuVO> getNoInventorySku();
    /**
     * 更新名称
     */
    void updateName(String id, String name);
    /**
     * @description: 根据skuNo集合查询
     * @author Will
     * @date: 2023/8/17 15:19
     * @param skuNos
     * @return List<ProductDetailEntity>
     */
    List<ProductDetailEntity> listBySkuNos(List<String> skuNos);

    /**
     * PDA:条件查询sku
     * @Author Luo_WG
     * @Date 2023/8/21 12:11
     * @param dto
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     **/
    List<SkuVO> pdaSearchSku(ProductDetailDTO.PdaSearchDTO dto);

    /**
     * 根据创建时间获取到对应实体
     * @author yl
     * @date 2023-09-01 12:21
     * @param createTimeList
     * @return java.util.List<com.erp.model.plm.entity.ProductDetailEntity>
     */
    List<ProductDetailEntity> listByCreateTimeList(List<LocalDateTime> createTimeList);

    /**
     * PDA:产品查询
     * @Author Luo_WG
     * @Date 2023/9/4 18:48
     * @param skuNo
     * @return com.erp.model.plm.dto.PdaProductDetailDTO.View
     **/
    PdaProductDetailDTO.View pdaProductView(String skuNo);

    /**
     * 批量修改产品信息
     * @param list
     * @return java.lang.Boolean
     */
    Boolean updateProductDetailBatch(List<ProductDetailEntity> list);
    /**
     * @description: 根据编号查询
     * @author Will
     * @date: 2023/10/24 12:06
     * @param skuParamDTO
     * @return List<ProductSearchDTO.SkuListDTO>
     */
    List<ProductSearchDTO.SkuListDTO> listSkuBySkuNos(ProductSearchDTO.SkuParamDTO skuParamDTO);
    /**
     * @description: 根据负责人id查询
     * @author Will
     * @date: 2023/10/24 18:22
     * @param chargeId
     * @return List<ProductDetailEntity>
     */
    List<ProductDetailEntity> listByChargeId(String chargeId);

    /**
     * 根据id修改产品仓位
     * @Author Luo_WG
     * @Date 2023/11/10 14:25
     * @param id 主键id
     * @param warehouseLocation 仓位信息
     * @return void
     **/
    Boolean updateWarehouseLocationById(String id, String warehouseLocation, String warehouseLocationLarge);

    /**
     * 根据skuNo查询
     * @param skuNoList
     * @return
     */
    List<ProductDetailEntity> listBySkuNoList(List<String> skuNoList);

    /**
     * 搜索SKU
     */
    List<SkuSimpleVO> searchSkuWithCombination(String searchKeyword);

    List<SkuVO> getSkuInfoAdvanceQuery(AdvanceQueryContainer advanceQueryContainer);

    /**
     * 导入产品信息
     * @Author Luo_WG
     * @Date 2024/3/22 10:35
     * @param excelFile
     * @param importType
     * @param response
     * @return java.lang.Boolean
     **/
    Boolean importProductFile(MultipartFile excelFile, Integer importType, HttpServletResponse response);


    /**
     *搜索sku
     * @param pagingDTO
     * @return
     */
    PagingVO<ProductDetailDTO.SkuDTO> listSku(PagingDTO<ProductSkuDTO> pagingDTO);

    /**
     * 根据SkuIds获取SKU简单信息
     * @param skuIds
     * @return
     */
    List<SkuInfoSimpleVO> getSimpleSkuInfoByIds(List<String> skuIds);

    /**
     * 根据skuId获取sku基础信息
     * @param skuIds
     * @return
     */
    List<SkuVO> getSkuBaseByIds(List<String> skuIds);
    /**
     * @description: 远程搜索包装辅料SKU
     * @author Will
     * @date: 2024/4/18 14:17
     * @param searchKeyword
     * @return List<SkuVO>
     */
    List<SkuVO> accessoriesSku(String searchKeyword);

    /**
     * 获取已审核sku 未计算目的国申报价数据
     * @return
     */
    List<ProductDetailEntity> getProductDetailByDestDeclarePrice();

    /**
     * 重算目的国申报单价
     * @param details
     */
    void recalDestDeclarePrice(List<ProductDetailEntity> details);

    /**
     * 历史数据sku 增加默认值 并且把已存在目的国海关编码值移到custom中
     * @param skuIds
     */
    void initProductCustom(List<String> skuIds);

    /**
     * 获取sku 采购信息
     * @param skuIds
     * @return
     */
    List<SkuVO> listSkuPurchaseBySkuIds(List<String> skuIds);
    /**
     * 根据skuid 集合获取到sku基础信息 + 费用信息
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2023-03-21 12:06
     */
    List<SkuVO> listSkuCostByIds(List<String> skuIds);
    /**
     * 根据skuid 集合获取到sku产品信息
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author yl
     * @date 2023-03-21 12:06
     */
    List<SkuVO> listSkuProductByIds(List<String> skuIds);
    /**
     * 根据skuid 集合获取到sku产品信息
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author yl
     * @date 2023-03-21 12:06
     */
    List<SkuVO> listSkuAllAttributeByIds(List<String> skuIds);
    /**
     * 根据skuid 集合获取到sku包装信息 （基础信息+产品信息+包装信息）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2023-03-21 12:06
     */
    List<SkuVO> listSkuPackByIds(List<String> skuIds);
    /**
     * 根据skuid 集合获取到sku销售信息 （基础信息+产品信息+销售信息）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2024-04-25 12:06
     */
    List<SkuVO> listSkuSaleByIds(List<String> skuIds);
    /**
     * 根据skuid 集合获取到sku物流信息 （基础信息+产品信息+物流信息）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2024-04-25 12:06
     */
    List<SkuVO> listSkuLogisticsByIds(List<String> skuIds);
    /**
     * 根据skuid 集合获取到sku分类信息（基础信息+产品信息+分类信息）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2023-03-21 12:06
     */
    List<SkuVO> listSkuCategoryByIds(List<String> skuIds);
    /**
     * 根据skuid 集合获取到sku分类信息（基础信息+产品信息+采购信息）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO>
     * @author zdy
     * @date 2024-04-25 12:06
     */
    List<SkuVO> listSkuPurchaseByIds(List<String> skuIds);

    ProductDetailEntity getBySkuNoOrEan(String skuCode);

    String dimensionalWeightMeasure(DimensionalWeightDTO dto);
    void initProductToWangDian(List<String> ids);
    PagingVO<SkuVO> pagingSelect(PagingDTO<SkuVO.SelectDTO> dto);

    /**
     * 添加已有sku到现有spu
     * @param changeSkuToSpuDTO
     * @return
     */
    List<ProductDetailEntity> changeSkuBySpu(ChangeSkuToSpuDTO changeSkuToSpuDTO);

    /**
     * 根据skuid 集合获取到sku信息（产品信息+项目经理+产品经理）
     *
     * @param skuIds
     * @return java.util.List<com.erp.model.plm.vo.SkuVO.ProductChargeInfoDTO>
     * @author jack
     * @date 2024-09-20
     */
    List<SkuVO.ProductChargeInfoDTO> listProductChargeInfoByIds(List<String> skuIds);

    /**
     * 获取已审核，已上市数据
     */
    List<SkuVO> listApproveAndListingSku();

    /**
     * 获取skuId
     */
    List<String> getCategoryByQuerySql(String compareCodeSplicingValueSql);
    /**
     * 获取skuId
     */
    List<String> getBrandByQuerySql(String compareCodeSplicingValueSql);


    /**
     * 打印EAN
     * @param printEanDTO 打印参数
     * @param response    响应
     */
    void printEan(PrintEanDTO printEanDTO, HttpServletResponse response);

    void uploadSkuImage(UploadSkuDTO dto);

    /**
     * 根据skuIds获取产品包装尺寸明细
     * @param ids skuIds
     */
    List<ProductPackViewDTO> listProductPackBySkuIds(List<String> ids);
    /**
     * 修改产品包装尺寸
     * @param viewDTO 参数
     */
    BatchResultDTO updateProductPack(ProductPackViewDTO viewDTO);
}
