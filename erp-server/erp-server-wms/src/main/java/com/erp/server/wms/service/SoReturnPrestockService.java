package com.erp.server.wms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoReturnPrestockDTO;
import com.erp.model.wms.dto.SoReturnPrestockDetailDTO;
import com.erp.model.wms.entity.SoReturnPrestockEntity;

import java.util.List;

/**
 * 预入库单 Service 接口
 *
 * @author auto
 * @since 2026-06-30
 */
public interface SoReturnPrestockService extends SuperService<SoReturnPrestockEntity> {

    /**
     * 分页查询
     *
     * @param dto 分页参数
     * @return 分页结果
     */
    PagingVO<SoReturnPrestockDTO.PagingView> paging(PagingDTO<SoReturnPrestockDTO.PagingParam> dto);

    /**
     * 手动新增预入库单
     * <p>来源类型写入 MANUAL，入库时间和操作时间均设为当前时间</p>
     *
     * @param dto 新增入参
     * @return 新增记录的 ID
     */
    String add(SoReturnPrestockDTO.Add dto);

    /**
     * 修改预入库单基础信息
     *
     * @param dto 修改入参
     * @return 是否成功
     */
    Boolean update(SoReturnPrestockDTO.Update dto);

    /**
     * 查询详情（含详情行列表）
     *
     * @param id 主表 ID
     * @return 详情出参
     */
    SoReturnPrestockDTO.View view(String id);

    /**
     * 确认关联售后单（预入库单维度批量关联）
     * <p>入参 afterSaleList 为在候选售后单列表（{@code SoReturnController.pagingLinkAfterSale}）中勾选的
     * 售后单明细行。按 SKU 将本次勾选的退货明细数量与预入库单未关联明细数量比较：</p>
     * <ul>
     *   <li>SKU 种类与数量完全一致：全部关联，返回"已完成关联"；</li>
     *   <li>预入库单明细多于退货明细：仅关联能匹配的 SKU（按需拆行，剩余数量保留为未关联行），
     *       主表联动为部分关联，返回差异提示；</li>
     *   <li>退货明细超过预入库单（SKU 种类或数量超出）：整批拒绝，返回失败提示，需调配售后退货单后再关联。</li>
     * </ul>
     *
     * @param dto 确认关联售后单入参
     * @return 操作结果（失败时 msg 说明超出原因，成功时区分完成关联/部分关联）
     */
    Boolean confirmLinkAfterSale(SoReturnPrestockDetailDTO.ConfirmLinkAfterSale dto);

    /**
     * 批量关联店铺
     * <p>入参 ids 为预入库单主表 ID 列表，将多张预入库单下未关联的明细行整行关联到同一店铺。
     * B2B 与 B2C 关联的店铺不同，因此校验本次所选预入库单的单据类型必须一致，混合类型则整批拒绝。
     * 更新明细行的店铺信息与关联状态，并联动刷新主表的关联状态。</p>
     *
     * @param dto 批量关联店铺入参
     * @return 每张预入库单的操作结果
     */
    List<BatchResultDTO> linkShop(SoReturnPrestockDetailDTO.LinkShop dto);

    /**
     * 确认关联店铺（预入库单维度，明细逐行选择店铺）
     * <p>与 {@link #linkShop} 不同：linkShop 是把整张预入库单的未关联行整体关联到同一店铺；
     * 本方法允许同一张预入库单内不同明细行分别关联到不同店铺，并支持按认领数量拆行：</p>
     * <ul>
     *   <li>仅未关联行可参与关联，未在入参中出现（未选择店铺）的行保持未关联；</li>
     *   <li>认领数量 = 实际收货数量：整行关联；认领数量 &lt; 实际收货数量：拆行，认领部分独立成行并关联，
     *       剩余数量拆为新未关联行；</li>
     *   <li>关联相同店铺的行合并生成一张《退货入库单》，并把生成的入库单号回写到对应明细行；</li>
     *   <li>处理完成后联动刷新主表关联状态。</li>
     * </ul>
     *
     * @param dto 确认关联店铺入参
     * @return 操作结果
     */
    BatchResultDTO confirmLinkShop(SoReturnPrestockDetailDTO.ConfirmLinkShop dto);

    /**
     * 批量删除预入库单（软删）
     *
     * @param ids 主表 ID 列表
     * @return 批量结果
     */
    List<BatchResultDTO> deleteByIds(List<String> ids);

    /**
     * 由海外仓退货入库单（无物流单号、无参考单号，本次消息无法关联到任何单据）自动创建预入库单（系统内部调用）
     * <p>幂等保证：以 thirdCode（第三方/平台退货单号）为唯一键，已存在则跳过；此场景下 returnLogisticCode 固定落空字符串。</p>
     *
     * @param dto 退货入库单相关信息（由调用方组装，returnLogisticCode 允许为空）
     * @return 新建的预入库单 ID，已存在则返回现有 ID
     */
    String createFromOverseasWhHeadless(SoReturnPrestockDTO.Add dto);

    /**
     * 由【退货入库单】新增/修改表单参数创建预入库单
     * <p>前提：退货客户（customerId）必须为空，否则应直接保存退货入库单，创建预入库单没有意义；
     * 退货物流单号必须非空（预入库单以物流单号唯一）。</p>
     *
     * @param dto 预入库单专属入参（与退货入库单表单 DTO 解耦）
     * @return 新建预入库单的 ID
     */
    String addFromReturnInstock(SoReturnPrestockDTO.FromInstock dto);

    /**
     * 强制关闭剩余未认领的预入库单（定时任务调用，每月1号23:50执行）。
     * <p>处理范围：仍存在未关联（UNLINKED）明细的预入库单（主表关联状态为未关联或部分关联，且未删除）。</p>
     * <p>处理逻辑：</p>
     * <ul>
     *   <li>将这些预入库单下关联状态为「未关联」的明细行 link_status 更新为「强制关闭」（已关联明细保持不变）；</li>
     *   <li>按明细最新关联状态联动刷新主表关联状态：明细全部已关联→已关联；全部强制关闭→强制关闭；
     *       部分已关联部分强制关闭→部分关联。</li>
     * </ul>
     * <p>强制关闭后，该预入库单及其明细不再允许关联店铺、关联售后单等操作。</p>
     *
     * @return 本次实际强制关闭处理的预入库单数量
     */
    int forceCloseUnclaimedPrestock();
}
