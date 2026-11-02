package dev.zemco.schemalens.modeling.edges

import dev.zemco.schemalens.modeling.fields.DataModelEdgeReferencedFieldsImmutableException
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
@RequestMapping("/model/{model}/edge")
class DataModelEdgeController(
    private val edgeService: DataModelEdgeService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createEdge(
        @PathVariable model: DataModel,
        @RequestBody @Validated(OnCreate::class) dto: DataModelEdgeInputDto,
    ): DataModelModificationDto = edgeService.createEdge(model, dto)

    @PutMapping("/{edgeId}")
    fun updateEdge(
        @PathVariable model: DataModel,
        @PathVariable edgeId: Long,
        @RequestBody @Validated(OnUpdate::class) dto: DataModelEdgeInputDto,
    ): ResponseEntity<DataModelModificationDto> =
        try {
            ResponseEntity.ok(edgeService.updateEdge(model, edgeId, dto))
        } catch (_: DataModelEdgeReferencedFieldsImmutableException) {
            ResponseEntity.badRequest().build()
        }

    @DeleteMapping("/{edgeId}")
    fun deleteEdge(
        @PathVariable model: DataModel,
        @PathVariable edgeId: Long,
    ): DataModelModificationDto = edgeService.deleteEdge(model, edgeId)
}
