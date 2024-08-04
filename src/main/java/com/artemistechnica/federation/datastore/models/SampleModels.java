package com.artemistechnica.federation.datastore.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.UUID;

public class SampleModels {

    @Entity
    @Getter
    @Builder
    @JsonAutoDetect
    @NoArgsConstructor
    @AllArgsConstructor
    @Table(name = "Messages")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Message {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        @ManyToOne
        @JoinColumn(name = "Account_ID", nullable = false)
        private Account account;

        @Column(nullable = false)
        private String message;

        @CreationTimestamp
        @Column(nullable = false, updatable = false)
        private Date created;
    }

    @Entity
    @Getter
    @Builder
    @JsonAutoDetect
    @NoArgsConstructor
    @AllArgsConstructor
    @Table(name = "Accounts")
    @SecondaryTables({
            @SecondaryTable(name = "accounttypes", pkJoinColumns = @PrimaryKeyJoinColumn(name = "account_type")),
            @SecondaryTable(name = "clients", pkJoinColumns = @PrimaryKeyJoinColumn(name = "client")),
            @SecondaryTable(name = "role_fk_arrays", pkJoinColumns = @PrimaryKeyJoinColumn(name = "roles"))
    })
    public static class Account {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @ManyToOne
        @JoinColumn(name = "Account_Type", nullable = false)
        private AccountType accountType;

        @Column(name = "First_Name", nullable = false)
        private String firstName;

        @Column(name = "Last_Name", nullable = false)
        private String lastName;

        @Column(nullable = false, unique = true)
        private String email;

        @Column(nullable = false)
        private Date dob;

        @ManyToOne
        @JoinColumn(name = "Client")
        private Client client;

        @ManyToOne
        @JoinColumn(name = "Roles")
        private RoleFkArray roles;

        @CreationTimestamp
        @Column(nullable = false, updatable = false)
        private Date created;

        @UpdateTimestamp
        @Column(nullable = false)
        private Date updated;
    }

    @Entity
    @Getter
    @Builder
    @JsonAutoDetect
    @NoArgsConstructor
    @AllArgsConstructor
    @Table(name = "Clients")
    public static class Client {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;

        @Column(nullable = false, unique = true)
        private String name;
    }

    @Entity
    @Getter
    @Builder
    @JsonAutoDetect
    @NoArgsConstructor
    @AllArgsConstructor
    @Table(name = "Grants")
    public static class Grant {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        @Column(nullable = false, unique = true)
        private String name;
    }

    @Entity
    @Getter
    @Builder
    @JsonAutoDetect
    @NoArgsConstructor
    @AllArgsConstructor
    @Table(name = "Role_FK_Arrays")
    public static class RoleFkArray {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        @Column(nullable = false)
        private int[] roleIds;
    }

    @Entity
    @Getter
    @Builder
    @JsonAutoDetect
    @NoArgsConstructor
    @AllArgsConstructor
    @Table(name = "Roles")
    public static class Role {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        @Column(nullable = false, unique = true)
        private String name;

        @Column(nullable = false)
        private int[] grants;
    }

    @Entity
    @Getter
    @Builder
    @JsonAutoDetect
    @NoArgsConstructor
    @AllArgsConstructor
    @Table(name = "AccountTypes")
    public static class AccountType {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        @Column(nullable = false, unique = true)
        private String name;

        @Column(nullable = false)
        private String description;
    }
}
