package dev.zemco.schemalens.modeling.nodes

class NodeExistsException(name: String) :
    RuntimeException("Node with name '$name' already exists!")
