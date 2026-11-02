import { computed, inject, Injectable, Signal } from '@angular/core';
import { DataModelStore } from '../data-model.store';
import { DirectResolvedField, EdgeResolvedField, ResolvedField } from '../models/resolved-field.model';
import { DataModelField } from '../models/data-model-node.model';

@Injectable({ providedIn: 'root' })
export class DataModelNodeFieldResolver {
  private readonly store = inject(DataModelStore);

  resolveFields(nodeId: number): Signal<ResolvedField[]> {
    return computed(() => {
      const nodes = this.store.nodes();
      const edges = this.store.edges();

      const node = nodes.find(n => n.nodeId === nodeId);

      const direct: DirectResolvedField[] = (node?.fields ?? []).map(field => ({
        source: 'direct' as const,
        field: field,
        position: field.position,
      }));

      const allFields = nodes.flatMap(n => n.fields);

      const fromEdges: EdgeResolvedField[] = edges
        .filter(e => e.toNodeId === nodeId)
        .flatMap(e => {
          return e.fields.map(field => ({
            source: 'edge' as const,
            field: field,
            edge: e,
            referencedField: allFields.find(f => f.fieldId === field.sourceNodeFieldId) as DataModelField,
            position: field.position,
          }));
        });

      return [...direct, ...fromEdges].sort((a, b) => {
        if (a.position !== b.position) {
          return a.position - b.position;
        }
        if (a.source !== b.source) {
          return a.source === 'direct' ? -1 : 1;
        }

        const idA = a.field.fieldId ?? 0;
        const idB = b.field.fieldId ?? 0;
        return idA - idB;
      });
    });
  }
}
