package com.erp.server.oms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoB2cReceiverDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * <p>
 * B2C销售订单买家信息表 服务类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
public interface SoB2cReceiverService extends SuperService<SoB2cReceiverEntity> {

    /**
     * @param receiverDTO
     * @param soB2cEntity
     * @return Boolean
     * @description: 新增
     * @author Will
     * @date: 2023/8/21 17:13
     */
    Boolean add(SoB2cReceiverDTO.AddDTO receiverDTO, SoB2cEntity soB2cEntity);
    /**
     * @param receiverDTO
     * @param soB2cEntity
     * @return Boolean
     * @description: 修改
     * @author Will
     * @date: 2023/8/21 17:13
     */
    Boolean update(SoB2cReceiverDTO.UpdateDTO receiverDTO, SoB2cEntity soB2cEntity);
    /**
     * @description: 根据主表id查询
     * @author Will
     * @date: 2023/8/22 11:16
     * @param mainId
     * @return SoB2cReceiverEntity
     */
    SoB2cReceiverEntity getByMainId(String mainId);
    /**
     * @description: 根据主表ids查询
     * @author Will
     * @date: 2023/8/23 10:04
     * @param mainIds
     * @return List<SoB2cReceiverEntity>
     */
    List<SoB2cReceiverEntity> listByMainIds(List<String> mainIds);
    /**
     * @description: 根据主表id删除
     * @author Will
     * @date: 2023/8/23 12:27
     * @param mainIds
     * @return Boolean
     */
    Boolean deleteByMainIds(List<String> mainIds);

    /**
     * 平台订单明细更新或保存
     *
     * @Author Jim
     * @since 2023-11-10
     **/
    SoB2cReceiverEntity saveOrUpdateEntity(PlatformOrderDTO dto, SoB2cEntity mainEntity, List<DictCountryEntity> countryList ,boolean notUpdateAddress);

    /**
     * 更新指定字段
     * @param receiver
     */
    void updateFieldById(SoB2cReceiverEntity receiver);

    /**
     * 封装分区Id
     */
    void buildPartitionId(SoB2cReceiverEntity receiverEntity, ShopInfoEntity shopInfoEntity);

    IPage<SoB2cReceiverEntity> pagePartitionIsNull(Page query);

    void importB2cCustomerFile(MultipartFile excelFile, HttpServletResponse response);

    void updateInvoiceAddress(String soId, String invoiceAddress);


    void checkAndUpdateCountry(String mainId, String country);
}
