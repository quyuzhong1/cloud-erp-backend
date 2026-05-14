package com.erp.server.tms.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.TmsDeclareBillDetailEntity;
import com.erp.model.tms.entity.TmsDeclareBillEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 报关单 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-03-27
 */
public interface TmsDeclareBillService extends SuperService<TmsDeclareBillEntity> {

    /**
    * 修改
    * @author lrp
    * @date: 2024-03-27
    * @param dto
    * @return
    */
    Boolean update(TmsDeclareBillDTO.UpdateDTO dto,SourceTypeEnum sourceTypeEnum);


    List<TmsDeclareBillDTO.TabListDTO> tabList(SourceTypeEnum sourceTypeEnum,PermissionsDTO permissionsDTO);

    PagingVO<TmsDeclareBillDTO.PagingVO> paging(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto);

    TmsDeclareBillDTO.StatisticsVO statisticsByFm(PermissionsDTO permissionsDTO);

    List<TmsDeclareBillDTO.DeliveryDTO> getCanGenerateDeliveryOrder(TmsDeclareBillDTO.QuerySourceDTO querySourceDTO);

    /**
     * 根据选中SKU查询报关表头信息
     * @author will
     * @date 2026/5/7 14:47
     * @param dto
     * @param sourceTypeEnum
     * @return com.erp.model.tms.dto.TmsDeclareBillDTO.SelectedSkuHeaderDTO
     */
    TmsDeclareBillDTO.SelectedSkuHeaderDTO querySelectedSkuHeader(TmsDeclareBillDTO.SelectedSkuHeaderParamDTO dto, SourceTypeEnum sourceTypeEnum);

    TmsDeclareBillDTO.ViewDTO view(String id);

    BatchResultDTO confirmDeclareStatus(TmsDeclareBillDTO.ConfirmDeclareStatusDTO dto, SourceTypeEnum sourceTypeEnum);

    List<BatchResultDTO> delete(TmsDeclareBillDTO.DeleteDTO dto);

    Boolean addFmDeclare(TmsDeclareBillDTO.AddDTO dto);

    /**
     * 根据来源id查询报关单
     * @Author Luo_WG
     * @Date 2024/4/1 12:05
     * @param sourceIds
     * @return java.util.List<com.erp.model.tms.entity.TmsDeclareBillEntity>
     **/
    List<TmsDeclareBillEntity> listBySourceIds(List<String> sourceIds);

    List<TmsDeclareBillDTO.SoOutDTO> getCanGenerateSoOut(TmsDeclareBillDTO.QuerySourceDTO querySourceDTO);

    TmsDeclareBillDTO.StatisticsVO statisticsBySoOut(PermissionsDTO permissionsDTO);

    Boolean addB2BDeclare(TmsDeclareBillDTO.AddDTO dto);

    BaseResultDTO.AddDTO add(TmsDeclareBillEntity tmsDeclareBillEntity,
                             List<TmsDeclareBillDetailEntity> detailEntityList,
                             SourceTypeEnum sourceTypeEnum,
                             boolean isMerged);

    PagingVO<TmsDeclareBillDTO.PagingVO> export(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto);

    void exportDeclare(TmsDeclareBillDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) throws IOException;

    /**
     * 多 sheet 报关单导出
     *
     * <p>业务规则：</p>
     * <ul>
     *   <li>单条记录 → 1 个 xlsx，包含 报关单 / 合同 两个 sheet（发票 / 装箱单 / 装箱明细 暂未实现）</li>
     *   <li>多条记录 → ZIP 包，包内每个 xlsx 同上述结构</li>
     *   <li>单次导出条数上限 100 条，超过抛业务异常</li>
     *   <li>合同 sheet 多明细行币别不一致时，取首行币别并打印 warn 日志</li>
     * </ul>
     *
     * @param pagingParamDTO 查询参数
     * @param response       响应流
     */
    void exportDeclareMulti(TmsDeclareBillDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) throws IOException;
    /**
     * 更新备注
     * @author will
     * @date 2026/4/21 14:43
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     */
    BatchResultDTO updateRemark(String id,String remark);

    /**
     * 批量更新报关单主表字段
     * @param dto dto
     * @param sourceTypeEnum 报关单类型
     * @return Boolean
     */
    Boolean updateBatchFiled(TmsDeclareBillDTO.BatchUpdateFieldDTO dto, SourceTypeEnum sourceTypeEnum);

    /**
     * 查询报关单批量更新字段下拉配置
     * @return 下拉字段配置
     */
    List<TmsDeclareBillDTO.BatchUpdateFieldDropDownDTO> batchUpdateFieldDropDown();

    /**
     * 删除报关单
     * @author will
     * @date 2026/4/30 10:59
     * @param id
     */
    void deleteDeclareBillById (String id);
    /**
     * 查询拆分报关明细
     * @author will
     * @date 2026/4/23 15:21
     * @param id
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.SplitDeclareDTO>
     */
    List<TmsDeclareBillDTO.SplitDeclareDTO> listSplitB2bDetail(String id);
    /**
     * 批量保存拆分报关明细
     * @author will
     * @date 2026/4/24 12:30
     * @param declareDTO
     * @return java.lang.Boolean
     */
    Boolean batchAddSplitB2bDetail(TmsDeclareBillDTO.AddSplitDeclareDTO declareDTO);
    /**
     * 查询拆分报关明细
     * @author will
     * @date 2026/4/23 15:21
     * @param id
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.SplitDeclareDTO>
     */
    List<TmsDeclareBillDTO.SplitDeclareDTO> listSplitFmDetail(String id);
    /**
     * 批量保存拆分报关明细
     * @author will
     * @date 2026/4/23 16:39
     * @param declareDTO
     */
    Boolean batchAddSplitFmDetail(TmsDeclareBillDTO.AddSplitDeclareDTO declareDTO);
    /**
     * 查询合并前的报关明细
     * @author will
     * @date 2026/4/24 10:21
     * @param ids
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.MergeDeclareBeforeDTO>
     */
    List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> listBeforeMergeDetail( List<String> ids);
    /**
     * 查询合并后的报关明细
     * @author will
     * @date 2026/4/23 19:09
     * @param ids
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.MergeDeclareBillDTO>
     */
    List<TmsDeclareBillDTO.MergeDeclareBillDTO> listAfterMergeDetail( List<String> ids);
    /**
     * 批量保存合并后的报关明细
     * @author will
     * @date 2026/4/24 10:22
     * @param list
     * @return Boolean
     */
    Boolean batchAddMergeDetail(String type, List<TmsDeclareBillDTO.MergeDeclareBillDTO> list) ;

    /**
     * 批量保存合并后的报关明细
     *
     * @param type 报关单类型
     * @param list 合并报关明细
     * @param idempotent 是否按自动生成幂等处理
     * @return Boolean
     * @author jack
     * @date 2026/5/12
     */
    Boolean batchAddMergeDetail(String type, List<TmsDeclareBillDTO.MergeDeclareBillDTO> list, Boolean idempotent);

    /**
     * 合并报关单数据
     * @author will
     * @date 2026/4/29 15:07
     * @param viewDTO
     * @return java.util.List<com.erp.model.tms.dto.TmsDeclareBillDTO.MergeDeclareBillDTO>
     */
    List<TmsDeclareBillDTO.MergeDeclareBillDTO> autoMergeDeclareBillView(TmsDeclareBillDTO.AutoMergeDeclareBillViewDTO viewDTO) ;
}
