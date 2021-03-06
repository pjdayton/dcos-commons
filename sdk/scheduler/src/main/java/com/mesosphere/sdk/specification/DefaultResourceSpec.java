package com.mesosphere.sdk.specification;

import com.mesosphere.sdk.offer.ResourceRequirement;
import com.mesosphere.sdk.offer.ResourceUtils;
import com.mesosphere.sdk.specification.validation.PositiveScalarProtoValue;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.mesos.Protos;
import com.mesosphere.sdk.specification.validation.ValidationUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Optional;

/**
 * This class provides a default implementation of the ResourceSpec interface.
 */
public class DefaultResourceSpec implements ResourceSpec {
    @NotNull
    @Size(min = 1)
    private final String name;
    @NotNull
    @PositiveScalarProtoValue
    private final Protos.Value value;
    @NotNull
    @Size(min = 1)
    private final String role;
    @NotNull
    @Size(min = 1)
    private final String principal;
    private final String envKey;

    @JsonCreator
    public DefaultResourceSpec(
            @JsonProperty("name") String name,
            @JsonProperty("value") Protos.Value value,
            @JsonProperty("role") String role,
            @JsonProperty("principal") String principal,
            @JsonProperty("env-key") String envKey) {
        this.name = name;
        this.value = value;
        this.role = role;
        this.principal = principal;
        this.envKey = envKey;
    }

    private DefaultResourceSpec(Builder builder) {
        name = builder.name;
        value = builder.value;
        role = builder.role;
        principal = builder.principal;
        envKey = builder.envKey;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(DefaultResourceSpec copy) {
        Builder builder = new Builder();
        builder.name = copy.name;
        builder.value = copy.value;
        builder.role = copy.role;
        builder.principal = copy.principal;
        builder.envKey = copy.envKey;
        return builder;
    }

    @Override
    public Protos.Value getValue() {
        return value;
    }

    @Override
    public String getRole() {
        return role;
    }

    @Override
    public String getPrincipal() {
        return principal;
    }

    @Override
    public ResourceRequirement getResourceRequirement(Protos.Resource resource) {
        return new ResourceRequirement(resource == null ? ResourceUtils.getDesiredResource(this) : resource);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public Optional<String> getEnvKey() {
        return Optional.ofNullable(envKey);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }


    /**
     * {@code DefaultResourceSpec} builder static inner class.
     */
    public static final class Builder {
        private String name;
        private Protos.Value value;
        private String role;
        private String principal;
        private String envKey;

        private Builder() {
        }

        /**
         * Sets the {@code name} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param name the {@code name} to set
         * @return a reference to this Builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the {@code value} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param value the {@code value} to set
         * @return a reference to this Builder
         */
        public Builder value(Protos.Value value) {
            this.value = value;
            return this;
        }

        /**
         * Sets the {@code role} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param role the {@code role} to set
         * @return a reference to this Builder
         */
        public Builder role(String role) {
            this.role = role;
            return this;
        }

        /**
         * Sets the {@code principal} and returns a reference to this Builder so that the methods can be chained
         * together.
         *
         * @param principal the {@code principal} to set
         * @return a reference to this Builder
         */
        public Builder principal(String principal) {
            this.principal = principal;
            return this;
        }

        /**
         * Sets the {@code envKey} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param envKey the {@code envKey} to set
         * @return a reference to this Builder
         */
        public Builder envKey(String envKey) {
            this.envKey = envKey;
            return this;
        }

        /**
         * Returns a {@code DefaultResourceSpec} built from the parameters previously set.
         *
         * @return a {@code DefaultResourceSpec} built with parameters of this
         * {@code DefaultResourceSpec.Builder}
         */
        public DefaultResourceSpec build() {
            DefaultResourceSpec defaultResourceSpec = new DefaultResourceSpec(this);
            ValidationUtils.validate(defaultResourceSpec);
            return defaultResourceSpec;
        }
    }
}
