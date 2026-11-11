package dev.zemco.schemalens.admin

class AdminUserSelfModificationException : RuntimeException("Cannot delete, deactivate or demote your own account")
