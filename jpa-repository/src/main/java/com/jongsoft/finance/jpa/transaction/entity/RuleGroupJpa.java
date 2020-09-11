package com.jongsoft.finance.jpa.transaction.entity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.jongsoft.finance.jpa.core.entity.EntityJpa;
import com.jongsoft.finance.jpa.user.entity.UserAccountJpa;

import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "rule_group")
public class RuleGroupJpa extends EntityJpa {

    private String name;
    private int sort;
    private boolean archived;

    @ManyToOne
    @JoinColumn
    private UserAccountJpa user;

    @Builder
    private RuleGroupJpa(String name, int sort, boolean archived, UserAccountJpa user) {
        this.name = name;
        this.sort = sort;
        this.archived = archived;
        this.user = user;
    }

    public RuleGroupJpa() {
    }

}
