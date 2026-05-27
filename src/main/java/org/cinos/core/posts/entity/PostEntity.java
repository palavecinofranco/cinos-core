package org.cinos.core.posts.entity;

import org.cinos.core.posts.models.CurrencySymbol;
import jakarta.persistence.*;
import lombok.*;
import org.cinos.core.posts.models.DocumentationStatus;
import org.cinos.core.posts.models.VerificationStatus;
import org.cinos.core.technical_verification.entity.TechnicalVerification;
import org.cinos.core.users.entity.AccountEntity;
import org.cinos.core.users.entity.UserEntity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "POSTS")
public class PostEntity implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String make;
    private String model;
    private String year;
    private Boolean isUsed;
    private Double price;
    @Enumerated(EnumType.STRING)
    private CurrencySymbol currencySymbol;
    private String kilometers;
    private String fuel;
    private String transmission;
    @Column(name = "publication_date")
    private LocalDateTime publicationDate;
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity userAccount;
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImageEntity> images = new ArrayList<>();
    private Boolean active;
    @ManyToMany
    @JoinTable(
            name = "account_saved_posts",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "account_id")
    )
    private List<AccountEntity> usersSaved;
    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private PostLocationEntity location;
    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private TechnicalVerification technicalVerification;
    @Enumerated(EnumType.STRING)
    private DocumentationStatus documentationStatus;
    private Boolean isVerified = (this.technicalVerification != null && this.technicalVerification.getStatus() == VerificationStatus.APPROVED);
    private Integer hp;
    private String motor;
    private String traccion;
    @ManyToMany(mappedBy = "unlockedTechnicalVerifications")
    private List<UserEntity> usersWithUnlockedVerification = new ArrayList<>();

}
