package com.sdk.wms.zhongbao.dto.response;

import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName OverseasInboundCreateRequest
 * @description: 海外仓入库单
 * @date 2026年03月03日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OutboundB2cCreateResponse implements Serializable {
    //订单类型:1=>B2C,2=>B2B
    private Integer orderType;
    //订单号
    private String orderNo;
    //自定义编号
    private String referenceNo;
    //物流跟踪号
    private String trackingNo;
    //拣货类型:1=>一票一件,2=>一票一件多个,3=>一票多件
    private Integer pickType;
    //公司名称
    private String companyName;
    //联系人名称
    private String contactName;
    //联系人电话
    private String contactMobile;
    //联系人分机号
    private String contactCellMobile;
    //联系人邮箱
    private String contactEmail;
    //州/省
    private String province;
    //城市
    private String city;
    //区/县
    private String district;
    //收件人门牌号
    private String doorplate;
    //详细地址
    private String address;
    //邮编
    private String postcode;
    //是否住宅地址:-1=>否,1=>是
    private Integer isResidential;
    //需要签名服务:-1=>否,1=>是
    private Integer isSign;
    //签名类型:1=>直接签名,2=>间接签名,3=>成人签名
    private Integer signType;
    //需要保险服务:-1=>否,1=>是
    private Integer isInsure;
    //保险金额(USD)
    private Double insurePrice;
    //加急类型:-1=>无,2=>VC订单,3=>Prime订单（B2C）
    private Integer primeType;
    //库存类型:1=>全部,2=>标准,3=>退件优先
    private Integer inventoryType;
    //产品总数
    private String totalProduct;
    //总重量(KG)
    private Double totalWeight;
    //包装类型:1=>标准包装,2=>异形包装,3=>无包装/软包装,4=>硬质包装,5=>其他特殊
    private Integer packageType;
    //包裹长(CM)
    private Double packageLength;
    //包裹宽(CM)
    private Double packageWidth;
    //包裹高(CM)
    private Double packageHeight;
    //包裹重(KG)
    private Double packageWeight;
    //分区代码
    private String zoneCode;
    //费用金额(USD)
    private Integer feePrice;
    //状态:-2=>异常,-1=>已取消,1=>草稿,2=>待审核,3=>已审核,4=>待出库,5=>已出库
    private Integer status;
    //平台制单状态:-1=不制单>,1=>待制单;2=>制单成功
    private Integer labelStatus;
    //备注
    private String remark;
}
