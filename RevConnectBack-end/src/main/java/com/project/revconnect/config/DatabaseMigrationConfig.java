package com.project.revconnect.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrateColumns() {
        try {
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN role VARCHAR(255)");
            System.out.println("Database migration: ensured users.role is VARCHAR(255)");
        } catch (Exception e) {
            System.out.println("Database migration note (users.role): " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("UPDATE posts SET is_published = TRUE WHERE is_published IS NULL");
            jdbcTemplate.execute("ALTER TABLE posts MODIFY COLUMN is_published BOOLEAN NOT NULL DEFAULT TRUE");
            System.out.println("Database migration: normalized posts.is_published");
        } catch (Exception e) {
            System.out.println("Database migration note (posts.is_published): " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("UPDATE posts SET is_pinned = FALSE WHERE is_pinned IS NULL");
            jdbcTemplate.execute("ALTER TABLE posts MODIFY COLUMN is_pinned BOOLEAN NOT NULL DEFAULT FALSE");
            System.out.println("Database migration: normalized posts.is_pinned");
        } catch (Exception e) {
            System.out.println("Database migration note (posts.is_pinned): " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE posts ADD COLUMN collaborator_user_id BIGINT NULL");
            jdbcTemplate.execute("ALTER TABLE posts ADD COLUMN collab_accepted BOOLEAN DEFAULT TRUE");
            jdbcTemplate.execute("ALTER TABLE posts ADD COLUMN series_name VARCHAR(140) NULL");
            jdbcTemplate.execute("ALTER TABLE posts ADD COLUMN series_order INT NULL");
            System.out.println("Database migration: added creator collab/series columns");
        } catch (Exception e) {
            System.out.println("Database migration note (posts creator columns): " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("UPDATE posts SET collab_accepted = TRUE WHERE collab_accepted IS NULL");
            jdbcTemplate.execute("ALTER TABLE posts MODIFY COLUMN collab_accepted BOOLEAN NOT NULL DEFAULT TRUE");
            System.out.println("Database migration: normalized posts.collab_accepted");
        } catch (Exception e) {
            System.out.println("Database migration note (posts.collab_accepted): " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE following ADD COLUMN created_at DATETIME NULL");
            jdbcTemplate.execute("UPDATE following SET created_at = NOW() WHERE created_at IS NULL");
            System.out.println("Database migration: added following.created_at");
        } catch (Exception e) {
            System.out.println("Database migration note (following.created_at): " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE userprofile ADD COLUMN age INT NULL");
            jdbcTemplate.execute("ALTER TABLE userprofile ADD COLUMN gender VARCHAR(50) NULL");
            System.out.println("Database migration: added userprofile age/gender");
        } catch (Exception e) {
            System.out.println("Database migration note (userprofile age/gender): " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("UPDATE stories SET is_active = TRUE WHERE is_active IS NULL");
            jdbcTemplate.execute("ALTER TABLE stories MODIFY COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE");
            System.out.println("Database migration: normalized stories.is_active");
        } catch (Exception e) {
            System.out.println("Database migration note (stories.is_active): " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("UPDATE stories SET active = COALESCE(active, is_active, TRUE)");
            jdbcTemplate.execute("ALTER TABLE stories MODIFY COLUMN active BOOLEAN NOT NULL DEFAULT TRUE");
            System.out.println("Database migration: normalized legacy stories.active");
        } catch (Exception e) {
            System.out.println("Database migration note (stories.active legacy column): " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("UPDATE stories SET subscriber_only = FALSE WHERE subscriber_only IS NULL");
            jdbcTemplate.execute("ALTER TABLE stories MODIFY COLUMN subscriber_only BOOLEAN NOT NULL DEFAULT FALSE");
            System.out.println("Database migration: normalized stories.subscriber_only");
        } catch (Exception e) {
            System.out.println("Database migration note (stories.subscriber_only): " + e.getMessage());
        }

        try {
            var uniqueStoryUserIndexes = jdbcTemplate.queryForList(
                    "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.STATISTICS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'stories' " +
                            "AND COLUMN_NAME = 'user_id' AND NON_UNIQUE = 0 AND INDEX_NAME <> 'PRIMARY'",
                    String.class
            );
            for (String indexName : uniqueStoryUserIndexes) {
                jdbcTemplate.execute("ALTER TABLE stories DROP INDEX " + indexName);
            }
            if (!uniqueStoryUserIndexes.isEmpty()) {
                System.out.println("Database migration: removed unique story-per-user constraint(s)");
            }
        } catch (Exception e) {
            System.out.println("Database migration note (stories unique user index): " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE messages ADD COLUMN is_read BOOLEAN NULL");
            jdbcTemplate.execute("UPDATE messages SET is_read = FALSE WHERE is_read IS NULL");
            jdbcTemplate.execute("ALTER TABLE messages MODIFY COLUMN is_read BOOLEAN NOT NULL DEFAULT FALSE");
            System.out.println("Database migration: normalized messages.is_read");
        } catch (Exception e) {
            System.out.println("Database migration note (messages.is_read): " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE business_profile ADD COLUMN business_category VARCHAR(120) NULL");
            System.out.println("Database migration: added business_profile.business_category");
        } catch (Exception e) {
            System.out.println("Database migration note (business_profile.business_category): " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("UPDATE brand_collaboration_opportunities SET view_count = 0 WHERE view_count IS NULL");
            jdbcTemplate.execute("ALTER TABLE brand_collaboration_opportunities MODIFY COLUMN view_count BIGINT NOT NULL DEFAULT 0");
            System.out.println("Database migration: normalized brand_collaboration_opportunities.view_count");
        } catch (Exception e) {
            System.out.println("Database migration note (brand_collaboration_opportunities.view_count): " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE brand_collaboration_applications ADD COLUMN promotion_details VARCHAR(2000) NULL");
            jdbcTemplate.execute("ALTER TABLE brand_collaboration_applications ADD COLUMN promotion_product_image_url VARCHAR(2000) NULL");
            jdbcTemplate.execute("ALTER TABLE brand_collaboration_applications ADD COLUMN promotion_product_link VARCHAR(500) NULL");
            jdbcTemplate.execute("ALTER TABLE brand_collaboration_applications ADD COLUMN promotion_business_post_id BIGINT NULL");
            jdbcTemplate.execute("ALTER TABLE brand_collaboration_applications ADD COLUMN creator_confirmation_note VARCHAR(1500) NULL");
            jdbcTemplate.execute("ALTER TABLE brand_collaboration_applications ADD COLUMN payment_status VARCHAR(20) NULL");
            jdbcTemplate.execute("ALTER TABLE brand_collaboration_applications ADD COLUMN payment_amount DOUBLE NULL");
            jdbcTemplate.execute("ALTER TABLE brand_collaboration_applications ADD COLUMN payment_reference VARCHAR(120) NULL");
            jdbcTemplate.execute("ALTER TABLE brand_collaboration_applications ADD COLUMN promotion_requested_at DATETIME NULL");
            jdbcTemplate.execute("ALTER TABLE brand_collaboration_applications ADD COLUMN promotion_accepted_at DATETIME NULL");
            jdbcTemplate.execute("ALTER TABLE brand_collaboration_applications ADD COLUMN creator_confirmed_at DATETIME NULL");
            jdbcTemplate.execute("ALTER TABLE brand_collaboration_applications ADD COLUMN completed_at DATETIME NULL");
            jdbcTemplate.execute("ALTER TABLE brand_collaboration_applications ADD COLUMN paid_at DATETIME NULL");
            System.out.println("Database migration: added promotion lifecycle columns to brand_collaboration_applications");
        } catch (Exception e) {
            System.out.println("Database migration note (brand_collaboration_applications promotion columns): " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("UPDATE brand_collaboration_applications SET payment_status = 'UNPAID' WHERE payment_status IS NULL");
            jdbcTemplate.execute("ALTER TABLE brand_collaboration_applications MODIFY COLUMN payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID'");
            System.out.println("Database migration: normalized brand_collaboration_applications.payment_status");
        } catch (Exception e) {
            System.out.println("Database migration note (brand_collaboration_applications.payment_status): " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE business_collaboration_proposals ADD COLUMN promotion_details VARCHAR(2000) NULL");
            jdbcTemplate.execute("ALTER TABLE business_collaboration_proposals ADD COLUMN promotion_product_image_url VARCHAR(2000) NULL");
            jdbcTemplate.execute("ALTER TABLE business_collaboration_proposals ADD COLUMN promotion_product_link VARCHAR(500) NULL");
            jdbcTemplate.execute("ALTER TABLE business_collaboration_proposals ADD COLUMN promotion_business_post_id BIGINT NULL");
            jdbcTemplate.execute("ALTER TABLE business_collaboration_proposals ADD COLUMN creator_confirmation_note VARCHAR(1500) NULL");
            jdbcTemplate.execute("ALTER TABLE business_collaboration_proposals ADD COLUMN payment_status VARCHAR(20) NULL");
            jdbcTemplate.execute("ALTER TABLE business_collaboration_proposals ADD COLUMN payment_amount DOUBLE NULL");
            jdbcTemplate.execute("ALTER TABLE business_collaboration_proposals ADD COLUMN payment_reference VARCHAR(120) NULL");
            jdbcTemplate.execute("ALTER TABLE business_collaboration_proposals ADD COLUMN promotion_requested_at DATETIME NULL");
            jdbcTemplate.execute("ALTER TABLE business_collaboration_proposals ADD COLUMN promotion_accepted_at DATETIME NULL");
            jdbcTemplate.execute("ALTER TABLE business_collaboration_proposals ADD COLUMN creator_confirmed_at DATETIME NULL");
            jdbcTemplate.execute("ALTER TABLE business_collaboration_proposals ADD COLUMN completed_at DATETIME NULL");
            jdbcTemplate.execute("ALTER TABLE business_collaboration_proposals ADD COLUMN paid_at DATETIME NULL");
            System.out.println("Database migration: added promotion lifecycle columns to business_collaboration_proposals");
        } catch (Exception e) {
            System.out.println("Database migration note (business_collaboration_proposals promotion columns): " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("UPDATE business_collaboration_proposals SET payment_status = 'UNPAID' WHERE payment_status IS NULL");
            jdbcTemplate.execute("ALTER TABLE business_collaboration_proposals MODIFY COLUMN payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID'");
            System.out.println("Database migration: normalized business_collaboration_proposals.payment_status");
        } catch (Exception e) {
            System.out.println("Database migration note (business_collaboration_proposals.payment_status): " + e.getMessage());
        }
    }
}
