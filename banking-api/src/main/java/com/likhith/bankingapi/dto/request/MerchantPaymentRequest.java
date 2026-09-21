package com.likhith.bankingapi.dto.request;

import java.math.BigDecimal;
import java.util.List;

import com.likhith.bankingapi.dto.request.shared.ChannelMetadataDto;
import com.likhith.bankingapi.dto.request.shared.MoneyAmountDto;
import com.likhith.bankingapi.entity.enums.MerchantPaymentEnums.MerchantCategory;
import com.likhith.bankingapi.entity.enums.MerchantPaymentEnums.PaymentInstrument;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for a point-of-sale or online merchant payment")
public class MerchantPaymentRequest {

    @NotBlank(message = "accountId is required")
    @Schema(example = "ACC-3D2E1F0A9B8C")
    private String accountId;

    @NotBlank(message = "merchantId is required")
    @Schema(example = "MER-771234")
    private String merchantId;

    @NotBlank(message = "merchantName is required")
    @Schema(example = "Synthetic Coffee Co.")
    private String merchantName;

    @NotNull(message = "merchantCategory is required")
    private MerchantCategory merchantCategory;

    @NotNull(message = "amount is required")
    @Valid
    private MoneyAmountDto amount;

    @DecimalMin(value = "0.0", message = "tipAmount must not be negative")
    @Schema(example = "2.50")
    private BigDecimal tipAmount;

    @NotNull(message = "paymentInstrument is required")
    private PaymentInstrument paymentInstrument;

    @Valid
    @Size(max = 20, message = "at most 20 line items are supported")
    private List<LineItem> items;

    @NotNull(message = "deviceMetadata is required")
    @Valid
    private ChannelMetadataDto deviceMetadata;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "A single purchased line item")
    public static class LineItem {

        @NotBlank(message = "itemName is required")
        @Schema(example = "Cappuccino")
        private String itemName;

        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be at least 1")
        @Schema(example = "2")
        private Integer quantity;

        @NotNull(message = "unitPrice is required")
        @DecimalMin(value = "0.0", message = "unitPrice must not be negative")
        @Schema(example = "4.75")
        private BigDecimal unitPrice;
    }
}
