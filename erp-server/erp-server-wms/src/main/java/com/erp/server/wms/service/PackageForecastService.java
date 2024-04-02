package com.erp.server.wms.service;

import cn.hutool.core.date.DateTime;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.entity.PackageForecastEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 组包预报表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2024-01-26
 */
public interface PackageForecastService extends SuperService<PackageForecastEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2024-01-26
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PackageForecastDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2024-01-26
    * @param dto
    * @return
    */
    Boolean update(PackageForecastDTO.UpdateDTO dto);


    /**
     *
     * @param
     * @return
     */
    List<PackageForecastDTO.TabListDTO> tabList();

    /**
     * 分页
     * @param dto
     * @return
     */
    PagingVO<PackageForecastDTO.PagingViewDTO> paging(PagingDTO<PackageForecastDTO.PagingParamDTO> dto);

    /**
     * 详情
     * @param id
     * @return
     */
    PackageForecastDTO.ViewDTO view(String id);

    /**
     * 删除组包预报单
     * @param id
     * @return
     */
    BatchResultDTO delete(String id);

    /**
     * 中转报关
     * @param id
     * @return
     */
    BatchResultDTO forecast(String id,String transferLogisticsSupplierId,String transferLogisticsChannelId);


    /**
     * 导出
     * @param dto
     * @param response
     * @return
     */
    Boolean exportExcel(PackageForecastDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 取消上传
     * @description
     * @param id
     * @return
     * @date 2024-02-18 10:28
     * @author Lambda
     */
    BatchResultDTO cancel(String id);

    /**
     * 上传
     * @param id 组包预报单
     * @param collectMode 揽收方式
     * @param collectAddressId 地址id
     * @return
     */
    BatchResultDTO upload(String id, String collectMode, String collectAddressId);

    /**
     * 打印面单
     * @description
     * @param id
     * @return
     * @date 2024-02-19 14:35
     * @author Lambda
     */
    String print(String id);

    /**
     * 获取速卖通授权信息
     * @param logisticsPlatform
     * @return
     */
    PackageForecastDTO.AlExpressHandoverBaseDTO getAlExpressHandoverBase(String logisticsPlatform);

    /**
     * 获取组包订单 更新订单明细
     * @return
     */
    List<PackageForecastEntity> getAliExpressHandoverList(DateTime dateTime);

    /**
     * 更新订单状态信息
     * @param packageForecastEntity
     * @param alExpressHandoverBase
     */
    void queryAliExpressInfo(PackageForecastEntity packageForecastEntity, PackageForecastDTO.AlExpressHandoverBaseDTO alExpressHandoverBase);

}
