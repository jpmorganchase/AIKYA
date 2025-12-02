package com.aikya.orchestrator.client.model.seeds

import jakarta.persistence.*
import lombok.Getter
import lombok.Setter

@Getter
@Setter
@Entity
@Table(name = "domains")
class DomainEntity {
    public constructor() {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
    @Column(name = "name")
    var name: String? = ""
    @Column(name = "label")
    var label: String? = ""
    @Column(name = "status")
    var status: String? = ""

}