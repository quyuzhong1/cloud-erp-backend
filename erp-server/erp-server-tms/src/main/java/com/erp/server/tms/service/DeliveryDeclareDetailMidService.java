package com.erp.server.tms.service;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.DeliveryDeclareDetailMidEntity;
import com.erp.model.tms.entity.TmsDeclareBillDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.DeliveryDeclareDetailMidDTO;
import com.common.business.vo.PagingVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 报关明细中间表 服务类
 * </p>
 *
 * @author jack
 * @since 2026-04-27
 */
public interface DeliveryDeclareDetailMidService extends SuperService<DeliveryDeclareDetailMidEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2026-04-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DeliveryDeclareDetailMidDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2026-04-27
    * @param dto
    * @return
    */
    Boolean update(DeliveryDeclareDetailMidDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author jack
    * @date: 2026-04-27
    * @param pagingParamDTO
    * @return PagingVO<DeliveryDeclareDetailMidDTO.ListDTO>>
    */
    PagingVO<DeliveryDeclareDetailMidDTO.ListDTO> paging(PagingDTO<DeliveryDeclareDetailMidDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author jack
    * @date: 2026-04-27
    * @param dto
    * @return List<DeliveryDeclareDetailMidDTO.TabListDTO>>
    */
    List<DeliveryDeclareDetailMidDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author jack
    * @date: 2026-04-27
    * @param id
    * @return
    */
    DeliveryDeclareDetailMidDTO.ViewDTO view(String id);

    /**
     * 合并前预览
     *
     * @param ids 报关明细中间表id集合
     * @return 合并前预览列表
     * @throws com.common.core.exception.ServiceException 校验失败时抛出
     * @author jack
     * @date 2026-04-29
     */
    List<DeliveryDeclareDetailMidDTO.MergePreviewDTO> mergePreview(List<String> ids);


    /**
     * 合并后预览
     *
     * @param ids 报关明细中间表id集合
     * @return 合并后预览列表
     * @throws com.common.core.exception.ServiceException 校验失败时抛出
     * @author jack
     * @date 2026-05-06
     */
    List<TmsDeclareBillDTO.MergeDeclareBillDTO> mergeAfterPreview(List<String> ids);

    /**
     * 根据报关单id查询
     * @author will
     * @date 2026/4/23 15:27
     * @param declareBillIdList
     * @return java.util.List<com.erp.model.tms.entity.DeliveryDeclareDetailMidEntity>
     */
    List<DeliveryDeclareDetailMidEntity> listByDeclareBillIdList(List<String> declareBillIdList);
    /**
     * 根据报关单 ids 查询来源信息（含申报要素及报关规则匹配维度）。
     * <p>拆分/合并保存链路依赖本方法回读销售组织、发货仓、中转仓等字段。</p>
     *
     * @author will
     * @date 2026/4/23 18:32
     * @param declareBillIdList 报关单 id 列表
     * @return 来源明细列表
     */
    List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> listSourceByDeclareIdList(List<String> declareBillIdList);
    /**
     * 删除tms发货明细
     * @author will
     * @date 2026/4/24 14:57
     * @param sourceIds
     * @return java.lang.Boolean
     */
    Boolean deleteDeliveryDeclareDetailMid(List<String> sourceIds);
    /**
     * 更新发货单明细业务单号。
     *
     * @param dto 业务单号更新参数
     * @return 是否处理成功
     */
    Boolean updateBusinessCode(TmsDeclareBillDTO.UpdateDeliveryDeclareBusinessCodeDTO dto);

    /**
     * 按报关单id恢复中间表为待生成状态
     * @author will
     * @date 2026/5/13 16:14
     * @param declareBillIds 报关单id集合
     * @return java.lang.Boolean
     */
    Boolean removeByDeclareBillIds(List<String> declareBillIds);
    /**
     * 按报关单表id恢复为待生成状态
     * @author will
     * @date 2026/5/13 16:19
     * @param declareBillIds 中间表报关id集合
     * @return java.lang.Boolean
     */
    Boolean restoreWaitGenerateByDeclareBillIds(List<String> declareBillIds);
    /**
     * 按中间表id恢复为待生成状态
     * @author will
     * @date 2026/5/13 16:19
     * @param ids 中间表id集合
     * @return java.lang.Boolean
     */
    Boolean restoreWaitGenerateByIds(List<String> ids);

    /**
     * 批量保存合并后的中间表报关明细。
     *
     * <p>该方法作为编排入口，负责在事务外完成来源明细校验、合并预览重算和报关单主/明细实体预构建，
     * 避免 Feign 查询、规则匹配等耗时逻辑进入数据库事务。</p>
     *
     * @param list 前端提交的合并预览结果，仅作为选择范围，保存前会重新计算最新合并结果
     * @param updateSourceDeclareStatus 是否同步回写来源单报关状态
     * @return 保存成功返回 true
     */
    Boolean batchAddMergeDetail(List<TmsDeclareBillDTO.MergeDeclareBillDTO> list,Boolean updateSourceDeclareStatus);

    /**
     * 批量保存合并报关单的写库阶段。
     *
     * <p>调用方需要先在事务外完成 Feign 查询、预览重算和实体构建，再通过代理进入本方法，
     * 使事务内仅保留报关单、中间表和来源状态回写。</p>
     *
     * @param sourceType 中间表来源类型
     * @param declareBillType 报关单类型
     * @param preparedBills 事务外预构建的报关单主表、明细实体及其合并明细映射
     * @param updateSourceDeclareStatus 是否同步回写来源单报关状态
     */
    void batchAddMergeDetailInTx(String sourceType,
                                 String declareBillType,
                                 List<TmsDeclareBillDTO.BatchMergeBillData> preparedBills,
                                 Boolean updateSourceDeclareStatus);

    /**
     * 保存报关单生成后的中间表数据，复用待生成行，避免重复插入。
     */
    List<DeliveryDeclareDetailMidEntity> saveGeneratedMidData(String sourceType,
                                                              List<TmsDeclareBillDTO.MergeDeclareBillDetailDTO> declareBillList,
                                                              List<TmsDeclareBillDetailEntity> detailEntityList,
                                                              String declareId,
                                                              String declareCode);
    /**
     * 来源id集合
     * @author will
     * @date 2026/5/12 09:26
     * @param sourceIds
     * @return java.util.List<com.erp.model.tms.entity.TmsDeclareBillEntity>
     */
    List<DeliveryDeclareDetailMidEntity> listBySourceIdList(List<String> sourceIds);

    /**
     * 批量取仓库 id → 名称 映射。
     * 入参为多条"逗号分隔的仓库 id 串"，会扁平化拆分、去重，单次 RPC 拉所有仓库名。
     *
     * <p>用于在写入 delivery_declare_detail_mid 之前一次性把 transfer_warehouse_names
     * 填齐：源表（first_mile_delivery / so_delivery_notice）只存 transfer_warehouse_ids，
     * 名称必须依据 id 反查。</p>
     *
     * @param transferWarehouseIdsList 形如 ["id1,id2", "id3"] 的集合
     * @return id -> name map；入参空或仓库查不到时返回空 map
     */
    Map<String, String> getTransferWarehouseNameMap(List<String> transferWarehouseIdsList);

    /**
     * 按 id 串与名称 map 拼出"name1,name2"形式的名称串。
     *
     * @param transferWarehouseIds 形如 "id1,id2"
     * @param transferWarehouseNameMap {@link #getTransferWarehouseNameMap} 的结果
     * @return "name1,name2"，无可用名称时返回 ""
     */
    String buildTransferWarehouseNames(String transferWarehouseIds, Map<String, String> transferWarehouseNameMap);
}
