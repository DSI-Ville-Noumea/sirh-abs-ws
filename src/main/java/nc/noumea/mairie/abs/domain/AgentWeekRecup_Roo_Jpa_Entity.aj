// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;
import nc.noumea.mairie.abs.domain.AgentWeekRecup;

privileged aspect AgentWeekRecup_Roo_Jpa_Entity {
    
    declare @type: AgentWeekRecup: @Entity;
    
    declare @type: AgentWeekRecup: @Table(name = "ABS_AGENT_WEEK_RECUP");
    
    @Version
    @Column(name = "version")
    private Integer AgentWeekRecup.version;
    
    public Integer AgentWeekRecup.getVersion() {
        return this.version;
    }
    
    public void AgentWeekRecup.setVersion(Integer version) {
        this.version = version;
    }
    
}
