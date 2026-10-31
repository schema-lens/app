import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButton } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { toSignal } from '@angular/core/rxjs-interop';
import { map, startWith } from 'rxjs';
import { AdminDataType, AdminDataTypeInput } from '../../models/admin-data-type.model';
import { TranslatePipe } from '../../../core/translate/translate.pipe';
import { FormatGenericValidationErrorsPipe } from '../../../shared/pipes/format-generic-validation-errors.pipe';
import { StringListFieldComponent } from '../../../shared/components/string-list-field/string-list-field.component';
import { dataModelDataTypeNameValidators, dataModelDataTypeParameterValidators } from '../../../data-models/validators/data-model-name.validators';
import { DataModelPhysicalDataTypeService } from '../../../data-models/services/data-model-physical-data-type.service';

export interface AdminDataTypeModifyDialogData {
  existingNames: string[];
  dataType?: AdminDataType;
}

@Component({
  selector: 'app-admin-data-type-modify-dialog',
  templateUrl: './admin-data-type-modify-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, MatDialogModule, MatButton, MatFormFieldModule, MatInput,
    MatAutocompleteModule, TranslatePipe, FormatGenericValidationErrorsPipe, StringListFieldComponent],
})
export class AdminDataTypeModifyDialogComponent {
  private data = inject<AdminDataTypeModifyDialogData>(MAT_DIALOG_DATA);
  private dialogRef = inject<MatDialogRef<AdminDataTypeModifyDialogComponent, AdminDataTypeInput>>(MatDialogRef);
  private fb = inject(FormBuilder);
  private physicalDataTypeService = inject(DataModelPhysicalDataTypeService);
  dataType = this.data.dataType;

  formGroup = this.fb.nonNullable.group({
    name: [this.dataType?.name ?? '', dataModelDataTypeNameValidators(this.data.existingNames)],
    parameters: this.fb.nonNullable.control(this.dataType?.parameters.map(parameter => parameter.name) ?? [], dataModelDataTypeParameterValidators()),
    physicalDataType: [this.dataType?.mappings[0]?.physicalDataType ?? '', Validators.maxLength(38)],
    checkConstraint: [this.dataType?.mappings[0]?.checkConstraint ?? ''],
  });

  filteredPhysicalDataTypes = toSignal(this.formGroup.controls.physicalDataType.valueChanges.pipe(
    startWith(this.formGroup.controls.physicalDataType.value),
    map(value => this.physicalDataTypeService.filterCategorizedPhysicalDataTypes('oracle', value)),
  ), { initialValue: [] });

  submit(): void {
    if (this.formGroup.invalid) {
      this.formGroup.markAllAsTouched();
      return;
    }

    const { name, parameters, physicalDataType, checkConstraint } = this.formGroup.getRawValue();

    this.dialogRef.close({
      name,
      parameters: parameters.map(name => ({ name })),
      mappings: physicalDataType.trim() ? [{
        dbSystem: 'oracle', physicalDataType: physicalDataType.trim(), checkConstraint: checkConstraint.trim() || null,
      }] : [],
    });
  }
}
