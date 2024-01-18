package com.erp.model.tms.vo.request;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author zdy
 * @ClassName ReceiverInfoVO
 * @description: 发货人信息
 * @date 2023年11月06日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
public class ReceiverInfoVO {
    /**
     * 名称
     */

    private String name;
    /**
     * 公司名
     */

    private String companyName;
    /**
     * 联系人
     */

    private String contact;
    /**
     * 邮箱
     */

    private String email;
    /**
     * 电话
     */

    private String telNumber;
    /**
     * 国家
     */

    private String country;
    /**
     * 省
     */

    private String province;
    /**
     * 城市
     */

    private String city;
    /**
     * 区
     */

    private String district;

    /**
     *  街道详细地址
      */
    private String  streetAddress;
    /**
     * 国家+城市+详细地址
     */

    private String addressFirst;
    /**
     * 国家+城市+详细地址 2
     */

    private String addressSecond;
    /**
     * 邮编
     */

    private String zipCode;
    //买家ID
    private String actId;
}
