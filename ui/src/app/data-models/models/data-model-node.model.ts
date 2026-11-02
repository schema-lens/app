export interface DataModelNodeSummary {
  nodeId: number | null;
  name: string;
}

export interface DataModelNode extends DataModelNodeSummary {
  fields: DataModelField[];
}

export interface DataModelField {
  fieldId: number | null;
  name: string;
  typeId: number;
  isPrimaryKey: boolean;
  isNullable: boolean;
  position: number;
  typeParameters: DataModelFieldDataTypeParameter[];
}

export interface DataModelFieldDataTypeParameter {
  name: string;
  value: string | null;
}

export interface DataModelNodeDefinitionUpdate extends DataModelNode {
  edgeFields?: DataModelEdgeFieldEdit[];
}

export interface DataModelEdgeFieldEdit {
  fieldId: number;
  name: string;
  position: number;
}
