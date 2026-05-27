package org.cinos.core.stripe.dto;

public class StripeInvoicePayment {
    private String id;
    private String object;
    private Integer amount_paid;
    private Integer amount_requested;
    private Long created;
    private String currency;
    private String invoice;
    private Boolean is_default;
    private Boolean livemode;
    private Payment payment;
    private String status;
    private StatusTransitions status_transitions;

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getObject() { return object; }
    public void setObject(String object) { this.object = object; }
    public Integer getAmount_paid() { return amount_paid; }
    public void setAmount_paid(Integer amount_paid) { this.amount_paid = amount_paid; }
    public Integer getAmount_requested() { return amount_requested; }
    public void setAmount_requested(Integer amount_requested) { this.amount_requested = amount_requested; }
    public Long getCreated() { return created; }
    public void setCreated(Long created) { this.created = created; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getInvoice() { return invoice; }
    public void setInvoice(String invoice) { this.invoice = invoice; }
    public Boolean getIs_default() { return is_default; }
    public void setIs_default(Boolean is_default) { this.is_default = is_default; }
    public Boolean getLivemode() { return livemode; }
    public void setLivemode(Boolean livemode) { this.livemode = livemode; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public StatusTransitions getStatus_transitions() { return status_transitions; }
    public void setStatus_transitions(StatusTransitions status_transitions) { this.status_transitions = status_transitions; }

    public static class Payment {
        private String payment_intent;
        private String type;
        public String getPayment_intent() { return payment_intent; }
        public void setPayment_intent(String payment_intent) { this.payment_intent = payment_intent; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    public static class StatusTransitions {
        private Long canceled_at;
        private Long paid_at;
        public Long getCanceled_at() { return canceled_at; }
        public void setCanceled_at(Long canceled_at) { this.canceled_at = canceled_at; }
        public Long getPaid_at() { return paid_at; }
        public void setPaid_at(Long paid_at) { this.paid_at = paid_at; }
    }
} 