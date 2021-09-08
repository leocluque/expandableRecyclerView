package com.example.expandablerecyclerview

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Adaptador ExpandableGroup para visualização de reciclagem precisa de um tipo filho e tipo pai e uma lista pai no construtor
 * para criar uma IU de visualização de lista expansível
 * @param ExpandableType Parent Type (esta é a classe que fornece implementação concreta da Interface ExpandableGroup)
 * @param ExpandedType Tipo filho (pode ser um subtipo de Qualquer objeto)
 */
abstract class ExpandableRecyclerViewAdapter<ExpandedType : Any,
        ExpandableType : ExpandableRecyclerViewAdapter.ExpandableGroup<ExpandedType>,
        PVH : ExpandableRecyclerViewAdapter.ExpandableViewHolder,
        CVH : ExpandableRecyclerViewAdapter.ExpandedViewHolder>

/**
 * Inicializa o adaptador com uma lista de grupos expansíveis e uma direção.
 * @param mExpandableList A lista de grupos expansíveis.
 * @param expansionDirection Um enum para direção.
 */
    (
    private val mExpandableList: ArrayList<ExpandableType>,
    private val expandingDirection: ExpandingDirection
) : RecyclerView.Adapter<PVH>() {

    /**
     * Manter o estado de expansão em toda a listagem.
     * Se a lista estiver totalmente expandida, ela será definida como verdadeira.
     */
    private var expanded = false

    /**
     * Um número inteiro para manter a posição para expansão singular em toda a lista.
     */
    private var lastExpandedPosition = -1

    /**
     * Variável para manter o status do anexo do adaptador para uma visão do reciclador.
     * Se este adaptador estiver conectado a uma visualização do reciclador, esse bit é definido como verdadeiro.
     */
    private var adapterAttached = false

    /**
     * Uma referência à visualização do reciclador à qual este adaptador está conectado no momento.
     */
    private var mParentRecyclerView: RecyclerView? = null

    /**
     * Uma tag para registro
     */
    private val mTAG = "ExpandableGroupAdapter"

    /**
     * Uma classe enum para direções de expansão.
     */
    enum class ExpandingDirection {
        HORIZONTAL,
        VERTICAL
    }

    private fun initializeChildRecyclerView(childRecyclerView: RecyclerView?) {

        if (childRecyclerView != null) {

            val linearLayoutManager = LinearLayoutManager(childRecyclerView.context)

            linearLayoutManager.orientation = if (expandingDirection == ExpandingDirection.VERTICAL)
                LinearLayoutManager.VERTICAL
            else LinearLayoutManager.HORIZONTAL

            childRecyclerView.layoutManager = linearLayoutManager
        }
    }

    override fun getItemCount(): Int {
        return mExpandableList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PVH {
        return onCreateParentView(parent, viewType)
    }

    private fun onCreateParentView(parent: ViewGroup, viewType: Int): PVH {
        val pvh = onCreateParentViewHolder(parent, viewType)

        initializeChildRecyclerView(pvh.containerView.getRecyclerView())

        pvh.containerView.setOnClickListener {
            val position = pvh.adapterPosition
            val expandable = mExpandableList[position]

            if (isSingleExpanded())
                handleSingleExpansion(position)
            else handleExpansion(expandable, position)

            handleLastPositionScroll(position)

            onExpandableClick(pvh, expandable)
        }
        return pvh
    }

    private fun collapseAllGroups() {
        setExpanded(false)
    }

    private fun reverseExpandableState(expandableGroup: ExpandableType) {
        expandableGroup.isExpanded = !expandableGroup.isExpanded
    }

    private fun collapseAllExcept(position: Int) {
        val expandableGroup = mExpandableList[position]
        reverseExpandableState(expandableGroup)
        notifyItemChanged(position)
        if (lastExpandedPosition > -1 && lastExpandedPosition != position) {
            val previousExpandableGroup = mExpandableList[lastExpandedPosition]
            if (previousExpandableGroup.isExpanded) {
                previousExpandableGroup.isExpanded = false
                notifyItemChanged(lastExpandedPosition)
            }
        }
        lastExpandedPosition = position
    }

    private fun handleSingleExpansion(position: Int) {
        if (expanded) {
            collapseAllGroups()
        } else {
            collapseAllExcept(position)
        }
    }

    private fun handleExpansion(expandableGroup: ExpandableType, position: Int) {
        reverseExpandableState(expandableGroup)
        notifyItemChanged(position)
    }

    private fun handleLastPositionScroll(position: Int) {
        if (position == mExpandableList.lastIndex)
            mParentRecyclerView?.smoothScrollToPosition(position)
    }

    override fun onBindViewHolder(holder: PVH, position: Int) {
        setupChildRecyclerView(holder, position)

    }

    private fun setupChildRecyclerView(holder: PVH, position: Int) {
        val expandableGroup = mExpandableList[position]
        val childListAdapter = ChildListAdapter(
            expandableGroup, holder, position
        ) { viewGroup, viewType ->
            onCreateChildViewHolder(viewGroup, viewType)

        }
        val childRecyclerView = holder.containerView.getRecyclerView()
        childRecyclerView?.adapter = childListAdapter

        clickEvent(expandableGroup, holder.containerView)

        onBindParentViewHolder(holder, expandableGroup, position)
    }

    private fun clickEvent(expandableGroup: ExpandableType, containerView: View) {
        val childRecyclerView = containerView.getRecyclerView()

        childRecyclerView?.visibility = if(expandableGroup.isExpanded)
            View.VISIBLE
        else View.GONE

    }

    override fun onAttachedToRecyclerView(recyclerView:RecyclerView) {
        adapterAttached = true

        mParentRecyclerView = recyclerView

        mParentRecyclerView?.layoutManager = LinearLayoutManager(recyclerView.context)

        Log.d(mTAG, "Attached: $adapterAttached")
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        adapterAttached = false
        this.mParentRecyclerView = null
    }

    /**
     * Especifica se você deseja mostrar todos os itens expandidos na IU.
     * @param expandido Um pouco para habilitar / desabilitar a expansão completa.
     * Observação: se qualquer grupo for clicado, a expansão geral será descartada instantaneamente.
     */
    fun setExpanded(expanded: Boolean) {
        this.expanded = expanded
        mExpandableList.applyExpansionState(expanded)
    }

    /**
     * Um método rápido para adicionar um novo grupo à lista.
     * @param expandableGroup O novo grupo.
     * @param expandido Um estado opcional para expansão a ser aplicado (falso por padrão).
     * @param position Uma posição atual opcional na qual inserir o novo grupo no adaptador. (não aplicável por padrão).
     */
    fun addGroup(expandableGroup: ExpandableType, expanded: Boolean = false, position: Int = -1) {
        var atPosition = itemCount

        if (position > atPosition) {
            Log.e(mTAG, "Position to add group exceeds the total group count of $atPosition")
            return
        }

        expandableGroup.isExpanded = expanded

        if (position == -1 || position == atPosition)
            mExpandableList.add(expandableGroup)
        else if (position > -1) {
            mExpandableList.add(position, expandableGroup)
            atPosition = position
        }

        if (adapterAttached)
            notifyItemInserted(atPosition)

        Log.d(mTAG, "Group added at $atPosition")

    }

    /**
     * Um método rápido para remover um grupo da lista.
     * @param position A posição atual do grupo do adaptador.
     */
    fun removeGroup(position: Int) {

        if (position < 0 || position > itemCount) {
            Log.e(mTAG, "Group can't be removed at position $position")
            return
        }

        mExpandableList.removeAt(position)

        if (adapterAttached)
            notifyItemRemoved(position)

        Log.d(mTAG, "Group removed at $position")

    }

    /**
     * Aplica de forma assíncrona o estado de expansão a toda a lista em um thread de segundo plano rapidamente
     * e notifica o adaptador de maneira eficiente para despachar atualizações.
     * @param expansionState O estado de expansão a ser aplicado a todas as listas.
     * Este método pode ser tornado público para funcionar no subconjunto da classe @see ExpandableGroup, declarando-o fora desta classe
     */
    private fun List<ExpandableType>.applyExpansionState(expansionState: Boolean) {

        CoroutineScope(Dispatchers.IO).launch {
            forEach {
                it.isExpanded = expansionState
            }

            launch(Dispatchers.Main) {
                if (adapterAttached)
                    notifyItemRangeChanged(0, itemCount)
            }
        }

    }

    /**
     * Pesquisa hierarquia de visualização para uma instância de RecyclerView
     * @return RecyclerView ou null se não for encontrado
     */
    private fun View.getRecyclerView(): RecyclerView? {
        if (this is ViewGroup && childCount > 0) {
            forEach {
                if (it is RecyclerView) {
                    return it
                }

            }
        }
        Log.e(mTAG, "Recycler View for expanded items not found in parent layout.")
        return null
    }

    private inner class ChildListAdapter(
        private val expandableGroup: ExpandableType,
        private val parentViewHolder: PVH,
        private val position: Int,
        private val onChildRowCreated: (ViewGroup, Int) -> CVH
    ) :
        RecyclerView.Adapter<CVH>() {

        private val mExpandedList = expandableGroup.getExpandingItems()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CVH {
            val cvh = onChildRowCreated(parent, viewType)
            cvh.containerView.setOnClickListener {
                val position = cvh.adapterPosition
                val expandedType = mExpandedList[position]
                onExpandedClick(
                    parentViewHolder,
                    cvh,
                    expandedType,
                    expandableGroup
                )
            }
            return cvh
        }

        override fun getItemCount(): Int {
            return mExpandedList.size
        }

        override fun onBindViewHolder(holder: CVH, position: Int) {
            val expanded = mExpandedList[position]
            onBindChildViewHolder(holder, expanded, expandableGroup, position)
        }

    }

    abstract class ExpandableViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView),
        LayoutContainer

    abstract class ExpandedViewHolder(override val containerView: View) :
        RecyclerView.ViewHolder(containerView),
        LayoutContainer

    abstract class ExpandableGroup<out E> {
        /**
         * retorna uma lista do tipo fornecido a ser usado para expansão.
         */
        abstract fun getExpandingItems(): List<E>

        /**
         * Especifica se você deseja mostrar a IU em formato expandido.
         */
        var isExpanded = false
    }

    abstract fun onCreateParentViewHolder(parent: ViewGroup, viewType: Int): PVH

    abstract fun onBindParentViewHolder(
        parentViewHolder: PVH,
        expandableType: ExpandableType,
        position: Int
    )

    abstract fun onCreateChildViewHolder(child: ViewGroup, viewType: Int): CVH

    abstract fun onBindChildViewHolder(
        childViewHolder: CVH,
        expandedType: ExpandedType,
        expandableType: ExpandableType,
        position: Int
    )

    /**
     * Um método de delegação para evento de clique em visualização expansível.
     * @param expandableViewHolder O titular da visualização para o item expansível.
     * @param expandableType O item expansível.
     */
    abstract fun onExpandableClick(
        expandableViewHolder: PVH,
        expandableType: ExpandableType
    )

    /**
     * Um método de delegação para evento de clique em visualização expandida.
     * @ param expandableViewHolder O titular da visualização para o item expansível.
     * @ param extendedViewHolder O portador de visualização para o item expandido.
     * @ param extendedType O item expandido.
     * @ param expandableType O item expansível.
     */
    abstract fun onExpandedClick(
        expandableViewHolder: PVH,
        expandedViewHolder: CVH,
        expandedType: ExpandedType,
        expandableType: ExpandableType
    )

    /**
     * Especifica se você deseja mostrar um item expandido na IU, no máximo.
     * @return true para habilitar uma expansão filha por vez.
     * retorna falso por padrão.
     */
    protected open fun isSingleExpanded() = false

}