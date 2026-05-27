package org.cinos.core.stripe.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.cinos.core.stripe.dto.SubscriptionPlanDto;
import org.cinos.core.users.entity.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
@Slf4j
public class StripeService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    // Simulación de base de datos para ejemplo (reemplaza por tu repo real)
    private final Map<String, String> userIdToCustomerId = new HashMap<>();
    private final Map<String, String> userIdToSubscriptionId = new HashMap<>();

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    /**
     * Devuelve solo el plan mensual
     */
    public List<SubscriptionPlanDto> getSubscriptionPlans() {
        List<SubscriptionPlanDto> plans = new ArrayList<>();
        plans.add(new SubscriptionPlanDto(
                "premium_monthly",
                "Premium Mensual",
                999, // $9.99 en centavos
                "USD",
                "month",
                List.of(
                        "Filtros avanzados ilimitados",
                        "Recomendaciones ilimitadas",
                        "Sin anuncios",
                        "Soporte prioritario"
                )
        ));
        return plans;
    }

    /**
     * Obtiene el Price ID real de Stripe para el plan mensual
     */
    private String getPriceIdForPlan(String planId) {
        Map<String, String> planToPriceId = new HashMap<>();
        planToPriceId.put("premium_monthly", "price_1RfkUKCTvKLO8QJ3rRZotpQl"); // Price ID real
        return planToPriceId.getOrDefault(planId, "price_1RfkUKCTvKLO8QJ3rRZotpQl");
    }

    /**
     * Obtiene o crea un Customer de Stripe para el usuario
     */
    private String getOrCreateCustomer(String userId, String email) throws StripeException {
        // Busca en tu base de datos real
        if (userIdToCustomerId.containsKey(userId)) {
            return userIdToCustomerId.get(userId);
        }
        Map<String, Object> params = new HashMap<>();
        params.put("email", email);
        Customer customer = Customer.create(params);
        userIdToCustomerId.put(userId, customer.getId());
        return customer.getId();
    }

    /**
     * Crea una suscripción real en Stripe y retorna el clientSecret del PaymentIntent de la primera factura
     */
    public String createSubscription(String planId, String userId, String email, boolean trial) throws StripeException {
        String customerId = getOrCreateCustomer(userId, email);

        Map<String, Object> item = new HashMap<>();
        item.put("price", getPriceIdForPlan(planId));
        List<Object> items = new ArrayList<>();
        items.add(item);

        Map<String, Object> params = new HashMap<>();
        params.put("customer", customerId);
        params.put("items", items);

        if (trial) {
            params.put("trial_period_days", 7); // Cambia el periodo si lo deseas
        }

        params.put("payment_behavior", "default_incomplete");
        params.put("expand", List.of("latest_invoice.payment_intent"));

        Subscription subscription = Subscription.create(params);

        userIdToSubscriptionId.put(userId, subscription.getId());

        Invoice invoice = subscription.getLatestInvoiceObject();
        PaymentIntent paymentIntent = invoice.getPaymentIntentObject();
        return paymentIntent.getClientSecret();
    }

    /**
     * Cancela una suscripción
     */
    public void cancelSubscription(String subscriptionId) throws StripeException {
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            
            // Verificar que la suscripción existe
            if (subscription == null) {
                throw new RuntimeException("Suscripción no encontrada");
            }
            
            // Verificar si ya está cancelada
            if ("canceled".equals(subscription.getStatus())) {
                throw new RuntimeException("La suscripción ya está cancelada");
            }
            
            // Verificar si ya está marcada para cancelar al final del período
            if (Boolean.TRUE.equals(subscription.getCancelAtPeriodEnd())) {
                throw new RuntimeException("La suscripción ya está marcada para cancelar al final del período");
            }
            
            if (!"active".equals(subscription.getStatus()) && !"trialing".equals(subscription.getStatus())) {
                throw new RuntimeException("La suscripción no está activa para cancelar");
            }
            
            // Cancelar la suscripción al final del período actual
            Map<String, Object> cancelParams = new HashMap<>();
            cancelParams.put("cancel_at_period_end", true);
            subscription.update(cancelParams);
            log.info("✅ Suscripción {} cancelada exitosamente", subscriptionId);
            
        } catch (StripeException e) {
            System.err.println("❌ Error al cancelar suscripción: " + e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            System.err.println("❌ Error de validación: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Reactiva una suscripción cancelada o marcada para cancelar
     */
    public void reactivateSubscription(String subscriptionId) throws StripeException {
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            
            // Verificar que la suscripción existe
            if (subscription == null) {
                throw new RuntimeException("Suscripción no encontrada");
            }
            
            // Verificar si está cancelada o marcada para cancelar
            if (!"canceled".equals(subscription.getStatus()) && !Boolean.TRUE.equals(subscription.getCancelAtPeriodEnd())) {
                throw new RuntimeException("La suscripción no está cancelada o marcada para cancelar");
            }
            
            // Reactivar la suscripción
            Map<String, Object> reactivateParams = new HashMap<>();
            reactivateParams.put("cancel_at_period_end", false);
            subscription.update(reactivateParams);
            
            System.out.println("✅ Suscripción " + subscriptionId + " reactivada exitosamente");
            
        } catch (StripeException e) {
            System.err.println("❌ Error al reactivar suscripción: " + e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            System.err.println("❌ Error de validación: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Verifica si una suscripción está cancelada o marcada para cancelar
     */
    public boolean isSubscriptionCanceled(String subscriptionId) throws StripeException {
        try {
            System.out.println("🔍 Verificando cancelación para subscriptionId: " + subscriptionId);
            Subscription subscription = Subscription.retrieve(subscriptionId);
            
            if (subscription == null) {
                System.out.println("❌ Subscription es null");
                return false;
            }
            
            String status = subscription.getStatus();
            Boolean cancelAtPeriodEnd = subscription.getCancelAtPeriodEnd();
            
            System.out.println("🔍 Status: " + status);
            System.out.println("🔍 CancelAtPeriodEnd: " + cancelAtPeriodEnd);
            
            // Verificar si está cancelada o marcada para cancelar
            boolean isCanceled = "canceled".equals(status) || Boolean.TRUE.equals(cancelAtPeriodEnd);
            System.out.println("🔍 ¿Está cancelada? " + isCanceled);
            
            return isCanceled;
        } catch (StripeException e) {
            System.err.println("❌ Error al verificar estado de cancelación: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Verifica el estado de la suscripción de un usuario
     */
    public String getSubscriptionStatus(String userId) throws StripeException {
        String subscriptionId = userIdToSubscriptionId.get(userId);
        if (subscriptionId == null) return "none";
        Subscription subscription = Subscription.retrieve(subscriptionId);
        return subscription.getStatus(); // Ej: active, canceled, incomplete, etc.
    }

    /**
     * Verifica el estado de un pago (opcional)
     */
    public String checkPaymentStatus(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return paymentIntent.getStatus();
    }

    /**
     * Confirma un pago exitoso (opcional, lo ideal es usar webhooks)
     */
    public boolean confirmPayment(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        
        // Si el pago ya está confirmado, retornar true
        if ("succeeded".equals(paymentIntent.getStatus())) {
            return true;
        }
        
        // Si el pago requiere confirmación, confirmarlo con un token de prueba
        if ("requires_payment_method".equals(paymentIntent.getStatus()) || "requires_confirmation".equals(paymentIntent.getStatus())) {
            // Usar un token de prueba de Stripe (tok_visa)
            Map<String, Object> confirmParams = new HashMap<>();
            confirmParams.put("payment_method_data", Map.of(
                "type", "card",
                "card", Map.of(
                    "token", "tok_visa"
                )
            ));
            
            paymentIntent = paymentIntent.confirm(confirmParams);
            System.out.println("✅ PaymentIntent confirmado con estado: " + paymentIntent.getStatus());
            
        return "succeeded".equals(paymentIntent.getStatus());
        }
        
        return false;
    }

    /**
     * Crea una sesión de Stripe Checkout para suscripción
     */
    public String createSubscriptionCheckoutSession(String priceId, String customerEmail) throws StripeException {
        Stripe.apiKey = stripeSecretKey;
        String successUrl = "cinos://suscripcion-exito";
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(successUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(priceId)
                                .setQuantity(1L)
                                .build()
                )
                .setCustomerEmail(customerEmail)
                .build();
        Session session = Session.create(params);
        return session.getUrl();
    }

    /**
     * Obtiene la fecha de próxima renovación de una suscripción de Stripe
     */
    public Long getSubscriptionNextRenewal(String subscriptionId) throws StripeException {
        Subscription subscription = Subscription.retrieve(subscriptionId);
        return subscription.getCurrentPeriodEnd(); // Epoch seconds
    }

    public String getLastCreatedSubscriptionIdForUser(String userId) {
        return userIdToSubscriptionId.get(userId);
    }

    public static class StripeSubscriptionResult {
        public final String clientSecret;
        public final String subscriptionId;
        public StripeSubscriptionResult(String clientSecret, String subscriptionId) {
            this.clientSecret = clientSecret;
            this.subscriptionId = subscriptionId;
        }
    }

    public StripeSubscriptionResult createSubscriptionWithId(String planId, String userId, String email, boolean trial) throws StripeException {
        String customerId = getOrCreateCustomer(userId, email);
        Map<String, Object> item = new HashMap<>();
        item.put("price", getPriceIdForPlan(planId));
        List<Object> items = new ArrayList<>();
        items.add(item);
        Map<String, Object> params = new HashMap<>();
        params.put("customer", customerId);
        params.put("items", items);
        if (trial) {
            params.put("trial_period_days", 7);
        }
        params.put("payment_behavior", "default_incomplete");
        params.put("expand", List.of("latest_invoice.payment_intent"));
        Subscription subscription = Subscription.create(params);
        Invoice invoice = subscription.getLatestInvoiceObject();
        PaymentIntent paymentIntent = invoice.getPaymentIntentObject();
        return new StripeSubscriptionResult(paymentIntent.getClientSecret(), subscription.getId());
    }

    public String createVerificationAccessPaymentIntent(Long postId, UserEntity user) throws StripeException {
        // Crear PaymentIntent para acceso a verificación específica
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
            .setAmount(499L) // $4.99 en centavos
            .setCurrency("usd")
            .setCustomer(user.getStripeCustomerId())
            .putMetadata("postId", postId.toString())
            .putMetadata("userId", user.getId().toString())
            .putMetadata("type", "verification_access")
            .setDescription("Acceso a informe técnico")
            .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        System.out.println("🔧 PaymentIntent creado con ID: " + paymentIntent.getId() + " y estado: " + paymentIntent.getStatus());
        
        return paymentIntent.getClientSecret();
    }

    public String createVerificationAccessCheckoutSession(Long postId, UserEntity user, String successUrl) throws StripeException {
        // Crear sesión de Stripe Checkout para acceso a verificación
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Acceso a Informe Técnico")
                                                                .setDescription("Desbloquea el acceso al informe técnico detallado")
                                                                .build()
                                                )
                                                .setUnitAmount(499L) // $4.99 en centavos
                                                .build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .setCustomerEmail(user.getEmail())
                .putMetadata("postId", postId.toString())
                .putMetadata("userId", user.getId().toString())
                .putMetadata("type", "verification_access")
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
}