package com.aikya.orchestrator.dto.common

open class GridColumnDef {
    var headerName: String? = null
    var field: String? = null
    var type: String? = null
    var filter: String? = null
    var filterParams: GridFilterParams? = null
    var editable: Boolean = false
    var lockPinned: Boolean = false
    var pinned: String? = null
    var cellClass: List<String>? = null
    var headerTooltip: String? = null
    var hide: Boolean = false
    var children: List<GridColumnDef>? = null
    var flex: Int? = null;
    var headerClass: String? = null;
    var width: Int? = null;
}


fun makeChildColumnsRecursively(childItems: List<GridColumnDef>?): List<Map<String, Any?>> {
    if (childItems.isNullOrEmpty()) {
        return listOf()
    }
    val retVal = listOf<Map<String, Any?>>().toMutableList();

    for (child in childItems) {
        val childItem = listOfNotNull(
            ("headerName" to child.headerName).takeIf { !child.headerName.isNullOrEmpty() },
            ("field" to child.field).takeIf { !child.field.isNullOrEmpty() },
            ("type" to child.type).takeIf { !child.type.isNullOrEmpty() },
            ("filter" to child.filter).takeIf { !child.filter.isNullOrEmpty() },
            ("filterParams" to child.filterParams).takeIf { child.filterParams != null },
            ("editable" to child.editable),
            "lockPinned" to child.lockPinned,
            ("pinned" to child.pinned).takeIf { child.pinned.isNullOrEmpty() },
            ("cellClass" to child.cellClass).takeIf { child.cellClass.isNullOrEmpty() },
            ("headerTooltip" to child.headerTooltip).takeIf { child.headerTooltip.isNullOrEmpty() },
            ("flex" to child.flex).takeIf { child.flex != null },
            ("headerClass" to child.headerClass).takeIf { child.headerClass.isNullOrEmpty() },
            ("width" to child.width).takeIf { child.width != null },
        ).toMap().toMutableMap();
        if (!child.children.isNullOrEmpty()) {
            val recChild = makeChildColumnsRecursively(child.children)
            if (recChild.isNotEmpty()) {
                childItem["children"] = recChild
            }
        }
        childItem.toMap().let { column -> retVal.add(column) }
    }
    return retVal;
}