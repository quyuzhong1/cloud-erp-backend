package com.erp.server.wms.service;
import com.alibaba.fastjson.JSONArray;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.ThirdWarehouseDTO;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * <p>
 * 海外物流商仓库 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
public interface OverseasProviderWarehouseService extends SuperService<OverseasProviderWarehouseEntity> {

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/12/5 11:49
     * @param dto
     * @param mainId
     * @return java.lang.Boolean
     **/
    Boolean update(OverseasProviderDTO.UpdateDTO dto, String mainId);

    /**
     * 根据仓库 id 查询启用中的三方仓绑定关系。
     * <p>
     * 同一仓库存在多条映射时，优先返回所属服务商已授权（{@code auth_status=already}）的记录，
     * 避免历史取消授权未解绑数据抢先命中。
     *
     * @param warehouseId ERP 仓库 ID
     * @return 优先的三方仓映射；无启用映射时返回 null
     **/
    OverseasProviderWarehouseEntity getByWarehouseId(String warehouseId);

    /**
     * 根据仓库 id 查询启用中的三方仓绑定关系（语义同 {@link #getByWarehouseId(String)}）。
     *
     * @param warehouseId ERP 仓库 ID
     * @return 优先的三方仓映射；无启用映射时返回 null
     **/
    OverseasProviderWarehouseEntity getByWarehouseIdWithNotDisabled(String warehouseId);

    /**
     * 根据仓库ids查询绑定关系
     * @Author Luo_WG
     * @Date 2023/11/17 12:18
     * @param warehouseIds
     * @return com.erp.model.wms.entity.OverseasProviderWarehouseEntity
     **/
    List<OverseasProviderWarehouseEntity> listByWarehouseIds(List<String> warehouseIds);

    /**
     * @Description 根据erp系统的仓库id 获取数据
     * @author lambda
     * @date 2023-12-13 16:08
     * @Param  warehouseIds
     * @Return
     */
    List<OverseasProviderWarehouseDTO.ViewDTO> listByWarehouseIdList(List<String> warehouseIds);

    /**
     * 根据第三方仓库信息查询
     **/
    OverseasProviderWarehouseEntity getByPlatform(String mainId,String platformWarehouseCode);

    /**
     * 根据主表id查询详情信息
     * @Author Luo_WG
     * @Date 2023/11/22 15:52
     * @param mainIds
     * @return java.util.List<com.erp.model.wms.entity.OverseasProviderWarehouseEntity>
     **/
    List<OverseasProviderWarehouseEntity> listByMainIds(List<String> mainIds);

    List<OverseasProviderWarehouseEntity> listByPlatformWarehouseCode(List<String> warehouseCodeList,String platform);

    /**
     * 根据仓库 ID 查询已授权的海外仓服务商。
     * <p>
     * 优先取未禁用且服务商已授权的映射；若启用映射均未授权，再回退到含禁用映射的历史数据。
     *
     * @param destWarehouseId ERP 目的仓 ID
     * @return 已授权服务商；不存在则返回 null
     **/
    OverseasProviderEntity findPlatformByWarehouseId(String destWarehouseId);

    PagingVO<ThirdWarehouseDTO.PageSelectDTO> pagingSelect(PagingDTO<OverseasProviderWarehouseDTO.SelectDTO> dto);
    Boolean feignBind(OverseasProviderDTO.FeignDTO feignDTO);


    /**
     * 是否 API 对接仓库（存在启用映射且所属服务商已授权）。
     *
     * @param destWarehouseId ERP 目的仓 ID
     * @return true=需走三方仓 API；false=仅本地处理
     */
    Boolean isApiWarehouse(String destWarehouseId);


    /**
     * 高级查询查code
     * @param compareCodeSplicingValueSql sql
     */
    List<String> listProviderWarehouseBySql(String compareCodeSplicingValueSql);

    /**
     * 独立站配送信息查询
     * @param shippedDTO
     * @return
     */
    List<OverseasProviderWarehouseDTO.ShippedViewDTO> getShippedInfo(OverseasProviderWarehouseDTO.ShippedDTO shippedDTO);

    BaseResultDTO.AddDTO addThirdWarehouse(ThirdWarehouseDTO.AddDTO addDTO);

    List<OverseasProviderWarehouseEntity> getOverseasWarehouseListByPlatformCode(String platform);

    /**
     * 同步WEGO平台返回的仓库基础数据（差异同步：增/软删/禁用）。
     * <p>
     * 字段映射：warehouseCode→platform_warehouse_code、warehouseName→platform_warehouse_name、warehouseRegion→country。
     * <p>
     * 同步策略（按 main_id + platform_warehouse_code 匹配）：
     * <ul>
     *     <li>API有、DB无：新增（disabled=true）</li>
     *     <li>API有、DB有：不做任何处理（保持现状）</li>
     *     <li>API无、DB有：
     *         <ul>
     *             <li>warehouse_id 为空（未绑定ERP仓）→ 软删（is_deleted=true）</li>
     *             <li>warehouse_id 非空（已绑定ERP仓）→ 仅置 disabled=true，保留绑定关系</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @param mainId            overseas_provider 主表id
     * @param wegoWarehouseList WEGO接口返回的 result 数组
     * @return int[] {新增数量, 软删数量, 禁用数量}
     */
    int[] syncFromWego(String mainId, JSONArray wegoWarehouseList);

    /**
     * 按爱亚（AIYA）仓库列表接口返回的 resultList 同步「三方仓配置-仓库设置」。
     * <p>
     * 同步策略与 {@link #syncFromWego(String, JSONArray)} 完全一致（按 main_id + platform_warehouse_code 匹配）：
     * <ul>
     *     <li>API有、DB无：新增（disabled=true）</li>
     *     <li>API有、DB有：不做任何处理（保持现状）</li>
     *     <li>API无、DB有：warehouse_id 为空（未映射）→ 软删；warehouse_id 非空（已映射）→ 置 disabled=true</li>
     * </ul>
     * 与 WEGO 的差异仅在字段名：爱亚为 warehouseCode / warehouseDescription / country。
     *
     * @param mainId            overseas_provider 主表id
     * @param aiyaWarehouseList 爱亚接口返回的 resultList 数组
     * @return int[] {新增数量, 软删数量, 禁用数量}
     */
    int[] syncFromAiya(String mainId, JSONArray aiyaWarehouseList);
}
