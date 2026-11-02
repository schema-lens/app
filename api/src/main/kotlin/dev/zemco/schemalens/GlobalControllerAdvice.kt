package dev.zemco.schemalens

import dev.zemco.schemalens.auth.ResourceAccessDeniedException
import dev.zemco.schemalens.backup.BackupAlreadyRunningException
import dev.zemco.schemalens.modeling.edges.DataModelEdgeIdentifyingCycleException
import dev.zemco.schemalens.modeling.fields.FieldDataTypeParameterExistsException
import dev.zemco.schemalens.modeling.fields.FieldDataTypeParameterUnknownException
import dev.zemco.schemalens.modeling.fields.FieldNameInvalidException
import dev.zemco.schemalens.modeling.fields.FieldNameNotUniqueException
import dev.zemco.schemalens.modeling.nodes.NodeExistsException
import dev.zemco.schemalens.modeling.types.DataTypeExistsException
import dev.zemco.schemalens.modeling.types.DataTypeInUseException
import dev.zemco.schemalens.modeling.types.DataTypeMappingExistsException
import dev.zemco.schemalens.modeling.types.DataTypeParameterExistsException
import dev.zemco.schemalens.projects.ProjectConnectionException
import dev.zemco.schemalens.projects.ProjectConnectionFailureDto
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalControllerAdvice {

    @ExceptionHandler(ResourceAccessDeniedException::class)
    fun handleResourceAccessDeniedException(ex: ResourceAccessDeniedException) =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.message)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFoundException(ex: ResourceNotFoundException) =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.message)

    @ExceptionHandler(FieldNameInvalidException::class)
    fun handleInvalidFieldNameException(ex: FieldNameInvalidException) =
        ResponseEntity.badRequest().body(ex.message)

    @ExceptionHandler(
        DataTypeInUseException::class,
        DataTypeExistsException::class,
        DataTypeParameterExistsException::class,
        DataTypeMappingExistsException::class,
        NodeExistsException::class,
        FieldNameNotUniqueException::class,
        FieldDataTypeParameterExistsException::class,
        FieldDataTypeParameterUnknownException::class,
        DataModelEdgeIdentifyingCycleException::class,
        BackupAlreadyRunningException::class,
    )
    fun handleConflictException(ex: RuntimeException) =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ex.message)

    @ExceptionHandler(ProjectConnectionException::class)
    fun handleProjectConnectionException(ex: ProjectConnectionException) = ex.also { ex.printStackTrace() }.run {
        ResponseEntity.status(HttpStatus.CONFLICT)
            .contentType(MediaType.APPLICATION_JSON)
            .body(ProjectConnectionFailureDto(
                type = ex.type,
                message = ex.message,
            ))}

}
