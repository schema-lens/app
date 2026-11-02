package dev.zemco.schemalens.modeling.edges

class DataModelEdgeIdentifyingCycleException :
    RuntimeException("Edge would introduce a cycle in identifying relationships.")
