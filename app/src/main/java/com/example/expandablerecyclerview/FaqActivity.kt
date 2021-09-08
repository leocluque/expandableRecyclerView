package com.example.expandablerecyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import kotlinx.android.synthetic.main.activity_faq.*

class FaqActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faq)
        setUpAdapter()
    }

    private fun setUpAdapter() {
        val list = ArrayList<Parent>()
        FaqQuestions.values().forEach {
            list.add(Parent(getString(it.question), getString(it.answer)))
        }
        val adapter = FaqAdapter(list)
        faqRv.adapter = adapter
        faqRv.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        adapter.setExpanded(false)
    }
}