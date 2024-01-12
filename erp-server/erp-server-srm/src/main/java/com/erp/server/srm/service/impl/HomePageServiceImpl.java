package com.erp.server.srm.service.impl;

import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.srm.dto.HomePageDTO;
import com.erp.model.srm.enums.DeliveryOrderConfirmStatusEnum;
import com.erp.model.sys.entity.SysUserWechatEntity;
import com.erp.rpc.sys.feign.UserInfoFeign;
import com.erp.rpc.wms.feign.SupplierFeign;
import com.erp.server.srm.service.CommonService;
import com.erp.server.srm.service.DeliveryOrderService;
import com.erp.server.srm.service.HomePageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * <p>
 * 首页 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-05-08
 */
@Service
public class HomePageServiceImpl implements HomePageService {

    @Resource
    private CommonService commonService;

    @Resource
    private UserInfoFeign userInfoFeign;

    @Resource
    private SupplierFeign supplierFeign;

    @Resource
    private DeliveryOrderService deliveryOrderService;

    @Override
    public HomePageDTO.AccountInfoDTO getAccountInfo() {
        LoginUser loginUser = commonService.getUserInfo();
        if(Objects.isNull(loginUser)){
            throw new ServiceException(ApiError.ERROR_403);
        }
        //查询微信
        SysUserWechatEntity sysUserWechatEntity = userInfoFeign.getWxInfo(loginUser.getUid());
        //查询供应商信息
        SupplierEntity supplier = supplierFeign.getSupplierByUid(loginUser.getUid());
        return HomePageDTO.AccountInfoDTO.builder()
                .userName(loginUser.getRealName())
                .phone(loginUser.getMobile())
                .wxName(Objects.isNull(sysUserWechatEntity)?null:sysUserWechatEntity.getNickName())
                .companyName(Objects.isNull(supplier)?null:supplier.getName())
                .companyStatus(Objects.isNull(supplier)?null: supplier.getApproveStatus().getName())
                .build();
    }

    @Override
    public HomePageDTO.ToDoItems getToDoItems() {
        LoginUser loginUser = commonService.getUserInfo();
        if(Objects.isNull(loginUser)){
            throw new ServiceException(ApiError.ERROR_403);
        }
        //查询供应商信息
        SupplierEntity supplier = supplierFeign.getSupplierByUid(loginUser.getUid());
        if(Objects.isNull(supplier)){
            throw new ServiceException(ApiError.ERROR_96001);
        }

        //TODO:还有两个数量
        return HomePageDTO.ToDoItems.builder()
                .waitPrintDeliveryCount(deliveryOrderService.countByPrint(supplier.getId(),false))
                .waitConfirmDeliveryCount(deliveryOrderService.countByReceiveStatus(supplier.getId(), DeliveryOrderConfirmStatusEnum.WAIT_CONFIRM.getCode()))
                .build();
    }

    @Override
    public HomePageDTO.Statistical getStatistical(String year) {
        return null;
    }
}
