package dev.zemco.schemalens.modeling.nodes

import dev.zemco.schemalens.modeling.fields.DataModelNodeFieldEditRequestInvalidException
import dev.zemco.schemalens.modeling.models.DataModel
import dev.zemco.schemalens.modeling.models.DataModelModificationDto
import dev.zemco.schemalens.validation.OnCreate
import dev.zemco.schemalens.validation.OnUpdate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/model/{model}/node")
class DataModelNodeController(
    private val service: DataModelNodeService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createNode(
        @PathVariable model: DataModel,
        @RequestBody @Validated(OnCreate::class) dto: DataModelNodeDefinitionInputDto,
    ): DataModelNodeDto = service.createNode(model, dto)

    @PutMapping("/{nodeId}/definition")
    fun updateNodeDefinition(
        @PathVariable model: DataModel,
        @PathVariable nodeId: Long,
        @RequestBody @Validated(OnUpdate::class) dto: DataModelNodeDefinitionInputDto,
    ): ResponseEntity<DataModelModificationDto> =
        try {
            ResponseEntity.ok(service.updateNodeDefinition(model, nodeId, dto))
        } catch (_: DataModelNodeFieldEditRequestInvalidException) {
            ResponseEntity.badRequest().build()
        }

    @DeleteMapping("/{nodeId}")
    fun deleteNode(
        @PathVariable model: DataModel,
        @PathVariable nodeId: Long,
    ): DataModelModificationDto = service.deleteNode(model, nodeId)
}
