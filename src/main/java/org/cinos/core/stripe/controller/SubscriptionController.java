package org.cinos.core.stripe.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cinos.core.posts.entity.PostEntity;
import org.cinos.core.stripe.dto.*;
import org.cinos.core.stripe.entity.PaymentDetail;
import org.cinos.core.stripe.repository.PaymentDetailRepository;
import org.cinos.core.stripe.service.StripeService;
import org.cinos.core.users.entity.UserEntity;
import org.cinos.core.users.model.Role;
import org.cinos.core.users.repository.UserRepository;
import org.cinos.core.posts.repository.PostRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("subscriptions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final StripeService stripeService;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PaymentDetailRepository paymentDetailRepository;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    /**
     * Obtiene los planes de suscripción disponibles
     */
    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlanDto>> getSubscriptionPlans() {
        try {
            List<SubscriptionPlanDto> plans = stripeService.getSubscriptionPlans();
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    /**
     * Obtiene los detalles de la suscripción del usuario
     */
    @GetMapping("/details")
    public ResponseEntity<SubscriptionResponse> getSubscriptionDetails() {
        try {
            // Obtener usuario autenticado
            UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            
            System.out.println("🔍 Verificando suscripción para usuario: " + userEntity.getEmail());
            System.out.println("🔍 StripeSubscriptionId: " + userEntity.getStripeSubscriptionId());
            System.out.println("🔍 Roles: " + userEntity.getRoles());
            
            if (userEntity.getStripeSubscriptionId() == null || userEntity.getStripeSubscriptionId().isEmpty()) {
                System.out.println("❌ Usuario no tiene stripeSubscriptionId");
                return ResponseEntity.ok(SubscriptionResponse.builder()
                        .message("No tienes una suscripción activa")
                        .success(false)
                        .estado("SIN_SUSCRIPCION")
                        .build());
            }
            
            // Verificar si la suscripción está cancelada
            boolean isCanceled = stripeService.isSubscriptionCanceled(userEntity.getStripeSubscriptionId());
            System.out.println("🔍 ¿Está cancelada? " + isCanceled);
            
            if (isCanceled) {
                System.out.println("⚠️ Suscripción cancelada para usuario: " + userEntity.getEmail());
                return ResponseEntity.ok(SubscriptionResponse.builder()
                        .message("Suscripción cancelada - No se renovará automáticamente")
                        .success(false)
                        .estado("CANCELADA")
                        .build());
            }
            
            // Obtener información de la suscripción desde Stripe
            String status = stripeService.getSubscriptionStatus(userEntity.getId().toString());
            Long nextRenewal = stripeService.getSubscriptionNextRenewal(userEntity.getStripeSubscriptionId());
            
            String message = "Estado: " + status + ", Próxima renovación: " + new java.util.Date(nextRenewal * 1000);
            System.out.println("✅ Suscripción activa para usuario: " + userEntity.getEmail());
            
            return ResponseEntity.ok(SubscriptionResponse.builder()
                    .message(message)
                    .success(true)
                    .estado("ACTIVA")
                    .build());
        } catch (Exception e) {
            System.err.println("❌ Error en getSubscriptionDetails: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(SubscriptionResponse.builder().message("Error: " + e.getMessage()).success(false).estado("ERROR").build());
        }
    }

    /**
     * Cancela una suscripción
     */
    @PostMapping("/cancel")
    public ResponseEntity<SubscriptionResponse> cancelSubscription() {
        try {
            // Obtener usuario autenticado
            UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            
            if (userEntity.getStripeSubscriptionId() == null || userEntity.getStripeSubscriptionId().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(SubscriptionResponse.builder()
                                .message("No tienes una suscripción activa para cancelar")
                                .success(false)
                                .build());
            }

            // Cancelar suscripción en Stripe (solo marca para cancelar al final del período)
            stripeService.cancelSubscription(userEntity.getStripeSubscriptionId());
            
            // NO eliminar el rol premium inmediatamente - se mantendrá hasta el final del período
            // El rol premium se eliminará automáticamente cuando Stripe envíe el webhook de cancelación
            // o cuando el período actual termine
            
            return ResponseEntity.ok(
                    SubscriptionResponse.builder()
                            .message("Suscripción cancelada exitosamente. Tu acceso premium se mantendrá hasta el final del período actual.")
                            .success(true)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SubscriptionResponse.builder().message("Error: " + e.getMessage()).success(false).build());
        }
    }

    /**
     * Reactiva una suscripción cancelada
     */
    @PostMapping("/reactivate")
    public ResponseEntity<SubscriptionResponse> reactivateSubscription() {
        try {
            // Obtener usuario autenticado
            UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            
            if (userEntity.getStripeSubscriptionId() == null || userEntity.getStripeSubscriptionId().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(SubscriptionResponse.builder()
                                .message("No tienes una suscripción para reactivar")
                                .success(false)
                                .build());
            }

            // Verificar si la suscripción está cancelada o marcada para cancelar
            boolean isCanceled = stripeService.isSubscriptionCanceled(userEntity.getStripeSubscriptionId());
            
            if (!isCanceled) {
                return ResponseEntity.badRequest()
                        .body(SubscriptionResponse.builder()
                                .message("Tu suscripción no está cancelada")
                                .success(false)
                                .build());
            }

            // Reactivar la suscripción en Stripe
            stripeService.reactivateSubscription(userEntity.getStripeSubscriptionId());
            
            return ResponseEntity.ok(
                    SubscriptionResponse.builder()
                            .message("Suscripción reactivada exitosamente. Se cobrará automáticamente al finalizar el período actual.")
                            .success(true)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SubscriptionResponse.builder().message("Error: " + e.getMessage()).success(false).build());
        }
    }

    /**
     * Obtiene el estado de la suscripción actual del usuario
     */
    @GetMapping("/status")
    public ResponseEntity<SubscriptionResponse> getSubscriptionStatus() {
        try {
            // Obtener usuario autenticado
            UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            
            boolean isPremium = userEntity.getRoles() != null && userEntity.getRoles().contains(Role.PREMIUM);
            String status = isPremium ? "premium" : "free";
            
            return ResponseEntity.ok(SubscriptionResponse.builder()
                    .message(status)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SubscriptionResponse.builder().message("Error: " + e.getMessage()).success(false).build());
        }
    }

    /**
     * Verifica el estado de un pago
     */
    @GetMapping("/payment/{paymentIntentId}/status")
    public ResponseEntity<SubscriptionResponse> getPaymentStatus(@PathVariable String paymentIntentId) {
        try {
            if (paymentIntentId == null || paymentIntentId.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(SubscriptionResponse.builder()
                                .message("Payment Intent ID es requerido")
                                .success(false)
                                .build());
            }

            String status = stripeService.checkPaymentStatus(paymentIntentId);
            return ResponseEntity.ok(SubscriptionResponse.builder()
                    .message("Estado del pago: " + status)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SubscriptionResponse.builder().message("Error: " + e.getMessage()).success(false).build());
        }
    }

    /**
     * Confirma un pago exitoso
     */
    @PostMapping("/payment/{paymentIntentId}/confirm")
    public ResponseEntity<SubscriptionResponse> confirmPayment(@PathVariable String paymentIntentId) {
        try {
            if (paymentIntentId == null || paymentIntentId.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(SubscriptionResponse.builder()
                                .message("Payment Intent ID es requerido")
                                .success(false)
                                .build());
            }

            boolean confirmed = stripeService.confirmPayment(paymentIntentId);
            if (confirmed) {
                return ResponseEntity.ok(
                        SubscriptionResponse.builder()
                                .message("Pago confirmado exitosamente")
                                .success(true)
                                .build());
            } else {
                return ResponseEntity.badRequest()
                        .body(SubscriptionResponse.builder()
                                .message("El pago no se pudo confirmar")
                                .success(false)
                                .build());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SubscriptionResponse.builder().message("Error: " + e.getMessage()).success(false).build());
        }
    }

    /**
     * Crea una sesión de Stripe Checkout para suscripción
     */
    @PostMapping("/checkout-session")
    public ResponseEntity<SubscriptionResponse> createCheckoutSession(@RequestBody CreateSubscriptionRequest request) {
        try {
            if (request.getPlanId() == null || request.getPlanId().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(SubscriptionResponse.builder()
                                .message("Plan ID es requerido")
                                .success(false)
                                .build());
            }
            // Obtener usuario autenticado
            UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();
            String email = userEntity.getEmail();
            // PriceId real de Stripe
            String priceId = request.getPlanId();
            String url = stripeService.createSubscriptionCheckoutSession(priceId, email);
            return ResponseEntity.ok(SubscriptionResponse.builder().checkoutUrl(url).success(true).build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SubscriptionResponse.builder().message("Error: " + e.getMessage()).success(false).build());
        }
    }

    /**
     * Crea una sesión de Stripe Checkout para acceso a verificación técnica
     */
    @PostMapping("/verification-access-checkout")
    public ResponseEntity<SubscriptionResponse> createVerificationAccessCheckoutSession(
            @RequestBody BuyVerificationAccessRequest request) {
        try {
            if (request.postId() == null) {
                return ResponseEntity.badRequest()
                        .body(SubscriptionResponse.builder()
                                .message("Post ID es requerido")
                                .success(false)
                                .build());
            }

            // Obtener usuario autenticado
            UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            UserEntity userEntity = (UserEntity) authentication.getPrincipal();

            String successUrl = frontendUrl + "/verification-details/" + request.postId() + "?justBought=true";

            String checkoutUrl = stripeService.createVerificationAccessCheckoutSession(
                request.postId(), 
                userEntity, 
                successUrl);

            return ResponseEntity.ok(SubscriptionResponse.builder()
                    .checkoutUrl(checkoutUrl)
                    .success(true)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(SubscriptionResponse.builder()
                            .message("Error: " + e.getMessage())
                            .success(false)
                            .build());
        }
    }
    /**
     * Webhook de Stripe para eventos de suscripción
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) throws JsonProcessingException, StripeException {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }
        log.info("➡Evento: {}", event.getType());
        if ("checkout.session.completed".equals(event.getType())) {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            Map<String, Object> sessionMap = null;
            String rawJson = deserializer.getRawJson();
            try {
                ObjectMapper mapper = new ObjectMapper();
                sessionMap = mapper.readValue(rawJson, Map.class);
            } catch (Exception ex) {
                log.error("Error al mapear manualmente el Session: {}", ex.getMessage());
                return ResponseEntity.badRequest().body("No se pudo deserializar el objeto Session");
            }
            if (sessionMap != null) {
                Map<String, Object> metadata = (Map<String, Object>) sessionMap.get("metadata");
                String postId = (String) metadata.get("postId");
                String userId = (String) metadata.get("userId");
                if (sessionMap.get("mode").equals("payment")){
                   UserEntity user = userRepository.findById(Long.parseLong(userId)).orElse(null);
                   PostEntity post = postRepository.findById( Long.parseLong(postId)).orElse(null);
                    if (user != null && post != null) {
                        if (!user.getUnlockedTechnicalVerifications().contains(post)) {
                            user.getUnlockedTechnicalVerifications().add(post);
                            userRepository.save(user);
                            log.info("\uD83D\uDD13 Acceso a verificación desbloqueado para usuario: {} y post: {}", user.getEmail(), postId);
                        } else {
                            log.warn("Usuario ya tenía acceso a esta verificación: {} y post: {}", user.getEmail(), postId);
                        }
                    }
                } else if (sessionMap.get("mode").equals("subscription")) {
                    Invoice invoice = Invoice.retrieve((String)sessionMap.get("invoice"));
                    String email = (String) sessionMap.get("customer_email");
                    log.info("📧 Email del cliente: {}", email);
                    UserEntity user = userRepository.findByEmail(email).orElseThrow(()->new UsernameNotFoundException("Usuario no encontrado con email: " + email));
                    log.info("👤 Usuario encontrado: {}", user);
                    if (!user.getRoles().contains(Role.PREMIUM)) {
                        user.setStripeSubscriptionId(invoice.getSubscription());
                        user.getRoles().add(Role.PREMIUM);
                    }
                    user.setTechnicalVerificationCredits(3);
                    user.setTechnicalVerificationReportsCredits(3);
                    userRepository.save(user);
                    log.info("🚀 Usuario actualizado a PREMIUM y créditos reseteados: {}", user.getEmail());
                }
            } else {
                log.error("Session es null incluso tras deserialización manual");
            }
        } else if ("customer.subscription.deleted".equals(event.getType())) {
            System.out.println("➡️ Evento: customer.subscription.deleted");
            String rawJson = event.getDataObjectDeserializer().getRawJson();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawJson);
            String subscriptionId = root.get("id").asText();
            System.out.println("📦 subscriptionId desde rawJson: " + subscriptionId);
            var userOpt = userRepository.findByStripeSubscriptionId(subscriptionId);
            if (userOpt.isPresent()) {
                UserEntity user = userOpt.get();
                if (user.getRoles() != null) {
                    user.getRoles().remove(org.cinos.core.users.model.Role.PREMIUM);
                }
                user.setStripeSubscriptionId(null);
                user.setTechnicalVerificationCredits(0);
                user.setTechnicalVerificationReportsCredits(0);
                userRepository.save(user);
                log.info("🚨 Suscripción cancelada para {}: rol PREMIUM removido, créditos reseteados a 0", user.getEmail());
            } else {
                System.err.println("❌ Usuario no encontrado con subscriptionId: " + subscriptionId);
            }
        } else if ("customer.subscription.updated".equals(event.getType())) {
            String rawJson = event.getDataObjectDeserializer().getRawJson();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawJson);
            String subscriptionId = root.get("id").asText();
            String status = root.get("status").asText();

            var userOpt = userRepository.findByStripeSubscriptionId(subscriptionId);
            if (userOpt.isPresent()) {
                UserEntity user = userOpt.get();
                
                // Si la suscripción se canceló pero aún está activa hasta el final del período
                if ("canceled".equals(status) || "unpaid".equals(status)) {
                    System.out.println("⚠️ Suscripción cancelada pero aún activa hasta el final del período para usuario: " + user.getEmail());
                    // El rol premium se mantiene hasta que la suscripción realmente termine
                } else if ("active".equals(status) || "trialing".equals(status)) {
                    log.info("✅ Suscripción activa/renovada para usuario: {}", user.getEmail());
                    if (user.getRoles() == null) {
                        user.setRoles(new ArrayList<>());
                    }
                    if (!user.getRoles().contains(Role.PREMIUM)) {
                        user.getRoles().add(Role.PREMIUM);
                    }
                    // Resetear créditos en cada renovación mensual
                    user.setTechnicalVerificationCredits(3);
                    user.setTechnicalVerificationReportsCredits(3);
                    userRepository.save(user);
                    log.info("🔄 Créditos renovados (3/3) para usuario: {}", user.getEmail());
                }
            } else {
                System.err.println("❌ Usuario no encontrado con subscriptionId: " + subscriptionId);
            }
        } else if ("charge.succeeded".equals(event.getType())){
            String rawJson = event.getDataObjectDeserializer().getRawJson();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(rawJson);
            String chargeId = root.get("id").asText();
            String customerId = root.get("customer").asText();
            String paymentMethodId = root.get("payment_method").asText();
            Double amount = root.get("amount").asDouble() / 100;
            String status = root.get("status").asText();

            // Guardar detalles del pago en la base de datos
            PaymentDetail paymentDetail = PaymentDetail.builder()
                    .id(chargeId)
                    .status(status)
                    .customerId(customerId)
                    .customerEmail(root.get("billing_details").get("email").asText())
                    .paymentMethodId(paymentMethodId)
                    .price(amount)
                    .createdAt(java.time.LocalDateTime.now())
                    .updatedAt(java.time.LocalDateTime.now())
                    .subscriptionId(root.has("invoice") ? root.get("invoice").get("subscription").asText() : null)
                    .build();
            paymentDetailRepository.save(paymentDetail);
            log.info("💳 Detalles del pago guardados: {}", paymentDetail);
        } else {
            log.info("Evento no manejado: {}", event.getType());
        }

        return ResponseEntity.ok("Webhook processed successfully");
    }

    /**
     * Compra acceso a un informe técnico específico
     */
    @PostMapping("/buy-verification-access")
    public ResponseEntity<Map<String, String>> buyVerificationAccess(
            @RequestBody BuyVerificationAccessRequest request,
            @AuthenticationPrincipal UserEntity user) {
        try {
            String clientSecret = stripeService.createVerificationAccessPaymentIntent(request.postId(), user);
            return ResponseEntity.ok(Map.of("clientSecret", clientSecret));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
