import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  input,
  signal,
  untracked,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import {
  DataModelNodeFieldsTableComponent,
  DirectFieldReference,
  EdgeFieldReference,
} from '../data-model-node-fields-table/data-model-node-fields-table.component';
import { DataModelEdgeFieldEdit, DataModelField, DataModelNode } from '../../models/data-model-node.model';
import { EdgeResolvedField, ResolvedField } from '../../models/resolved-field.model';
import { DataModelEdge } from '../../models/data-model-edge.model';
import { DataModelNodeFieldResolver } from '../../services/data-model-node-field.resolver';
import { filter, finalize, Observable, tap } from 'rxjs';
import { DataModelStore } from '../../data-model.store';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatIconButton } from '@angular/material/button';
import { MatTooltip } from '@angular/material/tooltip';
import { MatIcon } from '@angular/material/icon';
import { DataModelDialogService } from '../../services/data-model-dialog.service';
import { SectionHeaderComponent } from '../../../shared/components/section-header/section-header.component';
import { TranslatePipe } from '../../../core/translate/translate.pipe';
import { DataModelEditor } from '../data-model-editor/data-model-editor.component';
import { DataModelModification } from '../../models/data-model.model';
import { dataModelNodeNameValidators } from '../../validators/data-model-name.validators';
import { GO_TO_EDGE_HANDLER } from '../../services/data-model-go-to-edge-handler.service';

export const DEFAULT_NODE_FIELD_ADD_BUTTON_ID = 'node-editor-add-field-button';

@Component({
  selector: 'app-data-model-node-editor',
  templateUrl: './data-model-node-editor.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ReactiveFormsModule,
    MatFormField,
    MatLabel,
    MatInput,
    DataModelNodeFieldsTableComponent,
    MatIconButton,
    MatTooltip,
    MatIcon,
    SectionHeaderComponent,
    TranslatePipe,
  ],
})
export class DataModelNodeEditorComponent implements DataModelEditor {
  node = input.required<DataModelNode>();
  compact = input<boolean>(false);
  addFieldButtonId = input<string>(DEFAULT_NODE_FIELD_ADD_BUTTON_ID);

  nodeId = computed(() => this.node().nodeId ?? -1);

  store = inject(DataModelStore);
  private goToEdgeHandler = inject(GO_TO_EDGE_HANDLER);
  private fieldResolver = inject(DataModelNodeFieldResolver);
  private fb = inject(FormBuilder);
  private modelDialogService = inject(DataModelDialogService);
  private destroyRef = inject(DestroyRef);

  form = this.fb.nonNullable.group({
    name: this.fb.nonNullable.control('', []),
  });
  fields = signal<ResolvedField[]>([]);

  private modified = false;
  pendingSaveId = signal<number | null>(null);

  constructor() {
    effect(() => {
      const node = this.node();
      const existingNames = this.store.nodes()
        .filter(existingNode => existingNode.nodeId !== node.nodeId)
        .map(existingNode => existingNode.name);

      this.form.controls.name.setValidators(dataModelNodeNameValidators(existingNames));
      this.form.controls.name.updateValueAndValidity({ emitEvent: false });
    });

    effect(() => {
      const node = this.node();

      this.form.reset({
        name: node.name,
      });

      this.modified = false;
    });

    effect(() => {
      if (this.pendingSaveId() !== null) {
        return;
      }

      const fields = this.fieldResolver.resolveFields(this.nodeId());
      untracked(() => this.fields.set(fields()));
    });

    this.form.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => (this.modified = true));
  }

  orderChanged(fields: ResolvedField[]): void {
    this.fields.set(fields);
    this.modified = true;
  }

  addDirectField(): void {
    const existingNames = this.getExistingFieldNames();

    this.modelDialogService
      .openFieldFormDialog(existingNames)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(field => {
        if (!field) {
          return;
        }

        const position = this.nextFieldPosition();
        const newField = { ...field, fieldId: null, position };
        this.fields.update(fields => [...fields, { source: 'direct', field: newField, position }]);
        this.modified = true;
      });
  }

  editDirectField({ index, field }: DirectFieldReference): void {
    const existingNames = this.getExistingFieldNames(field.name);

    this.modelDialogService
      .openFieldFormDialog(existingNames, field)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(updated => {
        if (!updated) {
          return;
        }

        this.fields.update(fields => {
          const result = [...fields];
          result[index] = { source: 'direct', field: updated, position: fields[index].position };
          return result;
        });

        this.modified = true;
      });
  }

  deleteDirectField({ index }: DirectFieldReference): void {
    this.modelDialogService
      .openDeleteFieldConfirmationDialog()
      .pipe(
        filter(r => !!r),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        this.fields.update(fields => {
          const result = [...fields];
          result.splice(index, 1);
          return result;
        });

        this.modified = true;
      });
  }

  editEdgeField({ index, field }: EdgeFieldReference): void {
    const existingNames = this.getExistingFieldNames(field.name);

    this.modelDialogService
      .openEdgeFieldFormDialog(existingNames, field)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(updated => {
        if (!updated) {
          return;
        }

        this.fields.update(fields => {
          const result = [...fields];
          result[index] = { ...(result[index] as EdgeResolvedField), field: updated };
          return result;
        });

        this.modified = true;
      });
  }

  goToEdge(edge: DataModelEdge): void {
    this.goToEdgeHandler.goToEdge(edge);
  }

  save(): Observable<DataModelModification> | null {
    if (!this.modified || this.pendingSaveId() !== null) {
      return null;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return null;
    }

    const { name } = this.form.getRawValue();
    const fields: DataModelField[] = [];
    const edgeFields: DataModelEdgeFieldEdit[] = [];

    this.fields().forEach((resolved, position) => {
      if (resolved.source === 'direct') {
        fields.push({ ...resolved.field, position });
      } else {
        edgeFields.push({ fieldId: resolved.field.fieldId, name: resolved.field.name, position });
      }
    });

    const nodeId = this.nodeId();
    this.pendingSaveId.set(nodeId);

    return this.store.updateNodeDefinition({ nodeId, name, fields, edgeFields }).pipe(
      tap(() => this.modified = false),
      finalize(() => this.pendingSaveId.set(null)),
    );
  }

  private nextFieldPosition(): number {
    return this.fields().reduce((max, field) => Math.max(max, field.position), 0) + 1;
  }

  private getExistingFieldNames(excludeName?: string): string[] {
    const names = this.fields().map(f => f.field.name);

    return excludeName !== undefined
      ? names.filter(n => n !== excludeName)
      : names;
  }
}
