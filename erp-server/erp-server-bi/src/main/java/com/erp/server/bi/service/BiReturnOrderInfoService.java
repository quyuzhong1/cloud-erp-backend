package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.dmp.dto.DmpReturnOrderInfoDTO;
import com.erp.model.dmp.dto.DmpReturnOrderInfoExcelDTO;
import com.erp.model.dmp.dto.DmpReturnOrderInfoSearchDTO;
import com.erp.model.dmp.entity.BiReturnOrderInfoEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;

/**
 * 退货订单服务类
 */
public interface BiReturnOrderInfoService extends IService<BiReturnOrderInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/13 16:17
     * @param dto
     * @return PagingVO<DmpReturnOrderInfoDTO>
     */
    PagingVO<DmpReturnOrderInfoDTO> paging(PagingDTO<DmpReturnOrderInfoSearchDTO> dto);

    /**
     * 汇总退货金额 根据sku和订单号
     * @param orderIds
     * @param sku
     * @return
     */
    BigDecimal sumRefundAmount(List<String> orderIds, BiFilterDTO sku);

    /**
     * @description: 导出
     * @author Will
     * @date: 2022/12/15 10:48
     * @param dto
     */
    void exportExcel(DmpReturnOrderInfoSearchDTO dto, HttpServletResponse response);
    /**
     * @description: 根据退货单号查询
     * @author Will
     * @date: 2022/12/16 12:39
     * @param returnOrderId
     * @return DmpReturnOrderInfoEntity
     */
    BiReturnOrderInfoEntity getByReturnOrderId(String returnOrderId);
    /**
     * @description: 导入
     * @author Will
     * @date: 2023/1/4 16:41
     * @param excelFile
     * @param importType
     * @param response
     * @return Boolean
     */
    Boolean importOrderFile(MultipartFile excelFile, Integer importType, HttpServletResponse response);
    /**
     * @description: 根据明细更新主表订单金额
     * @author Will
     * @date: 2023/1/4 17:11
     * @param returnOrderId
     */
    void updateOrderFeeById(String returnOrderId);

    /**
     * 查询退货数据
     * @param dto
     * @return
     */
    PagingVO<DmpReturnOrderInfoExcelDTO> exportBiReturnOrderInfo(PagingDTO<DmpReturnOrderInfoSearchDTO> dto);
}
