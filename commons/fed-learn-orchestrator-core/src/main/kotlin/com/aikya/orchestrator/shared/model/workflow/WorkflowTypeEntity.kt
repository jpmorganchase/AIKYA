package com.aikya.orchestrator.shared.model.workflow

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*
import lombok.Getter
import lombok.Setter

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Getter
@Setter
@Table(name = "workflow_type")
class WorkflowTypeEntity {
    var name: String? = ""
    var desc: String? = ""
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}