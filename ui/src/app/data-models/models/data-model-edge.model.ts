export interface DataModelEdge {
  edgeId: number | null;
  modelId: number;
  fromNodeId: number;
  toNodeId: number;
  type: DataModelEdgeType;
  isMandatory: boolean;
  isIdentifying: boolean;
  fields: DataModelEdgeField[];
}

export type DataModelEdgeType = '1:1' | '1:N';

export interface DataModelEdgeField {
  fieldId: number;
  sourceNodeFieldId: number;
  parentFieldId: number;
  name: string;
  position: number;
}
