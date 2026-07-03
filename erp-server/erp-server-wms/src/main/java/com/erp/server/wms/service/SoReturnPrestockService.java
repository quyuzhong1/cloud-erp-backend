package com.erp.server.wms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.SoReturnInstockDTO;
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
     * 关联售后单
     * <p>若本次关联数量 &lt; 当前行 return_qty，触发拆行逻辑：
     * 原行数量减少，新建一行承载剩余数量；主表关联状态联动更新。</p>
     *
     * @param dto 关联售后单入参
     * @return 操作结果
     */
    BatchResultDTO linkAfterSale(SoReturnPrestockDetailDTO.LinkAfterSale dto);

    /**
     * 关联店铺
     * <p>若本次关联数量 &lt; 当前行 return_qty，触发拆行逻辑；主表关联状态联动更新。</p>
     *
     * @param dto 关联店铺入参
     * @return 操作结果
     */
    BatchResultDTO linkShop(SoReturnPrestockDetailDTO.LinkShop dto);

    /**
     * 批量删除预入库单（软删）
     *
     * @param ids 主表 ID 列表
     * @return 批量结果
     */
    List<BatchResultDTO> deleteByIds(List<String> ids);

    /**
     * 由海外仓退货入库单自动创建预入库单（系统内部调用）
     * <p>幂等保证：以 returnLogisticCode 为唯一键，已存在则跳过。</p>
     *
     * @param entity 退货入库单相关信息（由调用方组装）
     * @return 新建的预入库单 ID，已存在则返回现有 ID
     */
    String createFromOverseasWh(SoReturnPrestockDTO.Add entity);

    /**
     * 由海外仓退货入库单（无物流单号、无参考单号，本次消息无法关联到任何单据）自动创建预入库单（系统内部调用）
     * <p>幂等保证：以 thirdCode（第三方/平台退货单号）为唯一键，已存在则跳过；此场景下 returnLogisticCode 固定落空字符串。</p>
     *
     * @param dto 退货入库单相关信息（由调用方组装，returnLogisticCode 允许为空）
     * @return 新建的预入库单 ID，已存在则返回现有 ID
     */
    String createFromOverseasWhHeadless(SoReturnPrestockDTO.Add dto);

    /**
     * 由【退货入库单-新增】表单参数创建预入库单
     * <p>前提：退货客户（customerId）必须为空，否则应直接保存退货入库单，创建预入库单没有意义；
     * 退货物流单号必须非空（预入库单以物流单号唯一）。</p>
     *
     * @param dto 与退货入库单新增接口相同的入参
     * @return 新建预入库单的 ID
     */
    String addFromReturnInstockAdd(SoReturnInstockDTO.Add dto);

    /**
     * 由【退货入库单-修改】表单参数创建预入库单
     * <p>前提同 {@link #addFromReturnInstockAdd}；仅使用表单字段值新建预入库单，不影响原退货入库单记录。</p>
     *
     * @param dto 与退货入库单修改接口相同的入参
     * @return 新建预入库单的 ID
     */
    String addFromReturnInstockUpdate(SoReturnInstockDTO.Update dto);
}
