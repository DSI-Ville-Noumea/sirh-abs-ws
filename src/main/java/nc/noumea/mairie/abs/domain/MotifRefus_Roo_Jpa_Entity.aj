// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package nc.noumea.mairie.abs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Version;
import nc.noumea.mairie.abs.domain.MotifRefus;

privileged aspect MotifRefus_Roo_Jpa_Entity {
    
    declare @type: MotifRefus: @Entity;
    
    declare @type: MotifRefus: @Table(name = "ABS_MOTIF_REFUS");
    
    @Version
    @Column(name = "version")
    private Integer MotifRefus.version;
    
    public Integer MotifRefus.getVersion() {
        return this.version;
    }
    
    public void MotifRefus.setVersion(Integer version) {
        this.version = version;
    }
    
}