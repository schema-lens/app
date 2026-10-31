import { DataModelDataType } from '../../data-models/models/data-model-data-type.model';

export type AdminDataType = DataModelDataType & { typeId: number };
export type AdminDataTypeInput = Omit<DataModelDataType, 'typeId'>;
