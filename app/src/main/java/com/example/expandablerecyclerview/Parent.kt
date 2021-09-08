package com.example.expandablerecyclerview

data class Parent(val question: String, val answer : String) : ExpandableRecyclerViewAdapter.ExpandableGroup<Child>() {
    override fun getExpandingItems(): List<Child> {
        val list = ArrayList<Child>()
        list.add(Child(answer))
        return list
    }
}