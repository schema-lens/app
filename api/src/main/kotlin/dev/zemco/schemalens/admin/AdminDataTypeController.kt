package dev.zemco.schemalens.admin

import dev.zemco.schemalens.modeling.types.defaults.DataModelDefaultDataTypeService
import dev.zemco.schemalens.modeling.types.defaults.DuplicateDefaultDataTypeParameterException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/dataType")
class AdminDataTypeController(
    private val service: DataModelDefaultDataTypeService,
) {
    @GetMapping
    fun getDataTypes(): List<AdminDataTypeDto> = service.getDataTypes()

    @PostMapping
    fun createDataType(@RequestBody @Valid dto: AdminDataTypeInputDto): ResponseEntity<AdminDataTypeDto> = try {
        service.createDataType(dto).let { ResponseEntity.ok(it) }
    } catch (_: DuplicateDefaultDataTypeParameterException) {
        ResponseEntity.badRequest().build()
    }

    @PutMapping("/{typeId}")
    fun updateDataType(
        @PathVariable typeId: Long,
        @RequestBody @Valid dto: AdminDataTypeInputDto
    ): ResponseEntity<AdminDataTypeDto> = try {
        service.updateDataType(typeId, dto).let { ResponseEntity.ok(it) }
    } catch (_: DuplicateDefaultDataTypeParameterException) {
        ResponseEntity.badRequest().build()
    }

    @DeleteMapping("/{typeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDataType(@PathVariable typeId: Long) = service.deleteDataType(typeId)
}
