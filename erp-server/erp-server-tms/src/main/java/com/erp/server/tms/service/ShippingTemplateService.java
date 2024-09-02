package com.erp.server.tms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.ShippingTemplateOtherCostDTO;
import com.erp.model.tms.entity.ShippingTemplateEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.ShippingTemplateDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 运费模板 服务类
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
public interface ShippingTemplateService extends SuperService<ShippingTemplateEntity> {

    /**
    * 新增
    * @author Will
    * @date: 2023-11-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ShippingTemplateDTO.AddDTO dto);

    /**
    * 修改
    * @author Will
    * @date: 2023-11-03
    * @param dto
    * @return
    */
    Boolean update(ShippingTemplateDTO.UpdateDTO dto);

    /**
     * @description: tab列表
     * @author Will
     * @date: 2023/11/6 10:50
     * @param dto
     * @return List<TabListDTO>
     */
    List<ShippingTemplateDTO.TabListDTO> tabList(PermissionsDTO dto);
    /**
     * @description: 分页
     * @author Will
     * @date: 2023/11/6 10:51
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<ShippingTemplateDTO.ListDTO> paging(PagingDTO<ShippingTemplateDTO.PagingParamDTO> dto);
    /**
     * @param dto
     * @return Boolean
     * @description: 导出
     * @author Will
     * @date: 2023/11/6 14:09
     */
    Boolean exportExcel(ShippingTemplateDTO.ExportExcelParamDTO dto);
    /**
     * @description: 试算
     * @author Will
     * @date: 2023/11/6 14:11
     * @param dto
     * @return BigDecimal
     */
    BigDecimal trialCalculation(ShippingTemplateDTO.TrialCalculationParamDTO dto);
    /**
     * @description: 查询详情
     * @author Will
     * @date: 2023/11/6 14:14
     * @param id
     * @return ViewDTO
     */
    ShippingTemplateDTO.ViewDTO view(String id);
    /**
     * @description: 应用渠道
     * @author Will
     * @date: 2023/11/6 14:16
     * @param dto
     * @return Boolean
     */
     Boolean updateChannel(ShippingTemplateDTO.ChannelParamDTO dto);
    /**
     * @description: 启用/停用
     * @author Will
     * @date: 2023/11/6 14:35
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO updateStatus(String id,Boolean disabled);
    /**
     * @description: 删除
     * @author Will
     * @date: 2023/11/6 14:38
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO delete(String id);
    /**
     * @description: 下载模板
     * @author Will
     * @date: 2023/11/6 15:34
     * @param response
     */
    void downloadTemplate(HttpServletResponse response,String billingMethod,String type);
    /**
     * @description: 导入模板
     * @author Will
     * @date: 2023/11/6 15:35
     * @param billingMethod
     * @param type
     * @param excelFile
     * @param response
     * @return Boolean
     */
    Boolean importFile(String billingMethod, String type, MultipartFile excelFile, HttpServletResponse response);
    /**
     * @description: 查询其他费用
     * @author Will
     * @date: 2023/11/9 9:27
     * @return List<ViewDTO>
     */
    List<ShippingTemplateOtherCostDTO.ViewDTO> viewOtherCost();
    /**
     * @description: 仓库名称
     * @author Will
     * @date: 2023/11/9 11:24
     * @return List<String>
     */
    List<String> listWarehouseName();
    /**
     * @description: 分区名称
     * @author Will
     * @date: 2023/11/9 11:25
     * @return List<String>
     */
    List<String> listRegionName();
    /**
     * @description: 查询运费模板
     * @author Will
     * @date: 2023/11/15 16:24
     * @return List<SelectDTO>
     */
    List<ShippingTemplateDTO.SelectDTO> listShippingTemplate();

    /**
     * @description: 根据渠道id查询模板
     * @author Will
     * @date: 2023/11/20 12:05
     * @param channelId
     * @return ShippingTemplateEntity
     */
    ShippingTemplateEntity getByChannelId(String channelId);

    /**
     * 根据渠道ID获取模板
     */
    List<ShippingTemplateEntity> getByChannelIds(List<String> channelIds);

    PagingVO<ShippingTemplateDTO.ListDTO> exportShippingTemplate(PagingDTO<ShippingTemplateDTO.ExportExcelParamDTO> dto);
}
