package dev.zemco.schemalens.modeling.edges

import dev.zemco.schemalens.modeling.fields.DataModelEdgeField
import jakarta.persistence.EntityManager
import org.springframework.stereotype.Service

@Service
class DataModelEdgeWriteServiceImpl(
    private val entityManager: EntityManager,
    private val edgeRepository: DataModelEdgeRepository,
) : DataModelEdgeWriteService {

    override fun persistAndMapEdges(edges: List<DataModelEdge>): List<DataModelEdgeDto> {
        if (edges.isEmpty()) {
            return emptyList()
        }

        edges.forEach { edge ->
            edge.fields.forEach { persistFieldWithParents(it) }
        }

        val persistedById = edgeRepository.saveAll(edges).associateBy { it.id!! }
        return edges.map { DataModelEdgeDto.from(persistedById.getValue(it.id!!)) }
    }

    private fun persistFieldWithParents(field: DataModelEdgeField) {
        if (field.id != null) {
            return
        }

        val parent = field.parentField
        if (parent is DataModelEdgeField) {
            persistFieldWithParents(parent)
        }

        entityManager.persist(field)
    }
}
