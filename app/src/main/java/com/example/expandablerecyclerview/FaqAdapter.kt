package com.example.expandablerecyclerview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.expandable_child_item.view.*
import kotlinx.android.synthetic.main.expandable_parent_item.view.*

class FaqAdapter(parents: ArrayList<Parent>) :
    ExpandableRecyclerViewAdapter<Child, Parent, FaqAdapter.PViewHolder, FaqAdapter.CViewHolder>(
        parents, ExpandingDirection.VERTICAL
    ) {
    override fun onCreateParentViewHolder(parent: ViewGroup, viewType: Int): PViewHolder {
        return PViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.expandable_parent_item,
                parent,
                false
            )
        )
    }

    override fun onCreateChildViewHolder(child: ViewGroup, viewType: Int): CViewHolder {
        return CViewHolder(
            LayoutInflater.from(child.context).inflate(
                R.layout.expandable_child_item,
                child,
                false
            )
        )
    }

    override fun onBindParentViewHolder(
        parentViewHolder: PViewHolder,
        expandableType: Parent,
        position: Int
    ) {
        parentViewHolder.itemView.questionTv.text = expandableType.question
    }

    override fun onBindChildViewHolder(
        childViewHolder: CViewHolder,
        expandedType: Child,
        expandableType: Parent,
        position: Int
    ) {
        childViewHolder.itemView.answerTv.text = expandedType.answer
    }

    override fun onExpandedClick(
        expandableViewHolder: PViewHolder,
        expandedViewHolder: CViewHolder,
        expandedType: Child,
        expandableType: Parent
    ) {
    }

    override fun onExpandableClick(
        expandableViewHolder: PViewHolder,
        expandableType: Parent
    ) {
    }

    class PViewHolder(v: View) : ExpandableRecyclerViewAdapter.ExpandableViewHolder(v)
    class CViewHolder(v: View) : ExpandableRecyclerViewAdapter.ExpandedViewHolder(v)
}