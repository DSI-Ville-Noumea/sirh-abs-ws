// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;
import nc.noumea.mairie.abs.domain.AgentWeekAlimManuelle;

privileged aspect AgentWeekAlimManuelle_Roo_Jpa_Entity {
    
    declare @type: AgentWeekAlimManuelle: @Entity;
    
    declare @type: AgentWeekAlimManuelle: @Table(name = "ABS_AGENT_WEEK_ALIM_MANUELLE");
    
    @Version
    @Column(name = "version")
    private Integer AgentWeekAlimManuelle.version;
    
    public Integer AgentWeekAlimManuelle.getVersion() {
        return this.version;
    }
    
    public void AgentWeekAlimManuelle.setVersion(Integer version) {
        this.version = version;
    }
    
}