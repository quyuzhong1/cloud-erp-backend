package com.erp.sdk.oms.amz.spapi.SellingPartnerAPIAA;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.checkerframework.checker.units.qual.A;

/**
 * AWSAuthenticationCredentialsProvider
 */
@Data
@Builder
@AllArgsConstructor
public class AWSAuthenticationCredentialsProvider {
    /**
     * AWS IAM Role ARN
     */
    private String roleArn;

    /**
     * AWS IAM Role Session Name
     */
    private String roleSessionName;


}
